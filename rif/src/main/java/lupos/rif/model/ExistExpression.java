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
package lupos.rif.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.IRuleVisitor;
import lupos.rif.IVariableScope;
import lupos.rif.RIFException;

import com.google.common.collect.Multimap;
public class ExistExpression extends AbstractRuleNode implements IExpression,
IVariableScope {
	private final Set<RuleVariable> vars = new HashSet<RuleVariable>();
	public IExpression expr;

	/**
	 * <p>accept.</p>
	 *
	 * @param visitor a {@link lupos.rif.IRuleVisitor} object.
	 * @param arg a A object.
	 * @param <R> a R object.
	 * @param <A> a A object.
	 * @return a R object.
	 * @throws lupos.rif.RIFException if any.
	 */
	public <R, A> R accept(IRuleVisitor<R, A> visitor, A arg) throws RIFException {
		return visitor.visit(this, arg);
	}

	/**
	 * <p>getLabel.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLabel() {
		return "Exists";
	}

	/** {@inheritDoc} */
	@Override
	public List<IRuleNode> getChildren() {
		List<IRuleNode> ret = new ArrayList<IRuleNode>();
		ret.addAll(vars);
		ret.add(expr);
		return ret;
	}

	/**
	 * <p>containsOnlyVariables.</p>
	 *
	 * @return a boolean.
	 */
	public boolean containsOnlyVariables() {
		return false;
	}

	/**
	 * <p>getVariables.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<RuleVariable> getVariables() {
		// nur Variablen, die nicht deklariert sind, aber im K�rper vorkommen
		Set<RuleVariable> innerVars = expr.getVariables();
		innerVars.removeAll(vars);
		return innerVars;
	}

	/**
	 * <p>getPredicates.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<Uniterm> getPredicates() {
		// Exists steht f�r sich
		return Arrays.asList();
	}

	/** {@inheritDoc} */
	public Object evaluate(Bindings binding) {
		return evaluate(binding, null);
	}

	/** {@inheritDoc} */
	public Object evaluate(Bindings binding, Object optionalResult) {
		return evaluate(binding, optionalResult, null);
	}

	/** {@inheritDoc} */
	public Object evaluate(Bindings binding, Object result, Multimap<IExpression, IExpression> equalities) {
		throw new RIFException("Exists not supported in Evaluation!");
	}

	/** {@inheritDoc} */
	public boolean isBound(RuleVariable var, Collection<RuleVariable> boundVars) {
		return expr.isBound(var, boundVars);
	}

	/**
	 * <p>isPossibleAssignment.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPossibleAssignment() {
		return false;
	}

	/**
	 * <p>getDeclaredVariables.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<RuleVariable> getDeclaredVariables() {
		return vars;
	}

	/** {@inheritDoc} */
	public void addVariable(RuleVariable var) {
		var.setParent(this);
		vars.add(var);
	}

	/** {@inheritDoc} */
	public String toString(Prefix prefixInstance) {
		return toString();
	}
}
