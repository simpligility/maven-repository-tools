/** 
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

import java.io.File;
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

    private static Configuration config;

    private static Logger logger = LoggerFactory.getLogger( "MavenRepositoryProvisioner" );

    static File localRepo;

    public static void main( String[] args )
    {

        JCommander jcommander = null;
        Boolean validConfig = false;
        StringBuilder usage = new StringBuilder().append( "\n\nMaven Repository Provisioner)" )
            .append( "\nsimpligility technologies inc.\nhttp://www.simpligility.com\n\n" );

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
                logger.info( "Password: " + config.getPassword() );
                logger.info( "IncludeSources:" + config.getIncludeSources() );
                logger.info( "IncludeJavadoc:" + config.getIncludeJavadoc() );

                localRepo = new File( "local-repo" );

                ArtifactRetriever retriever = new ArtifactRetriever( localRepo );
                retriever.retrieve( config.getArtifactCoordinates(), config.getSourceUrl(), config.getIncludeSources(),
                                    config.getIncludeJavadoc() );

                logger.info( "--------------------------------------------" );
                logger.info( "Artifact retrieval completed." );
                logger.info( "--------------------------------------------" );

                MavenRepositoryHelper helper = new MavenRepositoryHelper( localRepo );
                helper.deployToRemote( config.getTargetUrl(), config.getUsername(), config.getPassword() );
                logger.info( "--------------------------------------------" );
                logger.info( "Artifact deployment completed." );
                logger.info( "--------------------------------------------" );
            }
        }
    }
}
