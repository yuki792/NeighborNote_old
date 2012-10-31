/*
 * This file is part of NeverNote 
 * Copyright 2009 Randy Baumgarte
 * 
 * This file may be licensed under the terms of of the
 * GNU General Public License Version 2 (the ``GPL'').
 *
 * Software distributed under the License is distributed
 * on an ``AS IS'' basis, WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the GPL for the specific language
 * governing rights and limitations.
 *
 * You should have received a copy of the GPL along with this
 * program. If not, go to http://www.gnu.org/licenses/gpl.html
 * or write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
*/
package cx.fbn.nevernote.evernote;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.CRC32;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cx.fbn.nevernote.utilities.Base64;

public class EnCrypt {

	// Convert a string of text to a hex string
     public static String asHex (byte buf[]) {
      StringBuffer strbuf = new StringBuffer(buf.length * 2);
      int i;

      for (i = 0; i < buf.length; i++) {
       if ((buf[i] & 0xff) < 0x10)
	    strbuf.append("0");

       strbuf.append(Long.toString(buf[i] & 0xff, 16));
      }

      return strbuf.toString();
     }

//	public static class EnCryptException extends Exception {
//    	 public EnCryptException(String message, Throwable cause) {
//    		 super(message, cause);
//    	 }
//     }
     
     /**
      * Choose the character set to use for encoding
      */
     private Charset getCharset() {
    	 
    	 // Just hard-coding choice here
    	boolean useUtf8 = true; 
 		
 		final Charset charSet;
 		if(useUtf8) {
 			charSet = Charset.forName("UTF-8");
 		} else {
 			charSet = Charset.defaultCharset();
 		}
 		return charSet;
     }
     
     
     // Useful for debugging, but not normally used
     @SuppressWarnings("unused")
	private byte[] encodeStringOld(String text) {

    	 int len = text.length()+4;
    	 int mod = (len%8);
    	 if (mod>0) {
    		 for (; mod !=0; len++) {
    			 mod = len%8;
    		 }
    		 len--;
    	 }
    	 len = len-4;
    	 StringBuffer textBuffer = new StringBuffer(text);
    	 textBuffer.setLength(len);

    	 // Setup parms for the cipher
    	 String encoded = crcHeader(textBuffer.toString()) +textBuffer;

    	 return encoded.getBytes();
     }

     /**
      * Main changes are
      * 
      * 1. Do padding based on encoded bytes, not string length (some chars -> 2 bytes)
      * 2. Use specific named charset
      */
     private byte[] encodeStringNew(String text) {
    	 
    	 final Charset charSet = getCharset();
 		
 		// Convert to bytes using given encoding, and align *bytes* to multiple of
 		// 8, with 4 bytes reserved for the crc
 		final byte[] bytes = text.getBytes(charSet);
 		int align8 = (bytes.length + 4) % 8;
 		int paddingNeeded = 8 - align8;
 		final byte[] paddedBytes = Arrays.copyOf(bytes, bytes.length + paddingNeeded);
 		
 		// Now calculate the crc, using the bytes
 		String crc = crcHeader(paddedBytes);
 		
 		byte[] crcBytes = crc.getBytes(charSet);
 		if(crcBytes.length != 4) {
 			System.err.println("CRC Bytes really should be 4 in length!");
 			return null;
 		}
 		
 		// Now combine crc bytes and string bytes into byte array
 		// for encryption
 		byte[] total = new byte[paddedBytes.length + crcBytes.length];
 		System.arraycopy(crcBytes, 0, total, 0, crcBytes.length);
 		System.arraycopy(paddedBytes, 0, total, crcBytes.length, paddedBytes.length);
 		
 		return total;
     }

     
     /**
      * Same as for encryption: use named charset, and
      * @param bytes
      * @return
      */
     private String decodeBytesNew(byte[] bytes) {
    	 
    	 Charset charSet = getCharset();
    	 
    	 byte[] crcBytes = Arrays.copyOfRange(bytes, 0, 4);
    	 byte[] textBytes = Arrays.copyOfRange(bytes, 4, bytes.length);
    	 
    	 CharBuffer crcChar = charSet.decode(ByteBuffer.wrap(crcBytes));
    	 CharBuffer textChar = charSet.decode(ByteBuffer.wrap(textBytes));
    	 
    	 // Get crc of text to see if same
    	 String cryptCRC = crcChar.toString();
    	 String realCRC = crcHeader(textBytes);
    	 
    	 if(realCRC.equals(cryptCRC)) {
    		 // Trim nulls at end
    		 while(textChar.get(textChar.limit() - 1) == 0 && textChar.limit() != 0) {
    			 textChar.limit(textChar.limit() - 1);
    		 }
    		 String str = textChar.toString();
    		 return str;
    	 }
    	 
    	 return null;
     }
     
