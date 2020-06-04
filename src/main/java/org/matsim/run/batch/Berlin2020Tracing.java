/*-
 * #%L
 * MATSim Episim
 * %%
 * Copyright (C) 2020 matsim-org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.matsim.run.batch;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.BatchRun;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.EpisimUtils;
import org.matsim.episim.TracingConfigGroup;
import org.matsim.episim.model.Transition;
import org.matsim.episim.policy.FixedPolicy;
import org.matsim.run.modules.SnzBerlinScenario25pct2020;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Batch run for {@link org.matsim.run.modules.SnzBerlinScenario25pct2020} and different tracing options.
 */
public final class Berlin2020Tracing implements BatchRun<Berlin2020Tracing.Params> {

	public static final List<Option> OPTIONS = List.of(
			Option.of("Contact tracing", 67)
					.measure("Tracing Distance", "tracingDayDistance")
					.measure("Tracing Probability", "tracingProbability"),

			Option.of("Out-of-home activities limited", "By type and percent (%)", 67)
					.measure("Work activities", "remainingFractionWork")
					.measure("Other activities", "remainingFractionShoppingBusinessErrands")
					.measure("Leisure activities", "remainingFractionLeisure"),

			Option.of("Reopening of educational facilities", "Students returning (%)", 74)
					.measure("Going to primary school", "remainingFractionPrima")
					.measure("Going to kindergarten", "remainingFractionKiga")
					.measure("Going to secondary/univ.", "remainingFractionSeconHigher")
	);

	@Override
	public LocalDate getDefaultStartDate() {
		return LocalDate.of(2020, 2, 10);
	}

	@Override
	public Metadata getMetadata() {
		return Metadata.of("berlin", "tracing");
	}

	@Override
	public Config baseCase(int id) {

		SnzBerlinScenario25pct2020 module = new SnzBerlinScenario25pct2020();
		Config config = module.config();

		config.plans().setInputFile("../../../episim-input/be_2020_snz_entirePopulation_emptyPlans_withDistricts_25pt.xml.gz");

		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);

		episimConfig.setInputEventsFile("../be_2020_snz_episim_events_25pt.xml.gz");

		episimConfig.setProgressionConfig(
				SnzBerlinScenario25pct2020.baseProgressionConfig(Transition.config("input/progression" + id + ".conf")).build()
		);

		return config;
	}

	@Override
	public List<Option> getOptions() {
		return OPTIONS;
	}

	@Override
	public Config prepareConfig(int id, Berlin2020Tracing.Params params) {

		Config config = baseCase(id);
		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);
		TracingConfigGroup tracingConfig = ConfigUtils.addOrGetModule(config, TracingConfigGroup.class);

		LocalDate startDate = LocalDate.parse("2020-02-10");

		// +1 because end date is exclusive
		int offset = (int) (ChronoUnit.DAYS.between(startDate, LocalDate.parse("2020-04-27")) + 1);
		tracingConfig.setPutTraceablePersonsInQuarantineAfterDay(offset);
		tracingConfig.setTracingProbability(params.tracingProbability);
		tracingConfig.setTracingPeriod_days(params.tracingDayDistance );
		tracingConfig.setMinContactDuration_sec(15 * 60. );
		tracingConfig.setQuarantineHouseholdMembers(true);
		tracingConfig.setEquipmentRate(1.);
		tracingConfig.setTracingCapacity_pers_per_day(params.tracingCapacity );
		tracingConfig.setTracingDelay_days(params.tracingDelay );


		double alpha = 1.4;
		double ciCorrection = 0.3;
		File csv = new File("../shared-svn/projects/episim/matsim-files/snz/BerlinV2/episim-input/BerlinSnzData_daily_until20200524.csv");
		String dateOfCiChange = "2020-03-08";

		com.typesafe.config.Config policyConf;
		try {
			policyConf = SnzBerlinScenario25pct2020.basePolicy(episimConfig, csv, alpha, ciCorrection, dateOfCiChange,
					EpisimUtils.Extrapolation.valueOf(params.extrapolation))
					.build();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		String policyFileName = "input/policy" + id + ".conf";
		episimConfig.setOverwritePolicyLocation(policyFileName);
		episimConfig.setPolicy(FixedPolicy.class, policyConf);

		return config;
	}

	public static final class Params {

		@IntParameter({14})
		int tracingDayDistance;

		@IntParameter({2})
		int tracingDelay;

		@IntParameter({0, 10, 20, 30, Integer.MAX_VALUE})
		int tracingCapacity;

		@Parameter({1.0, 0.75, 0.5})
		double tracingProbability;

		@StringParameter({"linear", "exponential"})
		String extrapolation;

	}

}
