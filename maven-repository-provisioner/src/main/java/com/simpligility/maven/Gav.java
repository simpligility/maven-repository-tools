/**
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven;

import java.util.Objects;

public final class Gav {
    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String packaging;

    public Gav(String groupId, String artifactId, String version, String packaging) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getPackaging() {
        return packaging;
    }

    public String getPomFilename() {
        return getFilenameStart() + "." + MavenConstants.POM;
    }

    public String getJarFilename() {
        return getFilenameStart() + "." + MavenConstants.JAR;
    }

    public String getFilenameStart() {
        return artifactId + "-" + version;
    }

    public String getSourceFilename() {
        return getFilenameStart() + MavenConstants.SOURCES_JAR;
    }

    public String getJavadocFilename() {
        return getFilenameStart() + MavenConstants.JAVADOC_JAR;
    }

    public String getRepositoryURLPath() {
        return groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Gav gav = (Gav) obj;
        return groupId.equals(gav.getGroupId())
                && artifactId.equals(gav.getArtifactId())
                && version.equals(gav.getVersion())
                && packaging.equals(gav.getPackaging());
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, packaging);
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
