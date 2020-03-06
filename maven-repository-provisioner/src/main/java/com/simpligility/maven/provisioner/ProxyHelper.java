package com.simpligility.maven.provisioner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProxyHelper 
{
   private static Logger log = LoggerFactory.getLogger( ProxyHelper.class );

   public static final String DEFAULT_PROTOCOL = "http";

   private ProxyHelper(  )
   {
   }

   public static Proxy getProxy( String url )
   {
       String protocol = getProtocol( url );
       String host = getHost( url );
       Authentication auth = null;

       String proxyUser = getProxyUser( protocol );
       if ( proxyUser != null )
       {
           String proxyPassword = getProxyPassword( protocol );
           auth = new AuthenticationBuilder(  )
                   .addUsername( proxyUser )
                   .addPassword( proxyPassword ).build(  );
       }

       Proxy proxy = null;
       String proxyHost = getProxyHost( protocol );
       if ( proxyHost != null )
       {
           boolean useProxy = isUseProxy( protocol, host );
           if ( useProxy )
           {
               int proxyPort = getProxyPort( protocol );
               proxy = new Proxy( protocol, proxyHost, proxyPort, auth );
           }
       }
       log.info( "getProxy: url = '{}'; proxy = {}", url, proxy );
       return proxy;
   }

   private static boolean isUseProxy( String protocol, String host )
   {
       String nonProxyHostsPattern = getNonProxyHostsPattern( protocol );
       boolean useProxyByPattern = isUseProxyByPattern( host, nonProxyHostsPattern );
       log.debug( "isUseProxy(  ): useProxyByPattern = {} - LEAVE", useProxyByPattern );
       return useProxyByPattern;
   }

   static boolean isUseProxyByPattern( String host, String nonProxyHostsPattern )
   {
       String nonProxyHostsPatternJava = convertToJavaPattern( nonProxyHostsPattern );
       boolean useProxy = !isHostMatchesNonProxyHostsPattern( host, nonProxyHostsPatternJava );
       log.info( "isUseProxyByPattern: useProxy = {}; host = '{}'", useProxy, host );
       return useProxy;
   }

   static String convertToJavaPattern( String pattern )
   {
       String javaPattern = pattern;
       if ( StringUtils.isNotBlank( pattern ) )
       {
           javaPattern = javaPattern.replaceAll( "\\.", "\\\\." );
           javaPattern = javaPattern.replaceAll( "\\*", ".*" );
       }
       log.debug( "convertToJavaPattern: javaPattern = '{}'", javaPattern );
       return javaPattern;
   }

   static boolean isHostMatchesNonProxyHostsPattern( String host, String nonProxyHostsPattern )
   {
       boolean matches = true;
       if ( StringUtils.isNotBlank( nonProxyHostsPattern ) )
       {
           try
           {
               matches = host.matches( nonProxyHostsPattern );
           }
           catch ( PatternSyntaxException e )
           {
               String message = String.format( "Invalid pattern for non-proxy hosts: '%s'", nonProxyHostsPattern );
               log.warn( message, e );
               matches = false;
           }
       }
       String format = "isHostMatchesNonProxyHostsPattern( host = '{}', nonProxyHostsPattern = '{}' ): {}";
       log.debug( format, host, nonProxyHostsPattern, matches );
       return matches;
   }

   private static int getProxyPort( String protocol )
   {
       String proxyPortKey = String.format( "%s.proxyPort", protocol );
       String proxyPortRaw = System.getProperty( proxyPortKey );
       int proxyPort = Integer.parseInt( proxyPortRaw );
       log.debug( "getProxyPort: proxyPort = {}", proxyPort );
       return proxyPort;
   }

   private static String getProxyHost( String protocol )
   {
       String proxyHostKey = String.format( "%s.proxyHost", protocol );
       String proxyHost = System.getProperty( proxyHostKey );
       log.debug( "getProxyHost: proxyHost = '{}'", proxyHost );
       return proxyHost;
   }

   private static String getNonProxyHostsPattern( String protocol )
   {
       String nonProxyHostsKey = String.format( "%s.nonProxyHosts", protocol );
       String nonProxyHostsPattern = System.getProperty( nonProxyHostsKey );
       log.info( "getNonProxyHostsPattern(  ): nonProxyHostsPattern = '{}'", nonProxyHostsPattern );
       return nonProxyHostsPattern;
   }

   private static String getProxyPassword( String protocol )
   {
       String proxyPasswordKey = String.format( "%s.proxyPassword", protocol );
       return System.getProperty( proxyPasswordKey );
   }

   private static String getProxyUser( String protocol )
   {
       String proxyUserKey = String.format( "%s.proxyUser", protocol );
       String proxyUser = System.getProperty( proxyUserKey );
       log.info( "getProxyUser: proxyUser = '{}'", proxyUser );
       return proxyUser;
   }

   static String getProtocol( String url )
   {
       log.debug( "getProtocol( url = '{}' )", url );
       String protocol = DEFAULT_PROTOCOL;

       try
       {
           URL u = new URL( url );
           protocol = u.getProtocol(  );
       }
       catch ( MalformedURLException e )
       {
           String message = String.format( "Failed to parse URL '%s'", url );
           log.warn( message, e );
       }

       log.debug( "getProtocol(  ): protocol = '{}'", protocol );
       return protocol;
   }

   static String getHost( String url )
   {
       log.debug( "getHost( url = '{}' )", url );
       String host = "";
       try
       {
           URL u = new URL( url );
           host = u.getHost(  );
       }
       catch ( MalformedURLException e )
       {
           String message = String.format( "Failed to parse URL '%s'", url );
           log.warn( message, e );
       }

       log.debug( "getHost(  ): host = '{}'", host );
       return host;
   }
}