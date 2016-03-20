/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author gbl
 */
public class D7CReader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String filename;
        if (args.length>=1)
            filename=args[0];
        else
            filename="sterne.stp";
        D7CBlob blob=new D7CBlob(new File(filename));
        blob.setDebugging(false);
        System.out.println("Stitch color map");
        byte[] pixels=blob.getDataBlock1Pixels();
        int pos;
        for (int i=0; i<blob.getHeight(); i++) {
            pos=(blob.getHeight()-i-1)*blob.getWidth();
            for (int j=0; j<blob.getWidth(); j++) 
                System.out.print((char)pixels[pos++]);
            System.out.println();
        }
        System.out.println("Color map");
        for (int i=32; i<75; i++) {
            System.out.println("'"+(char)i+"': "+blob.getColor(i).toString());
        }
        System.out.println("Stitch type map, decoding not completed");
        pixels=blob.getDataBlock2Pixels();
        for (int i=0; i<blob.getHeight(); i++) {
            pos=(blob.getHeight()-i-1)*blob.getWidth();
            for (int j=0; j<blob.getWidth(); j++) 
                System.out.print((char)pixels[pos++]);
            System.out.println();
        }
    }
}
