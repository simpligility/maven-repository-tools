package com.simpligility.maven.provisioner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.simpligility.maven.Gav;
import com.simpligility.maven.GavMatcher;
import com.simpligility.maven.GavPattern;

public class GavMatcherExecutor implements AutoCloseable {
    private final ExecutorService executorService;

    public GavMatcherExecutor(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public List<CompletableFuture<Boolean>> evaluateGav(Gav gav, Set<GavPattern> patterns) {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (GavPattern pattern : patterns) {
            CompletableFuture<Boolean> future =
                    CompletableFuture.supplyAsync(new GavMatcher(gav, pattern), executorService);
            futures.add(future);
        }
        return futures;
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }
}
