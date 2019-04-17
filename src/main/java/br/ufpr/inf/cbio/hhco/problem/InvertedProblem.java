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
package br.ufpr.inf.cbio.hhco.problem;

import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 */
public class InvertedProblem extends AbstractDoubleProblem {

    private final DoubleProblem problem;
    private final String name;

    public InvertedProblem(DoubleProblem problem, String name) {
        this.name = name;
        this.problem = problem;
        setNumberOfVariables(problem.getNumberOfVariables());
        setNumberOfObjectives(problem.getNumberOfObjectives());
        setNumberOfConstraints(problem.getNumberOfConstraints());

        List<Double> lower = new ArrayList<>(getNumberOfVariables()), upper = new ArrayList<>(getNumberOfVariables());

        for (int i = 0; i < getNumberOfVariables(); i++) {
            lower.add(problem.getLowerBound(i));
            upper.add(problem.getUpperBound(i));
        }

        setLowerLimit(lower);
        setUpperLimit(upper);
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        problem.evaluate(solution);
        for (int m = 0; m < getNumberOfObjectives(); m++) {
            solution.setObjective(m, solution.getObjective(m) * -1);
        }
    }

    @Override
    public String getName() {
        return name;
    }

}
