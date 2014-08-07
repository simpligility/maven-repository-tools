package com.simpligility.maven.provisioner;

import java.io.File;

import com.beust.jcommander.Parameter;

public class Configuration {
  
  @Parameter(names = "-help", help = true)
  private boolean help;

  
  @Parameter(names = {"-s", "-sourceUrl" },
             description = "URL for the source repository from which artifacts "
                 + "are resolved, defaults to https://repo1.maven.org/maven2, "
                 + "example for a Nexus isntall is "
                 + "http://localhost:8081/nexus/content/groups/public  "
             )
  private String sourceUrl = "https://repo1.maven.org/maven2";

  @Parameter(names = {"-t", "-targetUrl"}, 
             description = "Folder or URL for the target repository e.g. dist-repo "
                 + "or http://localhost:8081/nexus/content/repositories/release", 
             required = true)
  private String targetUrl; 

  @Parameter(names = {"-a", "-artifactCoordinates"}, 
             description = "GAV coordinate of the desired artifact in the "
                 + "syntax groupId:artifactId:version e.g. "
                 + "org.apache.commons:commons-lang3:3.3.2",
             required = true) 
  private String artifactCoordinate;
  
  @Parameter(names = {"-u", "-username"},
             description = "Username for the deployment, if required.")
  private String username;
  
  @Parameter(names = {"-p", "-password"},
  description = "Password for the deployment, if required.")
  private String password;

  public String getSourceUrl() {
    return sourceUrl;
  }
  public String getTargetUrl() {
    
    if (!targetUrl.startsWith("http")) {
      //if url does not start with http (or https..) we assume it is a file path and convert it with 
      return new File( targetUrl ).toURI().toString();
    } else {
      return targetUrl;
    }
    
  }
  public String getArtifactCoordinate() {
    return artifactCoordinate;
  }
  
  public String getUsername() {
    return username;
  }
  
  public String getPassword() {
    return password;
  }
}
