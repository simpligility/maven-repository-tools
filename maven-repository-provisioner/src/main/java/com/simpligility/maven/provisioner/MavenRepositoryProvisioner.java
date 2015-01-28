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
  private static Authentication auth = null;
  private static RemoteRepository sourceRepository;

  private static Configuration config;

  private static Logger logger; 
  
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
        
        deployArtifactResults(artifactResults);
      }
    }
  }

  private static void initialize() {
    system = Booter.newRepositorySystem();
    session = Booter.newRepositorySystemSession(system);
    
    String username = config.getUsername();
    String password = config.getPassword();
    if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
      auth = new AuthenticationBuilder().addUsername(username)
          .addPassword(password).build();
    }
    
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
    // TODO Auto-generated method stub
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

  private static void deployArtifactResults(List<ArtifactResult> artifactResults) {
    if (artifactResults != null) {
      for (ArtifactResult artifactResult : artifactResults) {
        logger.info(artifactResult.getArtifact() + " resolved to "
            + artifactResult.getArtifact().getFile());
        try
        {
          deployArtifactResult(artifactResult);
        } 
        catch (ArtifactTransferException ate)
        {
          logger.info("ArtifactTransferException");
        } 
        catch (HttpResponseException hre)
        {
          logger.info("HttpResponseException");
        } 
        catch (DeploymentException de)
        {
          logger.info("DeploymentException");
        }
      }
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

      try {
        artifactResults.addAll(system.resolveDependencies(session, dependencyRequest)
            .getArtifactResults());
      } catch (DependencyResolutionException e) {
        // TODO log error
        e.printStackTrace();
      }
    }
    
    return artifactResults;
  }

  private static void deployArtifactResult(ArtifactResult artifactResult)
      throws HttpResponseException, ArtifactTransferException,
      DeploymentException {
    Artifact mainArtifact = artifactResult.getArtifact();

    Artifact pomArtifact = new SubArtifact(mainArtifact, "", POM);
    String separator = FileSystems.getDefault().getSeparator(); // TBD maybe change to check actual file system used? 
    String pomPath = new StringBuilder()
      .append(mainArtifact.getFile().getParent())
      .append(separator)
      .append(mainArtifact.getArtifactId())
      .append(DASH)
      .append(mainArtifact.getVersion())
      .append(DOT)
      .append(POM)
      .toString();
    File pomFile = new File(pomPath);
    if (pomFile.exists()) {
      pomArtifact = pomArtifact.setFile(pomFile);
    }

    Artifact sourcesArtifact = new SubArtifact(mainArtifact, SOURCES, JAR);
    String sourcesPath = new StringBuilder()
      .append(mainArtifact.getFile().getParent())
      .append(separator)
      .append(mainArtifact.getArtifactId())
      .append(DASH)
      .append(mainArtifact.getVersion())
      .append(DASH)
      .append(SOURCES)
      .append(DOT)
      .append(JAR)
      .toString();
    File sourcesFile = new File(sourcesPath);
    if (sourcesFile.exists()) {
      sourcesArtifact = sourcesArtifact.setFile(sourcesFile);
    }

    RemoteRepository distRepo = new RemoteRepository.Builder(
        "repositoryIdentifier", "default", config.getTargetUrl())
        .setAuthentication(auth).build();

    DeployRequest deployRequest = new DeployRequest();
    deployRequest.addArtifact(mainArtifact);
    if ( pomArtifact.getFile() !=null ) 
    { 
      deployRequest.addArtifact(pomArtifact);
    }
    if ( sourcesArtifact.getFile() != null ) {
      deployRequest.addArtifact(sourcesArtifact);
    }
    deployRequest.setRepository(distRepo);

    system.deploy(session, deployRequest);
  }

}
