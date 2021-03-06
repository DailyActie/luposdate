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
package lupos.engine.operators.rdfs;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TripleOperator;
import lupos.engine.operators.tripleoperator.TriplePattern;
public class RDFSchemaInference extends RudimentaryRDFSchemaInference {

	/**
	 * <p>Constructor for RDFSchemaInference.</p>
	 */
	public RDFSchemaInference() {
	}

	/** {@inheritDoc} */
	public static void addInferenceRules(final Root ic,
			final TripleOperator tp) {
		addInferenceRules(ic, tp, null);
	}

	/** {@inheritDoc} */
	public static void addInferenceRulesForInstanceData(
			final Root ic, final TripleOperator tp) {
		addInferenceRulesForInstanceData(ic, tp, null);
	}

	/** {@inheritDoc} */
	public static void addInferenceRulesForExternalOntology(
			final Root ic, final TripleOperator tp) {
		addInferenceRulesForExternalOntology(ic, tp, null);
	}

	/** {@inheritDoc} */
	public static void addInferenceRulesForExternalOntology(
			final Root ic, final TripleOperator tp, final Item data) {
		RudimentaryRDFSchemaInference.addInferenceRulesForExternalOntology(ic,
				tp, data);
		try {
			TriplePattern tp_1;
			TriplePattern tp_2;

			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs6
			 * uuu rdf:type rdf:Property . uuu rdfs:subPropertyOf uuu .
			 */
			final TriplePattern tp_rdfs6 = new TriplePattern(v("uuu"), TYPE,
					PROPERTY);
			addTps(data, ic, tp, v("uuu"), SUBPROPERTY, v("uuu"), tp_rdfs6);
			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs8
			 * uuu rdf:type rdfs:Class . uuu rdfs:subClassOf rdfs:Resource .
			 */
			final TriplePattern tp_rdfs8 = new TriplePattern(v("uuu"), TYPE,
					CLASS);
			addTps(data, ic, tp, v("uuu"), SUBCLASS, RESSOURCE, tp_rdfs8);
			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs10
			 * uuu rdf:type rdfs:Class . uuu rdfs:subClassOf uuu .
			 */
			final TriplePattern tp_rdfs10 = new TriplePattern(v("uuu"), TYPE,
					CLASS);
			addTps(data, ic, tp, v("uuu"), SUBCLASS, v("uuu"), tp_rdfs10);
			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs12
			 * uuu rdf:type rdfs:ContainerMembershipProperty . uuu
			 * rdfs:subPropertyOf rdfs:member .
			 */
			final TriplePattern tp_rdfs12 = new TriplePattern(v("uuu"), TYPE,
					u("2000/01/rdf-schema#ContainerMembershipProperty"));
			addTps(data, ic, tp, v("uuu"), SUBPROPERTY,
					u("2000/01/rdf-schema#member"), tp_rdfs12);
			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs13
			 * uuu rdf:type rdfs:Datatype . uuu rdfs:subClassOf rdfs:URILiteral
			 * .
			 */
			final TriplePattern tp_rdfs13 = new TriplePattern(
					v("uuu"),
					TYPE,
					LiteralFactory
							.createURILiteral("<http://www.w3.org/2000/01/rdf-schema#Datatype>"));
			addTps(data, ic, tp, v("uuu"), SUBCLASS,
					u("2000/01/rdf-schema#URILiteral"), tp_rdfs13);
			/*
			 * Extensional entailment rules
			 */

			/*
			 * Rule ext1 If If E contains: uuu rdfs:domain vvv . vvv
			 * rdfs:subClassOf zzz .
			 * 
			 * Then add: uuu rdfs:domain zzz .
			 */
			tp_1 = tp(v("uuu"), DOMAIN, v("vvv"));
			tp_2 = tp(v("vvv"), SUBCLASS, v("zzz"));
			addTpsFilter(data, ic, tp, v("uuu"), DOMAIN, v("zzz"),
					"Filter(?vvv!=?zzz)", tp_1, tp_2);
			/*
			 * Rule ext2 If If E contains: uuu rdfs:range vvv . vvv
			 * rdfs:subClassOf zzz .
			 * 
			 * Then add: uuu rdfs:range zzz .
			 */
			tp_1 = tp(v("uuu"), RANGE, v("vvv"));
			tp_2 = tp(v("vvv"), SUBCLASS, v("zzz"));
			addTpsFilter(data, ic, tp, v("uuu"), RANGE, v("zzz"),
					"Filter(?vvv!=?zzz)", tp_1, tp_2);
			/*
			 * Rule ext3 If If E contains: uuu rdfs:domain vvv . www
			 * rdfs:subPropertyOf uuu .
			 * 
			 * Then add: www rdfs:domain vvv .
			 */
			tp_1 = tp(v("uuu"), DOMAIN, v("vvv"));
			tp_2 = tp(v("www"), SUBCLASS, v("uuu"));
			addTpsFilter(data, ic, tp, v("www"), DOMAIN, v("vvv"),
					"Filter(?www!=?uuu)", tp_1, tp_2);
			/*
			 * Rule ext4 If If E contains: uuu rdfs:range vvv . www
			 * rdfs:subPropertyOf uuu .
			 * 
			 * Then add: www rdfs:range vvv .
			 */
			tp_1 = tp(v("uuu"), RANGE, v("vvv"));
			tp_2 = tp(v("www"), SUBPROPERTY, v("uuu"));
			addTpsFilter(data, ic, tp, v("www"), RANGE, v("vvv"),
					"Filter(?www!=?uuu)", tp_1, tp_2);
			/*
			 * Rule ext5 If If E contains: rdf:type rdfs:subPropertyOf www . www
			 * rdfs:domain vvv .
			 * 
			 * Then add: rdfs:Resource rdfs:subClassOf vvv .
			 */
			tp_1 = tp(TYPE, SUBPROPERTY, v("www"));
			tp_2 = tp(v("www"), DOMAIN, v("vvv"));
			addTps(data, ic, tp, RESSOURCE, SUBCLASS, v("vvv"), tp_1, tp_2);
			/*
			 * Rule ext6 If If E contains: rdfs:subClassOf rdfs:subPropertyOf
			 * www . www rdfs:domain vvv .
			 * 
			 * Then add: rdfs:Class rdfs:subClassOf vvv .
			 */
			tp_1 = tp(SUBCLASS, SUBPROPERTY, v("www"));
			tp_2 = tp(v("www"), DOMAIN, v("vvv"));
			addTps(data, ic, tp, CLASS, SUBCLASS, v("vvv"), tp_1, tp_2);
			/*
			 * Rule ext7 If If E contains: rdf:subPropertyOf rdfs:subPropertyOf
			 * www . www rdfs:domain vvv .
			 * 
			 * Then add: rdf:Property rdfs:subClassOf vvv .
			 */
			tp_1 = tp(SUBPROPERTY, SUBPROPERTY, v("www"));
			tp_2 = tp(v("www"), DOMAIN, v("vvv"));
			addTps(data, ic, tp, PROPERTY, SUBCLASS, v("vvv"), tp_1, tp_2);
			/*
			 * Rule ext8 If If E contains: rdf:subClassOf rdfs:subPropertyOf www
			 * . www rdfs:range vvv .
			 * 
			 * Then add: rdfs:Class rdfs:subClassOf vvv .
			 */
			tp_1 = tp(SUBCLASS, SUBPROPERTY, v("www"));
			tp_2 = tp(v("www"), RANGE, v("vvv"));
			addTps(data, ic, tp, CLASS, SUBCLASS, v("vvv"), tp_1, tp_2);
			/*
			 * Rule ext9 If If E contains: rdf:subPropertyOf rdfs:subPropertyOf
			 * www . www rdfs:range vvv .
			 * 
			 * Then add: rdf:Property rdfs:subClassOf vvv .
			 */
			tp_1 = tp(SUBCLASS, SUBPROPERTY, v("www"));
			tp_2 = tp(v("www"), RANGE, v("vvv"));
			addTps(data, ic, tp, PROPERTY, SUBCLASS, v("vvv"), tp_1, tp_2);
		} catch (final Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	public static void addInferenceRulesForInstanceData(
			final Root ic, final TripleOperator tp, final Item data) {
		RudimentaryRDFSchemaInference.addInferenceRulesForInstanceData(ic, tp,
				data);
		try {
			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs2
			 * aaa rdfs:domain xxx . uuu aaa yyy . uuu rdf:type xxx .
			 */
			final TriplePattern tp_1_1 = tp(v("aaa"), DOMAIN, v("xxx"));
			final TriplePattern tp_1_2 = tp(v("uuu"), v("aaa"), v("yyy"));
			addTps(data, ic, tp, v("uuu"), TYPE, v("xxx"), tp_1_1, tp_1_2);
			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs3
			 * aaa rdfs:range xxx . uuu aaa vvv . vvv rdf:type xxx .
			 */
			final TriplePattern tp_2_1 = new TriplePattern(v("aaa"), RANGE,
					v("xxx"));
			final TriplePattern tp_2_2 = new TriplePattern(v("uuu"), v("aaa"),
					v("vvv"));
			addTps(data, ic, tp, v("vvv"), TYPE, v("xxx"), tp_2_1, tp_2_2);
			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs4a
			 * uuu aaa xxx . uuu rdf:type rdfs:Resource .
			 */
			final TriplePattern tp_rdfs4a = new TriplePattern(v("uuu"),
					v("aaa"), v("xxx"));
			addTps(data, ic, tp, v("uuu"), TYPE, RESSOURCE, tp_rdfs4a);
			/*
			 * RDFS entailment rules. Rule Name If E contains: then add: rdfs4b
			 * uuu aaa vvv. vvv rdf:type rdfs:Resource .
			 */
			final TriplePattern tp_rdfs4b = new TriplePattern(v("uuu"),
					v("aaa"), v("vvv"));
			addTpsFilter(data, ic, tp, v("vvv"), TYPE, RESSOURCE,
					"FILTER(isBLANK(?vvv) || isURI(?vvv))", tp_rdfs4b);

			/*
			 * Extensional entailment rules
			 */

		} catch (final Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	public static void addInferenceRules(final Root ic,
			final TripleOperator tp, final Item data) {
		addInferenceRulesForExternalOntology(ic, tp, data);
		addInferenceRulesForInstanceData(ic, tp, data);
	}
}
