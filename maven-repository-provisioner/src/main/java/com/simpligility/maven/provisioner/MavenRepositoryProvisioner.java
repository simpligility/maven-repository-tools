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

  private static final String DASH  = "-";
  private static final String DOT  = ".";
  private static final String POM  = "pom";
  private static final String SOURCES = "sources";
  private static final String JAR = "jar";
  
  private static RepositorySystem system;
  private static DefaultRepositorySystemSession session;
  private static RemoteRepository sourceRepository;

  private static Configuration config;

  private static Logger logger; 
  static File localRepo;
  
  public static void main(String[] args) {

    logger = LoggerFactory.getLogger("MavenRepositoryProvisioner");
    
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
        
        initialize();
        List<ArtifactResult> artifactResults = getArtifactResults();
        
        boolean addSources = true;
        if ( addSources ) 
        {
          getSources(artifactResults);
        }
        
        MavenRepositoryHelper helper = new MavenRepositoryHelper(localRepo);
        helper.deployToRemote(config.getTargetUrl(), config.getUsername(), config.getPassword());
      }
    }
  }

  private static void initialize() {
    localRepo = new File("local-repo");
    system = Booter.newRepositorySystem();
    session = Booter.newRepositorySystemSession( system, localRepo );
    
    sourceRepository = new RemoteRepository.Builder("central", "default",
        config.getSourceUrl()).build();

  }

  private static void getSources(List<ArtifactResult> artifactResults) {
    if (artifactResults != null) {
      for (ArtifactResult artifactResult : artifactResults) {
        logger.info(artifactResult.getArtifact() + " resolved to "
            + artifactResult.getArtifact().getFile());
          getSources(artifactResult);
      }
    }
  }

  private static void getSources(ArtifactResult artifactResult) {
    Artifact mainArtifact = artifactResult.getArtifact();
    Artifact sourceArtifact = new DefaultArtifact(
        mainArtifact.getGroupId(), mainArtifact.getArtifactId(), SOURCES, JAR, mainArtifact.getVersion());

    ArtifactRequest sourceRequest = new ArtifactRequest();
    sourceRequest.setArtifact( sourceArtifact );
    sourceRequest.setRepositories( Booter.newRepositories( system, session ) );

    try {
      ArtifactResult sourceResult = system.resolveArtifact( session, sourceRequest );
      logger.info("Retrieved " + sourceResult.getArtifact().getFile());
    }
    catch (ArtifactResolutionException e) {
      logger.info("ArtifactResolutionException when retrieving source");
    }
  }

  private static List<ArtifactResult> getArtifactResults() {

    List<Artifact> artifacts = new ArrayList<Artifact>();
    List<String> artifactCoordinates = config.getArtifactCoordinates();
    for (String artifactCoordinate : artifactCoordinates) {
      artifacts.add(new DefaultArtifact(artifactCoordinate));
    }

    List<ArtifactResult> artifactResults = new ArrayList<ArtifactResult>();
    for (Artifact artifact : artifacts) {
       DependencyFilter depFilter = DependencyFilterUtils
          .classpathFilter(JavaScopes.COMPILE);

      CollectRequest collectRequest = new CollectRequest();
      collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
      collectRequest.addRepository(sourceRepository);

      DependencyRequest dependencyRequest = new DependencyRequest(collectRequest,
          depFilter);

      try 
      {
        DependencyResult resolvedDependencies = system.resolveDependencies(session, dependencyRequest);
        artifactResults.addAll( resolvedDependencies.getArtifactResults() );
      } 
      catch ( DependencyResolutionException e ) 
      {
        logger.info( "DependencyResolutionException ");
      }
    }
    
    return artifactResults;
  }
}
