package com.simpligility.maven;

import static org.junit.Assert.*;

import org.junit.Test;

public class GavTest {

    @Test
    public void testEquals() {
        Gav gav1 = new Gav("groupId", "artifactId", "version", "jar");
        Gav gav2 = new Gav("groupId", "artifactId", "version", "jar");
        Gav gav3 = new Gav("groupId", "artifactId", "differentVersion", "jar");

        assertTrue(gav1.equals(gav2));
        assertFalse(gav1.equals(gav3));
        assertFalse(gav1.equals(null));
        assertFalse(gav1.equals(new Object()));
    }

    @Test
    public void testHashCode() {
        Gav gav1 = new Gav("groupId", "artifactId", "version", "jar");
        Gav gav2 = new Gav("groupId", "artifactId", "version", "jar");
        Gav gav3 = new Gav("groupId", "artifactId", "differentVersion", "jar");

        assertEquals(gav1.hashCode(), gav2.hashCode());
        assertNotEquals(gav1.hashCode(), gav3.hashCode());
    }
}
