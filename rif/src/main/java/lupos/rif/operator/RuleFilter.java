/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.rif.operator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.Filter;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.engine.operators.singleinput.ExpressionEvaluation.Helper;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.RIFException;
import lupos.rif.model.Conjunction;
import lupos.rif.model.Equality;
import lupos.rif.model.RuleVariable;

import com.google.common.collect.Multimap;

public class RuleFilter extends SingleInputOperator {

	protected IExpression expression;
	protected final Multimap<IExpression, IExpression> equalityMap;
	protected final Set<Variable> assignVariables = new HashSet<Variable>();
	protected int cardinality = -1;

	public RuleFilter(IExpression expression,
			Multimap<IExpression, IExpression> eqMap) {
		super();
		this.setExpression(expression);
		equalityMap = eqMap;
	}

	public RuleFilter() {
		expression = null;
		equalityMap = null;
	}
	
	public void setExpression(IExpression expression) {
		if (expression instanceof Conjunction) {
			if (((Conjunction) expression).exprs.size() == 1) {
				expression = ((Conjunction) expression).exprs.get(0);
			}
		}
		this.expression = expression;
	}

	public IExpression getExpression() {
		return expression;
	}
	
	public boolean equalFilterExpression(RuleFilter r) {
		return (this.expression.equals(r.expression));
	}

	public boolean isAssignment() {
		return !assignVariables.isEmpty();
	}

	public Set<Variable> getAssignedVariables() {
		return assignVariables;
	}

	public Set<Variable> getVariablesInExpression() {
		final Set<Variable> vars = new HashSet<Variable>();
		for (final RuleVariable var : expression.getVariables())
			vars.add(var.getVariable());
		return vars;
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final Iterator<Bindings> resultIterator = new Iterator<Bindings>() {
			final Iterator<Bindings> bindIt = bindings.oneTimeIterator();
			int number = 0;
			Bindings next = computeNext();

			public boolean hasNext() {
				return (next != null);
			}

			public Bindings next() {
				final Bindings zNext = next;
				next = computeNext();
				return zNext;
			}

			private Bindings computeNext() {
				while (bindIt.hasNext()) {
					final Bindings bind = bindIt.next();
					if (bind != null) {
						try{
							final boolean result = filter(bind);
							if (result) {
								number++;
								onAccepted(bind);
								return bind;
							} else
								onFilteredOut(bind);
						} catch(Exception e){
							onFilteredOut(bind);
						} catch(Error e){
							onFilteredOut(bind);
						}
					}
				}
				cardinality = number;
				return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};

		if (resultIterator.hasNext())
			return QueryResult.createInstance(resultIterator);
		else
			return null;
	}

	protected boolean filter(Bindings bind) {
		try {
			return Helper.booleanEffectiveValue(expression.evaluate(bind, null, equalityMap));
		} catch (TypeErrorException e) {
			throw new RIFException(e.getMessage());
		}
	}

	protected void onFilteredOut(final Bindings bind) {
	}

	protected void onAccepted(final Bindings bind) {
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		unionVariables = new HashSet<Variable>(msg.getVariables());
		for (final RuleVariable var : expression.getVariables())
			if (!unionVariables.contains(var.getVariable())) {
				unionVariables.add(var.getVariable());
				assignVariables.add(var.getVariable());
			}
		intersectionVariables = new HashSet<Variable>(unionVariables);
		result.getVariables().addAll(intersectionVariables);
		return result;
	}

	public String toString() {
		String result = "Rulefilter\n" + expression.toString();

		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}

		return result;
	}

	public String toString(final Prefix prefixInstance) {
		String result = "Rulefilter\n" + expression.toString(prefixInstance);

		if (this.cardinality >= 0) {
			result += "\nCardinality: " + this.cardinality;
		}

		return result;
	}

	public Multimap<IExpression, IExpression> getEqualities() {
		return equalityMap;
	}

	// @Override
	// public boolean equals(Object arg0) {
	// if (arg0 instanceof RuleFilter)
	// return ((RuleFilter) arg0).getExpression().equals(getExpression());
	// return false;
	// }
	//
	// @Override
	// public int hashCode() {
	// return expression.hashCode();
	// }
	
	public boolean remainsSortedData(Collection<Variable> sortCriterium){
		if (this.getExpression() instanceof Equality)
			if (((Equality) this.getExpression()).leftExpr instanceof RuleVariable) {
				final Variable assignVar = ((RuleVariable) ((Equality) this
						.getExpression()).leftExpr).getVariable();
				if (sortCriterium.contains(assignVar)) {
					return false;
				}
			}
		return true;
	}
}
