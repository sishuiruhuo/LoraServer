package phy;

import aes.LoRaMacCrypto;
import mac.MacPktForm;

public class PhyConstruct {

	private static final int MIC_LENGTH = 4;
	/**
	 * 
	 * @param macpktform : mac 层数据
	 * @param type : mhdr 的 mac 类型, 用于生成相对应的 mhdr 对象
	 * @return : byte[] phy 层数据，包含加密及 MIC 的计算
	 */
	public static byte[] PhyPkt2Byte(MacPktForm macpktform, int type){
		byte[] phy = new byte[macpktform.getLength() + 5];
		type = type < 5 ? type + 1 : type;
		// 封装 mhdr
		Mhdr mhdr = new Mhdr(type, 0, 1);
		// 分情况：
		// 1、对于 accept 数据帧，需要先计算 mic, 再连同 mic 一起加密
		// 2、对于数据帧，直接计算 mic 值, 不需加密		
		
		System.arraycopy(mhdr.MhdrPktToByte(), 0, phy, 0, mhdr.getLength());
		System.arraycopy(macpktform.MacPkt2Byte(), 0, phy, mhdr.getLength(), macpktform.getLength());
		System.arraycopy(MicCaculate.MicCaculate(macpktform, type),
				0, phy,  
				mhdr.getLength() + macpktform.getLength(), MIC_LENGTH);
		if(type == 1){
			// 如果是 accept 数据帧,则还需连同 MIC 加密, 使用秘钥 AppKey
			System.out.println("=====accept encrypt=====");
			phy = LoRaMacCrypto.LoRaMacAcceptEncrypt(phy, phy.length, LoRaMacCrypto.APPKEY);
		}
		return phy;		
	}
	
}
