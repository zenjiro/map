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
package org.apache.batik.ext.awt.image.codec;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

class CRC {

    private static final int[] crcTable = new int[256];

    static {
        // Initialize CRC table
        for (int n = 0; n < 256; n++) {
            int c = n;
            for (int k = 0; k < 8; k++) {
                if ((c & 1) == 1) {
                    c = 0xedb88320 ^ (c >>> 1);
                } else {
                    c >>>= 1;
                }

                crcTable[n] = c;
            }
        }
    }

    public static int updateCRC(final int crc, final byte[] data, final int off, final int len) {
        int c = crc;

        for (int n = 0; n < len; n++) {
             c = crcTable[(c ^ data[off + n]) & 0xff] ^ (c >>> 8);
        }

        return c;
    }
}


class ChunkStream extends OutputStream implements DataOutput {

    private String type;
    private ByteArrayOutputStream baos;
    private DataOutputStream dos;

    public ChunkStream(final String type) throws IOException {
        this.type = type;

        this.baos = new ByteArrayOutputStream();
        this.dos = new DataOutputStream(this.baos);
    }

    public void write(final byte[] b) throws IOException {
        this.dos.write(b);
    }

    public void write(final byte[] b, final int off, final int len) throws IOException {
        this.dos.write(b, off, len);
    }

    public void write(final int b) throws IOException {
        this.dos.write(b);
    }

    public void writeBoolean(final boolean v) throws IOException {
        this.dos.writeBoolean(v);
    }

    public void writeByte(final int v) throws IOException {
        this.dos.writeByte(v);
    }

    public void writeBytes(final String s) throws IOException {
        this.dos.writeBytes(s);
    }

    public void writeChar(final int v) throws IOException {
        this.dos.writeChar(v);
    }

    public void writeChars(final String s) throws IOException {
        this.dos.writeChars(s);
    }

    public void writeDouble(final double v) throws IOException {
        this.dos.writeDouble(v);
    }

    public void writeFloat(final float v) throws IOException {
        this.dos.writeFloat(v);
    }

    public void writeInt(final int v) throws IOException {
        this.dos.writeInt(v);
    }

    public void writeLong(final long v) throws IOException {
        this.dos.writeLong(v);
    }

    public void writeShort(final int v) throws IOException {
        this.dos.writeShort(v);
    }

    public void writeUTF(final String str) throws IOException {
        this.dos.writeUTF(str);
    }

    public void writeToStream(final DataOutputStream output) throws IOException {
        final byte[] typeSignature = new byte[4];
        typeSignature[0] = (byte)this.type.charAt(0);
        typeSignature[1] = (byte)this.type.charAt(1);
        typeSignature[2] = (byte)this.type.charAt(2);
        typeSignature[3] = (byte)this.type.charAt(3);

        this.dos.flush();
        this.baos.flush();

        final byte[] data = this.baos.toByteArray();
        final int len = data.length;

        output.writeInt(len);
        output.write(typeSignature);
        output.write(data, 0, len);

        int crc = 0xffffffff;
        crc = CRC.updateCRC(crc, typeSignature, 0, 4);
        crc = CRC.updateCRC(crc, data, 0, len);
        output.writeInt(crc ^ 0xffffffff);
    }
}


class IDATOutputStream extends FilterOutputStream {

    private static final byte[] typeSignature =
      {(byte)'I', (byte)'D', (byte)'A', (byte)'T'};

    private int bytesWritten = 0;
    private int segmentLength;
    byte[] buffer;

    public IDATOutputStream(final OutputStream output,
                            final int segmentLength) {
        super(output);
        this.segmentLength = segmentLength;
        this.buffer = new byte[segmentLength];
    }

    public void close() throws IOException {
        this.flush();
    }

    private void writeInt(final int x) throws IOException {
        this.out.write(x >> 24);
        this.out.write((x >> 16) & 0xff);
        this.out.write((x >> 8) & 0xff);
        this.out.write(x & 0xff);
    }

    public void flush() throws IOException {
        // Length
        this.writeInt(this.bytesWritten);
        // 'IDAT' signature
        this.out.write(typeSignature);
        // Data
        this.out.write(this.buffer, 0, this.bytesWritten);

        int crc = 0xffffffff;
        crc = CRC.updateCRC(crc, typeSignature, 0, 4);
        crc = CRC.updateCRC(crc, this.buffer, 0, this.bytesWritten);

        // CRC
        this.writeInt(crc ^ 0xffffffff);

        // Reset buffer
        this.bytesWritten = 0;
    }

