/** 
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

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

import com.simpligility.maven.Gav;
import com.simpligility.maven.MavenConstants;

/**
 * ArtifactRetriever can resolve a dependencies and all transitive dependencies and upstream parent pom's 
 * for a given GAV coordinate and fill a directory with the respective Maven repository containing those components.
 * 
 * @author Manfred Moser <manfred@simpligility.com>
 */
public class ArtifactRetriever
{

    private static Logger logger = LoggerFactory.getLogger( "ArtifactRetriever" );

    private RepositorySystem system;

    private DefaultRepositorySystemSession session;

    private File repositoryPath;

    private RemoteRepository sourceRepository;
    
    private final TreeSet<String> successfulRetrievals = new TreeSet<String>();

    private final TreeSet<String> failedRetrievals = new TreeSet<String>();


    public ArtifactRetriever( File repositoryPath )
    {
        this.repositoryPath = repositoryPath;
        initialize();
    }

    private void initialize()
    {
        system = RepositoryHandler.getRepositorySystem();
        session = RepositoryHandler.getRepositorySystemSession( system, repositoryPath );
    }

    public void retrieve( List<String> artifactCoordinates, String sourceUrl, boolean includeSources,
                          boolean includeJavadoc )
    {
        RemoteRepository.Builder builder = new RemoteRepository.Builder( "central", "default", sourceUrl );
        builder.setProxy( ProxyHelper.getProxy( sourceUrl ) );
        sourceRepository = builder.build();

        getArtifactResults( artifactCoordinates );

        getAdditionalArtifacts( includeSources, includeJavadoc );
    }

    private List<ArtifactResult> getArtifactResults( List<String> artifactCoordinates )
    {

        List<Artifact> artifacts = new ArrayList<Artifact>();
        for ( String artifactCoordinate : artifactCoordinates )
        {
            artifacts.add( new DefaultArtifact( artifactCoordinate ) );
        }

        List<ArtifactResult> artifactResults = new ArrayList<ArtifactResult>();
        for ( Artifact artifact : artifacts )
        {
            DependencyFilter depFilter = DependencyFilterUtils.classpathFilter( JavaScopes.COMPILE );

            CollectRequest collectRequest = new CollectRequest();
            collectRequest.setRoot( new Dependency( artifact, JavaScopes.COMPILE ) );
            collectRequest.addRepository( sourceRepository );

            DependencyRequest dependencyRequest = new DependencyRequest( collectRequest, depFilter );

            try
            {
                DependencyResult resolvedDependencies = system.resolveDependencies( session, dependencyRequest );
                artifactResults.addAll( resolvedDependencies.getArtifactResults() );
                for ( ArtifactResult result : resolvedDependencies.getArtifactResults() )
                {
                    successfulRetrievals.add( result.toString() );
                }
            }
            catch ( DependencyResolutionException e )
            {
                logger.info( "DependencyResolutionException ", e );
                failedRetrievals.add( e.getMessage() );
            }
            catch ( NullPointerException npe )
            {
                logger.info( "NullPointerException resolving dependencies ", npe );
                failedRetrievals.add( npe.getMessage() );
            }
        }

        return artifactResults;
    }

    private void getAdditionalArtifacts( boolean includeSources, boolean includeJavadoc )
    {
        Collection<File> pomFiles = MavenRepositoryHelper.getPomFiles( repositoryPath );
        for ( File pomFile : pomFiles )
        {
            logger.info( "Processing POM file " + pomFile.getAbsolutePath() );
            Gav gav = null;
            try
            {
                gav = MavenRepositoryHelper.getCoordinates( pomFile );
            }
            catch ( Exception e )
            {
                logger.info( "Failed to retrieve gav from " + pomFile.getAbsolutePath() );
            }

            if ( !"pom".equals( gav.getPackaging() ) )
            {
                getMainArtifact( gav );
                if ( includeSources )
                {
                    getArtifact( gav, MavenConstants.SOURCES );
                }
                if ( includeJavadoc )
                {
                    getArtifact( gav, MavenConstants.JAVADOC );
                }
            }
        }
    }

    private void getMainArtifact( Gav gav )
    {
        getArtifact( gav, null );
    }

    private void getArtifact( Gav gav, String classifier )
    {
        Artifact artifact = new DefaultArtifact( gav.getGroupId(), gav.getArtifactId(), classifier, gav.getPackaging(),
                                                 gav.getVersion() );
        // avoid download if we got it locally already? or not bother and just get it again? 
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact( artifact );
        artifactRequest.addRepository( sourceRepository );

        try
        {
            ArtifactResult artifactResult = system.resolveArtifact( session, artifactRequest );
            logger.info( "Retrieved " + artifactResult.getArtifact().getFile() );

            successfulRetrievals.add( artifact.toString() );
        }
        catch ( ArtifactResolutionException e )
        {
            logger.info( "ArtifactResolutionException when retrieving " + classifier );
            failedRetrievals.add( e.getMessage() );
        }
    }

    public String listSucessfulRetrievals()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "Sucessful Retrievals:\n\n" );
        for ( String artifact : successfulRetrievals ) 
        {
            builder.append( artifact + "\n" );
        }
        return builder.toString();
    }

    public String listFailedTransfers()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "Failed Retrievals:\n\n" );
        for ( String artifact : failedRetrievals ) 
        {
            builder.append( artifact + "\n" );
        }
        return builder.toString();
    }

    public boolean hasFailures() 
    {
      return failedRetrievals.size() > 0;
    }

    public String getFailureMessage() 
    {
      return "Failed to retrieve some artifacts.";
    }
}
