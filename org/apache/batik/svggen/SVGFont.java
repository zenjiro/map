/*

   Copyright 2001-2003  The Apache Software Foundation 

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

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Utility class that converts a Font object into a set of SVG
 * font attributes
 *
 * @author <a href="mailto:cjolif@ilog.fr">Christophe Jolif</a>
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: SVGFont.java,v 1.19 2004/08/18 07:15:00 vhardy Exp $
 */
public class SVGFont extends AbstractSVGConverter {
    public static final float EXTRA_LIGHT =
        TextAttribute.WEIGHT_EXTRA_LIGHT.floatValue();
    public static final float LIGHT =
        TextAttribute.WEIGHT_LIGHT.floatValue();
    public static final float DEMILIGHT =
        TextAttribute.WEIGHT_DEMILIGHT.floatValue();
    public static final float REGULAR =
        TextAttribute.WEIGHT_REGULAR.floatValue();
    public static final float SEMIBOLD =
        TextAttribute.WEIGHT_SEMIBOLD.floatValue();
    public static final float MEDIUM =
        TextAttribute.WEIGHT_MEDIUM.floatValue();
    public static final float DEMIBOLD =
        TextAttribute.WEIGHT_DEMIBOLD.floatValue();
    public static final float BOLD =
        TextAttribute.WEIGHT_BOLD.floatValue();
    public static final float HEAVY =
        TextAttribute.WEIGHT_HEAVY.floatValue();
    public static final float EXTRABOLD =
        TextAttribute.WEIGHT_EXTRABOLD.floatValue();
    public static final float ULTRABOLD =
        TextAttribute.WEIGHT_ULTRABOLD.floatValue();

    public static final float POSTURE_REGULAR =
        TextAttribute.POSTURE_REGULAR.floatValue();
    public static final float POSTURE_OBLIQUE =
        TextAttribute.POSTURE_OBLIQUE.floatValue();

    /**
     * Contains threshold value for the various Font styles. If a given
     * style is in an interval, then it is mapped to the style at the top
     * of that interval.
     * @see #styleToSVG
     */
    static final float fontStyles[] = {
        POSTURE_REGULAR + (POSTURE_OBLIQUE - POSTURE_REGULAR)/2
    };

    /**
     * SVG Styles corresponding to the fontStyles
     */
    static final String svgStyles[] = {
        /*POSTURE_REGULAR*/   SVG_NORMAL_VALUE,
        /*POSTURE_OBLIQUE*/   SVG_ITALIC_VALUE
    };

    /**
     * Contains threshold values for the various Font weights. If a given
     * weight is in an interval, then it is mapped to the weight at the top
     * of the interval.
     * @see #weightToSVG
     */
    static final float fontWeights[] = { EXTRA_LIGHT + (LIGHT - EXTRA_LIGHT)/2f,
                                         LIGHT + (DEMILIGHT - LIGHT)/2f,
                                         DEMILIGHT + (REGULAR - DEMILIGHT)/2f,
                                         REGULAR + (SEMIBOLD - REGULAR)/2f,
                                         SEMIBOLD + (MEDIUM - SEMIBOLD)/2f,
                                         MEDIUM + (DEMIBOLD - MEDIUM)/2f,
                                         DEMIBOLD + (BOLD - DEMIBOLD)/2f,
                                         BOLD + (HEAVY - BOLD)/2f,
                                         HEAVY + (EXTRABOLD - HEAVY)/2f,
                                         EXTRABOLD + (ULTRABOLD - EXTRABOLD),
    };

    /**
     * SVG Weights corresponding to the fontWeights
     */
    static final String svgWeights[] = {
        /*EXTRA_LIGHT*/ SVG_100_VALUE,
        /*LIGHT*/       SVG_200_VALUE,
        /*DEMILIGHT*/   SVG_300_VALUE,
        /*REGULAR*/     SVG_NORMAL_VALUE,
        /*SEMIBOLD*/    SVG_500_VALUE,
        /*MEDIUM*/      SVG_500_VALUE,
        /*DEMIBOLD*/    SVG_600_VALUE,
        /*BOLD*/        SVG_BOLD_VALUE,
        /*HEAVY*/       SVG_800_VALUE,
        /*EXTRABOLD*/   SVG_800_VALUE,
        /*ULTRABOLD*/   SVG_900_VALUE
    };

    /**
     * Logical fonts mapping
     */
    static Map logicalFontMap = new ConcurrentHashMap();

    static {
        logicalFontMap.put("dialog", "sans-serif");
        logicalFontMap.put("dialoginput", "monospace");
        logicalFontMap.put("monospaced", "monospace");
        logicalFontMap.put("serif", "serif");
        logicalFontMap.put("sansserif", "sans-serif");
        logicalFontMap.put("symbol", "'WingDings'");
    }

    /**
     * The common font size to use when generating all SVG fonts.
     */
    static final int COMMON_FONT_SIZE = 100;

    /**
     * Used to keep track of which characters have been rendered by each font
     * used.
     */
    Map fontStringMap = new ConcurrentHashMap();

    /**
     * @param generatorContext used to build Elements
     */
    public SVGFont(final SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }

