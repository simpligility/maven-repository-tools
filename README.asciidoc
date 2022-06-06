simpligility technologies inc. presents 

= Maven Repository Tools

== Introduction

A collection of tools to work with Maven repositories.

Maven Repository Provisioner::  a command line tool and library to
provision a component and all its transitive dependencies from a source Maven repository to a target 
Maven repository. See more in the https://github.com/simpligility/maven-repository-tools/tree/master/maven-repository-provisioner[readme file of the module].

Repository Provisioner Maven Plugin:: a maven plugin for the same
task - to be done.

== Requirememts

All tools require Java 11 or higher.

== Download

Everything is available from the Central Repository in the
simpligility space:

* https://repo1.maven.org/maven2/com/simpligility/maven/


== Roadmap, Issues, Changes

Check out the
https://github.com/simpligility/maven-repository-tools/issues[issues
list] for upcoming changes, existing problems and so on.

For past releases and already implemented changes, see the https://github.com/simpligility/maven-repository-tools/blob/master/changelog.asciidoc[changelog] as
well as the https://github.com/simpligility/maven-repository-tools/commits/master[commit history].

== License

Eclipse Public License - v 1.0

For full text see the `LICENSE` file or https://www.eclipse.org/legal/epl-v10.html

== Building

Run

----
mvn clean install
----

Verify full build for release with

----
mvn clean deploy -P release
----

Release with the usual

----
mvn release:prepare release:perform
----

== Contributions

are very welcome. Send a pull request or report issues on GitHub. Even just a
spelling fix in the readme or anything else really is a welcome help.

== Contributors

- Manfred Moser http://www.simpligility.com - project management and
  all coding
- Jason van Zyl https://github.com/jvanzyl - Maven and Aether help
- Igor Fedorenko https://github.com/ifedorenko - Maven and Aether help
- Ian Williams - initial discussion around concept
- More details on github - https://github.com/simpligility/maven-repository-tools/network/members[members] and https://github.com/simpligility/maven-repository-tools/graphs/contributors[contributors]

