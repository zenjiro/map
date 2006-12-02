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
 * Used to represent an SVG Composite. This can be achieved with
 * to values: an SVG opacity and a filter
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: SVGCompositeDescriptor.java,v 1.6 2004/08/18 07:14:59 vhardy Exp $
 */
public class SVGCompositeDescriptor implements SVGDescriptor, SVGSyntax{
    private Element def;
    private String opacityValue;
    private String filterValue;

    public SVGCompositeDescriptor(final String opacityValue,
                                  final String filterValue){
        this.opacityValue = opacityValue;
        this.filterValue = filterValue;
    }

    public SVGCompositeDescriptor(final String opacityValue,
                                  final String filterValue,
                                  final Element def){
        this(opacityValue, filterValue);
        this.def = def;
    }

    public String getOpacityValue(){
        return this.opacityValue;
    }

    public String getFilterValue(){
        return this.filterValue;
    }

    public Element getDef(){
        return this.def;
    }

    public Map getAttributeMap(Map attrMap) {
        if(attrMap == null)
            attrMap = new ConcurrentHashMap();

        attrMap.put(SVG_OPACITY_ATTRIBUTE, this.opacityValue);
        attrMap.put(SVG_FILTER_ATTRIBUTE, this.filterValue);

        return attrMap;
    }

    public List getDefinitionSet(List defSet) {
        if (defSet == null)
            defSet = new LinkedList();

        if (this.def != null)
            defSet.add(this.def);

        return defSet;
    }
}
