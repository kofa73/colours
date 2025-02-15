package kofa;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simulates processing in input buffer into an output buffer (does no processing, just copies). This should probably be a JMH test, but I'm lazy.
 * Data from Ryzen 5 5600X with 12 HT cores (6 real).
 * Of the sequential methods, the 'linear' approach (does not care about rows and columns, just processes pixels one after the other) is the fastest.
 * The 'direct' one has a loop for x and y, but still increments the index locally; because it is aware of row width, accessing the next/previous row is trivially possible, if needed.
 * The float[][] representation is very easy to use, but is slower because of the indirection.
 *
 * Of the parallel approaches, the linear approach is fastest. The algorithm is aware of the row number and width, so accessing the next/previous row is just as easy as the 'direct sequential'.
 * 2 threads provided the best results, the speed-up is only ~10% compared to the same code on 1 thread.
 * The number of threads (core size or 256) and the number of parallel partitions had little effect,
 * e.g. the 12/12 (12 partitions using 12 threads) performance was not much different from 32/12, 32/256 or 256/256.
 *
 * The parallel stream approach is very simple, but was ~20% slower than the sequential.
 *
 * All versions where the array was aligned to 4 floats (even if only 3 channels were copied) were much (> 30%) slower than the 3-float versions.
 *
 * copy3LinearFloat3Array_linearIndex:                        7_014 ms
 * copy3LinearFloat3Array_directIndex:                        7_166 ms
 * copy3LinearFloat3Array_linearIndex_parallelStream:         7_351 ms
 * copy3Float3Array_2D_parallel:                              7_375 ms
 * copy3LinearFloat3Array_computedIndex:                      7_449 ms
 *
 * copy3Float3Array_2D_sequential:                            8_471 ms
 *
 * copy4LinearFloat3Array_directIndex:                        9_318 ms
 * copy4LinearFloat3Array_linearIndex:                        9_373 ms
 * copy4LinearFloat4Array_linearIndex:                        9_485 ms
 * copy4LinearFloat4Array_directIndex:                        9_575 ms
 *
 * copy3LinearFloat3Array_linearIndex_parallel(1/256):        7_212 ms
 * copy3LinearFloat3Array_linearIndex_parallel(2/256):        6_676 ms
 * copy3LinearFloat3Array_linearIndex_parallel(4/256):        6_897 ms
 * copy3LinearFloat3Array_linearIndex_parallel(8/256):        7_124 ms
 * copy3LinearFloat3Array_linearIndex_parallel(12/256):       7_227 ms
 * copy3LinearFloat3Array_linearIndex_parallel(16/256):       7_234 ms
 * copy3LinearFloat3Array_linearIndex_parallel(32/256):       7_250 ms
 * copy3LinearFloat3Array_linearIndex_parallel(64/256):       7_244 ms
 * copy3LinearFloat3Array_linearIndex_parallel(128/256):      7_257 ms
 * copy3LinearFloat3Array_linearIndex_parallel(256/256):      7_274 ms
 * copy3LinearFloat3Array_linearIndex_parallel(512/256):      7_281 ms
 * copy3LinearFloat3Array_linearIndex_parallel(2048/256):     7_422 ms
 *
 * copy3LinearFloat3Array_linearIndex_parallel(1/12):         7_283 ms
 * copy3LinearFloat3Array_linearIndex_parallel(2/12):         6_603 ms
 * copy3LinearFloat3Array_linearIndex_parallel(4/12):         6_893 ms
 * copy3LinearFloat3Array_linearIndex_parallel(8/12):         7_123 ms
 * copy3LinearFloat3Array_linearIndex_parallel(12/12):        7_226 ms
 * copy3LinearFloat3Array_linearIndex_parallel(16/12):        7_187 ms
 * copy3LinearFloat3Array_linearIndex_parallel(32/12):        7_271 ms
 *
 * copy4LinearFloat3Array_computedIndex:                      9_307 ms
 * copy4LinearFloat4Array_computedIndex:                      9_552 ms
 */
@Disabled("A non-scientific performance test")
public class CopyTest {
    private static final boolean COMPARE_BUFFERS = false;

