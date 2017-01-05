package mac;

public class MacConfirmedDataUpForm extends MacPktForm{
	public byte[] DevAddr = new byte[4];
	public short Fcnt;
	public byte[] Fopts;
	public int Fport;
	public byte[] FRMPayload;
	public byte[] getDevAddr() {
		return DevAddr;
	}
	public void setDevAddr(byte[] devAddr) {
		DevAddr = devAddr;
	}
	public short getFcnt() {
		return Fcnt;
	}
	public void setFcnt(short fcnt) {
		Fcnt = fcnt;
	}
	public byte[] getFopts() {
		return Fopts;
	}
	public void setFopts(byte[] fopts) {
		Fopts = fopts;
	}
	public int getFport() {
		return Fport;
	}
	public void setFport(int fport) {
		Fport = fport;
	}
	public byte[] getFRMPayload() {
		return FRMPayload;
	}
	public void setFRMPayload(byte[] fRMPayload) {
		FRMPayload = fRMPayload;
	}
	@Override
	public byte[] MacPkt2Byte() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public class Fctrl{
		int ADR;
		int ADRACKReq;
		int ACK;
		int RFU;
		int FOptslen;
	};
}
