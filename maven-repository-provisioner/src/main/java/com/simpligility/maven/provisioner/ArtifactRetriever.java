package com.simpligility.maven.provisioner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
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

  public void retrieve(List<String> artifactCoordinates, String sourceUrl, boolean includeSources, boolean includeJavadoc) {
    sourceRepository = new RemoteRepository.Builder( "central", "default", sourceUrl ).build();
    
    List<ArtifactResult> artifactResults = getArtifactResults( artifactCoordinates);
    
    if ( includeSources )
    {
      getSources( artifactResults );
    }
    if ( includeJavadoc) 
    {
      getJavadoc( artifactResults );
    }
    
  }

  private  void getSources(List<ArtifactResult> artifactResults) {
    getArtifactsWithClassifier( artifactResults, "sources");
  }

  private void getJavadoc(List<ArtifactResult> artifactResults) {
    getArtifactsWithClassifier( artifactResults, "javadoc");
  }

  private void getArtifactsWithClassifier(List<ArtifactResult> artifactResults, String classifier) {
    if (artifactResults != null) {
      for (ArtifactResult artifactResult : artifactResults) {
          Artifact mainArtifact = artifactResult.getArtifact();
          Artifact classifierArtifact = new DefaultArtifact(
              mainArtifact.getGroupId(), mainArtifact.getArtifactId(), classifier, "jar", mainArtifact.getVersion());

          ArtifactRequest classifierRequest = new ArtifactRequest();
          classifierRequest.setArtifact( classifierArtifact );
          classifierRequest.addRepository( sourceRepository );

          try {
            ArtifactResult classifierResult = system.resolveArtifact( session, classifierRequest );
            logger.info("Retrieved " + classifierResult.getArtifact().getFile());
          }
          catch (ArtifactResolutionException e) {
            logger.info("ArtifactResolutionException when retrieving " + classifier);
          }
      }
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
