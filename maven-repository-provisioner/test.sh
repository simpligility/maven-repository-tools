#!/bin/bash
# fail if anything errors
set -e
# fail if a function call is missing an argument
set -u

# for my local Nexus
source=http://localhost:8081/nexus/content/groups/public
target=http://localhost:8081/nexus/content/repositories/test
# for my local Nexus 3
#source=http://localhost:9081/content/groups/public
#target=http://localhost:9081/content/repositories/test

function deploy {
  java -jar target/maven-reposito*-with-dependencies.jar -s $source  -t $target -u admin -p admin123 -a $1
}

# deploy "junit:junit:4.11|junit:junit:3.8.1:com.squareup.assertj:assertj-android:aar:1.0.0""

deploy "org.apache.maven.plugins:maven-surefire-plugin:jar:2.18.1"
