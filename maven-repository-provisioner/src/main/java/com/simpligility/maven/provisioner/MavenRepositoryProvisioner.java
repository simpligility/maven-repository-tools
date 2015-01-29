/**
 * 
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
public class MavenRepositoryProvisioner {

  private static Configuration config;

  private static Logger logger = LoggerFactory.getLogger("MavenRepositoryProvisioner");; 
  static File localRepo;
  
  public static void main(String[] args) {

    JCommander jcommander = null;
    Boolean validConfig = false;
    StringBuilder usage = new StringBuilder()
    .append("\n\nMaven Repository Provisioner\nsimpligility technologies inc.\n\n");
    
    config = new Configuration();
    try {
      jcommander = new JCommander(config);
      jcommander.usage(usage);
      jcommander.parse(args);
      validConfig = true;
    } catch (Exception error) {
      logger.info(usage.toString());
    }
    
    if (validConfig) {
      
      if ( config.getHelp())
      {
        logger.info(usage.toString());
      } 
      else
      {
        logger.info("Provisioning: " + config.getArtifactCoordinate());
        logger.info("Source: " + config.getSourceUrl());
        logger.info("Target: "  + config.getTargetUrl());
        logger.info("Username: " + config.getUsername());
        logger.info("Password: " + config.getPassword());
        logger.info("IncludeSources:" + config.getIncludeSources());
        logger.info("IncludeJavadoc:" + config.getIncludeJavadoc());
        
        localRepo = new File("local-repo");
   
        ArtifactRetriever retriever = new ArtifactRetriever( localRepo );
        retriever.retrieve( config.getArtifactCoordinates(), config.getSourceUrl(), 
            config.getIncludeSources(), config.getIncludeJavadoc());
        
        MavenRepositoryHelper helper = new MavenRepositoryHelper( localRepo );
        helper.deployToRemote(config.getTargetUrl(), config.getUsername(), config.getPassword());
      }
    }
  }
}