    public void write(final byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    public void write(final byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            final int bytes = Math.min(this.segmentLength - this.bytesWritten, len);
            System.arraycopy(b, off, this.buffer, this.bytesWritten, bytes);
            off += bytes;
            len -= bytes;
            this.bytesWritten += bytes;

            if (this.bytesWritten == this.segmentLength) {
                this.flush();
            }
        }
    }

    public void write(final int b) throws IOException {
        this.buffer[this.bytesWritten++] = (byte)b;
        if (this.bytesWritten == this.segmentLength) {
            this.flush();
        }
    }
}

/**
 * An ImageEncoder for the PNG file format.
 *
 * @since EA4
 */
public class PNGImageEncoder extends ImageEncoderImpl {

    private static final int PNG_COLOR_GRAY = 0;
    private static final int PNG_COLOR_RGB = 2;
    private static final int PNG_COLOR_PALETTE = 3;
    private static final int PNG_COLOR_GRAY_ALPHA = 4;
    private static final int PNG_COLOR_RGB_ALPHA = 6;

    private static final byte[] magic = {
        (byte)137, (byte) 80, (byte) 78, (byte) 71,
        (byte) 13, (byte) 10, (byte) 26, (byte) 10
    };

    private PNGEncodeParam param;

    private RenderedImage image;
    private int width;
    private int height;
    private int bitDepth;
    private int bitShift;
    private int numBands;
    private int colorType;

    private int bpp; // bytes per pixel, rounded up

    private boolean skipAlpha = false;
    private boolean compressGray = false;

    private boolean interlace;

    private byte[] redPalette = null;
    private byte[] greenPalette = null;
    private byte[] bluePalette = null;
    private byte[] alphaPalette = null;

    private DataOutputStream dataOutput;

    public PNGImageEncoder(final OutputStream output,
                           final PNGEncodeParam param) {
        super(output, param);

        if (param != null) {
            this.param = param;
        }
        this.dataOutput = new DataOutputStream(output);
    }

    private void writeMagic() throws IOException {
        this.dataOutput.write(magic);
    }

    private void writeIHDR() throws IOException {
        final ChunkStream cs = new ChunkStream("IHDR");
        cs.writeInt(this.width);
        cs.writeInt(this.height);
        cs.writeByte((byte)this.bitDepth);
        cs.writeByte((byte)this.colorType);
        cs.writeByte((byte)0);
        cs.writeByte((byte)0);
        cs.writeByte(this.interlace ? (byte)1 : (byte)0);

        cs.writeToStream(this.dataOutput);
    }

    private byte[] prevRow = null;
    private byte[] currRow = null;

    private byte[][] filteredRows = null;

    private static int clamp(final int val, final int maxValue) {
        return (val > maxValue) ? maxValue : val;
    }

