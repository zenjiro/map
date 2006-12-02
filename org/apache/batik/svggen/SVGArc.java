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

import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.w3c.dom.Element;

/**
 * Utility class that converts an Arc2D object into
 * a corresponding SVG element, i.e., a path with an elliptical arc
 * and optionally lines..
 *
 * @author <a href="mailto:thomas.deweese@kodak.com">Thomas DeWeese</a>
 * @version $Id: SVGArc.java,v 1.1 2004/10/31 11:21:30 deweese Exp $
 */
public class SVGArc extends SVGGraphicObjectConverter {
    /**
     * Line converter used for degenerate cases
     */
    private SVGLine svgLine;

    /**
     * @param generatorContext used to build Elements
     */
    public SVGArc(final SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }

    /**
     * @param arc the Arc2D object to be converted
     */
    public Element toSVG(final Arc2D arc) {
        if ((arc.getWidth() == 0) ||  (arc.getHeight() == 0)) {
            final Line2D line = new Line2D.Double
                (arc.getX(), arc.getY(), 
                 arc.getX() + arc.getWidth(), 
                 arc.getY() + arc.getHeight());
            if (this.svgLine == null)
                this.svgLine = new SVGLine(this.generatorContext);
            return this.svgLine.toSVG(line);
        }

        final Element svgPath = this.generatorContext.domFactory.createElementNS
            (SVG_NAMESPACE_URI, SVG_PATH_TAG);
        final StringBuffer d = new StringBuffer("");

        final Point2D startPt = arc.getStartPoint();
        final Point2D endPt   = arc.getEndPoint();
        final double  ext     = arc.getAngleExtent();
        final int     type    = arc.getArcType();

        d.append(PATH_MOVE);
        d.append(this.doubleString(startPt.getX()));
        d.append(SPACE);
        d.append(this.doubleString(startPt.getY()));
        d.append(SPACE);

        d.append(PATH_ARC);
        d.append(this.doubleString(arc.getWidth()/2));
        d.append(SPACE);
        d.append(this.doubleString(arc.getHeight()/2));
        d.append(SPACE);
        d.append("0");  // no rotation with J2D arc.
        d.append(SPACE);
        if (ext > 180)  d.append("1");  // use large arc.
        else            d.append("0");  // use small arc.
        d.append(SPACE);
        if (ext > 0)    d.append("0");  // sweep ccw
        else            d.append("1");  // sweep cw

        d.append(SPACE);
        d.append(this.doubleString(endPt.getX()));
        d.append(SPACE);
        d.append(this.doubleString(endPt.getY()));

        if (type == Arc2D.CHORD) {
            d.append(PATH_CLOSE);
        } else if (type == Arc2D.PIE) {
            final double cx = arc.getX()+arc.getWidth()/2;
            final double cy = arc.getY()+arc.getHeight()/2;
            d.append(PATH_LINE_TO);
            d.append(SPACE);
            d.append(this.doubleString(cx));
            d.append(SPACE);
            d.append(this.doubleString(cy));
            d.append(SPACE);
            d.append(PATH_CLOSE);
        }
        svgPath.setAttributeNS(null, SVG_D_ATTRIBUTE, d.toString());
        return svgPath;
    }
}
