/**
 * Created By Yufan Wu
 * 2021/10/11
 */
package edu.berkeley.cs.jqf.fuzz.automata;

public class Transition {
    private int targetState;

    private String symbol;

    public Transition(int targetState, String symbol) {
        this.targetState = targetState;
        this.symbol = symbol;
    }

    public int getTargetState() {
        return targetState;
    }

    public void setTargetState(int targetState) {
        this.targetState = targetState;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
