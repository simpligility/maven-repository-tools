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
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
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
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.slf4j.LoggerFactory;

import com.simpligility.maven.Gav;
import com.simpligility.maven.GavUtil;
import com.simpligility.maven.MavenConstants;

import org.slf4j.Logger;

public class MavenRepositoryDeployer
{
    private static Logger logger = LoggerFactory.getLogger( "MavenRepositoryHelper" );

    private static MavenRepositoryDeployer instance;

    public static MavenRepositoryDeployer getInstance( File repositoryPath, Configuration configuration )
    {
        if ( instance == null )
        {
            instance = new MavenRepositoryDeployer( repositoryPath, configuration );
        }
        return instance;
    }

    private File repositoryPath;

    private RepositorySystem system;

    private DefaultRepositorySystemSession session;

    private final String targetUrl;
    private final String username;
    private final String password;
    private final Boolean checkTarget;
    private final Boolean verifyOnly;
    private final int threads;

    public static class DeploymentResult
    {
        private final TreeSet<String> successfulDeploys = new TreeSet<String>();
        private final TreeSet<String> failedDeploys = new TreeSet<String>();
        private final TreeSet<String> potentialDeploys = new TreeSet<String>();

        public DeploymentResult( TreeSet<String> successfulDeploys,
                                 TreeSet<String> failedDeploys,
                                 TreeSet<String> potentialDeploys )
        {
            this.successfulDeploys.addAll( successfulDeploys );
            this.failedDeploys.addAll( failedDeploys );
            this.potentialDeploys.addAll( potentialDeploys );
        }

        public TreeSet<String> getSuccessfulDeploys()
        {
            return successfulDeploys;
        }

        public TreeSet<String> getFailedDeploys()
        {
            return failedDeploys;
        }

        public TreeSet<String> getPotentialDeploys()
        {
            return potentialDeploys;
        }
    }

    private final TreeSet<String> successfulDeploys = new TreeSet<>();

    private final TreeSet<String> failedDeploys = new TreeSet<>();

    private final TreeSet<String> skippedDeploys = new TreeSet<>();

    private final TreeSet<String> potentialDeploys = new TreeSet<>();

    public MavenRepositoryDeployer( File repositoryPath, Configuration configuration )
    {
        this.repositoryPath = repositoryPath;
        this.targetUrl = configuration.getTargetUrl();
        this.username = configuration.getUsername();
        this.password = configuration.getPassword();
        this.checkTarget = configuration.getCheckTarget();
        this.verifyOnly = configuration.getVerifyOnly();
        this.threads = configuration.getDeployThreads();
        initialize();
    }

    private void initialize()
    {
        system = RepositoryHandler.getRepositorySystem();
        session = RepositoryHandler.getRepositorySystemSession( system, repositoryPath );
    }

