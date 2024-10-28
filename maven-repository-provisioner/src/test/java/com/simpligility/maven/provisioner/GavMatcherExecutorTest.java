package com.simpligility.maven.provisioner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.simpligility.maven.Gav;
import com.simpligility.maven.GavPattern;
import org.junit.Test;

public class GavMatcherExecutorTest {

    @Test
    public void testEvaluateGav_MatchingPattern() throws ExecutionException, InterruptedException {
        Set<GavPattern> patterns = new HashSet<>();
        patterns.add(new GavPattern(Pattern.compile("org\\.apache\\.maven\\.resolver:.*:.*:.*"), false));

        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");

        try (GavMatcherExecutor executor = new GavMatcherExecutor(4)) {
            List<CompletableFuture<Boolean>> results = executor.evaluateGav(gav, patterns);
            boolean matchFound = false;
            for (CompletableFuture<Boolean> result : results) {
                if (result.get()) {
                    matchFound = true;
                    break;
                }
            }
            assertTrue(matchFound);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEvaluateGav_NonMatchingPattern() throws ExecutionException, InterruptedException {
        Set<GavPattern> patterns = new HashSet<>();
        patterns.add(new GavPattern(Pattern.compile("com\\.simpligility:.*:.*:.*"), false));

        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");

        try (GavMatcherExecutor executor = new GavMatcherExecutor(4)) {
            List<CompletableFuture<Boolean>> results = executor.evaluateGav(gav, patterns);
            boolean matchFound = false;
            for (CompletableFuture<Boolean> result : results) {
                if (result.get()) {
                    matchFound = true;
                    break;
                }
            }
            assertFalse(matchFound);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEvaluateGav_EmptyPatternSet() throws ExecutionException, InterruptedException {
        Set<GavPattern> patterns = new HashSet<>();

        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");

        try (GavMatcherExecutor executor = new GavMatcherExecutor(4)) {
            List<CompletableFuture<Boolean>> results = executor.evaluateGav(gav, patterns);
            boolean matchFound = false;
            for (CompletableFuture<Boolean> result : results) {
                if (result.get()) {
                    matchFound = true;
                    break;
                }
            }
            assertFalse(matchFound);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEvaluateGav_NullGav() throws ExecutionException, InterruptedException {
        Set<GavPattern> patterns = new HashSet<>();
        patterns.add(new GavPattern(Pattern.compile("org\\.apache\\.maven\\.resolver:.*:.*:.*"), false));

        try (GavMatcherExecutor executor = new GavMatcherExecutor(4)) {
            List<CompletableFuture<Boolean>> results = executor.evaluateGav(null, patterns);
            boolean matchFound = false;
            for (CompletableFuture<Boolean> result : results) {
                if (result.get()) {
                    matchFound = true;
                    break;
                }
            }
            assertFalse(matchFound);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEvaluateGav_MultiplePatternsWithDifferentResults() throws ExecutionException, InterruptedException {
        Set<GavPattern> patterns = new HashSet<>();
        patterns.add(
                new GavPattern(Pattern.compile("org\\.apache\\.maven\\.resolver:.*:.*:.*"), false)); // Should match
        patterns.add(new GavPattern(Pattern.compile("com\\.simpligility:.*:.*:.*"), false)); // Should not match
        patterns.add(new GavPattern(Pattern.compile("org\\.apache\\.commons:.*:.*:.*"), false)); // Should not match

        Gav gav = new Gav("org.apache.maven.resolver", "artifactId", "version", "jar");

        try (GavMatcherExecutor executor = new GavMatcherExecutor(4)) {
            List<CompletableFuture<Boolean>> results = executor.evaluateGav(gav, patterns);
            boolean matchFound = false;
            for (CompletableFuture<Boolean> result : results) {
                if (result.get()) {
                    matchFound = true;
                    break;
                }
            }
            assertTrue(matchFound);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
