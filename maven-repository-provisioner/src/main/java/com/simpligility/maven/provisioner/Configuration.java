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

    @Parameter( names = { "-su", "-sourceUsername" }, description = "Username for the source repository, if required." )
    private String sourceUsername;
    
    @Parameter( names = { "-sp", "-sourcePassword" }, description = "Password for the source repository, if required." )
    private String sourcePassword;

    @Parameter( names = { "-is", "-includeSources" },
                description = "Flag to enable/disable download of sources artifacts.", arity = 1 )
    private Boolean includeSources = true;

    @Parameter( names = { "-ij", "-includeJavadoc" },
                description = "Flag to enable/disable download of javadoc artifacts.", arity = 1 )
    private Boolean includeJavadoc = true;
    
    @Parameter( names = { "-ip", "-includeProvidedScope" },
                description = "Flag to include/exclude provisioning of dependencies with provided scope.", arity = 1 )
    private Boolean includeProvidedScope = false;

    @Parameter( names = { "-it", "-includeTestScope" },
        description = "Flag to include/exclude provisioning of dependencies with test scope.", arity = 1 )
    private Boolean includeTestScope = false;

    @Parameter( names = { "-ir", "-includeRuntimeScope" },
            description = "Flag to include/exclude provisioning of dependencies with runtime scope.", arity = 1 )
    private Boolean includeRuntimeScope = false;

    @Parameter( names = { "-cd", "-cacheDirectory" }, 
                description = "Local directory used as a cache between resolving and deploying or as the "
                    + "source repository that should be transferred." )
    private String cacheDirectory = "local-cache";

    @Parameter( names = { "-ct", "-checkTarget" }, 
                description = "Check target repository before deploying, if target GAV pom exists no deployment will be"
                    + " attempted", arity = 1 )
    private Boolean checkTarget = true;

    @Parameter( names = { "-vo", "-verifyOnly" },
        description = "Verify which artifacts would be deployed only, deployment is skipped and  potential "
            + "deployments of a second execution are logged.", arity = 1  )
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

    public void setSourceUsername( String sourceUsername )
    {
        this.sourceUsername = sourceUsername;
    }
    
    public void seSourcetPassword( String sourcePassword )
    {
        this.sourcePassword = sourcePassword;
    }
    
    public void setIncludeSources( Boolean includeSources )
    {
        this.includeSources = includeSources;
    }

    public void setIncludeJavadoc( Boolean includeJavadoc )
    {
        this.includeJavadoc = includeJavadoc;
    }

    public void setIncludeProvidedScope( Boolean includeProvidedScope )
    {
      this.includeProvidedScope = includeProvidedScope;
    }

    public void setIncludeTestScope( Boolean includeTestScope )
    {
      this.includeTestScope = includeTestScope;
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
        String result;
        if ( !targetUrl.startsWith( "http" ) )
        {
            // if url does not start with http (or https..) we assume it is a file path and convert it with
            result = new File( targetUrl ).toURI().toString();
        }
        else
        {
            // URL needs to end in / so that assembling target urls is correct
            if ( !targetUrl.endsWith( "/" ) )
            {
                result = targetUrl + "/";
            }
            else
            {
                result = targetUrl;
            }
        }
        return result;
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

    public String getSourceUsername()
    {
        return sourceUsername;
    }
    
    public String getSourcePassword()
    {
        return sourcePassword;
    }
    
    public Boolean getIncludeSources()
    {
        return includeSources;
    }

    public Boolean getIncludeJavadoc()
    {
        return includeJavadoc;
    }

    public Boolean getIncludeProvidedScope()
    {
        return includeProvidedScope;
    }
    
    public Boolean getIncludeTestScope()
    {
        return includeTestScope;
    }

    public Boolean getIncludeRuntimeScope()
    {
        return includeRuntimeScope;
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
        .append( "Source: " + getSourceUrl() + "\n" )
        .append( "Target: " + getTargetUrl() + "\n" )
        .append( "Username: " + getUsername() + "\n" );
      if ( this.getPassword() != null ) 
      {
          builder.append( "Password: " + getPassword().replaceAll( ".", "***" ) + "\n" );
      }
      builder.append( "IncludeSources: " + getIncludeSources() + "\n" )
        .append( "IncludeJavadoc: " + getIncludeJavadoc() + "\n" )
        .append( "IncludeProvidedScope: " + getIncludeProvidedScope() + "\n" )
        .append( "IncludeTestScope: " + getIncludeTestScope() + "\n" )
        .append( "IncludeRuntimeScope: " + getIncludeRuntimeScope() + "\n" )
        .append( "Check target: " + getCheckTarget() + "\n" )
        .append( "Verify only: " + getVerifyOnly() + "\n" )
        .append( "Local cache or source repository directory: " + getCacheDirectory() + "\n\n" );
      return builder.toString();
    }
}
