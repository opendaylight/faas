#!/bin/bash

#
# Manifasted constants and macros
#
OPTIND=1         # Reset in case getopts has been used previously in the shell.
ERRCODE=85

#
# Global variables
#
verbose_g=0
outputFile_g=""

#
# Function implementations
#
err()
{
  echo "$@"
  exit $ERRCODE
}

createLogicalNetwork_layer3_demo()
{
  python testCases.py -t vc01
  sleep 1
  python testCases.py -t vc02
  sleep 2
  python testCases.py -t vc03
  sleep 2
  python testCases.py -t vc04
  sleep 4
  #python testCases.py -t vc05
  #sleep 4
  echo; echo "Retrieve Topology:"; echo
  python testCases.py -t p1
  echo; echo; echo "Retrieve ACL:"; echo;
  python testCases.py -t p5
}

removeLogicalNetwork_layer3_demo()
{
  python testCases.py -t vc06
  sleep 3
  python testCases.py -t vc07
  echo; echo "Retrieve Topology:"; echo
  sleep 3
  python testCases.py -t p1
  echo; echo; echo "Retrieve ACL:"; echo;
  python testCases.py -t p5
} 

showHelp()
{  
  echo "$0 -c      # create layer 3 logical network demo"
  echo "$0 -d      # delete layer 3 logical network"
}

main() 
{
  while getopts "h?vf:cd" opt; do
    case "$opt" in
    h|\?)
        showHelp
        exit 0
        ;;
    c)  
        createLogicalNetwork_layer3_demo
        exit 0
        ;;
    d)  
        removeLogicalNetwork_layer3_demo
        exit 0
        ;;
    v)  verbose=1
        ;;
    f)  outputFile_g=$OPTARG
        ;;
    esac
  done

  shift $((OPTIND-1))

  [ "$1" = "--" ] && shift

  showHelp

}

#
# Main line
#

main $@

exit 0
