#!/bin/bash

if [ $# -lt 5 ]; then
  echo "Usage: $0 <NAME> <TEST_CLASS> <IDX> <TIME> <AUTOMATA>"
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

JQF_ZEST="$JQF_DIR/bin/jqf-zest"
JQF_AUTOMATA="$JQF_DIR/bin/jqf-automata"
NAME=$1
TEST_CLASS="edu.berkeley.cs.jqf.examples.$2"
IDX=$3
TIME=$4
AUTOMATA="$JQF_DIR/examples/target/test-classes/automata/$5"

e=$IDX

JQF_OUT_DIR="$NAME-zest-results-$e"
AUT_OUT_DIR="$NAME-automata-results-$e"
RND_OUT_DIR="$NAME-rnd-results-$e"

if [ -d "$JQF_OUT_DIR" ]; then
  echo "Error! There are already some results for $JQF_OUT_DIR in the given directory"
  echo "Please remove all the results in this directory or else use a different results directory"
  exit 3
fi

# Do not let GC mess with fuzzing
export JVM_OPTS="$JVM_OPTS -XX:-UseGCOverheadLimit"

# Run Automata
timeout $TIME $JQF_AUTOMATA -c $($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithAutomataGenerator $AUTOMATA $AUT_OUT_DIR || [ $? -eq 124 ]

# Run Zest
timeout $TIME $JQF_ZEST -c $($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithGenerator $JQF_OUT_DIR || [ $? -eq 124 ]

# Run Random
JVM_OPTS="$JVM_OPTS -Djqf.ei.TOTALLY_RANDOM=true" timeout $TIME $JQF_ZEST -c $($JQF_DIR/scripts/examples_classpath.sh) $TEST_CLASS testWithGenerator $RND_OUT_DIR || [ $? -eq 124 ]
