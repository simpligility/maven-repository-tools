package com.simpligility.maven.provisioner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class ConfigurationTest
{

    @Test
    public void testCoordinatesParsing()
    {
        Configuration configuration = new Configuration();
        configuration.setArtifactCoordinate( "a|b|c" );

        List<String> coordinates = configuration.getArtifactCoordinates();
        assertEquals( 3, coordinates.size() );
    }
}
