/**
 * Created By Yufan Wu
 * 2021/10/22
 */
package edu.berkeley.cs.jqf.examples.json;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.examples.common.AlphaStringGenerator;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Math.ceil;
import static java.lang.Math.log;

public class JsonGenerator extends Generator<String> {

    private GenerationStatus status;

    private static final int MAX_IDENTIFIERS = 100;
    private static final int MAX_DEPTH = 3;
    private int depth;
    private static Set<String> identifiers;

    public JsonGenerator() {
        super(String.class);
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        this.status = status;
        this.depth = 0;
        this.identifiers = new HashSet<>();
        return generateJson(random);
    }

    private String generateJson(SourceOfRandomness random) {
        depth++;
        String result;
        if (depth >= MAX_DEPTH || random.nextBoolean()) {
            result = generateLiteral(random);
        } else {
            result = random.choose(Arrays.<Function<SourceOfRandomness, String>>asList(
                    this::generateObject,
                    this::generateArray
            )).apply(random);
        }
        depth--;
        return result;
    }

    private String generateObject(SourceOfRandomness random) {
        return "{" + String.join(", ", generateItems(this::generateProperty, random, 3)) + "}";
    }

    private String generateProperty(SourceOfRandomness random) {
        return generateKey(random) + ":" + generateJson(random);
    }

    private String generateArray(SourceOfRandomness random) {
        return "[" + String.join(", ", generateItems(this::generateJson, random, 3)) + "]";
    }

    private String generateLiteral(SourceOfRandomness random) {
        return random.choose(Arrays.<Supplier<String>>asList(
                () -> String.valueOf(random.nextInt(-10, 1000)),
                () -> String.valueOf(random.nextDouble()),
                () -> String.valueOf(random.nextBoolean()),
                () -> '"' + new AlphaStringGenerator().generate(random, status) + '"',
                () -> "null"
        )).get();
    }

    private String generateKey(SourceOfRandomness random) {
        // Either generate a new identifier or use an existing one
        String identifier;
        if (identifiers.isEmpty() || (identifiers.size() < MAX_IDENTIFIERS && random.nextBoolean())) {
            identifier = random.nextChar('a', 'z') + "_" + identifiers.size();
            identifiers.add(identifier);
        } else {
            identifier = random.choose(identifiers);
        }

        return "\"" + identifier + "\"";
    }

    private static int sampleGeometric(SourceOfRandomness random, double mean) {
        double p = 1 / mean;
        double uniform = random.nextDouble();
        return (int) ceil(log(1 - uniform) / log(1 - p));
    }

    private static <T> List<T> generateItems(Function<SourceOfRandomness, T> generator, SourceOfRandomness random,
                                             double mean) {
        int len = sampleGeometric(random, mean);
        List<T> items = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            items.add(generator.apply(random));
        }
        return items;
    }
}
