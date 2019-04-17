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

import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 * Class providing int [] permutations
 */
public class Permutation {

    /**
     * Return a permutation vector between the 0 and (length - 1)
     *
     * @param length
     * @return
     */
    public int[] intPermutation(int length) {
        int[] aux = new int[length];
        int[] result = new int[length];
        JMetalRandom randomGenerator = JMetalRandom.getInstance();
        // First, create an array from 0 to length - 1. We call them result
        // Also is needed to create an random array of size length
        for (int i = 0; i < length; i++) {
            result[i] = i;
            aux[i] = randomGenerator.nextInt(0, length - 1);
        } // for

        // Sort the random array with effect in result, and then we obtain a
        // permutation array between 0 and length - 1
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                if (aux[i] > aux[j]) {
                    int tmp;
                    tmp = aux[i];
                    aux[i] = aux[j];
                    aux[j] = tmp;
                    tmp = result[i];
                    result[i] = result[j];
                    result[j] = tmp;
                } // if
            } // for
        } // for   
        return result;
    }
} // Permutation
