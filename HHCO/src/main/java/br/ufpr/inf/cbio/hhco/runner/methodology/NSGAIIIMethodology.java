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

import org.uma.jmetal.util.JMetalException;

/**
 * Based on K Deb and H Jain, "An Evolutionary Many-Objective Optimization
 * Algorithm Using Reference-Point-Based Nondominated Sorting Approach, Part I:
 * Solving Problems With Box Constraints". in IEEE Transactions on Evolutionary
 * Computation, vol. 18, no. 4, pp. 577-601, Aug. 2014.
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 */
public class NSGAIIIMethodology implements Methodology {

    private final int populationSize;
    private final int maxFitnessEvaluations;

    public NSGAIIIMethodology(String problemName, int numberOfObjectives) {
        this.populationSize = initPopulationSize(numberOfObjectives);
        this.maxFitnessEvaluations = this.populationSize * getGenerationsNumber(problemName, numberOfObjectives);
    }

    @Override
    public int getPopulationSize() {
        return populationSize;
    }

    @Override
    public int getMaxFitnessEvaluations() {
        return maxFitnessEvaluations;
    }

    protected static int getGenerationsNumber(String problem, int m) {

        int generations = 0;

        if (problem.startsWith("Minus")) {
            return getGenerationsNumber(problem.substring(5), m);
        }

        switch (m) {
            case 3:
                switch (problem) {
                    case "DTLZ1":
                        generations = 400; // DTLZ1
                        break;
                    case "DTLZ2":
                        generations = 250; // DTLZ2
                        break;
                    case "DTLZ3":
                        generations = 1000; // DTLZ3
                        break;
                    case "DTLZ4":
                        generations = 600; // DTLZ4
                        break;
                    case "DTLZ7":
                        generations = 1000; // DTLZ7
                        break;
                    case "WFG1":
                    case "WFG2":
                    case "WFG3":
                    case "WFG4":
                    case "WFG5":
                    case "WFG6":
                    case "WFG7":
                    case "WFG8":
                    case "WFG9":
                        generations = 400; // WFG
                        break;
                }
                break;
            case 5:
                switch (problem) {
                    case "DTLZ1":
                        generations = 600; // DTLZ1
                        break;
                    case "DTLZ2":
                        generations = 350; // DTLZ2
                        break;
                    case "DTLZ3":
                        generations = 1000; // DTLZ3
                        break;
                    case "DTLZ4":
                        generations = 1000; // DTLZ4
                        break;
                    case "DTLZ7":
                        generations = 1000; // DTLZ7
                        break;
                    case "WFG1":
                    case "WFG2":
                    case "WFG3":
                    case "WFG4":
                    case "WFG5":
                    case "WFG6":
                    case "WFG7":
                    case "WFG8":
                    case "WFG9":
                        generations = 750; // WFG
                        break;
                }
                break;
            case 8:
                switch (problem) {
                    case "DTLZ1":
                        generations = 750; // DTLZ1
                        break;
                    case "DTLZ2":
                        generations = 500; // DTLZ2
                        break;
                    case "DTLZ3":
                        generations = 1000; // DTLZ3
                        break;
                    case "DTLZ4":
                        generations = 1250; // DTLZ4
                        break;
                    case "DTLZ7":
                        generations = 1000; // DTLZ7
                        break;
                    case "WFG1":
                    case "WFG2":
                    case "WFG3":
                    case "WFG4":
                    case "WFG5":
                    case "WFG6":
                    case "WFG7":
                    case "WFG8":
                    case "WFG9":
                        generations = 1500; // WFG
                        break;
                }
                break;
            case 10:
                switch (problem) {
                    case "DTLZ1":
                        generations = 1000;  // DTLZ1
                        break;
                    case "DTLZ2":
                        generations = 750;   // DTLZ2
                        break;
                    case "DTLZ3":
                        generations = 1500;  // DTLZ3
                        break;
                    case "DTLZ4":
                        generations = 2000;  // DTLZ4
                        break;
                    case "DTLZ7":
                        generations = 1500; // DTLZ7
                        break;
                    case "WFG1":
                    case "WFG2":
                    case "WFG3":
                    case "WFG4":
                    case "WFG5":
                    case "WFG6":
                    case "WFG7":
                    case "WFG8":
                    case "WFG9":
                        generations = 2000; // WFG
                        break;
                }
                break;
            case 15:
                switch (problem) {
                    case "DTLZ1":
                        generations = 1500;  // DTLZ1
                        break;
                    case "DTLZ2":
                        generations = 1000;  // DTLZ2
                        break;
                    case "DTLZ3":
                        generations = 2000;  // DTLZ3
                        break;
                    case "DTLZ4":
                        generations = 3000;  // DTLZ4
                        break;
                    case "DTLZ7":
                        generations = 2000; // DTLZ7
                        break;
                    case "WFG1":
                    case "WFG2":
                    case "WFG3":
                    case "WFG4":
                    case "WFG5":
                    case "WFG6":
                    case "WFG7":
                    case "WFG8":
                    case "WFG9":
                        generations = 3000; // WFG
                        break;
                }
                break;
            default:
                throw new JMetalException("There is no configurations for " + m + " objectives");
        }
        return generations;
    }

    protected static int initPopulationSize(int m) {
        switch (m) {
            case 3:
                return 91;
            case 5:
                return 210;
            case 8:
                return 156;
            case 10:
                return 275;
            case 15:
                return 135;
            default:
                throw new JMetalException("This methodology does not contains any configuration of population size for " + m + " objectives");
        }
    }

}
