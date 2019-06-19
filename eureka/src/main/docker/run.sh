#!/bin/sh

echo "********************************************************"
echo "Starting Eureka"
echo "********************************************************"

java -Djava.security.egd=file:/dev/./urandom -jar /usr/local/eureka/@project.build.finalName@.jar
