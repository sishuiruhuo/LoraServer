package aes;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import base64.base64__;

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
	
	public static final byte[] APPKEY = {
			0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00};
	
	/**
	 * 
	 */
	public static final String KEY_ALGORITHM = "AES";
	
	/**
	 * 模式
	 */
	public static final String CIPHER_ALGORITHM_ECBNopadding = "AES/ECB/Nopadding";
	public static final String CIPHER_ALGORITHM_CBCNopadding = "AES/CBC/Nopadding";
	
	
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
	        sBlock = LoRaMacCrypto.encrypt_ECB(aBlock, key);
	        for( int i = 0; i < 16; i++ ) {
	            frmPayload[bufferIndex + i] = (byte) (frmPayload[bufferIndex + i] ^ sBlock[i]);
	        }
	        size -= 16;
	        bufferIndex += 16;
		}
		if( size > 0 ) {
	        aBlock[15] = (byte) ( ( ctr ) & 0xFF );
	        sBlock = LoRaMacCrypto.encrypt_ECB(aBlock, key);
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
		return LoRaMacCrypto.LoRaMacPayloadEncrypt(frmPayload, size, key, address, dir, sequenceCounter);
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
		return LoRaMacCrypto.decrypt_ECB(buffer, key);
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
		Key k = toKey(key);		
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECBNopadding);
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
            if( (i + 1) % 16 == 0)
            	System.out.println();
		}   
	}
	
	public static void main(String[] args){
	
		//String algorithm = CIPHER_ALGORITHM_ECBNopadding;
		byte[] AppSKey = {
				0x2B, 0x7e, 0x15, 0x16,
				0x28, (byte) 0xae, (byte) 0xd2, (byte) 0xa6, 
				(byte) 0xab, (byte) 0xf7, 0x15, (byte) 0x88,
				0x09, (byte) 0xcf, 0x4f, 0x3c};
		
		byte[] frmdata = {0x01,0x02,0x03,0x01,
				0x01,0x60,(byte) 0x8d,(byte) 0xc0,
				0x79,0x01,0x01,0x01,
				(byte) 0xf0,(byte) 0xf0,(byte) 0xf0,(byte) 0xf0,
				0x01,0x02,0x03,0x01,
				0x01,0x60,(byte) 0x8d,(byte) 0xc0,
				0x79,0x01,0x01,0x01,
				(byte) 0xf0,(byte) 0xf0,(byte) 0xf0,(byte) 0xf0};
		
		byte[] address = {(byte) 0x8D,(byte) 0xC0,0x79,0x00};
		byte dir = 0x01;
		byte[] sequenceCounter = {0x01,0x00,0x00,0x00};
		
		byte[] outputaccept = LoRaMacCrypto.LoRaMacAcceptEncrypt(frmdata, frmdata.length, AppSKey);
		System.out.println("数据帧：AppSKey");
		LoRaMacCrypto.myprintHex(outputaccept);
		System.out.println("accept：AppSKey");
		LoRaMacCrypto.myprintHex(LoRaMacCrypto.encrypt_ECB(outputaccept, AppSKey));
		
	}
	
} 