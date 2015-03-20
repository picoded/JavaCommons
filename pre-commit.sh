#!/bin/bash

if [ -z "$GIT_DIR" ]; then
	workingDir="`dirname \"$0\"`"
else 
	workingDir="$(dirname \"$GIT_DIR\")"
fi

cd "$workingDir" || exit 1

ant src-beautify