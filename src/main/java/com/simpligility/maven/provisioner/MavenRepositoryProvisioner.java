/**
 * 
 */
package com.simpligility.maven.provisioner;

import java.util.List;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.examples.util.Booter;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

/**
 * @author Manfred Moser
 */
public class MavenRepositoryProvisioner {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		RepositorySystem system = Booter.newRepositorySystem();

		RepositorySystemSession session = Booter
				.newRepositorySystemSession(system);

		Artifact artifact = new DefaultArtifact(
				"org.sonatype.aether:aether-impl:1.13");

		RemoteRepository repo = Booter.newCentralRepository();

		DependencyFilter classpathFlter = DependencyFilterUtils
				.classpathFilter(JavaScopes.COMPILE);

		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
		collectRequest.addRepository(repo);

		DependencyRequest dependencyRequest = new DependencyRequest(
				collectRequest, classpathFlter);

		List<ArtifactResult> artifactResults = null;
		try {
			artifactResults = system.resolveDependencies(
					session, dependencyRequest).getArtifactResults();
		} catch (DependencyResolutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (artifactResults != null) {
		
			for (ArtifactResult artifactResult : artifactResults) {
				System.out.println(artifactResult.getArtifact() + " resolved to "
						+ artifactResult.getArtifact().getFile());
			}
		}

	}

}
