package samples;

import com.powerdba.util.Tracer;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Encoder;
import sun.misc.CharacterEncoder;

// this implements a SHA-1 160 bit encryption algorithm.

public final class EncryptionService {

  private static EncryptionService instance;

  private EncryptionService() {}

  public synchronized String encrypt(String plaintext) throws Exception {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("SHA");
    }
    catch(NoSuchAlgorithmException e) {
      throw new Exception(e.getMessage());
    }

    try {
      md.update(plaintext.getBytes("UTF-8"));
    } catch(UnsupportedEncodingException e) {
      throw new Exception(e.getMessage());
    }

    byte raw[] = md.digest();
    String hash = (new BASE64Encoder()).encode(raw);
    return hash;
  }
  
  public synchronized String encrypt2(String plaintext) throws Exception {

        // Create the secret/symmetric key
        KeyGenerator kgen = KeyGenerator.getInstance("JohnnyQuest");
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "JohnnyQuest");

        // Create the cipher for encrypting
        Cipher cipher = Cipher.getInstance("JohnnyQuest");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        // Encrypt the data
        byte[] encrypted = cipher.doFinal( plaintext );

        // Save the encrypted data
        Tracer.log("Encrypted String " + encrypted.toString(), Tracer.DEBUG, this);

        // Save the cipher settings
        byte[] encodedKeySpec = skeySpec.getEncoded();
        FileOutputStream eksos = new FileOutputStream( KEY_FILE );
        eksos.write( encodedKeySpec );
        eksos.close();
  }
  
  public synchronized String decrypt2(String encryptedText) throws Exception {
  
        plaintextBytes = plaintext.getBytes();
  
        // Read the encrypted data
        FileInputStream fis = new FileInputStream(DATA_FILE);
        byte[] temp = new byte[ DATA_FILE.length()];
        int bytesRead = fis.read(temp);
        byte[] data = new byte[bytesRead];
        System.arraycopy(temp, 0, data, 0, bytesRead);


        // Read the cipher settings
        FileInputStream eksis = new FileInputStream( KEY_FILE );
        bytesRead = eksis.read(temp);
        byte[] encodedKeySpec = new byte[bytesRead];
        System.arraycopy(temp, 0, encodedKeySpec, 0, bytesRead);

        // Recreate the secret/symmetric key
        SecretKeySpec skeySpec = new SecretKeySpec( encodedKeySpec, "Blowfish");

        // Create the cipher for encrypting
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);

        // Decrypt the data
        byte[] decrypted = cipher.doFinal(data);
        
  }
 
  
  public static synchronized EncryptionService getInstance() {
    if(instance == null) {
     instance = new EncryptionService(); 
    } 
    return instance;
  }
}