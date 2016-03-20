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
        D7CBlob blob=new D7CBlob(new File("/home/gbl/Temp/SilverKnit/gaucho.stp"));
        blob.test();
    }
    
}
