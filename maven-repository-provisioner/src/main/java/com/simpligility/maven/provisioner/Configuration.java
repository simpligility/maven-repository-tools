/** 
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.Parameter;

public class Configuration
{
    @Parameter( names = { "-h", "-help" }, help = true, description = "Display the help information." )
    private boolean help;

    @Parameter( names = { "-s", "-sourceUrl" }, 
                description = 
                "URL for the source repository from which artifacts are resolved, "
              + "example for a Nexus install is http://localhost:8081/content/groups/public" )
    private String sourceUrl = "https://repo1.maven.org/maven2";

    @Parameter( names = { "-t", "-targetUrl" }, 
                description = "Folder or URL for the target repository e.g. dist-repo or "
                            + " http://localhost:8081/content/repositories/release", 
                required = true )
    private String targetUrl;

    @Parameter( names = { "-a", "-artifactCoordinates" }, 
                description = "GroupId/ArtifactId/Version (GAV) coordinates of the desired artifacts using the "
                    + "syntax (values in [] are optional): "
                    + "g:a[:extension][:classifier]:v|g:a[:extension][:classifier]:v "
                    + " e.g. org.apache.commons:commons-lang3:3.3.2|com.google.inject:guice:jar:no_aop:3.0", 
                required = false )
    private String artifactCoordinate;

    @Parameter( names = { "-u", "-username" }, description = "Username for the deployment, if required." )
    private String username;

    @Parameter( names = { "-p", "-password" }, description = "Password for the deployment, if required." )
    private String password;

    @Parameter( names = { "-is", "-includeSources" }, 
                description = "Flag to enable/disable download of sources artifacts.", arity = 1 )
    private Boolean includeSources = true;

    @Parameter( names = { "-ij", "-includeJavadoc" }, 
                description = "Flag to enable/disable download of javadoc artifacts.", arity = 1 )
    private Boolean includeJavadoc = true;

    @Parameter( names = { "-cd", "-cacheDirectory" }, 
                description = "Local directory used as a cache between resolving and deploying or as the "
                    + "source repository that should be transferred." )
    private String cacheDirectory = "local-cache";

    @Parameter( names = { "-ct", "-checkTarget" }, 
                description = "Check target repository before deploying, if target GAV pom exists no deployment will be"
                    + " attempted" )
    private Boolean checkTarget = true;

    @Parameter( names = { "-vo", "-verifyOnly" },
        description = "Verify which artifacts would be deployed only, deployment is skipped and  potential "
            + "deployments of a second execution are logged." )
    private Boolean verifyOnly = false;

    public void setSourceUrl( String sourceUrl )
    {
        this.sourceUrl = sourceUrl;
    }

    public void setTargetUrl( String targetUrl )
    {
        this.targetUrl = targetUrl;
    }

    public void setArtifactCoordinate( String artifactCoordinate )
    {
        this.artifactCoordinate = artifactCoordinate;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public void setIncludeSources( Boolean includeSources )
    {
        this.includeSources = includeSources;
    }

    public void setIncludeJavadoc( Boolean includeJavadoc )
    {
        this.includeJavadoc = includeJavadoc;
    }

    public void setCacheDirectory( String cacheDirectory )
    {
        this.cacheDirectory = cacheDirectory;
    }

    public void setCheckTarget( Boolean checkTarget )
    {
        this.checkTarget = checkTarget;
    }

    public void setVerifyOnly ( Boolean verifyOnly )
    {
        this.verifyOnly = verifyOnly;
    }

    public boolean getHelp()
    {
        return help;
    }

    public String getSourceUrl()
    {
        return sourceUrl;
    }

    public String getTargetUrl()
    {

        if ( !targetUrl.startsWith( "http" ) )
        {
            // if url does not start with http (or https..) we assume it is a file path and convert it with
            return new File( targetUrl ).toURI().toString();
        }
        else
        {
            return targetUrl;
        }
    }

    public String getArtifactCoordinate()
    {
        return artifactCoordinate;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public Boolean getIncludeSources()
    {
        return includeSources;
    }

    public Boolean getIncludeJavadoc()
    {
        return includeJavadoc;
    }

    public String getCacheDirectory()
    {
        return cacheDirectory;
    }

    public Boolean getCheckTarget()
    {
        return checkTarget;
    }

    public Boolean getVerifyOnly()
    {
      return verifyOnly;
    }

    public List<String> getArtifactCoordinates()
    {
        List<String> coords = Arrays.asList( artifactCoordinate.split( "\\|" ) );
        return coords;
    }

    public boolean hasArtifactsCoordinates()
    {
      return artifactCoordinate != null && !artifactCoordinate.isEmpty();
    }

    public String getConfigSummary() 
    {
      StringBuilder builder = new StringBuilder();
      builder.append( "\nProvisioning artifacts: " + this.getArtifactCoordinate() + "\n" )
        .append( "Source: " + this.getSourceUrl() + "\n" )
        .append( "Target: " + this.getTargetUrl() + "\n" )
        .append( "Username: " + this.getUsername() + "\n" );
      if ( this.getPassword() != null ) 
      {
          builder.append( "Password: " + this.getPassword().replaceAll( ".", "***" ) + "\n" );
      }
      builder.append( "IncludeSources: " + this.getIncludeSources() + "\n" )
        .append( "IncludeJavadoc: " + this.getIncludeJavadoc() + "\n" )
        .append( "Check target: " + this.getCheckTarget() + "\n" )
        .append( "Verify only: " + this.getVerifyOnly() + "\n" )
        .append( "Local cache or source repository directory: " + this.getCacheDirectory() + "\n\n" );
      return builder.toString();
    }
}
