/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gbl
 */
public class D7CBlob {
    private final int maxXorLength=21000;
    private final int length;
    private final byte data[];
    private boolean debugging;
    
    private boolean haveFirstDecryptionNumber;  private int firstDecryptionNumber;
    private boolean haveFirstKeyString;         private String firstKeyString;
    private boolean haveSecondDecryptionNumber; private int secondDecryptionNumber;
    private boolean haveSecondKeyString;        private String secondKeyString;
    private boolean haveXorKey;                 private byte xorKey[];
    
    private boolean dataBlocksInitialized;
    private List<D7CVarLenDataBlock> firstBlocks, secondBlocks;
    private int paletteStart;
    private int paletteRemap;
    
    public D7CBlob(File file) throws FileNotFoundException, IOException {
        length=(int) file.length();
        data=new byte[length];
        haveFirstDecryptionNumber=false;
        haveFirstKeyString=false;
        haveSecondDecryptionNumber=false;
        haveSecondKeyString=false;
        haveXorKey=false;
        dataBlocksInitialized=false;
        debugging=false;
        try (InputStream reader = new FileInputStream(file)) {
            int pos=0, n;
            while (pos<length) {
                n=reader.read(data, pos, length-pos);
                if (n<=0) {
                    throw new IOException("End of File reached before reading enough bytes");
                }
                pos+=n;
            }
        }
    }
    
    public void setDebugging(boolean b) {
        debugging=b;
    }
    
    public int getHeight() {
        return getWordAt(5);
    }
    
    public int getWidth() {
        return getWordAt(3);
    }
    
    private int getByteAt(int pos) {
        return (data[pos]&0xff);
    }
    
    private int getWordAt(int pos) {
        return getByteAt(pos) | (getByteAt(pos+1)<<8);
    }
    
    private int getDwordAt(int pos) {
        return getByteAt(pos) |
                (getByteAt(pos+1)<<8) |
                (getByteAt(pos+2)<<16) |
                (getByteAt(pos+3)<<24);
    }
    
    /* Pascal strings! */
    private String getStringAt(int pos) {
        int size=getByteAt(pos);
        char[] chars=new char[size];
        for (int i=0; i<size; i++) {
            chars[i]=(char) getByteAt(pos+i+1);
        }
        return new String(chars);
    }
    
    private void debug(int val) { if (debugging) System.out.println(Integer.toHexString(val)); }
    private void debug(String s, int val) { if (debugging) System.out.println(s+": "+Integer.toHexString(val)); }
    private void debug(String s, String t) { if (debugging) System.out.println(s+": "+t); }

    private int getFirstDecryptionNumber() {
        if (!haveFirstDecryptionNumber) {
            int temp;
            firstDecryptionNumber=getDwordAt(0x35)/2; debug(firstDecryptionNumber);
            temp=getByteAt(0x3f)*4;                   debug(temp); firstDecryptionNumber+=temp; debug(firstDecryptionNumber);
            temp=getDwordAt(0x39);                    debug(temp); firstDecryptionNumber+=temp; debug(firstDecryptionNumber);
            temp=getWordAt(0x3d);                     debug(temp); firstDecryptionNumber+=temp; debug(firstDecryptionNumber);
            temp=getByteAt(0x20);                     debug(temp); firstDecryptionNumber+=temp; debug(firstDecryptionNumber);
            haveFirstDecryptionNumber=true;
        }
        return firstDecryptionNumber;
    }
    
    private String getFirstKeyString() {
        if (!haveFirstKeyString) {
            firstKeyString=new String();
            firstKeyString+=getStringAt(0x60);
            firstKeyString+=getStringAt(0x41);
            firstKeyString+=getWordAt(0x3d);
            firstKeyString+=getByteAt(0x20);
            firstKeyString+=getStringAt(0x41);
            firstKeyString+=getByteAt(0x20);
            firstKeyString+=getWordAt(0x3d);
            debug("First Key String", firstKeyString);
            haveFirstKeyString=true;
        }
        
        return firstKeyString;
    }
    
    private int getSecondDecryptionNumber() {
        if (!haveSecondDecryptionNumber) {
            int salt1=getWordAt(0x39);
            int salt2=(getDwordAt(0x35)&0xfff)>0 ? 1 : 0;
            secondDecryptionNumber=getFirstDecryptionNumber();
            String tempString=getFirstKeyString();
            for (int i=0; i<tempString.length(); i++) {
                byte b=(byte)(tempString.charAt(i)/2);
                switch (i%3) {
                    // Warning: The original disassembly has these 1-indexed, as
                    // they use pascal strings there. We use 0-indexed. So be
                    // careful when comparing these case statements to the original
                    // ones.
                    case 0:
                        int temp=b/5*getWordAt(0x3f);
                        secondDecryptionNumber+=(i+1)*salt2;
                        secondDecryptionNumber+=b*6;
                        secondDecryptionNumber+=temp;
                        debug("after type 0", secondDecryptionNumber);
                        break;
                    case 1:
                        secondDecryptionNumber+=(i+1)*salt1;
                        secondDecryptionNumber+=b*4;
                        debug("after type 1", secondDecryptionNumber);
                        break;
                    case 2:
                        temp=(salt2+b)/7;
                        secondDecryptionNumber+=(i+1)*b+temp;
                        debug("after type 2", secondDecryptionNumber);
                        break;
                }
            }
            debug("calculated 2nd number", secondDecryptionNumber);
            haveSecondDecryptionNumber=true;
        }
        return secondDecryptionNumber;
    }
    
