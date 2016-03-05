#!/bin/bash
echo Releasing and publishing v$1
set -e

npm run cleanSite
rm -rf /tmp/site
git clone git@github.com:daviddenton/fintrospect.git /tmp/site
cd /tmp/site
git checkout gh-pages
cd -
npm run makeSite
./sbt doc
rm -rf /tmp/site/*
mv target/scala-2.11/api target/www/
mv -f target/www/* /tmp/site
cd /tmp/site
git add *
git commit -m "releasing $1 version of site"
git push origin gh-pages
cd -
