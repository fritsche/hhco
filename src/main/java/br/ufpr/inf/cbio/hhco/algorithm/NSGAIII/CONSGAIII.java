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
package br.ufpr.inf.cbio.hhco.algorithm.NSGAIII;

import br.ufpr.inf.cbio.hhco.hyperheuristic.CooperativeAlgorithm;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 * @param <S>
 */
public class CONSGAIII<S extends Solution<?>> extends NSGAIII<S> implements CooperativeAlgorithm<S> {

    public CONSGAIII(NSGAIIIBuilder builder) {
        super(builder);
        lambda_ = null;
    }

    private void environmentalSelection() {
        union_ = new ArrayList<>();
        union_.addAll(population_);
        union_.addAll(offspringPopulation_);

        // Ranking the union
        Ranking ranking = (new DominanceRanking()).computeRanking(union_);

        int remain = populationSize_;
        int index = 0;
        List<S> front;
        population_.clear();

        // Obtain the next front
        front = ranking.getSubfront(index);

        while ((remain > 0) && (remain >= front.size())) {

            for (int k = 0; k < front.size(); k++) {
                population_.add(front.get(k));
            } // for

            // Decrement remain
            remain = remain - front.size();

            // Obtain the next front
            index++;
            if (remain > 0) {
                front = ranking.getSubfront(index);
            } // if
        }

        if (remain > 0) { // front contains individuals to insert

            new Niching(population_, front, lambda_, remain, normalize_)
                    .execute();
        }
    }

    @Override
    public void init(int popSize) {
        this.populationSize_ = popSize;
        this.populationSize_ = popSize + (popSize % 2);
        List<S> initial = new ArrayList<>(this.populationSize_);
        for (int i = 0; i < this.populationSize_; i++) {
            S newSolution = problem_.createSolution();
            problem_.evaluate(newSolution);
            initial.add(newSolution);
        }
        init(initial, popSize);
    }

    @Override
    public void init(List<S> initialPopulation, int populationSize) {
        this.populationSize_ = populationSize;
        if (lambda_ == null) {
            initializeUniformWeight();
        }
        populationSize_ += (populationSize_ % 2);
        population_ = new ArrayList<>(populationSize_);
        population_.addAll(initialPopulation);
        // fit populationSize if initialPopulation is larger
        while (population_.size() > populationSize_) {
            int index = JMetalRandom.getInstance().nextInt(0, population_.size() - 1);
            population_.remove(index);
        }
        // fit populationSize if initialPopulation is smaller
        while (population_.size() < populationSize_) {
            int index = JMetalRandom.getInstance().nextInt(0, population_.size() - 1);
            population_.add((S) population_.get(index).copy());
        }
    }

    @Override
    public void doIteration() {
        offspringPopulation_ = new ArrayList<>(populationSize_);
        for (int i = 0; i < (populationSize_ / 2); i++) {

            List<S> parents = new ArrayList<>();
            parents.add(selection_.execute(population_));
            parents.add(selection_.execute(population_));

            List<S> offSpring = crossover_.execute(parents);

            mutation_.execute(offSpring.get(0));
            mutation_.execute(offSpring.get(1));

            problem_.evaluate(offSpring.get(0));
            problem_.evaluate(offSpring.get(1));

            offspringPopulation_.add(offSpring.get(0));
            offspringPopulation_.add(offSpring.get(1));

        } // for

        environmentalSelection();

    }

    @Override
    public List<S> getPopulation() {
        return population_;
    }

    @Override
    public void receive(List<S> solutions) {
        offspringPopulation_ = solutions;
        environmentalSelection();
    }

    @Override
    public List<S> getOffspring() {
        return offspringPopulation_;
    }

}
