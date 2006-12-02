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

import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;

import org.w3c.dom.Element;

/**
 * Utility class that converts an custom BufferedImageOp object into
 * an equivalent SVG filter.
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: SVGCustomBufferedImageOp.java,v 1.7 2004/08/18 07:14:59 vhardy Exp $
 * @see                org.apache.batik.svggen.SVGBufferedImageOp
 */
public class SVGCustomBufferedImageOp extends AbstractSVGFilterConverter {
    private static final String ERROR_EXTENSION =
        "SVGCustomBufferedImageOp:: ExtensionHandler could not convert filter";

    /**
     * @param generatorContext for use by SVGCustomBufferedImageOp to
     * build Elements.
     */
    public SVGCustomBufferedImageOp(final SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }

    /**
     * @param filter the BufferedImageOp object to convert to SVG
     * @param filterRect Rectangle, in device space, that defines the area
     *        to which filtering applies. May be null, meaning that the
     *        area is undefined.
     * @return an SVGFilterDescriptor mapping the SVG
     *         BufferedImageOp equivalent to the input BufferedImageOp.
     */
    public SVGFilterDescriptor toSVG(final BufferedImageOp filter,
                                     final Rectangle filterRect) {
        SVGFilterDescriptor filterDesc =
            (SVGFilterDescriptor)this.descMap.get(filter);

        if (filterDesc == null) {
            // First time this filter is used. Request handler
            // to do the convertion
            filterDesc =
                this.generatorContext.extensionHandler.
                handleFilter(filter, filterRect, this.generatorContext);

            if (filterDesc != null) {
                final Element def = filterDesc.getDef();
                if(def != null)
                    this.defSet.add(def);
                this.descMap.put(filter, filterDesc);
            } else {
                System.err.println(ERROR_EXTENSION);
            }
        }

        return filterDesc;
    }

}

