#!/bin/bash

workingDir="`dirname \"$0\"`"
cd "$workingDir" || exit 1
workingDir=$(pwd)

"$workingDir/build-tools/shell-helpers/rerun-command-on-file-change.sh" "./runTest.sh $1" "$workingDir" "$workingDir"
