package jsonform;

public class DownInfoA implements DownInfoForm{

	/**
	 * class A 的各项 JSON 数据
	 */
	private boolean imme;
	private float freq;
	private int rfch;
	private int powe;
	private String modu;
	private String datr;
	private String codr;
	private boolean ipol;
	private int size;
	private String data;	
	
	@Override
	public DownInfoForm ConstructDownInfo(InfoForm info, byte[] data) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isImme() {
		return imme;
	}
	public void setImme(boolean imme) {
		this.imme = imme;
	}
	
	public float getFreq() {
		return freq;
	}
	public void setFreq(float freq) {
		this.freq = freq;
	}

	public int getRfch() {
		return rfch;
	}
	public void setRfch(int rfch) {
		this.rfch = rfch;
	}

	public int getPowe() {
		return powe;
	}
	public void setPowe(int powe) {
		this.powe = powe;
	}

	public String getModu() {
		return modu;
	}
	public void setModu(String modu) {
		this.modu = modu;
	}

	public String getDatr() {
		return datr;
	}
	public void setDatr(String datr) {
		this.datr = datr;
	}

	public String getCodr() {
		return codr;
	}
	public void setCodr(String codr) {
		this.codr = codr;
	}
	
	public boolean isIpol() {
		return ipol;
	}
	public void setIpol(boolean ipol) {
		this.ipol = ipol;
	}
	
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}

	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}

}