    private void encodePass(final OutputStream os, final Raster ras,
                            int xOffset,     final int yOffset,
                            int xSkip,       final int ySkip) 
        throws IOException {
        final int minX   = ras.getMinX();
        final int minY   = ras.getMinY();
        final int width  = ras.getWidth();
        final int height = ras.getHeight();

        xOffset *= this.numBands;
        xSkip   *= this.numBands;

        final int samplesPerByte = 8/this.bitDepth;

        final int numSamples = width*this.numBands;
        final int[] samples = new int[numSamples];

        final int pixels = (numSamples - xOffset + xSkip - 1)/xSkip;
        int bytesPerRow = pixels*this.numBands;
        if (this.bitDepth < 8) {
            bytesPerRow = (bytesPerRow + samplesPerByte - 1)/samplesPerByte;
        } else if (this.bitDepth == 16) {
            bytesPerRow *= 2;
        }

        if (bytesPerRow == 0) {
            return;
        }

        this.currRow = new byte[bytesPerRow + this.bpp];
        this.prevRow = new byte[bytesPerRow + this.bpp];

        this.filteredRows = new byte[5][bytesPerRow + this.bpp];

        final int maxValue = (1 << this.bitDepth) - 1;

        for (int row = minY + yOffset; row < minY + height; row += ySkip) {
            ras.getPixels(minX, row, width, 1, samples);

            if (this.compressGray) {
                final int shift = 8 - this.bitDepth;
                for (int i = 0; i < width; i++) {
                    samples[i] >>= shift;
                }
            }

            int count = this.bpp; // leave first 'bpp' bytes zero
            int pos = 0;
            int tmp = 0;

            switch (this.bitDepth) {
            case 1: case 2: case 4:
                // Image can only have a single band

                final int mask = samplesPerByte - 1;
                for (int s = xOffset; s < numSamples; s += xSkip) {
                    final int val = clamp(samples[s] >> this.bitShift, maxValue);
                    tmp = (tmp << this.bitDepth) | val;

                    if (pos++  == mask) {
                        this.currRow[count++] = (byte)tmp;
                        tmp = 0;
                        pos = 0;
                    }
                }

                // Left shift the last byte
                if (pos != 0) {
                    tmp <<= (samplesPerByte - pos)*this.bitDepth;
                    this.currRow[count++] = (byte)tmp;
                }
                break;

            case 8:
                for (int s = xOffset; s < numSamples; s += xSkip) {
                    for (int b = 0; b < this.numBands; b++) {
                        this.currRow[count++] =
                            (byte)clamp(samples[s + b] >> this.bitShift, maxValue);
                    }
                }
                break;

            case 16:
                for (int s = xOffset; s < numSamples; s += xSkip) {
                    for (int b = 0; b < this.numBands; b++) {
                        final int val = clamp(samples[s + b] >> this.bitShift, maxValue);
                        this.currRow[count++] = (byte)(val >> 8);
                        this.currRow[count++] = (byte)(val & 0xff);
                    }
                }
                break;
            }

            // Perform filtering
            final int filterType = this.param.filterRow(this.currRow, this.prevRow,
                                             this.filteredRows,
                                             bytesPerRow, this.bpp);

            os.write(filterType);
            os.write(this.filteredRows[filterType], this.bpp, bytesPerRow);

            // Swap current and previous rows
            final byte[] swap = this.currRow;
            this.currRow = this.prevRow;
            this.prevRow = swap;
        }
    }

    private void writeIDAT() throws IOException {
        final IDATOutputStream ios = new IDATOutputStream(this.dataOutput, 8192);
        final DeflaterOutputStream dos =
            new DeflaterOutputStream(ios, new Deflater(9));

        // Future work - don't convert entire image to a Raster It
        // might seem that you could just call image.getData() but
        // 'BufferedImage.subImage' doesn't appear to set the Width
        // and height properly of the Child Raster, so the Raster
        // you get back here appears larger than it should.
        // This solves that problem by bounding the raster to the
        // image's bounds...
        Raster ras = this.image.getData(new Rectangle(this.image.getMinX(), 
                                                 this.image.getMinY(),
                                                 this.image.getWidth(),
                                                 this.image.getHeight()));
        // System.out.println("Image: [" + 
        //                    image.getMinY()  + ", " + 
        //                    image.getMinX()  + ", " + 
        //                    image.getWidth()  + ", " + 
        //                    image.getHeight() + "]");
        // System.out.println("Ras: [" + 
        //                    ras.getMinX()  + ", " + 
        //                    ras.getMinY()  + ", " + 
        //                    ras.getWidth()  + ", " + 
        //                    ras.getHeight() + "]");

        if (this.skipAlpha) {
            final int numBands = ras.getNumBands() - 1;
            final int[] bandList = new int[numBands];
            for (int i = 0; i < numBands; i++) {
                bandList[i] = i;
            }
            ras = ras.createChild(0, 0,
                                  ras.getWidth(), ras.getHeight(),
                                  0, 0,
                                  bandList);
        }

        if (this.interlace) {
            // Interlacing pass 1
            this.encodePass(dos, ras, 0, 0, 8, 8);
            // Interlacing pass 2
            this.encodePass(dos, ras, 4, 0, 8, 8);
            // Interlacing pass 3
            this.encodePass(dos, ras, 0, 4, 4, 8);
            // Interlacing pass 4
            this.encodePass(dos, ras, 2, 0, 4, 4);
            // Interlacing pass 5
            this.encodePass(dos, ras, 0, 2, 2, 4);
            // Interlacing pass 6
            this.encodePass(dos, ras, 1, 0, 2, 2);
            // Interlacing pass 7
            this.encodePass(dos, ras, 0, 1, 1, 2);
        } else {
            this.encodePass(dos, ras, 0, 0, 1, 1);
        }

        dos.finish();
        ios.flush();
    }

