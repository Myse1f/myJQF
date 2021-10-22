/**
 * Created By Yufan Wu
 * 2021/10/10
 */
package edu.berkeley.cs.jqf.fuzz.automata;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class FiniteAutomata {

    private static final Logger log  = LoggerFactory.getLogger(FiniteAutomata.class);

    private int numStates;
    private State[] states;
    private int initState;
    private int finalState;

    private Random jdkRandom = new Random();

    public FiniteAutomata(int numStates, State[] states, int initState, int finalState) {
        this.numStates = numStates;
        this.states = states;
        this.initState = initState;
        this.finalState = finalState;
    }

    public int getNumStates() {
        return numStates;
    }

    public void setNumStates(int numStates) {
        this.numStates = numStates;
    }

    public State[] getStates() {
        return states;
    }

    public State getState(int state) {
        return states[state];
    }

    public void setStates(State[] states) {
        this.states = states;
    }

    public int getInitState() {
        return initState;
    }

    public void setInitState(int initState) {
        this.initState = initState;
    }

    public int getFinalState() {
        return finalState;
    }

    public void setFinalState(int finalState) {
        this.finalState = finalState;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FiniteAutomata: \n");
        sb.append("\tnumStates: ").append(numStates);
        sb.append("\n");
        sb.append("\tinitState: ").append(initState);
        sb.append("\n");
        sb.append("\tfinalState: ").append(finalState);
        sb.append("\n");
        return sb.toString();
    }

    public String generateInput(Random random) {
        int state = initState;
        StringBuilder sb = new StringBuilder();
        while (state != finalState) {
            State stateData = states[state];
            int size = stateData.size();
            int randTrans = random.nextInt(size);
            Transition transition = stateData.getTransition(randTrans);
            sb.append(transition.getSymbol());
            state = transition.getTargetState();
        }
        return sb.toString();
    }

    public String generateInput(SourceOfRandomness random) {
        int state = initState;
        StringBuilder sb = new StringBuilder();
        while (state != finalState) {
            State stateData = states[state];
            int size = stateData.size();
            int randTrans = random.nextInt(size);
            Transition transition = stateData.getTransition(randTrans);
            sb.append(transition.getSymbol());
            state = transition.getTargetState();
        }
        return sb.toString();
    }

    public String generateInputWithState(SourceOfRandomness random) {
        StringBuilder sb = new StringBuilder();
        jdkRandom.setSeed(random.nextInt());
        int state = initState;
        int prev = state;
        while (state != finalState){
            state = random.nextInt();
            State stateData = states[prev];
            Terminal terminal = stateData.getTerminal(state);
            String symbol = handleSymbol(terminal.getSymbol());
            sb.append(symbol);
            prev = state;
        }
        return sb.toString();
    }

    private String handleSymbol(String symbol) {
        if (symbol.startsWith("$$") && symbol.endsWith("$$")) {
            switch (symbol) {
                case "$$INTEGER$$": symbol = jdkRandom.nextInt(65536) + ""; break;
                case "$$NUMBER$$": symbol = jdkRandom.nextDouble() + ""; break;
                case "$$STRING$$": symbol = "\"" + RandomStringUtils.random(jdkRandom.nextInt(10)+1, 32, 126, false, false, null, jdkRandom) +"\""; break;
                case "$$IDENTITY$$": symbol = RandomStringUtils.random(jdkRandom.nextInt(10)+1, '0', 'z', true, true, null, jdkRandom);  break;
            }
        }
        return symbol;
    }

    public static FiniteAutomata createAutomata(String automaFile) throws IOException {
        return createAutomata(new FileInputStream(automaFile));
    }

    public static FiniteAutomata createAutomata(InputStream in) throws IOException {
        String content = IOUtils.toString(in, "utf8");
        JSONObject automataJson = JSONObject.parseObject(content);

        int initState = automataJson.getInteger("init_state");
        int finalState = automataJson.getInteger("final_state");
        int numStates = automataJson.getInteger("numstates")  + 1;

        log.debug("initState: {}, finalState: {}, numStates: {}",  initState, finalState, numStates);

        State[] states = new State[numStates];
        JSONObject pda = automataJson.getJSONObject("pda");

        pda.forEach((k, v) -> {
            int idx = Integer.parseInt(k);
            JSONArray transitions = (JSONArray) v;
            State state = new State(idx);
            for (int transitionIdx = 0; transitionIdx < transitions.size(); transitionIdx++) {
                JSONArray transition = (JSONArray) transitions.get(transitionIdx);
                int targetState = transition.getInteger(1);
                String symbol = transition.getString(2);
                state.addTransition(new Transition(targetState, symbol));
                state.addTerminal(targetState, new Terminal(idx, symbol, transitionIdx));
            }
            states[idx] = state;
        });

        return new FiniteAutomata(numStates, states, initState, finalState);
    }
}
