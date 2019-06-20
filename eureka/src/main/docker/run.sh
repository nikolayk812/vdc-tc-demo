#!/bin/sh

echo "********************************************************"
echo "Starting Eureka"
echo "********************************************************"

java -Xms128m -Xmx128m -Djava.security.egd=file:/dev/./urandom -jar /usr/local/eureka/@project.build.finalName@.jar
