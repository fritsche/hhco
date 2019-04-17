/*
 * Copyright (C) 2018 Gian Fritsche <gmfritsche at inf.ufpr.br>
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
package br.ufpr.inf.cbio.hhco.metrics.fir;

import br.ufpr.inf.cbio.hhco.metrics.indicator.R2;
import br.ufpr.inf.cbio.hhco.metrics.utilityfunction.Tchebycheff;
import br.ufpr.inf.cbio.hhco.util.WeightVectorUtils;
import java.util.List;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 */
public class R2TchebycheffFIR implements FitnessImprovementRateCalculator<Solution<?>> {

    private final double[][] lambda;
    private final int m;

    public R2TchebycheffFIR(Problem problem, int populationSize) {
        this.lambda = WeightVectorUtils.initializeUniformWeight(problem, populationSize);
        this.m = problem.getNumberOfObjectives();
    }

    @Override
    public double computeFitnessImprovementRate(List<Solution<?>> parents, List<Solution<?>> offspring) {
        int points = parents.size() + offspring.size();
        Front reference = new ArrayFront(points, m);
        Front parentsFront = new ArrayFront(parents);
        Front offspringFront = new ArrayFront(offspring);
        int i = 0;
        for (int p = 0; p < parents.size(); p++, i++) {
            reference.setPoint(i, parentsFront.getPoint(p));
        }
        for (int o = 0; o < offspring.size(); o++, i++) {
            reference.setPoint(i, offspringFront.getPoint(o));
        }
        R2 r2 = new R2(lambda, reference, new Tchebycheff());
        double parentR2 = r2.r2(parentsFront);
        double offspringR2 = r2.r2(offspringFront);
        return (parentR2 - offspringR2) / offspringR2;
    }

}
