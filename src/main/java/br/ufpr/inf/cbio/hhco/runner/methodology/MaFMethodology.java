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
package br.ufpr.inf.cbio.hhco.runner.methodology;

/**
 * Based on Ran Cheng, Miqing Li, Ye Tian, Xingyi Zhang, Shengxiang Yang, Yaochu
 * Jin and Xin Yao "Benchmark Functions for the CEC'2018 Competition on
 * Many-Objective Optimization", Technical Report, University of Birmingham,
 * United Kingdom, 2018.
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 */
public class MaFMethodology implements Methodology {

    private final int populationSize;
    private final int maxFitnessEvaluations;

    public MaFMethodology(int numberOfObjectives, int numberOfVariables) {
        /**
         * Using same population size than NSGAIIIMethodology since
         * MaFMethodology does not define a population size. The MaFMethodology
         * only defines a final population size of 240. Therefore, when using
         * this methodology the final population should be pruned to this limit
         * of 240.
         */
        this.populationSize = NSGAIIIMethodology.initPopulationSize(numberOfObjectives);
        this.maxFitnessEvaluations = Math.max(100000, 10000 * numberOfVariables);
    }

    @Override
    public int getPopulationSize() {
        return populationSize;
    }

    @Override
    public int getMaxFitnessEvaluations() {
        return maxFitnessEvaluations;
    }

}
