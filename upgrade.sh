#!/bin/bash
set -e

function upgrade {
    echo Upgrade $1 to $2
    find . -type f -name '*.md' | grep -v "node_modules" | grep -v "bower_components" | xargs sed -i '' s/"$1"/"$2"/g
    sed -i '' s/"$1"/"$2"/g build.sbt
}

upgrade `./tools/jq .globals.fintrospect.old harp.json` `./tools/jq .globals.fintrospect.new harp.json`
upgrade `./tools/jq .globals.finagle.old harp.json` `./tools/jq .globals.finagle.new harp.json`
