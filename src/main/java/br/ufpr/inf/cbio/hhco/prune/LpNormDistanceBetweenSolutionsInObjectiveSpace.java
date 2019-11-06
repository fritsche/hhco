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
package br.ufpr.inf.cbio.hhcoanalysis.prune;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.distance.Distance;

/**
 * Lp-Norm distance based on Two_Arch2 algorithm.
 *
 * Wang, H., Jiao, L., & Yao, X. (2015). Two Arch2: An Improved Two-Archive
 * Algorithm for Many-Objective Optimization. IEEE Transactions on Evolutionary
 * Computation, 19(4), 524–541. https://doi.org/10.1109/TEVC.2014.2350987
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <S>
 */
public class LpNormDistanceBetweenSolutionsInObjectiveSpace<S extends Solution<?>> implements Distance<S, S> {

    private double p;
    private int m;

    public LpNormDistanceBetweenSolutionsInObjectiveSpace(int m) {
        this(1.0 / (double) m, m);
    }

    public LpNormDistanceBetweenSolutionsInObjectiveSpace(double p, int m) {
        this.p = p;
        this.m = m;
    }

    @Override
    public double getDistance(S solution1, S solution2) {
        double diff;
        double distance = 0.0;

        for (int nObj = 0; nObj < m; nObj++) {
            diff = solution1.getObjective(nObj) - solution2.getObjective(nObj);
            distance += Math.pow(Math.abs(diff), p);
        }
        return Math.pow(Math.abs(distance), 1.0 / p);
    }

}
