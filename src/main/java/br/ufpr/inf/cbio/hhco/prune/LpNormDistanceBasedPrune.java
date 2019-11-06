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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.ObjectiveComparator;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 */
public class LpNormDistanceBasedPrune {

    public static <S extends Solution<?>> List<S> getSubsetOfEvenlyDistributedSolutions(
            List<S> solutionList, int newSolutionListSize) {
        List<S> resultSolutionList = new ArrayList<>(newSolutionListSize);
        if (solutionList == null) {
            throw new JMetalException("The solution list is null");
        }
        int m = solutionList.get(0).getNumberOfObjectives();

        List<S> solutionListCopy = new ArrayList<>(solutionList);
        // 1. get boundary solutions
        for (int i = 0; i < m; i++) {
            Collections.sort(solutionListCopy, new ObjectiveComparator<>(i));
            resultSolutionList.add(solutionListCopy.get(0));
            solutionListCopy.remove(0);
        }

        // 2. iteratively get the most distant solution
        LpNormDistanceBetweenSolutionsInObjectiveSpace distanceCalculator = new LpNormDistanceBetweenSolutionsInObjectiveSpace(m);

        while (resultSolutionList.size() < newSolutionListSize) {
            double max_d = Double.NEGATIVE_INFINITY;
            S selected = null;
            for (S s : solutionListCopy) {
                double min_d = Double.POSITIVE_INFINITY;
                for (S r : resultSolutionList) {
                    double d = distanceCalculator.getDistance(s, r);
                    min_d = Math.min(min_d, d);
                }
                if (min_d > max_d) {
                    max_d = min_d;
                    selected = s;
                }
            }
            resultSolutionList.add(selected);
            solutionListCopy.remove(selected);
        }

        return resultSolutionList;
    }

}
