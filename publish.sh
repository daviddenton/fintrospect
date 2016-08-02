#!/bin/bash
echo Releasing and publishing v$1

set -e

git tag -a $1 -m "released version $1 to bintray"
git tag
git push origin $1

./sbt clean compile test package publish

./publish_site.sh $1

./sbt bintraySyncMavenCentral
