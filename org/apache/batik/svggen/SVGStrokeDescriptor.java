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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to represent an SVG Paint. This can be achieved with
 * to values: an SVG paint value and an SVG opacity value
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: SVGStrokeDescriptor.java,v 1.7 2004/08/18 07:15:09 vhardy Exp $
 */
public class SVGStrokeDescriptor implements SVGDescriptor, SVGSyntax{
    private String strokeWidth;
    private String capStyle;
    private String joinStyle;
    private String miterLimit;
    private String dashArray;
    private String dashOffset;


    public SVGStrokeDescriptor(final String strokeWidth, final String capStyle,
                               final String joinStyle, final String miterLimit,
                               final String dashArray, final String dashOffset){
        if(strokeWidth == null ||
           capStyle == null    ||
           joinStyle == null   ||
           miterLimit == null  ||
           dashArray == null   ||
           dashOffset == null)
            throw new SVGGraphics2DRuntimeException(ErrorConstants.ERR_STROKE_NULL);

        this.strokeWidth = strokeWidth;
        this.capStyle = capStyle;
        this.joinStyle = joinStyle;
        this.miterLimit = miterLimit;
        this.dashArray = dashArray;
        this.dashOffset = dashOffset;
    }

    String getStrokeWidth(){ return this.strokeWidth; }
    String getCapStyle(){ return this.capStyle; }
    String getJoinStyle(){ return this.joinStyle; }
    String getMiterLimit(){ return this.miterLimit; }
    String getDashArray(){ return this.dashArray; }
    String getDashOffset(){ return this.dashOffset; }

    public Map getAttributeMap(Map attrMap){
        if(attrMap == null)
            attrMap = new ConcurrentHashMap();

        attrMap.put(SVG_STROKE_WIDTH_ATTRIBUTE, this.strokeWidth);
        attrMap.put(SVG_STROKE_LINECAP_ATTRIBUTE, this.capStyle);
        attrMap.put(SVG_STROKE_LINEJOIN_ATTRIBUTE, this.joinStyle);
        attrMap.put(SVG_STROKE_MITERLIMIT_ATTRIBUTE, this.miterLimit);
        attrMap.put(SVG_STROKE_DASHARRAY_ATTRIBUTE, this.dashArray);
        attrMap.put(SVG_STROKE_DASHOFFSET_ATTRIBUTE, this.dashOffset);

        return attrMap;
    }

    public List getDefinitionSet(List defSet){
        if(defSet == null)
            defSet = new LinkedList();

        return defSet;
    }
}
