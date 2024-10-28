package com.simpligility.maven.provisioner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

import com.simpligility.maven.Gav;
import com.simpligility.maven.GavPattern;
import org.junit.Before;
import org.junit.Test;

public class MavenRepositoryDeployerTest {

    private MavenRepositoryDeployer deployer;
    private Configuration config;

    @Before
    public void setUp() {
        config = new Configuration();
        deployer = new MavenRepositoryDeployer(config);
    }

    @Test
    public void testLoadGavsFromFilterFile_ValidFile() throws IOException {
        File tempFile = createTempFile("org.apache.maven.resolver:maven-resolver:1.0.0:*\n");
        config.setDeployFilterFile(tempFile.getAbsolutePath());
        Set<GavPattern> gavPatterns = deployer.loadGavPatternsFromFilterFile(tempFile.getAbsolutePath());
        assertEquals(1, gavPatterns.size());
        assertTrue(gavPatterns
                .iterator()
                .next()
                .matches(new Gav("org.apache.maven.resolver", "maven-resolver", "1.0.0", "jar")));
        tempFile.delete();
    }

    @Test
    public void testLoadGavsFromFilterFile_EmptyFile() throws IOException {
        File tempFile = createTempFile("");
        config.setDeployFilterFile(tempFile.getAbsolutePath());
        Set<GavPattern> gavPatterns = deployer.loadGavPatternsFromFilterFile(tempFile.getAbsolutePath());
        assertTrue(gavPatterns.isEmpty());
        tempFile.delete();
    }

    @Test
    public void testLoadGavsFromFilterFile_InvalidFormat() throws IOException {
        File tempFile = createTempFile("invalid:format\n");
        config.setDeployFilterFile(tempFile.getAbsolutePath());
        Set<GavPattern> gavPatterns = deployer.loadGavPatternsFromFilterFile(tempFile.getAbsolutePath());
        assertTrue(gavPatterns.isEmpty());
        tempFile.delete();
    }

    @Test
    public void testLoadGavsFromFilterFile_MixedValidAndInvalid() throws IOException {
        File tempFile = createTempFile("org.apache.maven:apache-maven:3.9.9:*\ninvalid:format\n");
        config.setDeployFilterFile(tempFile.getAbsolutePath());
        Set<GavPattern> gavPatterns = deployer.loadGavPatternsFromFilterFile(tempFile.getAbsolutePath());
        assertEquals(1, gavPatterns.size());
        assertTrue(gavPatterns.iterator().next().matches(new Gav("org.apache.maven", "apache-maven", "3.9.9", "jar")));
        tempFile.delete();
    }

    @Test
    public void testLoadGavsFromFilterFile_NonExistentFile() {
        Set<GavPattern> gavPatterns = deployer.loadGavPatternsFromFilterFile("nonexistentfile.txt");
        assertTrue(gavPatterns.isEmpty());
    }

    private File createTempFile(String content) throws IOException {
        File tempFile = File.createTempFile("gavFilter", ".txt");
        config.setDeployFilterFile(tempFile.getAbsolutePath());
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile;
    }
}
