/*

 Copyright 2001,2003  The Apache Software Foundation 

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package org.apache.batik.svggen;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.batik.ext.awt.g2d.TransformStackElement;
import org.w3c.dom.Element;

/**
 * This class is used by the Graphics2D SVG Generator to manage
 * a group of Nodes that can later be added to the SVG DOM Tree
 * managed by the DOMTreeManager.
 *
 * There are two rules that control how children nodes are
 * added to the group managed by this class:
 *
 * + Children node are added to the group as long as
 *   there is no more than n graphic context overrides needed to
 *   describe the children style. A graphic context override
 *   happens when style attributes need to be added to a child
 *   node to reflect the state of the graphic context at the
 *   time the child was added. Note that the opacity is never
 *   reflected in a group node and therefore, is not accounted
 *   for in the number of overrides. The number of overrides can
 *   be configured and defaults to 2.
 * + Children nodes are added to the current group as long as
 *   the associated GraphicContext's transform stack is valid.
 *
 * When children nodes can no longer be added, the group is considered
 * complete and the associated DOMTreeManager is notified of the
 * availability of a completed group. Then, a new group is started.
 * <br>
 * The DOMTreeManager is also notified every thime a new element
 * is added to the current group. This is needed to let the
 * DOMTreeManager handle group managers that would be used concurrently.
 *
 * @author <a href="mailto:cjolif@ilog.fr">Christophe Jolif</a>
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: DOMGroupManager.java,v 1.14 2005/04/02 12:58:17 deweese Exp $
 */
public class DOMGroupManager implements SVGSyntax, ErrorConstants {
	public final static short DRAW = 0x01;

	public final static short FILL = 0x10;

	/**
	 * Reference to the GraphicContext this manager will use to
	 * reflect style attributes in the tree nodes.
	 */
	protected GraphicContext gc;

	/**
	 * DOMTreeManager that this group manager cooperates with
	 */
	protected DOMTreeManager domTreeManager;

	/**
	 * Current group's SVG GraphicContext state
	 */
	protected SVGGraphicContext groupGC;

	/**
	 * Current group node
	 */
	protected Element currentGroup;

	/**
	 * Constructor
	 * @param gc graphic context whose state will be reflected in the
	 *           element's style attributes.
	 * @param domTreeManager DOMTreeManager instance this group manager
	 *        cooperates with.
	 */
	public DOMGroupManager(final GraphicContext gc, final DOMTreeManager domTreeManager) {
		if (gc == null)
			throw new SVGGraphics2DRuntimeException(ERR_GC_NULL);

		if (domTreeManager == null)
			throw new SVGGraphics2DRuntimeException(ERR_DOMTREEMANAGER_NULL);

		this.gc = gc;
		this.domTreeManager = domTreeManager;

		// Start with a new Top Level Group
		this.recycleCurrentGroup();

		// Build the default GC descriptor
		this.groupGC = domTreeManager.gcConverter.toSVG(gc);
	}

	/**
	 * Reset the state of this object to handle a new currentGroup
	 */
	void recycleCurrentGroup() {
		// Create new initial current group node
		this.currentGroup = this.domTreeManager.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
	}

	/**
	 * Adds a node to the current group, if possible
	 * @param element child Element to add to the group
	 */
	public void addElement(final Element element) {
		this.addElement(element, (short) (DRAW | FILL));
	}

	/**
	 * Adds a node to the current group, if possible
	 * @param element child Element to add to the group
	 */
	public void addElement(final Element element, final short method) {
		//
		// If this is the first child to be added to the
		// currentGroup, 'freeze' the style attributes.
		//
		if (!this.currentGroup.hasChildNodes()) {
			this.currentGroup.appendChild(element);

			this.groupGC = this.domTreeManager.gcConverter.toSVG(this.gc);
			SVGGraphicContext deltaGC;
			deltaGC = this.processDeltaGC(this.groupGC, this.domTreeManager.defaultGC);
			this.domTreeManager.getStyleHandler().setStyle(this.currentGroup, deltaGC.getGroupContext(),
					this.domTreeManager.getGeneratorContext());
			if ((method & DRAW) == 0) {
				// force stroke:none
				deltaGC.getGraphicElementContext().put(SVG_STROKE_ATTRIBUTE, SVG_NONE_VALUE);
			}
			if ((method & FILL) == 0) {
				// force fill:none
				deltaGC.getGraphicElementContext().put(SVG_FILL_ATTRIBUTE, SVG_NONE_VALUE);
			}
			this.domTreeManager.getStyleHandler().setStyle(element, deltaGC.getGraphicElementContext(),
					this.domTreeManager.getGeneratorContext());
			this.setTransform(this.currentGroup, deltaGC.getTransformStack());
			this.domTreeManager.appendGroup(this.currentGroup, this);
		} else {
			if (this.gc.isTransformStackValid()) {
				//
				// There are children nodes already. Find
				// out delta between current gc and group
				// context
				//
				final SVGGraphicContext elementGC = this.domTreeManager.gcConverter.toSVG(this.gc);
				final SVGGraphicContext deltaGC = this.processDeltaGC(elementGC, this.groupGC);

				// If there are less than the maximum number
				// of differences, then add the node to the current
				// group and set its attributes
				this.trimContextForElement(deltaGC, element);
				if (this.countOverrides(deltaGC) <= this.domTreeManager.maxGCOverrides) {
					this.currentGroup.appendChild(element);
					// as there already are children we put all
					// attributes (group + element) on the element itself.
					if ((method & DRAW) == 0) {
						// force stroke:none
						deltaGC.getContext().put(SVG_STROKE_ATTRIBUTE, SVG_NONE_VALUE);
					}
					if ((method & FILL) == 0) {
						// force fill:none
						deltaGC.getContext().put(SVG_FILL_ATTRIBUTE, SVG_NONE_VALUE);
					}
					this.domTreeManager.getStyleHandler().setStyle(element, deltaGC.getContext(),
							this.domTreeManager.getGeneratorContext());
					this.setTransform(element, deltaGC.getTransformStack());
				} else {
					//
					// Need to create a new current group
					//
					this.currentGroup = this.domTreeManager.getDOMFactory().createElementNS(SVG_NAMESPACE_URI,
							SVG_G_TAG);
					this.addElement(element, method);
				}
			} else {
				//
				// Transform stack is invalid. Create a new current
				// group and validate the stack
				//
				this.currentGroup = this.domTreeManager.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
				this.gc.validateTransformStack();
				this.addElement(element, method);
			}
		}
	}

