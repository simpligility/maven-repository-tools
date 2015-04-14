package com.simpligility.maven.provisioner;

import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

public class ProxyHelper 
{
  public static Proxy getProxy( String sourceUrl )
  {

    String protocol = "http";
    Authentication auth = null;
    Proxy proxy = null;

    if ( System.getProperty( String.format( "%s.proxyUser", protocol ) ) != null ) 
    {
      auth = new AuthenticationBuilder()
          .addUsername( System.getProperty( String.format( "%s.proxyUser", protocol ) ) )
          .addPassword( System.getProperty( String.format( "%s.proxyPassword", protocol ) ) ).build();
    }

    if ( System.getProperty( String.format( "%s.proxyHost", protocol ) ) != null ) 
    {
      proxy = new Proxy( protocol, System.getProperty( String.format( "%s.proxyHost", protocol ) ),
                  Integer.parseInt( System.getProperty( String.format( "%s.proxyPort", protocol ) ) ), auth );
    }
    
    return proxy;
  }
}
