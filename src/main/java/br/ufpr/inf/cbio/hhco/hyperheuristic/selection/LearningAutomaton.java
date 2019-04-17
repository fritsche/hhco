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
package br.ufpr.inf.cbio.hhco.hyperheuristic.selection;

import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 *
 * Li, W., Ozcan, E., & John, R. (2017). A Learning Automata based
 * Multiobjective Hyper-heuristic. IEEE Transactions on Evolutionary
 * Computation, (c), 1–15. https://doi.org/10.1109/TEVC.2017.2785346
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <T>
 */
public class LearningAutomaton<T> extends SelectionFunction<T> {

    /**
     * Transition probability matrix
     */
    private double[][] p;
    /**
     * Number of MOEAS
     */
    private int r;

    /**
     * Estimated action value matrix
     */
    private double[][] q;

    /**
     * Previously applied heuristic
     */
    private int i;
    /**
     * Currently applied heuristic
     */
    private int j;

    /**
     * Fixed discount ratio of the past reward
     */
    private final double alpha = 0.1;

    /**
     * A small positive multiplier
     */
    private final double m;

    /**
     * Total number of iterations
     */
    private final int n;

    /**
     * Exploration phase parameter
     */
    private final double tau;

    private final JMetalRandom random;

    public LearningAutomaton(double m, int n, double tau) {
        random = JMetalRandom.getInstance();
        this.m = m;
        this.n = n;
        this.tau = tau;
    }

    @Override
    public void init() {
        r = lowlevelheuristics.size();
        p = new double[r][r];
        for (int k = 0; k < r; k++) {
            for (int l = 0; l < r; l++) {
                p[k][l] = 1.0 / (double) r;
            }
        }
        q = new double[r][r]; // initialized with zeros
        j = random.nextInt(0, r - 1); // set a random heuristic as "previous"
    }

    /**
     * Section III.C Meta-heuristic Selection Method
     *
     * @param it
     * @return
     */
    @Override
    public T getNext(int it) {
        i = j;
        if (it < tau * n) {
            s = roulette();
        } else {
            double epsilon = tau + (1 - tau) * it / n;
            double rand = random.nextDouble();
            if (rand <= epsilon) {
                s = greedy();
            } else {
                s = roulette();
            }
        }
        it++;
        return lowlevelheuristics.get(s);
    }

    private int greedy() {
        double max = 0.0;
        for (int l = 0; l < r; l++) {
            if (p[i][l] > max) {
                max = p[i][l];
                j = l;
            }
        }
        return j;
    }

    private int roulette() {
        double sum = 0.0;
        int l;
        double rand = random.nextDouble();
        for (l = 0; l < r && rand > sum; l++) {
            sum += p[i][l];
        }
        j = l-1;
        return j;
    }

    /**
     * Section III.B. Reinforcement Learning Scheme
     *
     * @param reward
     */
    @Override
    public void creditAssignment(double reward) {
        // Equation 9
        q[i][j] = q[i][j] + alpha * (reward - q[i][j]);
        // Equation 11
        double lambda = 0.1 + m * q[i][j];
        lambda = Math.max(lambda, 0.0);
        lambda = Math.min(lambda, 1.0);

        int beta = reward > 0.0 ? 1 : 0;

        // Equation 7
        p[i][j] = p[i][j] + lambda * beta * (1 - p[i][j]) - lambda * (1 - beta) * p[i][j];

        for (int l = 0; l < r; l++) {
            if (l != j) {
                // Equation 8
                p[i][l] = p[i][l] - lambda * beta * p[i][l] + lambda * (1 - beta) * (1 / (r - 1) - p[i][l]);
            }
        }
    }

}
