/*
Example parallel pi calculation program in java using PCJ library
Language specification at https://docs.oracle.com/javase/specs/
To run it on HPC, load java 11 at first using
$module load Java/11.0.2
Compile using
$javac -cp .:pcj-5.1.0.jar ParallelPi.java
Run it  using
$java -cp .:pcj-5.1.0.jar ParallelPi 10000000
*/

import org.pcj.PCJ;
import org.pcj.RegisterStorage;
import org.pcj.StartPoint;
import org.pcj.Storage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.LongStream;

@RegisterStorage(ParallelPi.Shared.class)
public class ParallelPi implements StartPoint {
    private static final Logger LOGGER = Logger.getLogger(ParallelPi.class.getName());
    private static int totalSample = 1000000;
    private final Random random;

    public ParallelPi() {
        this.random = new Random();
    }

    @Storage(ParallelPi.class)
    enum Shared {
        count
    }

    long count;

    @Override
    public void main() {
        PCJ.barrier();
        var samplePerThread = totalSample / PCJ.threadCount();
        //Calculate
        var time = System.nanoTime();
        count = insideCircle(samplePerThread);
        PCJ.barrier();

        //Communicate results
        var combinedCount = count;
        if (PCJ.myId() == 0) {
            combinedCount = PCJ.reduce(Long::sum, Shared.count);
        }

        PCJ.barrier();

        var pi = 4.0 * (double) combinedCount / (double) totalSample;
        time = System.nanoTime() - time;
        // Print results
        if (PCJ.myId() == 0) {
            LOGGER.log(Level.INFO, "Pi {0} calculation took {1}ms", new Object[]{String.valueOf(pi), TimeUnit.NANOSECONDS.toMillis(time)});
        }
    }

    private long insideCircle(long sample) {
        return LongStream.range(0, sample)
                .mapToObj(i -> getRandomXAndYAsEntry())
                .mapToDouble(this::radii)
                .filter(radii -> radii < 1.0)
                .count();
    }

    private AbstractMap.SimpleEntry<Double, Double> getRandomXAndYAsEntry() {
        return new AbstractMap.SimpleEntry<>(random.nextDouble(), random.nextDouble());
    }

    private double radii(AbstractMap.SimpleEntry<Double, Double> xyPair) {
        return Math.sqrt(Math.pow(xyPair.getKey(), 2) + Math.pow(xyPair.getValue(), 2));
    }

    public static void main(String[] args) {
        totalSample = Integer.parseInt(args[0]);
        var nodesFile = "nodes.txt";
        var nodeFileStream = Objects.requireNonNull(ParallelPi.class.getClassLoader().getResourceAsStream(nodesFile));
        var nodes = new BufferedReader(new InputStreamReader(nodeFileStream)).lines().toArray(String[]::new);
        PCJ.executionBuilder(ParallelPi.class)
                .addNodes(nodes)
                .start();
    }
}
