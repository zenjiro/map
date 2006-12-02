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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.w3c.dom.Element;

/**
 * This class is a default implementation of the GenericImageHandler
 * for handlers implementing a caching strategy.
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: DefaultCachedImageHandler.java,v 1.8 2004/08/18 07:14:59 vhardy Exp $
 * @see             org.apache.batik.svggen.SVGGraphics2D
 */
public abstract class DefaultCachedImageHandler 
    implements CachedImageHandler, 
               SVGSyntax,
               ErrorConstants {

    // duplicate the string here to remove dependencies on
    // org.apache.batik.dom.util.XLinkSupport
    static final String XLINK_NAMESPACE_URI =
        "http://www.w3.org/1999/xlink";

    static final AffineTransform IDENTITY = new AffineTransform();

    // for createGraphics method.
    private static Method createGraphics = null;
    private static boolean initDone = false;
    private final static Class[] paramc = new Class[] {BufferedImage.class};
    private static Object[] paramo = null;

    protected ImageCacher imageCacher;

    /**
     * The image cache can be used by subclasses for efficient image storage
     */
    public ImageCacher getImageCacher() {
        return this.imageCacher;
    }

    void setImageCacher(final ImageCacher imageCacher) {
        if (imageCacher == null){
            throw new IllegalArgumentException();
        }

        // Save current DOMTreeManager if any
        DOMTreeManager dtm = null;
        if (this.imageCacher != null){
            dtm = this.imageCacher.getDOMTreeManager();
        }

        this.imageCacher = imageCacher;
        if (dtm != null){
            this.imageCacher.setDOMTreeManager(dtm);
        }
    }

    /**
     * This <tt>GenericImageHandler</tt> implementation does not
     * need to interact with the DOMTreeManager.
     */
    public void setDOMTreeManager(final DOMTreeManager domTreeManager){
        this.imageCacher.setDOMTreeManager(domTreeManager);
    }

    /**
     * This method creates a <code>Graphics2D</code> from a
     * <code>BufferedImage</code>. If Batik extensions to AWT are
     * in the CLASSPATH it uses them, otherwise, it uses the regular
     * AWT method.
     */
    private static Graphics2D createGraphics(final BufferedImage buf) {
        if (!initDone) {
            try {
                final Class clazz = Class.forName("org.apache.batik.ext.awt.image.GraphicsUtil");
                createGraphics = clazz.getMethod("createGraphics", paramc);
                paramo = new Object[1];
            } catch (final Throwable t) {
                // happen only if Batik extensions are not their
            } finally {
                initDone = true;
            }
        }
        if (createGraphics == null)
            return buf.createGraphics();
        else {
            paramo[0] = buf;
            Graphics2D g2d = null;
            try {
                g2d = (Graphics2D)createGraphics.invoke(null, paramo);
            } catch (final Exception e) {
                // should not happened
            }
            return g2d;
        }
    }

    /**
     * Creates an Element which can refer to an image.
     * Note that no assumptions should be made by the caller about the
     * corresponding SVG tag. By default, an &lt;image&gt; tag is 
     * used, but the {@link CachedImageHandlerBase64Encoder}, for
     * example, overrides this method to use a different tag.
     */
    public Element createElement(final SVGGeneratorContext generatorContext) {
        // Create a DOM Element in SVG namespace to refer to an image
        final Element imageElement =
            generatorContext.getDOMFactory().createElementNS
            (SVG_NAMESPACE_URI, SVG_IMAGE_TAG);

        return imageElement;
    }

    /**
     * The handler sets the xlink:href tag and returns a transform
     */
    public AffineTransform handleImage(final Image image,
                                       final Element imageElement,
                                       final int x, final int y,
                                       final int width, final int height,
                                       final SVGGeneratorContext generatorContext) {

        final int imageWidth      = image.getWidth(null);
        final int imageHeight     = image.getHeight(null);
        AffineTransform af  = null;

        if(imageWidth == 0 || imageHeight == 0 ||
           width == 0 || height == 0) {

            // Forget about it
            this.handleEmptyImage(imageElement);

        } else {
            // First set the href
            try {
                this.handleHREF(image, imageElement, generatorContext);
            } catch (final SVGGraphics2DIOException e) {
                try {
                    generatorContext.errorHandler.handleError(e);
                } catch (final SVGGraphics2DIOException io) {
                    // we need a runtime exception because
                    // java.awt.Graphics2D method doesn't throw exceptions..
                    throw new SVGGraphics2DRuntimeException(io);
                }
            }

            // Then create the transformation:
            // Because we cache image data, the stored image may
            // need to be scaled.
            af = this.handleTransform(imageElement, x, y, imageWidth, imageHeight,
                                 width, height, generatorContext);
        }
        return af;
    }

    /**
     * The handler sets the xlink:href tag and returns a transform
     */
    public AffineTransform handleImage(final RenderedImage image,
                                       final Element imageElement,
                                       final int x, final int y,
                                       final int width, final int height,
                                       final SVGGeneratorContext generatorContext) {

        final int imageWidth      = image.getWidth();
        final int imageHeight     = image.getHeight();
        AffineTransform af  = null;

        if(imageWidth == 0 || imageHeight == 0 ||
           width == 0 || height == 0) {

            // Forget about it
            this.handleEmptyImage(imageElement);

        } else {
            // First set the href
            try {
                this.handleHREF(image, imageElement, generatorContext);
            } catch (final SVGGraphics2DIOException e) {
                try {
                    generatorContext.errorHandler.handleError(e);
                } catch (final SVGGraphics2DIOException io) {
                    // we need a runtime exception because
                    // java.awt.Graphics2D method doesn't throw exceptions..
                    throw new SVGGraphics2DRuntimeException(io);
                }
            }

            // Then create the transformation:
            // Because we cache image data, the stored image may
            // need to be scaled.
            af = this.handleTransform(imageElement, x, y, imageWidth, imageHeight,
                                 width, height, generatorContext);
        }
        return af;
    }

    /**
     * The handler sets the xlink:href tag and returns a transform
     */
    public AffineTransform handleImage(final RenderableImage image,
                                       final Element imageElement,
                                       final double x, final double y,
                                       final double width, final double height,
                                       final SVGGeneratorContext generatorContext) {

        final double imageWidth   = image.getWidth();
        final double imageHeight  = image.getHeight();
        AffineTransform af  = null;

        if(imageWidth == 0 || imageHeight == 0 ||
           width == 0 || height == 0) {

            // Forget about it
            this.handleEmptyImage(imageElement);

        } else {
            // First set the href
            try {
                this.handleHREF(image, imageElement, generatorContext);
            } catch (final SVGGraphics2DIOException e) {
                try {
                    generatorContext.errorHandler.handleError(e);
                } catch (final SVGGraphics2DIOException io) {
                    // we need a runtime exception because
                    // java.awt.Graphics2D method doesn't throw exceptions..
                    throw new SVGGraphics2DRuntimeException(io);
                }
            }

            // Then create the transformation:
            // Because we cache image data, the stored image may
            // need to be scaled.
            af = this.handleTransform(imageElement, x,y,
                                 imageWidth, imageHeight,
                                 width, height, generatorContext);
        }
        return af;
    }

    /**
     * Determines the transformation needed to get the cached image to
     * scale & position properly. Sets x and y attributes on the element
     * accordingly.
     */
    protected AffineTransform handleTransform(final Element imageElement,
                                              final double x, final double y,
                                              final double srcWidth,
                                              final double srcHeight,
                                              final double dstWidth,
                                              final double dstHeight,
                                              final SVGGeneratorContext generatorContext) {
        // In this the default case, <image> element, we just
        // set x, y, width and height attributes.
        // No additional transform is necessary.
        imageElement.setAttributeNS(null,
                                    SVG_X_ATTRIBUTE,
                                    generatorContext.doubleString(x));
        imageElement.setAttributeNS(null,
                                    SVG_Y_ATTRIBUTE,
                                    generatorContext.doubleString(y));
        imageElement.setAttributeNS(null,
                                    SVG_WIDTH_ATTRIBUTE,
                                    generatorContext.doubleString(dstWidth));
        imageElement.setAttributeNS(null,
                                    SVG_HEIGHT_ATTRIBUTE,
                                    generatorContext.doubleString(dstHeight));
        return null;
    }
              
    protected void handleEmptyImage(final Element imageElement) {
        imageElement.setAttributeNS(XLINK_NAMESPACE_URI,
                                    ATTR_XLINK_HREF, "");
        imageElement.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE, "0");
        imageElement.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, "0");
    }

    /**
     * The handler should set the xlink:href tag and the width and
     * height attributes.
     */
    public void handleHREF(final Image image, final Element imageElement,
                           final SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        if (image == null)
            throw new SVGGraphics2DRuntimeException(ERR_IMAGE_NULL);

        final int width = image.getWidth(null);
        final int height = image.getHeight(null);

        if (width==0 || height==0) {
            this.handleEmptyImage(imageElement);
        } else {
            if (image instanceof RenderedImage) {
                this.handleHREF((RenderedImage)image, imageElement,
                           generatorContext);
            } else {
                final BufferedImage buf = this.buildBufferedImage(new Dimension(width, height));
                final Graphics2D g = createGraphics(buf);
                g.drawImage(image, 0, 0, null);
                g.dispose();
                this.handleHREF((RenderedImage)buf, imageElement,
                           generatorContext);
            }
        }
    }

    /**
     * This method creates a BufferedImage of the right size and type
     * for the derived class.
     */
    public BufferedImage buildBufferedImage(final Dimension size){
        return new BufferedImage(size.width, size.height, this.getBufferedImageType());
    }

    /**
     * This template method should set the xlink:href attribute on the input
     * Element parameter
     */
    protected void handleHREF(final RenderedImage image, final Element imageElement,
                              final SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        //
        // Create an buffered image if necessary
        //
        BufferedImage buf = null;
        if (image instanceof BufferedImage 
            &&
            ((BufferedImage)image).getType() == this.getBufferedImageType()){
            buf = (BufferedImage)image;
        } else {
            final Dimension size = new Dimension(image.getWidth(), image.getHeight());
            buf = this.buildBufferedImage(size);
            
            final Graphics2D g = createGraphics(buf);
            
            g.drawRenderedImage(image, IDENTITY);
            g.dispose();
        }

        //
        // Cache image and set xlink:href
        //
        this.cacheBufferedImage(imageElement, buf, generatorContext);
    }

    /**
     * This method will delegate to the <tt>handleHREF</tt> which
     * uses a <tt>RenderedImage</tt>
     */
    protected void handleHREF(final RenderableImage image, final Element imageElement,
                              final SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        // Create an buffered image where the image will be drawn
        final Dimension size = new Dimension((int)Math.ceil(image.getWidth()),
                                       (int)Math.ceil(image.getHeight()));
        final BufferedImage buf = this.buildBufferedImage(size);

        final Graphics2D g = createGraphics(buf);

        g.drawRenderableImage(image, IDENTITY);
        g.dispose();

        this.handleHREF((RenderedImage)buf, imageElement, generatorContext);
    }

    protected void cacheBufferedImage(final Element imageElement,
                                      final BufferedImage buf,
                                      final SVGGeneratorContext generatorContext)
        throws SVGGraphics2DIOException {
        
        ByteArrayOutputStream os;
        
        if (generatorContext == null)
            throw new SVGGraphics2DRuntimeException(ERR_CONTEXT_NULL);
        
        try {
            os = new ByteArrayOutputStream();
            // encode the image in memory
            this.encodeImage(buf, os);
            os.flush();
            os.close();
        } catch (final IOException e) {
            // should not happen since we do in-memory processing
            throw new SVGGraphics2DIOException(ERR_UNEXPECTED, e);
        }
        
        // ask the cacher for a reference
        final String ref = this.imageCacher.lookup(os,
                                                  buf.getWidth(),
                                                  buf.getHeight(),
                                                  generatorContext);
        
        // set the URL
        imageElement.setAttributeNS(XLINK_NAMESPACE_URI,
                                    ATTR_XLINK_HREF,
                                    this.getRefPrefix() + ref);
    }                
 
    /**
     * Should return the prefix with wich the image reference
     * should be pre-concatenated.
     */
    public abstract String getRefPrefix();

    /**
     * Derived classes should implement this method and encode the input
     * BufferedImage as needed
     */
    public abstract void encodeImage(BufferedImage buf, OutputStream os)
        throws IOException;
 
    /**
     * This template method should be overridden by derived classes to 
     * declare the image type they need for saving to file.
     */
    public abstract int getBufferedImageType();
  
}
