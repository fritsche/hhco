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
package br.ufpr.inf.cbio.hhco.util;

import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.problem.multiobjective.dtlz.DTLZ1;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 */
public class RandomWeightVectorGenerator {

    public void printPoints(List<DefaultDoubleSolution> points, int n, int m) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                System.out.print("\t" + points.get(i).getObjective(j));
            }
            System.out.println();
        }
    }

    /**
     * Generate n points approximately uniformly distributed.
     *
     * @param m number of objectives
     * @param n number of points to generate
     * @param N number of generated random points
     */
    public void generate(int m, int n, int N) {
        List<DefaultDoubleSolution> points = new ArrayList<>(N); // list of points
        JMetalRandom rand = JMetalRandom.getInstance(); // random generator
        DefaultDoubleSolution aux = new DefaultDoubleSolution(new DTLZ1(0, m)); // empty solution

        // generate extreme points
        for (int i = 0; i < m; i++) {
            points.add(new DefaultDoubleSolution(aux));
            points.get(i).setObjective(i, 1);
        }

        // generate random points
        for (int i = m; i < N; i++) {
            points.add(new DefaultDoubleSolution(aux));
            double sum = 0;
            for (int j = 1; j < m; j++) {
                points.get(i).setObjective(j, rand.nextDouble(0, 1 - sum));
                sum += points.get(i).getObjective(j);
            }
            points.get(i).setObjective(0, 1 - sum);
        }

        printPoints(MOEADUtils.getSubsetOfEvenlyDistributedSolutions(points, n), n, m);

    }

    public void generate(int n) {
        List<DefaultDoubleSolution> points = new ArrayList<>(n); // list of points
        DefaultDoubleSolution aux = new DefaultDoubleSolution(new DTLZ1(0, 2)); // empty solution
        double inc = 1.0 / (n - 1.0);
        double v = inc;

        // generate extreme points
        for (int i = 0; i < 2; i++) {
            points.add(new DefaultDoubleSolution(aux));
            points.get(i).setObjective(i, 1);
        }

        // generate evenly distributed points
        for (int i = 2; i < n; i++, v += inc) {
            points.add(new DefaultDoubleSolution(aux));
            points.get(i).setObjective(0, v);
            points.get(i).setObjective(1, 1.0 - v);
        }

        printPoints(points, n, 2);

    }

    public static void main(String[] args) {

        if (args.length != 4) {
            throw new JMetalException("Wrong number of arguments: seed number_of_objectives number_of_output_points number_of_random_points");
        }

        int i = 0;
        int seed = Integer.parseInt(args[i++]);
        JMetalRandom.getInstance().setSeed(seed);

        int m = Integer.parseInt(args[i++]); // number of objectives
        int n = Integer.parseInt(args[i++]); // number of output points
        int N = Integer.parseInt(args[i++]); // number of random points

        RandomWeightVectorGenerator generator = new RandomWeightVectorGenerator();

        if (m == 2) {
            generator.generate(n);
        } else {
            generator.generate(m, n, N);
        }
    }
}
