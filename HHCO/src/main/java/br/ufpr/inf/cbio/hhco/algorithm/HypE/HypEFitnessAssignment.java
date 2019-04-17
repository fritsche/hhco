/*
 * Copyright (C) 2018 Gian Fritsche <gian.fritsche@gmail.com>
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
package br.ufpr.inf.cbio.hhco.algorithm.HypE;

import java.util.List;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.solutionattribute.impl.Fitness;

/**
 *
 * @author Gian Fritsche <gian.fritsche@gmail.com>
 * @param <S>
 */
public class HypEFitnessAssignment<S extends Solution> {

    private Fitness fitness = new Fitness();

    public void setHypEFitness(List<S> population, Solution reference, int k, int nrOfSamples) {
        if (reference.getNumberOfObjectives() <= 3) {
            setExactHypEFitness(population, reference, k);
        } else {
            setEstimatedHypEFitness(population, reference, k, nrOfSamples);
        }
    }

    void setExactHypEFitness(List<S> population, Solution reference, int k) {
        HypEFitness hy = new HypEFitness();

        int objs = reference.getNumberOfObjectives();

        int size = population.size();

        double[][] points = new double[size + 1][objs + 1];

        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= objs; j++) {
                points[i][j] = population.get(i - 1).getObjective(j - 1);
            }
        }

        double[] bounds = new double[objs + 1];
        for (int i = 1; i <= objs; i++) {
            bounds[i] = reference.getObjective(i - 1);
        }

        double[] result = hy.hypeIndicatorExact(points, bounds, k);

        for (int i = 1; i <= size; i++) {
            fitness.setAttribute(population.get(i - 1), result[i]);
        }

    }

    void setEstimatedHypEFitness(List<S> population,
            Solution reference, int k, int nrOfSamples) {

        HypEFitness hy = new HypEFitness();

        int objs = reference.getNumberOfObjectives();

        int size = population.size();

        double[][] points = new double[size][objs];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < objs; j++) {
                points[i][j] = population.get(i).getObjective(j);
            }
        }

        double lowerbound = 0;
        double upperbound = reference.getObjective(0);

        double[] result = hy.hypeIndicatorSampled(points, lowerbound, upperbound, k,
                nrOfSamples);

        for (int i = 0; i < size; i++) {
            fitness.setAttribute(population.get(i), result[i]);
        }

    }
}
