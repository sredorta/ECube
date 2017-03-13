package com.ecube.solutions.ecube.network;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by sredorta on 2/8/2017.
 */


public class Encryption {
    //Logs
    private static final String TAG = Encryption.class.getSimpleName();
    private static final boolean DEBUG = false;

    static char[] HEX_CHARS = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

    private String iv = "fdsfds85435nfdfs";//Dummy iv (CHANGE IT!)
    private String SecretKey = "89432hjfsd891787";//Dummy secretKey (CHANGE IT!)

    private IvParameterSpec ivspec;
    private SecretKeySpec keyspec;
    private Cipher cipher;


    public Encryption() {
        ivspec = new IvParameterSpec(iv.getBytes());
        keyspec = new SecretKeySpec(SecretKey.getBytes(), "AES");

        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public byte[] encrypt(String text) throws Exception {
        if(text == null || text.length() == 0)
                throw new Exception("Empty string");

        byte[] encrypted = null;

        try {
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            encrypted = cipher.doFinal(padString(text).getBytes());
        } catch (Exception e) {
                throw new Exception("[encrypt] " + e.getMessage());
        }

        return encrypted;
    }

    public byte[] decrypt(String code) throws Exception {
        if(code == null || code.length() == 0)
            throw new Exception("Empty string");

        byte[] decrypted = null;

        try {
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

            decrypted = cipher.doFinal(hexToBytes(code));
            //Remove trailing zeroes
            if( decrypted.length > 0) {
                int trim = 0;
                for( int i = decrypted.length - 1; i >= 0; i-- ) if( decrypted[i] == 0 ) trim++;

                if( trim > 0 ) {
                    byte[] newArray = new byte[decrypted.length - trim];
                    System.arraycopy(decrypted, 0, newArray, 0, decrypted.length - trim);
                    decrypted = newArray;
                }
            }
        } catch (Exception e) {
            throw new Exception("[decrypt] " + e.getMessage());
        }
        return decrypted;
    }


    public static String bytesToHex(byte[] buf) {
            char[] chars = new char[2 * buf.length];
            for (int i = 0; i < buf.length; ++i) {
                chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
                chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
            }
            return new String(chars);
        }


    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    //Remove any non hex character from a string
    public static String hexToString(String in) {
        return in.replaceAll("[^a-f0-9]", "");
    }

    private static String padString(String source) {
        char paddingChar = 0;
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;

        for (int i = 0; i < padLength; i++) {
            source += paddingChar;
        }
        return source;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SHA1 part for passwords
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    private static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] textBytes = text.getBytes("UTF-8");
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    public static String getSHA1(String myStr) {
        String encryptPass = null;
        if (myStr!=null) {
            try {
                encryptPass = Encryption.SHA1(myStr);
            } catch (Exception e) {
                Log.i(TAG, "Caught exception: " + e);
            }
            if (DEBUG) Log.i(TAG, "Encrypted password: " + encryptPass);
        }
        return encryptPass;
    }

}

/* Java usage
                   Encryption cipher = new Encryption();
                String s = new String();
                s = "this is a test1";
                String encrypted = new String();

                try {
                    encrypted = Encryption.bytesToHex(cipher.encrypt(s));
                    Logs.i("Encrypted: " + encrypted);
                } catch (Exception e) {
                    Logs.i("Caught exception: " + e);
                }
                try {
                    String decrypted = new String( cipher.decrypt(encrypted ));
                    Logs.i("Decrypted: " + decrypted);
                } catch (Exception e) {
                    Logs.i("Caught exception: " + e);
                }
    */



/*In PHP that what is needed:
<?php
class MCrypt
{
    private $iv = 'fedcba9876543210'; #Same as in JAVA
    private $key = '0123456789abcdef'; #Same as in JAVA
    function __construct()
    {
    }

function encrypt($str, $isBinary = false)
{
    $iv = $this->iv;
    $str = $isBinary ? $str : utf8_decode($str);
    $td = mcrypt_module_open('rijndael-128', ' ', 'cbc', $iv);
    mcrypt_generic_init($td, $this->key, $iv);
    $encrypted = mcrypt_generic($td, $str);
    mcrypt_generic_deinit($td);
    mcrypt_module_close($td);
    return $isBinary ? $encrypted : bin2hex($encrypted);
}
*/
    /*
    function decrypt($code, $isBinary = false)
    {
        $code = $isBinary ? $code : $this->hex2bin($code);
        $iv = $this->iv;
        $td = mcrypt_module_open('rijndael-128', ' ', 'cbc', $iv);
        mcrypt_generic_init($td, $this->key, $iv);
        $decrypted = mdecrypt_generic($td, $code);
        mcrypt_generic_deinit($td);
        mcrypt_module_close($td);
        return $isBinary ? trim($decrypted) : utf8_encode(trim($decrypted));
    }
    protected function hex2bin($hexdata)
    {
        $bindata = '';
        for ($i = 0; $i < strlen($hexdata); $i += 2) {
            $bindata .= chr(hexdec(substr($hexdata, $i, 2)));
        }
        return $bindata;
    }
}
 */

    /*PHP usage
    $mcrypt = new MCrypt();


    $encrypted = $mcrypt->encrypt("Text to encrypt");
    $decrypted = $mcrypt->decrypt($encrypted);
     */

