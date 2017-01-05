package mac;

import aes.LoRaMacCrypto;
import base64.base64__;
import util.StringHex.ParseByte2HexStr;

public class OperateMacJoinRequest implements OperateMacPkt {

	@Override
	public	MacPktForm MacParseData(byte[] data) {
		MacJoinRequestForm macjoinrequest = new MacJoinRequestForm();
		System.arraycopy(data, 1, macjoinrequest.AppEui, 0, 8);
		System.arraycopy(data, 9, macjoinrequest.DevEui, 0, 8);
		System.arraycopy(data, 17, macjoinrequest.DevNonce,0 ,2);
		
		base64__.myprintHex((macjoinrequest.getAppEui()));
		base64__.myprintHex((macjoinrequest.getDevEui()));
		base64__.myprintHex((macjoinrequest.getDevNonce()));
		return macjoinrequest;
	}

	/**
	 * 返回 Accept 帧
	 */
	@Override
	public MacPktForm MacConstructData(MacPktForm macpkt) {
		byte[] appnonce = {0x01,0x02,0x03};//随机
		byte[] netid = {0x00,0x00,0x60}; //devaddr 高7位  = netid 低7位        netid高17位自己定  高17位为全0
		byte[] devaddr = {(byte) 0xc0,(byte) 0xa8,0x01,0x01}; // 随机唯一 IP 192.168.1.1
		byte[] rxdelay = {0x01}; //默认1 
		byte[] cflist = null; //  
		byte[] dlsetting = {0x00};
		MacJoinAcceptForm macjoinaccept = new MacJoinAcceptForm();
		//CreateRandom createrandom = new CreateRandom();
		//macjoinaccept.AppNonce = createrandom.RandomArray(8);
		macjoinaccept.AppNonce = appnonce;
		macjoinaccept.NetId = netid;
		macjoinaccept.DevAddr = devaddr;
		
		macjoinaccept.dlset.RFU = 0;
		macjoinaccept.dlset.RX1DRoffset = 0x01;
		macjoinaccept.dlset.Rx2DataRate = 0x02;
		
		macjoinaccept.RxDelay = rxdelay;
		macjoinaccept.CfList = cflist  ;
	
		// 加密放在组装 phy 数据时再进行计算,同时便于 MIC 值的提取
		/*byte[] output;
		
		try {
			// 加密
			output = AesEncrypt.Encrypt(AesEncrypt.APPKEY,
				ParseByte2HexStr.Byte2HexStr(macjoinaccept.MacPkt2Byte()));			
			return output;
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		return macjoinaccept;
				
	}
	
	

}
