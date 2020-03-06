package com.simpligility.maven.provisioner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProxyHelperTest
{

    public static final String PATTERN = "localhost|*.my.company|192.168.*|127.0.0.1";
    public static final String HOST = "test.my.company";
    public static final String SOURCE_URL = "https://test.my.company/repository/thirdparty/";

    @Test
    public void testIsHostMatchesNonProxyHostsPattern(   )
    {
        String javaPattern = ProxyHelper.convertToJavaPattern( PATTERN );
        boolean matches = ProxyHelper.isHostMatchesNonProxyHostsPattern( HOST, javaPattern );
        String message = String.format( "host '%s' must match pattern '%s'", HOST, PATTERN );
        assertTrue( message, matches );
    }


    @Test
    public void testConvertToJavaPattern(  )
    {
        String javaPattern = ProxyHelper.convertToJavaPattern( PATTERN );
        String expected = "localhost|.*\\.my\\.company|192\\.168\\..*|127\\.0\\.0\\.1";
        assertEquals( "javaPattern", expected, javaPattern );
    }

    @Test
    public void testIsUseProxyByPattern(  )
    {
        boolean useProxyByPattern = ProxyHelper.isUseProxyByPattern( HOST, PATTERN );
        assertFalse( "useProxyByPattern", useProxyByPattern );
    }


    @Test
    public void testGetProtocol(  )
    {
        String protocol = ProxyHelper.getProtocol( SOURCE_URL );
        assertEquals( "protocol", "https", protocol );
    }

    @Test
    public void testGetHost(  )
    {
        String host = ProxyHelper.getHost( SOURCE_URL );
        assertEquals( "host", HOST, host );
    }

}