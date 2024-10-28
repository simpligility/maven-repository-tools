package com.simpligility.maven;

import java.util.regex.Pattern;

public class GavPattern {
    private final Pattern pattern;
    private final boolean inverse;

    public GavPattern(Pattern pattern, boolean inverse) {
        this.inverse = inverse;
        this.pattern = pattern;
    }

    public boolean matches(Gav gav) {
        if (gav == null) {
            return false;
        }
        String gavString =
                gav.getGroupId() + ":" + gav.getArtifactId() + ":" + gav.getVersion() + ":" + gav.getPackaging();
        boolean matchResult = pattern.matcher(gavString).matches();

        if (inverse) {
            return !matchResult;
        }
        return matchResult;
    }
}
