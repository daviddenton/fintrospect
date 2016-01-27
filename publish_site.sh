#!/bin/bash
echo Releasing and publishing v$1

npm run cleanSite
cd target
git clone git@github.com:daviddenton/fintrospect.git www
cd www
git checkout --orphan gh-pages
rm -rf .
cd ../..
npm run makeSite
cd target/www
git add *
git commit -a -m "releasing $1 version of site"
git push origin gh-pages
cd ../..
