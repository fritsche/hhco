/*
 * Copyright (C) 2018 Gian Fritsche <gian.fritsche at gmail.com>
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
package br.ufpr.inf.cbio.hhco.algorithm.SPEA2SDE;

import java.util.List;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author Gian Fritsche <gian.fritsche at gmail.com>
 */
public class SPEA2SDEUtils {

    public static <S extends Solution<?>> double[][] distanceMatrixSDE(List<S> solutionSet) {
        // The matrix of distances
        double[][] distance = new double[solutionSet.size()][solutionSet.size()];
        // -> Calculate the distances
        for (int i = 0; i < solutionSet.size(); i++) {
            for (int j = 0; j < solutionSet.size(); j++) {
                if (j == i) {
                    distance[i][j] = 0;
                } else {
                    distance[i][j] = distanceBetweenObjectivesSDE(
                            solutionSet.get(i), solutionSet.get(j));
                }
            }
        } // for

        // ->Return the matrix of distances
        return distance;
    
   }
    
    public static double distanceBetweenObjectivesSDE(Solution ind1, Solution ind2) {
        int i;
        double d = 0.0;
        int nobj = ind1.getNumberOfObjectives();

        for (i = 0; i < nobj; i++) {
            if (ind1.getObjective(i) < ind2.getObjective(i)) {
                d += (ind2.getObjective(i) - ind1.getObjective(i))
                        * (ind2.getObjective(i) - ind1.getObjective(i));
            }
        }
        return Math.sqrt(d);
    }

}
