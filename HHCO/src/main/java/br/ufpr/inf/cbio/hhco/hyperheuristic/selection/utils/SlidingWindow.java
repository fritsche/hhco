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
package br.ufpr.inf.cbio.hhco.hyperheuristic.selection.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gian Fritsche <gmfritsche@inf.ufpr.br>
 * @param <T>
 */
public class SlidingWindow<T> {

    protected int w;
    protected List<Double> credits;
    protected List<T> heuristics;

    public SlidingWindow(int w) {
        this.w = w;
        credits = new ArrayList<>();
        heuristics = new ArrayList<>();
    }

    public void add(T heuristic, double credit) {
        if (heuristics.size() >= w) // if sliding window is full 
        {
            remove(0); // remove older entry
        }		//System.out.println("add: "+heuristic);
        credits.add(credit); // add new entry at end (FIFO)
        heuristics.add(heuristic);
    }

    protected void remove(int index) {
        //System.out.println("remove: "+heuristics.get(index));
        heuristics.remove(index);
        credits.remove(index);
    }

    public int size() {
        return heuristics.size();
    }

    public T getHeuristic(int index) {
        return heuristics.get(index);
    }

    public double getCredit(int index) {
        return credits.get(index);
    }

}