    private long start;
    private int loops;
    // avoid inlining constant
    public volatile int widthParam;
    public volatile int heightParam;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @BeforeEach
    void before() {
        loops = COMPARE_BUFFERS ? 1 : 100;
        widthParam = COMPARE_BUFFERS ? 31 : 9000;
        heightParam = COMPARE_BUFFERS ? 17 : 6000;
    }
    
    @AfterEach
    void after() {
        System.out.println("Took " + (System.currentTimeMillis() - start));
    }

    @AfterAll
    static void shutdownThreadPool() {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void copy3LinearFloat3Array_linearIndex() {
        float[] in = allocateInArray(widthParam, heightParam, 3);
        float[] out = allocateOutArray(widthParam, heightParam, 3);
        start();
        for (int i = 0; i < loops; i++) {
            copy3_3_linearIndex(in, out);
        }
        assertPixels(in, out, 3);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8, 12, 16, 32})
    void copy3LinearFloat3Array_linearIndex_parallel(int nThreads) {
        float[] in = allocateInArray(widthParam, heightParam, 3);
        float[] out = allocateOutArray(widthParam, heightParam, 3);
        start();
        for (int i = 0; i < loops; i++) {
            copy3_3_linearIndex_parallel(in, out, widthParam, heightParam, threadPool, nThreads);
        }
        assertPixels(in, out, 3);
    }

    @Test
    void copy3LinearFloat3Array_linearIndex_parallelStream() {
        float[] in = allocateInArray(widthParam, heightParam, 3);
        float[] out = allocateOutArray(widthParam, heightParam, 3);
        start();
        for (int i = 0; i < loops; i++) {
            copy3_3_linearIndex_parallelStream(in, out, widthParam, heightParam);
        }
        assertPixels(in, out, 3);
    }

    @Test
    void copy4LinearFloat3Array_linearIndex() {
        float[] in = allocateInArray(widthParam, heightParam, 4);
        float[] out = allocateOutArray(widthParam, heightParam, 4);
        start();
        for (int i = 0; i < loops; i++) {
            copy4_3_linearIndex(in, out);
        }
        assertPixels(in, out, 4);
    }

    @Test
    void copy4LinearFloat4Array_linearIndex() {
        float[] in = allocateInArray(widthParam, heightParam, 4);
        float[] out = allocateOutArray(widthParam, heightParam, 4);
        start();
        for (int i = 0; i < loops; i++) {
            copy4_4_linearIndex(in, out);
        }
        assertPixels(in, out, 4);
    }

    @Test
    void copy3LinearFloat3Array_directIndex() {
        float[] in = allocateInArray(widthParam, heightParam, 3);
        float[] out = allocateOutArray(widthParam, heightParam, 3);
        start();
        for (int i = 0; i < loops; i++) {
            copy3_3_sequentialDirectIndex(in, out, widthParam, heightParam);
        }
        assertPixels(in, out, 3);
    }

    @Test
    void copy3LinearFloat3Array_computedIndex() {
        float[] in = allocateInArray(widthParam, heightParam, 3);
        float[] out = allocateOutArray(widthParam, heightParam, 3);
        start();
        for (int i = 0; i < loops; i++) {
            copy3_3_sequentialComputedIndex(in, out, widthParam, heightParam);
        }
        assertPixels(in, out, 3);
    }

    @Test
    void copy4LinearFloat3Array_computedIndex() {
        float[] in = allocateInArray(widthParam, heightParam, 4);
        float[] out = allocateOutArray(widthParam, heightParam, 4);
        start();
        for (int i = 0; i < loops; i++) {
            copy4_3_sequentialComputedIndex(in, out, widthParam, heightParam);
        }
        assertPixels(in, out, 4);
    }

    @Test
    void copy4LinearFloat4Array_computedIndex() {
        float[] in = allocateInArray(widthParam, heightParam, 4);
        float[] out = allocateOutArray(widthParam, heightParam, 4);
        start();
        for (int i = 0; i < loops; i++) {
            copy4_4_sequentialComputedIndex(in, out, widthParam, heightParam);
        }
        assertPixels(in, out, 4);
    }

    @Test
    void copy4LinearFloat3Array_directIndex() {
        float[] in = allocateInArray(widthParam, heightParam, 4);
        float[] out = allocateOutArray(widthParam, heightParam, 4);
        start();
        for (int i = 0; i < loops; i++) {
            copy4_3_sequentialDirectIndex(in, out, widthParam, heightParam);
        }
        assertPixels(in, out, 4);
    }

