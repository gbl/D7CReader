/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram;

/**
 *
 * @author gbl
 */
public class D7CColor {
    byte n, r, g, b;
    D7CColor(byte[] blob, int startpos) {
        n=blob[startpos];
        r=blob[startpos+6];
        g=blob[startpos+7];
        b=blob[startpos+8];
    }
    
    int getN() { return n; }
    int getR() { return r; }
    int getG() { return g; }
    int getB() { return b; }
    @Override
    public String toString() { return String.format("%02x (%02x,%02x,%02x)", n, r, g, b); }
}
