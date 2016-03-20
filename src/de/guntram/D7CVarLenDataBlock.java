/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram;

import java.util.Arrays;

/**
 *
 * @author gbl
 */
public class D7CVarLenDataBlock {
    private final int height;
    private final int nbytes;
    private final byte[] data;
    
    D7CVarLenDataBlock(byte[] buffer, int startpos) {
        this(buffer, startpos, null);
    }
    
    D7CVarLenDataBlock(byte[] buffer, int startpos, byte[] xorkey) {
        height=getWordAt(buffer, startpos);
        nbytes=getWordAt(buffer, startpos+2);
        if (xorkey != null) {
            data=new byte[nbytes];
            for (int i=0; i<nbytes; i++)
                data[i]=(byte)(buffer[startpos+4+i]^xorkey[i]);
        } else {
            data=Arrays.copyOfRange(buffer, startpos+4, startpos+4+nbytes-1);
        }
    }
    
    int getHeight()  { return height; }
    int getNbytes()  { return nbytes; }
    byte[] getData() { return data; }
    
    private int getByteAt(byte[] data, int pos) {
        return (data[pos]&0xff);
    }

    private int getWordAt(byte[] data, int pos) {
        return getByteAt(data, pos) | (getByteAt(data, pos+1)<<8);
    }
}