    @Test
    void copy4LinearFloat4Array_directIndex() {
        float[] in = allocateInArray(widthParam, heightParam, 4);
        float[] out = allocateOutArray(widthParam, heightParam, 4);
        start();
        for (int i = 0; i < loops; i++) {
            copy4_4_sequentialDirectIndex(in, out, widthParam, heightParam);
        }
        assertPixels(in, out, 4);
    }

    @Test
    void copy3Float3Array_2D_sequential() {
        float[][] in = allocateInArray2D(widthParam, heightParam, 3);
        float[][] out = allocateOutArray2D(widthParam, heightParam, 3);
        start();
        for (int i = 0; i < loops; i++) {
            copy_sequential2D(in, out, 3);
        }
        assertPixels2D(in, out, 3);
    }

    @Test
    void copy3Float3Array_2D_parallel() {
        float[][] in = allocateInArray2D(widthParam, heightParam, 3);
        float[][] out = allocateOutArray2D(widthParam, heightParam, 3);
        start();
        for (int i = 0; i < loops; i++) {
            copy_parallel2D(in, out, 3);
        }
        assertPixels2D(in, out, 3);
    }

    private static void copy_sequential2D(float[][] in, float[][] out, int channels) {
        for (int y = 0; y < in.length; y++) {
            float[] inRow = in[y];
            float[] outRow = out[y];
            for (int x = 0; x < inRow.length; x += channels) {
                outRow[x] = inRow[x];
                outRow[x + 1] = inRow[x + 1];
                outRow[x + 2] = inRow[x + 2];
            }
        }
    }
    
    private static void copy_parallel2D(float[][] in, float[][] out, int channels) {
        IntStream.range(0, in.length).parallel().forEach(y -> {
            float[] inRow = in[y];
            float[] outRow = out[y];
            for (int x = 0; x < inRow.length; x += channels) {
                outRow[x] = inRow[x];
                outRow[x + 1] = inRow[x + 1];
                outRow[x + 2] = inRow[x + 2];
            }
        });
    }

