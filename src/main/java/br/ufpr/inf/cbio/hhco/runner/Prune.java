/*
 * Copyright (C) 2018 Gian Fritsche <gmfritsche at inf.ufpr.br>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.ufpr.inf.cbio.hhco.runner;

import br.ufpr.inf.cbio.hhco.algorithm.SPEA2SDE.EnvironmentalSelectionSDE;
import br.ufpr.inf.cbio.hhco.algorithm.SPEA2SDE.StrengthRawFitnessSDE;
import br.ufpr.inf.cbio.hhco.runner.methodology.NSGAIIIMethodology;
import br.ufpr.inf.cbio.hhco.util.output.Utils;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 */
public class Prune {

    public static void main(String[] args) {

        try {
            /**
             * Parameters.
             */
            int i = 0;
            /**
             * Input path: args[0]
             * "experiment/MaFMethodology/5/data/HHCO/MaF01/"
             */
            String input = args[i++];
            /**
             * Output path: args[1]
             * "experiment/MaFMethodology/5/data/HHCOPruned/MaF01/"
             */
            String output = args[i++];
            /**
             * Number of objectives: args[2] "5"
             */
            int m = Integer.parseInt(args[i++]);
            /**
             * FUN file id: args[3] "0"
             */
            int id = Integer.parseInt(args[i++]);

            /**
             * Prune file.
             */
            /**
             * Load input FUN file to a solution set
             */
            List population = FrontUtils.convertFrontToSolutionList(new ArrayFront(input + "/FUN" + id + ".tsv"));
            /**
             * Get population size from methodology
             */
            int popSize = Math.min(240, (new NSGAIIIMethodology("", m)).getPopulationSize());
            /**
             * Prune solution set to population size
             */
            if (population.size() > popSize) {
                (new StrengthRawFitnessSDE()).computeDensityEstimator(population);
                population = (new EnvironmentalSelectionSDE<>(popSize)).execute(population);
            }
            /**
             * Create output path and file
             */
            (new Utils(output)).prepareOutputDirectory();
            /**
             * Output solution set to file
             */
            new SolutionListOutput(population).setSeparator("\t")
                    .setFunFileOutputContext(new DefaultFileOutputContext(output + "/FUN" + id + ".tsv"))
                    .print();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Prune.class.getName()).log(Level.SEVERE, "Failed to load input FUN file to a solution set.", ex);
        }

    }
}
