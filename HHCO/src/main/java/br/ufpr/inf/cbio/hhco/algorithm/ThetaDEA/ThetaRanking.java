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
package br.ufpr.inf.cbio.hhco.algorithm.ThetaDEA;

import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 * @param <S>
 */
public final class ThetaRanking<S extends Solution> {

    private final List<S> solutionSet_;

    private final List<List<S>> ranking_;

    private final List<S>[] refSets_;

    double[][] lambda_;
    double[] zideal_;

    double theta_;

    int obj_;

    final double inf = 1E6;

    boolean normalize_;

    public ThetaRanking(List<S> solutionSet, double[][] lambda,
            double[] zideal,
            double theta, boolean normalize) {
        this.solutionSet_ = solutionSet;
        this.lambda_ = lambda;

        this.theta_ = theta;
        this.obj_ = solutionSet.get(0).getNumberOfObjectives();

        this.zideal_ = zideal;

        this.normalize_ = normalize;

        ranking_ = new ArrayList<>();

        refSets_ = new List[lambda_.length];
        for (int i = 0; i < refSets_.length; i++) {
            refSets_[i] = new ArrayList<>();
        }

        associate();
        rank();

    }

    void associate() {

        double[] dists;
        for (int k = 0; k < solutionSet_.size(); k++) {

            S sol = solutionSet_.get(k);

            dists = getDistances(sol, lambda_[0]);
            double d2 = dists[1];
            double d1 = dists[0];
            int index = 0;

            for (int j = 1; j < lambda_.length; j++) {

                dists = getDistances(sol, lambda_[j]);

                if (dists[1] < d2) {
                    d2 = dists[1];
                    d1 = dists[0];
                    index = j;
                }
            }

            setFitness(sol, index, d1, d2);
            refSets_[index].add(sol);
            sol.setAttribute("ClusterID", index);
        }
    }

    void setFitness(Solution sol, int index, double d1, double d2) {
        if (this.normalize_) {
            if (!isObjAxis(index)) {
                sol.setAttribute("Fitness", d1 + theta_ * d2);
            } else {
                sol.setAttribute("Fitness", d1 + inf * d2);
            }
        } else {
            sol.setAttribute("Fitness", d1 + theta_ * d2);
        }
    }

    double[] getDistances(Solution sol, double[] ref) {
        if (this.normalize_) {
            return getDistancesWithNormalize(sol, ref);
        } else {
            return getDistancesWithoutNormalize(sol, ref);
        }
    }

    boolean isObjAxis(int index) {
        for (int j = 0; j < obj_; j++) {
            if (lambda_[index][j] != 0 && lambda_[index][j] != 1) {
                return false;
            }
        }
        return true;
    }

    double[] getDistancesWithoutNormalize(Solution sol, double[] ref) {
        double[] d = new double[2];

        double d1 = .0, d2, nl = .0;

        for (int i = 0; i < sol.getNumberOfObjectives(); i++) {
            d1 += (sol.getObjective(i) - zideal_[i]) * ref[i];
            nl += (ref[i] * ref[i]);
        }
        nl = Math.sqrt(nl);
        d1 = Math.abs(d1) / nl;

        d2 = 0;
        for (int i = 0; i < sol.getNumberOfObjectives(); i++) {
            d2 += ((sol.getObjective(i) - zideal_[i]) - d1 * (ref[i] / nl))
                    * ((sol.getObjective(i) - zideal_[i]) - d1 * (ref[i] / nl));
        }
        d2 = Math.sqrt(d2);

        d[0] = d1;
        d[1] = d2;

        return d;
    }

    double[] getDistancesWithNormalize(Solution sol, double[] ref) {
        double ip = 0;
        double refLenSQ = 0;

        double[] d = new double[2];

        double[] normalizedObjective_ = (double[]) sol.getAttribute("NormalizedObjective");
        
        for (int j = 0; j < obj_; j++) {

            ip += normalizedObjective_[j] * ref[j];
            refLenSQ += (ref[j] * ref[j]);
        }
        refLenSQ = Math.sqrt(refLenSQ);

        d[0] = Math.abs(ip) / refLenSQ;

        d[1] = 0;

        
        for (int i = 0; i < sol.getNumberOfObjectives(); i++) {
            d[1] += (normalizedObjective_[i] - d[0] * (ref[i] / refLenSQ))
                    * (normalizedObjective_[i] - d[0]
                    * (ref[i] / refLenSQ));
        }
        d[1] = Math.sqrt(d[1]);

        return d;
    }

    void rank() {

        int maxLen = Integer.MIN_VALUE;
        for (List<S> refSets_1 : refSets_) {
            if (refSets_1.size() > maxLen) {
                maxLen = refSets_1.size();
            }
            refSets_1.sort(new FitnessComparator());
        }

        for (int i = 0; i < maxLen; i++) {
            List<S> set = new ArrayList<>();
            for (List<S> refSets_1 : refSets_) {
                if (refSets_1.size() > i) {
                    refSets_1.get(i).setAttribute("Rank", i);
                    set.add(refSets_1.get(i));
                }
            }
            ranking_.add(set);
        }
    }

    public List<S> getSubfront(int rank) {
        return ranking_.get(rank);
    } // getSubFront

    /**
     * Returns the total number of subFronts founds.
     * @return 
     */
    public int getNumberOfSubfronts() {
        return ranking_.size();
    } // getNumberOfSubfronts

}
