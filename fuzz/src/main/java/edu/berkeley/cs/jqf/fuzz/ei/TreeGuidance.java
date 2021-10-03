/**
 * Created By Yufan Wu
 * 2021/9/29
 */
package edu.berkeley.cs.jqf.fuzz.ei;

import com.pholser.junit.quickcheck.generator.GenerationStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class TreeGuidance extends ZestGuidance {

    private static int MAX_DEPTH = 10;
    private static int MAX_WIDTH = 300;

    private GenerationStatus status;
    public static final GenerationStatus.Key<Integer> DEPTH = new GenerationStatus.Key<>("depth", Integer.class);
    public static final GenerationStatus.Key<Integer> POS = new GenerationStatus.Key<>("pos", Integer.class);

    public TreeGuidance(String testName, Duration duration, Long trials, File outputDirectory, Random sourceOfRandomness) throws IOException {
        super(testName, duration, trials, outputDirectory, sourceOfRandomness);
    }

    public TreeGuidance(String testName, Duration duration, Long trials, File outputDirectory, File[] seedInputFiles, Random sourceOfRandomness) throws IOException {
        super(testName, duration, trials, outputDirectory, seedInputFiles, sourceOfRandomness);
    }

    public TreeGuidance(String testName, Duration duration, Long trials, File outputDirectory, File seedInputDir, Random sourceOfRandomness) throws IOException {
        super(testName, duration, trials, outputDirectory, seedInputDir, sourceOfRandomness);
    }

    public TreeGuidance(String testName, Duration duration, File outputDirectory, File seedInputDir) throws IOException {
        super(testName, duration, outputDirectory, seedInputDir);
    }

    public TreeGuidance(String testName, Duration duration, File outputDirectory) throws IOException {
        super(testName, duration, outputDirectory);
    }

    public TreeGuidance(String testName, Duration duration, File outputDirectory, File[] seedFiles) throws IOException {
        super(testName, duration, outputDirectory, seedFiles);
    }


    /* Returns the banner to be displayed on the status screen */
    protected String getTitle() {
        if (blind) {
            return  "Generator-based random fuzzing (no guidance)\n" +
                    "--------------------------------------------\n";
        } else {
            return  "Semantic Fuzzing with Tree\n" +
                    "--------------------------\n";
        }
    }

    public Input<?> createFreshInput() {
        return new TreeInput();
    }

    protected InputStream createParameterStream() {
        // Return an input stream that reads bytes from a linear array
        return new InputStream() {

            @Override
            public int read() throws IOException {
                assert currentInput instanceof TreeInput : "TreeGuidance should only mutate TreeInput(s)";


                TreeInput treeInput = (TreeInput) currentInput;

                int depth = status.valueOf(DEPTH).orElse(-1);
                int pos = status.valueOf(POS).orElse(-1);
                int ret = 0;
                if (depth < 0 || pos < 0) {
                    ret = random.nextInt();
                } else {
                    ret = treeInput.getOrGenerateFresh(depth * MAX_WIDTH + pos, random);
                }
                // infoLog("read(%d) = %d", bytesRead, ret);
                return ret;
            }
        };
    }

    public GenerationStatus getStatus() {
        return status;
    }

    public void setStatus(GenerationStatus status) {
        this.status = status;
    }


    public class TreeInput extends Input<Integer> {
        private int[][]  seeds;
        private int curPos = -1;

        public TreeInput() {
            super();
            seeds = new int[MAX_DEPTH][MAX_WIDTH];
        }

        public TreeInput(TreeInput other) {
            super(other);
            seeds = new int[MAX_DEPTH][];
            for (int i = 0; i < MAX_DEPTH; i++) {
                seeds[i] = Arrays.copyOf(other.seeds[i], MAX_WIDTH);
            }
        }

        @Override
        public int getOrGenerateFresh(Integer key, Random random) {
            if (curPos == -1) {
                for (int i = 0; i < MAX_DEPTH; i++) {
                    for (int j = 0; j < MAX_WIDTH; j++) {
                        seeds[i][j] = random.nextInt();
                    }
                }
                curPos = 0;
            }
            int r = key / MAX_WIDTH;
            int c  = key % MAX_WIDTH;
            return seeds[r][c];

        }

        @Override
        public int size() {
            return MAX_DEPTH * MAX_WIDTH;
        }

        @Override
        public Input fuzz(Random random) {
            // todo make fuzz with heuristic algorithm
            // Clone this input to create initial version of new child
            TreeInput newInput = new TreeInput(this);

            // Stack a bunch of mutations
            int numMutations = sampleGeometric(random, MEAN_MUTATION_COUNT);
            newInput.desc += ",havoc:"+numMutations;

            for (int mutation = 1; mutation <= numMutations; mutation++) {
                // Select a random offset and size
                int offset = random.nextInt(newInput.size());
                int row = offset / MAX_WIDTH;
                int col = offset % MAX_WIDTH;
                newInput.seeds[row][col] = random.nextInt();
            }

            return newInput;
        }

        @Override
        public void gc() {

        }

        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return curPos > 0 && curPos < MAX_WIDTH * MAX_WIDTH;
                }

                @Override
                public Integer next() {
                    int row = curPos / MAX_WIDTH;
                    int col = curPos % MAX_WIDTH;
                    int res = seeds[row][col];
                    curPos++;
                    return res;
                }
            };
        }
    }
}
