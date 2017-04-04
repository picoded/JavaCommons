#!/bin/bash

workingDir="`dirname \"$0\"`"
cd "$workingDir" || exit 1

ant compile-tests

if [[ $? != 0 ]] 
then
	echo "---------------------------------------------------------------------------------"
	echo "! Compilation failed, aborting test";
else
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
		
		
		# Ensure test classes ends with the _test suffixes
		#------------------------------------------------------------------------------
		_TEST="_test";
		
		if [[ $TESTCLASS =~ \_test$ ]] 
		then
			TESTCLASS="$TESTCLASS";
		else 
			TESTCLASS="$TESTCLASS$_TEST";
		fi
		
		# No more picodedTest case prefix adding : Test cases now run in same namespace
		#------------------------------------------------------------------------------
		if [[ $TESTCLASS == picoded* ]] 
		then
			TESTCLASS="$TESTCLASS";
		else 
			TESTCLASS="picoded.$TESTCLASS";
		fi
		
		# Runs the test
		#----------------------------------------------------------
		echo "Test Class: $TESTCLASS"
		if [[ -z "$TESTFUNCTION" ]]
		then
			echo "---------------------------------------------------------------------------------"
			java -cp "./build-tools/junit/*;./bin/libs/*;./bin/classes" org.junit.runner.JUnitCore "$TESTCLASS"
		else
			echo "Function: $TESTFUNCTION"
			echo "---------------------------------------------------------------------------------"
			java -cp "./build-tools/junit/*;./bin/libs/*;./bin/classes" picoded.junit.SingleJUnitTestRunner "$TESTCLASS#$TESTFUNCTION"
		fi
	done
fi
