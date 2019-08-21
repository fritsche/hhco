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
package br.ufpr.inf.cbio.hhco.metrics.indicator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.point.impl.ArrayPoint;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 */
public class HypervolumeApproxTest {

    public HypervolumeApproxTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of normalize method, of class HypervolumeApprox.
     */
    @Test
    public void testNormalize() {
        System.out.println("normalize");
        int points = 3, dimensions = 2;
        Front front = new ArrayFront(points, dimensions);
        front.setPoint(0, new ArrayPoint(new double[]{0, 3}));
        front.setPoint(1, new ArrayPoint(new double[]{5, 0}));
        front.setPoint(2, new ArrayPoint(new double[]{2.5, 1.5}));

        HypervolumeApprox instance = new HypervolumeApprox(front);
        instance.normalize(front);

        Front result = new ArrayFront(points, dimensions);
        result.setPoint(0, new ArrayPoint(new double[]{0, (1.0 / 1.1) * 1.0}));
        result.setPoint(1, new ArrayPoint(new double[]{(1.0 / 1.1) * 1.0, 0}));
        result.setPoint(2, new ArrayPoint(new double[]{(1.0 / 1.1) * 0.5, (1.0 / 1.1) * 0.5}));

        assertEquals(front, result);
    }

}
