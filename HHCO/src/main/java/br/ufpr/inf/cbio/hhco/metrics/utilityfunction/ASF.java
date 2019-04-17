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
public class ASF implements UtilityFunction {

    @Override
    public double execute(double[] lambda, Point point, int m) {

        double result = point.getValue(0) / ((lambda[0] > 0.0) ? lambda[0] : 1e-2);
        for (int n = 1; n < m; n++) {
            result = Math.max(result, point.getValue(n) / ((lambda[n] > 0.0) ? lambda[n] : 1e-2));
        }
        return result;
    }

}
