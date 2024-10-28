/**
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

import java.io.File;
import java.io.IOException;

import com.beust.jcommander.JCommander;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MavenRepositoryProvisioner
 *
 * @author Manfred Moser - manfred@simpligility.com
 */
public class MavenRepositoryProvisioner {
    private static final String DASH_LINE = "-----------------------------------";

    private static Configuration config;

    private static Logger logger = LoggerFactory.getLogger("MavenRepositoryProvisioner");

    static File cacheDirectory;

    public static void main(String[] args) {
        JCommander jcommander = null;
        Boolean validConfig = false;
        logger.info(DASH_LINE);
        logger.info(" Maven Repository Provisioner      ");
        logger.info(" simpligility technologies inc.    ");
        logger.info(" http://www.simpligility.com       ");
        logger.info(DASH_LINE);

        StringBuilder usage = new StringBuilder();
        config = new Configuration();
        try {
            jcommander = new JCommander(config);
            jcommander.getUsageFormatter().usage(usage);
            jcommander.parse(args);
            validConfig = true;
        } catch (Exception error) {
            logger.info(usage.toString());
            exitFailure("Problem parsing configuration.");
        }

        if (validConfig) {
            if (config.getHelp()) {
                exitWithHelpMessage(usage.toString());
            } else {
                logger.info(config.getConfigSummary());

                prepareCacheDirectory();

                ArtifactRetriever retriever = retrieveArtifacts();

                MavenRepositoryDeployer deployer = deployArtifacts();

                reportResults(retriever, deployer);
            }
        }
    }

    private static void prepareCacheDirectory() {
        cacheDirectory = new File(config.getCacheDirectory());
        logger.info(" Absolute path: " + cacheDirectory.getAbsolutePath());
        if (cacheDirectory.exists() && cacheDirectory.isDirectory()) {
            logger.info("Detected local cache directory '" + config.getCacheDirectory() + "'.");
            if (!config.hasArtifactsCoordinates()) {
                logger.info("No artifact coordinates specified - using cache directory as source.");
            } else {
                logger.info(
                        "Artifact coordinates specified " + "- removing stale cache directory from prior execution.");
                try {
                    FileUtils.deleteDirectory(cacheDirectory);
                    logger.info(config.getCacheDirectory() + " deleted.");
                } catch (IOException e) {
                    logger.info(config.getCacheDirectory() + " deletion failed");
                    exitFailure("Failed to delete stale cache directory.");
                }
                cacheDirectory = new File(config.getCacheDirectory());
                cacheDirectory.mkdirs();
            }
        } else {
            cacheDirectory = new File(config.getCacheDirectory());
            cacheDirectory.mkdirs();
        }
    }

    private static ArtifactRetriever retrieveArtifacts() {
        ArtifactRetriever retriever = null;
        if (config.hasArtifactsCoordinates()) {
            logger.info("Artifact retrieval starting.");
            retriever = new ArtifactRetriever(cacheDirectory);
            retriever.retrieve(
                    config.getArtifactCoordinates(),
                    config.getSourceUrl(),
                    config.getSourceUsername(),
                    config.getSourcePassword(),
                    config.getIncludeSources(),
                    config.getIncludeJavadoc(),
                    config.getIncludeProvidedScope(),
                    config.getIncludeTestScope(),
                    config.getIncludeRuntimeScope());

            logger.info("Artifact retrieval completed.");
        } else {
            logger.info("Artifact retrieval skipped. ");
        }
        return retriever;
    }

    private static MavenRepositoryDeployer deployArtifacts() {
        logger.info("Artifact deployment starting.");
        MavenRepositoryDeployer helper = new MavenRepositoryDeployer(cacheDirectory, config.getParallelDeploy(),
                                                                     config.getDeployThreads());
        helper.deployToRemote(
                config.getTargetUrl(),
                config.getUsername(),
                config.getPassword(),
                config.getCheckTarget(),
                config.getVerifyOnly());
        logger.info("Artifact deployment completed.");
        return helper;
    }

    private static void reportResults(ArtifactRetriever retriever, MavenRepositoryDeployer deployer) {
        boolean provisioningSuccess;
        StringBuilder provisioningSuccessMessage = new StringBuilder();

        logger.info("Processing Completed.");
        StringBuilder summary = new StringBuilder();
        summary.append("\nProcessing Summary\n").append(DASH_LINE).append("\n");
        summary.append("Configuration:\n").append(config.getConfigSummary());
        if (retriever != null) {
            summary.append(retriever.listSucessfulRetrievals())
                    .append("\n")
                    .append(retriever.listFailedTransfers())
                    .append("\n");

            if (retriever.hasFailures()) {
                provisioningSuccess = false;
                provisioningSuccessMessage.append(retriever.getFailureMessage()).append("\n");
            } else {
                provisioningSuccess = true;
                provisioningSuccessMessage.append("Retrieval completed successfully.\n");
            }
        }

        summary.append(deployer.listSucessfulDeployments()).append("\n");
        summary.append(deployer.listFailedDeployments()).append("\n");
        summary.append(deployer.listSkippedDeployment()).append("\n");
        summary.append(deployer.listPotentialDeployment()).append("\n");

        if (deployer.hasFailure()) {
            provisioningSuccess = false;
            provisioningSuccessMessage.append(deployer.getFailureMessage()).append("\n");
        } else {
            provisioningSuccess = true;
            provisioningSuccessMessage.append("Deployment completed successfully.\n");
        }
        logger.info(summary.toString());

        if (provisioningSuccess) {
            exitSuccess(provisioningSuccessMessage.toString());
        } else {
            exitFailure(provisioningSuccessMessage.toString());
        }
    }

    private static void exitWithHelpMessage(String helpText) {
        logger.info(helpText);
        logger.info("\nIf you need to access the source repository via a proxy server,");
        logger.info("you can configure the standard Java proxy parameters http.proxyHost, ");
        logger.info("http.proxyPort, http.proxyUser and http.proxyPassword. More at ");
        logger.info("https://docs.oracle.com/javase/8/docs/api/java/net/doc-files/net-properties.html");
        exitFailure("Invalid configuration.");
    }

    private static void exitSuccess(String message) {
        logger.info("Exiting: SUCCESS \n" + message);
        System.exit(0);
    }

    private static void exitFailure(String message) {
        logger.info("Exiting: FAILURE \n" + message);
        System.exit(1);
    }
}
