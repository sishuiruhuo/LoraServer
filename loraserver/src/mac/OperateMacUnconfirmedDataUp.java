package mac;

import aes.LoRaMacCrypto;

public class OperateMacUnconfirmedDataUp implements OperateMacPkt{

	@Override
	public MacPktForm MacParseData(byte[] data) {
		System.out.println("hello lora======");
		MacUnconfirmedDataUpForm macunconfirmeddataup = new MacUnconfirmedDataUpForm();
		
		System.arraycopy(data, 1, macunconfirmeddataup.DevAddr, 0, 4);
		
		macunconfirmeddataup.fctrl.setFctrl(data[5]);
		
		System.arraycopy(data, 6, macunconfirmeddataup.Fcnt, 0, 2);
		int foptlen = macunconfirmeddataup.fctrl.FOptslen;
		macunconfirmeddataup.Fopts = new byte[foptlen];
		System.arraycopy(data, 8, macunconfirmeddataup.Fopts, 0, foptlen);
		System.arraycopy(data, 8 + foptlen, macunconfirmeddataup.Fport, 0, 1);
		
		int framelen = data.length - (9 + foptlen);
		macunconfirmeddataup.FRMPayload = new byte[framelen];
		System.arraycopy(data, 8 + foptlen + 1, macunconfirmeddataup.FRMPayload, 0, framelen);
		
		// 对于 FRMPayload 要进行解密
		byte[] fcnt = new byte[4];
		System.arraycopy(macunconfirmeddataup.Fcnt, 0, fcnt, 0, 2);
		System.out.println();
		macunconfirmeddataup.FRMPayload = LoRaMacCrypto.LoRaMacPayloadDecrypt(
				macunconfirmeddataup.FRMPayload, framelen, LoRaMacCrypto.APPKEY, 
				macunconfirmeddataup.DevAddr, (byte) 0x00, 
				fcnt);// TODO fcnt 需要 4 字节的，这里是 2 字节的, 加密秘钥也需要变动, 应为 AppSKey
		return macunconfirmeddataup;
	}

	/**
	 * 返回 MacUnconfirmedDataDownForm, 其中的 FRMPayload 已加密
	 */
	@Override
	public MacPktForm MacConstructData(MacPktForm macpkt) {
		/**
		 * 应用层数据
		 */
		byte[] frmPayload = {0x0f, 0x0f};
		
		MacUnconfirmedDataUpForm macunconfirmeddataup = (MacUnconfirmedDataUpForm) macpkt;
		MacUnconfirmedDataDownForm macunconfirmeddatadown = new MacUnconfirmedDataDownForm();
		macunconfirmeddatadown.DevAddr = macunconfirmeddataup.DevAddr;
		
		macunconfirmeddatadown.fctrl.ADR = 1;
		macunconfirmeddatadown.fctrl.RFU = 0;
		macunconfirmeddatadown.fctrl.ACK = 1;
		macunconfirmeddatadown.fctrl.FPending = 0;
		macunconfirmeddatadown.fctrl.FOptslen = 4;
		
		macunconfirmeddatadown.Fcnt = macunconfirmeddataup.Fcnt;
		macunconfirmeddatadown.Fopts = new byte[macunconfirmeddatadown.fctrl.FOptslen];
		macunconfirmeddatadown.Fopts = macunconfirmeddataup.DevAddr;
		
		macunconfirmeddatadown.FRMPayload = new byte[frmPayload.length];
		// 对 FRMPayload 进行加密
		byte[] fcnt = new byte[4];
		System.arraycopy(macunconfirmeddataup.Fcnt, 0, fcnt, 0, 2);
		macunconfirmeddatadown.FRMPayload = LoRaMacCrypto.LoRaMacPayloadEncrypt(
				frmPayload, frmPayload.length, LoRaMacCrypto.APPKEY, 
				macunconfirmeddatadown.DevAddr, (byte) 0x01, 
				fcnt);	// TODO fcnt 需要 4 字节的，这里是 2 字节的, 加密秘钥也需要变动, 应为 AppSKey
		
		return macunconfirmeddatadown;
	}

}
