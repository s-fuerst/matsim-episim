package org.matsim.episim;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.run.modules.SnzScenario;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class BenchmarkIteration {

	private EpisimRunner runner;
	private InfectionEventHandler handler;
	private ReplayHandler replay;
	private int iteration;

	public static void main(String[] args) throws RunnerException {

		Options opt = new OptionsBuilder()
				.include(BenchmarkIteration.class.getSimpleName())
				.warmupIterations(12).warmupTime(TimeValue.seconds(1))
				.measurementIterations(30).measurementTime(TimeValue.seconds(1))
				.forks(1)
				.build();

		new Runner(opt).run();
	}

	@Setup
	public void setup() throws IOException {

		Injector injector = Guice.createInjector(new EpisimModule(), new SnzScenario());

		Config config = injector.getInstance(Config.class);
		Files.createDirectories(Path.of(config.controler().getOutputDirectory()));

		runner = injector.getInstance(EpisimRunner.class);
		replay = injector.getInstance(ReplayHandler.class);
		handler = injector.getInstance(InfectionEventHandler.class);


		injector.getInstance(EventsManager.class).addHandler(handler);

	}

	@Benchmark
	public void iteration() {

		runner.doStep(replay, handler, null, iteration);
		iteration++;

	}
}