    private void writeIEND() throws IOException {
        final ChunkStream cs = new ChunkStream("IEND");
        cs.writeToStream(this.dataOutput);
    }

    private static final float[] srgbChroma = {
        0.31270F, 0.329F, 0.64F, 0.33F, 0.3F, 0.6F, 0.15F, 0.06F
    };

    private void writeCHRM() throws IOException {
        if (this.param.isChromaticitySet() || this.param.isSRGBIntentSet()) {
            final ChunkStream cs = new ChunkStream("cHRM");

            float[] chroma;
            if (!this.param.isSRGBIntentSet()) {
                chroma = this.param.getChromaticity();
            } else {
                chroma = srgbChroma; // SRGB chromaticities
            }

            for (int i = 0; i < 8; i++) {
                cs.writeInt((int)(chroma[i]*100000));
            }
            cs.writeToStream(this.dataOutput);
        }
    }

    private void writeGAMA() throws IOException {
        if (this.param.isGammaSet() || this.param.isSRGBIntentSet()) {
            final ChunkStream cs = new ChunkStream("gAMA");

            float gamma;
            if (!this.param.isSRGBIntentSet()) {
                gamma = this.param.getGamma();
            } else {
                gamma = 1.0F/2.2F; // SRGB gamma
            }
            // TD should include the .5 but causes regard to say
            // everything is different.
            cs.writeInt((int)(gamma*100000/*+0.5*/));
            cs.writeToStream(this.dataOutput);
        }
    }

    private void writeICCP() throws IOException {
        if (this.param.isICCProfileDataSet()) {
            final ChunkStream cs = new ChunkStream("iCCP");
            final byte[] ICCProfileData = this.param.getICCProfileData();
            cs.write(ICCProfileData);
            cs.writeToStream(this.dataOutput);
        }
    }

    private void writeSBIT() throws IOException {
        if (this.param.isSignificantBitsSet()) {
            final ChunkStream cs = new ChunkStream("sBIT");
            final int[] significantBits = this.param.getSignificantBits();
            final int len = significantBits.length;
            for (int i = 0; i < len; i++) {
                cs.writeByte(significantBits[i]);
            }
            cs.writeToStream(this.dataOutput);
        }
    }

    private void writeSRGB() throws IOException {
        if (this.param.isSRGBIntentSet()) {
            final ChunkStream cs = new ChunkStream("sRGB");

            final int intent = this.param.getSRGBIntent();
            cs.write(intent);
            cs.writeToStream(this.dataOutput);
        }
    }

    private void writePLTE() throws IOException {
        if (this.redPalette == null) {
            return;
        }

        final ChunkStream cs = new ChunkStream("PLTE");
        for (int i = 0; i < this.redPalette.length; i++) {
            cs.writeByte(this.redPalette[i]);
            cs.writeByte(this.greenPalette[i]);
            cs.writeByte(this.bluePalette[i]);
        }

        cs.writeToStream(this.dataOutput);
    }

    private void writeBKGD() throws IOException {
        if (this.param.isBackgroundSet()) {
            final ChunkStream cs = new ChunkStream("bKGD");

            switch (this.colorType) {
            case PNG_COLOR_GRAY:
            case PNG_COLOR_GRAY_ALPHA:
                final int gray = ((PNGEncodeParam.Gray)this.param).getBackgroundGray();
                cs.writeShort(gray);
                break;

            case PNG_COLOR_PALETTE:
                final int index =
                   ((PNGEncodeParam.Palette)this.param).getBackgroundPaletteIndex();
                cs.writeByte(index);
                break;

            case PNG_COLOR_RGB:
            case PNG_COLOR_RGB_ALPHA:
                final int[] rgb = ((PNGEncodeParam.RGB)this.param).getBackgroundRGB();
                cs.writeShort(rgb[0]);
                cs.writeShort(rgb[1]);
                cs.writeShort(rgb[2]);
                break;
            }

            cs.writeToStream(this.dataOutput);
        }
    }

