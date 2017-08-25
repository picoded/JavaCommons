#!/bin/bash

#
# Utility script used to replace the entire terminal window, 
# with the completed FULL output of a command, with the following
# additional tracking information.
#
# + Working Directory
# + Command that was called
# + Unix time when the command was called
# + How long it took
#
# To run this script, call it as following
#
#  ```./clear-screen-and-output-full-command.sh "command to run" ["working directory"]```
#
# Note if working directory does not exists, this simply assumes the script directory
#

cmd_to_run="$1"
workingDir="$2"

if [ -z "$workingDir" ]; then
	workingDir="`dirname \"$0\"`"
fi

cd "$workingDir" || exit 1

epochtime_start=$(date +%s)

cmd_output=$($cmd_to_run)

epochtime_end=$(date +%s)
start_readable_time=$(date -f %s -j $epochtime_start)
cmd_time_taken_ms=$(($epochtime_end - $epochtime_start))

# Clear screen
printf "\033c";
echo "-------------------------------------------------------------------";
echo "Working Directory : $workingDir";
echo "Ran this command  : $cmd_to_run";
echo "At this time      : $epochtime_start | $start_readable_time";
echo "For this long     : $cmd_time_taken_ms s";
echo "-------------------------------------------------------------------";
echo "";
echo "$cmd_output";
echo "";
echo "-------------------------------------------------------------------";

exit;
