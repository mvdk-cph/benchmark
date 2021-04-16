package dk.cphbusiness.soft.mrv.benchmark;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Map;

import static java.lang.String.format;
import static java.lang.System.*;

/**
 * Rough and dirty benchmarking utility. Use at own risk!
 * For serious microbenchmarking, take a look at jmh.
 */
public class Benchmark {

    public static void runMany(Map<String,Runnable> rs, int reps, int printOptions) throws IOException {
        for (var key : rs.keySet().stream().sorted().toArray()) {
            out.printf("Running %s%n", key);
            run(key.toString(), rs.get(key), reps, printOptions);
        }
    }

    public static void run(String name, Runnable r, int n, int printOptions) throws IOException {
        Timer t = new Timer();
        double st = 0.0, sst = 0.0;

        boolean writeFile = (printOptions & PrintOptions.WRITE_TO_FILE) > 0;
        OutputStream out = getOutputStream(name, printOptions, writeFile);
        printHeader(name, n, printOptions, out);
        for (int i = 0; i < n; i++) {
            r.run();
            double time = t.check();

            // Used for mean and standard deviation in the end
            st += time;
            sst += time*time;

            if (printOptions != 0) {
                printIterationResult(n, printOptions, out, i, time);
            }

            // restart timer before next iteration
            t.play();
        }

        printEndData(n, printOptions, st, sst, writeFile, out);
    }

    private static void printHeader(String name, int n, int printOptions, OutputStream out) throws IOException {
        if ((printOptions & PrintOptions.HEADER) > 0) {
            out.write(String.format("Running '%s' %d times%n", name, n).getBytes());
        }
    }

    private static void printEndData(int n, int printOptions, double st, double sst, boolean writeFile, OutputStream out) throws IOException {
        if ((printOptions & PrintOptions.ONE_LINE_PER_RESULT) == 0) {
            out.write(new byte[]{'\n'});
        }

        if ((printOptions & (PrintOptions.MEAN_AND_STANDARD_DEVIATION)) > 0) {
            double mean = st / n;
            double variance = (sst - mean*mean* n) / (n -1);
            double sdev = Math.sqrt(variance);

            out.write(String.format("Mean run time: %5.6f ms ± %5.6f ms%n", mean, sdev).getBytes());
        }

        if (writeFile) {
            out.flush();
            out.close();
        }
    }

    private static void printIterationResult(int n, int printOptions, OutputStream out, int i, double time) throws IOException {
        if ((printOptions & PrintOptions.RESULT_FOR_EVERY_RUN) > 0) {
            var newline = "";
            var separator = " ";
            var unit = "";
            if ((printOptions & PrintOptions.ONE_LINE_PER_RESULT) > 0) newline = "\n";
            if ((printOptions & PrintOptions.COMMA_BETWEEN_RUNS) > 0 && i +1 < n) separator = ",";
            if ((printOptions & PrintOptions.UNIT) > 0) unit = " ms";
            out.write(String.format("%5.6f%s%s%s", time,unit,separator,newline).getBytes());
        }
    }

    private static OutputStream getOutputStream(String name, int printOptions, boolean writeFile) throws FileNotFoundException {
        OutputStream out = System.out;

        if (writeFile) {
            boolean randomFileName = (printOptions & PrintOptions.RANDOM_FILE_NAME) > 0;
            boolean appendToFile = (printOptions & PrintOptions.APPEND_TO_FILE) > 0;
            var randPart = randomFileName ? String.format("_%05d",(int)(Math.random()*99999)) : "";
            var fileName = name.replace(' ','_')
                    .replace('%','p')
                    .replace('/','-')
                    .replace('\\','-')
                    .replace('"','_')
                    .replace(',','_')
                    .replace('\'', '_') + randPart+".dat";
            boolean append = appendToFile && new File(fileName).exists();
            out = new FileOutputStream(fileName, append);
        }
        return out;
    }

    public static class PrintOptions {
        public static final int DONT_PRINT = 0;
        public static final int RESULT_FOR_EVERY_RUN        = 1;
        public static final int ONE_LINE_PER_RESULT         = 1 << 1;
        public static final int HEADER                      = 1 << 2;
        public static final int MEAN_AND_STANDARD_DEVIATION = 1 << 3;
        public static final int COMMA_BETWEEN_RUNS          = 1 << 4;
        public static final int UNIT                        = 1 << 5;
        public static final int WRITE_TO_FILE               = 1 << 6;
        public static final int APPEND_TO_FILE              = 1 << 7;
        public static final int RANDOM_FILE_NAME            = 1 << 8;
    }
}
