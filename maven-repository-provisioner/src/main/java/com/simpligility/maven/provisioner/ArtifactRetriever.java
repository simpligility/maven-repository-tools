package com.simpligility.maven.provisioner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.examples.util.Booter;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactRetriever
{

  private static Logger logger = LoggerFactory.getLogger("ArtifactRetriever"); 
  
  private RepositorySystem system;
  private DefaultRepositorySystemSession session;
  private File localRepo;
  private RemoteRepository sourceRepository;

  public ArtifactRetriever(File localRepo) {
    this.localRepo = localRepo;
    initialize();

  }
  
  private void initialize() {
    system = Booter.newRepositorySystem();
    session = Booter.newRepositorySystemSession( system, localRepo );
  }

  public void retrieve(List<String> artifactCoordinates, String sourceUrl) {
    sourceRepository = new RemoteRepository.Builder( "central", "default", sourceUrl ).build();
    
    List<ArtifactResult> artifactResults = getArtifactResults( artifactCoordinates);
    
    // make configurable
    boolean addSources = true;
    if ( addSources )
    {
      getSources(artifactResults);
    }
    
  }

  private  void getSources(List<ArtifactResult> artifactResults) {
    if (artifactResults != null) {
      for (ArtifactResult artifactResult : artifactResults) {
        logger.info(artifactResult.getArtifact() + " resolved to "
            + artifactResult.getArtifact().getFile());
          getSources(artifactResult);
      }
    }
  }

  private void getSources(ArtifactResult artifactResult) {
    Artifact mainArtifact = artifactResult.getArtifact();
    Artifact sourceArtifact = new DefaultArtifact(
        mainArtifact.getGroupId(), mainArtifact.getArtifactId(), "sources", "jar", mainArtifact.getVersion());

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

  private List<ArtifactResult> getArtifactResults(List<String> artifactCoordinates) {

    List<Artifact> artifacts = new ArrayList<Artifact>();
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
