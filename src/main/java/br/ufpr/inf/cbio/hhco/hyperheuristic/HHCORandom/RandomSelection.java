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
package br.ufpr.inf.cbio.hhco.hyperheuristic.HHCORandom;

import br.ufpr.inf.cbio.hhco.hyperheuristic.selection.SelectionFunction;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <T>
 */
public class RandomSelection<T> extends SelectionFunction<T> {

    private final JMetalRandom random;

    public RandomSelection() {
        this.random = JMetalRandom.getInstance();
    }

    @Override
    public void init() {

    }

    @Override
    public T getNext(int it) {
        s = random.nextInt(0, lowlevelheuristics.size() - 1);
        return lowlevelheuristics.get(s);
    }

    @Override
    public void creditAssignment(double reward) {

    }

}
