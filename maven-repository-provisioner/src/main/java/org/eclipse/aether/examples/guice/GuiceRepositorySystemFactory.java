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

import org.eclipse.aether.RepositorySystem;

import com.google.inject.Guice;

/**
 * A factory for repository system instances that employs JSR-330 via Guice to wire up the system's components.
 */
public class GuiceRepositorySystemFactory
{

    public static RepositorySystem newRepositorySystem()
    {
        return Guice.createInjector( new DemoAetherModule() ).getInstance( RepositorySystem.class );
    }

}
