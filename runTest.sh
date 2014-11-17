#!/bin/bash

workingDir="`dirname \"$0\"`"
cd "$workingDir" || exit 1

ant picodedTest

for ARG in $*
do
	if [[ $ARG == *mysql* || $ARG == *_all ]]
	then
	  echo "================================================================================="
	  echo "= IMPORTANT NOTE: mysql related tests, assumes the server is 127.0.0.1:3306,    ="
	  echo "=                 with the db, user, and pass of 'SERVLETCOMMONS'               ="
	  echo "=                                                                               ="
	  echo "= 'mysql -uroot' (lets you access mysql as root in most test enviroments)       ="
	  echo "= > CREATE DATABASE IF NOT EXISTS SERVLETCOMMONS;                               ="
	  echo "= > CREATE USER SERVLETCOMMONS@'localhost' IDENTIFIED BY 'SERVLETCOMMONS';      ="
	  echo "= > GRANT ALL PRIVILEGES ON SERVLETCOMMONS.* TO SERVLETCOMMONS@'localhost';     ="
	  echo "================================================================================="
	fi
	
	echo "Running test: picodedTests.$ARG"
	echo "---------------------------------------------------------------------------------"
	
	java -Djava.library.path="./lib" -cp "./lib/*:./classes" org.junit.runner.JUnitCore picodedTests.$ARG
done