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
package br.ufpr.inf.cbio.hhco.runner.methodology;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 */
public class ArionMethodology implements Methodology {

    private final int populationSize;
    private final int maxFitnessEvaluations;

    public ArionMethodology(String problemName) {
        populationSize = initPopulationSize(problemName);
        maxFitnessEvaluations = 100 * populationSize;
    }

    private int initPopulationSize(String problem) {
        if (problem.equals("DTLZ1") || problem.equals("DTLZ3")) {
            return 256;
        }
        return 128;
    }

    @Override
    public int getPopulationSize() {
        return populationSize;
    }

    @Override
    public int getMaxFitnessEvaluations() {
        return maxFitnessEvaluations;
    }
}
