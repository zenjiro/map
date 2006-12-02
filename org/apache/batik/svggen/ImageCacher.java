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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class implements caching functionality for raster images.
 *
 * @author <a href="mailto:paul_evenblij@compuware.com">Paul Evenblij</a>
 * @version $Id: ImageCacher.java,v 1.6 2004/08/18 07:14:59 vhardy Exp $
 */
public abstract class ImageCacher implements SVGSyntax, ErrorConstants {
  
    DOMTreeManager  domTreeManager = null;
    Hashtable       imageCache;
    Checksum        checkSum;
    
    /**
     * Creates an ImageCacher.
     */
    public ImageCacher() {
        this.imageCache = new Hashtable();
        this.checkSum = new Adler32();
    }

    /**
     * Creates an ImageCacher.
     *
     * @param domTreeManager the DOMTreeManager for the tree this cacher works on
     */
    public ImageCacher(final DOMTreeManager domTreeManager) {
        this();
        this.setDOMTreeManager(domTreeManager);
    }

    /**
     * Sets the DOMTreeManager this cacher should work on.
     *
     * @param domTreeManager the DOMTreeManager for the tree this cacher works on
     */
    public void setDOMTreeManager(final DOMTreeManager domTreeManager) {
        if (domTreeManager == null){
            throw new IllegalArgumentException();
        }
        this.domTreeManager = domTreeManager;
    }

    public DOMTreeManager getDOMTreeManager(){
        return this.domTreeManager;
    }

    /**
     * Checks if the image is already in the cache, and
     * adds it if not. Returns a unique id for the entry.
     *
     * @param os                the image as a byte stream
     * @param width             the width of the image
     * @param height            the height of the image
     * @param ctx               the SVGGeneratorContext
     *
     * @return a URI for the image
     * @throws SVGGraphics2DIOException if an error occurs during image file i/o
     */     
    public String lookup(final ByteArrayOutputStream os,
                         final int width, final int height,
                         final SVGGeneratorContext ctx)
                             throws SVGGraphics2DIOException {
        // We determine a checksum value for the byte data, and use it
        // as hash key for the image. This may not be unique, so we
        // need to check on actual byte-for-byte equality as well.
        // The checksum will be sufficient in most cases.
        final int     checksum = this.getChecksum(os.toByteArray());
        final Integer key      = new Integer(checksum);
        String  href     = null;
        
        final Object data = this.getCacheableData(os);
        
        LinkedList list = (LinkedList) this.imageCache.get(key);
        if(list == null) {
            // Key not found: make a new key/value pair
            list = new LinkedList();
            this.imageCache.put(key, list);
        } else {
            // Key found: check if the image is already in the list
            for(final ListIterator i = list.listIterator(0); i.hasNext(); ) {
                final ImageCacheEntry entry = (ImageCacheEntry) i.next();
                if(entry.checksum == checksum && this.imagesMatch(entry.src, data)) {
                    href = entry.href;
                    break;
                }
            }
        }
        
        if(href == null) {
            // Still no hit: add our own
            final ImageCacheEntry newEntry = this.createEntry(checksum, data,
                                                   width, height,
                                                   ctx);
            list.add(newEntry);
            href = newEntry.href;
        }
 
        return href;
    }
    
    /**
     * Returns an object which can be cached.
     * Implementation must determine which information
     * should actually be stored.
     *
     * @param os the byte stream which is to be coerced
     */
    abstract Object getCacheableData(ByteArrayOutputStream os);
    
    /**
     * Determines if two images are equal.
     * Interpretation of the objects referred to by
     * <code>o1</code> and <code>o2</code> is entirely
     * implementation-dependent.
     *
     * @param o1 object referring to one image
     * @param o2 object referring to the other image
     */
    abstract boolean imagesMatch(Object o1, Object o2)
                                             throws SVGGraphics2DIOException;

    /**
     * Creates a new entry for keeping in the cache.
     *
     * @param checksum the checksum from which the hash key is derived
     * @param data     the data to be cached
     * @param width    image width
     * @param height   image height
     * @param ctx      the SVGGeneratorContext
     */
    abstract ImageCacheEntry createEntry(int checksum,
                                         Object data,
                                         int width, int height,
                                         SVGGeneratorContext ctx)
                                             throws SVGGraphics2DIOException;

    /**
     *  Calculates a checksum value for the given data.
     */
    int getChecksum(final byte[] data) {
        this.checkSum.reset();
        this.checkSum.update(data, 0, data.length);
        return (int) this.checkSum.getValue();
    }

    /**
     * Instances of this class are created to keep track of the
     * set of images processed by the ImageHandler. Each entry
     * corresponds to one unique member of this set.
     */
    private class ImageCacheEntry {

        /** A checksum calculated for the data cached */
        public int checksum;
        
        /** An implementation-dependent object referring to the data */
        public Object src;
        
        /** A uri identifying the data */
        public String href;

        /**
         * Creates a new entry
         */
        public ImageCacheEntry(final int    checksum,
                               final Object src,
                               final String href) {
            this.checksum = checksum;
            this.src      = src;
            this.href     = href;
        }
    }

    /**
     * Cache implementation for images embedded in the SVG file.
     */
    public static class Embedded extends ImageCacher {
    
