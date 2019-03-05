package com.simpligility.maven;

public final class MavenConstants
{
    public static final String JAVADOC = "javadoc";
    public static final String SOURCES = "sources";
    public static final String POM = "pom";
    public static final String JAVADOC_JAR = "-javadoc.jar";
    public static final String SOURCES_JAR = "-sources.jar";

    // packaging types with jar as additional artifact
    public static final String HPI = "hpi";
    public static final String JPI = "jpi";
    public static final String AAR = "aar";
    public static final String ZIP = "zip";

    // packaging types with no file of the same extension, instead normal jar is the main file
    public static final String JAR = "jar";
    public static final String BUNDLE = "bundle";
    public static final String MAVEN_PLUGIN = "maven-plugin";

    /**
     * Return if a specific packaging uses only a jar file as artifact, but is not necessarily pure jar packaging.
     * @param packaging
     * @return
     */
    public static boolean packagingUsesJarOnly( String packaging )
    {
      boolean result = false;
      if ( JAR.equals( packaging )
          || BUNDLE.equals( packaging )
          || MAVEN_PLUGIN.equals( packaging ) )
      {
        result = true;
      }
      return result;
    }

    /**
     * Return if a specific packaging uses jar file as additional artifacts.
     * @param packaging
     * @return
     */
    public static boolean packagingUsesAdditionalJar( String packaging )
    {
      boolean result = false;
      if ( HPI.equals( packaging )
          || JPI.equals( packaging )
          || AAR.equals( packaging )
          || ZIP.equals( packaging ) )
      {
        result = true;
      }
      return result;
    }

    /**
     * Return if a specific packaging uses jar artifacts (main or additional)
     * @param packaging
     * @return
     */
    public static boolean packagingUsesJar( String packaging )
    {
      return ( packagingUsesJarOnly( packaging ) || packagingUsesAdditionalJar( packaging ) );
    }
}