    /**
     * Records that the specified font has been used to draw the text string.
     * This is so we can keep track of which glyphs are required for each
     * SVG font that is generated.
     */
    public void recordFontUsage(final String string, final Font font) {

        final Font commonSizeFont = createCommonSizeFont(font);
        final String fontKey = commonSizeFont.getFamily() + commonSizeFont.getStyle();
        String textUsingFont = (String)this.fontStringMap.get(fontKey);
        if (textUsingFont == null) {
            // font has not been used before
            textUsingFont = "";
        }
        // append any new characters to textUsingFont
        char ch;
        for (int i = 0; i < string.length(); i++) {
            ch = string.charAt(i);
            if (textUsingFont.indexOf(ch) == -1) {
                textUsingFont += ch;
            }
        }
        this.fontStringMap.put(fontKey, textUsingFont);
    }

    /**
     * Creates a new Font that is of the common font size used for generating
     * SVG fonts. The new Font will be the same as the specified font, with
     * only its size attribute modified.
     */
    private static Font createCommonSizeFont(final Font font) {
        final HashMap attributes = new HashMap(font.getAttributes());
        attributes.put(TextAttribute.SIZE, new Float(COMMON_FONT_SIZE));
        return new Font(attributes);
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
        return this.toSVG(gc.getFont(), gc.getFontRenderContext());
    }

    /**
     * @param font Font object which should be converted to a set
     *        of SVG attributes
     * @param frc The FontRenderContext which will be used to generate glyph
     * elements for the SVGFont definition element
     * @return description of attribute values that describe the font
     */
    public SVGFontDescriptor toSVG(final Font font, final FontRenderContext frc) {

        final String fontSize = "" + this.doubleString(font.getSize2D());
        final String fontWeight = weightToSVG(font);
        final String fontStyle = styleToSVG(font);
        final String fontFamilyStr = familyToSVG(font);

        final Font commonSizeFont = createCommonSizeFont(font);
        final String fontKey = commonSizeFont.getFamily() + commonSizeFont.getStyle();

        final String textUsingFont = (String)this.fontStringMap.get(fontKey);

        if (textUsingFont == null) {
            // this font hasn't been used by any text yet,
            // so don't create an SVG Font element for it
            return new SVGFontDescriptor(fontSize, fontWeight,
                                         fontStyle, fontFamilyStr,
                                         null);
        }

        final Document domFactory = this.generatorContext.domFactory;

        // see if a description already exists for this font
        final SVGFontDescriptor fontDesc =
            (SVGFontDescriptor)this.descMap.get(fontKey);

        Element fontDef;

        if (fontDesc != null) {

            // use the SVG Font element that has already been created
            fontDef = fontDesc.getDef();

        } else {

            // create a new SVG Font element
            fontDef = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                 SVG_FONT_TAG);

            //
            // create the font-face element
            //
            final Element fontFace = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                          SVG_FONT_FACE_TAG);
            String svgFontFamilyString = fontFamilyStr;
            if (fontFamilyStr.startsWith("'") && fontFamilyStr.endsWith("'")) {
                // get rid of the quotes
                svgFontFamilyString
                    = fontFamilyStr.substring(1, fontFamilyStr.length()-1);
            }
            fontFace.setAttributeNS(null, SVG_FONT_FAMILY_ATTRIBUTE,
                                    svgFontFamilyString);
            fontFace.setAttributeNS(null, SVG_FONT_WEIGHT_ATTRIBUTE,
                                    fontWeight);
            fontFace.setAttributeNS(null, SVG_FONT_STYLE_ATTRIBUTE,
                                    fontStyle);
            fontFace.setAttributeNS(null, SVG_UNITS_PER_EM_ATTRIBUTE,
                                    ""+COMMON_FONT_SIZE);
            fontDef.appendChild(fontFace);

