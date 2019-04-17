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
package br.ufpr.inf.cbio.hhco.hyperheuristic.selection;

import br.ufpr.inf.cbio.hhco.hyperheuristic.selection.utils.SlidingWindow;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 * @param <T>
 */
public class FRRMAB<T> extends SelectionFunction<T> {

    protected JMetalRandom random;
    protected SlidingWindow<Integer> slidingWindow;
    protected final double c;
    protected final int w;
    protected double[] UCB;
    protected int[] count;
    protected double[] reward; // accumulated reward over sliding window of each llh
    protected int[] usage; // count of usage over sliding window of each llh
    protected int[] rank;
    protected int unplayed; // amount of unplayed heuristics

    public FRRMAB() {
        this.c = 1.0;
        this.w = 100;
        this.random = JMetalRandom.getInstance();
    }

    // init the high level heuristic attributes
    @Override
    public void init() {
        int size = lowlevelheuristics.size();
        UCB = new double[size];
        count = new int[size];
        reward = new double[size];
        usage = new int[size];
        rank = new int[size];
        for (int i = 0; i < lowlevelheuristics.size(); ++i) {
            UCB[i] = 0.0;
            count[i] = 0;
        }
        unplayed = size;
        slidingWindow = new SlidingWindow<>(w);

    }

    protected int getRandomHeuristic(int values[], int max, int ref) {
        int i = random.nextInt(0, max); // select the ith randomly
        int j, p;
        j = p = 0;
        while (j <= i) { // search for ith
            if (values[p] == ref) // ref is a valid heuristic, not ref is not valid
            {
                j++;
            }
            p++;
        }
        return p - 1;
    }

    // return the next low-level heuristic
    @Override
    public T getNext(int it) {
        if (unplayed > 0) { // if has unplayed heuristics
            setS(getRandomHeuristic(count, unplayed - 1, 0)); // get a random heuristic with count equals to zero (0)
            //System.out.println("s: "+s+" unplayed: "+unplayed);
            unplayed--;
        } else { // get best heuristic
            double max = Double.NEGATIVE_INFINITY;
            int[] ties = new int[lowlevelheuristics.size()];
            int t = 0;
            int countties = 0;
            for (int i = 0; i < lowlevelheuristics.size(); ++i) {
                //System.out.println("UCB: "+UCB[i]+" max: "+max);
                if (UCB[i] > max) { 	// if ith is better 
                    max = UCB[i]; 	// update max
                    setS(i); 			// select ith
                    countties = 0;	// there is no ties
                    t++;			// update reference value
                    ties[i] = t;	// each value equals t is a tie
                } else if (UCB[i] == max) { // if is a tie
                    ties[i] = t; // insert a new tie
                    countties++; // update count of ties
                }
            }
            if (countties > 0) { // it there is a tie
                setS(getRandomHeuristic(ties, countties, t)); // get a random heuristic with tie value equals t
                //System.out.println("s: "+s+" ties: "+Arrays.toString(ties)+" countties: "+countties+" t: "+t);
            }
        }

        count[s]++; // increment count of selected heuristic
        return lowlevelheuristics.get(getS()); // return selected heuristic
    }

    // evaluate the last given low-level heuristic
    @Override
    public void creditAssignment(double reward) {

        refresment();

        slidingWindow.add(getS(), Math.max(0, reward)); // Update the sliding window

        updateRewards();

        rankRewards();

        creaditAssignmentDecay();

        UCB();
    }

    protected void UCB() {
        double[] variance = new double[lowlevelheuristics.size()];
        int total_usage = slidingWindow.size();
        for (int i = 0; i < lowlevelheuristics.size(); ++i) {
            UCB[i] = reward[i] + this.c * Math.sqrt(2.0 * Math.log(total_usage) / (usage[i]));
            //System.out.println("UCB["+i+"]: "+UCB[i]);
        }
        //System.out.println();
    }

    protected void refresment() {
        for (int i = 0; i < reward.length; ++i) {
            reward[i] = 0.0;
            usage[i] = 0;
        }
    }

    protected void updateRewards() {
        for (int i = 0; i < slidingWindow.size(); ++i) {
            int j = slidingWindow.getHeuristic(i);
            double credit = slidingWindow.getCredit(i);
            if (credit > reward[j]) {
                reward[j] = credit;
            }
            usage[j]++;
            // //System.out.println("reward["+j+"]: "+reward[j]+" usage: "+usage[j]);
        }
        // //System.out.println();
    }

    protected void rankRewards() {
        int i, j;
        double[][] temp;
        double temp_index;
        double temp_value;

        temp = new double[2][lowlevelheuristics.size()];
        for (i = 0; i < lowlevelheuristics.size(); i++) {
            temp[0][i] = reward[i];
            temp[1][i] = i;
        }

        for (i = 0; i < lowlevelheuristics.size() - 1; i++) {
            for (j = i + 1; j < lowlevelheuristics.size(); j++) {
                if (temp[0][i] < temp[0][j]) {
                    temp_value = temp[0][j];
                    temp[0][j] = temp[0][i];
                    temp[0][i] = temp_value;

                    temp_index = temp[1][j];
                    temp[1][j] = temp[1][i];
                    temp[1][i] = temp_index;
                }
            }
        }

        for (i = 0; i < lowlevelheuristics.size(); i++) {
            rank[i] = (int) temp[1][i];
            // //System.out.println("rank["+i+"]: "+rank[i]);
        }
    }

    protected void creaditAssignmentDecay() {
        int i;
        double decayed, decay_sum, decayFactor = 1.0;
        double[] decay_value;

        decay_value = new double[lowlevelheuristics.size()];

        for (i = 0; i < lowlevelheuristics.size(); i++) {
            decayed = Math.pow(decayFactor, i);
            decay_value[rank[i]] = reward[rank[i]] * decayed;
        }

        decay_sum = 0.0;
        for (i = 0; i < lowlevelheuristics.size(); i++) {
            decay_sum += decay_value[i];
        }

        for (i = 0; i < lowlevelheuristics.size(); i++) {
            // //System.out.println("reward["+i+"]: "+reward[i]);
            if (decay_sum == 0) {
                reward[i] = 0.0;
            } else {
                reward[i] = decay_value[i] / decay_sum;
            }
            // //System.out.println("reward["+i+"]: "+reward[i]);
        }
    }
}
