/*

   Copyright 2001  The Apache Software Foundation 

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

/**
 * Thrown when an SVG Generator method receives an illegal argument in parameter.
 *
 * @author <a href="mailto:cjolif@ilog.fr">Christophe Jolif</a>
 * @version $Id: SVGGraphics2DRuntimeException.java,v 1.3 2004/08/18 07:15:07 vhardy Exp $
 */
public class SVGGraphics2DRuntimeException extends RuntimeException {
    /** The enclosed exception. */
    private Exception embedded;

    /**
     * Constructs a new <code>SVGGraphics2DRuntimeException</code> with the
     * specified detail message.
     * @param s the detail message of this exception
     */
    public SVGGraphics2DRuntimeException(final String s) {
        this(s, null);
    }

    /**
     * Constructs a new <code>SVGGraphics2DRuntimeException</code> with the
     * specified detail message.
     * @param ex the enclosed exception
     */
    public SVGGraphics2DRuntimeException(final Exception ex) {
        this(null, ex);
    }

    /**
     * Constructs a new <code>SVGGraphics2DRuntimeException</code> with the
     * specified detail message.
     * @param s the detail message of this exception
     * @param ex the original exception
     */
    public SVGGraphics2DRuntimeException(final String s, final Exception ex) {
        super(s);
        this.embedded = ex;
    }

    /**
     * Returns the message of this exception. If an error message has
     * been specified, returns that one. Otherwise, return the error message
     * of enclosed exception or null if any.
     */
    public String getMessage() {
        final String msg = super.getMessage();
        if (msg != null) {
            return msg;
        } else if (this.embedded != null) {
            return this.embedded.getMessage();
        } else {
            return null;
        }
    }

    /**
     * Returns the original enclosed exception or null if any.
     */
    public Exception getException() {
        return this.embedded;
    }
}
