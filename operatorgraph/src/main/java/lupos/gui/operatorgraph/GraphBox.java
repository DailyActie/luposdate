/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.gui.operatorgraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.guielements.ContainerArrange;

/**
 * Represents one box in the graph.
 */
public class GraphBox {
	private GraphWrapper op;
	public int height = 0;
	public int width = 0;
	private int x = -1;
	private int y = -1;
	protected AbstractSuperGuiComponent element;
	private Hashtable<GraphWrapper, AbstractSuperGuiComponent> lineAnnotations = new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();
	private final OperatorGraph parent;

	/**
	 * Line Colors for the arrows between the elements.
	 */
	private static Color[] lineColors = {
		Color.black, Color.blue, Color.green, Color.red, Color.cyan,
		Color.GRAY, Color.magenta, Color.orange, Color.DARK_GRAY
	};

	/**
	 * index of lineColors for the line color.
	 */
	private static int lineColor = 0;

	private int[] colorIndices = null;

	/**
	 * Constructor for the box.
	 * 
	 * @param parent
	 *            QueryGraph where the box is in
	 * @param op
	 *            element to put in the box
	 */
	public GraphBox(final OperatorGraph parent, final GraphWrapper op) {
		this.parent = parent;

		this.updateColorIndizes(op);

		this.initBox(op);
	}

	public void initBox(final GraphWrapper op) {
		this.op = op;

		this.element = this.op.createObject(this.parent);

		this.removeAnnotations();

		this.lineAnnotations = this.op.drawLineAnnotations(this.parent);

		this.width = this.element.getPreferredSize().width;
		this.height = this.element.getPreferredSize().height;
	}

	public void arrange(final boolean flipX, final boolean flipY,
			final boolean rotate,
			final Arrange arrange) {
		if (this.element instanceof ContainerArrange) {
			((ContainerArrange) this.element).arrange(flipX, flipY, rotate, arrange);
		}

		this.arrangeWithoutUpdatingParentsSize();

		this.parent.updateSize();
	}

	public void arrangeWithoutUpdatingParentsSize() {
		this.drawLineAnnotations(true);

		this.element.setBounds(this.x, this.y, this.width, this.height);

		this.parent.add(this.element);
	}

	public void draw(final Graphics2D g) {
		// walk through child boxes...
		for(int i = 0; i < this.op.getSucceedingElements().size(); i += 1) {
			final GraphWrapper childGW = (this.op.getSucceedingElements().get(i)).getOperator();
			final GraphBox childBox = this.parent.getBoxes().get(childGW);

			// check for direct circle with one drawn direction...
			final boolean directCircle = this.op.getPrecedingElements().contains(childGW) && this.parent.annotationWasProcessed(childGW, this.op);

			// draw arrow to child box...
			final Point startPoint = GraphBox.determineEdgePoint(this.x,
					this.y,
					this.width, this.height, childBox.x, childBox.y,
					childBox.width, childBox.height);

			final Point endPoint = GraphBox.determineEdgePoint(childBox.x,
					childBox.y, childBox.width, childBox.height, this.x,
					this.y, this.width, this.height);

			int colorIndex = 0;

			if(this.parent.useLineColors() && this.colorIndices.length > 0 && i < this.colorIndices.length) {
				colorIndex = this.colorIndices[i];
			}

			g.setColor(GraphBox.lineColors[colorIndex]);

			if(!directCircle) {
				// drawConnection(g, startX, startY, endPoint.x, endPoint.y,
				// true);
				drawConnection(g, startPoint.x, startPoint.y, endPoint.x,
						endPoint.y, true);
			}

			final JPanel annotationPanel = this.drawLineAnnotation(childGW, false);

			if(annotationPanel == null) { // DummyOpetator
				continue;
			}

			final Dimension annotationDimension = annotationPanel.getPreferredSize();
			final Point annotationPoint = annotationPanel.getLocation();

			if(childBox.equals(this)) { // connection to itself...
				final int startPoint1x = this.x + (this.width / 2) - 5;
				final int startPoint1y = this.y + this.height;

				final int endPoint1x = annotationPoint.x + (annotationDimension.width / 2) - 5;
				final int endPoint1y = annotationPoint.y;

				drawConnection(g, startPoint1x, startPoint1y, endPoint1x, endPoint1y, false);

				final int startPoint2x = annotationPoint.x + (annotationDimension.width / 2) + 5;
				final int startPoint2y = annotationPoint.y;

				final int endPoint2x = this.x + (this.width / 2) + 5;
				final int endPoint2y = this.y + this.height;

				drawConnection(g, startPoint2x, startPoint2y, endPoint2x, endPoint2y, true);
			}
			else if(directCircle) { // direct circle...

				final Point point1 = GraphBox.determineEdgePoint(
						annotationPoint.x, annotationPoint.y,
						annotationDimension.width, annotationDimension.height,
						this.x, this.y, this.width, this.height);

				final Point endPoint1 = GraphBox.determineEdgePoint(this.x,
						this.y,
						this.width, this.height, annotationPoint.x,
						annotationPoint.y, annotationDimension.width,
						annotationDimension.height);

				drawConnection(g, point1.x, point1.y, endPoint1.x, endPoint1.y,
						false);

				final Point point2 = GraphBox.determineEdgePoint(childBox.x,
						childBox.y, childBox.width, childBox.height,
						annotationPoint.x, annotationPoint.y,
						annotationDimension.width, annotationDimension.height);

				final Point endPoint2 = GraphBox
						.determineEdgePoint(annotationPoint.x,
								annotationPoint.y, annotationDimension.width,
								annotationDimension.height, childBox.x,
								childBox.y, childBox.width, childBox.height);

				drawConnection(g, point2.x, point2.y, endPoint2.x, endPoint2.y,
						true);
			}
		}
	}

