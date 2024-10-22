/**
 * Copyright simpligility technologies inc. http://www.simpligility.com
 * Licensed under Eclipse Public License - v 1.0 http://www.eclipse.org/legal/epl-v10.html
 */
package com.simpligility.maven.provisioner;

import java.util.List;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void testCoordinatesParsing() {
        Configuration configuration = new Configuration();
        configuration.setArtifactCoordinate("a|b|c");

        List<String> coordinates = configuration.getArtifactCoordinates();
        assertEquals(3, coordinates.size());
    }
}
