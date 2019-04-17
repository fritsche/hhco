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
package br.ufpr.inf.cbio.hhco.algorithm.SPEA2SDE;

import br.ufpr.inf.cbio.hhco.config.AlgorithmConfiguration;
import java.util.List;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <S>
 */
public class SPEA2SDEConfiguration<S extends Solution> implements AlgorithmConfiguration<SPEA2SDE<?>> {

    Problem<S> problem;
    CrossoverOperator crossoverOperator;
    MutationOperator mutationOperator;
    SelectionOperator<List<S>, S> selectionOperator;

    @Override
    public void setup() {
        double crossoverProbability = 1.0;
        double crossoverDistributionIndex = 20.0;
        crossoverOperator = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);
        double mutationProbability = 1.0 / problem.getNumberOfVariables();
        double mutationDistributionIndex = 20.0;
        mutationOperator = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
        selectionOperator = new BinaryTournamentSelection<>();
    }

    @Override
    public SPEA2SDE<?> configure(int popSize, int maxFitnessEvaluations, Problem problem) {
        this.problem = problem;
        setup();
        return new SPEA2SDE(problem, maxFitnessEvaluations / popSize, popSize, crossoverOperator,
                mutationOperator, selectionOperator, new SequentialSolutionListEvaluator<>());
    }

}
