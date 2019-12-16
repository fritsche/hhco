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
package br.ufpr.inf.cbio.hhco.hyperheuristic.HHCO.logger;

import br.ufpr.inf.cbio.hhco.hyperheuristic.CooperativeAlgorithm;
import br.ufpr.inf.cbio.hhco.hyperheuristic.HHCO.HHCO;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 */
public class MOEASFIRLogger extends HHCOLogger {

    public MOEASFIRLogger(String folder, String file) {
        super(folder, file);
    }

    @Override
    public void update(HHCO hhco) {
        List<CooperativeAlgorithm> algorithms = hhco.getAlgorithms();
        ArrayList<Double> list = hhco.getMoeasfir();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < algorithms.size(); i++) {
            buffer.append(list.get(i));
            buffer.append("\t");
        }
        ow.writeLine(buffer.toString());

        if (hhco.getEvaluations() >= hhco.getMaxEvaluations()) {
            close();
        }
    }
}
