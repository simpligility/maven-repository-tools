package com.simpligility.maven.provisioner;

import com.simpligility.maven.Gav;
import com.simpligility.maven.MavenConstants;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.repository.RemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

public class MavenRepositoryDeploymentCallable implements Callable<MavenRepositoryDeployer.DeploymentResult>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( "MavenRepositoryDeploymentCallable" );

    private final RemoteRepository distRepo;
    private final Collection<File> artifacts;
    private final Gav gav;
    private final boolean verifyOnly;
    private final RepositorySystem system;
    private final DefaultRepositorySystemSession session;

    private final Set<String> successfulDeploys = Collections.synchronizedSet( new TreeSet<>() );
    private final Set<String> failedDeploys = Collections.synchronizedSet( new TreeSet<>() );
    private final Set<String> potentialDeploys = Collections.synchronizedSet( new TreeSet<>() );

    public MavenRepositoryDeploymentCallable( Gav gav,
                                              Collection<File> artifacts,
                                              RemoteRepository distRepo,
                                              boolean verifyOnly,
                                              RepositorySystem system,
                                              DefaultRepositorySystemSession session )
    {
        this.gav = gav;
        this.artifacts = artifacts;
        this.distRepo = distRepo;
        this.verifyOnly = verifyOnly;
        this.system = system;
        this.session = session;
    }

    @Override
    public MavenRepositoryDeployer.DeploymentResult call()
    {
        DeployRequest deployRequest = createDeployRequest();
        try
        {
            if ( verifyOnly )
            {
                addPotentialDeploys( deployRequest );
            }
            else
            {
                deployArtifacts( deployRequest );
            }
        }
        catch ( Exception e )
        {
            handleDeploymentFailure( deployRequest, e );
        }
        return new MavenRepositoryDeployer.DeploymentResult( new TreeSet<>( successfulDeploys ),
                                                             new TreeSet<>( failedDeploys ),
                                                             new TreeSet<>( potentialDeploys ) );
    }

    private DeployRequest createDeployRequest()
    {
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setRepository( distRepo );
        for ( File file : artifacts )
        {
            Artifact artifact = getArtifact( gav, file );
            if ( artifact != null )
            {
                artifact = artifact.setFile( file );
                deployRequest.addArtifact( artifact );
            }
        }
        return deployRequest;
    }

    private void addPotentialDeploys( DeployRequest deployRequest )
    {
        for ( Artifact artifact : deployRequest.getArtifacts() )
        {
            potentialDeploys.add( artifact.toString() );
        }
    }

    private void deployArtifacts( DeployRequest deployRequest ) throws Exception
    {
        system.deploy( session, deployRequest );
        for ( Artifact artifact : deployRequest.getArtifacts() )
        {
            successfulDeploys.add( artifact.toString() );
        }
    }

    private void handleDeploymentFailure( DeployRequest deployRequest, Exception e )
    {
        LOGGER.warn( "Deployment failed with {}, artifact might be deployed already.", e.getMessage() );
        for ( Artifact artifact : deployRequest.getArtifacts() )
        {
            failedDeploys.add( artifact.toString() );
        }
    }

    public Artifact getArtifact( Gav gav, File file )
    {
        String extension = getExtension( file );
        String baseFileName = gav.getFilenameStart() + "." + extension;
        String fileName = file.getName();
        String g = gav.getGroupId();
        String a = gav.getArtifactId();
        String v = gav.getVersion();

        if ( gav.getPomFilename().equals( fileName ) )
        {
            return new DefaultArtifact( g, a, MavenConstants.POM, v );
        }
        else if ( gav.getJarFilename().equals( fileName ) )
        {
            return new DefaultArtifact( g, a, MavenConstants.JAR, v );
        }
        else if ( gav.getSourceFilename().equals( fileName ) )
        {
            return new DefaultArtifact( g, a, MavenConstants.SOURCES, MavenConstants.JAR, v );
        }
        else if ( gav.getJavadocFilename().equals( fileName ) )
        {
            return new DefaultArtifact( g, a, MavenConstants.JAVADOC, MavenConstants.JAR, v );
        }
        else if ( baseFileName.equals( fileName ) )
        {
            return new DefaultArtifact( g, a, extension, v );
        }
        else
        {
            String classifier = file.getName()
                                    .substring( gav.getFilenameStart().length() + 1,
                                                file.getName().length() - ( "." + extension ).length() );
            return new DefaultArtifact( g, a, classifier, extension, v );
        }
    }

    public String getExtension( File file )
    {
        if ( file.getName().endsWith( "tar.gz" ) )
        {
            return "tar.gz";
        }
        else
        {
            return FilenameUtils.getExtension( file.getName() );
        }
    }
}
