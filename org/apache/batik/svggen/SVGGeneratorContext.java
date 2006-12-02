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

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.w3c.dom.Document;

/**
 * This class contains all non graphical contextual information that
 * are needed by the {@link org.apache.batik.svggen.SVGGraphics2D} to
 * generate SVG from Java 2D primitives.
 * You can subclass it to change the defaults.
 *
 * @see org.apache.batik.svggen.SVGGraphics2D#SVGGraphics2D(SVGGeneratorContext,boolean)
 * @author <a href="mailto:cjolif@ilog.fr">Christophe Jolif</a>
 * @version $Id: SVGGeneratorContext.java,v 1.21 2005/03/27 08:58:35 cam Exp $
 */
public class SVGGeneratorContext implements ErrorConstants {
    // this fields are package access for read-only purpose

    /**
     * Factory used by this Graphics2D to create Elements
     * that make the SVG DOM Tree
     */
    Document domFactory;

    /**
     * Handler that defines how images are referenced in the
     * generated SVG fragment. This allows different strategies
     * to be used to handle images.
     * @see org.apache.batik.svggen.ImageHandler
     * @see org.apache.batik.svggen.ImageHandlerBase64Encoder
     * @see org.apache.batik.svggen.ImageHandlerPNGEncoder
     * @see org.apache.batik.svggen.ImageHandlerJPEGEncoder
     */
    ImageHandler imageHandler;

    /**
     * Generic image handler. This allows more sophisticated 
     * image handling strategies than the <tt>ImageHandler</tt>
     * interfaces.
     */
    GenericImageHandler genericImageHandler;

    /**
     * To deal with Java 2D extension (custom java.awt.Paint for example).
     */
    ExtensionHandler extensionHandler;

    /**
     * To generate consitent ids.
     */
    SVGIDGenerator idGenerator;

    /**
     * To set style.
     */
    StyleHandler styleHandler;

    /**
     * The comment to insert at generation time.
     */
    String generatorComment;

    /**
     * The error handler.
     */
    ErrorHandler errorHandler;

    /**
     * Do we accept SVG Fonts generation?
     */
    boolean svgFont = false;

    /**
     * GraphicContextDefaults
     */
    GraphicContextDefaults gcDefaults;

    /**
     * Number of decimal places to use in output values.
     * 3 decimal places are used by default.
     */
    int precision;

    /**
     * Class to describe the GraphicContext defaults to
     * be used. Note that this class does *not* contain
     * a default for the initial transform, as this 
     * transform *has to be identity* for the SVGGraphics2D
     * to operate (the TransformStacks operation is based
     * on that assumption. See the DOMTreeManager class).
     */
    public static class GraphicContextDefaults {
        protected Paint paint;
        protected Stroke stroke;
        protected Composite composite;
        protected Shape clip;
        protected RenderingHints hints;
        protected Font font;
        protected Color background; 

        public void setStroke(final Stroke stroke){
            this.stroke = stroke;
        }

        public Stroke getStroke(){
            return this.stroke;
        }

        public void setComposite(final Composite composite){
            this.composite = composite;
        }

        public Composite getComposite(){
            return this.composite;
        }

        public void setClip(final Shape clip){
            this.clip = clip;
        }

        public Shape getClip(){
            return this.clip;
        }

        public void setRenderingHints(final RenderingHints hints){
            this.hints = hints;
        }

        public RenderingHints getRenderingHints(){
            return this.hints;
        }

        public void setFont(final Font font){
            this.font = font;
        }

        public Font getFont(){
            return this.font;
        }

        public void setBackground(final Color background){
            this.background = background;
        }

        public Color getBackground(){
            return this.background;
        }

        public void setPaint(final Paint paint){
            this.paint = paint;
        }

        public Paint getPaint(){
            return this.paint;
        }
    }