    private String getSecondKeyString() {
        if (!haveSecondKeyString) {
            int val=getSecondDecryptionNumber();
            secondKeyString= ""+
                    val*3  +
                    val    +
                    val*4  +
                    val*2  +
                    val*5  +
                    val*6  +
                    val*8  +
                    val*7;
            debug("Second Key String", secondKeyString);
            haveSecondKeyString=true;
        }
        return secondKeyString;
    }
    
    private byte[] getPatternXorKey() {
        if (!haveXorKey) {
            String key=getSecondKeyString();
            int val=getSecondDecryptionNumber();
            xorKey=new byte[maxXorLength];
            for (int i=0; i<maxXorLength; i++) {
                int index=(((i+1)%key.length()));
                byte temp1=(byte)key.charAt(index);
                byte temp2=(byte)((val%(i+1))&0xff);
                xorKey[i] = (byte)(temp1 ^ temp2);
            }
            haveXorKey=true;
        }
        return xorKey;
    }
    
    private void initDataBlocks() {
        if (!dataBlocksInitialized) {
            int startPos=0xf8;
            byte[] key=getPatternXorKey();
            D7CVarLenDataBlock nextBlock;
            firstBlocks=new ArrayList<>();
            secondBlocks=new ArrayList<>();
            do {
                nextBlock=new D7CVarLenDataBlock(data, startPos, key);
                firstBlocks.add(nextBlock);
                if (debugging) {
                    System.out.println("Block at "+Integer.toHexString(startPos)+
                        " height "+nextBlock.getHeight()+" has "+
                            Integer.toHexString(nextBlock.getNbytes())+" bytes, "+
                            "next start at "+Integer.toHexString(startPos+nextBlock.getNbytes()+4)
                        );
                }
                startPos+=nextBlock.getNbytes()+4;
            } while (nextBlock.getHeight()!=this.getHeight());
            do {
                nextBlock=new D7CVarLenDataBlock(data, startPos, key);
                secondBlocks.add(nextBlock);
                if (debugging) {
                    System.out.println("Block at "+Integer.toHexString(startPos)+
                        " height "+nextBlock.getHeight()+" has "+
                            Integer.toHexString(nextBlock.getNbytes())+" bytes, "+
                            "next start at "+Integer.toHexString(startPos+nextBlock.getNbytes()+4)
                        );
                }
                startPos+=nextBlock.getNbytes()+4;
            } while (nextBlock.getHeight()!=this.getHeight());

            paletteStart=startPos;  debug("Palette starts at", paletteStart); startPos+=1775;
            paletteRemap=startPos;  debug("Palette remap  at", paletteRemap);
            dataBlocksInitialized=true;
        }
    }
    
    private void decodeRLEData(byte[] pixels, List<D7CVarLenDataBlock>blocks,
            int height, int width, int remapStart) {
        int pixelRowStart=0;
        int blockno=0;
        int posInData=0;
        int posOutData=0;
        byte[] inputData=blocks.get(0).getData();
        for (int row=0; row<height; row++) {
            if (row==blocks.get(blockno).getHeight()) {
                inputData=blocks.get(++blockno).getData();
                posInData=0;
            }
            for (int col=0; col<width; ) {
                byte b=inputData[posInData++];
                int  len=1;
                if ((b&0x80)!=0) {
                    len=b&0x7f;
                    b=inputData[posInData++];
                }
                if (remapStart!=0) {
                    b=this.data[remapStart+b*2-2];
                }
                for (int i=0; i<len; i++) {
                    pixels[posOutData++]=b;
                }
                col+=len;
            }
        }
    }
    
    public D7CColor getColor(int index) {
        initDataBlocks();
        return new D7CColor(data, paletteStart+index*25);
    }
    
    public byte[] getDataBlock2Pixels() {
        byte[] pixels=new byte[getWidth()*getHeight()];
        initDataBlocks();
        decodeRLEData(pixels, secondBlocks, getHeight(), getWidth(), paletteRemap);
        return pixels;
    }
    
    public byte[] getDataBlock1Pixels() {
        byte[] pixels=new byte[getWidth()*getHeight()];
        initDataBlocks();
        decodeRLEData(pixels, firstBlocks, getHeight(), getWidth(), 0);
        return pixels;
    }
}