	private static Point determineEdgePoint(final int x, final int y,
			final int width, final int height, final int x2, final int y2,
			final int width2, final int height2) {
		if (y < y2) {
			final int diffy = y2 - y;
			if (x2 + width2 < x && x - x2 - width2 > diffy) {
				return new Point(x, y + height / 2);
			} else {
				if (x2 > x + width && x2 - x - width > diffy)
					return new Point(x + width, y + height / 2);
				else
					return new Point(x + width / 2, y + height);
			}
		} else {
			final int diffy = y - y2;
			if (x2 + width2 < x && x - x2 - width2 > diffy) {
				return new Point(x, y + height / 2);
			} else {
				if (x2 > x + width && x2 - x - width > diffy)
					return new Point(x + width, y + height / 2);
				else
					return new Point(x + width / 2, y);
			}
		}
	}

	private synchronized JPanel drawLineAnnotation(final GraphWrapper childGW, final boolean align) {
		// get current child box...
		final GraphBox childBox = this.parent.getBoxes().get(childGW);

		if(childBox == null) {
			this.parent.remove(this.lineAnnotations.get(childGW));
			this.parent.getBoxes().remove(childGW);
			this.lineAnnotations.remove(childGW);

			return null;
		}

		// get panel for predicate to this arrow...
		final JPanel annotationPanel = this.lineAnnotations.get(childGW);

		if(annotationPanel == null) { // DummyOpetator...
			return annotationPanel;
		}

		int centerX = -1;
		int centerY = -1;

		if(childBox.equals(this)) { // connection to itself...
			centerX = this.x + (this.width / 2);
			centerY = this.y + this.height + 10 + (annotationPanel.getPreferredSize().height / 2);
		}
		else {
			// determine start point of the connection...
			final Point startPoint = GraphBox.determineEdgePoint(this.x,
					this.y, this.width, this.height, childBox.x, childBox.y,
					childBox.width, childBox.height);

			// determine end point of the connection...
			final Point endPoint = GraphBox.determineEdgePoint(childBox.x,
					childBox.y, childBox.width, childBox.height, this.x,
					this.y, this.width, this.height);

			// determine center of the connection...
			centerX = (startPoint.x + endPoint.x) / 2;
			centerY = (startPoint.y + endPoint.y) / 2;
		}

		// position panel for predicate so, that center of predicate panel is
		// equal to center of the arrow...
		int x = centerX - (annotationPanel.getPreferredSize().width / 2);
		final int y = centerY - (annotationPanel.getPreferredSize().height / 2);

		if(align == true && x < this.parent.PADDING) {
			final int diff = Math.abs((int) this.parent.PADDING - x);

			childBox.updateX(childBox.getX() + diff, y, new HashSet<GraphBox>());

			this.x += diff;

			x += diff;
		}

		// connection in opposite direction does exist and was already drawn...
		if(this.op.getPrecedingElements().contains(childGW) && this.parent.annotationWasProcessed(childGW, this.op)) {
			x += this.lineAnnotations.get(childGW).getPreferredSize().width + 10;
		}

		annotationPanel.setBounds(x, y, annotationPanel.getPreferredSize().width, annotationPanel.getPreferredSize().height);

		this.parent.add(annotationPanel); // add predicate panel to the parent object...

		this.parent.addProcessedAnnotation(this.op, childGW);

		return annotationPanel;
	}

	public void updateX(final int x, final int y,
			final HashSet<GraphBox> visited) {
		if (visited.contains(this)) {
			return;
		}

		visited.add(this);

		final int offset = this.x - x;

		this.setX(x);

		// --- update position of subtree - begin ---
		for (final Object o : this.getOp().getSucceedingElements()) {
			final GraphWrapperIDTuple gwIDt = (GraphWrapperIDTuple) o;
			final GraphBox box = parent.getBoxes().get(gwIDt.getOperator());
			if (box.y > y) {
				box.updateX(box.getX() + offset, y, visited);
			}
		}
		// --- update position of subtree - end ---
	}

