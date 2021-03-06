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
package lupos.datastructures.items;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
public class TripleComparator implements Comparator<Triple>, Externalizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5337742667604259992L;

	public enum COMPARE {
		NONE, SUBJECT, PREDICATE, OBJECT
	};

	protected COMPARE primary = COMPARE.NONE;
	protected COMPARE secondary = COMPARE.NONE;
	protected COMPARE tertiary = COMPARE.NONE;

	/**
	 * <p>Constructor for TripleComparator.</p>
	 */
	public TripleComparator() {
	}

	/**
	 * <p>Constructor for TripleComparator.</p>
	 *
	 * @param primary a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 */
	public TripleComparator(final COMPARE primary) {
		this.primary = primary;
	}

	/**
	 * <p>Constructor for TripleComparator.</p>
	 *
	 * @param primary a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 * @param secondary a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 */
	public TripleComparator(final COMPARE primary, final COMPARE secondary) {
		this.primary = primary;
		this.secondary = secondary;
	}

	/**
	 * <p>Constructor for TripleComparator.</p>
	 *
	 * @param primary a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 * @param secondary a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 * @param tertiary a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 */
	public TripleComparator(final COMPARE primary, final COMPARE secondary,
			final COMPARE tertiary) {
		this.primary = primary;
		this.secondary = secondary;
		this.tertiary = tertiary;
	}

	/**
	 * <p>Constructor for TripleComparator.</p>
	 *
	 * @param orderPattern a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 */
	public TripleComparator(final RDF3XIndexScan.CollationOrder orderPattern) {
		switch (orderPattern) {
		default:
		case SPO:
			this.primary = COMPARE.SUBJECT;
			this.secondary = COMPARE.PREDICATE;
			this.tertiary = COMPARE.OBJECT;
			break;
		case SOP:
			this.primary = COMPARE.SUBJECT;
			this.secondary = COMPARE.OBJECT;
			this.tertiary = COMPARE.PREDICATE;
			break;
		case PSO:
			this.primary = COMPARE.PREDICATE;
			this.secondary = COMPARE.SUBJECT;
			this.tertiary = COMPARE.OBJECT;
			break;
		case POS:
			this.primary = COMPARE.PREDICATE;
			this.secondary = COMPARE.OBJECT;
			this.tertiary = COMPARE.SUBJECT;
			break;
		case OSP:
			this.primary = COMPARE.OBJECT;
			this.secondary = COMPARE.SUBJECT;
			this.tertiary = COMPARE.PREDICATE;
			break;
		case OPS:
			this.primary = COMPARE.OBJECT;
			this.secondary = COMPARE.PREDICATE;
			this.tertiary = COMPARE.SUBJECT;
			break;
		}
	}

	/**
	 * <p>Constructor for TripleComparator.</p>
	 *
	 * @param readByte a byte.
	 */
	public TripleComparator(final byte readByte) {
		init(readByte);
	}

	/**
	 * <p>init.</p>
	 *
	 * @param readByte a byte.
	 */
	protected void init(final byte readByte) {
		this.primary = COMPARE.values()[readByte % COMPARE.values().length];
		this.secondary = COMPARE.values()[(readByte / COMPARE.values().length)
				% COMPARE.values().length];
		this.tertiary = COMPARE.values()[(readByte / (COMPARE.values().length * COMPARE
				.values().length))
				% COMPARE.values().length];
	}

	/**
	 * <p>compare.</p>
	 *
	 * @param t0 a {@link lupos.datastructures.items.Triple} object.
	 * @param t1 a {@link lupos.datastructures.items.Triple} object.
	 * @return a int.
	 */
	public int compare(final Triple t0, final Triple t1) {
		try {
			if (primary != COMPARE.NONE) {
				final Literal l0 = getLiteral(primary, t0);
				final Literal l1 = getLiteral(primary, t1);
				if (!(l0 == null || l1 == null)) {
					final int compare = l0
							.compareToNotNecessarilySPARQLSpecificationConform(l1);
					if (compare != 0)
						return compare;
				} else
					return 0;
			} else
				return 0;

			if (secondary != COMPARE.NONE) {
				final Literal l0 = getLiteral(secondary, t0);
				final Literal l1 = getLiteral(secondary, t1);
				if (!(l0 == null || l1 == null)) {
					final int compare = l0
							.compareToNotNecessarilySPARQLSpecificationConform(l1);
					if (compare != 0)
						return compare;
				} else
					return 0;
			} else
				return 0;

			if (tertiary != COMPARE.NONE) {
				final Literal l0 = getLiteral(tertiary, t0);
				final Literal l1 = getLiteral(tertiary, t1);
				if (!(l0 == null || l1 == null))
					return l0
							.compareToNotNecessarilySPARQLSpecificationConform(l1);
				else
					return 0;
			} else
				return 0;
		} catch (final Exception e) {
			System.err.println(" t0:" + t0);
			System.err.println(" t1:" + t1);
			System.err.println(e);
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * <p>getLiteral.</p>
	 *
	 * @param comp a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 * @param t a {@link lupos.datastructures.items.Triple} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 */
	public static Literal getLiteral(final COMPARE comp, final Triple t) {
		switch (comp) {
		case SUBJECT:
			return t.getSubject();
		case PREDICATE:
			return t.getPredicate();
		case OBJECT:
			return t.getObject();
		default:
			return null;
		}
	}

	/**
	 * <p>getObject.</p>
	 *
	 * @param comp a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 * @param items an array of {@link java.lang.Object} objects.
	 * @return a {@link java.lang.Object} object.
	 */
	public static Object getObject(final COMPARE comp, final Object[] items) {
		switch (comp) {
		case SUBJECT:
			return items[0];
		case PREDICATE:
			return items[1];
		case OBJECT:
			return items[2];
		default:
			return null;
		}
	}

	/**
	 * <p>getItem.</p>
	 *
	 * @param comp a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 * @param items an array of {@link lupos.datastructures.items.Item} objects.
	 * @return a {@link lupos.datastructures.items.Item} object.
	 */
	public static Item getItem(final COMPARE comp, final Item[] items) {
		switch (comp) {
		case SUBJECT:
			return items[0];
		case PREDICATE:
			return items[1];
		case OBJECT:
			return items[2];
		default:
			return null;
		}
	}

	/**
	 * <p>getBytePattern.</p>
	 *
	 * @return a byte.
	 */
	public byte getBytePattern() {
		return (byte) ((byte) primary.ordinal() + COMPARE.values().length
				* (secondary.ordinal() + COMPARE.values().length
						* tertiary.ordinal()));
	}

	/**
	 * <p>add.</p>
	 *
	 * @param c a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 */
	public void add(final COMPARE c) {
		if (primary == COMPARE.NONE)
			primary = c;
		else if (secondary == COMPARE.NONE)
			secondary = c;
		else if (tertiary == COMPARE.NONE)
			tertiary = c;
		else
			System.err
					.println("TripleComparator: TripleComparator-Comparisons already full up!");
	}

	/**
	 * <p>Getter for the field <code>primary</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 */
	public COMPARE getPrimary() {
		return primary;
	}

	/**
	 * <p>Setter for the field <code>primary</code>.</p>
	 *
	 * @param primary a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 */
	public void setPrimary(final COMPARE primary) {
		this.primary = primary;
	}

	/**
	 * <p>Getter for the field <code>secondary</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 */
	public COMPARE getSecondary() {
		return secondary;
	}

	/**
	 * <p>Setter for the field <code>secondary</code>.</p>
	 *
	 * @param secondary a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 */
	public void setSecondary(final COMPARE secondary) {
		this.secondary = secondary;
	}

	/**
	 * <p>Getter for the field <code>tertiary</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 */
	public COMPARE getTertiary() {
		return tertiary;
	}

	/**
	 * <p>Setter for the field <code>tertiary</code>.</p>
	 *
	 * @param tertiary a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 */
	public void setTertiary(final COMPARE tertiary) {
		this.tertiary = tertiary;
	}

	/**
	 * <p>makeNoneForNull.</p>
	 *
	 * @param t a {@link lupos.datastructures.items.Triple} object.
	 */
	public void makeNoneForNull(final Triple t) {
		if (primary != null && primary != COMPARE.NONE) {
			if (t.getPos(primary.ordinal() - 1) == null) {
				primary = COMPARE.NONE;
			}
		}
		if (secondary != null && secondary != COMPARE.NONE) {
			if (t.getPos(secondary.ordinal() - 1) == null) {
				secondary = COMPARE.NONE;
			}
		}
		if (tertiary != null && tertiary != COMPARE.NONE) {
			if (t.getPos(tertiary.ordinal() - 1) == null) {
				tertiary = COMPARE.NONE;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Compare triple by " + toString(primary) + " "
				+ toString(secondary) + " " + toString(tertiary);
	}

	/**
	 * <p>toString.</p>
	 *
	 * @param compare a {@link lupos.datastructures.items.TripleComparator.COMPARE} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String toString(final COMPARE compare) {
		if (compare == null)
			return "null";
		else
			return compare.toString();
	}

	/** {@inheritDoc} */
	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		init((byte) in.read());
	}

	/** {@inheritDoc} */
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeByte(this.getBytePattern());
	}
}
