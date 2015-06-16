/** 
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
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

/**
 * ArtifactRetriever can resolve a depenedencies and all transitive dependencies and upstream parent pom's 
 * for a given GAV coordinate and fill a directory with the respective Maven repository containing those components.
 * 
 * @author Manfred Moser <manfred@simpligility.com>
 */
public class ArtifactRetriever
{

    private static final String JAVADOC = "javadoc";

    private static final String SOURCES = "sources";

    private static final String POM = "pom";

    private static final String JAR = "jar";

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

        List<ArtifactResult> artifactResults = getArtifactResults( artifactCoordinates );

        if ( includeSources )
        {
            getSources( artifactResults );
        }
        if ( includeJavadoc )
        {
            getJavadoc( artifactResults );
        }

    }

    private void getSources( List<ArtifactResult> artifactResults )
    {
        getArtifactsWithClassifier( artifactResults, SOURCES );
    }

    private void getJavadoc( List<ArtifactResult> artifactResults )
    {
        getArtifactsWithClassifier( artifactResults, JAVADOC );
    }

    private void getArtifactsWithClassifier( List<ArtifactResult> artifactResults, String classifier )
    {
        if ( artifactResults != null && StringUtils.isNotBlank( classifier ) )
        {
            for ( ArtifactResult artifactResult : artifactResults )
            {
                Artifact mainArtifact = artifactResult.getArtifact();
                if ( isValidRequest( mainArtifact, classifier ) )
                {
                    Artifact classifierArtifact =
                        new DefaultArtifact( mainArtifact.getGroupId(), mainArtifact.getArtifactId(), classifier, JAR,
                                             mainArtifact.getVersion() );

                    ArtifactRequest classifierRequest = new ArtifactRequest();
                    classifierRequest.setArtifact( classifierArtifact );
                    classifierRequest.addRepository( sourceRepository );

                    try
                    {
                        ArtifactResult classifierResult = system.resolveArtifact( session, classifierRequest );
                        logger.info( "Retrieved " + classifierResult.getArtifact().getFile() );
                        
                        successfulRetrievals.add( classifierArtifact.toString() );
                    }
                    catch ( ArtifactResolutionException e )
                    {
                        logger.info( "ArtifactResolutionException when retrieving " + classifier );
                     // TBD add to logging
                    }
                }
            }
        }
    }

    /**
     * Determine if a request for a classifier artifact is valid. E.g. javadoc and source for extension pom is deemed 
     * not valid, but default is valid.
     * @param mainArtifact
     * @param classifier
     * @return
     */
    private boolean isValidRequest( Artifact mainArtifact, String classifier )
    {
        boolean isValidRequest = true;
        String extension = mainArtifact.getExtension();
        
        if ( POM.equalsIgnoreCase( extension ) && classifier.endsWith( JAVADOC ) )
        {
            isValidRequest = false;
            logger.info( "Skipping retrieval of javadoc for pom extension" );
        } 
        else if ( POM.equalsIgnoreCase( extension ) && classifier.endsWith( SOURCES ) )
        {
            isValidRequest = false;
            logger.info( "Skipping retrieval of sources for pom extension" );
        }
        return isValidRequest;
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
                // TBD hook up logging 
                //failedRetrievals.add( e );
            }
            catch ( NullPointerException npe )
            {
                logger.info( "NullPointerException resolving dependencies ", npe );
                // TBD hook up logging 
                //failedRetrievals.add( e );
            }
        }

        return artifactResults;
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
}