	private void drawLineAnnotations(final boolean align) {
		final List<GraphWrapper> list = new LinkedList<GraphWrapper>();
		list.addAll(this.lineAnnotations.keySet());

		for(final GraphWrapper child : list) {
			this.drawLineAnnotation(child, align);
		}
	}

	public void setLineAnnotations(final Hashtable<GraphWrapper, AbstractSuperGuiComponent> annotationList) {
		this.removeAnnotations();

		this.lineAnnotations = annotationList;

		this.drawLineAnnotations(false);
	}

	/**
	 * Draws an arrow from one box to an other.
	 * 
	 * @param g
	 *            Graphics2D object
	 * @param x
	 *            x coordinate of box
	 * @param y
	 *            y coordinate of box
	 * @param xChild
	 *            x coordinate of child box
	 * @param yChild
	 *            y coordinate of child box
	 */
	public static void drawConnection(final Graphics2D g, final int x, final int y, final int xChild, final int yChild, final boolean arrowHead) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawLine(x, y, xChild, yChild); // draw normal line

		if(arrowHead) {
			g.setStroke(new BasicStroke(1f)); // solid arrow head

			final int sideLength = (int) (8 * 1.0);
			final int sideStrength = (int) (5 * 1.0);
			final double aDir = Math.atan2(x - xChild, y - yChild);

			// create new polygon for arrow head...
			final Polygon tmpPoly = new Polygon();
			// add arrow tip as point...
			tmpPoly.addPoint(xChild, yChild);
			// add one edge as point...
			tmpPoly.addPoint(xChild + xCor(sideLength, aDir + 0.5), yChild + yCor(sideLength, aDir + 0.5));
			// add between the edges as point...
			tmpPoly.addPoint(xChild + xCor(sideStrength, aDir), yChild + yCor(sideStrength, aDir));
			// add other edge as point...
			tmpPoly.addPoint(xChild + xCor(sideLength, aDir - 0.5), yChild + yCor(sideLength, aDir - 0.5));
			// add arrow tip as point...
			tmpPoly.addPoint(xChild, yChild);

			g.drawPolygon(tmpPoly); // draw the arrow head
			g.fillPolygon(tmpPoly); // fill the arrow head
		}
	}

	private static int yCor(final int len, final double dir) {
		return (int) (len * Math.cos(dir));
	}

	private static int xCor(final int len, final double dir) {
		return (int) (len * Math.sin(dir));
	}

	public GraphWrapper getOp() {
		return this.op;
	}

	public AbstractSuperGuiComponent getElement() {
		return this.element;
	}

	public void deleteLineAnnotation(final GraphWrapper gw) {
		this.lineAnnotations.remove(gw);
	}

	@Override
	public String toString() {
		return this.op.toString();
	}

	public void removeAnnotations() {
		for(final AbstractSuperGuiComponent agc : this.lineAnnotations.values()) {
			agc.setVisible(false);
		}

		this.lineAnnotations.clear();
	}

	public void removeAnnotationsTo(final GraphWrapper gw) {
		final AbstractSuperGuiComponent asgc = this.lineAnnotations.get(gw);

		if(asgc != null) {
			asgc.setVisible(false);
		}

		this.lineAnnotations.remove(gw);
	}

	public void setX(final int x) {
		this.x = x;

		this.element.setBounds(this.x, this.y, this.width, this.height);

		this.parent.updateSize();
	}

	public void setXWithoutUpdatingParentsSize(final int x) {
		this.x = x;

		this.element.setBounds(this.x, this.y, this.width, this.height);
	}

	public void setY(final int y) {
		this.y = y;

		this.element.setBounds(this.x, this.y, this.width, this.height);

		this.parent.updateSize();
	}

	public void setYWithoutUpdatingParentsSize(final int y) {
		this.y = y;

		this.element.setBounds(this.x, this.y, this.width, this.height);
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public Hashtable<GraphWrapper, AbstractSuperGuiComponent> getLineAnnotations() {
		return this.lineAnnotations;
	}

	@Override
	public boolean equals(final Object o) {
		if(o instanceof GraphBox) {
			final GraphBox g = (GraphBox) o;

			return g.op.equals(this.op) && g.x == this.x && g.y == this.y;
		}

		return false;
	}

	public static void resetLineColorIndex() {
		GraphBox.lineColor = 0;
	}

	public OperatorGraph getParent() {
		return parent;
	}

	public void updateColorIndizes() {
		this.updateColorIndizes(this.op);
	}

	public void updateColorIndizes(final GraphWrapper op) {
		this.colorIndices = new int[op.getSucceedingElements().size()];

		for(int i = 0; i < op.getSucceedingElements().size(); i += 1) {
			this.colorIndices[i] = GraphBox.lineColor++ % GraphBox.lineColors.length;
		}
	}
}