    private void writeHIST() throws IOException {
        if (this.param.isPaletteHistogramSet()) {
            final ChunkStream cs = new ChunkStream("hIST");

            final int[] hist = this.param.getPaletteHistogram();
            for (int i = 0; i < hist.length; i++) {
                cs.writeShort(hist[i]);
            }

            cs.writeToStream(this.dataOutput);
        }
    }

    private void writeTRNS() throws IOException {
        if (this.param.isTransparencySet() &&
            (this.colorType != PNG_COLOR_GRAY_ALPHA) &&
            (this.colorType != PNG_COLOR_RGB_ALPHA)) {
            final ChunkStream cs = new ChunkStream("tRNS");

            if (this.param instanceof PNGEncodeParam.Palette) {
                final byte[] t =
                    ((PNGEncodeParam.Palette)this.param).getPaletteTransparency();
                for (int i = 0; i < t.length; i++) {
                    cs.writeByte(t[i]);
                }
            } else if (this.param instanceof PNGEncodeParam.Gray) {
                final int t = ((PNGEncodeParam.Gray)this.param).getTransparentGray();
                cs.writeShort(t);
            } else if (this.param instanceof PNGEncodeParam.RGB) {
                final int[] t = ((PNGEncodeParam.RGB)this.param).getTransparentRGB();
                cs.writeShort(t[0]);
                cs.writeShort(t[1]);
                cs.writeShort(t[2]);
            }

            cs.writeToStream(this.dataOutput);
        } else if (this.colorType == PNG_COLOR_PALETTE) {
            final int lastEntry = Math.min(255, this.alphaPalette.length - 1);
            int nonOpaque;
            for (nonOpaque = lastEntry; nonOpaque >= 0; nonOpaque--) {
                if (this.alphaPalette[nonOpaque] != (byte)255) {
                    break;
                }
            }

            if (nonOpaque >= 0) {
                final ChunkStream cs = new ChunkStream("tRNS");
                for (int i = 0; i <= nonOpaque; i++) {
                    cs.writeByte(this.alphaPalette[i]);
                }
                cs.writeToStream(this.dataOutput);
            }
        }
    }

    private void writePHYS() throws IOException {
        if (this.param.isPhysicalDimensionSet()) {
            final ChunkStream cs = new ChunkStream("pHYs");

            final int[] dims = this.param.getPhysicalDimension();
            cs.writeInt(dims[0]);
            cs.writeInt(dims[1]);
            cs.writeByte((byte)dims[2]);

            cs.writeToStream(this.dataOutput);
        }
    }

    private void writeSPLT() throws IOException {
        if (this.param.isSuggestedPaletteSet()) {
            final ChunkStream cs = new ChunkStream("sPLT");

            System.out.println("sPLT not supported yet.");

            cs.writeToStream(this.dataOutput);
        }
    }

    private void writeTIME() throws IOException {
        if (this.param.isModificationTimeSet()) {
            final ChunkStream cs = new ChunkStream("tIME");

            final Date date = this.param.getModificationTime();
            final TimeZone gmt = TimeZone.getTimeZone("GMT");

            final GregorianCalendar cal = new GregorianCalendar(gmt);
            cal.setTime(date);

            final int year = cal.get(Calendar.YEAR);
            final int month = cal.get(Calendar.MONTH);
            final int day = cal.get(Calendar.DAY_OF_MONTH);
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            final int second = cal.get(Calendar.SECOND);

            cs.writeShort(year);
            cs.writeByte(month + 1);
            cs.writeByte(day);
            cs.writeByte(hour);
            cs.writeByte(minute);
            cs.writeByte(second);

            cs.writeToStream(this.dataOutput);
        }
    }

    private void writeTEXT() throws IOException {
        if (this.param.isTextSet()) {
            final String[] text = this.param.getText();

            for (int i = 0; i < text.length/2; i++) {
                final byte[] keyword = text[2*i].getBytes();
                final byte[] value = text[2*i + 1].getBytes();

                final ChunkStream cs = new ChunkStream("tEXt");

                cs.write(keyword, 0, Math.min(keyword.length, 79));
                cs.write(0);
                cs.write(value);

                cs.writeToStream(this.dataOutput);
            }
        }
    }

