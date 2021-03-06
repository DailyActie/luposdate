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
package lupos.engine.operators.singleinput;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BindingsFactoryMessage;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
public class Projection extends SingleInputOperator {
	private final HashSet<Variable> s = new HashSet<Variable>();
	protected BindingsFactory bindingsFactory;

	/**
	 * <p>Constructor for Projection.</p>
	 */
	public Projection() {
	}

	/**
	 * <p>addProjectionElement.</p>
	 *
	 * @param var a {@link lupos.datastructures.items.Variable} object.
	 */
	public void addProjectionElement(final Variable var) {
		if (!this.s.contains(var)) {
			this.s.add(var);
		}
	}

	/**
	 * <p>getProjectedVariables.</p>
	 *
	 * @return a {@link java.util.HashSet} object.
	 */
	public HashSet<Variable> getProjectedVariables() {
		return this.s;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Handles the BoundVariablesMessage by removing all variables from it that
	 * are not projected to.
	 */
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		for (final Variable v : msg.getVariables()) {
			if (this.s.contains(v)) {
				result.getVariables().add(v);
			}
		}
		this.unionVariables = result.getVariables();
		this.intersectionVariables = result.getVariables();
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final BindingsFactoryMessage msg){
		this.bindingsFactory = msg.getBindingsFactory();
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final Iterator<Bindings> itb = new ParallelIterator<Bindings>() {
			final Iterator<Bindings> itbold = bindings.oneTimeIterator();

			@Override
			public boolean hasNext() {
				return this.itbold.hasNext();
			}

			@Override
			public Bindings next() {
				if (!this.itbold.hasNext()) {
					return null;
				}
				final Bindings bind1 = this.itbold.next();
				if (!this.itbold.hasNext()) {
					if (this.itbold instanceof ParallelIterator) {
						((ParallelIterator) this.itbold).close();
					}
				}
				final Bindings bnew = Projection.this.bindingsFactory.createInstance();

				final Iterator<Variable> it = Projection.this.s.iterator();
				while (it.hasNext()) {
					final Variable elem = it.next();
					bnew.add(elem, bind1.get(elem));
				}
				bnew.addAllTriples(bind1);
				bnew.addAllPresortingNumbers(bind1);
				return bnew;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void finalize() {
				this.close();
			}

			@Override
			public void close() {
				if (this.itbold instanceof ParallelIterator) {
					((ParallelIterator) this.itbold).close();
				}
			}
		};

		return QueryResult.createInstance(itb);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return super.toString()+" to " + this.s;
	}

	/** {@inheritDoc} */
	@Override
	public boolean remainsSortedData(final Collection<Variable> sortCriterium){
		return true;
	}
}
