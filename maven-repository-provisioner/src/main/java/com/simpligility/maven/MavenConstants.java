package com.simpligility.maven;

public final class MavenConstants
{
    public static final String JAVADOC = "javadoc";
    public static final String SOURCES = "sources";
    public static final String POM = "pom";
    public static final String JAR = "jar";
    public static final String JAVADOC_JAR = "-javadoc.jar";
    public static final String SOURCES_JAR = "-sources.jar";

    // packaging types with jar as additional artifact
    public static final String HPI = "hpi";

    // packaging types with no file of the same extension, instead normal jar is the main file
    public static final String BUNDLE = "bundle";
    public static final String PLUGIN = "maven-plugin";
}
