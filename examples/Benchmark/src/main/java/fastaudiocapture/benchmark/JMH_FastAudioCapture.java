package fastaudiocapture.benchmark;
import fastaudiocapture.FastAudioCapture;
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class JMH_Capture {
    private FastAudioCapture capture;
    @Setup public void setup() { capture = new FastAudioCapture(); }
    @Benchmark public FastAudioCapture benchmarkCapture() { return capture; }
}