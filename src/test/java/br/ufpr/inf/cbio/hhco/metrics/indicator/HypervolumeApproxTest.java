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

import java.util.List;
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
        int points = 4, dimensions = 2;
        Front front = new ArrayFront(points, dimensions);
        front.setPoint(0, new ArrayPoint(new double[]{1.00, 4.00}));
        front.setPoint(1, new ArrayPoint(new double[]{1.50, 3.00}));
        front.setPoint(2, new ArrayPoint(new double[]{1.75, 2.00}));
        front.setPoint(3, new ArrayPoint(new double[]{2.00, 1.00}));

        HypervolumeApprox instance = new HypervolumeApprox(front);
        instance.normalize(front);

        Front result = new ArrayFront(points, dimensions);

        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
