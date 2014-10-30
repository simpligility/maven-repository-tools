# java -jar target/maven-reposito*-with-dependencies.jar -s http://localhost:8081/content/groups/public -t http://localhost:8081/content/repositories/test -u admin -p admin123 -a "junit:junit:4.11|junit:junit:3.8.1:com.squareup.assertj:assertj-android:aar:1.0.0""

java -jar target/maven-reposito*-with-dependencies.jar -s http://localhost:8081/content/groups/public -t http://localhost:8081/content/repositories/test -u admin -p admin123 -a "com.squareup.assertj:assertj-android:aar:1.0.0"
