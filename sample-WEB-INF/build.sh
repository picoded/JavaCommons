#!/bin/sh

DIR="$( cd "$( dirname "$0" )" && pwd )"

cd "$DIR"

ant buildJavaCommons;
ant source;