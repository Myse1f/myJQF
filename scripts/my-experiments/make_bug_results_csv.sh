#!/bin/bash

# This script should be run inside an aggregated results directory (e.g. pre-baked or fresh-baked)
# Typically, this script will not be launched by a human but by other scripts

set -e

if [ $# -lt 1 ]; then
  echo "Usage: $0 REPS"
  exit 1
fi
REPS=$1

function repro_bugs() {
  pushd $1 > /dev/null
  echo -n "Analyzing test failures (bugs) for $1... "
  if [ $(ls $4 | wc -l) -ne 0 ]; then
    $JQF_DIR/bin/jqf-repro -r -c $($JQF_DIR/scripts/examples_classpath.sh) "edu.berkeley.cs.jqf.examples.$2" "$3" $4/id* 2>/dev/null 1>/dev/null
  fi
  echo "Done!"
  popd > /dev/null

}

function process() {
  bench=$1
  class=$2
  for i in $(seq 1 $3); do
    repro_bugs $bench-rnd-results-$i $class testWithGenerator failures
    repro_bugs $bench-zest-results-$i $class testWithGenerator failures
    repro_bugs $bench-automata-results-$i $class testWithAutomataGenerator failures
  done
}

process closure closure.CompilerTest $REPS
process rhino rhino.CompilerTest $REPS
process fastjson fastjson.JsonParseTest $REPS
process gson gson.JsonParseTest $REPS
