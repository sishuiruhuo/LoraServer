package APP;

public class FRMPayloadSendToMac implements OperatePayload {

	
	//添加应用层的frmpayload 
	@Override
	public byte[] Send(byte[] macPayload) {
		byte[] macPayload_frmpayload;
		byte[] frmpayload = {0x00};
		System.arraycopy(frmpayload, 0, macPayload, macPayload.length, macPayload.length);
		macPayload_frmpayload = macPayload;
		return macPayload_frmpayload;
	}


	@Override
	public void Recv() {
		;

	}

}
