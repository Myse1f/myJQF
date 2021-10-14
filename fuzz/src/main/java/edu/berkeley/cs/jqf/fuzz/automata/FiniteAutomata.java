/**
 * Created By Yufan Wu
 * 2021/10/10
 */
package edu.berkeley.cs.jqf.fuzz.automata;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

public class FiniteAutomata {

    private static final Logger log  = LoggerFactory.getLogger(FiniteAutomata.class);

    private int numStates;
    private State[] states;
    private int initState;
    private int finalState;

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

    public String generateInput() {
        int state = initState;
        Random random = new Random();
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

    public static FiniteAutomata createAutomata(String automaFile) throws IOException {
        return createAutomata(new FileInputStream(automaFile));
    }

    public static FiniteAutomata createAutomata(FileInputStream in) throws IOException {
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
            for (Object trans : transitions) {
                JSONArray transition = (JSONArray) trans;
                state.addTransition(new Transition(transition.getInteger(1), transition.getString(2)));
            }

            states[idx] = state;
        });

        return new FiniteAutomata(numStates, states, initState, finalState);
    }

    public static void main(String[] args) throws IOException {
        FiniteAutomata automata = createAutomata("/Users/myse1f/Documents/Projects/myJQF/examples/src/test/java/edu/berkeley/cs/jqf/examples/json/json_automata.json");
        String input = "";
        while (input.length() < 50) {
            input = automata.generateInput();
        }
        System.out.println(input);
    }
}
