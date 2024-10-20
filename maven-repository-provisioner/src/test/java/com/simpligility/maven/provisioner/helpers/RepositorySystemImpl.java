package com.simpligility.maven.provisioner.helpers;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.SyncContext;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallResult;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.resolution.MetadataRequest;
import org.eclipse.aether.resolution.MetadataResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.resolution.VersionRequest;
import org.eclipse.aether.resolution.VersionResolutionException;
import org.eclipse.aether.resolution.VersionResult;

import java.util.Collection;
import java.util.List;

public class RepositorySystemImpl implements RepositorySystem
{
    @Override
    public VersionRangeResult resolveVersionRange( RepositorySystemSession repositorySystemSession,
                                                   VersionRangeRequest versionRangeRequest )
    throws VersionRangeResolutionException
    {
        return null;
    }

    @Override
    public VersionResult resolveVersion( RepositorySystemSession repositorySystemSession,
                                         VersionRequest versionRequest ) throws VersionResolutionException
    {
        return null;
    }

    @Override
    public ArtifactDescriptorResult readArtifactDescriptor( RepositorySystemSession repositorySystemSession,
                                                            ArtifactDescriptorRequest artifactDescriptorRequest )
    throws ArtifactDescriptorException
    {
        return null;
    }

    @Override
    public CollectResult collectDependencies( RepositorySystemSession repositorySystemSession,
                                              CollectRequest collectRequest ) throws DependencyCollectionException
    {
        return null;
    }

    @Override
    public DependencyResult resolveDependencies( RepositorySystemSession repositorySystemSession,
                                                 DependencyRequest dependencyRequest )
    throws DependencyResolutionException
    {
        return null;
    }

    @Override
    public ArtifactResult resolveArtifact( RepositorySystemSession repositorySystemSession,
                                           ArtifactRequest artifactRequest ) throws ArtifactResolutionException
    {
        return null;
    }

    @Override
    public List<ArtifactResult> resolveArtifacts( RepositorySystemSession repositorySystemSession,
                                                  Collection<? extends ArtifactRequest> collection )
    throws ArtifactResolutionException
    {
        return List.of();
    }

    @Override
    public List<MetadataResult> resolveMetadata( RepositorySystemSession repositorySystemSession,
                                                 Collection<? extends MetadataRequest> collection )
    {
        return List.of();
    }

    @Override
    public InstallResult install( RepositorySystemSession repositorySystemSession,
                                  InstallRequest installRequest ) throws InstallationException
    {
        return null;
    }

    @Override
    public DeployResult deploy( RepositorySystemSession repositorySystemSession,
                                DeployRequest deployRequest ) throws DeploymentException
    {
        return null;
    }

    @Override
    public LocalRepositoryManager newLocalRepositoryManager( RepositorySystemSession repositorySystemSession,
                                                             LocalRepository localRepository )
    {
        return null;
    }

    @Override public SyncContext newSyncContext( RepositorySystemSession repositorySystemSession, boolean b )
    {
        return null;
    }

    @Override
    public List<RemoteRepository> newResolutionRepositories( RepositorySystemSession repositorySystemSession,
                                                             List<RemoteRepository> list )
    {
        return List.of();
    }

    @Override
    public RemoteRepository newDeploymentRepository( RepositorySystemSession repositorySystemSession,
                                                     RemoteRepository remoteRepository )
    {
        return null;
    }
}
