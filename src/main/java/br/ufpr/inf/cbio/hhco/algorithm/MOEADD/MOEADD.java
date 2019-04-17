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
package br.ufpr.inf.cbio.hhco.algorithm.MOEADD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

/**
 * Based on the source distributed by Ke Li
 * (http://www.cs.bham.ac.uk/~likw/code/MOEADD.zip) <k.li at exeter.ac.uk>
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 * @param <S>
 */
public class MOEADD<S extends Solution<?>> implements Algorithm<List<S>> {

    public enum MOEADD_ATTRIBUTES {
        Region, associateDist
    }

    protected int populationSize_;

    protected List<S> population_; // Pareto-based population

    protected double[][] lambda_; 	// Lambda vectors

    protected int T_; 				// neighborhood size
    protected int evaluations_; 		// counter of evaluation times
    protected double delta_; 			// probability that parent solutions are selected from neighborhood
    protected int[][] neighborhood_;  // neighborhood structure

    protected int[][] rankIdx_;			// index matrix for the non-domination levels
    protected int[][] subregionIdx_;		// index matrix for subregion record
    protected double[][] subregionDist_;	// distance matrix for perpendicular distance

    protected String functionType_;

    protected CrossoverOperator<S> crossover_;
    protected MutationOperator<S> mutation_;

    protected int numRanks;

    protected double[] zp_; 	// ideal point for Pareto-based population
    protected double[] nzp_; 	// nadir point for Pareto-based population

    protected int maxEvaluations;

    protected final Problem<S> problem_;

    protected JMetalRandom randomGenerator = JMetalRandom.getInstance();

    protected Object dominanceRankingAttributeIdentifier;

    public MOEADD(MOEADDBuilder<S> builder) {

        functionType_ = "_PBI";

        problem_ = builder.getProblem();
        crossover_ = builder.getCrossover();
        mutation_ = builder.getMutation();

        maxEvaluations = builder.getMaxEvaluations();
        populationSize_ = builder.getPopulationSize();

    }

    @Override
    public void run() {

        evaluations_ = 0;

        T_ = 20;
        delta_ = 0.9;

        population_ = new ArrayList<>(populationSize_);

        neighborhood_ = new int[populationSize_][T_];
        lambda_ = new double[populationSize_][problem_.getNumberOfObjectives()];

        zp_ = new double[problem_.getNumberOfObjectives()]; // ideal point for Pareto-based population
        nzp_ = new double[problem_.getNumberOfObjectives()]; // nadir point for Pareto-based population

        rankIdx_ = new int[populationSize_][populationSize_];
        subregionIdx_ = new int[populationSize_][populationSize_];
        subregionDist_ = new double[populationSize_][populationSize_];

        // STEP 1. Initialization
        initUniformWeight();
        initNeighborhood();
        initPopulation();
        initIdealPoint();
        initNadirPoint();

        // initialize the distance
        for (int i = 0; i < populationSize_; i++) {
            double distance = calculateDistance2(population_.get(i), lambda_[i], zp_, nzp_);
            subregionDist_[i][i] = distance;
        }

        Ranking ranking = (new DominanceRanking()).computeRanking(population_);
        int curRank;
        dominanceRankingAttributeIdentifier = (new DominanceRanking()).getAttributeIdentifier();
        for (int i = 0; i < populationSize_; i++) {
            curRank = (int) population_.get(i).getAttribute(dominanceRankingAttributeIdentifier);
            rankIdx_[curRank][i] = 1;
        }

        // main procedure
        do {
            int[] permutation = new int[populationSize_];
            Utils.randomPermutation(permutation, populationSize_);

            for (int i = 0; i < populationSize_; i++) {
                int cid = permutation[i];

                int type;
                double rnd = randomGenerator.nextDouble();

                // mating selection style
                if (rnd < delta_) {
                    type = 1; // neighborhood
                } else {
                    type = 2; // whole population
                }
                List<S> parents;
                List<S> offSpring;
                parents = matingSelection(cid, type);

                // SBX crossover
                offSpring = crossover_.execute(parents);

                mutation_.execute(offSpring.get(0));
                mutation_.execute(offSpring.get(1));

                problem_.evaluate(offSpring.get(0));
                problem_.evaluate(offSpring.get(1));

                evaluations_ += 2;

                // update ideal points
                updateReference(offSpring.get(0), zp_);
                updateReference(offSpring.get(1), zp_);

                // update nadir points
                updateNadirPoint(offSpring.get(0), nzp_);
                updateNadirPoint(offSpring.get(1), nzp_);

                updateArchive(offSpring.get(0));
                updateArchive(offSpring.get(1));
            } // for			
        } while (evaluations_ < maxEvaluations);

    }

    @Override
    public List<S> getResult() {
        return SolutionListUtils.getNondominatedSolutions(population_);
    }

    @Override
    public String getName() {
        return "MOEADD";
    }

    @Override
    public String getDescription() {
        return "An Evolutionary Many-Objective Optimization Algorithm Based on Dominance and Decomposition";
    }

    /**
     * Initialize the weight vectors for subproblems (We only use the data that
     * are already available)
     */
    public void initUniformWeight() {
        String dataFileName;
        dataFileName = "W" + problem_.getNumberOfObjectives() + "D_"
                + populationSize_ + ".dat";

        lambda_ = new double[populationSize_][problem_.getNumberOfObjectives()];

        try {
            InputStream in = getClass().getResourceAsStream("/WeightVectors/" + dataFileName);
            InputStreamReader isr = new InputStreamReader(in);
            try (BufferedReader br = new BufferedReader(isr)) {
                int i = 0;
                int j;
                String aux = br.readLine();
                while (aux != null) {
                    StringTokenizer st = new StringTokenizer(aux);
                    j = 0;
                    while (st.hasMoreTokens()) {
                        double value = new Double(st.nextToken());
                        lambda_[i][j] = value;
                        j++;
                    }
                    aux = br.readLine();
                    i++;
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new JMetalException("initializeUniformWeight: failed when reading for file: /WeightVectors/" + dataFileName, e);
        }
    }

    /**
     * Initialize the neighborhood structure
     */
    public void initNeighborhood() {
        double[] x = new double[populationSize_];
        int[] idx = new int[populationSize_];

        for (int i = 0; i < populationSize_; i++) {
            // calculate the distances based on weight vectors
            for (int j = 0; j < populationSize_; j++) {
                x[j] = Utils.distVector(lambda_[i], lambda_[j]);
                idx[j] = j;
            } // for

            // find 'niche' nearest neighboring subproblems
            Utils.minFastSort(x, idx, populationSize_, T_);
            System.arraycopy(idx, 0, neighborhood_[i], 0, T_);
        } // for
    } // initNeighborhood

    /**
     * Initialize the population
     */
    public void initPopulation() {
        for (int i = 0; i < populationSize_; i++) {
            S newSolution = problem_.createSolution();
            problem_.evaluate(newSolution);
            evaluations_++;
            population_.add(newSolution);
            subregionIdx_[i][i] = 1;
        }
    } // initPopulation

    /**
     * Initialize the ideal objective vector
     *
     * @throws JMException
     * @throws ClassNotFoundException
     */
    void initIdealPoint() {
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            zp_[i] = 1.0e+30;
        }

        for (int i = 0; i < populationSize_; i++) {
            updateReference(population_.get(i), zp_);
        }
    } // initIdealPoint

    /**
     * Initialize the nadir point
     *
     * @throws JMException
     * @throws ClassNotFoundException
     */
    void initNadirPoint() {
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            nzp_[i] = -1.0e+30;
        }

        for (int i = 0; i < populationSize_; i++) {
            updateNadirPoint(population_.get(i), nzp_);
        }
    } // initNadirPoint

