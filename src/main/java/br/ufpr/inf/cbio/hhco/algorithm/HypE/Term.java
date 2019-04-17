/*
 * Copyright (C) 2018 Gian Fritsche <gian.fritsche@gmail.com>
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
package br.ufpr.inf.cbio.hhco.algorithm.HypE;

/**
 *
 * @author Gian Fritsche <gian.fritsche@gmail.com>
 */
public class Term implements Comparable<Term> {

    public static int loc = 1;

    public double[] data;
    public int index;

    public Term(double[] data, int index) {
        this.data = data;
        this.index = index;
    }

    @Override
    public int compareTo(Term arg0) {
        double v1 = data[loc];
        double v2 = arg0.data[loc];

        if (v1 < v2) {
            return -1;
        } else if (v1 > v2) {
            return 1;
        } else {
            return 0;
        }
    }
}
