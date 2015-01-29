/*******************************************************************************
 * Copyright (c) 2010, 2014 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package com.simpligility.maven.provisioner;

import java.io.File;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.LocalRepository;

/**
 * A helper to boot the repository system and a repository system session.
 */
public class Booter
{

    public static RepositorySystem newRepositorySystem()
    {
        return GuiceRepositorySystemFactory.newRepositorySystem();
    }

    public static DefaultRepositorySystemSession newRepositorySystemSession( RepositorySystem system, File localRepoPath)
    {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository( localRepoPath );
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

        session.setTransferListener( new LoggingTransferListener() );
        session.setRepositoryListener( new LoggingRepositoryListener() );

        return session;
    }
}
