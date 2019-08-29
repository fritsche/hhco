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
import org.uma.jmetal.util.point.impl.ArrayPoint;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 */
public class R2TchebycheffFIR implements FitnessImprovementRateCalculator<Solution<?>> {

    private final double[][] lambda;
    private final int m;

    protected double[] zp_;
    protected double[] nzp_;

    public R2TchebycheffFIR(Problem problem, int populationSize) {
        this.lambda = WeightVectorUtils.initializeUniformWeight(problem, populationSize);
        this.m = problem.getNumberOfObjectives();
        initIdealPoint();
        initNadirPoint();
    }

    @Override
    public double computeFitnessImprovementRate(List<Solution<?>> parents, List<Solution<?>> offspring) {

        for (Solution<?> s : parents) {
            updateReference(s, zp_);
            updateNadirPoint(s, nzp_);
        }

        Front reference = new ArrayFront(2, m);
        reference.setPoint(0, new ArrayPoint(zp_));
        reference.setPoint(1, new ArrayPoint(nzp_));

        Front parentsFront = new ArrayFront(parents);
        Front offspringFront = new ArrayFront(offspring);

        R2 r2 = new R2(lambda, reference, new Tchebycheff());
        double parentR2 = r2.r2(parentsFront);
        double offspringR2 = r2.r2(offspringFront);

        if (offspringR2 == parentR2) {
            return 0.0;
        } else if (offspringR2 == 0.0) {
            return (parentR2 - offspringR2) * Double.POSITIVE_INFINITY;
        } else {
            return (parentR2 - offspringR2) / offspringR2;
        }
    }

    void updateReference(Solution indiv, double[] z_) {
        for (int i = 0; i < m; i++) {
            if (indiv.getObjective(i) < z_[i]) {
                z_[i] = indiv.getObjective(i);
            }
        }
    }

    void updateNadirPoint(Solution indiv, double[] nz_) {
        for (int i = 0; i < m; i++) {
            if (indiv.getObjective(i) > nz_[i]) {
                nz_[i] = indiv.getObjective(i);
            }
        }
    }

    final void initIdealPoint() {
        zp_ = new double[m];
        for (int i = 0; i < m; i++) {
            zp_[i] = 1.0e+30;
        }
    }

    final void initNadirPoint() {
        nzp_ = new double[m];
        for (int i = 0; i < m; i++) {
            nzp_[i] = -1.0e+30;
        }
    }

}
