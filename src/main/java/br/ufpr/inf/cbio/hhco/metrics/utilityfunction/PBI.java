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
package br.ufpr.inf.cbio.hhco.metrics.utilityfunction;

import org.uma.jmetal.util.point.Point;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 */
public class PBI implements UtilityFunction {

    @Override
    public double execute(double[] lambda, Point point, int m) {
        double fitness;
        double theta; // penalty parameter
        theta = 5.0;
        // normalize the weight vector (line segment)
        double nd = norm_vector(lambda, m);
        for (int i = 0; i < m; i++) {
            lambda[i] = lambda[i] / nd;
        }
        double[] realA = new double[m];
        double[] realB = new double[m];
        // difference between current point and reference point
        for (int n = 0; n < m; n++) {
            realA[n] = point.getValue(n);
        }   // distance along the line segment
        double d1 = Math.abs(innerproduct(realA, lambda));
        // distance to the line segment
        for (int n = 0; n < m; n++) {
            realB[n] = (point.getValue(n) - d1 * lambda[n]);
        }
        double d2 = norm_vector(realB, m);
        fitness = d1 + theta * d2;
        return fitness;
    }

    public double norm_vector(double[] z, int m) {
        double sum = 0;

        for (int i = 0; i < m; i++) {
            sum += z[i] * z[i];
        }

        return Math.sqrt(sum);
    }

    public double innerproduct(double[] vec1, double[] vec2) {
        double sum = 0;

        for (int i = 0; i < vec1.length; i++) {
            sum += vec1[i] * vec2[i];
        }

        return sum;
    }

}
