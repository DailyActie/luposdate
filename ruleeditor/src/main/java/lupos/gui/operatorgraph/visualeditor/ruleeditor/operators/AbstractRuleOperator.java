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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.operators;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.regex.Pattern;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.AbstractRuleOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.AnnotationPanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.ModeEnum;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleEnum;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.misc.util.OperatorIDTuple;
import lupos.misc.Triple;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public abstract class AbstractRuleOperator extends Operator {
	protected RuleEnum classType = RuleEnum.Operator;
	/** Constant <code>internal_name="internal_node_name"</code> */
	public static final String internal_name = "internal_node_name";
	protected String name = "";
	private static int internal_global_id = 0;
	private int internal_id = -1;
	protected boolean alsoSubClasses = false;
	protected int opID = -1;
	protected String opIDLabel = "";
	private HashMap<AbstractRuleOperator, Triple<Boolean, String, ModeEnum>> annotations = new HashMap<AbstractRuleOperator, Triple<Boolean, String, ModeEnum>>();
	private static final HashSet<String> reservedKeyWords = new HashSet<String>();

	static {
		reservedKeyWords.add("abstract");
		reservedKeyWords.add("continue");
		reservedKeyWords.add("for");
		reservedKeyWords.add("new");
		reservedKeyWords.add("switch");
		reservedKeyWords.add("assert");
		reservedKeyWords.add("default");
		reservedKeyWords.add("goto");
		reservedKeyWords.add("package");
		reservedKeyWords.add("synchronized");
		reservedKeyWords.add("boolean");
		reservedKeyWords.add("do");
		reservedKeyWords.add("if");
		reservedKeyWords.add("private");
		reservedKeyWords.add("this");
		reservedKeyWords.add("break");
		reservedKeyWords.add("double");
		reservedKeyWords.add("implements");
		reservedKeyWords.add("protected");
		reservedKeyWords.add("throw");
		reservedKeyWords.add("byte");
		reservedKeyWords.add("else");
		reservedKeyWords.add("import");
		reservedKeyWords.add("public");
		reservedKeyWords.add("throws");
		reservedKeyWords.add("case");
		reservedKeyWords.add("enum");
		reservedKeyWords.add("instanceof");
		reservedKeyWords.add("return");
		reservedKeyWords.add("transient");
		reservedKeyWords.add("catch");
		reservedKeyWords.add("extends");
		reservedKeyWords.add("int");
		reservedKeyWords.add("short");
		reservedKeyWords.add("try");
		reservedKeyWords.add("char");
		reservedKeyWords.add("final");
		reservedKeyWords.add("interface");
		reservedKeyWords.add("static");
		reservedKeyWords.add("void");
		reservedKeyWords.add("class");
		reservedKeyWords.add("finally");
		reservedKeyWords.add("long");
		reservedKeyWords.add("strictfp");
		reservedKeyWords.add("volatile");
		reservedKeyWords.add("const");
		reservedKeyWords.add("float");
		reservedKeyWords.add("native");
		reservedKeyWords.add("super");
		reservedKeyWords.add("while");
	}

	/**
	 * <p>Constructor for AbstractRuleOperator.</p>
	 */
	public AbstractRuleOperator() {
		super();
	}

	/**
	 * <p>Constructor for AbstractRuleOperator.</p>
	 *
	 * @param classType a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleEnum} object.
	 * @param name a {@link java.lang.String} object.
	 */
	public AbstractRuleOperator(RuleEnum classType, String name) {
		this.classType = classType;
		this.name = name;
	}

	/**
	 * <p>Constructor for AbstractRuleOperator.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param loadObject a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public AbstractRuleOperator(String name, JSONObject loadObject) throws JSONException {
		this.classType = RuleEnum.valueOf(loadObject.getString("class type"));

		this.name = name;

		if(this.name.matches(AbstractRuleOperator.internal_name + "\\d+")) {
			this.internal_id = AbstractRuleOperator.internal_global_id;
			AbstractRuleOperator.internal_global_id++;
		}

		this.alsoSubClasses = loadObject.getBoolean("also subclasses");

		this.fromJSON(loadObject);
	}

	/**
	 * <p>prefixAdded.</p>
	 */
	public void prefixAdded() {}
	/** {@inheritDoc} */
	public void prefixModified(String oldPrefix, String newPrefix) {}
	/** {@inheritDoc} */
	public void prefixRemoved(String prefix, String namespace) {}

	/**
	 * <p>serializeOperator.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public StringBuffer serializeOperator() {
		return new StringBuffer();
	}

	/** {@inheritDoc} */
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		return new StringBuffer();
	}

	/** {@inheritDoc} */
	public boolean variableInUse(String variable, HashSet<Operator> visited) {
		return false;
	}

	/**
	 * <p>applyChange.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 * @throws lupos.gui.operatorgraph.visualeditor.util.ModificationException if any.
	 */
	public void applyChange(String value) throws ModificationException {
		if(!value.equals("")) {
			if(AbstractRuleOperator.reservedKeyWords.contains(value)) {
				throw new ModificationException("Operator name can not be a java keyword!", this);
			}

			Pattern p = Pattern.compile("^[a-z]\\w*$", Pattern.CASE_INSENSITIVE);

			if(!p.matcher(value).matches()) {
				throw new ModificationException("Invalid operator name! Operator name must match /^[a-z]\\w*$/", this);
			}
		}

		this.name = value;
	}

	/**
	 * <p>determineNameForDrawing.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	protected String determineNameForDrawing() {
		if(this.name.matches(AbstractRuleOperator.internal_name + "\\d+")) {
			return "";
		}
		else {
			return this.name;
		}
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		String name = this.name;

		if(name.equals("")) {
			if(this.internal_id == -1) {
				this.internal_id = AbstractRuleOperator.internal_global_id;
				AbstractRuleOperator.internal_global_id++;
			}

			name = AbstractRuleOperator.internal_name + this.internal_id;
		}

		return name;
	}

	/**
	 * <p>Setter for the field <code>alsoSubClasses</code>.</p>
	 *
	 * @param state a boolean.
	 */
	public void setAlsoSubClasses(boolean state) {
		this.alsoSubClasses = state;
	}

	/**
	 * <p>alsoSubClasses.</p>
	 *
	 * @return a boolean.
	 */
	public boolean alsoSubClasses() {
		return this.alsoSubClasses;
	}

	/** {@inheritDoc} */
	public boolean validateOperator(boolean showErrors, HashSet<Operator> visited, Object data) {
		if(visited.contains(this)) {
			return true;
		}

		visited.add(this);

		if(this.panel.validateOperatorPanel(showErrors, data) == false) {
			return false;
		}

		for(AbstractGuiComponent<Operator> agc : this.annotationLabels.values()) {
			if(((AnnotationPanel) agc).validateOperatorPanel(showErrors, data) == false) {
				return false;
			}
		}

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators) {
			//			if(this.annotationLabels.get(opIDT.getOperator()).validateOperatorPanel(showErrors, data) == false) {
			//				return false;
			//			}

			if(opIDT.getOperator().validateOperator(showErrors, visited, data) == false) {
				return false;
			}
		}

		return true;
	}

	/**
	 * <p>Setter for the field <code>classType</code>.</p>
	 *
	 * @param classType a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleEnum} object.
	 */
	public void setClassType(RuleEnum classType) {
		this.classType = classType;
	}

	/**
	 * <p>Getter for the field <code>classType</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleEnum} object.
	 */
	public RuleEnum getClassType() {
		return this.classType;
	}

	/**
	 * <p>Getter for the field <code>opID</code>.</p>
	 *
	 * @return a int.
	 */
	public int getOpID() {
		return this.opID;
	}

	/**
	 * <p>Getter for the field <code>opIDLabel</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getOpIDLabel() {
		return this.opIDLabel;
	}

	/** {@inheritDoc} */
	public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawAnnotations(VisualGraph<Operator> parent) {
		Hashtable<GraphWrapper, AbstractSuperGuiComponent> predicates = new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();

		// walk through children of this RDFTerm...
		for(OperatorIDTuple<Operator> opIDTuple : this.getSucceedingOperators()) {
			AbstractRuleOperator child = (AbstractRuleOperator) opIDTuple.getOperator(); // get current children

			// create predicate panel...
			AnnotationPanel annotationPanel = new AnnotationPanel(parent, this, child, this.annotations.get(child));

			this.annotationLabels.put(child, annotationPanel);

			// add predicate panel to hash table with its GraphWrapper...
			predicates.put(new GraphWrapperOperator(child), annotationPanel);
		}

		return predicates;
	}

	/**
	 * <p>Setter for the field <code>opID</code>.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 * @param active a boolean.
	 * @return a boolean.
	 * @throws lupos.gui.operatorgraph.visualeditor.util.ModificationException if any.
	 */
	public boolean setOpID(String value, boolean active) throws ModificationException {
		try {
			if(value.equals("")) {
				if(active) {
					throw new NumberFormatException("the operand id must be a number >= 0");
				}
				else {
					return false;
				}
			}

			int tmp = Integer.parseInt(value);

			if(tmp >= 0) {
				this.opID = tmp;
			}
			else {
				throw new NumberFormatException("the operand id must be >= 0");
			}
		}
		catch(NumberFormatException nfe) {
			if(!value.equals("")) {
				if(AbstractRuleOperator.reservedKeyWords.contains(value)) {
					throw new ModificationException("OperatorID label can not be a java keyword!", this);
				}

				Pattern p = Pattern.compile("^[a-z]\\w*$", Pattern.CASE_INSENSITIVE);

				if(!p.matcher(value).matches()) {
					throw new ModificationException("Invalid operatorID label! OperatorID label must match /^[a-z]\\w*$/", this);
				}

				this.opIDLabel = value;
			}
			else {
				this.handleParseError(nfe);
			}
		}

		return true;
	}

	/**
	 * <p>setActiveConnection.</p>
	 *
	 * @param child a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator} object.
	 * @param state a boolean.
	 */
	public void setActiveConnection(AbstractRuleOperator child, boolean state) {
		Triple<Boolean, String, ModeEnum> t = this.annotations.get(child);

		if(t == null) {
			t = new Triple<Boolean, String, ModeEnum>(state, "-1", ModeEnum.EXISTS);

			this.annotations.put(child, t);
		}
		else {
			t.setFirst(state);
		}
	}

	/**
	 * <p>setChildOpID.</p>
	 *
	 * @param child a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator} object.
	 * @param opID a {@link java.lang.String} object.
	 */
	public void setChildOpID(AbstractRuleOperator child, String opID) {
		Triple<Boolean, String, ModeEnum> t = this.annotations.get(child);

		if(t == null) {
			t = new Triple<Boolean, String, ModeEnum>(true, opID, ModeEnum.EXISTS);

			this.annotations.put(child, t);
		}
		else {
			t.setSecond(opID);
		}
	}

	/**
	 * <p>setMode.</p>
	 *
	 * @param child a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator} object.
	 * @param mode a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.util.ModeEnum} object.
	 */
	public void setMode(AbstractRuleOperator child, ModeEnum mode) {
		Triple<Boolean, String, ModeEnum> t = this.annotations.get(child);

		if(t == null) {
			t = new Triple<Boolean, String, ModeEnum>(false, "-1", mode);

			this.annotations.put(child, t);
		}
		else {
			t.setThird(mode);
		}
	}

	/**
	 * <p>internalToJSON.</p>
	 *
	 * @param connectionsObject a {@link org.json.JSONObject} object.
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	protected JSONObject internalToJSON(JSONObject connectionsObject) throws JSONException {
		JSONObject saveObject = new JSONObject();

		Point position = ((AbstractRuleOperatorPanel) this.panel).getPositionAndDimension().getFirst();

		saveObject.put("op type", this.getClass().getSimpleName());
		saveObject.put("also subclasses", this.alsoSubClasses);
		saveObject.put("class type", this.getClassType());
		saveObject.put("position", new double[]{position.getX(), position.getY()});

		// --- handle connections - begin ---
		JSONArray connectionsArray = new JSONArray();

		for(Operator child : this.annotationLabels.keySet()) {
			AbstractRuleOperator childOp = (AbstractRuleOperator) child;
			AnnotationPanel ap = (AnnotationPanel) this.annotationLabels.get(child);

			JSONObject childConnectionObject = new JSONObject();
			childConnectionObject.put("to", childOp.getName());
			childConnectionObject.put("active", ap.isActive());
			childConnectionObject.put("id", ap.getOpID());
			childConnectionObject.put("id label", ap.getOpLabel());
			childConnectionObject.put("mode", ap.getMode().name());

			connectionsArray.put(childConnectionObject);
		}

		if(connectionsArray.length() > 0) {
			connectionsObject.put(this.getName(), connectionsArray);
		}
		// --- handle connections - end ---

		return saveObject;
	}

	/**
	 * <p>fromJSON.</p>
	 *
	 * @param loadObject a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	protected abstract void fromJSON(JSONObject loadObject) throws JSONException;
	/**
	 * <p>toJSON.</p>
	 *
	 * @param connectionsObject a {@link org.json.JSONObject} object.
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public abstract JSONObject toJSON(JSONObject connectionsObject) throws JSONException;

	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return this.getClass().getSimpleName() + "(" + this.classType + ")";
	}
}
