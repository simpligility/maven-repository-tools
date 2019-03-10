#!/bin/bash
# fail if anything errors
set -e
# fail if a function call is missing an argument
set -u

# Central Repository directly
#source=https://repo1.maven.org/maven2/

# for my local WaRM
source=http://localhost:8081/content/groups/public
target=http://localhost:8081/content/repositories/test
# for my local Nexus
#source=http://localhost:8081/nexus/content/groups/public
#target=http://localhost:8081/nexus/content/repositories/test
# for my local Nexus 3
source=http://localhost:8081/repository/maven-public
target=http://localhost:8081/repository/test

creds="-u admin -p admin123"
#options=""
options="-ij false -is false"

function deploy {
  if [ -n "$1" ]; then
    java -jar target/maven-reposito*-with-dependencies.jar -s $source  -t $target $creds $options -a $1
  else
    java -jar target/maven-reposito*-with-dependencies.jar -s $source  -t $target $creds $options
  fi
}

# normal JAR

#deploy "junit:junit:4.11"
#deploy "org.testng:testng:jar:6.9.10"
#deploy "org.apache.commons:commons-lang3:jar:3.3.2"
#deploy "org.apache.abdera:abdera-bundle:1.1.3"
#deploy "com.google.inject:guice:jar:no_aop:3.0"
#deploy "org.apache.commons:commons-lang3:jar:3.3.2|junit:junit:4.11|com.squareup.assertj:assertj-android:aar:1.1.1"

# POM packaging
#deploy "com.simpligility.maven:progressive-organization-pom:pom:4.1.0"

# AAR

# should download aar and jar
#deploy "com.squareup.assertj:assertj-android:aar:1.1.1"
# however this will NOT work since the pom file uses jar packaging so only jar
# is downloaded - there is no way to tell this is actually an aar as well...
# deploy "com.squareup.assertj:assertj-android:1.1.1"


# testing OSGI bundle packaging and related .jar transfer
# should always download jar and NOT cause a failure
#deploy org.apache.geronimo.specs:geronimo-ejb_3.1_spec:1.0.2
#deploy org.apache.geronimo.specs:geronimo-ejb_3.1_spec:bundle:1.0.2
#deploy org.drools:drools-compiler:6.5.0.Final
#deploy org.drools:drools-compiler:bundle:6.5.0.Final
#deploy org.kie:kie-api:bundle:6.5.0.Final
deploy org.kie:kie-api:6.5.0.Final

# testing hpi packaging, both should get a hpi file and a jar file
#deploy org.jenkins-ci.plugins:git:hpi:3.4.0
#deploy org.jenkins-ci.plugins:git:3.4.0

# testing maven-plugin packaging
#deploy "org.apache.maven.plugins:maven-surefire-plugin:jar:2.18.1"
#deploy "org.apache.maven.plugins:maven-surefire-plugin:maven-plugin:2.18.1"

# test for including provided scope
#deploy com.hazelcast:hazelcast:3.7.2

# testing repo folder transfer only
#deploy ""

#java -jar target/maven-repository-provisioner-*-jar-with-dependencies.jar  -cd "local-cache" -t $target $creds
