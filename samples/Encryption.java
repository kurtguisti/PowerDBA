package samples;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import org.apache.axis.encoding.*;

import com.powerdba.util.Tracer;

/**

 * Title: Encryption class using Blowfish

 * Description: 

 * Copyright: Ajay Swamy Copyright (c) 2003

 * Company: 

 * @author Ajay Swamy

 * @version 1.0

 */


public class Encryption {


  static final int keylength = 42;

  SecretKey key;

  String permission;




  public Encryption(String perm, SecretKey k) {



    this.permission=perm;

    this.key=k;



  }



  public SecretKey getkey() {

  return key;

  }




  private String returnKey() {


	//Do whatever here
  	  return "";


  }





  public static void main(String[] args) {


    try {
    	
    	String testString = "kwg5377";
    	
    	System.out.println("Testing String " + testString);
    	
    	String encryptResult = encryptAndEncode(testString);
    	
    	System.out.println("Encrypted and then Encoded: " + encryptResult);
    	
    	String decryptResult = decodeAndDecrypt(encryptResult);
    	
    	System.out.println("Decoded and then Decrypted: " + decryptResult);


  } catch(Exception e) {

      e.printStackTrace();

    }

  }
  
  public static String encode(String string) { 	
  	return Base64.encode(string.getBytes());
  }
  
  public static String decode(String string) { 	
  	return new String(Base64.decode(string));
  }
  
  public static String decodeAndDecrypt(String string) {
  	Tracer.log("About to decode and decrypt " + string, Tracer.DEBUG, "Encryption");
  	return decrypt(decode(string));
  }
  
  public static String encryptAndEncode(String string) {
  	return encode(encrypt(string));
  }
  
  public static String encrypt(String string) {
  	
    String keys = "ABCDEFGHIJKL";
    byte [] keys_raw = keys.getBytes();
    SecretKeySpec skeySpec = new SecretKeySpec(keys_raw, "Blowfish");
    String key_string = new String(keys_raw);

    //System.out.println("key is: "+key_string);

    Cipher cipher=null;
		try {
			cipher = Cipher.getInstance("Blowfish");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    try {
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    byte[] encrypted = null;
		try {
			encrypted = cipher.doFinal(string.getBytes());
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    String encrypted_string = new String(encrypted);

    //System.out.println("Encrypted String is:  "+encrypted_string);
    
    return encrypted_string;
  }
  
  public static String decrypt(String string) {
  	
    String keys = "ABCDEFGHIJKL";
    byte [] keys_raw = keys.getBytes();
    SecretKeySpec skeySpec = new SecretKeySpec(keys_raw, "Blowfish");
    String key_string = new String(keys_raw);

    //System.out.println("key is: "+key_string);
    
    byte[] encrypted = string.getBytes();

    Cipher cipher=null;
		try {
			cipher = Cipher.getInstance("Blowfish");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    try {
			cipher.init(Cipher.DECRYPT_MODE,skeySpec);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    byte[] decrypted = null;
		try {
			decrypted = cipher.doFinal(encrypted);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    //System.out.println("Decrypted String is: " + new String(decrypted));
    return new String(decrypted);
  }


}
