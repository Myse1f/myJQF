/**
 * Created By Yufan Wu
 * 2021/10/22
 */
package edu.berkeley.cs.jqf.examples.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import edu.berkeley.cs.jqf.examples.common.Automata;
import edu.berkeley.cs.jqf.examples.common.AutomataGenerator;
import edu.berkeley.cs.jqf.examples.json.JsonGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
public class JsonParseTest {

    @Fuzz
    public void testWithString(@From(AsciiStringGenerator.class) String input) {
        try {
            Object obj = JSON.parse(input);
        } catch (JSONException e) {
            Assume.assumeNoException(e);
        }
    }

    @Fuzz
    public void testWithGenerator(@From(JsonGenerator.class) String input) {
        testWithString(input);
    }

    @Fuzz
    public void testWithAutomataGenerator(@From(AutomataGenerator.class)
                                                      @Automata("automata/json/json_automata.json") String input) {
        testWithString(input);
    }

    @Fuzz
    public void debugWithString(@From(AsciiStringGenerator.class) String code) {
        System.out.println("\nInput:  " + code);
        testWithString(code);
        System.out.println("Success!");
    }

    @Fuzz
    public void debugWithGenerator(@From(JsonGenerator.class) String input) {
        debugWithString(input);
    }

    @Fuzz
    public void debugWithAutomataGenerator(@From(AutomataGenerator.class)
                                          @Automata("automata/json/json_automata.json") String input) {
        debugWithString(input);
    }

    @Test
    public void test() {
        String input = "{\"a\": 1}";
        testWithString(input);
    }
}
