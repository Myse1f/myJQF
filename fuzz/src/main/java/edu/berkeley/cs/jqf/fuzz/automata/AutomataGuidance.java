/**
 * Created By Yufan Wu
 * 2021/10/15
 */
package edu.berkeley.cs.jqf.fuzz.automata;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;

public class AutomataGuidance extends ZestGuidance {
    private static final int RECURSIVE_THRESHOLD = 6;

    private FiniteAutomata automata;

    public AutomataGuidance(String testName, Duration duration, Long trials, File outputDirectory, Random sourceOfRandomness, String automataFile) throws IOException {
        super(testName, duration, trials, outputDirectory, sourceOfRandomness);
        automata = FiniteAutomata.createAutomata(automataFile);
    }

    /* Returns the banner to be displayed on the status screen */
    protected String getTitle() {
        if (blind) {
            return  "Generator-based random fuzzing (no guidance)\n" +
                    "--------------------------------------------\n";
        } else {
            return  "Semantic Fuzzing with Automata\n" +
                    "--------------------------\n";
        }
    }

    protected InputStream createParameterStream() {
        // Return an input stream that reads bytes from a linear array
        return new InputStream() {
            int bytesRead = 0;

            @Override
            public int read() throws IOException {
                assert currentInput instanceof LinearInput : "AutomataGuidance should only mutate TerminalInput(s)";

                TerminalInput terminalInput = (TerminalInput) currentInput;
                // Attempt to get a value from the list, we only get int from TerminalInput
                // But read() can only read 1 byte at a time
                int ret = terminalInput.getOrGenerateFresh(bytesRead / 4, random);
                int bytes = bytesRead % 4;
                ret >>= (bytes * 8);
                ret &= 0xff;
                bytesRead++;
                // infoLog("read(%d) = %d", bytesRead, ret);
                return ret;
            }
        };
    }

    public Input<?> createFreshInput() {
        TerminalInput input = new TerminalInput();
        input.genNewInput(automata.getInitState());
        return input;
    }

    public class TerminalInput extends Input<Integer> {
        private List<Integer> terminals;
        private Set<Integer> recursiveState = new HashSet<>();
        private Map<Integer, List<Integer>> stateIndex = new HashMap<>();

        boolean init = false;

        public TerminalInput() {
            super();
            this.terminals = new ArrayList<>();
        }

        public TerminalInput(TerminalInput other) {
            super(other);
            this.terminals = new ArrayList<>(other.terminals);
        }

        public void genNewInput(int state) {
            // create an random input sequence
            int finalState = automata.getFinalState();
            while (state != finalState) {
                terminals.add(state);
                State stateData = automata.getState(state);
                int size = stateData.size();
                int randTrans = random.nextInt(size);
                Transition transition = stateData.getTransition(randTrans);
                state = transition.getTargetState();
            }
            terminals.add(finalState);
        }

        private void initRecursiveFeature() {
            if (!init) {
                // map state to index
                for (int i = 0; i < terminals.size(); i++) {
                    int state = terminals.get(i);
                    if (!stateIndex.containsKey(state)) {
                        stateIndex.put(state, new ArrayList<>());
                    }
                    List<Integer> index = stateIndex.get(state);
                    index.add(i);
                    if (index.size() >= 2) {
                        recursiveState.add(state);
                    }
                }
                init = true;
            }
        }

        @Override
        public int getOrGenerateFresh(Integer key, Random random) {
            // If it exists in the list, return it
            if (key < terminals.size()) {
                // infoLog("Returning old byte at key=%d, total requested=%d", key, requested);
                return terminals.get(key);
            }

            throw new RuntimeException("Reading data out of size");
        }

        @Override
        public int size() {
            return terminals.size();
        }

        @Override
        public Input fuzz(Random random) {
            // todo splice, recursive, random mutate, random generate
            initRecursiveFeature();
            TerminalInput newInput = null;
            int initState = automata.getInitState();
            int choice = random.nextInt(4);
            switch (choice) {
                case 0:
                    // splice with other input
                    if (savedInputs.isEmpty()) {
                        newInput = new TerminalInput();
                        newInput.genNewInput(initState);
                    } else {
                        int len = savedInputs.size();
                        int randIdx = random.nextInt(len);
                        TerminalInput spliceInput = (TerminalInput) savedInputs.get(randIdx);
                        // identify potential splice point
                        List<int[]> splicePoints = new ArrayList<>();
                        for (int i = 0; i <  spliceInput.terminals.size(); i++) {
                            int s = spliceInput.terminals.get(i);
                            if (stateIndex.containsKey(s)) {
                                List<Integer> index = stateIndex.get(s);
                                splicePoints.add(new int[]{index.get(random.nextInt(index.size())), i});
                            }
                        }
                        int[] splicePoint = splicePoints.get(random.nextInt(splicePoints.size()));
                        newInput = splice(splicePoint[0], spliceInput, splicePoint[1]);
                    }
                    break;
                case 1:
                    // self recursive
                    newInput = new TerminalInput();
                    if (recursiveState.isEmpty()) {
                        newInput.genNewInput(initState);
                    } else {
                        List<Integer> tmp = new ArrayList<>(recursiveState);
                        int randState = tmp.get(random.nextInt(tmp.size()));
                        List<Integer> indices = stateIndex.get(randState);
                        Collections.shuffle(indices);
                        int firstIndex = indices.get(0);
                        int secondIndex = indices.get(1);
                        List<Integer> feature;
                        if (firstIndex < secondIndex) {
                            feature = slice(firstIndex, secondIndex);
                        } else {
                            feature = slice(secondIndex, firstIndex);
                        }
                        for (int i = 0; i < firstIndex; i++) {
                            newInput.terminals.add(terminals.get(i));
                        }
                        int randRecursive = random.nextInt(RECURSIVE_THRESHOLD);
                        for (int i = 0; i < randRecursive; i++) {
                            newInput.terminals.addAll(feature);
                        }
                        for (int i = secondIndex; i < terminals.size(); i++) {
                            newInput.terminals.add(terminals.get(i));
                        }
                    }
                    break;
                case 2:
                    // self random mutate
                    newInput = new TerminalInput();
                    int randIdx = random.nextInt(terminals.size());
                    for (int i = 0; i < randIdx; i++) {
                        newInput.terminals.add(terminals.get(i));
                    }
                    newInput.genNewInput(terminals.get(randIdx));
                    break;
                case 3:
                    // generate an random input
                    newInput = new TerminalInput();
                    newInput.genNewInput(initState);
                    break;
            }
            return newInput;
        }

        @Override
        public void gc() {
            // ignore
        }

        @Override
        public Iterator<Integer> iterator() {
            return terminals.iterator();
        }

        private TerminalInput splice(int origIdx, TerminalInput spliceInput, int spliceIdx) {
            TerminalInput newInput = new TerminalInput();
            for (int i = 0; i < origIdx; i++) {
                newInput.terminals.add(terminals.get(i));
            }
            for (int i = spliceIdx; i < spliceInput.terminals.size(); i++) {
                newInput.terminals.add(spliceInput.terminals.get(i));
            }
            return newInput;
        }

        private List<Integer> slice(int start, int end) {
            return terminals.subList(start, end);
        }
    }
}
