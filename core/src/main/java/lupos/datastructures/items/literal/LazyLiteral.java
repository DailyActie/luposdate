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
package lupos.datastructures.items.literal;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.buffermanager.BufferManager_CachedFiles;
import lupos.datastructures.buffermanager.BufferManager_CachedFiles.REPLACEMENTSTRATEGY;
import lupos.datastructures.items.literal.codemap.IntegerStringMap;
import lupos.datastructures.items.literal.codemap.StringIntegerMap;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
import lupos.sparql1_1.ASTBlankNode;
import lupos.sparql1_1.ASTBooleanLiteral;
import lupos.sparql1_1.ASTDoubleCircumflex;
import lupos.sparql1_1.ASTFloatingPoint;
import lupos.sparql1_1.ASTInteger;
import lupos.sparql1_1.ASTLangTag;
import lupos.sparql1_1.ASTNIL;
import lupos.sparql1_1.ASTObjectList;
import lupos.sparql1_1.ASTQName;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.ASTRDFLiteral;
import lupos.sparql1_1.ASTStringLiteral;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;

/**
 * This class determines the type of it (like URILiteral, AnonymousLiteral,
 * TypedLiteral, ...) lazy, i.e., only up on request by a special method.
 * Internally, it uses a code for its string representation.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class LazyLiteral extends Literal {

	/**
	 *
	 */
	private static final long serialVersionUID = 2768495922178003010L;
	private int code;
	private Literal materializedLiteral = null;
	/** Constant <code>lock</code> */
	protected static ReentrantLock lock = new ReentrantLock();

	public static int MAX_LITERALS_IN_CACHE = 100000;

	protected static REPLACEMENTSTRATEGY<Integer> replacementstrategy =
			new BufferManager_CachedFiles.LeastRecentlyUsed<Integer>(MAX_LITERALS_IN_CACHE);
	protected static HashMap<Integer, Literal> cache = new HashMap<Integer, Literal>(MAX_LITERALS_IN_CACHE);

	/**
	 * <p>Constructor for LazyLiteral.</p>
	 */
	public LazyLiteral() {
		// nothing to initialize
	}

	/**
	 * <p>Constructor for LazyLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 */
	public LazyLiteral(final String content) {
		final Integer codeFromHashMap = hm.get(content);
		if (codeFromHashMap != null && codeFromHashMap != 0) {
			this.code = codeFromHashMap.intValue();
		} else {
			lock.lock();
			try {
				this.code = v.size() + 1;
				hm.put(content, new Integer(this.code));
				if (this.code == Integer.MAX_VALUE) {
					System.err.println("Literal code overflow! Not good!");
				}
				v.put(new Integer(this.code), content);
			} finally{
				lock.unlock();
			}
		}
	}

	/**
	 * <p>Constructor for LazyLiteral.</p>
	 *
	 * @param code a int.
	 */
	public LazyLiteral(final int code) {
		this.code = code;
	}

	/**
	 * <p>Constructor for LazyLiteral.</p>
	 *
	 * @param code a int.
	 * @param materializedLiteral a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public LazyLiteral(final int code, final Literal materializedLiteral) {
		this.code = code;
		this.materializedLiteral = materializedLiteral;
	}

	/** {@inheritDoc} */
	@Override
	public String[] getUsedStringRepresentations() {
		return new String[] { this.toString() };
	}

	/** {@inheritDoc} */
	@Override
	public String getKey(){
		return ""+this.code;
	}

	/** {@inheritDoc} */
	@Override
	public String getOriginalKey(){
		return this.getKey();
	}

	/**
	 * <p>getLiteral.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public Literal getLiteral() {
		return this.getLiteral(false);
	}
	private Literal getLiteral(final boolean lookup) {
		if (this.materializedLiteral == null) {
			this.materializedLiteral = cache.get(this.code);
			if(this.materializedLiteral==null){
				this.materializedLiteral = getLiteral(lookup? v.get(this.code) : this.originalString());
				if(cache.size()>MAX_LITERALS_IN_CACHE){
					final Integer r = replacementstrategy.getToBeReplaced();
					cache.remove(r);
				}
			}
			replacementstrategy.accessNow(this.code);
		}
		return this.materializedLiteral;
	}

	/** {@inheritDoc} */
	@Override
	public int compareToNotNecessarilySPARQLSpecificationConform(final Literal l) {
		if (l instanceof LazyLiteral) {
			return (this.code - ((LazyLiteral) l).code);
		} else {
			return this.getLiteral().compareTo(l);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (this.materializedLiteral == null){
			final Literal literal = this.getLiteral(true);
			return literal.toString();
		}
		return this.materializedLiteral.toString();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.code;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object l) {
		if (!(l instanceof Literal)) {
			return false;
		}
		return this.compareToNotNecessarilySPARQLSpecificationConform((Literal) l) == 0;
	}

	/** {@inheritDoc} */
	@Override
	public boolean valueEquals(final Literal lit) {
		return this.compareToNotNecessarilySPARQLSpecificationConform(lit) == 0;
	}

	/**
	 * <p>Getter for the field <code>code</code>.</p>
	 *
	 * @return a int.
	 */
	public int getCode() {
		return this.code;
	}

	/**
	 * <p>isMaterialized.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isMaterialized() {
		return (this.materializedLiteral != null);
	}

	/** Constant <code>hm</code> */
	protected static StringIntegerMap hm = null;
	/** Constant <code>v</code> */
	protected static IntegerStringMap v = null;

	/**
	 * <p>maxID.</p>
	 *
	 * @return a int.
	 */
	public static int maxID() {
		return v.size();
	}

	/**
	 * <p>Getter for the field <code>hm</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.codemap.StringIntegerMap} object.
	 */
	public static StringIntegerMap getHm() {
		return hm;
	}

	/**
	 * <p>Setter for the field <code>hm</code>.</p>
	 *
	 * @param hm a {@link lupos.datastructures.items.literal.codemap.StringIntegerMap} object.
	 */
	public static void setHm(final StringIntegerMap hm) {
		LazyLiteral.hm = hm;
	}

	/**
	 * <p>Getter for the field <code>v</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.literal.codemap.IntegerStringMap} object.
	 */
	public static IntegerStringMap getV() {
		return v;
	}

	/**
	 * <p>Setter for the field <code>v</code>.</p>
	 *
	 * @param v a {@link lupos.datastructures.items.literal.codemap.IntegerStringMap} object.
	 */
	public static void setV(final IntegerStringMap v) {
		LazyLiteral.v = v;
	}

	/**
	 * <p>getLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal getLiteral(final String content) {
		return LazyLiteral.getLiteral(content, false);
	}


	/**
	 * <p>getLiteral.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param allowLazyLiteral a boolean.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal getLiteral(final String content, final boolean allowLazyLiteral) {
		try {
			final SimpleNode node = SPARQL1_1Parser.parseGraphTerm(content, null);
			return getLiteral(node, allowLazyLiteral);
		} catch (final Throwable t) {
			System.err.println("Trying to parse string "+content+" for transforming it to a literal...");
			System.err.println(t);
			t.printStackTrace();
			return null;
		}
	}

	/**
	 * <p>getLiteral.</p>
	 *
	 * @param n a lupos$sparql1_1$Node object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal getLiteral(final Node n) {
		return getLiteral(n, false);
	}

	/**
	 * <p>getLiteral.</p>
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 * @param allowLazyLiteral a boolean.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal getLiteral(final Node node, final boolean allowLazyLiteral) {
		Literal literal = null;
		Node n = node;

		if (n instanceof ASTNIL) {
			try {
				literal = (allowLazyLiteral) ? LiteralFactory
						.createURILiteral("<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>")
						: LiteralFactory
								.createURILiteralWithoutLazyLiteral("<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>");
			} catch (final URISyntaxException e1) {
				e1.printStackTrace();
			}
		} else if (n instanceof ASTBlankNode) {
			final ASTBlankNode blankNode = (ASTBlankNode) n;
			final String name = blankNode.getIdentifier();
			literal = (allowLazyLiteral) ? LiteralFactory
					.createAnonymousLiteral(name) : LiteralFactory
					.createAnonymousLiteralWithoutLazyLiteral(name);
		} else if (n instanceof ASTQuotedURIRef) {
			final ASTQuotedURIRef uri = (ASTQuotedURIRef) n;
			final String name = uri.getQRef();

			if (URILiteral.isURI("<" + name + ">")) {
				try {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createURILiteral("<" + name + ">")
							: LiteralFactory
									.createURILiteralWithoutLazyLiteral("<"
											+ name + ">");
				} catch (final Exception e) {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createLiteral("<" + name + ">") : LiteralFactory
							.createLiteralWithoutLazyLiteral("<" + name + ">");
				}
			} else {
				literal = (allowLazyLiteral) ? LiteralFactory.createLiteral("<"
						+ name + ">") : LiteralFactory
						.createLiteralWithoutLazyLiteral("<" + name + ">");
			}
		} else if (n instanceof ASTRDFLiteral) {
			n = n.jjtGetChild(0);
		}

		if (literal != null) {
			return literal;
		}

		if (n instanceof ASTStringLiteral) {
			final ASTStringLiteral lit = (ASTStringLiteral) n;
			final String quotedContent = lit.getStringLiteral();

			literal = (allowLazyLiteral) ?
					LiteralFactory.createLiteral(quotedContent)
					: LiteralFactory.createLiteralWithoutLazyLiteral(quotedContent);
		} else if (n instanceof ASTInteger) {
			final ASTInteger lit = (ASTInteger) n;
			final String content = String.valueOf(lit.getValue());

			try {
				literal = (allowLazyLiteral) ? LiteralFactory
						.createTypedLiteral("\"" + content + "\"",
								"<http://www.w3.org/2001/XMLSchema#integer>")
						: TypedLiteralOriginalContent.createTypedLiteral("\""
								+ content + "\"",
								"<http://www.w3.org/2001/XMLSchema#integer>");
			} catch (final URISyntaxException e) {
				literal = (allowLazyLiteral) ? LiteralFactory
						.createLiteral(content) : LiteralFactory
						.createLiteralWithoutLazyLiteral(content);
			}
		} else if (n instanceof ASTFloatingPoint) {
			final ASTFloatingPoint lit = (ASTFloatingPoint) n;
			final String content = lit.getValue();

			try {
				if (content.contains("e") || content.contains("E")) {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createTypedLiteral("\"" + content + "\"",
									"<http://www.w3.org/2001/XMLSchema#double>")
							: TypedLiteralOriginalContent
									.createTypedLiteral("\"" + content + "\"",
											"<http://www.w3.org/2001/XMLSchema#double>");
				} else {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createTypedLiteral("\"" + content + "\"",
									"<http://www.w3.org/2001/XMLSchema#decimal>")
							: TypedLiteralOriginalContent
									.createTypedLiteral("\"" + content + "\"",
											"<http://www.w3.org/2001/XMLSchema#decimal>");
				}
			} catch (final URISyntaxException e) {
				literal = (allowLazyLiteral) ? LiteralFactory
						.createLiteral(content) : LiteralFactory
						.createLiteralWithoutLazyLiteral(content);
			}
		} else if (n instanceof ASTBooleanLiteral) {
			final String content = ((ASTBooleanLiteral) n).getState() + "";

			try {
				literal = (allowLazyLiteral) ? LiteralFactory
						.createTypedLiteral("\"" + content + "\"",
								"<http://www.w3.org/2001/XMLSchema#boolean>")
						: TypedLiteralOriginalContent.createTypedLiteral("\""
								+ content + "\"",
								"<http://www.w3.org/2001/XMLSchema#boolean>");
			} catch (final URISyntaxException e) {
				literal = (allowLazyLiteral) ? LiteralFactory
						.createLiteral(content) : LiteralFactory
						.createLiteralWithoutLazyLiteral(content);
			}
		} else if (n instanceof ASTDoubleCircumflex) {
			if (n.jjtGetNumChildren() != 2) {
				System.err.println(n + " is expected to have 2 children!");
			} else {
				final String content = getLiteral(n.jjtGetChild(0), false).toString();
				final String type = getLiteral(n.jjtGetChild(1), false).toString();

				try {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createTypedLiteral(content, type)
							: TypedLiteralOriginalContent.createTypedLiteral(
									content, type);
				} catch (final Exception e) {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createLiteral(content + "^^" + type)
							: LiteralFactory
									.createLiteralWithoutLazyLiteral(content
											+ "^^" + type);
				}
			}
		} else if (n instanceof ASTLangTag) {
			final String content = getLiteral(n.jjtGetChild(0), false).toString();
			final String lang = ((ASTLangTag) n).getLangTag();
			literal = (allowLazyLiteral) ? LiteralFactory
					.createLanguageTaggedLiteral(content, lang)
					: LanguageTaggedLiteralOriginalLanguage
							.createLanguageTaggedLiteral(content, lang);
		} else if (n instanceof ASTQName) {
			final ASTQName uri = (ASTQName) n;
			final String namespace = uri.getNameSpace();
			final String localName = uri.getLocalName();

			final String name = namespace + localName;

			if (URILiteral.isURI("<" + name + ">")) {
				try {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createURILiteral("<" + name + ">")
							: LiteralFactory
									.createURILiteralWithoutLazyLiteral("<"
											+ name + ">");
				} catch (final Exception e) {
					literal = (allowLazyLiteral) ? LiteralFactory
							.createLiteral("<" + name + ">") : LiteralFactory
							.createLiteralWithoutLazyLiteral("<" + name + ">");
				}
			} else {
				literal = (allowLazyLiteral) ? LiteralFactory.createLiteral("<"
						+ name + ">") : LiteralFactory
						.createLiteralWithoutLazyLiteral("<" + name + ">");
			}
		} else if(n instanceof ASTObjectList){
			literal = getLiteral(n.jjtGetChild(0), allowLazyLiteral);
		} else {
			System.err.println("Unexpected type! "
					+ n.getClass().getSimpleName());
		}

		return literal;
	}

	/** {@inheritDoc} */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		this.code =InputHelper.readLuposInt(in);
	}

	/** {@inheritDoc} */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		OutHelper.writeLuposInt(this.code, out);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isBlank() {
		return this.getLiteral().isBlank();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isURI() {
		return this.getLiteral().isURI();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isTypedLiteral(){
		return this.getLiteral().isTypedLiteral();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isLanguageTaggedLiteral(){
		return this.getLiteral().isLanguageTaggedLiteral();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSimpleLiteral(){
		return this.getLiteral().isSimpleLiteral();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isXMLSchemaStringLiteral(){
		return this.getLiteral().isXMLSchemaStringLiteral();
	}

	/** {@inheritDoc} */
	@Override
	public Literal createThisLiteralNew() {
		return this.getLiteral().createThisLiteralNew();
	}
}
