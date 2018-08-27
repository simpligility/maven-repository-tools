package com.simpligility.maven.provisioner;

import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit representation of the test.sh.
 */
public class CliIT
{
    /**
     * String constant for boolean value true.
     */
    private static final String TRUE = String.valueOf( true );
    /**
     * String constant for boolean value false.
     */
    private static final String FALSE = String.valueOf( false );

    // Maven central source
    //private static final String SOURCE_REPO = "https://repo1.maven.org/maven2";

    // for my local WaRM
    private static final String SOURCE_REPO = "http://localhost:8081/content/groups/public";
    private static final String TARGET_REPO = "http://localhost:8081/content/repositories/test";

    // for my local Nexus
    //private static final String SOURCE_REPO="http://localhost:8081/nexus/content/groups/public"
    //private static final String TARGET_REPO="http://localhost:8081/nexus/content/repositories/test"

    // for my local Nexus 3
    //private static final String SOURCE_REPO="http://localhost:8081/repository/maven-public";
    //private static final String TARGET_REPO="http://localhost:8081/repository/tmp";

    // Nexus Default User and Password, nothing production related
    private static final String OPT_USER = "admin";
    private static final String OPT_PASS = "admin123";

    private static final String OPT_INCLUDE_JAVA_DOC = FALSE;
    private static final String OPT_INCLUDE_SOURCES = FALSE;


    /**
     * normal JAR
     */
    @Test
    @Ignore( "no automated test setup yet" )
    public void testNormalJar()
    {
        deploy( "junit:junit:4.11" );
        deploy( "org.testng:testng:jar:6.9.10" );
        deploy( "org.apache.commons:commons-lang3:jar:3.3.2" );
        deploy( "org.apache.abdera:abdera-bundle:1.1.3" );
        deploy( "com.google.inject:guice:jar:no_aop:3.0" );
        deploy( "org.apache.commons:commons-lang3:jar:3.3.2|"
                + "junit:junit:4.11|"
                + "com.squareup.assertj:assertj-android:aar:1.1.1" );
    }

    /**
     * POM packaging
     **/
    @Test
    @Ignore( "no automated test setup yet" )
    public void testPomPackaging()
    {
        deploy( "com.simpligility.maven:progressive-organization-pom:pom:4.1.0" );
    }

    /**
     * AAR
     */
    @Test
    @Ignore( "no automated test setup yet" )
    public void testAar()
    {
        // should download aar and jar
        deploy( "com.squareup.assertj:assertj-android:aar:1.1.1" );
        // however this will NOT work since the pom file uses jar packaging so only jar
        // is downloaded - there is no way to tell this is actually an aar as well...
        deploy( "com.squareup.assertj:assertj-android:1.1.1" );
    }

    /**
     * testing OSGI bundle packaging and related .jar transfer
     */
    @Test
    @Ignore( "no automated test setup yet" )
    public void testOsgi()
    {
        // should always download jar and NOT cause a failure
        deploy( "org.apache.geronimo.specs:geronimo-ejb_3.1_spec:1.0.2" );
        deploy( "org.apache.geronimo.specs:geronimo-ejb_3.1_spec:bundle:1.0.2" );
        deploy( "org.drools:drools-compiler:6.5.0.Final" );
        deploy( " org.drools:drools-compiler:bundle:6.5.0.Final" );
        deploy( " org.kie:kie-api:bundle:6.5.0.Final" );
        deploy( " org.kie:kie-api:6.5.0.Final" );
    }

    /**
     * testing hpi packaging, both should get a hpi file and a jar file
     */
    @Test
    @Ignore( "no automated test setup yet" )
    public void testHpi()
    {
        deploy( "org.jenkins-ci.plugins:git:hpi:3.4.0" );
        deploy( "org.jenkins-ci.plugins:git:3.4.0" );
    }

    /**
     * testing maven-plugin packaging
     */
    @Test
    @Ignore( "no automated test setup yet" )
    public void testMavenPlugins()
    {
        deploy( "org.apache.maven.plugins:maven-surefire-plugin:jar:2.18.1" );
        deploy( "org.apache.maven.plugins:maven-surefire-plugin:maven-plugin:2.18.1" );
    }

    /**
     * test for including provided scope
     */
    @Test
    @Ignore( "no automated test setup yet" )
    public void testProvided()
    {
        deploy( "com.hazelcast:hazelcast:3.7.2" );
    }

    /**
     * testing repo folder transfer only
     */
    @Test
    @Ignore( "no automated test setup yet" )
    public void testFolderTransfer()
    {
        deploy( "" );
    }

    /**
     * start the  transfer via CLI Interface.
     */
    private void deploy( String gav )
    {
        String[] args = {
                "-sourceUrl", SOURCE_REPO,
                "-targetUrl", TARGET_REPO,
                "-username", OPT_USER,
                "-password", OPT_PASS,
                "-checkTarget", TRUE,
                "-includeJavadoc", OPT_INCLUDE_JAVA_DOC,
                "-includeProvidedScope", FALSE,
                "-includeRuntimeScope", FALSE,
                "-includeSources", OPT_INCLUDE_SOURCES,
                "-includeTestScope", FALSE,
                "-artifactCoordinates", gav,
                "-vo", FALSE
        };
        MavenRepositoryProvisioner.main( args ); // TODO capture exit code
    }
}
