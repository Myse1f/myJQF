/**
 * Created By Yufan Wu
 * 2021/10/10
 */
package edu.berkeley.cs.jqf.fuzz.automata;

public class Terminal {
    private int state;
    private String symbol;
    private int transitionIdx;

    public Terminal(int state, String symbol, int transitionIdx) {
        this.state = state;
        this.symbol = symbol;
        this.transitionIdx = transitionIdx;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getTransitionIdx() {
        return transitionIdx;
    }

    public void setTransitionIdx(int transitionIdx) {
        this.transitionIdx = transitionIdx;
    }
}
