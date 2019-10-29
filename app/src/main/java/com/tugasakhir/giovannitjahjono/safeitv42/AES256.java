package com.tugasakhir.giovannitjahjono.safeitv42;

import android.util.Base64;

import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256 {
    //Prepare variable that will save the AES256 key
    String aesKeyString;
    //Method to generate the key
    public void KeyMaker(){
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(256);
            SecretKey skey = kgen.generateKey();
            //byte[] keyd = skey.getEncoded();
            aesKeyString = Base64.encodeToString(skey.getEncoded(), 0);
        } catch (Exception e){}
    }
    //Method to encrypt
    public static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }
    //Method to decrypt
    public static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }
}
