/**
 * Created By Yufan Wu
 * 2021/10/11
 */
package edu.berkeley.cs.jqf.fuzz.automata;

import java.util.ArrayList;
import java.util.List;

public class State {
    private int state;
    private List<Transition> transitions;

    public State(int state) {
        this.state = state;
        transitions = new ArrayList<>();
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

    public int size() {
        return transitions.size();
    }

    public Transition getTransition(int idx) {
        return transitions.get(idx);
    }
}
