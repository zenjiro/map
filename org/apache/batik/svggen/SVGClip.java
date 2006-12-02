/*

   Copyright 2001-2003  The Apache Software Foundation 

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

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Element;

/**
 * Utility class that converts a Path object into an SVG clip
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: SVGClip.java,v 1.14 2004/08/18 07:14:59 vhardy Exp $
 */
public class SVGClip extends AbstractSVGConverter {
    /**
     * Constant used for some degenerate cases
     */
    public static final Shape ORIGIN = new Line2D.Float(0,0,0,0);

    /**
     * Descriptor to use where there is no clip on an element
     */
    public static final SVGClipDescriptor NO_CLIP =
        new SVGClipDescriptor(SVG_NONE_VALUE, null);

    /**
     * Used to convert clip object to SVG elements
     */
    private SVGShape shapeConverter;

    /**
     * @param generatorContext used to build Elements
     */
    public SVGClip(final SVGGeneratorContext generatorContext) {
        super(generatorContext);
        this.shapeConverter = new SVGShape(generatorContext);
    }

    /**
     * Converts part or all of the input GraphicContext into
     * a set of attribute/value pairs and related definitions.
     * @param gc GraphicContext to be converted
     * @return descriptor of the attributes required to represent
     *         some or all of the GraphicContext state, along
     *         with the related definitions
     * @see org.apache.batik.svggen.SVGDescriptor
     */
    public SVGDescriptor toSVG(final GraphicContext gc) {
        final Shape clip = gc.getClip();

        SVGClipDescriptor clipDesc = null;

        if (clip != null) {
            final StringBuffer clipPathAttrBuf = new StringBuffer(URL_PREFIX);

            // First, convert to a GeneralPath so that the
            final GeneralPath clipPath = new GeneralPath(clip);

            // Check if this object is already in the Map
            final ClipKey clipKey = new ClipKey(clipPath, this.generatorContext);
            clipDesc = (SVGClipDescriptor)this.descMap.get(clipKey);

            if (clipDesc == null) {
                final Element clipDef = this.clipToSVG(clip);
                if (clipDef == null)
                    clipDesc = NO_CLIP;
                else {
                    clipPathAttrBuf.append(SIGN_POUND);
                    clipPathAttrBuf.append(clipDef.getAttributeNS(null, ATTR_ID));
                    clipPathAttrBuf.append(URL_SUFFIX);

                    clipDesc = new SVGClipDescriptor(clipPathAttrBuf.toString(),
                                                     clipDef);

                    this.descMap.put(clipKey, clipDesc);
                    this.defSet.add(clipDef);
                }
            }
        } else
            clipDesc = NO_CLIP;

        return clipDesc;
    }

    /**
     * In the following method, an clipping Shape is converted to
     * an SVG clipPath.
     *
     * @param clip path to convert to an SVG clipPath
     *        element
     */
    private Element clipToSVG(final Shape clip) {
        final Element clipDef =
            this.generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_CLIP_PATH_TAG);
        clipDef.setAttributeNS(null, SVG_CLIP_PATH_UNITS_ATTRIBUTE,
                               SVG_USER_SPACE_ON_USE_VALUE);

        clipDef.setAttributeNS(null, ATTR_ID,
                               this.generatorContext.
                               idGenerator.generateID(ID_PREFIX_CLIP_PATH));

        final Element clipPath = this.shapeConverter.toSVG(clip);
        // unfortunately it may be null because of SVGPath that may produce null
        // SVG elements.
        if (clipPath != null) {
            clipDef.appendChild(clipPath);
            return clipDef;
        } else {
            // Here, we know clip is not null but we got a
            // null clipDef. This means we ran into a degenerate 
            // case which in Java 2D means everything is clippped.
            // To provide an equivalent behavior, we clip to a point
            clipDef.appendChild(this.shapeConverter.toSVG(ORIGIN));
            return clipDef;
        }
    }
}

/**
 * Inner class used to key clip definitions in a Map.
 * This is needed because we need to test equality
 * on the value of GeneralPath and GeneralPath's equal
 * method does not implement that behavior.
 */
class ClipKey {
    /**
     * This clip hash code. Based on the serialized path
     * data
     */
    int hashCodeValue = 0;

    /**
     * @param proxiedPath path used as an index in the Map
     */
    public ClipKey(final GeneralPath proxiedPath, final SVGGeneratorContext gc){
        final String pathData = SVGPath.toSVGPathData(proxiedPath, gc);
        this.hashCodeValue = pathData.hashCode();
    }

    /**
     * @return this object's hashcode
     */
    public int hashCode() {
        return this.hashCodeValue;
    }

    /**
     * @param object to compare
     * @return true if equal, false otherwise
     */
    public boolean equals(final Object clipKey){
        boolean isEqual = false;
        if((clipKey != null) &&clipKey instanceof ClipKey)
            isEqual = (this.hashCodeValue == ((ClipKey)clipKey).hashCodeValue);

        return isEqual;
    }
}
