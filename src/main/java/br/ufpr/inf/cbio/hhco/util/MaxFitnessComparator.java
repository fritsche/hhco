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
package br.ufpr.inf.cbio.hhco.util;

import java.util.Comparator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.FitnessComparator;

/**
 *
 * @author Gian Fritsche <gian.fritsche at gmail.com>
 * @param <S>
 */
public class MaxFitnessComparator<S extends Solution<?>> implements Comparator<S> {

    private final FitnessComparator<S> comparator = new FitnessComparator<>();

    @Override
    public int compare(S s1, S s2) {
        return comparator.compare(s2, s1);
    }

}
