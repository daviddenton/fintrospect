#!/bin/bash
echo Releasing and publishing v$1
set -e

npm run cleanSite
rm -rf /tmp/site
git clone git@github.com:daviddenton/fintrospect.git /tmp/site
cd /tmp/site
git checkout --orphan gh-pages
rm -rf *
cd -
npm run makeSite
cp -R target/www/* /tmp/site
cd /tmp/site
git add *
git commit -a -m "releasing $1 version of site"
git push origin gh-pages
cd -
