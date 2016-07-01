#!/bin/bash

if [ -z "$GIT_DIR" ]; then
	workingDir="`dirname \"$0\"`"
else 
	workingDir="$(dirname \"$GIT_DIR\")"
fi

cd "$workingDir" || exit 1

# Permission nuke
chmod -R 0777 .;
chmod -R +x .;

# Source beautify
ant src-beautify;
