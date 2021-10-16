/**
 * Created By Yufan Wu
 * 2021/10/11
 */
package edu.berkeley.cs.jqf.fuzz.automata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class State {
    private int state;
    private List<Transition> transitions;
    private Map<Integer, Terminal> terminals;

    public State(int state) {
        this.state = state;
        transitions = new ArrayList<>();
        terminals = new HashMap<>();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<Transition> transitions) {
        this.transitions = transitions;
    }

    public void addTransition(Transition t) {
        transitions.add(t);
    }

    public void addTerminal(int state, Terminal t) {
        terminals.put(state, t);
    }

    public int size() {
        return transitions.size();
    }

    public Transition getTransition(int idx) {
        return transitions.get(idx);
    }

    public Terminal getTerminal(int state) {
        return terminals.get(state);
    }
}
