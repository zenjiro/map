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

import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.batik.ext.awt.g2d.TransformStackElement;

/**
 * This class performs the task of converting the state of the
 * Java 2D API graphic context into a set of graphic attributes.
 * It also manages a set of SVG definitions referenced by the
 * SVG attributes.
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: SVGGraphicContextConverter.java,v 1.14 2004/08/18 07:15:00 vhardy Exp $
 */
public class SVGGraphicContextConverter {
    private static final int GRAPHIC_CONTEXT_CONVERTER_COUNT = 6;

    private SVGTransform transformConverter;
    private SVGPaint paintConverter;
    private SVGBasicStroke strokeConverter;
    private SVGComposite compositeConverter;
    private SVGClip clipConverter;
    private SVGRenderingHints hintsConverter;
    private SVGFont fontConverter;
    private final SVGConverter converters[] =
        new SVGConverter[GRAPHIC_CONTEXT_CONVERTER_COUNT];

    public SVGTransform getTransformConverter() { return this.transformConverter; }
    public SVGPaint getPaintConverter(){ return this.paintConverter; }
    public SVGBasicStroke getStrokeConverter(){ return this.strokeConverter; }
    public SVGComposite getCompositeConverter(){ return this.compositeConverter; }
    public SVGClip getClipConverter(){ return this.clipConverter; }
    public SVGRenderingHints getHintsConverter(){ return this.hintsConverter; }
    public SVGFont getFontConverter(){ return this.fontConverter; }

    /**
     * @param generatorContext the context that will be used to create
     * elements, handle extension and images.
     */
    public SVGGraphicContextConverter(final SVGGeneratorContext generatorContext) {
        if (generatorContext == null)
            throw new SVGGraphics2DRuntimeException(ErrorConstants.ERR_CONTEXT_NULL);

        this.transformConverter = new SVGTransform(generatorContext);
        this.paintConverter = new SVGPaint(generatorContext);
        this.strokeConverter = new SVGBasicStroke(generatorContext);
        this.compositeConverter = new SVGComposite(generatorContext);
        this.clipConverter = new SVGClip(generatorContext);
        this.hintsConverter = new SVGRenderingHints(generatorContext);
        this.fontConverter = new SVGFont(generatorContext);

        int i=0;
        this.converters[i++] = this.paintConverter;
        this.converters[i++] = this.strokeConverter;
        this.converters[i++] = this.compositeConverter;
        this.converters[i++] = this.clipConverter;
        this.converters[i++] = this.hintsConverter;
        this.converters[i++] = this.fontConverter;
    }

    /**
     * @return a String containing the transform attribute value
     *         equivalent of the input transform stack.
     */
    public String toSVG(final TransformStackElement transformStack[]) {
        return this.transformConverter.toSVGTransform(transformStack);
    }

    /**
     * @return an object that describes the set of SVG attributes that
     *         represent the equivalent of the input GraphicContext state.
     */
    public SVGGraphicContext toSVG(final GraphicContext gc) {
        // no need for synchronized map => use HashMap
        final Map groupAttrMap = new ConcurrentHashMap();

        for (int i=0; i<this.converters.length; i++) {
            final SVGDescriptor desc = this.converters[i].toSVG(gc);
            if (desc != null)
                desc.getAttributeMap(groupAttrMap);
        }

        // the ctor will to the splitting (group/element) job
        return new SVGGraphicContext(groupAttrMap,
                                     gc.getTransformStack());
    }

    /**
     * @return a set of element containing definitions for the attribute
     *         values generated by this converter since its creation.
     */
    public List getDefinitionSet() {
        final List defSet = new LinkedList();
        for(int i=0; i<this.converters.length; i++)
            defSet.addAll(this.converters[i].getDefinitionSet());

        return defSet;
    }
}
