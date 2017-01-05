package phy;

import aes.LoRaMacCrypto;
import mac.MacConfirmedDataDownForm;
import mac.MacPktForm;
import mac.MacUnconfirmedDataDownForm;

public class MicCaculate {

	/**
	 * 
	 * @param macPayload: mac 层数据, 不含 mhdr, 这里需要根据 type 创建 mhdr
	 * @param type: 用于判断 是否为 Accept 帧
	 * @return 4 字节的 MIC
	 */
	public static byte[] MicCaculate(MacPktForm macpktform, int type){
		byte[] macbyte = macpktform.MacPkt2Byte();
		byte[] mic = new byte[4];
		byte[] data = new byte[macpktform.getLength() + 1];
		Mhdr mhdr = new Mhdr(type, 0, 1);
		System.arraycopy(mhdr.MhdrPktToByte(), 0, data, 0, mhdr.getLength());
		System.arraycopy(macbyte, 0, data,  mhdr.getLength(), macbyte.length);
		// TODO 需要讨论将秘钥存在哪里
		byte[] appNonce = new byte[3];
		byte[] devNonce = new byte[2];
		byte[] nwkskey = LoRaMacCrypto.LoRaMacJoinComputeNwkSKey(LoRaMacCrypto.APPKEY.getBytes(), appNonce, devNonce);
		
		// MIC 计算
		if(type == 1){
			mic = LoRaMacCrypto.LoRaMacJoinComputeMic(data, data.length, LoRaMacCrypto.APPKEY.getBytes());
		} else if(type == 3){ 
			MacUnconfirmedDataDownForm mac = (MacUnconfirmedDataDownForm) macpktform;
			mic = LoRaMacCrypto.LoRaMacComputeMic(data, data.length, nwkskey, mac.DevAddr, (byte) 0x01, mac.Fcnt);
		} else if(type == 5){
			MacConfirmedDataDownForm mac = (MacConfirmedDataDownForm) macpktform;
			mic = LoRaMacCrypto.LoRaMacComputeMic(data, data.length, nwkskey, mac.DevAddr, (byte) 0x01, mac.Fcnt);
		} else {
			return null;
		}		
		return mic;
	}
	
}