    private void writeZTXT() throws IOException {
        if (this.param.isCompressedTextSet()) {
            final String[] text = this.param.getCompressedText();

            for (int i = 0; i < text.length/2; i++) {
                final byte[] keyword = text[2*i].getBytes();
                final byte[] value = text[2*i + 1].getBytes();

                final ChunkStream cs = new ChunkStream("zTXt");

                cs.write(keyword, 0, Math.min(keyword.length, 79));
                cs.write(0);
                cs.write(0);

                final DeflaterOutputStream dos = new DeflaterOutputStream(cs);
                dos.write(value);
                dos.finish();

                cs.writeToStream(this.dataOutput);
            }
        }
    }

    private void writePrivateChunks() throws IOException {
        final int numChunks = this.param.getNumPrivateChunks();
        for (int i = 0; i < numChunks; i++) {
            final String type = this.param.getPrivateChunkType(i);
            final byte[] data = this.param.getPrivateChunkData(i);

            final ChunkStream cs = new ChunkStream(type);
            cs.write(data);
            cs.writeToStream(this.dataOutput);
        }
    }

    /**
     * Analyzes a set of palettes and determines if it can be expressed
     * as a standard set of gray values, with zero or one values being
     * fully transparent and the rest being fully opaque.  If it
     * is possible to express the data thusly, the method returns
     * a suitable instance of PNGEncodeParam.Gray; otherwise it
     * returns null.
     */
    private PNGEncodeParam.Gray createGrayParam(final byte[] redPalette,
                                                final byte[] greenPalette,
                                                final byte[] bluePalette,
                                                final byte[] alphaPalette) {
        final PNGEncodeParam.Gray param = new PNGEncodeParam.Gray();
        int numTransparent = 0;

        final int grayFactor = 255/((1 << this.bitDepth) - 1);
        final int entries = 1 << this.bitDepth;
        for (int i = 0; i < entries; i++) {
            final byte red = redPalette[i];
            if ((red != i*grayFactor) ||
                (red != greenPalette[i]) ||
                (red != bluePalette[i])) {
                return null;
            }

            // All alphas must be 255 except at most 1 can be 0
            final byte alpha = alphaPalette[i];
            if (alpha == (byte)0) {
                param.setTransparentGray(i);

                ++numTransparent;
                if (numTransparent > 1) {
                    return null;
                }
            } else if (alpha != (byte)255) {
                return null;
            }
        }

        return param;
    }