    public static Collection<File> getLeafDirectories( File repoPath )
    {
        // Using commons-io, if performance or so is a problem it might be worth looking at the Java 8 streams API
        // e.g. http://blog.jooq.org/2014/01/24/java-8-friday-goodies-the-new-new-io-apis/
        // not yet though..
        Collection<File> subDirectories =
                FileUtils.listFilesAndDirs( repoPath, DirectoryFileFilter.DIRECTORY,
                                            VisibleDirectoryFileFilter.DIRECTORY );
        Collection<File> leafDirectories = new ArrayList<File>();
        for ( File subDirectory : subDirectories )
        {
            if ( isLeafVersionDirectory( subDirectory ) && subDirectory != repoPath )
            {
                leafDirectories.add( subDirectory );
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
    private static boolean isLeafVersionDirectory( File subDirectory )
    {
        boolean isLeafVersionDirectory;
        Collection<File> subDirectories =
                FileUtils.listFilesAndDirs( subDirectory,
                                            VisibleDirectoryFileFilter.DIRECTORY,
                                            VisibleDirectoryFileFilter.DIRECTORY );
        // it finds at least itself so have to check for > 1
        isLeafVersionDirectory = subDirectories.size() <= 1;
        return isLeafVersionDirectory;
    }

    public static Collection<File> getPomFiles( File repoPath )
    {
        Collection<File> pomFiles = new ArrayList<>();
        Collection<File> leafDirectories = getLeafDirectories( repoPath );
        for ( File leafDirectory : leafDirectories )
        {
            IOFileFilter fileFilter = new AndFileFilter( new WildcardFileFilter( "*.pom" ),
                                                         new NotFileFilter( new SuffixFileFilter( "sha1" ) ) );
            pomFiles.addAll( FileUtils.listFiles( leafDirectory, fileFilter, null ) );
        }
        return pomFiles;
    }


    public void run()
    {
        Collection<File> leafDirectories = getLeafDirectories( repositoryPath );
        ExecutorService executorService = Executors.newFixedThreadPool( threads );
        try
        {
            List<Future<DeploymentResult>> futures = new ArrayList<>();

            for ( File leafDirectory : leafDirectories )
            {
                String leafAbsolutePath = leafDirectory.getAbsoluteFile()
                                                       .toString();
                int repoAbsolutePathLength = repositoryPath.getAbsoluteFile()
                                                           .toString()
                                                           .length();
                String leafRepoPath = leafAbsolutePath.substring( repoAbsolutePathLength + 1 );

                Gav gav = GavUtil.getGavFromRepositoryPath( leafRepoPath );

                boolean pomInTarget = false;
                if ( checkTarget )
                {
                    pomInTarget = checkIfPomInTarget( targetUrl, username, password, gav );
                }

                if ( pomInTarget )
                {
                    logger.info( "Found POM for {} already in target. Skipping deployment.", gav );
                    addSkippedDeploy( gav.toString() );
                }
                else
                {
                    IOFileFilter fileFilter = new AndFileFilter(
                            new WildcardFileFilter( gav.getArtifactId() + "-" + gav.getVersion() + "*" ),
                            new NotFileFilter( new SuffixFileFilter( "sha1" ) )
                    );
                    Collection<File> artifacts = FileUtils.listFiles(
                            leafDirectory, fileFilter, null
                    );

                    Authentication auth = new AuthenticationBuilder()
                            .addUsername( username )
                            .addPassword( password )
                            .build();

                    RemoteRepository distRepo = new RemoteRepository.Builder(
                            "repositoryIdentifier",
                            "default",
                            targetUrl )
                            .setProxy( ProxyHelper.getProxy( targetUrl ) )
                            .setAuthentication( auth )
                            .build();

                    MavenRepositoryDeploymentCallable deploymentCallable = new MavenRepositoryDeploymentCallable(
                            gav, artifacts, distRepo, verifyOnly, system, session
                    );
                    futures.add( executorService.submit( deploymentCallable ) );
                }
            }

            for ( Future<MavenRepositoryDeployer.DeploymentResult> future : futures )
            {
                try
                {
                    MavenRepositoryDeployer.DeploymentResult result = future.get();
                    synchronized ( this )
                    {
                        successfulDeploys.addAll( result.getSuccessfulDeploys() );
                        failedDeploys.addAll( result.getFailedDeploys() );
                        potentialDeploys.addAll( result.getPotentialDeploys() );
                    }
                }
                catch ( Exception e )
                {
                    logger.error( "Error while getting deployment result", e );
                }
            }
        }
        finally
        {
            executorService.shutdown();
        }
    }

    /**
     * Check if POM file for provided gav can be found in target. Just does
     * a HTTP get of the header and verifies http status OK 200.
     *
     * @param targetUrl url of the target repository
     * @param gav       group artifact version string
     * @return {@code true} if the pom.xml already exists in the target repository
     */
    private boolean checkIfPomInTarget( String targetUrl, String username, String password, Gav gav )
    {
        boolean alreadyInTarget = false;

        String artifactUrl = targetUrl + gav.getRepositoryURLPath() + gav.getPomFilename();
        logger.debug( "Headers for {}", artifactUrl );

        HttpHead httphead;
        try
        {
            httphead = new HttpHead( artifactUrl );
        }
        catch ( Exception e )
        {
            logger.error( "Skipped : {}", artifactUrl, e );
            return true;
        }

        if ( !StringUtils.isEmpty( username ) && !StringUtils.isEmpty( password ) )
        {
            String encoding = java.util.Base64.getEncoder().encodeToString( ( username + ":" + password ).getBytes() );
            httphead.setHeader( "Authorization", "Basic " + encoding );
        }

        try ( CloseableHttpClient httpClient = HttpClientBuilder.create().build() )
        {
            HttpResponse response = httpClient.execute( httphead );
            int statusCode = response.getStatusLine().getStatusCode();
            if ( statusCode == HttpURLConnection.HTTP_OK )
            {
                alreadyInTarget = true;
            }
            else
            {
                logger.debug( "Headers not found HTTP: {}", statusCode );
            }
        }
        catch ( IOException ioe )
        {
            logger.warn( "Could not check target repository for already existing pom.xml.", ioe );
        }
        return alreadyInTarget;
    }


    public String listSucessfulDeployments()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "Sucessful Deployments:\n\n" );
        for ( String artifact : successfulDeploys )
        {
            builder.append( artifact + "\n" );
        }
        return builder.toString();
    }

    public String listFailedDeployments()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "Failed Deployments:\n\n" );
        for ( String artifact : failedDeploys )
        {
            builder.append( artifact + "\n" );
        }