        /**
         * Sets the DOMTreeManager this cacher should work on.
         *
         * @param domTreeManager the DOMTreeManager for the tree this cacher works on
         */
        public void setDOMTreeManager(final DOMTreeManager domTreeManager) {
            // A new DOMTreeManager implies a new cache, because we cache
            // images in the SVG tree itself
            if(this.domTreeManager != domTreeManager) {
                this.domTreeManager = domTreeManager;
                this.imageCache     = new Hashtable();
            }
        }

        Object getCacheableData(final ByteArrayOutputStream os) {
            // In order to have only one instance of the image data
            // in memory, we cache the entire xlink:href attribute value,
            // so we can just pass a reference to the tree manager.
            return DATA_PROTOCOL_PNG_PREFIX + os.toString();
        }
        
        boolean imagesMatch(final Object o1, final Object o2) {
            return o1.equals(o2);
        }

        ImageCacheEntry createEntry(final int checksum, final Object data,
                                    final int width, final int height,
                                    final SVGGeneratorContext ctx) {

            // Get a new unique id
            final String id = ctx.idGenerator.generateID(ID_PREFIX_IMAGE);
            
            // Add the image data reference to the <defs> section
            this.addToTree(id, (String) data, width, height, ctx);

            // Create new cache entry
            return new ImageCacheEntry(checksum, data, SIGN_POUND + id);
        }

        /**
         *  Adds a new image element to the defs section for cached images.
         */
        private void addToTree(final String id,
                               final String href,
                               final int width, final int height,
                               final SVGGeneratorContext ctx) {

            final Document domFactory = this.domTreeManager.getDOMFactory();
            // Element imageDefs = getImageDefs(domFactory, ctx);
            
            // Create and initialize the new image element
            final Element imageElement = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                              SVG_IMAGE_TAG);
            imageElement.setAttributeNS(null, SVG_ID_ATTRIBUTE,
                                              id);
            imageElement.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE,
                                              Integer.toString(width));
            imageElement.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE,
                                              Integer.toString(height));
            imageElement.setAttributeNS(DefaultImageHandler.XLINK_NAMESPACE_URI,
                                              ATTR_XLINK_HREF,
                                              href);
            // imageDefs.appendChild(imageElement);
            this.domTreeManager.addOtherDef(imageElement);
        }
        

       /**
         *  Returns the top level defs section dedicated to cached
         *  embedded images, creating one if necessary.
         *  This one very simply creates a new defs section for each image,
         *  causing them to be spread throughout the entire SVG tree.
         *  A nicer implementation would group all imageDefs sections into
         *  one.
         */
        /*private Element getImageDefs(Document domFactory,
                                     SVGGeneratorContext ctx) {
        
            Element imageDefs = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                           SVG_DEFS_TAG);

            String id = ctx.idGenerator.generateID(ID_PREFIX_IMAGE_DEFS);        
            imageDefs.setAttributeNS(null, SVG_ID_ATTRIBUTE, id);
            
            domTreeManager.appendGroup(imageDefs, null);

            return imageDefs;
            }*/
    }
        
    /**
     * Cache implementation for file-based images.
     */
    public static class External extends ImageCacher {
    
        private String imageDir;
        private String prefix;
        private String suffix;
    
        public External(final String imageDir, final String prefix, final String suffix) {
            super();
            this.imageDir = imageDir;
            this.prefix   = prefix;
            this.suffix   = suffix;
        }

        Object getCacheableData(final ByteArrayOutputStream os) {
            return os;
        }
        
        boolean imagesMatch(final Object o1, final Object o2)
                throws SVGGraphics2DIOException {
            boolean match = false;
            try {     
                final FileInputStream imageStream =
                                    new FileInputStream((File) o1);
                final int imageLen = imageStream.available();
                final byte[] imageBytes = new byte[imageLen];
                final byte[] candidateBytes =
                        ((ByteArrayOutputStream) o2).toByteArray();
                
                int bytesRead = 0;
                while (bytesRead != imageLen) {
                    bytesRead += imageStream.read
                      (imageBytes, bytesRead, imageLen-bytesRead);
                }
                            
                match = Arrays.equals(imageBytes, candidateBytes);
            } catch(final IOException e) {
                throw new SVGGraphics2DIOException(
                                    ERR_READ+((File) o1).getName());
            }
            return match;
        }

        ImageCacheEntry createEntry(final int checksum, final Object data,
                                    final int width, final int height,
                                    final SVGGeneratorContext ctx)
            throws SVGGraphics2DIOException {
        
            // Create a new file in image directory
            File imageFile = null;

            try {    
                // While the files we are generating exist, try to create
                // another unique id.
                while (imageFile == null) {
                    final String fileId = ctx.idGenerator.generateID(this.prefix);
                    imageFile = new File(this.imageDir, fileId + this.suffix);
                    if (imageFile.exists())
                        imageFile = null;
                }
                
                // Write data to file
                final OutputStream outputStream = new FileOutputStream(imageFile);
                ((ByteArrayOutputStream) data).writeTo(outputStream);
                ((ByteArrayOutputStream) data).close();
            } catch(final IOException e) {
                throw new SVGGraphics2DIOException(ERR_WRITE+imageFile.getName());
            }
            
            // Create new cache entry
            return new ImageCacheEntry(checksum, imageFile, imageFile.getName());
        }

    }

}


