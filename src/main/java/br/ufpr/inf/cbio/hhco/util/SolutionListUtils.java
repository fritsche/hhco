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
package br.ufpr.inf.cbio.hhcoanalysis.util;

import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 */
public class SolutionListUtils extends org.uma.jmetal.util.SolutionListUtils {

    public static <S extends Solution<?>> List<S> removeRepeatedSolutions(List<S> solutions) {

        List<S> newPopulation = new ArrayList<>();

        for (int i = 0; i < solutions.size(); i++) {

            S s = solutions.get(i);

            if (!contains(newPopulation, s)) {
                newPopulation.add((S) s.copy());
            }
        }

        return newPopulation;
    }

    public static<S extends Solution<?>> boolean contains(List<S> population, Solution s1) {

        if (population == null || s1 == null) {
            throw new IllegalArgumentException("The list of solutions or the solution cannot be null");
        }

        for (int i = 0; i < population.size(); i++) {

            Solution s2 = population.get(i);

            if (isEqual(s1, s2)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isEqual(Solution s1, Solution s2) {

        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("Soluton s1 and s2 cannot be null");
        }

        if (s1.getNumberOfObjectives() != s2.getNumberOfObjectives()) {
            throw new IllegalArgumentException("Solutions cannot be different number of objetives");
        }

        for (int i = 0; i < s1.getNumberOfObjectives(); i++) {

            if (s1.getObjective(i) != s2.getObjective(i)) {
                return false;
            }
        }

        return true;
    }
}
