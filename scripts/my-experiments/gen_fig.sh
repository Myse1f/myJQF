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

# Process all the result.csv files to calculate MTF and repeatibility for each bu
python3 $SCRIPT_DIR/gen_fig.py $results_dir $reps

echo "Done!"