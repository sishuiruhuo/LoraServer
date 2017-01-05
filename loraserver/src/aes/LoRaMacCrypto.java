package aes;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import base64.base64__;
import phy.Mhdr;

public class LoRaMacCrypto {
	 /**
	  * 终端具有的加密函数, 可以进行仿照
	  * void LoRaMacComputeMic( const uint8_t *buffer, uint16_t size, const uint8_t *key, 
	  * uint32_t address, uint8_t dir, uint32_t sequenceCounter, uint32_t *mic );
	  * 
	  * void LoRaMacPayloadEncrypt( const uint8_t *buffer, uint16_t size, const uint8_t *key, 
	  * uint32_t address, uint8_t dir, uint32_t sequenceCounter, uint8_t *encBuffer );
	  * 
	  * void LoRaMacPayloadDecrypt( const uint8_t *buffer, uint16_t size, const uint8_t *key, 
	  * uint32_t address, uint8_t dir, uint32_t sequenceCounter, uint8_t *decBuffer );
	  * 
	  * void LoRaMacJoinComputeMic( const uint8_t *buffer, uint16_t size, const uint8_t *key, uint32_t *mic );
	  * 
	  * void LoRaMacJoinDecrypt( const uint8_t *buffer, uint16_t size, const uint8_t *key, uint8_t *decBuffer );
	  * 
	  * void LoRaMacJoinComputeSKeys( const uint8_t *key, const uint8_t *appNonce, 
	  * uint16_t devNonce, uint8_t *nwkSKey, uint8_t *appSKey );
	  * 
	  */
	
	public static final String APPKEY = "0123456123abcdef";
	
	/**
	 * 
	 */
	public static final String KEY_ALGORITHM = "AES";
	
