package edu.berkeley.cs.jqf.fuzz.automata; /**
 * Created By Yufan Wu
 * 2021/10/16
 */

import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import org.junit.runner.Result;

import java.io.File;
import java.util.Random;

public class AutomataDriver {
    public static void main(String[] args) {
        if (args.length < 3){
            System.err.println("Usage: java " + AutomataDriver.class + " TEST_CLASS TEST_METHOD AUTOMATA_FILE [OUTPUT_DIR]");
            System.exit(1);
        }

        String testClassName  = args[0];
        String testMethodName = args[1];
        String automataFile = args[2];
        String outputDirectoryName = args.length > 3 ? args[3] : "fuzz-results";
        File outputDirectory = new File(outputDirectoryName);
        Long maxTrials = args.length > 4 ? Long.parseLong(args[4]) : null;

        try {
            // Load the guidance
            String title = testClassName+"#"+testMethodName;
            AutomataGuidance guidance = new AutomataGuidance(title, null, maxTrials, outputDirectory, new Random(), automataFile);
            if (maxTrials != null || System.getenv("JQF_DISABLE_INSTRUMENTATION") != null) {
                guidance.setBlind(true);
            }
            // Run the Junit test
            Result res = GuidedFuzzing.run(testClassName, testMethodName, guidance, System.out);
            if (Boolean.getBoolean("jqf.logCoverage")) {
                System.out.println(String.format("Covered %d edges.",
                        guidance.getTotalCoverage().getNonZeroCount()));
            }
            if (Boolean.getBoolean("jqf.ei.EXIT_ON_CRASH") && !res.wasSuccessful()) {
                System.exit(3);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

    }
}
