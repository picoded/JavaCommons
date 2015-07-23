#!/bin/bash

workingDir="`dirname \"$0\"`"
cd "$workingDir" || exit 1

ant compile-tests

for ARG in $*
do

#
#	The below no longer applies as the MYSQL database is "centralised" with the rest of the DB's
#
#	if [[ $ARG == *mysql* || $ARG == *_all ]]
#	then
#	  echo "================================================================================="
#	  echo "= IMPORTANT NOTE: mysql related tests, assumes the server is 127.0.0.1:3306,    ="
#	  echo "=                 with the db, user, and pass of 'SERVLETCOMMONS'               ="
#	  echo "=                                                                               ="
#	  echo "= 'mysql -uroot' (lets you access mysql as root in most test enviroments)       ="
#	  echo "= > CREATE DATABASE IF NOT EXISTS SERVLETCOMMONS;                               ="
#	  echo "= > CREATE USER SERVLETCOMMONS@'localhost' IDENTIFIED BY 'SERVLETCOMMONS';      ="
#	  echo "= > GRANT ALL PRIVILEGES ON SERVLETCOMMONS.* TO SERVLETCOMMONS@'localhost';     ="
#	  echo "================================================================================="
#	fi
#
	
	echo "Running test: picodedTests."$ARG"_test"
	echo "---------------------------------------------------------------------------------"
	
	#-Djava.library.path="./build-tools/junit/*.jar:./bin/build/picodedJavaCommons-libsOnly.jar"
	java -cp "./build-tools/junit/*:./bin/build/picodedJavaCommons-libsOnly.jar:./bin/classes" org.junit.runner.JUnitCore picodedTests."$ARG"_test
done