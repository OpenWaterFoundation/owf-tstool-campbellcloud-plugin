#!/bin/bash

LOGFILE="/var/log/systems/PA-Commission-SRBC/download-campbell-cloud-data.tstool.cron.log"
# This appends to the logfile, which is not required and would bloat the system.
#exec &> >(tee -a "$LOGFILE")
# If append is needed, use exec >>
exec > ${LOGFILE} 2>&1

# Script to run the tstool command file:
# - this can be run manually or run from cron
# - the log file should capture stderr and stdout
# - the NovaStar scheduler does not allow redirecting output so use the exec command above
# - if running in Git Bash during development, /var and child folders must be created 
#   (use cygpath -w / to see where files exist, typically the user's files:  C:\Users\user\AppData\Local\Programs\Git\)

# Get the start time in seconds:
startSeconds=$(date +%s)

# Get the location of this script.
scriptFolder=$(cd $(dirname "$0") && pwd)

echo "Starting run at $(date)"
# Default TSTool is to find in the path.
tstoolScript=tstool
debug=""
if [ "$(hostname)" = "crystal" ]; then
  # Steve's development environment - use the latest TSTool:
  # - the run script determines the classpath
  # - the --dev parameter is needed to properly find plugins, etc.
  # - use a TSTool command parameter --debug 0,2 to turn on debug for troubleshooting
  #debug=" --debug 0,2"
  tstoolScript="/C/Users/${USERNAME}/cdss-dev/TSTool/git-repos/cdss-app-tstool-main/resources/runtime/bin/tstool --dev"
  :
fi
runGUI=false
if [ ${runGUI} = "true" ]; then
  # Run the TSTool GUI interactively, useful for troubleshooting.
  ${tstoolScript} -- ${debug} --disable-datastores=* --enable-datastores=nsdataws-nscloud,CampbellCloud-SRBC
else
  # Run TSTool in batch mode:
  # - set the timeout to be shorter than the expected scheduler interval (5 minutes) to avoid multiple processes running at the same time
  timeout 250 ${tstoolScript} --nodiscovery --headless -- ${debug} --nomaingui --disable-datastores=* --enable-datastores=nsdataws-nscloud,CampbellCloud-SRBC --commands ${scriptFolder}/download-campbell-cloud-data.tstool
fi
tstoolExitStatus=$?
# Get the start time in seconds:
endSeconds=$(date +%s)
runSeconds=$(( ${endSeconds} - ${startSeconds} ))
echo "TSTool exit status is ${tstoolExitStatus}"
echo "Ending run at $(date)"
echo "Run time = ${runSeconds} seconds"
exit ${tstoolExitStatus}
