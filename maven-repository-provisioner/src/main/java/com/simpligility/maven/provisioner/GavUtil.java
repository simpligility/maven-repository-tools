/** 
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

import java.io.File;

public class GavUtil
{

    public static Gav getGavFromRepositoryPath( String leafRepoPath )
    {

        int versionStartSlash = leafRepoPath.lastIndexOf( File.separator );
        String version = leafRepoPath.substring( versionStartSlash + 1, leafRepoPath.length() );

        String gaPath = leafRepoPath.substring( 0, versionStartSlash );
        int gaStartSlash = gaPath.lastIndexOf( File.separator );
        String artifactId = gaPath.substring( gaStartSlash + 1, gaPath.length() );

        String gPath = gaPath.substring( 0, gaStartSlash );
        String groupId = gPath.replace( File.separator, "." );

        return new Gav( groupId, artifactId, version );
    }
    

}
