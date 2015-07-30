#!/bin/bash

workingDir="`dirname \"$0\"`"
cd "$workingDir" || exit 1

ant compile-tests

for ARG in $*
do
	
	# Split class and function names if needed
	#----------------------------------------------------------
	if [[ $ARG == *"#"* ]]
	then
		TESTCLASS=`(echo $ARG | cut -d"#" -f 1)`
		TESTFUNCTION=`(echo $ARG | cut -d"#" -f 2)`
	else
		TESTCLASS="$ARG";
		TESTFUNCTION="";
	fi
	
	
	# Ensure test classes ends with the _test suffixes, and picodedTest prefix
	#------------------------------------------------------------------------------
	_TEST="_test";
	
	if [[ $TESTCLASS =~ \_test$ ]] 
	then
		TESTCLASS="$TESTCLASS";
	else 
		TESTCLASS="$TESTCLASS$_TEST";
	fi
	
	if [[ $TESTCLASS == picodedTests* ]] 
	then
		TESTCLASS="$TESTCLASS";
	else 
		TESTCLASS="picodedTests.$TESTCLASS";
	fi
	
	# Runs the test
	#----------------------------------------------------------
	echo "Test Class: $TESTCLASS"
	if [[ -z "$TESTFUNCTION" ]]
	then
		echo "---------------------------------------------------------------------------------"
		java -cp "./build-tools/junit/*:./bin/build/picodedJavaCommons-libsOnly.jar:./bin/classes" org.junit.runner.JUnitCore "$TESTCLASS"
	else
		echo "Function: $TESTFUNCTION"
		echo "---------------------------------------------------------------------------------"
		java -cp "./build-tools/junit/*:./bin/build/picodedJavaCommons-libsOnly.jar:./bin/classes" picodedTests.SingleJUnitTestRunner "$TESTCLASS#$TESTFUNCTION"
	fi
done