    /**
     * This method encodes a <code>RenderedImage</code> into PNG.
     * The stream into which the PNG is dumped is not closed at
     * the end of the operation, this should be done if needed
     * by the caller of this method.
     */
    public void encode(final RenderedImage im) throws IOException {
        this.image = im;
        this.width = this.image.getWidth();
        this.height = this.image.getHeight();

        final SampleModel sampleModel = this.image.getSampleModel();

        final int[] sampleSize = sampleModel.getSampleSize();

        // Set bitDepth to a sentinel value
        this.bitDepth = -1;
        this.bitShift = 0;

        // Allow user to override the bit depth of gray images
        if (this.param instanceof PNGEncodeParam.Gray) {
            final PNGEncodeParam.Gray paramg = (PNGEncodeParam.Gray)this.param;
            if (paramg.isBitDepthSet()) {
                this.bitDepth = paramg.getBitDepth();
            }

            if (paramg.isBitShiftSet()) {
                this.bitShift = paramg.getBitShift();
            }
        }

        // Get bit depth from image if not set in param
        if (this.bitDepth == -1) {
            // Get bit depth from channel 0 of the image

            this.bitDepth = sampleSize[0];
            // Ensure all channels have the same bit depth
            for (int i = 1; i < sampleSize.length; i++) {
                if (sampleSize[i] != this.bitDepth) {
                    throw new RuntimeException();
                }
            }

            // Round bit depth up to a power of 2
            if (this.bitDepth > 2 && this.bitDepth < 4) {
                this.bitDepth = 4;
            } else if (this.bitDepth > 4 && this.bitDepth < 8) {
                this.bitDepth = 8;
            } else if (this.bitDepth > 8 && this.bitDepth < 16) {
                this.bitDepth = 16;
            } else if (this.bitDepth > 16) {
                throw new RuntimeException();
            }
        }

        this.numBands = sampleModel.getNumBands();
        this.bpp = this.numBands*((this.bitDepth == 16) ? 2 : 1);

        final ColorModel colorModel = this.image.getColorModel();
        if (colorModel instanceof IndexColorModel) {
            if (this.bitDepth < 1 || this.bitDepth > 8) {
                throw new RuntimeException();
            }
            if (sampleModel.getNumBands() != 1) {
                throw new RuntimeException();
            }

            final IndexColorModel icm = (IndexColorModel)colorModel;
            int size = icm.getMapSize();

            this.redPalette = new byte[size];
            this.greenPalette = new byte[size];
            this.bluePalette = new byte[size];
            this.alphaPalette = new byte[size];

            icm.getReds(this.redPalette);
            icm.getGreens(this.greenPalette);
            icm.getBlues(this.bluePalette);
            icm.getAlphas(this.alphaPalette);

            this.bpp = 1;

            if (this.param == null) {
                this.param = this.createGrayParam(this.redPalette,
                                        this.greenPalette,
                                        this.bluePalette,
                                        this.alphaPalette);
            }

            // If param is still null, it can't be expressed as gray
            if (this.param == null) {
                this.param = new PNGEncodeParam.Palette();
            }

            if (this.param instanceof PNGEncodeParam.Palette) {
                // If palette not set in param, create one from the ColorModel.
                final PNGEncodeParam.Palette parami = (PNGEncodeParam.Palette)this.param;
                if (parami.isPaletteSet()) {
                    final int[] palette = parami.getPalette();
                    size = palette.length/3;

                    int index = 0;
                    for (int i = 0; i < size; i++) {
                        this.redPalette[i] = (byte)palette[index++];
                        this.greenPalette[i] = (byte)palette[index++];
                        this.bluePalette[i] = (byte)palette[index++];
                        this.alphaPalette[i] = (byte)255;
                    }
                }
                this.colorType = PNG_COLOR_PALETTE;
            } else if (this.param instanceof PNGEncodeParam.Gray) {
                this.redPalette = this.greenPalette = this.bluePalette = this.alphaPalette = null;
                this.colorType = PNG_COLOR_GRAY;
            } else {
                throw new RuntimeException();
            }
        } else if (this.numBands == 1) {
            if (this.param == null) {
                this.param = new PNGEncodeParam.Gray();
            }
            this.colorType = PNG_COLOR_GRAY;
        } else if (this.numBands == 2) {
            if (this.param == null) {
                this.param = new PNGEncodeParam.Gray();
            }

            if (this.param.isTransparencySet()) {
                this.skipAlpha = true;
                this.numBands = 1;
                if ((sampleSize[0] == 8) && (this.bitDepth < 8)) {
                    this.compressGray = true;
                }
                this.bpp = (this.bitDepth == 16) ? 2 : 1;
                this.colorType = PNG_COLOR_GRAY;
            } else {
                if (this.bitDepth < 8) {
                    this.bitDepth = 8;
                }
                this.colorType = PNG_COLOR_GRAY_ALPHA;
            }
        } else if (this.numBands == 3) {
            if (this.param == null) {
                this.param = new PNGEncodeParam.RGB();
            }
            this.colorType = PNG_COLOR_RGB;
        } else if (this.numBands == 4) {
            if (this.param == null) {
                this.param = new PNGEncodeParam.RGB();
            }
            if (this.param.isTransparencySet()) {
                this.skipAlpha = true;
                this.numBands = 3;
                this.bpp = (this.bitDepth == 16) ? 6 : 3;
                this.colorType = PNG_COLOR_RGB;
            } else {
                this.colorType = PNG_COLOR_RGB_ALPHA;
            }
        }

        this.interlace = this.param.getInterlacing();

        this.writeMagic();

        this.writeIHDR();

        this.writeCHRM();
        this.writeGAMA();
        this.writeICCP();
        this.writeSBIT();
        this.writeSRGB();

        this.writePLTE();

        this.writeHIST();
        this.writeTRNS();
        this.writeBKGD();

        this.writePHYS();
        this.writeSPLT();
        this.writeTIME();
        this.writeTEXT();
        this.writeZTXT();

        this.writePrivateChunks();

        this.writeIDAT();

        this.writeIEND();

        this.dataOutput.flush();
    }
}
