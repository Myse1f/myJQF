/*
 * Copyright (c) 2018, University of California, Berkeley
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.cs.jqf.fuzz.ei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import edu.berkeley.cs.jqf.fuzz.ei.ExecutionIndexingGuidance.Input;
import edu.berkeley.cs.jqf.fuzz.ei.ExecutionIndexingGuidance.InputLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionIndexingGuidanceTest {

    private static Random r;

    private ExecutionIndex e1 = new ExecutionIndex(new int[]{1,1});
    private ExecutionIndex e2 = new ExecutionIndex(new int[]{1,1,3,1}); // Same EC as e4/e6
    private ExecutionIndex e3 = new ExecutionIndex(new int[]{1,2,2,1});
    private ExecutionIndex e4 = new ExecutionIndex(new int[]{1,2,3,4}); // Same EC as e2/e6
    private ExecutionIndex e5 = new ExecutionIndex(new int[]{5,5,5,5});
    private ExecutionIndex e6 = new ExecutionIndex(new int[]{1,6,3,6}); // Same EC as e2/e4


    @Before
    public void seedRandom() {
        r = new Random(42);
    }

    @Test
    public void testGetOrFresh() {
        Input input = new Input();
        int k1a = input.getOrGenerateFresh(e1, r);
        int k1b = input.getOrGenerateFresh(e1, r);
        assertEquals(k1a, k1b);

        int k2a = input.getOrGenerateFresh(e2, r);
        int k2b = input.getOrGenerateFresh(e2, r);
        assertEquals(k2a, k2b);

        int k1c = input.getOrGenerateFresh(e1, r);
        assertEquals(k1a, k1c);
    }

    @Test
    public void testClone() {
        Input input = new Input();
        int k1 = input.getOrGenerateFresh(e1, r);
        int k2 = input.getOrGenerateFresh(e2, r);
        int k3 = input.getOrGenerateFresh(e3, r);
        int k4 = input.getOrGenerateFresh(e4, r);
        int k5 = input.getOrGenerateFresh(e5, r);

        Input clone = new Input(input);
        assertEquals(k1, clone.getOrGenerateFresh(e1, r));
        assertEquals(k2, clone.getOrGenerateFresh(e2, r));
        assertEquals(k3, clone.getOrGenerateFresh(e3, r));
        assertEquals(k4, clone.getOrGenerateFresh(e4, r));
        assertEquals(k5, clone.getOrGenerateFresh(e5, r));

    }


    @Test
    public void testGc() {
        Input input = new Input();
        int k1 = input.getOrGenerateFresh(e1, r);
        int k2 = input.getOrGenerateFresh(e2, r);
        int k3 = input.getOrGenerateFresh(e3, r);
        int k4 = input.getOrGenerateFresh(e4, r);
        int k5 = input.getOrGenerateFresh(e5, r);

        Input clone = new Input(input);
        assertEquals(k1, clone.getOrGenerateFresh(e1, r));
        assertEquals(k5, clone.getOrGenerateFresh(e5, r));
        assertEquals(k2, clone.getOrGenerateFresh(e2, r));

        clone.gc();

        assertNull(clone.getValueAtKey(e3));
        assertNull(clone.getValueAtKey(e4));

    }

    @Test
    public void testExecutionContexts() {
        assertEquals(new ExecutionContext(e2), new ExecutionContext(e4));
        assertEquals(new ExecutionContext(e4), new ExecutionContext(e6));
        assertEquals(new ExecutionContext(e6), new ExecutionContext(e2));
        assertNotEquals(new ExecutionContext(e1), new ExecutionContext(e2));
        assertNotEquals(new ExecutionContext(e4), new ExecutionContext(e5));
        assertNotEquals(new ExecutionContext(e1), new ExecutionContext(e5));
    }


    @Test
    public void testSplice() {
        Input srcInput = new Input();
        srcInput.setValueAtKey(e1, 23);
        srcInput.setValueAtKey(e2, 46);
        srcInput.setValueAtKey(e3, 69);
        srcInput.setValueAtKey(e4, 92);

        Input baseInput = new Input();
        baseInput.setValueAtKey(e3, 12);
        baseInput.setValueAtKey(e4, 24);
        baseInput.setValueAtKey(e5, 36);
        baseInput.setValueAtKey(e6, 48);

        // Simulate executions
        srcInput.getOrGenerateFresh(e1, r);
        srcInput.getOrGenerateFresh(e2, r);
        srcInput.getOrGenerateFresh(e3, r);
        srcInput.getOrGenerateFresh(e4, r);
        srcInput.gc();
        baseInput.getOrGenerateFresh(e3, r);
        baseInput.getOrGenerateFresh(e4, r);
        baseInput.getOrGenerateFresh(e5, r);
        baseInput.getOrGenerateFresh(e6, r);
        baseInput.gc();


        // Map EC of e2 (= EC of e4 or EC of e6) to locations in srcInput
        Map<ExecutionContext, ArrayList<InputLocation>>
                ecToInputLoc = new HashMap<>();
        ecToInputLoc.put(new ExecutionContext(e2),
                new ArrayList<>(Arrays.asList(new InputLocation[]{
                        new InputLocation(srcInput, 1), // e2
                        new InputLocation(srcInput, 3), // e4
                })));

        Random mockRandom = Mockito.mock(Random.class);
        when(mockRandom.nextBoolean())
                .thenReturn(true)  // Yes to splicing
                .thenReturn(false); // No to havoc
        when(mockRandom.nextInt(anyInt()))
                .thenReturn(1)  // Pick target offset as e4
                .thenReturn(0)  // Pick first input location
                .thenReturn(1); // Splice 1+1 bytes

        Input fuzzedInput = baseInput.fuzz(mockRandom, ecToInputLoc);

        assertEquals(12, fuzzedInput.getOrGenerateFresh(e3, r));
        assertEquals(46, fuzzedInput.getOrGenerateFresh(e4, r));
        assertEquals(69, fuzzedInput.getOrGenerateFresh(e5, r));
        assertEquals(48, fuzzedInput.getOrGenerateFresh(e6, r));



    }
}