    private static void copy3_3_sequentialDirectIndex(float[] in, float[] out, int width, int height) {
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                out[index] = in[index];
                out[index + 1] = in[index + 1];
                out[index + 2] = in[index + 2];
                index += 3;
            }
        }
    }

    private static void copy3_3_linearIndex(float[] in, float[] out) {
        for (int index = 0; index < in.length; index += 3) {
            out[index] = in[index];
            out[index + 1] = in[index + 1];
            out[index + 2] = in[index + 2];
        }
    }

    private static void copy3_3_linearIndex_parallel(float[] in, float[] out, int width, int height, ExecutorService threadPool, int nThreads) {
        Set<? extends Future<?>> futures = IntStream.range(0, nThreads)
                .mapToObj(partition -> copyPartition(in, out, width, height, partition, nThreads))
                .map(threadPool::submit)
                .collect(Collectors.toSet());
        futures.stream().forEach(CopyTest::join);
    }

    private static void copy3_3_linearIndex_parallelStream(float[] in, float[] out, int width, int height) {
        IntStream.range(0, height)
                .parallel()
                .forEach(y -> copy3_3_linearIndex_range(in, out, width, y, y + 1));
    }

    private static Runnable copyPartition(float[] in, float[] out, int width, int height, int partition, int nThreads) {
        return () -> {
            int startY = height * partition / nThreads;
            int endY = partition == nThreads - 1 ? height : height * (partition + 1) / nThreads;
            copy3_3_linearIndex_range(in, out, width, startY, endY);
        };
    }

    private static void join(Future<?> future) {
        try {
            future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void copy3_3_linearIndex_range(float[] in, float[] out, int width, int startY, int endY) {
        int startIndex = index(0, startY, width, 3);
        int endIndex = index(0, endY, width, 3);

        for (int index = startIndex; index < endIndex; index += 3) {
            out[index] = in[index];
            out[index + 1] = in[index + 1];
            out[index + 2] = in[index + 2];
        }
    }

    private static void copy4_3_linearIndex(float[] in, float[] out) {
        for (int index = 0; index < in.length; index += 4) {
            out[index] = in[index];
            out[index + 1] = in[index + 1];
            out[index + 2] = in[index + 2];
        }
    }

    private static void copy4_4_linearIndex(float[] in, float[] out) {
        for (int index = 0; index < in.length; index += 4) {
            out[index] = in[index];
            out[index + 1] = in[index + 1];
            out[index + 2] = in[index + 2];
            out[index + 3] = in[index + 3];
        }
    }

    private static void copy3_3_sequentialComputedIndex(float[] in, float[] out, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x ++) {
                int index = index(x, y, width, 3);
                out[index] = in[index];
                out[index + 1] = in[index + 1];
                out[index + 2] = in[index + 2];
            }
        }
    }

    private static void copy4_3_sequentialComputedIndex(float[] in, float[] out, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x ++) {
                int index = index(x, y, width, 4);
                out[index] = in[index];
                out[index + 1] = in[index + 1];
                out[index + 2] = in[index + 2];
            }
        }
    }

    private static void copy4_4_sequentialComputedIndex(float[] in, float[] out, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x ++) {
                int index = index(x, y, width, 4);
                out[index] = in[index];
                out[index + 1] = in[index + 1];
                out[index + 2] = in[index + 2];
                out[index + 3] = in[index + 3];
            }
        }
    }

    private static void copy4_3_sequentialDirectIndex(float[] in, float[] out, int width, int height) {
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x ++) {
                out[index] = in[index];
                out[index + 1] = in[index + 1];
                out[index + 2] = in[index + 2];
                index += 4;
            }
        }
    }

    private static void copy4_4_sequentialDirectIndex(float[] in, float[] out, int width, int height) {
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                out[index] = in[index];
                out[index + 1] = in[index + 1];
                out[index + 2] = in[index + 2];
                out[index + 3] = in[index + 3];
                index += 4;
            }
        }
    }

    private static float[] allocateInArray(int width, int height, int channels) {
        var array = new float[channels * width * height];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        return array;
    }

    private static float[] allocateOutArray(int width, int height, int channels) {
        return new float[channels * width * height];
    }

    private float[][] allocateInArray2D(int width, int height, int channels) {
        int rowLength = width * channels;
        int value = 0;
        float[][] in = new float[height][];
        for (int y = 0; y < in.length; y++) {
            in[y] = new float[rowLength];
            for (int x = 0; x < rowLength; x++) {
                in[y][x] = value;
                value++;
            }
        }
        return in;
    }

    private float[][] allocateOutArray2D(int width, int height, int channels) {
        int rowLength = width * channels;
        int value = 0;
        float[][] out = new float[height][];
        for (int y = 0; y < out.length; y++) {
            out[y] = new float[rowLength];
        }
        return out;
    }

    private static int index(int x, int y, int width, int channels) {
        return (y * width + x) * channels;
    }

    private void start() {
        start = System.currentTimeMillis();
    }

    private void assertPixels(float[] in, float[] out, int channels) {
        if (!COMPARE_BUFFERS) {
            return;
        }
        for (int y = 0; y < heightParam; y++) {
            for (int x = 0; x < widthParam; x++) {
                int index = index(x, y, widthParam, channels);
                assertThat(out[index]).describedAs("0 at (" + x + "," + y + ")").isEqualTo(in[index]);
                assertThat(out[index + 1]).describedAs("1 at (" + x + "," + y + ")").isEqualTo(in[index + 1]);
                assertThat(out[index + 2]).describedAs("2 at (" + x + "," + y + ")").isEqualTo(in[index + 2]);
            }
        }
    }

    private static void assertPixels2D(float[][] in, float[][] out, int channels) {
        if (!COMPARE_BUFFERS) {
            return;
        }
        for (int y = 0; y < in.length; y++) {
            float[] inRow = in[y];
            float[] outRow = out[y];
            for (int x = 0; x < inRow.length; x += channels) {
                assertThat(outRow[x]).describedAs("0 at (" + x + "," + y + ")").isEqualTo(inRow[x]);
                assertThat(outRow[x + 1]).describedAs("1 at (" + x + "," + y + ")").isEqualTo(inRow[x + 1]);
                assertThat(outRow[x + 2]).describedAs("2 at (" + x + "," + y + ")").isEqualTo(inRow[x + 2]);
            }
        }
    }
}
