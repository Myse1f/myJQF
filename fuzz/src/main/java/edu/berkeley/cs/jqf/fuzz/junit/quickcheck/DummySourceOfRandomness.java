/**
 * Created By Yufan Wu
 * 2021/10/3
 */
package edu.berkeley.cs.jqf.fuzz.junit.quickcheck;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.Random;

public class DummySourceOfRandomness extends SourceOfRandomness {
    /**
     * Makes a new source of randomness.
     *
     * @param delegate a JDK source of randomness, to which the new instance
     *                 will delegate
     */
    public DummySourceOfRandomness(Random delegate, int seed) {
        super(new Random());
        setSeed(seed);
    }
}