     /**  
      * For reference: old version.  Useful for debugging
      * @param bytes
      * @return
      */  
     @SuppressWarnings("unused")
	private String decodeBytesOld(byte[] bytes) {

    	 // We have a result.  Separate it into the 4 byte header and the decrypted text
    	 StringBuffer buffer = new StringBuffer(new String(bytes));
    	 String cryptCRC = buffer.substring(0,4);
    	 String clearText = buffer.substring(4);
    	 String realCRC = crcHeader(clearText);
    	 // We need to get the real CRC of the decrypted text
    	 if (realCRC.equalsIgnoreCase(cryptCRC)) {
    		 int endPos = clearText.length();
    		 for (int i=buffer.length()-1; i>=0; i--) {
    			 if (buffer.charAt(i) == 0) 
    				 endPos--;
    			 else
    				 i=-1;
    		 }
    		 clearText = clearText.substring(0,endPos);
    		 return clearText;
    	 }

    	 return null;

     }

     
     
	// Encrypte the text and return the base64 string
	public String encrypt(String text, String passphrase, int keylen) {
		
		
		RC2ParameterSpec parm = new RC2ParameterSpec(keylen);
	   
		try {
			// Get a MD5 for the passphrase
			 MessageDigest md = MessageDigest.getInstance("MD5");
			 // NB Use specific Charset
		    md.update(passphrase.getBytes(getCharset()));
		    
		    // Setup parms for the cipher
		    SecretKeySpec skeySpec = new SecretKeySpec(md.digest(), "RC2");
			Cipher cipher = Cipher.getInstance("RC2/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, parm);
			
			//byte[] oldBytes = encodeStringOld(text);
			byte[] newBytes = encodeStringNew(text);
			//boolean areSame = Arrays.equals(oldBytes, newBytes);
			//System.out.println("Same? " + areSame);
			byte[] d = cipher.doFinal(newBytes);
			
			return Base64.encodeBytes(d);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}		
		
		return null;
	}
	// Decrypt the base64 text and return the unsecure text
	public String decrypt(String text, String passphrase, int keylen) {
		RC2ParameterSpec parm = new RC2ParameterSpec(keylen);
	    MessageDigest md;
		try {
			// Get a MD5 for the passphrase
			md = MessageDigest.getInstance("MD5");
			md.update(passphrase.getBytes(getCharset()));
		    
		    // Setup parms for the cipher
		    SecretKeySpec skeySpec = new SecretKeySpec(md.digest(), "RC2");
			Cipher cipher = Cipher.getInstance("RC2/ECB/NOPADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, parm);
			
			// Decode the encrypted text and decrypt
			byte[] dString = Base64.decode(text);
			byte[] d = cipher.doFinal(dString);
			
			//String clearTextOld = decodeBytesOld(d);
			String clearTextNew = decodeBytesNew(d);
			//if(clearTextNew != null) {
			//	System.out.println("Are same decrypted ? " + clearTextNew.equals(clearTextOld));
			//}
			return clearTextNew;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		return null;
	}
	// Utility function to return the CRC header of an encoded string.  This is
	// used to verify good decryption and put in front of a new encrypted string
	private String crcHeader(String text) {
		return crcHeader(text.getBytes());
	}
	private String crcHeader(byte[] bytes) {
		CRC32 crc = new CRC32();
		crc.update(bytes);
		int realCRC = (int)crc.getValue();
		
		// The first 4 chars of the hex string will equal the first
		// 4 chars of the decyphered text.  If they match we have a
		// good password.  This is what we return
		realCRC = realCRC ^ (-1);
		realCRC = realCRC >>> 0;
		String hexCRC = Integer.toHexString(realCRC).substring(0,4);
		return hexCRC.toString().toUpperCase();

	}

}