/*
 * Copyright (C) 2018 Gian Fritsche <gmfritsche@inf.ufpr.br>
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
package br.ufpr.inf.cbio.hhco.algorithm.NSGAIII;

import br.ufpr.inf.cbio.hhco.config.AlgorithmConfiguration;
import java.util.List;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.RandomSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 */
public class NSGAIIIConfiguration implements AlgorithmConfiguration<NSGAIII<?>> {

    protected double crossoverProbability;
    protected double crossoverDistributionIndex;
    protected double mutationProbability;
    protected double mutationDistributionIndex;
    protected Problem problem;
    protected CrossoverOperator<DoubleSolution> crossover;
    protected MutationOperator<DoubleSolution> mutation;
    protected SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;
    protected boolean normalize;

    @Override
    public void setup() {
        crossoverProbability = 1.0;
        crossoverDistributionIndex = 30.0;
        mutationProbability = 1.0 / problem.getNumberOfVariables();
        mutationDistributionIndex = 20.0;
        normalize = true;
        crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
        mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
        selection = new RandomSelection();
    }

    @Override
    public NSGAIII<?> configure(int popSize, int maxFitnessEvaluations, Problem problem) {

        this.problem = problem;

        setup();

        return new NSGAIIIBuilder(problem).setCrossover(crossover)
                .setMutation(mutation)
                .setSelection(selection)
                .setNormalize(normalize)
                .setMaxEvaluations(maxFitnessEvaluations)
                .setPopulationSize(popSize).build();
    }

}
