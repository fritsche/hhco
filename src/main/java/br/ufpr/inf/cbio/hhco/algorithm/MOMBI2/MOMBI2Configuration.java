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
package br.ufpr.inf.cbio.hhco.algorithm.MOMBI2;

import br.ufpr.inf.cbio.hhco.config.AlgorithmConfiguration;
import java.util.List;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 */
public class MOMBI2Configuration implements AlgorithmConfiguration<MOMBI2<?>> {

    private CrossoverOperator<DoubleSolution> crossover;
    private MutationOperator<DoubleSolution> mutation;
    private SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;
    private Problem<DoubleSolution> problem;

    @Override
    public MOMBI2<?> configure(int popSize, int maxFitnessEvaluations, Problem problem) {
        this.problem = problem;
        setup();
        return new MOMBI2<>(problem, maxFitnessEvaluations / popSize, crossover, mutation, selection, new SequentialSolutionListEvaluator<>(), "WeightVectorsMOMBI2/W" + problem.getNumberOfObjectives() + "D_" + popSize + ".dat");
    }

    @Override
    public void setup() {
        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;
        crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

        double mutationProbability = 1.0 / problem.getNumberOfVariables();
        double mutationDistributionIndex = 20.0;
        mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

        selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());
    }

    public CrossoverOperator<DoubleSolution> getCrossover() {
        return crossover;
    }

    public MutationOperator<DoubleSolution> getMutation() {
        return mutation;
    }

    public SelectionOperator<List<DoubleSolution>, DoubleSolution> getSelection() {
        return selection;
    }

    public Problem<DoubleSolution> getProblem() {
        return problem;
    }

    public void setCrossover(CrossoverOperator<DoubleSolution> crossover) {
        this.crossover = crossover;
    }

    public void setMutation(MutationOperator<DoubleSolution> mutation) {
        this.mutation = mutation;
    }

    public void setSelection(SelectionOperator<List<DoubleSolution>, DoubleSolution> selection) {
        this.selection = selection;
    }

    public void setProblem(Problem<DoubleSolution> problem) {
        this.problem = problem;
    }

}
