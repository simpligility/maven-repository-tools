package com.simpligility.maven;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class GavMatcher implements Callable<Boolean>, Supplier<Boolean> {
    private final Gav gav;
    private final GavPattern pattern;

    public GavMatcher(Gav gav, GavPattern pattern) {
        this.gav = gav;
        this.pattern = pattern;
    }

    @Override
    public Boolean call() {
        return pattern.matches(gav);
    }

    @Override
    public Boolean get() {
        return call();
    }
}
