/*

   Copyright 1999-2003  The Apache Software Foundation 

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

package org.apache.batik.ext.awt.g2d;

/**
 * Enumeration for transformation types.
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: TransformType.java,v 1.4 2005/03/27 08:58:32 cam Exp $
 */
public class TransformType{
    /*
     * Transform type constants
     */
    /**
     * translate
     */
    public static final int TRANSFORM_TRANSLATE = 0;
    /**
     * rotate
     */
    public static final int TRANSFORM_ROTATE = 1;
    /**
     * scale
     */
    public static final int TRANSFORM_SCALE = 2;
    /**
     * shear
     */
    public static final int TRANSFORM_SHEAR = 3;
    /**
     * general
     */
    public static final int TRANSFORM_GENERAL = 4;

    /**
     * Strings describing the elementary transforms
     */
    public static final String TRANSLATE_STRING = "translate";
    /**
     * rotate string
     */
    public static final String ROTATE_STRING = "rotate";
    /**
     * scale string
     */
    public static final String SCALE_STRING = "scale";
    /**
     * shear string
     */
    public static final String SHEAR_STRING = "shear";
    /**
     * general string
     */
    public static final String GENERAL_STRING = "general";

    /**
     * TransformType values
     */
    public static final TransformType TRANSLATE = new TransformType(TRANSFORM_TRANSLATE, TRANSLATE_STRING);
    /**
     * rotate
     */
    public static final TransformType ROTATE = new TransformType(TRANSFORM_ROTATE, ROTATE_STRING);
    /**
     * scale
     */
    public static final TransformType SCALE = new TransformType(TRANSFORM_SCALE, SCALE_STRING);
    /**
     * shear
     */
    public static final TransformType SHEAR = new TransformType(TRANSFORM_SHEAR, SHEAR_STRING);
    /**
     * general
     */
    public static final TransformType GENERAL = new TransformType(TRANSFORM_GENERAL, GENERAL_STRING);

    /**
     * desc
     */
    private String desc;
    /**
     * val
     */
    private int val;

    /**
     * Constructor is private so that no instances other than
     * the ones in the enumeration can be created.
     * @param val 
     * @param desc 
     * @see #readResolve
     */
    private TransformType(final int val, final String desc){
        this.desc = desc;
        this.val = val;
    }

    /**
     * @return description
     */
    public String toString(){
        return this.desc;
    }

    /**
     * Convenience for enumeration switching.
     * That is,
     * <pre>
     *   switch(transformType.toInt()){
     *       case TransformType.TRANSFORM_TRANSLATE:
     *        ....
     *       case TransformType.TRANSFORM_ROTATE:
     * </pre>
     * @return to int value
     */
    public int toInt(){
        return this.val;
    }

    /**
     *  This is called by the serialization code before it returns an unserialized
     * object. To provide for unicity of instances, the instance that was read
     * is replaced by its static equivalent
     * @return type
     */
    public Object readResolve() {
        switch(this.val){
        case TRANSFORM_TRANSLATE:
            return TransformType.TRANSLATE;
        case TRANSFORM_ROTATE:
            return TransformType.ROTATE;
        case TRANSFORM_SCALE:
            return TransformType.SCALE;
        case TRANSFORM_SHEAR:
            return TransformType.SHEAR;
        case TRANSFORM_GENERAL:
            return TransformType.GENERAL;
        default:
            throw new Error("Unknown TransformType value");
        }
    }
}
