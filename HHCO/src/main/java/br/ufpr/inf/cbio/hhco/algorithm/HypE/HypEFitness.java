/*
 * Copyright (C) 2018 Gian Fritsche <gian.fritsche@gmail.com>
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

import java.util.Arrays;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 *
 * @author Gian Fritsche <gian.fritsche@gmail.com>
 */
public class HypEFitness {

    public double[] hypeIndicatorSampled(double[][] points, double lowerbound[],
            double upperbound[], int param_k, int nrOfSamples) {

        int i, j, k, s;

        int popsize = points.length;

        int dim = points[0].length;

        double[] val = new double[popsize];

        double[] rho = new double[param_k + 1];

        rho[0] = 0;
        for (i = 1; i <= param_k; i++) {
            rho[i] = 1.0 / (double) i;
            for (j = 1; j <= i - 1; j++) {
                rho[i] *= (double) (param_k - j) / (double) (popsize - j);
            }
        }

        int hitstat[] = new int[popsize];
        int domCount;

        double[] sample = new double[dim];
        for (s = 0; s < nrOfSamples; s++) {
            for (k = 0; k < dim; k++) {
                sample[k] = JMetalRandom.getInstance().nextDouble(lowerbound[k], upperbound[k]);
            }

            domCount = 0;
            for (i = 0; i < popsize; i++) {
                if (weaklyDominates(points[i], sample, dim)) {
                    domCount++;
                    if (domCount > param_k) {
                        break;
                    }
                    hitstat[i] = 1;
                } else {
                    hitstat[i] = 0;
                }
            }
            if (domCount > 0 && domCount <= param_k) {
                for (i = 0; i < popsize; i++) {
                    if (hitstat[i] == 1) {
                        val[i] += rho[domCount];
                    }
                }
            }
        }

        double temp = 1.0;
        for (i = 0; i < dim; i++) {
            temp *= (upperbound[i] - lowerbound[i]);
        }

        for (i = 0; i < popsize; i++) {
            val[i] = val[i] * temp / (double) nrOfSamples;
        }

        return val;
    }

    public double[] hypeIndicatorSampled(double[][] points, double lowerbound,
            double upperbound, int param_k, int nrOfSamples) {

        int i, j, k, s;

        int popsize = points.length;

        int dim = points[0].length;

        double[] val = new double[popsize];

        double[] rho = new double[param_k + 1];

        rho[0] = 0;
        for (i = 1; i <= param_k; i++) {
            rho[i] = 1.0 / (double) i;
            for (j = 1; j <= i - 1; j++) {
                rho[i] *= (double) (param_k - j) / (double) (popsize - j);
            }
        }

        int hitstat[] = new int[popsize];
        int domCount;

        double[] sample = new double[dim];
        for (s = 0; s < nrOfSamples; s++) {
            for (k = 0; k < dim; k++) {
                sample[k] = JMetalRandom.getInstance().nextDouble(lowerbound, upperbound);
            }

            domCount = 0;
            for (i = 0; i < popsize; i++) {
                if (weaklyDominates(points[i], sample, dim)) {
                    domCount++;
                    if (domCount > param_k) {
                        break;
                    }
                    hitstat[i] = 1;
                } else {
                    hitstat[i] = 0;
                }
            }
            if (domCount > 0 && domCount <= param_k) {
                for (i = 0; i < popsize; i++) {
                    if (hitstat[i] == 1) {
                        val[i] += rho[domCount];
                    }
                }
            }
        }

        double tmp = Math.pow((upperbound - lowerbound), dim)
                / (double) nrOfSamples;

        for (i = 0; i < popsize; i++) {
            val[i] = val[i] * tmp;
        }

        return val;
    }

    public double[] hypeIndicatorExact(double[][] points, double[] bounds, int k) {

        int Ps = points.length - 1;
        if (k < 0) {
            k = Ps;
        }

        int actDim = points[0].length - 1;

        int[] pvec = new int[Ps + 1];

        for (int i = 1; i <= Ps; i++) {
            pvec[i] = i;
        }

        double[] alpha = new double[Ps + 1];
        for (int i = 1; i <= k; i++) {
            alpha[i] = 1;
            for (int j = 1; j <= i - 1; j++) {
                alpha[i] *= ((double) (k - j) / (Ps - j));
            }
            alpha[i] = alpha[i] / i;
        }

        return hypesub(Ps, points, actDim, bounds, pvec, alpha, k);
    }

    double[] hypesub(int Ps, double[][] A, int actDim, double[] bounds,
            int[] pvec, double[] alpha, int k) {

        double[] h = new double[Ps + 1];

        double[][] S = copy(A);

        Term.loc = actDim;
        int[] index = sortrows(S);

        int[] temp = new int[pvec.length];

        for (int i = 1; i < pvec.length; i++) {
            temp[i] = pvec[index[i]];
        }

        pvec = temp;

        for (int i = 1; i < S.length; i++) {
            double extrusion = 0;
            if (i < S.length - 1) {
                extrusion = S[i + 1][actDim] - S[i][actDim];
            } else {
                extrusion = bounds[actDim] - S[i][actDim];
            }

            if (actDim == 1) {
                if (i > k) {
                    break;
                }

                for (int u = 1; u <= i; u++) {
                    h[pvec[u]] = h[pvec[u]] + extrusion * alpha[i];
                }

            } else if (extrusion > 0) {
                double[] f = hypesub(Ps, getPartMatrix(S, i), actDim - 1,
                        bounds, getPartArray(pvec, i), alpha, k);

                for (int w = 1; w <= Ps; w++) {
                    h[w] = h[w] + extrusion * f[w];
                }
            }

        }

        return h;
    }

    boolean weaklyDominates(double[] point1, double[] point2, int no_objectives) {
        boolean better;
        int i = 0;
        better = true;

        while (i < no_objectives && better) {
            better = (point1[i] <= point2[i]);
            i++;
        }
        return better;
    }

    int[] getPartArray(int[] array, int size) {
        int out[] = new int[size + 1];
        for (int i = 1; i <= size; i++) {
            out[i] = array[i];
        }

        return out;
    }

    double[][] getPartMatrix(double[][] S, int size) {
        double[][] out = new double[size + 1][S[0].length];

        for (int i = 1; i <= size; i++) {
            for (int j = 1; j < out[0].length; j++) {
                out[i][j] = S[i][j];
            }
        }
        return out;
    }

    int[] sortrows(double[][] T) {

        Term[] termArray = new Term[T.length - 1];
        for (int i = 0; i < termArray.length; i++) {
            termArray[i] = new Term(T[i + 1], i + 1);
        }
        Arrays.sort(termArray);

        int[] index = new int[T.length];
        for (int i = 1; i < T.length; i++) {
            index[i] = termArray[i - 1].index;
            T[i] = termArray[i - 1].data;
        }
        return index;
    }

    double[][] copy(double[][] matrix) {
        double[][] out = new double[matrix.length][matrix[0].length];

        for (int i = 1; i < out.length; i++) {
            for (int j = 1; j < out[i].length; j++) {
                out[i][j] = matrix[i][j];
            }
        }
        return out;
    }

}
