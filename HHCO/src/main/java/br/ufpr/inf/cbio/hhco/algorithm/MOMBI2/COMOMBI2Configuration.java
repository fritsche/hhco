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
package br.ufpr.inf.cbio.hhco.algorithm.MOMBI2;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 */
public class COMOMBI2Configuration extends MOMBI2Configuration {

    @Override
    public COMOMBI2<?> configure(int popSize, int maxFitnessEvaluations, Problem problem) {
        this.problem = problem;
        super.setup();
        return new COMOMBI2<>(problem, popSize * maxFitnessEvaluations, crossover, mutation, selection, new SequentialSolutionListEvaluator<>(), "WeightVectorsMOMBI2/W" + problem.getNumberOfObjectives() + "D_" + popSize + ".dat");
    }

}
