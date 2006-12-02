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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.batik.ext.awt.g2d.TransformStackElement;
import org.apache.batik.util.SVGConstants;

/**
 * Represents the SVG equivalent of a Java 2D API graphic
 * context attribute.
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: SVGGraphicContext.java,v 1.7 2004/08/18 07:15:00 vhardy Exp $
 */
public class SVGGraphicContext implements SVGConstants, ErrorConstants {
    // this properties can only be set of leaf nodes =>
    // if they have default values they can be ignored
    private static final String leafOnlyAttributes[] = {
        SVG_OPACITY_ATTRIBUTE,
        SVG_FILTER_ATTRIBUTE,
        SVG_CLIP_PATH_ATTRIBUTE
    };

    private static final String defaultValues[] = {
        "1",
        SVG_NONE_VALUE,
        SVG_NONE_VALUE
    };

    private Map context;
    private Map groupContext;
    private Map graphicElementContext;
    private TransformStackElement transformStack[];

    /**
     * @param context Set of style attributes in this context.
     * @param transformStack Sequence of transforms that where
     *        applied to create the context's current transform.
     */
    public SVGGraphicContext(final Map context,
                             final TransformStackElement transformStack[]) {
        if (context == null)
            throw new SVGGraphics2DRuntimeException(ERR_MAP_NULL);
        if (transformStack == null)
            throw new SVGGraphics2DRuntimeException(ERR_TRANS_NULL);
        this.context = context;
        this.transformStack = transformStack;
        this.computeGroupAndGraphicElementContext();
    }

    /**
     * @param groupContext Set of attributes that apply to group
     * @param graphicElementContext Set of attributes that apply to
     *        elements but not to groups (e.g., opacity, filter).
     * @param transformStack Sequence of transforms that where
     *        applied to create the context's current transform.
     */
    public SVGGraphicContext(final Map groupContext, final Map graphicElementContext,
                             final TransformStackElement transformStack[]) {
        if (groupContext == null || graphicElementContext == null)
            throw new SVGGraphics2DRuntimeException(ERR_MAP_NULL);
        if (transformStack == null)
            throw new SVGGraphics2DRuntimeException(ERR_TRANS_NULL);

        this.groupContext = groupContext;
        this.graphicElementContext = graphicElementContext;
        this.transformStack = transformStack;
        this.computeContext();
    }


    /**
     * @return set of all attributes.
     */
    public Map getContext() {
        return this.context;
    }

    /**
     * @return set of attributes that can be set on a group
     */
    public Map getGroupContext() {
        return this.groupContext;
    }

    /**
     * @return set of attributes that can be set on leaf node
     */
    public Map getGraphicElementContext() {
        return this.graphicElementContext;
    }

    /**
     * @return set of TransformStackElement for this context
     */
    public TransformStackElement[] getTransformStack() {
        return this.transformStack;
    }

    private void computeContext() {
        if (this.context != null)
            return;

        this.context = new ConcurrentHashMap(this.groupContext);
        this.context.putAll(this.graphicElementContext);
    }

    private void computeGroupAndGraphicElementContext() {
        if (this.groupContext != null)
            return;
        //
        // Now, move attributes that only apply to
        // leaf elements to a separate map.
        //
        this.groupContext = new ConcurrentHashMap(this.context);
        this.graphicElementContext = new ConcurrentHashMap();
        for (int i=0; i< leafOnlyAttributes.length; i++) {
            final Object attrValue = this.groupContext.get(leafOnlyAttributes[i]);
            if (attrValue != null){
                if (!attrValue.equals(defaultValues[i]))
                    this.graphicElementContext.put(leafOnlyAttributes[i], attrValue);
                this.groupContext.remove(leafOnlyAttributes[i]);
            }
        }
    }
}
