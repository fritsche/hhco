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
package br.ufpr.inf.cbio.hhco.algorithm.HypE;

import org.uma.jmetal.problem.Problem;

/**
 *
 * @author Gian Fritsche <gian.fritsche at gmail.com>
 */
public class COHypEConfiguration extends HypEConfiguration {

    @Override
    public COHypE configure(int popSize, int maxFitnessEvaluations, Problem problem) {
        this.problem = problem;
        setup();
        return (COHypE) (new COHypEBuilder(problem))
                .setCrossoverOperator(crossoverOperator)
                .setMaxEvaluations(maxFitnessEvaluations)
                .setMutationOperator(mutationOperator)
                .setPopulationSize(popSize).setSamples(samples)
                .setSelectionOperator(selectionOperator).build();
    }

}
