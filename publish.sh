#!/bin/bash
echo Releasing and publishing v$1

git tag -a $1 -m "released version $1 to bintray"
git tag
git push origin $1

./sbt "set version:=\"$1\"" clean compile test +package +publish