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
package br.ufpr.inf.cbio.hhco.utils;

import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 */
public class MockRandomNumberGenerator implements PseudoRandomGenerator {

    private int index;
    private final double[] values;

    public MockRandomNumberGenerator(int index, double[] values) {
        this.index = index;
        this.values = values;
    }

    public MockRandomNumberGenerator(double[] d) {
        this(-1, d);
    }
    
    @Override
    public int nextInt(int i, int i1) {
        return (int) nextDouble();
    }

    @Override
    public double nextDouble(double d, double d1) {
        return nextDouble();
    }

    @Override
    public double nextDouble() {
        index = (index+1) % values.length;
        return values[index];
    }

    @Override
    public void setSeed(long l) {
        this.index = (int) l;
    }

    @Override
    public long getSeed() {
        return index;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

}
