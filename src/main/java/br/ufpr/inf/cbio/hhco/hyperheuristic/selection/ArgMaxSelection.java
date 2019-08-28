/*
 * Copyright (C) 2018 Gian Fritsche <gmfritsche at inf.ufpr.br>
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
package br.ufpr.inf.cbio.hhco.hyperheuristic.selection;

import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <T>
 */
public class ArgMaxSelection<T> extends SelectionFunction<T> {

    private final JMetalRandom random;
    boolean first;
    private double[] credits;

    public ArgMaxSelection() {
        this.random = JMetalRandom.getInstance();
    }

    @Override
    public void init() {
        first = true;
        credits = new double[lowlevelheuristics.size()];
    }

    public static boolean equals(double a, double b) {
        double EPSILON = 1E-6;
        return a == b ? true : Math.abs(a - b) < EPSILON;
    }

    @Override
    public T getNext(int it) {
        if (!first) {
            double max = credits[0];
            for (int i = 1; i < credits.length; i++) {
                if (max < credits[i]) {
                    max = credits[i];
                }
            }
            List<Integer> best = new ArrayList<>();
            for (int i = 0; i < credits.length; i++) {
                if (equals(credits[i], max)) {
                    best.add(i);
                }
            }
            s = best.get(random.nextInt(0, best.size() - 1));
        } else {
            first = false;
            s = random.nextInt(0, lowlevelheuristics.size() - 1);
        }
        return lowlevelheuristics.get(s);
    }

    @Override
    public void creditAssignment(double reward) {
        credits[s] = reward;
    }

}
