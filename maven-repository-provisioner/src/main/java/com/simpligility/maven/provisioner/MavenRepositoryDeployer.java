/**
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import com.simpligility.maven.Gav;
import com.simpligility.maven.GavPattern;
import com.simpligility.maven.GavUtil;
import com.simpligility.maven.MavenConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenRepositoryDeployer {
    private static Logger logger = LoggerFactory.getLogger("MavenRepositoryHelper");

    private File repositoryPath;

    private RepositorySystem system;

    private DefaultRepositorySystemSession session;

    private final Configuration config;

    private final TreeSet<String> successfulDeploys = new TreeSet<String>();

    private final TreeSet<String> failedDeploys = new TreeSet<String>();

    private final TreeSet<String> skippedDeploys = new TreeSet<String>();

    private final TreeSet<String> potentialDeploys = new TreeSet<String>();

    private final Set<GavPattern> gavPatterns;

    public MavenRepositoryDeployer(File repositoryPath, Configuration configuration) {
        this.repositoryPath = repositoryPath;
        this.config = configuration;
        initialize();
        gavPatterns = loadGavPatternsFromFilterFile(config.getDeployFilterFile());
    }

    /**
     * Default constructor to make unit testing easier
     */
    public MavenRepositoryDeployer(Configuration configuration) {
        gavPatterns = new HashSet<>();
        this.config = configuration;
    }

    private void initialize() {
        system = RepositoryHandler.getRepositorySystem();
        session = RepositoryHandler.getRepositorySystemSession(system, repositoryPath);
        if (config.getParallelDeploy()) {
            session.setConfigProperty("aether.connector.basic.parallelPut", "true");
            session.setConfigProperty("aether.connector.basic.threads", config.getDeployThreads());
        }
    }

    public static Collection<File> getLeafDirectories(File repoPath) {
        // Using commons-io, if performance or so is a problem it might be worth looking at the Java 8 streams API
        // e.g. http://blog.jooq.org/2014/01/24/java-8-friday-goodies-the-new-new-io-apis/
        // not yet though..
        Collection<File> subDirectories = FileUtils.listFilesAndDirs(
                repoPath, DirectoryFileFilter.DIRECTORY, VisibleDirectoryFileFilter.DIRECTORY);
        Collection<File> leafDirectories = new ArrayList<File>();
        for (File subDirectory : subDirectories) {
            if (isLeafVersionDirectory(subDirectory) && subDirectory != repoPath) {
                leafDirectories.add(subDirectory);
            }
        }
        return leafDirectories;
    }

    /**
     * Determine if it is a leaf directory with artifacts in it. Criteria used is that there is no subdirectory.
     *
     * @param subDirectory
     * @return
     */
    private static boolean isLeafVersionDirectory(File subDirectory) {
        boolean isLeafVersionDirectory;
        Collection<File> subDirectories = FileUtils.listFilesAndDirs(
                subDirectory, (IOFileFilter) VisibleDirectoryFileFilter.DIRECTORY, (IOFileFilter)
                        VisibleDirectoryFileFilter.DIRECTORY);
        // it finds at least itself so have to check for > 1
        isLeafVersionDirectory = subDirectories.size() > 1 ? false : true;
        return isLeafVersionDirectory;
    }

    public static Collection<File> getPomFiles(File repoPath) {
        Collection<File> pomFiles = new ArrayList<File>();
        Collection<File> leafDirectories = getLeafDirectories(repoPath);
        for (File leafDirectory : leafDirectories) {
            IOFileFilter fileFilter =
                    new AndFileFilter(new WildcardFileFilter("*.pom"), new NotFileFilter(new SuffixFileFilter("sha1")));
            pomFiles.addAll(FileUtils.listFiles(leafDirectory, fileFilter, null));
        }
        return pomFiles;
    }

    public DeploymentResult deployToRemote() {
        Collection<File> leafDirectories = getLeafDirectories(repositoryPath);

        for (File leafDirectory : leafDirectories) {
            String leafAbsolutePath = leafDirectory.getAbsoluteFile().toString();
            int repoAbsolutePathLength =
                    repositoryPath.getAbsoluteFile().toString().length();
            String leafRepoPath = leafAbsolutePath.substring(repoAbsolutePathLength + 1, leafAbsolutePath.length());

            Gav gav = GavUtil.getGavFromRepositoryPath(leafRepoPath);

            if (!canDeployGav(gav, 10)) {
                logger.info("Skipping deployment of " + gav + " as it is not in the deploy filter file.");
                skippedDeploys.add(gav.toString());
                continue;
            }

            boolean pomInTarget = false;
            if (config.getCheckTarget()) {
                pomInTarget = checkIfPomInTarget(gav);
            }

            if (pomInTarget) {
                logger.info("Found POM for " + gav + " already in target. Skipping deployment.");
                skippedDeploys.add(gav.toString());
            } else {
                // only interested in files using the artifactId-version* pattern
                // don't bother with .sha1 files
                IOFileFilter fileFilter = new AndFileFilter(
                        new WildcardFileFilter(gav.getArtifactId() + "-" + gav.getVersion() + "*"),
                        new NotFileFilter(new SuffixFileFilter("sha1")));
                Collection<File> artifacts = FileUtils.listFiles(leafDirectory, fileFilter, null);

                Authentication auth = new AuthenticationBuilder()
                        .addUsername(config.getUsername())
                        .addPassword(config.getPassword())
                        .build();

                RemoteRepository distRepo = new RemoteRepository.Builder(
                                "repositoryIdentifier", "default", config.getTargetUrl())
                        .setProxy(ProxyHelper.getProxy(config.getTargetUrl()))
                        .setAuthentication(auth)
                        .build();

                DeployRequest deployRequest = new DeployRequest();
                deployRequest.setRepository(distRepo);
                for (File file : artifacts) {
                    String extension;
                    if (file.getName().endsWith("tar.gz")) {
                        extension = "tar.gz";
                    } else {
                        extension = FilenameUtils.getExtension(file.getName());
                    }

                    String baseFileName = gav.getFilenameStart() + "." + extension;
                    String fileName = file.getName();
                    String g = gav.getGroupId();
                    String a = gav.getArtifactId();
                    String v = gav.getVersion();

                    Artifact artifact = null;
                    if (gav.getPomFilename().equals(fileName)) {
                        artifact = new DefaultArtifact(g, a, MavenConstants.POM, v);
                    } else if (gav.getJarFilename().equals(fileName)) {
                        artifact = new DefaultArtifact(g, a, MavenConstants.JAR, v);
                    } else if (gav.getSourceFilename().equals(fileName)) {
                        artifact = new DefaultArtifact(g, a, MavenConstants.SOURCES, MavenConstants.JAR, v);
                    } else if (gav.getJavadocFilename().equals(fileName)) {
                        artifact = new DefaultArtifact(g, a, MavenConstants.JAVADOC, MavenConstants.JAR, v);
                    } else if (baseFileName.equals(fileName)) {
                        artifact = new DefaultArtifact(g, a, extension, v);
                    } else {
                        String classifier = file.getName()
                                .substring(
                                        gav.getFilenameStart().length() + 1,
                                        file.getName().length() - ("." + extension).length());
                        artifact = new DefaultArtifact(g, a, classifier, extension, v);
                    }

                    if (artifact != null) {
                        artifact = artifact.setFile(file);
                        deployRequest.addArtifact(artifact);
                    }
                }

                try {
                    if (config.getVerifyOnly()) {
                        for (Artifact artifact : deployRequest.getArtifacts()) {
                            potentialDeploys.add(artifact.toString());
                        }
                    } else {
                        system.deploy(session, deployRequest);
                        for (Artifact artifact : deployRequest.getArtifacts()) {
                            successfulDeploys.add(artifact.toString());
                        }
                    }
                } catch (Exception e) {
                    logger.info("Deployment failed with " + e.getMessage() + ", artifact might be deployed already.");
                    for (Artifact artifact : deployRequest.getArtifacts()) {
                        failedDeploys.add(artifact.toString());
                    }
                }
            }
        }
        summarize();
        return new DeploymentResult(successfulDeploys, failedDeploys, skippedDeploys, potentialDeploys);
    }

    public void summarize() {
        logger.info("Deployed {} artifacts.", successfulDeploys.size());
        logger.info("Failed to deploy {} artifacts.", failedDeploys.size());
        logger.info("Skipped {} artifacts.", skippedDeploys.size());
        logger.info("Potentially deploy {} artifacts.", potentialDeploys.size());
    }

    /**
     * Check if POM file for provided gav can be found in target. Just does
     * a HTTP get of the header and verifies http status OK 200.
     * @param gav group artifact version string
     * @return {@code true} if the pom.xml already exists in the target repository
     */
    private boolean checkIfPomInTarget(Gav gav) {
        boolean alreadyInTarget = false;

        String artifactUrl = config.getTargetUrl() + gav.getRepositoryURLPath() + gav.getPomFilename();
        logger.debug("Headers for {}", artifactUrl);

        HttpHead httphead;
        try {
            httphead = new HttpHead(artifactUrl);
        } catch (Exception e) {
            logger.error("Skipped : {}", artifactUrl, e);
            return true;
        }

        if (!StringUtils.isEmpty(config.getUsername()) && !StringUtils.isEmpty(config.getUsername())) {
            String encoding = java.util.Base64.getEncoder()
                    .encodeToString((config.getUsername() + ":" + config.getPassword()).getBytes());
            httphead.setHeader("Authorization", "Basic " + encoding);
        }

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = httpClient.execute(httphead);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                alreadyInTarget = true;
            } else {
                logger.debug("Headers not found HTTP: {}", statusCode);
            }
        } catch (IOException ioe) {
            logger.warn("Could not check target repository for already existing pom.xml.", ioe);
        }
        return alreadyInTarget;
    }

    public String listSucessfulDeployments() {
        StringBuilder builder = new StringBuilder();
        builder.append("Sucessful Deployments:\n\n");
        for (String artifact : successfulDeploys) {
            builder.append(artifact + "\n");
        }
        return builder.toString();
    }

    public String listFailedDeployments() {
        StringBuilder builder = new StringBuilder();
        builder.append("Failed Deployments:\n\n");
        for (String artifact : failedDeploys) {
            builder.append(artifact + "\n");
        }

        return builder.toString();
    }

    public String listSkippedDeployment() {
        StringBuilder builder = new StringBuilder();
        builder.append("Skipped Deployments (POM already in target):\n\n");
        for (String artifact : skippedDeploys) {
            builder.append(artifact + "\n");
        }

        return builder.toString();
    }

    public String listPotentialDeployment() {
        StringBuilder builder = new StringBuilder();
        builder.append("Potential Deployments :\n\n");
        for (String artifact : potentialDeploys) {
            builder.append(artifact + "\n");
        }

        return builder.toString();
    }

    public static Gav getCoordinates(File pomFile) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(pomFile));
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(in);
        // get coordinates and take care of inheritance and default
        String g = model.getGroupId();
        if (StringUtils.isEmpty(g)) {
            g = model.getParent().getGroupId();
        }
        String a = model.getArtifactId();
        if (StringUtils.isEmpty(a)) {
            a = model.getParent().getArtifactId();
        }
        String v = model.getVersion();
        if (StringUtils.isEmpty(v)) {
            v = model.getParent().getVersion();
        }
        String p = model.getPackaging();
        if (StringUtils.isEmpty(p)) {
            p = MavenConstants.JAR;
        }
        Gav gav = new Gav(g, a, v, p);
        return gav;
    }

    public boolean hasFailure() {
        return failedDeploys.size() > 0;
    }

    public String getFailureMessage() {
        return "Failed to deploy some artifacts.";
    }

    public Set<GavPattern> loadGavPatternsFromFilterFile(String deployFilterFile) {
        Set<GavPattern> gavPatterns = new HashSet<>();
        if (deployFilterFile == null) {
            return gavPatterns;
        }
        try {
            File file = new File(deployFilterFile);
            if (!file.exists()) {
                return gavPatterns;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                // If the line starts with a "!" then it is an inverse pattern,
                // and we want to ignore this GAV specifically
                boolean inverse = false;
                if (line.startsWith("!")) {
                    line = line.substring(1);
                    inverse = true;
                }
                String[] parts = line.split(":");
                if (parts.length != 4) {
                    logger.warn("Invalid GAV filter line: {}", line);
                    continue;
                }

                String groupId = parts[0];
                String artifactId = parts[1];
                String version = parts[2];

                if (!patternIsValid(groupId, artifactId, version)) {
                    logger.warn("Invalid GAV filter line: {}", line);
                    continue;
                }

                String packagingPattern = parts[3].replace("*", ".*");

                Pattern pattern = Pattern.compile(groupId + ":" + artifactId + ":" + version + ":" + packagingPattern);

                gavPatterns.add(new GavPattern(pattern, inverse));
            }
        } catch (IOException e) {
            logger.error("Failed to load GAVs from filter file", e);
        }

        return gavPatterns;
    }

    public boolean patternIsValid(String groupId, String artifactId, String version) {
        return !groupId.contains("*") && !artifactId.contains("*") && !version.contains("*");
    }

    public boolean canDeployGav(Gav gav, int threadPoolSize) {

        if (gavPatterns == null || gavPatterns.isEmpty()) {
            return true;
        }

        try (GavMatcherExecutor gavMatcherExecutor = new GavMatcherExecutor(threadPoolSize)) {
            List<CompletableFuture<Boolean>> futures = gavMatcherExecutor.evaluateGav(gav, gavPatterns);
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Error evaluating GAV {} against patterns", gav, e);
        }
        return false;
    }

    public static class DeploymentResult {
        private final TreeSet<String> successfulDeploys;
        private final TreeSet<String> failedDeploys;
        private final TreeSet<String> skippedDeploys;
        private final TreeSet<String> potentialDeploys;

        public DeploymentResult(
                TreeSet<String> successfulDeploys,
                TreeSet<String> failedDeploys,
                TreeSet<String> skippedDeploys,
                TreeSet<String> potentialDeploys) {
            this.successfulDeploys = successfulDeploys;
            this.failedDeploys = failedDeploys;
            this.skippedDeploys = skippedDeploys;
            this.potentialDeploys = potentialDeploys;
        }

        public TreeSet<String> getSuccessfulDeploys() {
            return successfulDeploys;
        }

        public TreeSet<String> getFailedDeploys() {
            return failedDeploys;
        }

        public TreeSet<String> getSkippedDeploys() {
            return skippedDeploys;
        }

        public TreeSet<String> getPotentialDeploys() {
            return potentialDeploys;
        }
    }
}
