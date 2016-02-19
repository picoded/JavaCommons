#!/bin/bash

workingDir="`dirname \"$0\"`"
cd "$workingDir" || exit 1
workingDir="`pwd`"
cd "$workingDir" || exit 1

ant source

baseClass="$1"
contextURI="$2"
contextPath="`dirname \"$workingDir\"`"

if [ -z $1 ]; then
	baseClass="picoded.servlet.CommonsPage"
fi
if [ -z $2 ]; then
	contextURI="/demo"
fi

if [[ $? != 0 ]] 
then
	echo "---------------------------------------------------------------------------------"
	echo "! Compilation failed, aborting pages build";
else
	java -cp "./lib/*:./classes" "picoded.servlet.CommonsPage" "$baseClass" "$contextPath" "$contextURI"
fi