    /**
     * Builds an instance of <code>SVGGeneratorContext</code> with the given
     * <code>domFactory</code> but let the user set later the other contextual
     * information. Please note that none of the parameter below should be
     * <code>null</code>.
     * @see #setIDGenerator
     * @see #setExtensionHandler
     * @see #setImageHandler
     * @see #setStyleHandler
     * @see #setErrorHandler
     */
    protected SVGGeneratorContext(final Document domFactory) {
        this.setDOMFactory(domFactory);
    }

    /**
     * Creates an instance of <code>SVGGeneratorContext</code> with the
     * given <code>domFactory</code> and with the default values for the
     * other information.
     * @see org.apache.batik.svggen.SVGIDGenerator
     * @see org.apache.batik.svggen.DefaultExtensionHandler
     * @see org.apache.batik.svggen.ImageHandlerBase64Encoder
     * @see org.apache.batik.svggen.DefaultStyleHandler
     * @see org.apache.batik.svggen.DefaultErrorHandler
     */
    public static SVGGeneratorContext createDefault(final Document domFactory) {
        final SVGGeneratorContext ctx = new SVGGeneratorContext(domFactory);
        ctx.setIDGenerator(new SVGIDGenerator());
        ctx.setExtensionHandler(new DefaultExtensionHandler());
        ctx.setImageHandler(new ImageHandlerBase64Encoder());
        ctx.setStyleHandler(new DefaultStyleHandler());
        ctx.setComment("Generated by the Batik Graphics2D SVG Generator");
        ctx.setErrorHandler(new DefaultErrorHandler());
        return ctx;
    }

    /**
     * Returns the set of defaults which should be used for the 
     * GraphicContext.
     */
    final public GraphicContextDefaults getGraphicContextDefaults(){
        return this.gcDefaults;
    }

    /**
     * Sets the default to be used for the graphic context.
     * Note that gcDefaults may be null and that any of its attributes
     * may be null.
     */
    final public void setGraphicContextDefaults(final GraphicContextDefaults gcDefaults){
        this.gcDefaults = gcDefaults;
    }

    /**
     * Returns the {@link org.apache.batik.svggen.SVGIDGenerator} that
     * has been set.
     */
    final public SVGIDGenerator getIDGenerator() {
        return this.idGenerator;
    }

    /**
     * Sets the {@link org.apache.batik.svggen.SVGIDGenerator}
     * to be used. It should not be <code>null</code>.
     */
    final public void setIDGenerator(final SVGIDGenerator idGenerator) {
        if (idGenerator == null)
            throw new SVGGraphics2DRuntimeException(ERR_ID_GENERATOR_NULL);
        this.idGenerator = idGenerator;
    }

    /**
     * Returns the DOM Factory that
     * has been set.
     */
    final public Document getDOMFactory() {
        return this.domFactory;
    }

    /**
     * Sets the DOM Factory
     * to be used. It should not be <code>null</code>.
     */
    final public void setDOMFactory(final Document domFactory) {
        if (domFactory == null)
            throw new SVGGraphics2DRuntimeException(ERR_DOM_FACTORY_NULL);
        this.domFactory = domFactory;
    }

    /**
     * Returns the {@link org.apache.batik.svggen.ExtensionHandler} that
     * has been set.
     */
    final public ExtensionHandler getExtensionHandler() {
        return this.extensionHandler;
    }

    /**
     * Sets the {@link org.apache.batik.svggen.ExtensionHandler}
     * to be used. It should not be <code>null</code>.
     */
    final public void setExtensionHandler(final ExtensionHandler extensionHandler) {
        if (extensionHandler == null)
            throw new SVGGraphics2DRuntimeException(ERR_EXTENSION_HANDLER_NULL);
        this.extensionHandler = extensionHandler;
    }

    /**
     * Returns the {@link org.apache.batik.svggen.ImageHandler} that
     * has been set.
     */
    final public ImageHandler getImageHandler() {
        return this.imageHandler;
    }

    /**
     * Sets the {@link org.apache.batik.svggen.ImageHandler}
     * to be used. It should not be <code>null</code>.
     */
    final public void setImageHandler(final ImageHandler imageHandler) {
        if (imageHandler == null)
            throw new SVGGraphics2DRuntimeException(ERR_IMAGE_HANDLER_NULL);
        this.imageHandler = imageHandler;
        this.genericImageHandler = new SimpleImageHandler(imageHandler);
    }