	/**
	 * 模式
	 */
	public static final String CIPHER_ALGORITHM_ECBNopadding = "AES/ECB/Nopadding";
	public static final String CIPHER_ALGORITHM_CBCNopadding = "AES/CBC/Nopadding";
	public static final String CIPHER_ALGORITHM_pkcs5padding = "AES/CBC/pkcs5padding";
	
	
	/**
	 * 对含有 FRMPayload 的数据帧进行加密, 
	 * 使用 CBC/NoPadding 模式, 
	 * 加密秘钥及需要加密的内容均为 16B, 
	 * 加密前后等长, 即为 16B
	 * 
	 * @param frmPayload
	 * @param size : frmPayload 的大小
	 * @param key: 加密的秘钥 AppSKey/NwkSKey
	 * @param address: DevAddr
	 * @param dir: up:0 down:1
	 * @param sequenceCounter: fcnt 传进来的参数是 2 字节的, 需将其转为 4 字节的
	 * @return
	 */
	public static byte[] LoRaMacPayloadEncrypt(byte[] frmPayload, int size, byte[] key, byte[] address, byte dir, byte[] sequenceCounter){
		byte[] aBlock = {
				0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		byte[] sBlock = {
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		aBlock[5] = dir;
		System.arraycopy(address, 0, aBlock, 6, 4);
		System.arraycopy(sequenceCounter, 0, aBlock, 10, 4);
		
		int ctr = 1, bufferIndex = 0;
		while(size >= 16){
			aBlock[15] = (byte) ( ( ctr ) & 0xFF );
	        ctr++;
	        sBlock = LoRaMacCrypto.encrypt_CBC(aBlock, key);
	        for( int i = 0; i < 16; i++ ) {
	            frmPayload[bufferIndex + i] = (byte) (frmPayload[bufferIndex + i] ^ sBlock[i]);
	        }
	        size -= 16;
	        bufferIndex += 16;
		}
		if( size > 0 ) {
	        aBlock[15] = (byte) ( ( ctr ) & 0xFF );
	        sBlock = LoRaMacCrypto.encrypt_CBC(aBlock, key);
	        for(int i = 0; i < size; i++ ) {
	        	frmPayload[bufferIndex + i] = (byte) (frmPayload[bufferIndex + i] ^ sBlock[i]);
	        }
	    }
		return frmPayload;
		
	}
	
	/**
	 * 对含有 FRMPayload 的数据帧进行解密, 
	 * 使用 CBC/NoPadding 模式, 
	 * 解密秘钥及需要加密的内容均为 16B, 
	 * 解密前后等长, 即为 16B
	 * 
	 * @param frmPayload
	 * @param size : frmPayload 的大小
	 * @param key: 加密的秘钥 AppSKey/NwkSKey
	 * @param address: DevAddr
	 * @param dir: up:0 down:1
	 * @param sequenceCounter: fcnt 传进来的参数是 2 字节的, 需将其转为 4 字节的
	 * @return
	 */
	public static byte[] LoRaMacPayloadDecrypt(byte[] frmPayload, int size, byte[] key, byte[] address, byte dir, byte[] sequenceCounter){
		byte[] aBlock = {
				0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		byte[] sBlock = {
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		aBlock[5] = dir;
		System.arraycopy(address, 0, aBlock, 6, 4);
		System.arraycopy(sequenceCounter, 0, aBlock, 10, 4);
		
		int ctr = 1, bufferIndex = 0;
		while(size >= 16){
			aBlock[15] = (byte) ( ( ctr ) & 0xFF );
	        ctr++;
	        sBlock = LoRaMacCrypto.decrypt_CBC(aBlock, key);
	        for( int i = 0; i < 16; i++ ) {
	            frmPayload[bufferIndex + i] = (byte) (frmPayload[bufferIndex + i] ^ sBlock[i]);
	        }
	        size -= 16;
	        bufferIndex += 16;
		}
		if( size > 0 ) {
	        aBlock[15] = (byte) ( ( ctr ) & 0xFF );
	        sBlock = LoRaMacCrypto.decrypt_CBC(aBlock, key);
	        for(int i = 0; i < size; i++ ) {
	        	frmPayload[bufferIndex + i] = (byte) (frmPayload[bufferIndex + i] ^ sBlock[i]);
	        }
	    }
		return frmPayload;
		
	}
	
	/**
	 * 
	 * @param buffer: 用于计算 MIC 的数据
	 * @param size: 用于计算 MIC 的长度
	 * @param key: 秘钥 AppSKey/NwkSKey
	 * @param address: DevAddr
	 * @param dir: up 0  down 1
	 * @param sequenceCounter: fcnt, 传进来的参数是 2 字节的, 需将其转为 4 字节的
	 * @return 4B 的 mic
	 */
	public static byte[] LoRaMacComputeMic(byte[] buffer, int size, byte[] key, byte[] address, byte dir, byte[] sequenceCounter){
		/*byte[] data = new byte[macbyte.length + 17];		// 用于计算 MIC 的所有字段
		byte[] B0 = {0x49,0x00,0x00,0x00,0x00,
				下行固定值:10x01,
				设备地址 60x00,0x00,0x00,0x00,
				Fcnt 100x00,0x00,0x00,0x00,
				0x00,0x00};
		
		// 准备用于计算 MIC 的字段
		System.arraycopy(macbyte, 0, B0, 6, 4);
		System.arraycopy(macbyte, 5, B0, 10, 2);
		System.arraycopy((byte)(macbyte.length + 1), 0, B0, 15, 1);
		Mhdr mhdr = new Mhdr(type, 0, 1);
		System.arraycopy(mhdr.MhdrPktToByte(), 0, data, 0, mhdr.getLength());
		System.arraycopy(macbyte, 0, data,  mhdr.getLength(), macbyte.length);*/
		
		byte[] mic = new byte[4];
		mic[0] = 0x01;
		mic[1] = 0x02;
		mic[2] = 0x04;
		mic[3] = 0x08;
		return mic;
	}
	
	/**
	 * 
	 * @param buffer: 用于计算 MIC 的数据
	 * @param size: 用于计算 MIC 的数据长度
	 * @param key: 秘钥 AppSKey/NwkSKey
	 * @return 4B 的 MIC
	 */
	public static byte[] LoRaMacJoinComputeMic(byte[] buffer, int size, byte[] key){
		byte[] mic = new byte[4];
		mic[0] = 0x01;
		mic[1] = 0x02;
		mic[2] = 0x04;
		mic[3] = 0x08;
		return mic;
	}
	
	/**
	 * 对含有 Accept 帧进行加密, 使用解密算法进行加密, 秘钥 AppKey 
	 * 使用 CBC/NoPadding 模式, 
	 * 加密秘钥及需要加密的内容均为 16B, 
	 * 加密前后等长, 即为 16B
	 *  
	 * @param buffer: 需要加密的数据
	 * @param size: 加密数据长度
	 * @param key: 秘钥 AppSKey
	 * @return 加密完成后的字节数组
	 */
	public static byte[] LoRaMacAcceptEncrypt(byte[] buffer, int size, byte[] key){
		byte[] output = LoRaMacCrypto.encrypt_Test(LoRaMacCrypto.APPKEY, buffer);
		
		
		return output;
	}
	
	/**
	 * 
	 * @param key 秘钥 AppSKey/NwkSKey
	 * @param appNonce
	 * @param devNonce
	 * @return 16B 的 NwkSKey
	 */
	public static byte[] LoRaMacJoinComputeNwkSKey(byte[] key, byte[] appNonce, byte[] devNonce){
		byte[] nwkskey = new byte[16];
		nwkskey[0] = 0x01;
		nwkskey[1] = 0x02;
		nwkskey[2] = 0x04;
		nwkskey[3] = 0x08;
		return nwkskey;
	}
	
	/**
	 * 
	 * @param key
	 * @param appNonce
	 * @param devNonce
	 * @return 16B 的 AppSKey
	 */
	public static byte[] LoRaMacJoinComputeAppSKey(byte[] key, byte[] appNonce, byte[] devNonce){
		byte[] appskey = new byte[16];
		appskey[0] = 0x01;
		appskey[1] = 0x02;
		appskey[2] = 0x04;
		appskey[3] = 0x08;
		return appskey;
	}
	
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	private static Key toKey(byte[] key){
		SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
		return secretKey;
	}
	
	/**
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt_ECB(byte[] data, byte[] key) {
		String ivParameter = "0123456123abcdef";
		Key k = toKey(key);		
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECBNopadding);
			cipher.init(Cipher.DECRYPT_MODE, k);
			return cipher.doFinal(data);		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
		
	}
	
	/**
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt_CBC(byte[] data, byte[] key) {
		String ivParameter = "0123456123abcdef";
		Key k = toKey(key);		
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBCNopadding);
			// CBC
			IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, k, iv);
			return cipher.doFinal(data);		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
		
	}
	
	/**
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt_ECB(byte[] data, byte[] key){
		String ivParameter = "0123456789abcdef";
		Key k = toKey(key);		
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECBNopadding);
			// CBC
			IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, k);
			return cipher.doFinal(data);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
	}
	
	/**
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt_CBC(byte[] data, byte[] key){
		String ivParameter = "0123456789abcdef";
		Key k = toKey(key);		
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBCNopadding);
			// CBC
			IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, k, iv);
			return cipher.doFinal(data);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
	}
	
	/**
	 * 
	 * @return
	 */
	public static byte[] initKey(){
		KeyGenerator kg;
		try {
			kg = KeyGenerator.getInstance(KEY_ALGORITHM);
			kg.init(128);
			SecretKey secretKey = kg.generateKey();
			return secretKey.getEncoded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

	
	/**
	 * 
	 * @param key
	 * @param content
	 * @return
	 */
	public static byte[] encrypt_Test(String key, byte[] content){
		int cnt = 0;
		byte[] outputbyte = new byte[(content.length / 16 + 1) * 16];
		base64__.myprintHex(content);
		byte[] byte16 = new byte[16];
		System.out.println("aes content length " + content.length);
		for(int i = 0; i < content.length / 16 + 1; i++){
			System.out.println("aes----------");
			System.arraycopy(content, i * 16, byte16, 0, 13);
			base64__.myprintHex(byte16);
			byte16 = LoRaMacCrypto.encrypt_ECB(byte16, key.getBytes());
			System.arraycopy(byte16, 0, outputbyte, i * 16, 16);
		}
		base64__.myprintHex(outputbyte);
		System.out.println("aes success!");
		return outputbyte;
	}
	
	/**
	 * 
	 * @param inputData
	 */
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
	
		String algorithm = CIPHER_ALGORITHM_ECBNopadding;
//		String algorithm = CIPHER_ALGORITHM_pkcs5padding;
		System.out.println(algorithm);
		/*byte[] keybyte = {
				0x01,0x01,0x01,0x01,
				0x01,0x01,0x01,0x01,
				0x01,0x01,0x01,0x01,
				0x01,0x01,0x01,0x01};
		System.out.println("0000: " + new String(keybyte));*/
//		String inputStr = "AESAESAESAESAESA";		
//		byte[] inputData = inputStr.getBytes();
		byte[] inputData = {
				0x21, 0x01, 0x02, 0x03, 
				0x00, 0x00, 0x60, (byte) 0xc0, 
				(byte) 0xa8, 0x01, 0x01, 0x12, 
				0x01,0x01,0x01,0x01};
		/*byte[] inputData = {
				0x41,0x45,0x53,0x41,
				0x45,0x53,0x41,0x45,
				0x53,0x41,0x45,0x53,
				0x41,0x45,0x53};*/
		System.out.print("加密前: " + inputData.length + " ");
		LoRaMacCrypto.myprintHex(inputData);
		//byte[] keybyte = AesEncrypt.initKey();
	//	AesEncrypt.Encrypt(APPKEY, inputData);
		
		
		byte[] outputData = LoRaMacCrypto.encrypt_ECB(inputData, (LoRaMacCrypto.APPKEY).getBytes());
		inputData = LoRaMacCrypto.decrypt_ECB(outputData, (LoRaMacCrypto.APPKEY).getBytes());
		System.out.print("加密后: " + inputData.length + " ");	
		LoRaMacCrypto.myprintHex(inputData);
		
		
		System.out.print("解密后: " + outputData.length + " ");		
		LoRaMacCrypto.myprintHex(outputData);
		LoRaMacCrypto.myprintHex(LoRaMacCrypto.APPKEY.getBytes());
		
		byte bb = (byte) 0xff;
		System.out.println(" --------_+："+ ( ((int)(bb & 0xff)) >> 5));
		/*
		byte buf[] = new byte[2];
		buf[0] = 1;
		buf[1] = 15;
		byte[] tt = {0x00};
		//System.arraycopy(buf, 0, tt, 0, 2);
		//System.arraycopy(buf, 0, tt, 2, 2);
		tt = buf;
		StringBuffer sb = new StringBuffer();   
	    for (int i = 0; i < tt.length; i++) {   
	            String hex = Integer.toHexString(tt[i] & 0xFF);   
	            if (hex.length() == 1) {   
	                    hex = '0' + hex;   
	            }   
	            sb.append(hex.toUpperCase());   
	    }   
	    System.out.println(sb.toString());
	    */
	}
	
} 