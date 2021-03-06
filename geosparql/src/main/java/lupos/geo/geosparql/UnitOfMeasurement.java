/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.geo.geosparql;

import java.util.HashMap;

import lupos.engine.operators.singleinput.TypeErrorException;

/**
 * Richard Mietz
 * Date: 21.02.13
 *
 * @author groppe
 * @version $Id: $Id
 */
public class UnitOfMeasurement {
    /** Constant <code>conversionRate</code> */
    public static final HashMap<String,Double> conversionRate = new HashMap<String, Double>();

    static {
        conversionRate.put("<http://www.opengis.net/def/uom/OGC/1.0/metre>", 1.0);
    }

    private UnitOfMeasurement() {
    }

    /**
     * <p>convert.</p>
     *
     * @param url a {@link java.lang.String} object.
     * @param valueToConvert a double.
     * @return a double.
     * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
     */
    public static double convert(final String url, final double valueToConvert) throws TypeErrorException {
        if(conversionRate.containsKey(url)) {
            return valueToConvert * conversionRate.get(url);
        }
        else {
            throw new TypeErrorException("URL for measurement unit not known.");
        }
    }

}
