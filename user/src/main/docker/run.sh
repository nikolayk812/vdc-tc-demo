#!/bin/sh

echo "********************************************************"
echo "Starting User Service"
echo "********************************************************"

while ! `nc -z eureka-alias $EUREKASERVER_PORT`; do sleep 3; done

java -Xms128m -Xmx128m -Djava.security.egd=file:/dev/./urandom -jar /usr/local/user/@project.build.finalName@.jar