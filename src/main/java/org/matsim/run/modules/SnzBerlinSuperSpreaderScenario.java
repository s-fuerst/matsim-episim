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
package org.matsim.run.modules;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.controler.ControlerUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.model.InfectionModel;
import org.matsim.episim.model.InfectionModelWithViralLoad;
import org.matsim.episim.model.ContactModel;
import org.matsim.episim.model.SymmetricContactModel;

import java.util.Map;
import java.util.SplittableRandom;

import static org.matsim.episim.EpisimUtils.nextLogNormalFromMeanAndSigma;
import static org.matsim.episim.model.InfectionModelWithViralLoad.SUSCEPTIBILITY;
import static org.matsim.episim.model.InfectionModelWithViralLoad.VIRAL_LOAD;

/**
 * Snz scenario for Berlin with enabled viral load infection model.
 *
 * @see AbstractSnzScenario
 */
public class SnzBerlinSuperSpreaderScenario extends AbstractSnzScenario2020 {

	private final double sigmaInf;
	private final double sigmaSusp;

	private static final Map<Double, Double> calibration = Map.of(
			0d, 1.06e-5,
			0.5, 1.12e-5,
			0.75, 1.25e-5,
			1d, 1.55e-5
	);

	/**
	 * Constructor with default values.
	 */
	public SnzBerlinSuperSpreaderScenario() {
		this(0.75, 0.75);
	}

	public SnzBerlinSuperSpreaderScenario(double sigmaInf, double sigmaSusp) {
		this.sigmaInf = sigmaInf;
		this.sigmaSusp = sigmaSusp;
	}

	@Override
	protected void configure() {
		super.configure();

		bind(InfectionModel.class).to(InfectionModelWithViralLoad.class).in(Singleton.class);
		bind(ContactModel.class).to(SymmetricContactModel.class).in(Singleton.class);
	}

	@Provides
	@Singleton
	public Config config() {


		Config config = new SnzBerlinWeekScenario25pct2020().config();

		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);

		episimConfig.setMaxContacts(30);

		episimConfig.setCalibrationParameter(calibration.get(sigmaInf));

		// set start
		episimConfig.setStartDate("2020-02-16");

		// maybe ci calibration needed
		config.controler().setOutputDirectory("./output-berlin-25pct-superSpreader-calibrParam-" + episimConfig.getCalibrationParameter());

		return config;
	}

	@Provides
	@Singleton
	public Scenario scenario(Config config) {

		config.vspExperimental().setVspDefaultsCheckingLevel(VspExperimentalConfigGroup.VspDefaultsCheckingLevel.ignore);

		// save some time for not needed inputs
		config.facilities().setInputFile(null);
		config.vehicles().setVehiclesFile(null);

		ControlerUtils.checkConfigConsistencyAndWriteToLog(config, "before loading scenario");

		final Scenario scenario = ScenarioUtils.loadScenario(config);

		SplittableRandom rnd = new SplittableRandom(4715);
		for (Person person : scenario.getPopulation().getPersons().values()) {
			person.getAttributes().putAttribute(VIRAL_LOAD, nextLogNormalFromMeanAndSigma(rnd, 1, sigmaInf));
			person.getAttributes().putAttribute(SUSCEPTIBILITY, nextLogNormalFromMeanAndSigma(rnd, 1, sigmaSusp));
		}

		return scenario;
	}

}
