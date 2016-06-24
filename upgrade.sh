#!/bin/bash
set -e

function upgrade {
    echo Upgrade $1 to $2
    find . -type f -name 'build.sbt' | grep -v "node_modules" | grep -v "bower_components" | xargs sed -i '' s/"$1"/"$2"/g
    find . -type f -name '*.md' | grep -v "node_modules" | grep -v "bower_components" | xargs sed -i '' s/"$1"/"$2"/g
    find . -type f -name '*.jade' | grep -v "node_modules" | grep -v "bower_components" | xargs sed -i '' s/"$1"/"$2"/g
    sed -i '' s/"$1"/"$2"/g build.sbt
}

upgrade `./tools/jq .globals.fintrospect.old harp.json` `./tools/jq .globals.fintrospect.new harp.json`