        return builder.toString();
    }

    public String listSkippedDeployment()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "Skipped Deployments (POM already in target):\n\n" );
        for ( String artifact : skippedDeploys )
        {
            builder.append( artifact + "\n" );
        }

        return builder.toString();
    }

    public String listPotentialDeployment()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "Potential Deployments :\n\n" );
        for ( String artifact : potentialDeploys )
        {
            builder.append( artifact + "\n" );
        }

        return builder.toString();
    }

    public static Gav getCoordinates( File pomFile ) throws Exception
    {
        BufferedReader in = new BufferedReader( new FileReader( pomFile ) );
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read( in );
        // get coordinates and take care of inheritance and default
        String g = model.getGroupId();
        if ( StringUtils.isEmpty( g ) )
        {
            g = model.getParent().getGroupId();
        }
        String a = model.getArtifactId();
        if ( StringUtils.isEmpty( a ) )
        {
            a = model.getParent().getArtifactId();
        }
        String v = model.getVersion();
        if ( StringUtils.isEmpty( v ) )
        {
            v = model.getParent().getVersion();
        }
        String p = model.getPackaging();
        if ( StringUtils.isEmpty( p ) )
        {
            p = MavenConstants.JAR;
        }
        Gav gav = new Gav( g, a, v, p );
        return gav;
    }

    public boolean hasFailure()
    {
        return failedDeploys.size() > 0;
    }

    public String getFailureMessage()
    {
        return "Failed to deploy some artifacts.";
    }

    public void addSkippedDeploy( String artifact )
    {
        skippedDeploys.add( artifact );
    }

    public void summarize()
    {
        logger.info( "Summary of Deployment Results:" );
        logger.info( "Successful Deployments {}: {}", successfulDeploys.size(), String.join( ",", successfulDeploys ) );
        logger.info( "Failed Deployments {}: {}", failedDeploys.size(), String.join( ",", failedDeploys ) );
        logger.info( "Skipped Deployments {}: {}", skippedDeploys.size(), String.join( ",", skippedDeploys ) );
        logger.info( "Potential Deployments {}: {}", potentialDeploys.size(), String.join( ",", potentialDeploys ) );
    }
}
