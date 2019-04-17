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

import br.ufpr.inf.cbio.hhco.hyperheuristic.CooperativeAlgorithm;
import java.util.List;
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
public class COMOMBI2<S extends Solution<?>> extends MOMBI2<S> implements CooperativeAlgorithm<S> {

    public List<S> offspringPopulation;

    public COMOMBI2(Problem<S> problem, int maxIterations, CrossoverOperator<S> crossover, MutationOperator<S> mutation, SelectionOperator<List<S>, S> selection, SolutionListEvaluator<S> evaluator, String pathWeights) {
        super(problem, maxIterations, crossover, mutation, selection, evaluator, pathWeights);
    }

    @Override
    public void init(int populationSize) {
        setMaxPopulationSize(populationSize);
        init(createInitialPopulation(), populationSize);
    }

    @Override
    public void init(List<S> initialPopulation, int popSize) {
        this.setPopulation(initialPopulation);
        setMaxPopulationSize(popSize);
        this.evaluatePopulation(this.getPopulation());
        this.initProgress();
        this.specificMOEAComputations();
    }

    @Override
    public void doIteration() {
        List<S> matingPopulation;
        matingPopulation = selection(this.getPopulation());
        offspringPopulation = reproduction(matingPopulation);
        offspringPopulation = evaluatePopulation(offspringPopulation);
        this.setPopulation(replacement(this.getPopulation(), offspringPopulation));
        // specific GA needed computations
        this.specificMOEAComputations();
    }

    @Override
    public void receive(List<S> solutions) {
        this.setPopulation(replacement(this.getPopulation(), solutions));
        // specific GA needed computations
        this.specificMOEAComputations();
    }

    @Override
    public List<S> getOffspring() {
        return offspringPopulation;
    }

}
