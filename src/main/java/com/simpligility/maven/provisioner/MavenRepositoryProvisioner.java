/**
 * 
 */
package com.simpligility.maven.provisioner;

import java.io.File;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
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
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.transfer.ArtifactTransferException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import com.beust.jcommander.JCommander;

/**
 * MavenRepositoryProvisioner
 * 
 * @author Manfred Moser <manfred@simpligility.com>
 */
public class MavenRepositoryProvisioner {

  private static RepositorySystem system;
  private static RepositorySystemSession session;

  private static Configuration config;

  public static void main(String[] args) {

    JCommander jcommander = null;
    Boolean validConfig = false;
    
    config = new Configuration();
    try {
      jcommander = new JCommander(config, args);
      validConfig = true;
    } catch (Exception e) {
      jcommander.usage();
    }
    
    if (validConfig) {
      system = Booter.newRepositorySystem();
      session = Booter.newRepositorySystemSession(system);
  
      Artifact artifact = new DefaultArtifact(config.getArtifactCoordinate());
  
      RemoteRepository repo = new RemoteRepository.Builder("central", "default",
          config.getSourceUrl()).build();
  
      DependencyFilter depFilter = DependencyFilterUtils
          .classpathFilter(JavaScopes.COMPILE);
  
      CollectRequest collectRequest = new CollectRequest();
      collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
      collectRequest.addRepository(repo);
  
      DependencyRequest dependencyRequest = new DependencyRequest(collectRequest,
          depFilter);
  
      List<ArtifactResult> artifactResults = null;
      try {
        artifactResults = system.resolveDependencies(session, dependencyRequest)
            .getArtifactResults();
      } catch (DependencyResolutionException e) {
        // TODO log error
        e.printStackTrace();
      }
      if (artifactResults != null) {
  
        for (ArtifactResult artifactResult : artifactResults) {
          System.out.println(artifactResult.getArtifact() + " resolved to "
              + artifactResult.getArtifact().getFile());
          try {
            deployArtifactResult(artifactResult);
          } catch (ArtifactTransferException ate) {
            System.out.println("ate");
          } catch (HttpResponseException hre) {
            System.out.println("hre");
          } catch (DeploymentException de) {
            System.out.println("de");
          }
        }
      }
    }
  }

  private static void deployArtifactResult(ArtifactResult artifactResult)
      throws HttpResponseException, ArtifactTransferException,
      DeploymentException {
    Artifact jarArtifact = artifactResult.getArtifact();

    Artifact pomArtifact = new SubArtifact(jarArtifact, "", "pom");
    pomArtifact = pomArtifact.setFile(new File("pom.xml"));

    RemoteRepository distRepo = new RemoteRepository.Builder(
        "repositoryIdentifier", "default", config.getTargetUrl())
        .setAuthentication(
            new AuthenticationBuilder().addUsername("admin")
                .addPassword("admin123").build()).build();

    DeployRequest deployRequest = new DeployRequest();
    deployRequest.addArtifact(jarArtifact).addArtifact(pomArtifact);
    deployRequest.setRepository(distRepo);

    system.deploy(session, deployRequest);
  }

}
