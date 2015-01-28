package com.simpligility.maven.provisioner;

public class GavUtil
{

  public static Gav getGavFromRepositoryPath(String leafRepoPath) {

    int versionStartSlash = leafRepoPath.lastIndexOf( '/' );
    String version = leafRepoPath.substring( versionStartSlash + 1 , leafRepoPath.length() );

    String gaPath = leafRepoPath.substring( 0 , versionStartSlash );
    int gaStartSlash = gaPath.lastIndexOf( '/' );
    String artifactId = gaPath.substring(  gaStartSlash + 1, gaPath.length() );
    
    String gPath = gaPath.substring( 0 , gaStartSlash );
    String groupId = gPath.replace( '/', '.' );
    
    //String artifactId = leafRepoPath.substring( gEndPos + 1, aEndPos );
    System.out.println( " groupId " + groupId);
    System.out.println( " artifactId " + artifactId);
    System.out.println( " version " + version);
    return new Gav(groupId, artifactId, version);
  }

}
