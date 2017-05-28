#!/bin/bash

scriptDir="`dirname \"$0\"`"
cd "$scriptDir" || exit 1
scriptDir=$(pwd)

cmd_to_run="$1"
workingDir="$2"
scan_dir="$3"

if [ -z "$workingDir" ]; then
	workingDir="`dirname \"$0\"`"
fi
if [ -z "$cmd_to_run" ]; then
	cmd_to_run="ant source"
fi
if [ -z "$scan_dir" ]; then
	scan_dir="$workingDir"
fi

cd "$workingDir" || exit 1
workingDir=$(pwd)

# Clear screen
printf "\033c";
echo "-------------------------------------------------------------------";
echo "Working Directory  : $workingDir";
echo "With this command  : $cmd_to_run";
echo "Scanning Directory : $scan_dir";
echo "-------------------------------------------------------------------";
echo "This script will automatically call '$cmd_to_run'";
echo "and output its response on any detected file change. Neat isnt it.";
echo "";
echo "Note that this script depends on : fswatch";
echo "> Installation instructions: https://github.com/emcrisostomo/fswatch";
echo "";
echo "Hopefully if your seeing this message, without any error belows";
echo "it means your ready to get this running. Just modify any file!";
echo "";
echo "Enjoys =)";
echo "~ eugene@picoded.com";
echo "";
echo "PS: Sam, you may want to port this to windows yourself =x";
echo "-------------------------------------------------------------------";
echo "";
echo "> Waiting for file change event inside, stand by  =| : $scan_dir ";
echo "";
echo "-------------------------------------------------------------------";
echo "";
echo "> Waiting ... ";

while true; do
	fswatch -r -1 -i "\\.java$" -e "\\.git.*" "$scan_dir";
	printf "\e[1A";
	printf "\e[1A";
	printf "\e[1A";
	printf "\e[1A";
	printf "\e[1A";
	printf "\e[1A";
	printf "\e[1A";
	printf "\e[1A";
	printf "\e[1A";
	echo "";
	echo "-------------------------------------------------------------------";
	echo "";
	echo "> Possible file changed detected, running command XD";
	echo "";
	echo "-------------------------------------------------------------------";
	"$scriptDir/clear-screen-and-output-full-command.sh" "$cmd_to_run" "$workingDir";
	echo "";
	echo "> Waiting for file change event inside, stand by  =| : $scan_dir ";
	echo "";
	echo "-------------------------------------------------------------------";
	echo "";
	echo "> Waiting ... ";
	sleep 1; # Delay induction
done
