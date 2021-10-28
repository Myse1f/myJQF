#!/bin/bash

# Check usage
if [ $# -lt 2 ]; then
  echo "Usage: $0 RESULTS_DIR REPS"
  exit 1
fi

set -e

# Figure out script absolute path
pushd `dirname $0` > /dev/null
SCRIPT_DIR=`pwd`
popd > /dev/null

results_dir=$1
reps=$2

# The remaining two scripts are run inside the aggregate results dir
cd "$results_dir"

# Create the directory where plots and CSVs will be written
mkdir -p figs

# Repro all the test failures to get exception types
$SCRIPT_DIR/make_bug_results_csv.sh $reps

# Process all the result.csv files to calculate MTF and repeatibility for each bu
python3 $SCRIPT_DIR/calculate_bug_times.py $reps

echo "Done! Table is ready in $results_dir/figs/Table.csv"
