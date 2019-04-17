/*
 * Copyright (C) 2018 Gian Fritsche <gian.fritsche at gmail.com>
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

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * Based on the source received from Lei Cai <caileid at gmail.com>
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <S>
 */
public class SPEA2SDE<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {

    protected final int maxIterations;
    protected final SolutionListEvaluator<S> evaluator;
    protected int iterations;
    protected List<S> archive;
    protected final StrengthRawFitnessSDE<S> strenghtRawFitness = new StrengthRawFitnessSDE<>();
    protected final EnvironmentalSelectionSDE<S> environmentalSelection;

    public SPEA2SDE(Problem<S> problem, int maxIterations, int populationSize,
            CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
            SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
        super(problem);
        this.maxIterations = maxIterations;
        this.setMaxPopulationSize(populationSize);
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;
        this.environmentalSelection = new EnvironmentalSelectionSDE<>(populationSize);
        this.archive = new ArrayList<>(populationSize);
        this.evaluator = evaluator;
    }

    @Override
    protected void initProgress() {
        iterations = 1;
    }

    @Override
    protected void updateProgress() {
        iterations++;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return iterations >= maxIterations;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        population = evaluator.evaluate(population, getProblem());
        return population;
    }

    @Override
    protected List<S> selection(List<S> population) {
        List<S> union = new ArrayList<>(2 * getMaxPopulationSize());
        union.addAll(archive);
        union.addAll(population);
        strenghtRawFitness.computeDensityEstimator(union);
        archive = environmentalSelection.execute(union);
        return archive;
    }

    @Override
    protected List<S> reproduction(List<S> population) {
        List<S> offSpringPopulation = new ArrayList<>(getMaxPopulationSize());

        while (offSpringPopulation.size() < getMaxPopulationSize()) {
            List<S> parents = new ArrayList<>(2);
            S candidateFirstParent = selectionOperator.execute(population);
            parents.add(candidateFirstParent);
            S candidateSecondParent;
            candidateSecondParent = selectionOperator.execute(population);
            parents.add(candidateSecondParent);

            List<S> offspring = crossoverOperator.execute(parents);
            mutationOperator.execute(offspring.get(0));
            offSpringPopulation.add(offspring.get(0));
        }
        return offSpringPopulation;
    }

    @Override
    protected List<S> replacement(List<S> population,
            List<S> offspringPopulation) {
        return offspringPopulation;
    }

    @Override
    public List<S> getResult() {
        return archive;
    }

    @Override
    public String getName() {
        return "SPEA2SDE";
    }

    @Override
    public String getDescription() {
        return "Shift-Based Density Estimation for SPEA2";
    }
}
