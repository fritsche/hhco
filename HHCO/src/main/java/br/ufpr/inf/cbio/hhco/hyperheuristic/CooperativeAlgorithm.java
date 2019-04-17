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
package br.ufpr.inf.cbio.hhco.hyperheuristic;

import java.util.List;
import org.uma.jmetal.solution.Solution;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 * @param <S>
 */
public interface CooperativeAlgorithm<S extends Solution<?>> {

    /**
     * Initialize the algorithm with a random population.
     *
     * @param populationSize
     */
    public void init(int populationSize);

    /**
     * Initialize the algorithm with {@code initialPopulation}.Used on the
 traditional hyper-heuristic framework to keep only one population where
 the heuristics are applied.
     *
     * @param initialPopulation
     * @param populationSize
     */
    public void init(List<S> initialPopulation, int populationSize);

    public void doIteration();

    public List<S> getPopulation();

    public List<S> getOffspring();

    public void receive(List<S> solutions);

}
