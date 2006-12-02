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

import org.w3c.dom.Element;

/**
 * Describes an SVG font
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: SVGFontDescriptor.java,v 1.8 2004/08/18 07:15:00 vhardy Exp $
 * @see             org.apache.batik.svggen.SVGFont
 */
public class SVGFontDescriptor implements SVGDescriptor, SVGSyntax {
    private Element def;
    private String fontSize;
    private String fontWeight;
    private String fontStyle;
    private String fontFamily;

    /**
     * Constructor
     */
    public SVGFontDescriptor(final String fontSize,
                             final String fontWeight,
                             final String fontStyle,
                             final String fontFamily,
                             final Element def){
        if (fontSize == null ||
            fontWeight == null ||
            fontStyle == null ||
            fontFamily == null)
            throw new SVGGraphics2DRuntimeException(ErrorConstants.ERR_FONT_NULL);

        this.fontSize = fontSize;
        this.fontWeight = fontWeight;
        this.fontStyle = fontStyle;
        this.fontFamily = fontFamily;
        this.def = def;
    }

    public Map getAttributeMap(Map attrMap){
        if(attrMap == null)
            attrMap = new ConcurrentHashMap();

        attrMap.put(SVG_FONT_SIZE_ATTRIBUTE, this.fontSize);
        attrMap.put(SVG_FONT_WEIGHT_ATTRIBUTE, this.fontWeight);
        attrMap.put(SVG_FONT_STYLE_ATTRIBUTE, this.fontStyle);
        attrMap.put(SVG_FONT_FAMILY_ATTRIBUTE, this.fontFamily);

        return attrMap;
    }

    public Element getDef(){
        return this.def;
    }

    public List getDefinitionSet(List defSet){
        if (defSet == null)
            defSet = new LinkedList();

        if(this.def != null && !defSet.contains(this.def))
            defSet.add(this.def);

        return defSet;
    }
}
