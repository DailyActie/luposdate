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
package lupos.engine.operators.rdfs.index;

import java.util.Set;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.tripleoperator.TripleConsumer;
public class RDFSPutIntoIndicesCyclicComputation extends RDFSPutIntoIndices
		implements TripleConsumer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4752511368314625259L;
	private final Set<Triple> newTriplesSet;

	/**
	 * <p>Constructor for RDFSPutIntoIndicesCyclicComputation.</p>
	 *
	 * @param newTriplesSet a {@link java.util.Set} object.
	 * @param indices a {@link lupos.engine.operators.index.Indices} object.
	 */
	public RDFSPutIntoIndicesCyclicComputation(final Set<Triple> newTriplesSet,
			final Indices indices) {
		super(indices);
		this.newTriplesSet = newTriplesSet;
	}

	/**
	 * <p>newTripleProcessing.</p>
	 */
	public void newTripleProcessing() {
		newTriplesSet.clear();
	}

	/**
	 * <p>getNewTriples.</p>
	 *
	 * @return a boolean.
	 */
	public boolean getNewTriples() {
		return !newTriplesSet.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public void consume(final Triple triple) {
		if (!newTriplesSet.contains(triple) && !indices.contains(triple)) {
			newTriplesSet.add(triple);
		}
	}
}
