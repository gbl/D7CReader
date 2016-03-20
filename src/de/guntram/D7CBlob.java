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

/**
 *
 * @author gbl
 */
public class D7CBlob {
    private final int maxXorLength=21000;
    private final int length;
    private final byte data[];
    private boolean debugging=false;
    
    public D7CBlob(File file) throws FileNotFoundException, IOException {
        length=(int) file.length();
        data=new byte[length];
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
    
    public int getByteAt(int pos) {
        return (data[pos]&0xff);
    }
    
    public int getWordAt(int pos) {
        return getByteAt(pos) | (getByteAt(pos+1)<<8);
    }
    
    public int getDwordAt(int pos) {
        return getByteAt(pos) |
                (getByteAt(pos+1)<<8) |
                (getByteAt(pos+2)<<16) |
                (getByteAt(pos+3)<<24);
    }
    
    /* Pascal strings! */
    public String getStringAt(int pos) {
        int size=getByteAt(pos);
        char[] chars=new char[size];
        for (int i=0; i<size; i++) {
            chars[i]=(char) getByteAt(pos+i+1);
        }
        return new String(chars);
    }
    
    private void debug(int val) { System.out.println(Integer.toHexString(val)); }

    private int getInitialDecryptionNumber() {
        int result, temp;
        result=getDwordAt(0x35)/2; debug(result);
        temp=getByteAt(0x3f)*4; debug(temp); result+=temp; debug(result);
        temp=getDwordAt(0x39);  debug(temp); result+=temp; debug(result);
        temp=getWordAt(0x3d);   debug(temp); result+=temp; debug(result);
        temp=getByteAt(0x20);   debug(temp); result+=temp; debug(result);
        return result;
    }
    
    private String getFirstKeyString() {
        String decryptionString="";
        decryptionString+=getStringAt(0x60);
        decryptionString+=getStringAt(0x41);
        decryptionString+=getWordAt(0x3d);
        decryptionString+=getByteAt(0x20);
        decryptionString+=getStringAt(0x41);
        decryptionString+=getByteAt(0x20);
        decryptionString+=getWordAt(0x3d);
        if (debugging) { System.out.println(decryptionString); }
        
        return decryptionString;
    }
    
    private int getSecondDecryptionNumber() {
        int var4=getWordAt(0x39);
        int var8=(getDwordAt(0x35)&0xfff)>0 ? 1 : 0;
        int result=getInitialDecryptionNumber();
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
                    result+=(i+1)*var8;
                    result+=b*6;
                    result+=temp;
                    debug(result);
                    break;
                case 1:
                    result+=(i+1)*var4;
                    result+=b*4;
                    debug(result);
                    break;
                case 2:
                    temp=(var8+b)/7;
                    result+=(i+1)*b+temp;
                    debug(result);
                    break;
            }
        }
        debug(result);
        return result;
    }
    
    private String getSecondKeyString() {
        int val=getSecondDecryptionNumber();
        String result= ""+
                val*3  +
                val    +
                val*4  +
                val*2  +
                val*5  +
                val*6  +
                val*8  +
                val*7;
        if (debugging) System.out.println(result);
        return result;
    }
    
    public byte[] getPatternXorKey() {
        String key=getSecondKeyString();
        int val=getSecondDecryptionNumber();
        byte[] result=new byte[maxXorLength];
        for (int i=0; i<maxXorLength; i++) {
            int index=(((i+1)%key.length()));
            byte temp1=(byte)key.charAt(index);
            byte temp2=(byte)((val%(i+1))&0xff);
            result[i] = (byte)(temp1 ^ temp2);
        }
        return result;
    }
    
    public void test() {
        setDebugging(true);
        getPatternXorKey();
    }
}
