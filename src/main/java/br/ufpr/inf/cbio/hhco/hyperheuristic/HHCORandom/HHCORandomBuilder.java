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

import br.ufpr.inf.cbio.hhco.hyperheuristic.CooperativeAlgorithm;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <S>
 */
public class HHCORandomBuilder<S extends Solution<?>> implements AlgorithmBuilder<HHCORandom<S>> {

    protected List<CooperativeAlgorithm> algorithms;
    protected int populationSize;
    protected int maxEvaluations;
    protected final Problem problem;

    public List<CooperativeAlgorithm> getAlgorithms() {
        return algorithms;
    }

    public HHCORandomBuilder setAlgorithms(List<CooperativeAlgorithm> algorithms) {
        this.algorithms = algorithms;
        return this;
    }

    public HHCORandomBuilder addAlgorithm(CooperativeAlgorithm algorithm) {
        if (algorithms == null) {
            algorithms = new ArrayList<>();
        }
        algorithms.add(algorithm);
        return this;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public HHCORandomBuilder setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
        return this;
    }

    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    public HHCORandomBuilder setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;
        return this;
    }
    
    public HHCORandomBuilder(Problem problem) {
        this.problem = problem;
    }
    
    @Override
    public HHCORandom<S> build() {
        return new HHCORandom(algorithms, populationSize, maxEvaluations, problem);
    }

}
