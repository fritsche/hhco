/*
 * Copyright (C) 2019 Gian Fritsche <gmfritsche at inf.ufpr.br>
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
package br.ufpr.inf.cbio.hhco.hyperheuristic.HHCORandom;

import br.ufpr.inf.cbio.hhco.algorithm.HypE.COHypEConfiguration;
import br.ufpr.inf.cbio.hhco.algorithm.MOEAD.COMOEADConfiguration;
import br.ufpr.inf.cbio.hhco.algorithm.MOEADD.COMOEADDConfiguration;
import br.ufpr.inf.cbio.hhco.algorithm.MOMBI2.COMOMBI2Configuration;
import br.ufpr.inf.cbio.hhco.algorithm.NSGAII.CONSGAIIConfiguration;
import br.ufpr.inf.cbio.hhco.algorithm.NSGAIII.CONSGAIIIConfiguration;
import br.ufpr.inf.cbio.hhco.algorithm.SPEA2.COSPEA2Configuration;
import br.ufpr.inf.cbio.hhco.algorithm.SPEA2SDE.COSPEA2SDEConfiguration;
import br.ufpr.inf.cbio.hhco.algorithm.ThetaDEA.COThetaDEAConfiguration;
import br.ufpr.inf.cbio.hhco.config.AlgorithmConfiguration;
import br.ufpr.inf.cbio.hhco.hyperheuristic.HHCO.HHCO;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <S>
 */
public class HHCORandomConfiguration<S extends Solution> implements AlgorithmConfiguration<HHCO> {

    protected Problem problem;
    protected int popSize;

    public HHCORandomConfiguration() {

    }

    @Override
    public HHCO configure(int popSize, int maxFitnessEvaluations, Problem problem) {

        this.problem = problem;
        this.popSize = popSize;

        setup();

        HHCORandomBuilder builder = new HHCORandomBuilder(problem);

        builder
                .addAlgorithm(new COSPEA2Configuration().configure(popSize, maxFitnessEvaluations, problem))
                .addAlgorithm(new COMOEADConfiguration().configure(popSize, maxFitnessEvaluations, problem))
                .addAlgorithm(new CONSGAIIConfiguration().configure(popSize, maxFitnessEvaluations, problem))
                .addAlgorithm(new COMOEADDConfiguration().configure(popSize, maxFitnessEvaluations, problem))
                .addAlgorithm(new COMOMBI2Configuration().configure(popSize, maxFitnessEvaluations, problem))
                .addAlgorithm(new CONSGAIIIConfiguration().configure(popSize, maxFitnessEvaluations, problem))
                .addAlgorithm(new COThetaDEAConfiguration().configure(popSize, maxFitnessEvaluations, problem))
                .addAlgorithm(new COSPEA2SDEConfiguration().configure(popSize, maxFitnessEvaluations, problem))
                .addAlgorithm(new COHypEConfiguration().configure(popSize, maxFitnessEvaluations, problem));

        return builder.setMaxEvaluations(maxFitnessEvaluations).setPopulationSize(popSize).build();
    }

    @Override
    public void setup() {
        
    }
}
