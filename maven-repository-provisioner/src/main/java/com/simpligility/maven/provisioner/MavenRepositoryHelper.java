package com.simpligility.maven.provisioner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.examples.util.Booter;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

public class MavenRepositoryHelper
{

  private File repositoryPath;

  public MavenRepositoryHelper(File repositoryPath) {
    this.repositoryPath = repositoryPath;
  }

  public void deployToRemote(String targetUrl, String username, String password) {
    // Using commons-io, if performance or so is a problem it might be worth looking at the Java 8 streams API
    // e.g. http://blog.jooq.org/2014/01/24/java-8-friday-goodies-the-new-new-io-apis/
    // not yet though.. 
    
    RepositorySystem system = Booter.newRepositorySystem();
    DefaultRepositorySystemSession session = Booter.newRepositorySystemSession( system, repositoryPath );
    
    Collection<File> subDirectories = FileUtils.listFilesAndDirs( repositoryPath, (IOFileFilter) DirectoryFileFilter.DIRECTORY, TrueFileFilter.INSTANCE);
    Collection<File> leafDirectories = new ArrayList<File>();
    for ( File subDirectory : subDirectories ) 
    {
      if ( isLeafVersionDirectory(subDirectory) )
      {
        
        leafDirectories.add( subDirectory );
      }
    }
    for ( File leafDirectory : leafDirectories )
    {
      String leafAbsolutePath = leafDirectory.getAbsoluteFile().toString();
      int repoAbsolutePathLength = repositoryPath.getAbsoluteFile().toString().length();
      String leafRepoPath = leafAbsolutePath.substring(repoAbsolutePathLength + 1, leafAbsolutePath.length());
      
      
      System.out.println( " leafRepoPath " + leafRepoPath);
      Gav gav = GavUtil.getGavFromRepositoryPath(leafRepoPath);

      // only interested in files using the artifactId-version* pattern
      // don't bother with .sha1 files
      IOFileFilter fileFilter = 
          new AndFileFilter(
              new WildcardFileFilter(gav.getArtifactId() + "-" + gav.getVersion() + "*"),
              new NotFileFilter(new SuffixFileFilter("sha1"))
          );
      Collection<File> artifacts = FileUtils.listFiles(leafDirectory, fileFilter, null);
      
      Authentication auth = new AuthenticationBuilder().addUsername(username)
            .addPassword(password).build();
      
      RemoteRepository distRepo = new RemoteRepository.Builder(
          "repositoryIdentifier", "default", targetUrl)
          .setAuthentication(auth).build();


      DeployRequest deployRequest = new DeployRequest();
      deployRequest.setRepository(distRepo);
      for ( File file : artifacts) 
      {
        System.out.println( "Processing file " + file.getAbsolutePath());
        String extension;
        if ( file.getName().endsWith("tar.gz") )
        {
          extension = "tar.gz";
        }
        else 
        {
          extension = FilenameUtils.getExtension(file.getName());
        }
        
        Artifact artifact = null;
        if ( gav.getPomFilename().equals( file.getName() ) )
        {
          artifact = new DefaultArtifact(gav.getGroupdId(), gav.getArtifactId(), "pom" , gav.getVersion());
        } 
        else if ( gav.getJarFilename().equals( file.getName() ) ) 
        {
          artifact = new DefaultArtifact(gav.getGroupdId(), gav.getArtifactId(), "jar" , gav.getVersion());
        } 
        else if ( gav.getSourceFilename().equals( file.getName() ) ) 
        {
          artifact = new DefaultArtifact(gav.getGroupdId(), gav.getArtifactId(), "sources", "jar" , gav.getVersion());
        } 
        else if ( gav.getJavadocFilename().equals( file.getName() ) ) 
        {
          artifact = new DefaultArtifact(gav.getGroupdId(), gav.getArtifactId(), "javadoc", "jar" , gav.getVersion());
        } 
        else 
        {
          String classifier = file.getName().substring( 
              gav.getFilenameStart().length(), file.getName().length() - extension.length()
              );
          System.out.println( "classifier " + classifier );
        }
        
        if (artifact != null) 
        {
          artifact = artifact.setFile(file);
          deployRequest.addArtifact(artifact);
        }

      }
      
      try {
        system.deploy(session, deployRequest);
      }
      catch (DeploymentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * Determine if it is a leaf directory with artifacts in it. Criteria used is that there is no subdirectory.
   * @param subDirectory
   * @return
   */
  private boolean isLeafVersionDirectory(File subDirectory) 
  {
    Collection<File> subDirectories = FileUtils.listFilesAndDirs( subDirectory, (IOFileFilter) DirectoryFileFilter.DIRECTORY, TrueFileFilter.INSTANCE);
    // it finds at least itself... 
    if ( subDirectories.size() > 1 ) {
      return false;
    }
    else 
    {
      return true;
    }
  }


}

