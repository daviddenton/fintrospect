#!/bin/bash
echo Publishing site for v$1
set -e

npm run cleanSite
rm -rf /tmp/site
git clone git@github.com:fintrospect/fintrospect.github.io.git /tmp/site
cd /tmp/site
cd -
npm run makeSite
./sbt unidoc
rm -rf /tmp/site/*
mv target/scala-2.11/unidoc target/www/api
mv -f target/www/* /tmp/site
cd /tmp/site
git add *
git commit -m "releasing $1 version of site"
git push origin master
cd -