            //
            // create missing glyph element
            //
            final Element missingGlyphElement
                = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                             SVG_MISSING_GLYPH_TAG);

            final int missingGlyphCode[] = new int[1];
            missingGlyphCode[0] = commonSizeFont.getMissingGlyphCode();
            final GlyphVector gv = commonSizeFont.createGlyphVector(frc, missingGlyphCode);
            Shape missingGlyphShape = gv.getGlyphOutline(0);
            final GlyphMetrics gm = gv.getGlyphMetrics(0);

            // need to turn the missing glyph upside down to be in the font
            // coordinate system (i.e Y axis up)
            final AffineTransform at = AffineTransform.getScaleInstance(1, -1);
            missingGlyphShape = at.createTransformedShape(missingGlyphShape);

            missingGlyphElement.setAttributeNS(null, SVG_D_ATTRIBUTE,
                                    SVGPath.toSVGPathData(missingGlyphShape, this.generatorContext));
            missingGlyphElement.setAttributeNS(null, SVG_HORIZ_ADV_X_ATTRIBUTE,
                                               "" + gm.getAdvance());
            fontDef.appendChild(missingGlyphElement);

            // set the font's default horizontal advance to be the same as
            // the missing glyph
            fontDef.setAttributeNS(null, SVG_HORIZ_ADV_X_ATTRIBUTE,  "" + gm.getAdvance());

            // set the ascent and descent attributes
            final LineMetrics lm = commonSizeFont.getLineMetrics("By", frc);
            fontFace.setAttributeNS(null, SVG_ASCENT_ATTRIBUTE, "" + lm.getAscent());
            fontFace.setAttributeNS(null, SVG_DESCENT_ATTRIBUTE, "" + lm.getDescent());

            //
            // Font ID
            //
            fontDef.setAttributeNS(null, ATTR_ID,
                                   this.generatorContext.idGenerator.
                                   generateID(ID_PREFIX_FONT));
        }

        //
        // add any new glyphs to the fontDef here
        //

        // process the characters in textUsingFont backwards since the new chars
        // are at the end, can stop when find a char that already has a glyph
        for (int i = textUsingFont.length()-1; i >= 0; i--) {
            final char c = textUsingFont.charAt(i);
            boolean foundGlyph = false;
            final NodeList fontChildren = fontDef.getChildNodes();
            for (int j = 0; j < fontChildren.getLength(); j++) {
                if (fontChildren.item(j) instanceof Element) {
                    final Element childElement = (Element)fontChildren.item(j);
                    if (childElement.getAttributeNS(null,
                            SVG_UNICODE_ATTRIBUTE).equals(""+c)) {
                        foundGlyph = true;
                        break;
                    }
                }
            }
            if (!foundGlyph) {
                // need to create one
                final Element glyphElement
                    = domFactory.createElementNS(SVG_NAMESPACE_URI,
                                                 SVG_GLYPH_TAG);

                final GlyphVector gv = commonSizeFont.createGlyphVector(frc, ""+c);
                Shape glyphShape = gv.getGlyphOutline(0);
                final GlyphMetrics gm = gv.getGlyphMetrics(0);

                // need to turn the glyph upside down to be in the font
                // coordinate system (i.e Y axis up)
                final AffineTransform at = AffineTransform.getScaleInstance(1, -1);
                glyphShape = at.createTransformedShape(glyphShape);

                glyphElement.setAttributeNS(null, SVG_D_ATTRIBUTE,
                                            SVGPath.toSVGPathData(glyphShape, this.generatorContext));
                glyphElement.setAttributeNS(null, SVG_HORIZ_ADV_X_ATTRIBUTE,
                                            "" + gm.getAdvance());
                glyphElement.setAttributeNS(null, SVG_UNICODE_ATTRIBUTE,
                                            "" + c);

                fontDef.appendChild(glyphElement);
            } else {
                // have reached the chars in textUsingFont that already
                // have glyphs, don't need to process any further
                break;
            }
        }

        //
        // create a new font description for this instance of the font usage
        //
        final SVGFontDescriptor newFontDesc
            = new SVGFontDescriptor(fontSize, fontWeight,
                                    fontStyle, fontFamilyStr,
                                    fontDef);

        //
        // Update maps so that the font def can be reused if needed
        //
        if (fontDesc == null) {
            this.descMap.put(fontKey, newFontDesc);
            this.defSet.add(fontDef);
        }

        return newFontDesc;
    }

    /**
     * @param font whose family should be converted to an SVG string
     *   value.
     */
    public static String familyToSVG(final Font font) {
        String fontFamilyStr = font.getFamily();
        final String logicalFontFamily =
            (String)logicalFontMap.get(font.getName().toLowerCase());
        if (logicalFontFamily != null)
            fontFamilyStr = logicalFontFamily;
        else {
            final StringBuffer fontFamily = new StringBuffer("'");
            fontFamily.append(fontFamilyStr);
            fontFamily.append("'");
            fontFamilyStr = fontFamily.toString();
        }
        return fontFamilyStr;
    }

    /**
     * @param font whose style should be converted to an SVG string
     *        value.
     */
    public static String styleToSVG(final Font font) {
        final Map attrMap = font.getAttributes();
        Float styleValue = (Float)attrMap.get(TextAttribute.POSTURE);

        if (styleValue == null) {
            if (font.isItalic())
                styleValue = TextAttribute.POSTURE_OBLIQUE;
            else
                styleValue = TextAttribute.POSTURE_REGULAR;
        }

        final float style = styleValue.floatValue();

        int i = 0;
        for (i=0; i< fontStyles.length; i++) {
            if (style <= fontStyles[i])
                break;
        }

        return svgStyles[i];
    }

    /**
     * @param font whose weight should be converted to an SVG string
     *        value. Note that there is loss of precision for
     *        semibold and extrabold.
     */
    public static String weightToSVG(final Font font) {
        final Map attrMap = font.getAttributes();
        Float weightValue = (Float)attrMap.get(TextAttribute.WEIGHT);
        if (weightValue==null) {
            if (font.isBold())
                weightValue = TextAttribute.WEIGHT_BOLD;
            else
                weightValue = TextAttribute.WEIGHT_REGULAR;
        }

        final float weight = weightValue.floatValue();

        int i = 0;
        for (i=0; i<fontWeights.length; i++) {
            if (weight<=fontWeights[i])
                break;
        }

        return svgWeights[i];
    }
}
