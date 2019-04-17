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
package br.ufpr.inf.cbio.hhco.algorithm.SPEA2;

import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 */
public class COSPEA2Builder extends SPEA2Builder<Solution<?>> {
    
    public COSPEA2Builder(Problem<Solution<?>> problem, CrossoverOperator<Solution<?>> crossoverOperator, MutationOperator<Solution<?>> mutationOperator) {
        super(problem, crossoverOperator, mutationOperator);
    }

    @Override
    public COSPEA2<Solution<?>> build() {
        return new COSPEA2(problem, maxIterations, populationSize, crossoverOperator, mutationOperator, selectionOperator, evaluator);
    }
  
}
