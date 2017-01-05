package base64;

import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

public class base64__ {
	/**
	  * @param base64String
	  * @return
	  */
	  public static byte[] decode(String base64String) {
	    return Base64.decodeBase64(base64String);
	  }

	  /**
	  * 二进制数据编码为BASE64字符串
	  *
	  * @param bytes
	  * @return
	  * @throws Exception
	  */
	  public static String encode(final byte[] bytes) {
	    return new String(Base64.encodeBase64(bytes));
	  }
	  
	  public static void myprintHex(byte[] inputData){
			for (int i = 0; i < inputData.length; i++) {   
	            String hex = Integer.toHexString(inputData[i] & 0xFF);   
	            if (hex.length() == 1) {   
	                    hex = '0' + hex;   
	            }   
	            System.out.print("0x" + hex + " "); 
			}   
			System.out.println();
		}

	  public static void main(String[] args){
		  byte[] buffer = new byte[8192];
		  byte[] buff = new byte[13];
		  buff[0] = 0x01;	// mhdr
			buff[1] = 0x0f;	// DevAddr
			buff[2] = 0x0f;
			buff[3] = 0x0f;
			buff[4] = 0x0f;
			buff[5] = 0x01;	// Fctrl
			buff[6] = 0x0f;	// Fcnt
			buff[7] = 0x0f;
			buff[8] = 0x0f; //Fport
			buff[9] = 0x0f; //FramePayload
			buff[10] = 0x0f;
			buff[11] = 0x0f;
			buff[12] = (byte) 0xf0;
		  String base64String = "AQ8PDw8BDw8PDw8P8A==";
		  buffer = base64__.decode(base64String);
		  
		  System.out.println(Arrays.toString(buffer));//字节数组打印
		  base64__.myprintHex(buffer);
		  buffer[0] = 0x0f;
		  buffer[1] = 0x0f;
		  buffer[2] = 0x0f;
		  System.out.println(base64__.encode(buff));
	  }
}
