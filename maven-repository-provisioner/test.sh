#!/bin/bash
# fail if anything errors
set -e
# fail if a function call is missing an argument
set -u

# for my local Nexus
source=http://localhost:8081/nexus/content/groups/public
target=http://localhost:8081/nexus/content/repositories/test
# for my local Nexus 3
#source=http://localhost:8081/repository/maven-public
#target=http://localhost:8081/repository/tmp

creds="-u admin -p admin123"


function deploy {
  if [ -n "$1" ]; then
    java -jar target/maven-reposito*-with-dependencies.jar -s $source  -t $target -u admin -p admin123 -a $1
  else
    java -jar target/maven-reposito*-with-dependencies.jar -s $source  -t $target -u admin -p admin123
  fi
}

#deploy "junit:junit:4.11|org.testng:testng:6.9.10|com.squareup.assertj:assertj-android:aar:1.0.0"

# deploy "org.apache.maven.plugins:maven-surefire-plugin:jar:2.18.1"

#deploy "org.apache.commons:commons-lang3:jar:3.3.2"

# deploy "org.apache.abdera:abdera-bundle:1.1.3" 

#deploy "com.google.inject:guice:no_aop:jar:3.0"

# deploy "org.apache.commons:commons-lang3:jar:3.3.2|junit:junit:4.11|com.squareup.assertj:assertj-android:aar:1.0.0"

#deploy "com.simpligility.maven:progressive-organization-pom:pom:4.1.0"

# testing repo folder transfer only
#deploy ""

# testing OSGI bundle packaging and related .jar transfer
# deploy org.apache.geronimo.specs:geronimo-ejb_3.1_spec:1.0.2


# test for including provided scope
deploy com.hazelcast:hazelcast:3.7.2

#java -jar target/maven-repository-provisioner-*-jar-with-dependencies.jar  -cd "local-cache" -t $target $creds
