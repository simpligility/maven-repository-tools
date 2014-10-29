/*******************************************************************************
 * Copyright (c) 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.aether.examples.guice;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.repository.internal.MavenAetherModule;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

class DemoAetherModule
    extends AbstractModule
{

    @Override
    protected void configure()
    {
        install( new MavenAetherModule() );
        // alternatively, use the Guice Multibindings extensions
        bind( RepositoryConnectorFactory.class ).annotatedWith( Names.named( "basic" ) ).to( BasicRepositoryConnectorFactory.class );
        bind( TransporterFactory.class ).annotatedWith( Names.named( "file" ) ).to( FileTransporterFactory.class );
        bind( TransporterFactory.class ).annotatedWith( Names.named( "http" ) ).to( HttpTransporterFactory.class );
    }

    @Provides
    @Singleton
    Set<RepositoryConnectorFactory> provideRepositoryConnectorFactories( @Named( "basic" ) RepositoryConnectorFactory basic )
    {
        Set<RepositoryConnectorFactory> factories = new HashSet<RepositoryConnectorFactory>();
        factories.add( basic );
        return Collections.unmodifiableSet( factories );
    }

    @Provides
    @Singleton
    Set<TransporterFactory> provideTransporterFactories( @Named( "file" ) TransporterFactory file,
                                                         @Named( "http" ) TransporterFactory http )
    {
        Set<TransporterFactory> factories = new HashSet<TransporterFactory>();
        factories.add( file );
        factories.add( http );
        return Collections.unmodifiableSet( factories );
    }

}
