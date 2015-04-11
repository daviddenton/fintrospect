#!/bin/sh
java -Dsbt.boot.directory=project/boot/ -Xmx2048m -XX:MaxPermSize=512M -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -jar `find tools -name sbt-launch*.jar` "$@"
