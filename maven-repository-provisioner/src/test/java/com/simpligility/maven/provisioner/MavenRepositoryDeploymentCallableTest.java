package com.simpligility.maven.provisioner;

import com.simpligility.maven.Gav;
import com.simpligility.maven.MavenConstants;
import com.simpligility.maven.provisioner.helpers.RepositorySystemImpl;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class MavenRepositoryDeploymentCallableTest
{

    private Gav gav;
    private Collection<File> artifacts;
    private RemoteRepository distRepo;
    private RepositorySystem system;
    private DefaultRepositorySystemSession session;
    private MavenRepositoryDeploymentCallable callable;

    @Before
    public void setUp( )
    {
        gav = new Gav( "groupId", "artifactId", "version", "jar" );
        artifacts = Arrays.asList( new File( "artifact1.jar" ), new File( "artifact2.jar" ) );
        distRepo = new RemoteRepository.Builder( "id", "type", "url" ).build();
        system = new RepositorySystemImpl();
        session = new DefaultRepositorySystemSession();
        callable = new MavenRepositoryDeploymentCallable( gav, artifacts, distRepo, false, system, session );
    }

    @Test
    public void testGetExtension( )
    {
        File file1 = new File( "artifact1.jar" );
        File file2 = new File( "archive.tar.gz" );
        File file3 = new File( "document.txt" );

        assertEquals( "jar", callable.getExtension( file1 ) );
        assertEquals( "tar.gz", callable.getExtension( file2 ) );
        assertEquals( "txt", callable.getExtension( file3 ) );
    }

    @Test
    public void testGetArtifact( )
    {
        File pomFile = new File( "artifactId-version.pom" );
        File jarFile = new File( "artifactId-version.jar" );
        File sourceFile = new File( "artifactId-version-sources.jar" );
        File javadocFile = new File( "artifactId-version-javadoc.jar" );
        File customFile = new File( "artifactId-version-custom.ext" );

        assertEquals( new DefaultArtifact( "groupId", "artifactId", MavenConstants.POM, "version" ),
                      callable.getArtifact( gav, pomFile ) );
        assertEquals( new DefaultArtifact( "groupId", "artifactId", MavenConstants.JAR, "version" ),
                      callable.getArtifact( gav, jarFile ) );
        assertEquals( new DefaultArtifact( "groupId",
                                           "artifactId",
                                           MavenConstants.SOURCES,
                                           MavenConstants.JAR,
                                           "version" ), callable.getArtifact( gav, sourceFile ) );
        assertEquals( new DefaultArtifact( "groupId",
                                           "artifactId",
                                           MavenConstants.JAVADOC,
                                           MavenConstants.JAR,
                                           "version" ), callable.getArtifact( gav, javadocFile ) );
        assertEquals( new DefaultArtifact( "groupId", "artifactId", "custom", "ext", "version" ),
                      callable.getArtifact( gav, customFile ) );
    }
}
