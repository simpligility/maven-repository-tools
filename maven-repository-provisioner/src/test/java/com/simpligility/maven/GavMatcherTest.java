package com.simpligility.maven;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GavMatcherTest {

    @Test
    public void testCall_MatchingPattern() throws ExecutionException, InterruptedException {
        GavPattern pattern = new GavPattern(Pattern.compile("org\\.apache\\.maven\\.resolver:.*:.*:.*"), false);
        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");
        GavMatcher matcher = new GavMatcher(gav, pattern);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> result = executor.submit(matcher);
        assertTrue(result.get());
        executor.shutdown();
    }

    @Test
    public void testCall_NonMatchingPattern() throws ExecutionException, InterruptedException {
        GavPattern pattern = new GavPattern(Pattern.compile("com\\.simpligility:.*:.*:.*"), false);
        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");
        GavMatcher matcher = new GavMatcher(gav, pattern);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> result = executor.submit(matcher);
        assertFalse(result.get());
        executor.shutdown();
    }

    @Test
    public void testCall_EmptyPattern() throws ExecutionException, InterruptedException {
        GavPattern pattern = new GavPattern(Pattern.compile(""), false);
        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");
        GavMatcher matcher = new GavMatcher(gav, pattern);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> result = executor.submit(matcher);
        assertFalse(result.get());
        executor.shutdown();
    }

    @Test
    public void testCall_NullGav() throws ExecutionException, InterruptedException {
        GavPattern pattern = new GavPattern(Pattern.compile("org\\.apache\\.maven\\.resolver:.*:.*:.*"), false);
        GavMatcher matcher = new GavMatcher(null, pattern);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> result = executor.submit(matcher);
        assertFalse(result.get());
        executor.shutdown();
    }
}
