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

import org.uma.jmetal.solution.Solution;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author Juan J. Durillo
 * @param <S>
 */
public class StrengthFitnessComparatorSDE<S extends Solution<?>> implements Comparator<S>, Serializable {

    private final StrengthRawFitnessSDE<S> fitnessValue = new StrengthRawFitnessSDE<>();

    @Override
    public int compare(S solution1, S solution2) {
        int result;
        if (solution1 == null) {
            if (solution2 == null) {
                result = 0;
            } else {
                result = 1;
            }
        } else if (solution2 == null) {
            result = -1;
        } else {
            double strengthFitness1 = Double.MIN_VALUE;
            double strengthFitness2 = Double.MIN_VALUE;

            if (fitnessValue.getAttribute(solution1) != null) {
                strengthFitness1 = (double) fitnessValue.getAttribute(solution1);
            }

            if (fitnessValue.getAttribute(solution2) != null) {
                strengthFitness2 = (double) fitnessValue.getAttribute(solution2);
            }

            if (strengthFitness1 < strengthFitness2) {
                result = -1;
            } else if (strengthFitness1 > strengthFitness2) {
                result = 1;
            } else {
                result = 0;
            }
        }
        return result;
    }

}
