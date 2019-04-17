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

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 * @param <S>
 */
public class NSGAIIIBuilder<S extends Solution> implements AlgorithmBuilder<NSGAIII<S>> {

    private final Problem<S> problem;
    private int maxEvaluations;
    private boolean normalize;
    private int populationSize;
    private CrossoverOperator crossover;
    private MutationOperator mutation;
    private SelectionOperator selection;

    NSGAIIIBuilder(Problem<S> problem) {
        this.problem = problem;
    }

    public Problem getProblem() {
        return this.problem;
    }

    public NSGAIIIBuilder setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;
        return this;
    }

    public NSGAIIIBuilder setNormalize(boolean normalize) {
        this.normalize = normalize;
        return this;
    }

    public NSGAIIIBuilder setCrossover(CrossoverOperator crossover) {
        this.crossover = crossover;
        return this;
    }

    public NSGAIIIBuilder setMutation(MutationOperator mutation) {
        this.mutation = mutation;
        return this;
    }

    public NSGAIIIBuilder setSelection(SelectionOperator selection) {
        this.selection = selection;
        return this;
    }

    public NSGAIIIBuilder setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
        return this;
    }

    @Override
    public NSGAIII build() {
        return new NSGAIII(this);
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public boolean getNormalize() {
        return this.normalize;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    public CrossoverOperator getCrossover() {
        return this.crossover;
    }

    public MutationOperator getMutation() {
        return this.mutation;
    }

    public SelectionOperator getSelection() {
        return this.selection;
    }
}
