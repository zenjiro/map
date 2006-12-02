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
package org.apache.batik.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This class implements a Base64 Character encoder as specified in RFC1113.
 * Unlike some other encoding schemes there is nothing in this encoding
 * that indicates where a buffer starts or ends.
 *
 * This means that the encoded text will simply start with the first line
 * of encoded text and end with the last line of encoded text.
 *
 * @author <a href="mailto:deweese@apache.org">Thomas DeWeese</a>
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @author      Chuck McManis
 * @version $Id: Base64EncoderStream.java,v 1.6 2005/03/27 08:58:36 cam Exp $
 */
public class Base64EncoderStream extends OutputStream {

    /** This array maps the 6 bit values to their characters */
    private final static byte pem_array[] = {
    //   0   1   2   3   4   5   6   7
        'A','B','C','D','E','F','G','H', // 0
        'I','J','K','L','M','N','O','P', // 1
        'Q','R','S','T','U','V','W','X', // 2
        'Y','Z','a','b','c','d','e','f', // 3
        'g','h','i','j','k','l','m','n', // 4
        'o','p','q','r','s','t','u','v', // 5
        'w','x','y','z','0','1','2','3', // 6
        '4','5','6','7','8','9','+','/'  // 7
    };

    byte [] atom = new byte[3];
    int     atomLen = 0;
    byte [] encodeBuf = new byte[4];
    int     lineLen = 0;

    PrintStream  out;
    boolean closeOutOnClose;

    public Base64EncoderStream(final OutputStream out) {
        this.out = new PrintStream(out);
        this.closeOutOnClose = true;
    }

    public Base64EncoderStream(final OutputStream out, final boolean closeOutOnClose) {
        this.out = new PrintStream(out);
        this.closeOutOnClose = closeOutOnClose;
    }

    public void close () throws IOException {
        if (this.out != null) {
            this.encodeAtom();
            this.out.flush();        
            if (this.closeOutOnClose)
                this.out.close();
            this.out=null;
        }
    }

    /**
     * This can't really flush out output since that may generate
     * '=' chars which would indicate the end of the stream.
     * Instead we flush out.  You can only be sure all output is 
     * writen by closing this stream.
     */
    public void flush() throws IOException {
        this.out.flush();
    }

    public void write(final int b) throws IOException {
        this.atom[this.atomLen++] = (byte)b;
        if (this.atomLen == 3)
            this.encodeAtom();
    }

    public void write(final byte []data) throws IOException {
        this.encodeFromArray(data, 0, data.length);
    }

    public void write(final byte [] data, final int off, final int len) throws IOException {
        this.encodeFromArray(data, off, len);
    }

    /**
     * enocodeAtom - Take three bytes of input and encode it as 4
     * printable characters. Note that if the length in len is less
     * than three is encodes either one or two '=' signs to indicate
     * padding characters.
     */
    void encodeAtom() throws IOException {
        byte a, b, c;

        switch (this.atomLen) {
        case 0: return;
        case 1:
            a = this.atom[0];
            this.encodeBuf[0] = pem_array[((a >>> 2) & 0x3F)];
            this.encodeBuf[1] = pem_array[((a <<  4) & 0x30)];
            this.encodeBuf[2] = this.encodeBuf[3] = '=';
            break;
        case 2:
            a = this.atom[0];
            b = this.atom[1];
            this.encodeBuf[0] = pem_array[((a >>> 2) & 0x3F)];
            this.encodeBuf[1] = pem_array[(((a << 4) & 0x30) | ((b >>> 4) & 0x0F))];
            this.encodeBuf[2] = pem_array[((b  << 2) & 0x3C)];
            this.encodeBuf[3] = '=';
            break;
        default:
            a = this.atom[0];
            b = this.atom[1];
            c = this.atom[2];
            this.encodeBuf[0] = pem_array[((a >>> 2) & 0x3F)];
            this.encodeBuf[1] = pem_array[(((a << 4) & 0x30) | ((b >>> 4) & 0x0F))];
            this.encodeBuf[2] = pem_array[(((b << 2) & 0x3C) | ((c >>> 6) & 0x03))];
            this.encodeBuf[3] = pem_array[c & 0x3F];
        }
        if (this.lineLen == 64) {
            this.out.println();
            this.lineLen = 0;
        }
        this.out.write(this.encodeBuf);

        this.lineLen += 4;
        this.atomLen = 0;
    }

    /**
     * enocodeAtom - Take three bytes of input and encode it as 4
     * printable characters. Note that if the length in len is less
     * than three is encodes either one or two '=' signs to indicate
     * padding characters.
     */
    void encodeFromArray(final byte[] data, int offset, int len) 
        throws IOException{
        byte a, b, c;
        if (len == 0)
            return;

        // System.out.println("atomLen: " + atomLen + 
        //                    " len: " + len + 
        //                    " offset:  " + offset);

        if (this.atomLen != 0) {
            switch(this.atomLen) {
            case 1:
                this.atom[1] = data[offset++]; len--; this.atomLen++;
                if (len == 0) return;
                this.atom[2] = data[offset++]; len--; this.atomLen++;
                break;
            case 2:
                this.atom[2] = data[offset++]; len--; this.atomLen++;
                break;
            default:
            }
            this.encodeAtom();
        }

        while (len >=3) {
            a = data[offset++];
            b = data[offset++];
            c = data[offset++];
            
            this.encodeBuf[0] = pem_array[((a >>> 2) & 0x3F)];
            this.encodeBuf[1] = pem_array[(((a << 4) & 0x30) | ((b >>> 4) & 0x0F))];
            this.encodeBuf[2] = pem_array[(((b << 2) & 0x3C) | ((c >>> 6) & 0x03))];
            this.encodeBuf[3] = pem_array[c & 0x3F];
            this.out.write(this.encodeBuf);

            this.lineLen += 4;
            if (this.lineLen == 64) {
                this.out.println();
                this.lineLen = 0;
            }

            len -=3;
        }

        switch (len) {
        case 1:
            this.atom[0] = data[offset];
            break;
        case 2:
            this.atom[0] = data[offset];
            this.atom[1] = data[offset+1];
            break;
        default:
        }
        this.atomLen = len;
    }

    
    
}
