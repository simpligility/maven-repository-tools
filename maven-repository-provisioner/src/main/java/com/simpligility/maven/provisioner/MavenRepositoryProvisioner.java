/** 
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

/**
 * MavenRepositoryProvisioner
 * 
 * @author Manfred Moser <manfred@simpligility.com>
 */
public class MavenRepositoryProvisioner
{
    private static final String DASH_LINE = "-----------------------------------";

    private static Configuration config;

    private static Logger logger = LoggerFactory.getLogger( "MavenRepositoryProvisioner" );

    static File cacheDirectory;

    public static void main( String[] args )
    {

        JCommander jcommander = null;
        Boolean validConfig = false;
        logger.info( DASH_LINE );
        logger.info( " Maven Repository Provisioner      " );
        logger.info( " simpligility technologies inc.    " );
        logger.info( " http://www.simpligility.com       " );
        logger.info( DASH_LINE );
        
        StringBuilder usage = new StringBuilder();
        config = new Configuration();
        try
        {
            jcommander = new JCommander( config );
            jcommander.usage( usage );
            jcommander.parse( args );
            validConfig = true;
        }
        catch ( Exception error )
        {
            logger.info( usage.toString() );
        }

        if ( validConfig )
        {

            if ( config.getHelp() )
            {
                logger.info( usage.toString() );
            }
            else
            {
                logger.info( "Provisioning: " + config.getArtifactCoordinate() );
                logger.info( "Source: " + config.getSourceUrl() );
                logger.info( "Target: " + config.getTargetUrl() );
                logger.info( "Username: " + config.getUsername() );
                if ( config.getPassword() != null ) 
                {
                    logger.info( "Password: " + config.getPassword().replaceAll( ".", "***" ) );
                }
                logger.info( "IncludeSources:" + config.getIncludeSources() );
                logger.info( "IncludeJavadoc:" + config.getIncludeJavadoc() );
                logger.info( "Local cache directory: " + config.getCacheDirectory() );

                cacheDirectory = new File( config.getCacheDirectory() );
                if ( cacheDirectory.exists() && cacheDirectory.isDirectory() ) 
                {
                    logger.info( "Detected local cache directory '" + config.getCacheDirectory() 
                                 + "' from prior execution." );
                    try
                    {
                        FileUtils.deleteDirectory( cacheDirectory );
                        logger.info( config.getCacheDirectory() + " deleted." );
                    }
                    catch ( IOException e )
                    {
                        logger.info( config.getCacheDirectory() + " deletion failed" );
                    }
                    cacheDirectory = new File( config.getCacheDirectory() );
                }

                ArtifactRetriever retriever = new ArtifactRetriever( cacheDirectory );
                retriever.retrieve( config.getArtifactCoordinates(), config.getSourceUrl(), config.getIncludeSources(),
                                    config.getIncludeJavadoc() );

                logger.info( "Artifact retrieval completed." );

                MavenRepositoryHelper helper = new MavenRepositoryHelper( cacheDirectory );
                helper.deployToRemote( config.getTargetUrl(), config.getUsername(), config.getPassword(), 
                                       config.getCheckTarget() );
                logger.info( "Artifact deployment completed." );

                logger.info( "Processing Completed." );

                logger.info( "\nProcessing Summary\n"
                             + DASH_LINE + "\n"
                             + retriever.listSucessfulRetrievals() + "\n"
                             + retriever.listFailedTransfers() + "\n"
                             + helper.listSucessfulDeployments() + "\n"
                             + helper.listFailedDeployments() + "\n"
                             + helper.listSkippedDeployment() + "\n" );
            }
        }
    }
}
