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

import br.ufpr.inf.cbio.hhco.hyperheuristic.CooperativeAlgorithm;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2;
import org.uma.jmetal.algorithm.multiobjective.spea2.util.EnvironmentalSelection;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 * @param <S>
 */
public class COSPEA2<S extends Solution<?>> extends SPEA2<S> implements CooperativeAlgorithm<S> {

    public List<S> offspringPopulation;
    public EnvironmentalSelection<S> environmentalSelectionOverride;

    public COSPEA2(Problem problem, int maxIterations, int populationSize, CrossoverOperator crossoverOperator, MutationOperator mutationOperator, SelectionOperator selectionOperator, SolutionListEvaluator evaluator, int k) {
        super(problem, maxIterations, populationSize, crossoverOperator, mutationOperator, selectionOperator, evaluator, k);
    }

    @Override
    protected List<S> selection(List<S> population) {
        List<S> union = new ArrayList<>();
        union.addAll(archive);
        union.addAll(population);
        strenghtRawFitness.computeDensityEstimator(union);
        archive = environmentalSelectionOverride.execute(union);
        return archive;
    }

    @Override
    public void init(int populationSize) {
        setMaxPopulationSize(populationSize);
        List<S> initial = createInitialPopulation();
        initial = evaluatePopulation(initial);
        init(initial, populationSize);
    }

    @Override
    public void init(List<S> initialPopulation, int popSize) {
        setMaxPopulationSize(popSize);
        population = initialPopulation;
        environmentalSelectionOverride = new EnvironmentalSelection<>(getMaxPopulationSize());
    }

    @Override
    public void doIteration() {
        List<S> matingPopulation;
        matingPopulation = selection(population);
        offspringPopulation = reproduction(matingPopulation);
        offspringPopulation = evaluatePopulation(offspringPopulation);
        population = replacement(population, offspringPopulation);
    }

    @Override
    public void receive(List<S> solutions) {
        population = selection(solutions);
    }

    @Override
    public List<S> getOffspring() {
        return offspringPopulation;
    }

}
