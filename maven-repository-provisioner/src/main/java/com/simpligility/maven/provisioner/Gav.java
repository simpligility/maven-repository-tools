/** 
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

public final class Gav
{
    private final String groupId;

    private final String artifactId;

    private final String version;
    
    private final String packaging;

    public Gav( String groupId, String artifactId, String version, String packaging )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public String getPackaging()
    {
        return packaging;
    }

    public String getPomFilename()
    {
        return getFilenameStart() + ".pom";
    }

    public String getJarFilename()
    {
        return getFilenameStart() + ".jar";
    }

    public String getFilenameStart()
    {
        return artifactId + "-" + version;
    }

    public String getSourceFilename()
    {
        return getFilenameStart() + "-sources.jar";
    }

    public String getJavadocFilename()
    {
        return getFilenameStart() + "-javadoc.jar";
    }
    
    public String getRepositoryURLPath()
    {
        return groupId.replace( ".", "/" ) + "/" + artifactId + "/" + version 
                        + "/";
    }

    @Override
    public String toString()
    {
        return groupId + ":" + artifactId + ":" + version;
    }
}