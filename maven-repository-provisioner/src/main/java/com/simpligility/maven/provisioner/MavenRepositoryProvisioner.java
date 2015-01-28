/**
 * 
 */
package com.simpligility.maven.provisioner;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.maven.repository.internal.ArtifactDescriptorUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.examples.util.Booter;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.transfer.ArtifactTransferException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
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
        
        localRepo = new File("local-repo");
   
        ArtifactRetriever retriever = new ArtifactRetriever( localRepo );
        retriever.retrieve( config.getArtifactCoordinates(), config.getSourceUrl() );
        
        MavenRepositoryHelper helper = new MavenRepositoryHelper( localRepo );
        helper.deployToRemote(config.getTargetUrl(), config.getUsername(), config.getPassword());
      }
    }
  }
}