    /**
     * Sets the {@link org.apache.batik.svggen.GenericImageHandler}
     * to be used. 
     */
    final public void setGenericImageHandler(final GenericImageHandler genericImageHandler){
        if (genericImageHandler == null){
            throw new SVGGraphics2DRuntimeException(ERR_IMAGE_HANDLER_NULL);
        }
        this.imageHandler = null;
        this.genericImageHandler = genericImageHandler;
    }

    /**
     * Returns the {@link org.apache.batik.svggen.StyleHandler} that
     * has been set.
     */
    final public StyleHandler getStyleHandler() {
        return this.styleHandler;
    }

    /**
     * Sets the {@link org.apache.batik.svggen.StyleHandler}
     * to be used. It should not be <code>null</code>.
     */
    final public void setStyleHandler(final StyleHandler styleHandler) {
        if (styleHandler == null)
            throw new SVGGraphics2DRuntimeException(ERR_STYLE_HANDLER_NULL);
        this.styleHandler = styleHandler;
    }

    /**
     * Returns the comment to be generated in the SVG file.
     */
    final public String getComment() {
        return this.generatorComment;
    }

    /**
     * Sets the comment to be used. It can be <code>null</code> if you
     * want to disable it.
     */
    final public void setComment(final String generatorComment) {
        this.generatorComment = generatorComment;
    }

    /**
     * Returns the {@link org.apache.batik.svggen.ErrorHandler} that
     * has been set.
     */
    final public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    /**
     * Sets the {@link org.apache.batik.svggen.ErrorHandler}
     * to be used. It should not be <code>null</code>.
     */
    final public void setErrorHandler(final ErrorHandler errorHandler) {
        if (errorHandler == null)
            throw new SVGGraphics2DRuntimeException(ERR_ERROR_HANDLER_NULL);
        this.errorHandler = errorHandler;
    }

    /**
     * Returns <code>true</code> if we should generate SVG Fonts for
     * texts.
     */
    final public boolean isEmbeddedFontsOn() {
        return this.svgFont;
    }

    /**
     * Sets if we should generate SVG Fonts for texts. Default value
     * is <code>false</code>.
     */
    final public void setEmbeddedFontsOn(final boolean svgFont) {
        this.svgFont = svgFont;
    }

    /**
     * Returns the current precision used by this context
     */
    final public int getPrecision() {
        return this.precision;
    }

    /**
     * Sets the precision used by this context. The precision controls
     * the number of decimal places used in floating point values
     * output by the SVGGraphics2D generator.
     * Note that the precision is clipped to the [0,12] range.
     */
    final public void setPrecision(final int precision) {
        if (precision < 0) {
            this.precision = 0;
        } else if (precision > 12) {
            this.precision = 12;
        } else {
            this.precision = precision;
        }
        this.decimalFormat = decimalFormats[this.precision];
    }

    /**
     * Converts the input double value to a string with a number of 
     * decimal places controlled by the precision attribute.
     */
    final public String doubleString(final double value) {
        final double absvalue = Math.abs(value);
        // above 10e7 we do not output decimals as anyway
        // in scientific notation they were not available
        if (absvalue >= 10e7 || (int)value == value) {
            return Integer.toString((int)value);
        }
        // under 10e-3 we have to put decimals
        else {
            return this.decimalFormat.format(value);
        } 
    }

    /**
     * Current double value formatter
     */
    protected DecimalFormat decimalFormat = decimalFormats[3];

    protected static DecimalFormatSymbols dsf 
        = new DecimalFormatSymbols(Locale.US);

    protected static DecimalFormat decimalFormats[] = new DecimalFormat[13];

    static {
        decimalFormats[0] = new DecimalFormat("#", dsf);

        String format = "#.";
        for (int i=0; i<=12; i++) {
            format += "#";
            decimalFormats[i] = new DecimalFormat(format, dsf);
        }
    }

}
