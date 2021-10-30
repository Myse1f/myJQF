#!/bin/bash

if [ $# -lt 4 ]; then
  echo "Usage: $0 <NAME> <TEST_CLASS> <TIME> <AUTOMATA>"
  exit 1
fi

set -e

pushd `dirname $0` > /dev/null
SCRIPT_DIR=`pwd`
popd > /dev/null

if [ ! -d "$JQF_DIR" ]; then
  echo "JQF_DIR is not set!"
  exit 2
fi

JQF_AUTOMATA="$JQF_DIR/bin/jqf-automata"
NAME=$1
TEST_CLASS="edu.berkeley.cs.jqf.examples.$2"
TIME=$3
AUTOMATA="$JQF_DIR/examples/target/test-classes/automata/$4"

BLACKBOX_OUT_DIR="$NAME-black-results"
REPLAY_OUT_DIR="$NAME-replay-results"
GREYBOX_OUT_DIR="$NAME-grey-results"

if [ -d "$JQF_OUT_DIR" ]; then
  echo "Error! There are already some results for $JQF_OUT_DIR in the given directory"
  echo "Please remove all the results in this directory or else use a different results directory"
  exit 3
fi

# Do not let GC mess with fuzzing
export JVM_OPTS="$JVM_OPTS -XX:-UseGCOverheadLimit"

# Run Blockbox Automata
timeout $TIME $JQF_AUTOMATA -n -c $($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithAutomataGenerator $AUTOMATA $BLACKBOX_OUT_DIR || [ $? -eq 124 ]
REPLAYNUM=$(tail -n 1 $BLACKBOX_OUT_DIR/plot_data | awk -F', ' '{print $5}')
echo "replay number: $REPLAYNUM"
$JQF_AUTOMATA -c $($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithAutomataGenerator $AUTOMATA $REPLAY_OUT_DIR $REPLAYNUM

# Run GreyBox Automata
timeout $TIME $JQF_AUTOMATA -c $($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithAutomataGenerator $AUTOMATA $GREYBOX_OUT_DIR || [ $? -eq 124 ]
