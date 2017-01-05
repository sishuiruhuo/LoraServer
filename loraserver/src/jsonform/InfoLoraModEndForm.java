package jsonform;

public class InfoLoraModEndForm implements InfoForm{
	public String time;
	public double tmst;
	public int freq;
	public int chan;
	public int rfch;
	public int stat;
	public String modu;
	public String datr_lora;
	public String codr ;
	public int rssi;
	public int lsnr;
	public int size;
	public byte[] data = null;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public double getTmst() {
		return tmst;
	}
	public void setTmst(int tmst) {
		this.tmst = tmst;
	}
	public int getFreq() {
		return freq;
	}
	public void setFreq(int freq) {
		this.freq = freq;
	}
	public int getChan() {
		return chan;
	}
	public void setChan(int chan) {
		this.chan = chan;
	}
	public int getRfch() {
		return rfch;
	}
	public void setRfch(int rfch) {
		this.rfch = rfch;
	}
	public int getStat() {
		return stat;
	}
	public void setStat(int stat) {
		this.stat = stat;
	}
	public String getModu() {
		return modu;
	}
	public void setModu(String modu) {
		this.modu = modu;
	}
	public String getDatr_lora() {
		return datr_lora;
	}
	public void setDatr_lora(String datr_lora) {
		this.datr_lora = datr_lora;
	}
	
	public String getCodr() {
		return codr;
	}
	public void setCodr(String codr) {
		this.codr = codr;
	}
	public int getRssi() {
		return rssi;
	}
	public void setRssi(int rssi) {
		this.rssi = rssi;
	}
	public int getLsnr() {
		return lsnr;
	}
	public void setLsnr(int lsnr) {
		this.lsnr = lsnr;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	@Override
	public byte[] getData() {
		return this.data;
	}
		
	public void saveData() {
		// TODO Auto-generated method stub
		System.out.println("Lora");
	}
	
}