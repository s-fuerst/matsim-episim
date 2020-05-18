/* *********************************************************************** *
 * project: org.matsim.*
 * Controler.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.episim.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.matsim.core.utils.io.IOUtils;

/**
 * This class reads the SENEZON data for every day. The data is filtered by the
 * zip codes of every area. It is possible to create results for every day or
 * for every week. The weekly results sums up the activityDuration over every
 * day of the week. The base line is always the first day or the first week.
 * The results are the percentile of the changes compared to the base.
 */
class AnalyzeSnzData {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		boolean weekly = true;

		// zip codes for Berlin
		List<Integer> zipCodesBerlin = new ArrayList<Integer>();
		for (int i = 10115; i <= 14199; i++)
			zipCodesBerlin.add(i);

		// zip codes for Munich
		List<Integer> zipCodesMunich = new ArrayList<Integer>();
		for (int i = 80331; i <= 81929; i++)
			zipCodesMunich.add(i);

		// zip codes for the district "Kreis Heinsberg"
		List<Integer> zipCodesHeinsberg = Arrays.asList(41812, 52538, 52511, 52525, 41836, 52538, 52531, 41849, 41844);

		analyzeDataForCertainArea(zipCodesBerlin, "Berlin", weekly);
		analyzeDataForCertainArea(zipCodesMunich, "Munich", weekly);
		analyzeDataForCertainArea(zipCodesHeinsberg, "Heinsberg", weekly);

	}

	private static void analyzeDataForCertainArea(List<Integer> listZipCodes, String area, boolean weekly)
			throws IOException, FileNotFoundException {
		String outputFile = null;
		if (weekly)
			outputFile = "output/" + area + "SnzData_weekly.csv";
		else
			outputFile = area + "SnzData_daily.csv";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(
				"../shared-svn/projects/episim/data/Bewegungsdaten/snzDataD_until20200509.csv.gz"))))) {
			BufferedWriter writer = IOUtils.getBufferedWriter(outputFile);
			try {
				writer.write(
						"date;accomp;business;education;errands;home;leisure;shop_daily;shop_other;traveling;undefined;visit;work;notAtHome"
								+ "\n");

				String line;
				int dateLocation = 0;
				int zipCodeLocation = 1;
				int actTypeLocation = 2;
				int durationLocation = 12;

				double sumAccomp = 0;
				double accompBase = 0;
				double sumBusiness = 0;
				double businessBase = 0;
				double sumEducation = 0;
				double educationBase = 0;
				double sumErrands = 0;
				double errandsBase = 0;
				double sumHome = 0;
				double homeBase = 0;
				double sumLeisure = 0;
				double leisureBase = 0;
				double sumShopDaily = 0;
				double shopDailyBase = 0;
				double sumShopOther = 0;
				double shopOtherBase = 0;
				double sumTraveling = 0;
				double travelingBase = 0;
				double sumUndefined = 0;
				double undefinedBase = 0;
				double sumVisit = 0;
				double visitBase = 0;
				double sumWork = 0;
				double workBase = 0;
				double sumNotAtHome = 0;
				double notAtHomeBase = 0;
				int countingDays = 0;
				String currantDate = null;

				while ((line = br.readLine()) != null) {
					String[] parts = line.split(",");
					if (parts[dateLocation].contains("date"))
						continue;
					if (!weekly)
						countingDays = 7;
					if (currantDate != null && !parts[dateLocation].contains(currantDate)) {
						countingDays++;
						if (countingDays == 7) {
							countingDays = 0;
							// sets the base
							if (homeBase == 0) {
								accompBase = sumAccomp;
								businessBase = sumBusiness;
								educationBase = sumEducation;
								errandsBase = sumErrands;
								homeBase = sumHome;
								leisureBase = sumLeisure;
								shopDailyBase = sumShopDaily;
								shopOtherBase = sumShopOther;
								travelingBase = sumTraveling;
								undefinedBase = sumUndefined;
								visitBase = sumVisit;
								workBase = sumWork;
								notAtHomeBase = sumNotAtHome;
							}
							writer.write(currantDate + ";" + Math.round((sumAccomp / accompBase - 1) * 100) + ";"
									+ Math.round((sumBusiness / businessBase - 1) * 100) + ";"
									+ Math.round((sumEducation / educationBase - 1) * 100) + ";"
									+ Math.round((sumErrands / errandsBase - 1) * 100) + ";"
									+ Math.round((sumHome / homeBase - 1) * 100) + ";"
									+ Math.round((sumLeisure / leisureBase - 1) * 100) + ";"
									+ Math.round((sumShopDaily / shopDailyBase - 1) * 100) + ";"
									+ Math.round((sumShopOther / shopOtherBase - 1) * 100) + ";"
									+ Math.round((sumTraveling / travelingBase - 1) * 100) + ";"
									+ Math.round((sumUndefined / undefinedBase - 1) * 100) + ";"
									+ Math.round((sumVisit / visitBase - 1) * 100) + ";"
									+ Math.round((sumWork / workBase - 1) * 100) + ";"
									+ Math.round((sumNotAtHome / notAtHomeBase - 1) * 100) + "\n");

							sumAccomp = 0;
							sumBusiness = 0;
							sumEducation = 0;
							sumErrands = 0;
							sumHome = 0;
							sumLeisure = 0;
							sumShopDaily = 0;
							sumShopOther = 0;
							sumTraveling = 0;
							sumUndefined = 0;
							sumVisit = 0;
							sumWork = 0;
							sumNotAtHome = 0;
						}
					}
					currantDate = parts[dateLocation];
					if (listZipCodes.contains(Integer.parseInt(parts[zipCodeLocation]))) {

						if (parts[actTypeLocation].contains("accomp")) {
							sumAccomp = sumAccomp + Double.parseDouble(parts[durationLocation]);
							sumNotAtHome = sumNotAtHome + Double.parseDouble(parts[durationLocation]);
							continue;
						}
						if (parts[actTypeLocation].contains("business")) {
							sumBusiness = sumBusiness + Double.parseDouble(parts[durationLocation]);
							sumNotAtHome = sumNotAtHome + Double.parseDouble(parts[durationLocation]);
							continue;
						}
						if (parts[actTypeLocation].contains("education")) {
							sumEducation = sumEducation + Double.parseDouble(parts[durationLocation]);
							sumNotAtHome = sumNotAtHome + Double.parseDouble(parts[durationLocation]);
							continue;
						}
						if (parts[actTypeLocation].contains("errands")) {
							sumErrands = sumErrands + Double.parseDouble(parts[durationLocation]);
							sumNotAtHome = sumNotAtHome + Double.parseDouble(parts[durationLocation]);
							continue;
						}
						if (parts[actTypeLocation].contains("home")) {
							sumHome = sumHome + Double.parseDouble(parts[durationLocation]);
							continue;
						}
						if (parts[actTypeLocation].contains("leisure")) {
							sumLeisure = sumLeisure + Double.parseDouble(parts[durationLocation]);
							sumNotAtHome = sumNotAtHome + Double.parseDouble(parts[durationLocation]);
							continue;
						}
						if (parts[actTypeLocation].contains("shop_daily")) {
							sumShopDaily = sumShopDaily + Double.parseDouble(parts[durationLocation]);
							sumNotAtHome = sumNotAtHome + Double.parseDouble(parts[durationLocation]);
							continue;
						}
						if (parts[actTypeLocation].contains("shop_other")) {
							sumShopOther = sumShopOther + Double.parseDouble(parts[durationLocation]);
							sumNotAtHome = sumNotAtHome + Double.parseDouble(parts[durationLocation]);
							continue;
						}
						if (parts[actTypeLocation].contains("traveling")) {
							sumTraveling = sumTraveling + Double.parseDouble(parts[durationLocation]);
							sumNotAtHome = sumNotAtHome + Double.parseDouble(parts[durationLocation]);
							continue;
						}
						if (parts[actTypeLocation].contains("undefined")) {
							sumUndefined = sumUndefined + Double.parseDouble(parts[durationLocation]);
							sumNotAtHome = sumNotAtHome + Double.parseDouble(parts[durationLocation]);
							continue;
						}
						if (parts[actTypeLocation].contains("visit")) {
							sumVisit = sumVisit + Double.parseDouble(parts[durationLocation]);
							sumNotAtHome = sumNotAtHome + Double.parseDouble(parts[durationLocation]);
							continue;
						}
						if (parts[actTypeLocation].contains("work")) {
							sumWork = sumWork + Double.parseDouble(parts[durationLocation]);
							sumNotAtHome = sumNotAtHome + Double.parseDouble(parts[durationLocation]);
							continue;
						}
					}
				}
				if (weekly && countingDays != 7)
					writer.close();
				else {
					writer.write(currantDate + ";" + Math.round((sumAccomp / accompBase - 1) * 100) + ";"
							+ Math.round((sumBusiness / businessBase - 1) * 100) + ";"
							+ Math.round((sumEducation / educationBase - 1) * 100) + ";"
							+ Math.round((sumErrands / errandsBase - 1) * 100) + ";"
							+ Math.round((sumHome / homeBase - 1) * 100) + ";"
							+ Math.round((sumLeisure / leisureBase - 1) * 100) + ";"
							+ Math.round((sumShopDaily / shopDailyBase - 1) * 100) + ";"
							+ Math.round((sumShopOther / shopOtherBase - 1) * 100) + ";"
							+ Math.round((sumTraveling / travelingBase - 1) * 100) + ";"
							+ Math.round((sumUndefined / undefinedBase - 1) * 100) + ";"
							+ Math.round((sumVisit / visitBase - 1) * 100) + ";"
							+ Math.round((sumWork / workBase - 1) * 100) + ";"
							+ Math.round((sumNotAtHome / notAtHomeBase - 1) * 100));
					writer.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
