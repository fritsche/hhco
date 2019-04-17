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
package br.ufpr.inf.cbio.hhco.algorithm.ThetaDEA;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 * @param <S>
 */
public class ThetaDEABuilder<S extends Solution> implements AlgorithmBuilder<ThetaDEA<S>> {

    private final Problem<S> problem;
    private int maxEvaluations;
    private double theta;
    private boolean normalize;
    private int populationSize;
    private CrossoverOperator crossover;
    private MutationOperator mutation;

    ThetaDEABuilder(Problem<S> problem) {
        this.problem = problem;
    }

    public Problem getProblem() {
        return this.problem;
    }

    public ThetaDEABuilder setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;
        return this;
    }

    public ThetaDEABuilder setTheta(double theta) {
        this.theta = theta;
        return this;
    }

    public ThetaDEABuilder setNormalize(boolean normalize) {
        this.normalize = normalize;
        return this;
    }
    
    public ThetaDEABuilder setCrossover(CrossoverOperator crossover) {
        this.crossover = crossover;
        return this;
    }
    
    public ThetaDEABuilder setMutation (MutationOperator mutation) {
        this.mutation = mutation;
        return this;
    }

    public ThetaDEABuilder setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
        return this;
    }

    @Override
    public ThetaDEA build() {
        return new ThetaDEA(this);
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public double getTheta() {
        return this.theta;
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

    public MutationOperator getMutation(){
        return this.mutation;
    }
}
