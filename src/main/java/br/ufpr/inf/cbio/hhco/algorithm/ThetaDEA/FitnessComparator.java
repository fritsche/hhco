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
package br.ufpr.inf.cbio.hhco.algorithm.ThetaDEA;

import java.util.Comparator;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 * @param <S>
 */
public class FitnessComparator<S extends Solution> implements Comparator {


	private final boolean ascendingOrder_;


	public FitnessComparator() {
		ascendingOrder_ = true;
	} 

	public FitnessComparator(boolean descendingOrder) {
            ascendingOrder_ = !descendingOrder;
	} 

        @Override
	public int compare(Object o1, Object o2) {
		if (o1 == null)
			return 1;
		else if (o2 == null)
			return -1;

		double f1 = (double) ((S) o1).getAttribute("Fitness");
		double f2 = (double) ((S) o2).getAttribute("Fitness");
		if (ascendingOrder_) {
			if (f1 < f2) {
				return -1;
			} else if (f1 > f2) {
				return 1;
			} else {
				return 0;
			}
		} else {
			if (f1 < f2) {
				return 1;
			} else if (f1 > f2) {
				return -1;
			} else {
				return 0;
			}
		}
	} // compare
} 
