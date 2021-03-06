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
package lupos.sparql1_1.operatorgraph;

import java.util.LinkedList;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.Group;
import lupos.engine.operators.singleinput.SeveralSucceedingOperators;
import lupos.engine.operators.singleinput.federated.FederatedQuery;
import lupos.engine.operators.singleinput.sort.Sort;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorVariables;
import lupos.sparql1_1.ASTService;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;
public abstract class ServiceGeneratorToJoinWithOriginal extends ServiceGenerator {
	/** {@inheritDoc} */
	@Override
	public void insertFederatedQueryOperator(final ASTService node, final OperatorConnection connection){
		SeveralSucceedingOperators sso = new SeveralSucceedingOperators();
		BasicOperator federatedQuery = this.getFederatedQuery(node);
		Node child0 = node.jjtGetChild(0);
		if(child0 instanceof ASTVar){
			Sort sort = new Sort();
			LinkedList<Variable> listOfVars = new LinkedList<Variable>();
			listOfVars.add(new Variable(((ASTVar)child0).getName()));
			ComparatorVariables comparator = new ComparatorVariables(listOfVars);
			sort.setComparator(comparator);
			Group group = new Group(comparator);
			sort.addSucceedingOperator(group);
			group.addSucceedingOperator(federatedQuery);
			sso.addSucceedingOperator(sort);
		} else {
			sso.addSucceedingOperator(federatedQuery);
		}
		Join join = new Join();
		federatedQuery.addSucceedingOperator(join, 1);
		sso.addSucceedingOperator(join, 0);
		connection.connect(join);
		connection.setOperatorConnection(sso);
	}

	/**
	 * <p>getFederatedQuery.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTService} object.
	 * @return a {@link lupos.engine.operators.singleinput.federated.FederatedQuery} object.
	 */
	protected abstract FederatedQuery getFederatedQuery(ASTService node);
}
