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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.util.LinkedList;
import java.util.List;

import org.apache.batik.ext.awt.g2d.GraphicContext;

/**
 * Utility class that converts a Composite object into
 * a set of SVG properties and definitions.
 * <p>Here is how Composites are mapped to SVG:</p>
 * <ul>
 *   <li>AlphaComposite.SRC_OVER with extra alpha is mapped
 *     to the opacity attribute</li>
 *   <li>AlphaComposite's other rules are translated into
 *     predefined filter effects.</li>
 *   <li>Custom Composite implementations are handled by the
 *     extension mechanism.</li>
 * </ul>
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: SVGComposite.java,v 1.9 2005/03/27 08:58:35 cam Exp $
 * @see                org.apache.batik.svggen.SVGAlphaComposite
 */
public class SVGComposite implements SVGConverter {
    /**
     * All AlphaComposite convertion is handed to svgAlphaComposite
     */
    private SVGAlphaComposite svgAlphaComposite;

    /**
     * All custom Composite convertion is handed to svgCustomComposite
     */
    private SVGCustomComposite svgCustomComposite;

    /**
     * Used to create DOM elements
     */
    private SVGGeneratorContext generatorContext;

    /**
     * @param generatorContext The generator context used for handling
     *        custom and alpha composites
     */
    public SVGComposite(final SVGGeneratorContext generatorContext) {
        this.svgAlphaComposite =  new SVGAlphaComposite(generatorContext);
        this.svgCustomComposite = new SVGCustomComposite(generatorContext);
        this.generatorContext = generatorContext;
    }

    /**
     * @return Set of filter Elements defining the composites this
     *         Converter has processed since it was created.
     */
    public List getDefinitionSet() {
        final List compositeDefs = new LinkedList(this.svgAlphaComposite.getDefinitionSet());
        compositeDefs.addAll(this.svgCustomComposite.getDefinitionSet());
        return compositeDefs;
    }

    public SVGAlphaComposite getAlphaCompositeConverter() {
        return this.svgAlphaComposite;
    }

    public SVGCustomComposite getCustomCompositeConverter() {
        return this.svgCustomComposite;
    }

    /**
     * Converts part or all of the input GraphicContext into
     * a set of attribute/value pairs and related definitions
     *
     * @param gc GraphicContext to be converted
     * @return descriptor of the attributes required to represent
     *         some or all of the GraphicContext state, along
     *         with the related definitions
     * @see org.apache.batik.svggen.SVGDescriptor
     */
    public SVGDescriptor toSVG(final GraphicContext gc) {
        return this.toSVG(gc.getComposite());
    }

    /**
     * @param composite Composite to be converted to SVG
     * @return an SVGCompositeDescriptor mapping the SVG composite
     *         equivalent of the input Composite
     */
    public SVGCompositeDescriptor toSVG(final Composite composite) {
        if (composite instanceof AlphaComposite)
            return this.svgAlphaComposite.toSVG((AlphaComposite)composite);
        else
            return this.svgCustomComposite.toSVG(composite);
    }
}
