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

import java.awt.Polygon;
import java.awt.geom.PathIterator;

import org.w3c.dom.Element;

/**
 * Utility class that converts a Polygon object into
 * an SVG element.
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: SVGPolygon.java,v 1.10 2004/08/18 07:15:08 vhardy Exp $
 */
public class SVGPolygon extends SVGGraphicObjectConverter {
    /**
     * @param generatorContext used to build Elements
     */
    public SVGPolygon(final SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }

    /**
     * @param polygon polygon object to convert to SVG
     */
    public Element toSVG(final Polygon polygon) {
        final Element svgPolygon =
            this.generatorContext.domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                        SVG_POLYGON_TAG);
        final StringBuffer points = new StringBuffer(" ");
        final PathIterator pi = polygon.getPathIterator(null);
        final float seg[] = new float[6];
        int segType = 0;
        while(!pi.isDone()){
            segType = pi.currentSegment(seg);
            switch(segType){
            case PathIterator.SEG_MOVETO:
                this.appendPoint(points, seg[0], seg[1]);
                break;
            case PathIterator.SEG_LINETO:
                this.appendPoint(points, seg[0], seg[1]);
                break;
            case PathIterator.SEG_CLOSE:
                break;
            case PathIterator.SEG_QUADTO:
            case PathIterator.SEG_CUBICTO:
            default:
                throw new Error();
            }
            pi.next();
        } // while !isDone

        svgPolygon.setAttributeNS(null,
                                  SVG_POINTS_ATTRIBUTE,
                                  points.substring(0, points.length() - 1));

        return svgPolygon;
    }

    /**
     *  Appends a coordinate to the path data
     */
    private void appendPoint(final StringBuffer points, final float x, final float y){
        points.append(this.doubleString(x));
        points.append(SPACE);
        points.append(this.doubleString(y));
        points.append(SPACE);
    }
}
