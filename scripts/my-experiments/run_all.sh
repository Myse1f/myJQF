#!/bin/bash

# Check usage
if [ $# -lt 3 ]; then
  echo "Usage: $0 RESULTS_DIR TIME REPS"
  exit 1
fi

set -e

# Figure out script absolute path
pushd `dirname $0` > /dev/null
SCRIPT_DIR=`pwd`
popd > /dev/null

out_dir=$1
time=$2
id1=1
id2=$3

mkdir -p "$out_dir"
cd "$out_dir"

# run tech comparison
# for id in $(seq $id1 $id2); do

#   $SCRIPT_DIR/run_benchmark.sh closure closure.CompilerTest $id $time js/js_automata.json

#   $SCRIPT_DIR/run_benchmark.sh rhino rhino.CompilerTest $id $time js/js_automata.json

#   $SCRIPT_DIR/run_benchmark.sh fastjson fastjson.JsonParseTest $id $time json/json_automata.json

#   $SCRIPT_DIR/run_benchmark.sh gson gson.JsonParseTest $id $time json/json_automata.json

# done

# run blackbox vs greybox
$SCRIPT_DIR/run_blackvsgrey.sh closure closure.CompilerTest $time js/js_automata.json

$SCRIPT_DIR/run_blackvsgrey.sh rhino rhino.CompilerTest $time js/js_automata.json

$SCRIPT_DIR/run_blackvsgrey.sh fastjson fastjson.JsonParseTest $time json/json_automata.json

$SCRIPT_DIR/run_blackvsgrey.sh gson gson.JsonParseTest $time json/json_automata.json