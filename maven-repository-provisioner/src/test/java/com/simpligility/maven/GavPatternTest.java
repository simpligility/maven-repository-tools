package com.simpligility.maven;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GavPatternTest {

    @Test
    public void testMatches_ValidPattern() {
        GavPattern pattern = new GavPattern(Pattern.compile("org\\.apache\\.maven\\.resolver:.*:.*:.*"), false);
        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");
        assertTrue(pattern.matches(gav));
    }

    @Test
    public void testMatches_InvalidPattern() {
        GavPattern pattern = new GavPattern(Pattern.compile("com\\.simpligility:.*:.*:.*"), false);
        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");
        assertFalse(pattern.matches(gav));
    }

    @Test
    public void testMatches_EmptyPattern() {
        GavPattern pattern = new GavPattern(Pattern.compile(""), false);
        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");
        assertFalse(pattern.matches(gav));
    }

    @Test
    public void testMatches_NullGav() {
        GavPattern pattern = new GavPattern(Pattern.compile("org\\.apache\\.maven\\.resolver:.*:.*:.*"), false);
        assertFalse(pattern.matches(null));
    }

    @Test
    public void testMatches_ValidPattern_Inverse() {
        GavPattern pattern = new GavPattern(Pattern.compile("org\\.apache\\.maven\\.resolver:.*:.*:.*"), true);
        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");
        assertFalse(pattern.matches(gav));
    }

    @Test
    public void testMatches_InvalidPattern_Inverse() {
        GavPattern pattern = new GavPattern(Pattern.compile("com\\.simpligility:.*:.*:.*"), true);
        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");
        assertTrue(pattern.matches(gav));
    }

    @Test
    public void testMatches_EmptyPattern_Inverse() {
        GavPattern pattern = new GavPattern(Pattern.compile(""), true);
        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");
        assertTrue(pattern.matches(gav));
    }
}
