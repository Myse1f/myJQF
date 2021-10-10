#!/bin/bash

GRAMMAR_FILE=$1
GRAMMAR_DIR="$(dirname $GRAMMAR_FILE)"
START="$2"
STACK_LIMIT="$3"

# Get filename
FILE=$(basename -- "$GRAMMAR_FILE")
echo "File:$FILE"
FILENAME="${FILE%.*}"
echo "Name:$FILENAME"

# Create the GNF form of the grammar
CMD="python3 cfg2gnf.py --grammar $GRAMMAR_FILE --out ${FILENAME}_gnf.json --start $START"
$CMD

# Generate grammar automaton
# Check if user provided a stack limit
if [ -z "${STACK_LIMIT}" ]; then
CMD="python3 gnf2automata.py --grammar ${FILENAME}_gnf.json --out ${FILENAME}_automata.json"
else
CMD="python3 gnf2automata.py --grammar ${FILENAME}_gnf.json --out ${FILENAME}_automata.json --limit ${STACK_LIMIT}"
fi
echo $CMD
$CMD

echo "Copying ${FILENAME}_automata.json to $GRAMMAR_DIR"
mv "${FILENAME}_automata.json" $GRAMMAR_DIR/