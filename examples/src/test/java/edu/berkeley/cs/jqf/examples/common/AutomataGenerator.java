/**
 * Created By Yufan Wu
 * 2021/10/14
 */
package edu.berkeley.cs.jqf.examples.common;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.automata.FiniteAutomata;

import java.io.*;

public class AutomataGenerator extends Generator<String> {
    private FiniteAutomata automata;

    public AutomataGenerator() {
        super(String.class);
    }

    /**
     * Configures the Automata used by this generator to produce  structural input
     *
     * @param automata the automata file
     * @throws IOException if the automata file cannot be read
     */
    public void configure(Automata automata) throws IOException {
        String file = automata.value();
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(file)) {
            if (in == null) {
                throw new FileNotFoundException("Automata file not found: " + file);
            }
            this.automata = FiniteAutomata.createAutomata(in);
        }
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        return automata.generateInputWithState(random);
    }
}