    /**
     * Update the ideal objective vector
     *
     * @param indiv
     */
    void updateReference(Solution indiv, double[] z_) {
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            if (indiv.getObjective(i) < z_[i]) {
                z_[i] = indiv.getObjective(i);
            }
        }
    } // updateReference

    /**
     * Update the nadir point
     *
     * @param indiv
     */
    void updateNadirPoint(Solution indiv, double[] nz_) {
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            if (indiv.getObjective(i) > nz_[i]) {
                nz_[i] = indiv.getObjective(i);
            }
        }
    } // updateNadirPoint

    void RefreshNadirPoint() {
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            nzp_[i] = -1.0e+30;
        }

        for (int i = 0; i < populationSize_; i++) {
            updateNadirPoint(population_.get(i), nzp_);
        }
    } // initNadirPoint

    /**
     * Select two parents for reproduction
     *
     * @param cid
     * @param type
     * @return
     */
    public List<S> matingSelection(int cid, int type) {

        int rnd1, rnd2;

        List<S> parents = new ArrayList<>(2);

        int nLength = neighborhood_[cid].length;

        List<Integer> activeList = new ArrayList<>();
        if (type == 1) {
            for (int i = 0; i < nLength; i++) {
                int idx = neighborhood_[cid][i];
                for (int j = 0; j < populationSize_; j++) {
                    if (subregionIdx_[idx][j] == 1) {
                        activeList.add(idx);
                        break;
                    }
                }
            }
            if (activeList.size() < 2) {
                activeList.clear();
                for (int i = 0; i < populationSize_; i++) {
                    for (int j = 0; j < populationSize_; j++) {
                        if (subregionIdx_[i][j] == 1) {
                            activeList.add(i);
                            break;
                        }
                    }
                }
            }
            int activeSize = activeList.size();
            rnd1 = randomGenerator.nextInt(0, activeSize - 1);
            do {
                rnd2 = randomGenerator.nextInt(0, activeSize - 1);
            } while (rnd1 == rnd2);	// in a very extreme case, this will be a dead loop
            List<Integer> list1 = new ArrayList<>();
            List<Integer> list2 = new ArrayList<>();
            int id1 = activeList.get(rnd1);
            int id2 = activeList.get(rnd2);
            for (int i = 0; i < populationSize_; i++) {
                if (subregionIdx_[id1][i] == 1) {
                    list1.add(i);
                }
                if (subregionIdx_[id2][i] == 1) {
                    list2.add(i);
                }
            }
            int p1 = randomGenerator.nextInt(0, list1.size() - 1);
            int p2 = randomGenerator.nextInt(0, list2.size() - 1);
            parents.add(0, population_.get(list1.get(p1)));
            parents.add(1, population_.get(list2.get(p2)));
        } else {
            for (int i = 0; i < populationSize_; i++) {
                for (int j = 0; j < populationSize_; j++) {
                    if (subregionIdx_[i][j] == 1) {
                        activeList.add(i);
                        break;
                    }
                }
            }
            int activeSize = activeList.size();
            rnd1 = randomGenerator.nextInt(0, activeSize - 1);
            do {
                rnd2 = randomGenerator.nextInt(0, activeSize - 1);
            } while (rnd1 == rnd2);	// in a very extreme case, this will be a dead loop
            List<Integer> list1 = new ArrayList<>();
            List<Integer> list2 = new ArrayList<>();
            int id1 = activeList.get(rnd1);
            int id2 = activeList.get(rnd2);
            for (int i = 0; i < populationSize_; i++) {
                if (subregionIdx_[id1][i] == 1) {
                    list1.add(i);
                }
                if (subregionIdx_[id2][i] == 1) {
                    list2.add(i);
                }
            }
            int p1 = randomGenerator.nextInt(0, list1.size() - 1);
            int p2 = randomGenerator.nextInt(0, list2.size() - 1);
            parents.add(0, population_.get(list1.get(p1)));
            parents.add(1, population_.get(list2.get(p2)));
        }

        return parents;
    } // matingSelection

    /**
     * update the parent population by using the ENLU method, instead of fast
     * non-dominated sorting
     *
     * @param indiv
     */
    public void updateArchive(S indiv) {

        // find the location of 'indiv'
        setLocation(indiv, zp_, nzp_);
        int location = (int) indiv.getAttribute(MOEADD_ATTRIBUTES.Region);

        numRanks = nondominated_sorting_add(indiv);

        if (numRanks == 1) {
            deleteRankOne(indiv, location);
        } else {
            List<S> lastFront = new ArrayList<>(populationSize_);
            int frontSize = countRankOnes(numRanks - 1);
            if (frontSize == 0) {	// the last non-domination level only contains 'indiv'
                frontSize++;
                lastFront.add(indiv);
            } else {
                for (int i = 0; i < populationSize_; i++) {
                    if (rankIdx_[numRanks - 1][i] == 1) {
                        lastFront.add(population_.get(i));
                    }
                }
                if ((int) indiv.getAttribute(dominanceRankingAttributeIdentifier) == (numRanks - 1)) {
                    frontSize++;
                    lastFront.add(indiv);
                }
            }

            if (frontSize == 1 && lastFront.get(0).equals(indiv)) {	// the last non-domination level only has 'indiv'
                int curNC = countOnes(location);
                if (curNC > 0) {	// if the subregion of 'indiv' has other solution, drop 'indiv'
                    nondominated_sorting_delete(indiv);
                } else {	// if the subregion of 'indiv' has no solution, keep 'indiv'
                    deleteCrowdRegion1(indiv, location);
                }
            } else if (frontSize == 1 && !lastFront.get(0).equals(indiv)) { // the last non-domination level only has one solution, but not 'indiv'
                int targetIdx = findPosition(lastFront.get(0));
                int parentLocation = findRegion(targetIdx);
                int curNC = countOnes(parentLocation);
                if (parentLocation == location) {
                    curNC++;
                }

                if (curNC == 1) {	// the subregion only has the solution 'targetIdx', keep solution 'targetIdx'
                    deleteCrowdRegion2(indiv, location);
                } else {	// the subregion contains some other solutions, drop solution 'targetIdx'
                    int indivRank = (int) indiv.getAttribute(dominanceRankingAttributeIdentifier);
                    int targetRank = (int) population_.get(targetIdx).getAttribute(dominanceRankingAttributeIdentifier);
                    rankIdx_[targetRank][targetIdx] = 0;
                    rankIdx_[indivRank][targetIdx] = 1;

                    Solution targetSol = population_.get(targetIdx).copy();

                    population_.set(targetIdx, indiv);
                    subregionIdx_[parentLocation][targetIdx] = 0;
                    subregionIdx_[location][targetIdx] = 1;

                    // update the non-domination level structure
                    nondominated_sorting_delete(targetSol);
                }
            } else {
                double indivFitness = fitnessFunction(indiv, lambda_[location]);

                // find the index of the solution in the last non-domination level, and its corresponding subregion
                int[] idxArray = new int[frontSize];
                int[] regionArray = new int[frontSize];

                for (int i = 0; i < frontSize; i++) {
                    idxArray[i] = findPosition(lastFront.get(i));
                    if (idxArray[i] == -1) {
                        regionArray[i] = location;
                    } else {
                        regionArray[i] = findRegion(idxArray[i]);
                    }
                }

                // find the most crowded subregion, if more than one exist, keep them in 'crowdList'
                List<Integer> crowdList = new ArrayList<>();
                int crowdIdx;
                int nicheCount = countOnes(regionArray[0]);
                if (regionArray[0] == location) {
                    nicheCount++;
                }
                crowdList.add(regionArray[0]);
                for (int i = 1; i < frontSize; i++) {
                    int curSize = countOnes(regionArray[i]);
                    if (regionArray[i] == location) {
                        curSize++;
                    }
                    if (curSize > nicheCount) {
                        crowdList.clear();
                        nicheCount = curSize;
                        crowdList.add(regionArray[i]);
                    } else if (curSize == nicheCount) {
                        crowdList.add(regionArray[i]);
                    }
                }
                // find the index of the most crowded subregion
                if (crowdList.size() == 1) {
                    crowdIdx = crowdList.get(0);
                } else {
                    int listLength = crowdList.size();
                    crowdIdx = crowdList.get(0);
                    double sumFitness = sumFitness(crowdIdx);
                    if (crowdIdx == location) {
                        sumFitness = sumFitness + indivFitness;
                    }
                    for (int i = 1; i < listLength; i++) {
                        int curIdx = crowdList.get(i);
                        double curFitness = sumFitness(curIdx);
                        if (curIdx == location) {
                            curFitness = curFitness + indivFitness;
                        }
                        if (curFitness > sumFitness) {
                            crowdIdx = curIdx;
                            sumFitness = curFitness;
                        }
                    }
                }

                switch (nicheCount) {
                    case 0:
                        System.out.println("Impossible empty subregion!!!");
                        break;
                    case 1:
                        // if the subregion of each solution in the last non-domination level only has one solution, keep them all
                        deleteCrowdRegion2(indiv, location);
                        break;
                    default:
                        // delete the worst solution from the most crowded subregion in the last non-domination level
                        List<Integer> list = new ArrayList<>();
                        for (int i = 0; i < frontSize; i++) {
                            if (regionArray[i] == crowdIdx) {
                                list.add(i);
                            }
                        }
                        if (list.isEmpty()) {
                            System.out.println("Cannot happen!!!");
                        } else {
                            double maxFitness, curFitness;
                            int targetIdx = list.get(0);
                            if (idxArray[targetIdx] == -1) {
                                maxFitness = indivFitness;
                            } else {
                                maxFitness = fitnessFunction(population_.get(idxArray[targetIdx]), lambda_[crowdIdx]);
                            }
                            for (int i = 1; i < list.size(); i++) {
                                int curIdx = list.get(i);
                                if (idxArray[curIdx] == -1) {
                                    curFitness = indivFitness;
                                } else {
                                    curFitness = fitnessFunction(population_.get(idxArray[curIdx]), lambda_[crowdIdx]);
                                }
                                if (curFitness > maxFitness) {
                                    targetIdx = curIdx;
                                    maxFitness = curFitness;
                                }
                            }
                            if (idxArray[targetIdx] == -1) {
                                nondominated_sorting_delete(indiv);
                            } else {
                                int indivRank = (int) indiv.getAttribute(dominanceRankingAttributeIdentifier);
                                int targetRank = (int) population_.get(idxArray[targetIdx]).getAttribute(dominanceRankingAttributeIdentifier);
                                rankIdx_[targetRank][idxArray[targetIdx]] = 0;
                                rankIdx_[indivRank][idxArray[targetIdx]] = 1;

                                Solution targetSol = population_.get(idxArray[targetIdx]).copy();

                                population_.set(idxArray[targetIdx], indiv);
                                subregionIdx_[crowdIdx][idxArray[targetIdx]] = 0;
                                subregionIdx_[location][idxArray[targetIdx]] = 1;

                                // update the non-domination level structure
                                nondominated_sorting_delete(targetSol);
                            }
                        }
                        break;
                }
            }
        }

    }

    /**
     * update the non-domination level structure after deleting a solution
     *
     * @param indiv
     */
    public void nondominated_sorting_delete(Solution indiv) {

        // find the non-domination level of 'indiv'
        int indivRank = (int) indiv.getAttribute(dominanceRankingAttributeIdentifier);

        List<Integer> curLevel = new ArrayList<>();	// used to keep the solutions in the current non-domination level
        List<Integer> dominateList = new ArrayList<>();	// used to keep the solutions need to be moved

        for (int i = 0; i < populationSize_; i++) {
            if (rankIdx_[indivRank][i] == 1) {
                curLevel.add(i);
            }
        }

        int flag;
        // find the solutions belonging to the 'indivRank+1'th level and are dominated by 'indiv'
        int investigateRank = indivRank + 1;
        if (investigateRank < numRanks) {
            for (int i = 0; i < populationSize_; i++) {
                if (rankIdx_[investigateRank][i] == 1) {
                    flag = 0;
                    if (checkDominance(indiv, population_.get(i)) == 1) {
                        for (int j = 0; j < curLevel.size(); j++) {
                            if (checkDominance(population_.get(i), population_.get(curLevel.get(j))) == -1) {
                                flag = 1;
                                break;
                            }
                        }
                        if (flag == 0) {	// the ith solution can move to the prior level
                            dominateList.add(i);
                            rankIdx_[investigateRank][i] = 0;
                            rankIdx_[investigateRank - 1][i] = 1;
                            population_.get(i).setAttribute(dominanceRankingAttributeIdentifier, investigateRank - 1);
                        }
                    }
                }
            }
        }

        int curIdx;
        int curListSize = dominateList.size();
        while (curListSize != 0) {
            curLevel.clear();
            for (int i = 0; i < populationSize_; i++) {
                if (rankIdx_[investigateRank][i] == 1) {
                    curLevel.add(i);
                }
            }
            investigateRank = investigateRank + 1;

            if (investigateRank < numRanks) {
                for (int i = 0; i < curListSize; i++) {
                    curIdx = dominateList.get(i);
                    for (int j = 0; j < populationSize_; j++) {
                        if (j == populationSize_) {
                            System.out.println("Fuck me!!!");
                        }
                        if (rankIdx_[investigateRank][j] == 1) {
                            flag = 0;
                            if (checkDominance(population_.get(curIdx), population_.get(j)) == 1) {
                                for (int k = 0; k < curLevel.size(); k++) {
                                    if (checkDominance(population_.get(j), population_.get(curLevel.get(k))) == -1) {
                                        flag = 1;
                                        break;
                                    }
                                }
                                if (flag == 0) {
                                    dominateList.add(j);
                                    rankIdx_[investigateRank][j] = 0;
                                    rankIdx_[investigateRank - 1][j] = 1;
                                    population_.get(j).setAttribute(dominanceRankingAttributeIdentifier, investigateRank - 1);
                                }
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < curListSize; i++) {
                dominateList.remove(0);
            }

            curListSize = dominateList.size();
        }

    }

    /**
     * update the non-domination level when adding a solution
     *
     * @param indiv
     * @return
     */
    public int nondominated_sorting_add(Solution indiv) {

        int flag = 0;
        int flag1, flag2, flag3;

        // count the number of non-domination levels
        int num_ranks = 0;
        List<Integer> frontSize = new ArrayList<>();
        for (int i = 0; i < populationSize_; i++) {
            int rankCount = countRankOnes(i);
            if (rankCount != 0) {
                frontSize.add(rankCount);
                num_ranks++;
            } else {
                break;
            }
        }

        List<Integer> dominateList = new ArrayList<>();	// used to keep the solutions dominated by 'indiv'
        int level = 0;
        for (int i = 0; i < num_ranks; i++) {
            level = i;
            if (flag == 1) {	// 'indiv' is non-dominated with all solutions in the ith non-domination level, then 'indiv' belongs to the ith level
                indiv.setAttribute(dominanceRankingAttributeIdentifier, i - 1);
                return num_ranks;
            } else if (flag == 2) {	// 'indiv' dominates some solutions in the ith level, but is non-dominated with some others, then 'indiv' belongs to the ith level, and move the dominated solutions to the next level
                indiv.setAttribute(dominanceRankingAttributeIdentifier, i - 1);

                int prevRank = i - 1;

                // process the solutions belong to 'prevRank'th level and are dominated by 'indiv' ==> move them to 'prevRank+1'th level and find the solutions dominated by them
                int curIdx;
                int newRank = prevRank + 1;
                int curListSize = dominateList.size();
                for (int j = 0; j < curListSize; j++) {
                    curIdx = dominateList.get(j);
                    rankIdx_[prevRank][curIdx] = 0;
                    rankIdx_[newRank][curIdx] = 1;
                    population_.get(curIdx).setAttribute(dominanceRankingAttributeIdentifier, newRank);
                }
                for (int j = 0; j < populationSize_; j++) {
                    if (rankIdx_[newRank][j] == 1) {
                        for (int k = 0; k < curListSize; k++) {
                            curIdx = dominateList.get(k);
                            if (checkDominance(population_.get(curIdx), population_.get(j)) == 1) {
                                dominateList.add(j);
                                break;
                            }

                        }
                    }
                }
                for (int j = 0; j < curListSize; j++) {
                    dominateList.remove(0);
                }

                // if there are still some other solutions moved to the next level, check their domination situation in their new level
                prevRank = newRank;
                newRank = newRank + 1;
                curListSize = dominateList.size();
                if (curListSize == 0) {
                    return num_ranks;
                } else {
                    int allFlag = 0;
                    do {
                        for (int j = 0; j < curListSize; j++) {
                            curIdx = dominateList.get(j);
                            rankIdx_[prevRank][curIdx] = 0;
                            rankIdx_[newRank][curIdx] = 1;
                            population_.get(curIdx).setAttribute(dominanceRankingAttributeIdentifier, newRank);
                        }
                        for (int j = 0; j < populationSize_; j++) {
                            if (rankIdx_[newRank][j] == 1) {
                                for (int k = 0; k < curListSize; k++) {
                                    curIdx = dominateList.get(k);
                                    if (checkDominance(population_.get(curIdx), population_.get(j)) == 1) {
                                        dominateList.add(j);
                                        break;
                                    }
                                }
                            }
                        }
                        for (int j = 0; j < curListSize; j++) {
                            dominateList.remove(0);
                        }

                        curListSize = dominateList.size();
                        if (curListSize != 0) {
                            prevRank = newRank;
                            newRank = newRank + 1;
                            if (curListSize == frontSize.get(prevRank)) {	// if all solutions in the 'prevRank'th level are dominated by the newly added solution, move them all to the next level
                                allFlag = 1;
                                break;
                            }
                        }
                    } while (curListSize != 0);

                    if (allFlag == 1) {	// move the solutions after the 'prevRank'th level to their next levels
                        int remainSize = num_ranks - prevRank;
                        int[][] tempRecord = new int[remainSize][populationSize_];

                        int tempIdx = 0;
                        for (int j = 0; j < dominateList.size(); j++) {
                            tempRecord[0][tempIdx] = dominateList.get(j);
                            tempIdx++;
                        }

                        int k = 1;
                        int curRank = prevRank + 1;
                        while (curRank < num_ranks) {
                            tempIdx = 0;
                            for (int j = 0; j < populationSize_; j++) {
                                if (rankIdx_[curRank][j] == 1) {
                                    tempRecord[k][tempIdx] = j;
                                    tempIdx++;
                                }
                            }
                            curRank++;
                            k++;
                        }

                        k = 0;
                        curRank = prevRank;
                        while (curRank < num_ranks) {
                            int level_size = frontSize.get(curRank);

                            int tempRank;
                            for (int j = 0; j < level_size; j++) {
                                curIdx = tempRecord[k][j];
                                tempRank = (int) population_.get(curIdx).getAttribute(dominanceRankingAttributeIdentifier);
                                newRank = tempRank + 1;
                                population_.get(curIdx).setAttribute(dominanceRankingAttributeIdentifier, newRank);

                                rankIdx_[tempRank][curIdx] = 0;
                                rankIdx_[newRank][curIdx] = 1;
                            }
                            curRank++;
                            k++;
                        }
                        num_ranks++;
                    }

                    if (newRank == num_ranks) {
                        num_ranks++;
                    }

                    return num_ranks;
                }
            } else if (flag == 3 || flag == 0) {	// if 'indiv' is dominated by some solutions in the ith level, skip it, and term to the next level
                flag1 = flag2 = flag3 = 0;
                for (int j = 0; j < populationSize_; j++) {
                    if (rankIdx_[i][j] == 1) {
                        switch (checkDominance(indiv, population_.get(j))) {
                            case 1: {
                                flag1 = 1;
                                dominateList.add(j);
                                break;
                            }
                            case 0: {
                                flag2 = 1;
                                break;
                            }
                            case -1: {
                                flag3 = 1;
                                break;
                            }
                        }

                        if (flag3 == 1) {
                            flag = 3;
                            break;
                        } else if (flag1 == 0 && flag2 == 1) {
                            flag = 1;
                        } else if (flag1 == 1 && flag2 == 1) {
                            flag = 2;
                        } else if (flag1 == 1 && flag2 == 0) {
                            flag = 4;
                        } else {
                            continue;
                        }
                    }
                }

            } else {	// (flag == 4) if 'indiv' dominates all solutions in the ith level, solutions in the current level and beyond move their current next levels
                indiv.setAttribute(dominanceRankingAttributeIdentifier, i - 1);
                i = i - 1;
                int remainSize = num_ranks - i;
                int[][] tempRecord = new int[remainSize][populationSize_];

                int k = 0;
                while (i < num_ranks) {
                    int tempIdx = 0;
                    for (int j = 0; j < populationSize_; j++) {
                        if (rankIdx_[i][j] == 1) {
                            tempRecord[k][tempIdx] = j;
                            tempIdx++;
                        }
                    }
                    i++;
                    k++;
                }

                k = 0;
                i = (int) indiv.getAttribute(dominanceRankingAttributeIdentifier);
                while (i < num_ranks) {
                    int level_size = frontSize.get(i);

                    int curIdx;
                    int curRank, newRank;
                    for (int j = 0; j < level_size; j++) {
                        curIdx = tempRecord[k][j];
                        curRank = (int) population_.get(curIdx).getAttribute(dominanceRankingAttributeIdentifier);
                        newRank = curRank + 1;
                        population_.get(curIdx).setAttribute(dominanceRankingAttributeIdentifier, newRank);

                        rankIdx_[curRank][curIdx] = 0;
                        rankIdx_[newRank][curIdx] = 1;
                    }
                    i++;
                    k++;
                }
                num_ranks++;

                return num_ranks;
            }
        }
        // if flag is still 3 after the for-loop, it means that 'indiv' is in the current last level
        switch (flag) {
            case 1:
                indiv.setAttribute(dominanceRankingAttributeIdentifier, level);
                break;
            case 2:
                indiv.setAttribute(dominanceRankingAttributeIdentifier, level);
                int curIdx;
                int tempSize = dominateList.size();
                for (int i = 0; i < tempSize; i++) {
                    curIdx = dominateList.get(i);
                    population_.get(curIdx).setAttribute(dominanceRankingAttributeIdentifier, level + 1);

                    rankIdx_[level][curIdx] = 0;
                    rankIdx_[level + 1][curIdx] = 1;
                }
                num_ranks++;
                break;
            case 3:
                indiv.setAttribute(dominanceRankingAttributeIdentifier, level + 1);
                num_ranks++;
                break;
            default:
                indiv.setAttribute(dominanceRankingAttributeIdentifier, level);
                for (int i = 0; i < populationSize_; i++) {
                    if (rankIdx_[level][i] == 1) {
                        population_.get(i).setAttribute(dominanceRankingAttributeIdentifier, level + 1);

                        rankIdx_[level][i] = 0;
                        rankIdx_[level + 1][i] = 1;
                    }
                }
                num_ranks++;
                break;
        }

        return num_ranks;
    }

    /**
     * Delete a solution from the most crowded subregion (this function only
     * happens when: it should delete 'indiv' based on traditional method.
     * However, the subregion of 'indiv' only has one solution, so it should be
     * kept)
     *
     * @param indiv
     * @param location
     */
    public void deleteCrowdRegion1(S indiv, int location) {

        // find the most crowded subregion, if more than one such subregion exists, keep them in the crowdList
        List<Integer> crowdList = new ArrayList<>();
        int crowdIdx;
        int nicheCount = countOnes(0);
        crowdList.add(0);
        for (int i = 1; i < populationSize_; i++) {
            int curSize = countOnes(i);
            if (curSize > nicheCount) {
                crowdList.clear();
                nicheCount = curSize;
                crowdList.add(i);
            } else if (curSize == nicheCount) {
                crowdList.add(i);
            }
        }
        // find the index of the crowded subregion
        if (crowdList.size() == 1) {
            crowdIdx = crowdList.get(0);
        } else {
            int listLength = crowdList.size();
            crowdIdx = crowdList.get(0);
            double sumFitness = sumFitness(crowdIdx);
            for (int i = 1; i < listLength; i++) {
                int curIdx = crowdList.get(i);
                double curFitness = sumFitness(curIdx);
                if (curFitness > sumFitness) {
                    crowdIdx = curIdx;
                    sumFitness = curFitness;
                }
            }
        }

        // find the solution indices within the 'crowdIdx' subregion
        List<Integer> indList = new ArrayList<>();
        for (int i = 0; i < populationSize_; i++) {
            if (subregionIdx_[crowdIdx][i] == 1) {
                indList.add(i);
            }
        }

        // find the solution with the largest rank
        List<Integer> maxRankList = new ArrayList<>();
        int maxRank = (int) population_.get(indList.get(0)).getAttribute(dominanceRankingAttributeIdentifier);
        maxRankList.add(indList.get(0));
        for (int i = 1; i < indList.size(); i++) {
            int curRank = (int) population_.get(indList.get(i)).getAttribute(dominanceRankingAttributeIdentifier);
            if (curRank > maxRank) {
                maxRankList.clear();
                maxRank = curRank;
                maxRankList.add(indList.get(i));
            } else if (curRank == maxRank) {
                maxRankList.add(indList.get(i));
            }
        }

        // find the solution with the largest rank and worst fitness
        int rankSize = maxRankList.size();
        int targetIdx = maxRankList.get(0);
        double maxFitness = fitnessFunction(population_.get(targetIdx), lambda_[crowdIdx]);
        for (int i = 1; i < rankSize; i++) {
            int curIdx = maxRankList.get(i);
            double curFitness = fitnessFunction(population_.get(curIdx), lambda_[crowdIdx]);
            if (curFitness > maxFitness) {
                targetIdx = curIdx;
                maxFitness = curFitness;
            }
        }

        int indivRank = (int) indiv.getAttribute(dominanceRankingAttributeIdentifier);
        int targetRank = (int) population_.get(targetIdx).getAttribute(dominanceRankingAttributeIdentifier);
        rankIdx_[targetRank][targetIdx] = 0;
        rankIdx_[indivRank][targetIdx] = 1;

        Solution targetSol = population_.get(targetIdx).copy();

        population_.set(targetIdx, indiv);
        subregionIdx_[crowdIdx][targetIdx] = 0;
        subregionIdx_[location][targetIdx] = 1;

        // update the non-domination level structure
        nondominated_sorting_delete(targetSol);

    }

    /**
     * delete a solution from the most crowded subregion (this function happens
     * when: it should delete the solution in the 'parentLocation' subregion,
     * but since this subregion only has one solution, it should be kept)
     *
     * @param indiv
     * @param location
     */
    public void deleteCrowdRegion2(S indiv, int location) {

        double indivFitness = fitnessFunction(indiv, lambda_[location]);

        // find the most crowded subregion, if there are more than one, keep them in crowdList
        List<Integer> crowdList = new ArrayList<>();
        int crowdIdx;
        int nicheCount = countOnes(0);
        if (location == 0) {
            nicheCount++;
        }
        crowdList.add(0);
        for (int i = 1; i < populationSize_; i++) {
            int curSize = countOnes(i);
            if (location == i) {
                curSize++;
            }
            if (curSize > nicheCount) {
                crowdList.clear();
                nicheCount = curSize;
                crowdList.add(i);
            } else if (curSize == nicheCount) {
                crowdList.add(i);
            }
        }
        // determine the index of the crowded subregion
        if (crowdList.size() == 1) {
            crowdIdx = crowdList.get(0);
        } else {
            int listLength = crowdList.size();
            crowdIdx = crowdList.get(0);
            double sumFitness = sumFitness(crowdIdx);
            if (crowdIdx == location) {
                sumFitness = sumFitness + indivFitness;
            }
            for (int i = 1; i < listLength; i++) {
                int curIdx = crowdList.get(i);
                double curFitness = sumFitness(curIdx);
                if (curIdx == location) {
                    curFitness = curFitness + indivFitness;
                }
                if (curFitness > sumFitness) {
                    crowdIdx = curIdx;
                    sumFitness = curFitness;
                }
            }
        }

        // find the solution indices within the 'crowdIdx' subregion
        List<Integer> indList = new ArrayList<>();
        for (int i = 0; i < populationSize_; i++) {
            if (subregionIdx_[crowdIdx][i] == 1) {
                indList.add(i);
            }
        }
        if (crowdIdx == location) {
            int temp = -1;
            indList.add(temp);
        }

        // find the solution with the largest rank
        List<Integer> maxRankList = new ArrayList<>();
        int maxRank = (int) population_.get(indList.get(0)).getAttribute(dominanceRankingAttributeIdentifier);
        maxRankList.add(indList.get(0));
        for (int i = 1; i < indList.size(); i++) {
            int curRank;
            if (indList.get(i) == -1) {
                curRank = (int) indiv.getAttribute(dominanceRankingAttributeIdentifier);
            } else {
                curRank = (int) population_.get(indList.get(i)).getAttribute(dominanceRankingAttributeIdentifier);
            }

            if (curRank > maxRank) {
                maxRankList.clear();
                maxRank = curRank;
                maxRankList.add(indList.get(i));
            } else if (curRank == maxRank) {
                maxRankList.add(indList.get(i));
            }
        }

        double maxFitness;
        int rankSize = maxRankList.size();
        int targetIdx = maxRankList.get(0);
        if (targetIdx == -1) {
            maxFitness = indivFitness;
        } else {
            maxFitness = fitnessFunction(population_.get(targetIdx), lambda_[crowdIdx]);
        }
        for (int i = 1; i < rankSize; i++) {
            double curFitness;
            int curIdx = maxRankList.get(i);
            if (curIdx == -1) {
                curFitness = indivFitness;
            } else {
                curFitness = fitnessFunction(population_.get(curIdx), lambda_[crowdIdx]);
            }

            if (curFitness > maxFitness) {
                targetIdx = curIdx;
                maxFitness = curFitness;
            }
        }

        if (targetIdx == -1) {

            nondominated_sorting_delete(indiv);

            return;
        } else {
            int indivRank = (int) indiv.getAttribute(dominanceRankingAttributeIdentifier);
            int targetRank = (int) population_.get(targetIdx).getAttribute(dominanceRankingAttributeIdentifier);
            rankIdx_[targetRank][targetIdx] = 0;
            rankIdx_[indivRank][targetIdx] = 1;

            Solution targetSol = population_.get(targetIdx).copy();

            population_.set(targetIdx, indiv);
            subregionIdx_[crowdIdx][targetIdx] = 0;
            subregionIdx_[location][targetIdx] = 1;

            // update the non-domination level structure of the population
            nondominated_sorting_delete(targetSol);
        }

    }

    /**
     * if there is only one non-domination level (i.e., all solutions are
     * non-dominated with each other), we should delete a solution from the most
     * crowded subregion
     *
     * @param indiv
     * @param location
     */
    public void deleteRankOne(S indiv, int location) {

        double indivFitness = fitnessFunction(indiv, lambda_[location]);

        // find the most crowded subregion, if there are more than one, keep them in crowdList
        List<Integer> crowdList = new ArrayList<>();
        int crowdIdx;
        int nicheCount = countOnes(0);
        if (location == 0) {
            nicheCount++;
        }
        crowdList.add(0);
        for (int i = 1; i < populationSize_; i++) {
            int curSize = countOnes(i);
            if (location == i) {
                curSize++;
            }
            if (curSize > nicheCount) {
                crowdList.clear();
                nicheCount = curSize;
                crowdList.add(i);
            } else if (curSize == nicheCount) {
                crowdList.add(i);
            }
        }
        // determine the index of the crowded subregion
        if (crowdList.size() == 1) {
            crowdIdx = crowdList.get(0);
        } else {
            int listLength = crowdList.size();
            crowdIdx = crowdList.get(0);
            double sumFitness = sumFitness(crowdIdx);
            if (crowdIdx == location) {
                sumFitness = sumFitness + indivFitness;
            }
            for (int i = 1; i < listLength; i++) {
                int curIdx = crowdList.get(i);
                double curFitness = sumFitness(curIdx);
                if (curIdx == location) {
                    curFitness = curFitness + indivFitness;
                }
                if (curFitness > sumFitness) {
                    crowdIdx = curIdx;
                    sumFitness = curFitness;
                }
            }
        }

        switch (nicheCount) {
            case 0:
                System.out.println("Empty subregion!!!");
                break;
            case 1:
                // if every subregion only contains one solution, delete the worst from indiv's subregion
                int targetIdx;
                for (targetIdx = 0; targetIdx < populationSize_; targetIdx++) {
                    if (subregionIdx_[location][targetIdx] == 1) {
                        break;
                    }
                }
                double prev_func = fitnessFunction(population_.get(targetIdx), lambda_[location]);
                if (indivFitness < prev_func) {
                    population_.set(targetIdx, indiv);
                }
                break;
            default:
                if (location == crowdIdx) {	// if indiv's subregion is the most crowded one
                    deleteCrowdIndiv_same(location, nicheCount, indivFitness, indiv);
                } else {
                    int curNC = countOnes(location);
                    int crowdNC = countOnes(crowdIdx);

                    if (crowdNC > (curNC + 1)) {	// if the crowdIdx subregion is more crowded, delete one from this subregion
                        deleteCrowdIndiv_diff(crowdIdx, location, crowdNC, indiv);
                    } else if (crowdNC < (curNC + 1)) { // crowdNC == curNC, delete one from indiv's subregion
                        deleteCrowdIndiv_same(location, curNC, indivFitness, indiv);
                    } else { // crowdNC == (curNC + 1)
                        if (curNC == 0) {
                            deleteCrowdIndiv_diff(crowdIdx, location, crowdNC, indiv);
                        } else {
                            double rnd = randomGenerator.nextDouble();
                            if (rnd < 0.5) {
                                deleteCrowdIndiv_diff(crowdIdx, location, crowdNC, indiv);
                            } else {
                                deleteCrowdIndiv_same(location, curNC, indivFitness, indiv);
                            }
                        }
                    }
                }
                break;
        }

    }

    /**
     * calculate the sum of fitnesses of solutions in the location subregion
     *
     * @param location
     * @return
     */
    public double sumFitness(int location) {

        double sum = 0;
        for (int i = 0; i < populationSize_; i++) {
            if (subregionIdx_[location][i] == 1) {
                sum = sum + fitnessFunction(population_.get(i), lambda_[location]);
            }
        }

        return sum;
    }

    /**
     * delete one solution from the most crowded subregion, which is indiv's
     * subregion. Compare indiv's fitness value and the worst one in this
     * subregion
     *
     * @param crowdIdx
     * @param nicheCount
     * @param indivFitness
     * @param indiv
     */
    public void deleteCrowdIndiv_same(int crowdIdx, int nicheCount, double indivFitness, S indiv) {

        // find the solution indices within this crowdIdx subregion
        List<Integer> indList = new ArrayList<>();
        for (int i = 0; i < populationSize_; i++) {
            if (subregionIdx_[crowdIdx][i] == 1) {
                indList.add(i);
            }
        }

        // find the solution with the worst fitness value
        int listSize = indList.size();
        int worstIdx = indList.get(0);
        double maxFitness = fitnessFunction(population_.get(worstIdx), lambda_[crowdIdx]);
        for (int i = 1; i < listSize; i++) {
            int curIdx = indList.get(i);
            double curFitness = fitnessFunction(population_.get(curIdx), lambda_[crowdIdx]);
            if (curFitness > maxFitness) {
                worstIdx = curIdx;
                maxFitness = curFitness;
            }
        }

        // if indiv has a better fitness, use indiv to replace the worst one
        if (indivFitness < maxFitness) {
            population_.set(worstIdx, indiv);
        }
    }

    /**
     * delete one solution from the most crowded subregion, which is different
     * from indiv's subregion. just use indiv to replace the worst solution in
     * that subregion
     *
     * @param crowdIdx
     * @param nicheCount
     * @param indiv
     */
    public void deleteCrowdIndiv_diff(int crowdIdx, int curLocation, int nicheCount, S indiv) {

        // find the solution indices within this crowdIdx subregion
        List<Integer> indList = new ArrayList<>();
        for (int i = 0; i < populationSize_; i++) {
            if (subregionIdx_[crowdIdx][i] == 1) {
                indList.add(i);
            }
        }

        // find the solution with the worst fitness value
        int worstIdx = indList.get(0);
        double maxFitness = fitnessFunction(population_.get(worstIdx), lambda_[crowdIdx]);
        for (int i = 1; i < nicheCount; i++) {
            int curIdx = indList.get(i);
            double curFitness = fitnessFunction(population_.get(curIdx), lambda_[crowdIdx]);
            if (curFitness > maxFitness) {
                worstIdx = curIdx;
                maxFitness = curFitness;
            }
        }

        // use indiv to replace the worst one
        population_.set(worstIdx, indiv);
        subregionIdx_[crowdIdx][worstIdx] = 0;
        subregionIdx_[curLocation][worstIdx] = 1;

    }

    /**
     * Count the number of 1s in the 'location'th subregion
     *
     * @param location
     * @return
     */
    public int countOnes(int location) {

        int count = 0;
        for (int i = 0; i < populationSize_; i++) {
            if (subregionIdx_[location][i] == 1) {
                count++;
            }
        }

        return count;
    }

    /**
     * count the number of 1s in a row of rank matrix
     *
     * @param location
     * @return
     */
    public int countRankOnes(int location) {

        int count = 0;
        for (int i = 0; i < populationSize_; i++) {
            if (rankIdx_[location][i] == 1) {
                count++;
            }
        }

        return count;
    }

    /**
     * find the index of the solution 'indiv' in the population_
     *
     * @param indiv
     * @return
     */
    public int findPosition(Solution indiv) {

        for (int i = 0; i < populationSize_; i++) {
            if (indiv.equals(population_.get(i))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * find the subregion of the 'idx'th solution in the population_
     *
     * @param idx
     * @return
     */
    public int findRegion(int idx) {

        for (int i = 0; i < populationSize_; i++) {
            if (subregionIdx_[i][idx] == 1) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Set the location of a solution based on the orthogonal distance
     *
     * @param indiv
     */
    public void setLocation(Solution indiv, double[] z_, double[] nz_) {

        int minIdx;
        double distance, minDist;

        minIdx = 0;
        distance = calculateDistance2(indiv, lambda_[0], z_, nz_);
        minDist = distance;
        for (int i = 1; i < populationSize_; i++) {
            distance = calculateDistance2(indiv, lambda_[i], z_, nz_);
            if (distance < minDist) {
                minIdx = i;
                minDist = distance;
            }
        }

        indiv.setAttribute(MOEADD_ATTRIBUTES.Region, minIdx);
        indiv.setAttribute(MOEADD_ATTRIBUTES.associateDist, minDist);

    }

    /**
     * check the dominance relationship between a and b: 1 -> a dominates b, -1
     * -> b dominates a 0 -> non-dominated with each other
     *
     * @param a
     * @param b
     * @return
     */
    public int checkDominance(Solution a, Solution b) {

        int flag1 = 0;
        int flag2 = 0;

        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            if (a.getObjective(i) < b.getObjective(i)) {
                flag1 = 1;
            } else {
                if (a.getObjective(i) > b.getObjective(i)) {
                    flag2 = 1;
                }
            }
        }
        if (flag1 == 1 && flag2 == 0) {
            return 1;
        } else {
            if (flag1 == 0 && flag2 == 1) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Calculate the perpendicular distance between the solution and reference
     * line
     *
     * @param individual
     * @param lambda
     * @return
     */
    public double calculateDistance(Solution individual, double[] lambda,
            double[] z_, double[] nz_) {

        double scale;
        double distance;

        double[] vecInd = new double[problem_.getNumberOfObjectives()];
        double[] vecProj = new double[problem_.getNumberOfObjectives()];

        // normalize the weight vector (line segment)
        double nd = norm_vector(lambda);
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            lambda[i] = lambda[i] / nd;
        }

        // vecInd has been normalized to the range [0,1]
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            vecInd[i] = (individual.getObjective(i) - z_[i]) / (nz_[i] - z_[i]);
        }

        scale = innerproduct(vecInd, lambda);
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            vecProj[i] = vecInd[i] - scale * lambda[i];
        }

        distance = norm_vector(vecProj);

        return distance;
    }

    public double calculateDistance2(Solution indiv, double[] lambda,
            double[] z_, double[] nz_) {

        // normalize the weight vector (line segment)
        double nd = norm_vector(lambda);
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            lambda[i] = lambda[i] / nd;
        }

        double[] realA = new double[problem_.getNumberOfObjectives()];
        double[] realB = new double[problem_.getNumberOfObjectives()];

        // difference between current point and reference point
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            realA[i] = (indiv.getObjective(i) - z_[i]);
        }

        // distance along the line segment
        double d1 = Math.abs(innerproduct(realA, lambda));

        // distance to the line segment
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            realB[i] = (indiv.getObjective(i) - (z_[i] + d1 * lambda[i]));
        }

        double distance = norm_vector(realB);

        return distance;
    }

    /**
     * Calculate the dot product of two vectors
     *
     * @param vec1
     * @param vec2
     * @return
     */
    public double innerproduct(double[] vec1, double[] vec2) {
        double sum = 0;

        for (int i = 0; i < vec1.length; i++) {
            sum += vec1[i] * vec2[i];
        }

        return sum;
    }

    /**
     * Calculate the norm of the vector
     *
     * @param z
     * @return
     */
    public double norm_vector(double[] z) {
        double sum = 0;

        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            sum += z[i] * z[i];
        }

        return Math.sqrt(sum);
    }

    public int countTest() {

        int sum = 0;
        for (int i = 0; i < populationSize_; i++) {
            for (int j = 0; j < populationSize_; j++) {
                if (subregionIdx_[i][j] == 1) {
                    sum++;
                }
            }
        }

        return sum;
    }

    double fitnessFunction(Solution indiv, double[] lambda) {
        double fitness = 0;

        switch (functionType_) {
            case "_TCHE1": {
                double maxFun = -1.0e+30;
                for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
                    double diff = Math.abs(indiv.getObjective(n) - zp_[n]);

                    double feval;
                    if (lambda[n] == 0) {
                        feval = 0.0001 * diff;
                    } else {
                        feval = diff * lambda[n];
                    }
                    if (feval > maxFun) {
                        maxFun = feval;
                    }
                } // for
                fitness = maxFun;
                break;
            }
            case "_TCHE2": {
                double maxFun = -1.0e+30;
                for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
                    double diff = Math.abs(indiv.getObjective(i) - zp_[i]);

                    double feval;
                    if (lambda[i] == 0) {
                        feval = diff / 0.000001;
                    } else {
                        feval = diff / lambda[i];
                    }
                    if (feval > maxFun) {
                        maxFun = feval;
                    }
                } // for
                fitness = maxFun;
                break;
            }
            case "_PBI":
                double theta; // penalty parameter
                theta = 5.0;
                // normalize the weight vector (line segment)
                double nd = norm_vector(lambda);
                for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
                    lambda[i] = lambda[i] / nd;
                }
                double[] realA = new double[problem_.getNumberOfObjectives()];
                double[] realB = new double[problem_.getNumberOfObjectives()];
                // difference between current point and reference point
                for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
                    realA[n] = (indiv.getObjective(n) - zp_[n]);
                }   // distance along the line segment
                double d1 = Math.abs(innerproduct(realA, lambda));
                // distance to the line segment
                for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
                    realB[n] = (indiv.getObjective(n) - (zp_[n] + d1 * lambda[n]));
                }
                double d2 = norm_vector(realB);
                fitness = d1 + theta * d2;
                break;
            default:
                System.out.println("MOEAD.fitnessFunction: unknown type "
                        + functionType_);
                System.exit(-1);
        }
        return fitness;
    } // fitnessEvaluation

} // MOEAD