	/**
	 * Analyses the Map to define how many attributes constitute
	 * overrides. Only differences in the group context are considered
	 * overrides.
	 */
	protected int countOverrides(final SVGGraphicContext deltaGC) {
		return deltaGC.getGroupContext().size();
	}

	/**
	 * Removes properties that do not apply for a specific element
	 */
	protected void trimContextForElement(final SVGGraphicContext svgGC, final Element element) {
		final String tag = element.getTagName();
		final Map groupAttrMap = svgGC.getGroupContext();
		if (tag != null) {
			// For each attribute, check if there is an attribute
			// descriptor. If there is, check if the attribute
			// applies to the input element. If there is none,
			// assume the attribute applies to the element.
			final Iterator iter = groupAttrMap.keySet().iterator();
			while (iter.hasNext()) {
				final String attrName = (String) iter.next();
				final SVGAttribute attr = SVGAttributeMap.get(attrName);
				if (attr != null && !attr.appliesTo(tag))
					groupAttrMap.remove(attrName);
			}
		}
	}

	/**
	 * Processes the transform attribute value corresponding to a
	 * given transform stack
	 */
	protected void setTransform(final Element element, final TransformStackElement transformStack[]) {
		final String transform = this.domTreeManager.gcConverter.toSVG(transformStack).trim();
		if (transform.length() > 0)
			element.setAttributeNS(null, SVG_TRANSFORM_ATTRIBUTE, transform);
	}

	/**
	 * Processes the difference between two graphic contexts. The values
	 * in gc that are different from the values in referenceGc will be
	 * present in the delta. Other values will no.
	 */
	protected SVGGraphicContext processDeltaGC(final SVGGraphicContext gc, final SVGGraphicContext referenceGc) {
		final Map groupDelta = this.processDeltaMap(gc.getGroupContext(), referenceGc.getGroupContext());
		final Map graphicElementDelta = gc.getGraphicElementContext();

		final TransformStackElement gcTransformStack[] = gc.getTransformStack();
		final TransformStackElement referenceStack[] = referenceGc.getTransformStack();
		final int deltaStackLength = gcTransformStack.length - referenceStack.length;
		final TransformStackElement deltaTransformStack[] = new TransformStackElement[deltaStackLength];

		System.arraycopy(gcTransformStack, referenceStack.length, deltaTransformStack, 0, deltaStackLength);

		/**
		 System.err.println("gc transform stack length: " +
		 gc.getTransformStack().length);
		 System.err.println("reference stack length   : " +
		 referenceGc.getTransformStack().length);
		 System.err.println("delta stack length       : " +
		 deltaTransformStack.length);
		 */

		/*
		 TransformStackElement gcStack[] = gc.getTransformStack();
		 for(int i=0; i<gcStack.length; i++)
		 System.err.println("gcStack[" + i + "] = " + gcStack[i].toString());

		 TransformStackElement refStack[] = referenceGc.getTransformStack();
		 for(int i=0; i<refStack.length; i++)
		 System.err.println("refStack[" + i + "] = " + refStack[i].toString());

		 for(int i=0; i<deltaTransformStack.length; i++)
		 System.err.println("deltaStack[" + i + "] = " +
		 deltaTransformStack[i].toString());
		 */

		final SVGGraphicContext deltaGC = new SVGGraphicContext(groupDelta, graphicElementDelta, deltaTransformStack);

		return deltaGC;
	}

	/**
	 * Processes the difference between two Maps. The code assumes
	 * that the input Maps have the same key sets. Values in map that
	 * are different from values in referenceMap are place in the
	 * returned delta Map.
	 */
	protected Map processDeltaMap(final Map map, final Map referenceMap) {
		// no need to be synch => HashMap
		final Map mapDelta = new ConcurrentHashMap();
		final Iterator iter = map.keySet().iterator();
		while (iter.hasNext()) {
			final String key = (String) iter.next();
			final String value = (String) map.get(key);
			final String refValue = (String) referenceMap.get(key);
			if (!value.equals(refValue)) {
				/*if(key.equals(SVG_TRANSFORM_ATTRIBUTE)){
				 // Special handling for the transform attribute.
				 // At this point in the processing, the transform
				 // in map has to be a substring of the one in
				 // referenceMap. see the addElement member.
				 value = value.substring(refValue.length()).trim();
				 }*/
				mapDelta.put(key, value);
			}
		}
		return mapDelta;
	}
}
