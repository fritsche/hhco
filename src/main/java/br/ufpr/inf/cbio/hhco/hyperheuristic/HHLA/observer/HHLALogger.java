/*
 * Copyright (C) 2019 Gian Fritsche <gmfritsche at inf.ufpr.br>
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
package br.ufpr.inf.cbio.hhco.hyperheuristic.HHLA.observer;

import br.ufpr.inf.cbio.hhco.hyperheuristic.HHLA.HHLA;
import br.ufpr.inf.cbio.hhco.util.output.OutputWriter;
import java.util.logging.Level;
import org.uma.jmetal.util.JMetalLogger;

/**
 *
 * @author Gian Fritsche <gmfritsche at inf.ufpr.br>
 */
public abstract class HHLALogger {

    protected final OutputWriter ow;

    public HHLALogger(String folder, String file) {
        JMetalLogger.logger.log(Level.CONFIG, "{0}: ENABLED", this.getClass().getSimpleName());
        ow = new OutputWriter(folder, file);
    }

    public abstract void update(HHLA hhla);

    /**
     * Close buffer and write to file
     */
    public void close() {
        ow.close();
    }

}
