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
package br.ufpr.inf.cbio.hhco.algorithm.SPEA2;

import br.ufpr.inf.cbio.hhco.config.AlgorithmConfiguration;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 */
public class SPEA2Configuration implements AlgorithmConfiguration<SPEA2<?>> {

    protected double crossoverProbability;
    protected double crossoverDistributionIndex;
    protected double mutationProbability;
    protected double mutationDistributionIndex;
    protected Problem problem;
    protected CrossoverOperator crossover;
    protected MutationOperator mutation;
    protected SelectionOperator selection;

    @Override
    public void setup() {

        crossoverProbability = 0.9;
        crossoverDistributionIndex = 20.0;
        crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

        mutationProbability = 1.0 / problem.getNumberOfVariables();
        mutationDistributionIndex = 20.0;
        mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

        selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());

    }

    @Override
    public SPEA2<?> configure(int popSize, int maxFitnessEvaluations, Problem problem) {

        this.problem = problem;

        setup();

        return new SPEA2Builder<>(problem, crossover, mutation)
                .setSelectionOperator(selection)
                .setMaxIterations(maxFitnessEvaluations / popSize)
                .setPopulationSize(popSize)
                .build();
    }

}
