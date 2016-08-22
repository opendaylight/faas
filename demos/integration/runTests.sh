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
  python testCases.py -t vc051
  sleep 4
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

runGbpFaasIntegrationDemo()
{
  read -n1 -r -p "Ready to compose fabric. Press any key to continue..." key
  ../demo-faas/composeFabric.py
  sleep 2
  python testCases.py -t vc02

  read -n1 -r -p "Ready to create Group Based Policy. Press any key to continue..." key
  python testCases.py -t vc034

  read -n1 -r -p "Ready to register endpoints. Press any key to continue..." key
  python testCases.py -t vc044
  sleep 4
  python testCases.py -t vc054

  read -n1 -r -p "Ready to demo endpoint migration. Press any key to continue..." key
  python testCases.py -t vc064
}


runGbpFaasMultiFabricDemo()
{
  read -n1 -r -p "Ready to create vcontainer. Press any key to continue..." key
  python testCases.py -t vc023

  read -n1 -r -p "Ready to create Group Based Policy. Press any key to continue..." key
  python testCases.py -t vc035

  read -n1 -r -p "Ready to register endpoints. Press any key to continue..." key
  python testCases.py -t vc045
  sleep 4
  python testCases.py -t vc055
}

showHelp()
{  
  echo "$0 -t 1     # create layer 3 logical network for sanity test"
  echo "$0 -t 2     # run GBP-FAAS integration demo"
  echo "$0 -t 3     # run GBP-FAAS integration demo with multi-fabric"
  echo "$0 -d       # delete layer 3 logical network for sanity test"
}

main() 
{
  while getopts "h?vf:t:d" opt; do
    case "$opt" in
    h|\?)
        showHelp
        exit 0
        ;;
    t)  
        if [[ $OPTARG == '1' ]]
        then
          createLogicalNetwork_layer3_demo
        elif [[ $OPTARG == '2' ]]
        then
          runGbpFaasIntegrationDemo
        elif [[ $OPTARG == '3' ]]
        then
          runGbpFaasMultiFabricDemo
        fi
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
