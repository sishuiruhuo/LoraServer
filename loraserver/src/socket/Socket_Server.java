package socket;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import dao.DataBaseAction;
import jsonform.DownInfoForm;
import jsonform.InfoForm;
import mac.MacPktForm;
import mac.OperateMacPkt;
import phy.PhyConstruct;
import util.ConstructJson;
import util.ParseJson;

public class Socket_Server implements Runnable {
	private HashMap<String, InfoForm> UpInfoMap = new HashMap();
	private HashMap<String, DownInfoForm> DownInfoMap = new HashMap();
	private int port;
	private DatagramSocket dsock;
	private InetSocketAddress socketAddress = null;
	private static final byte PKT_PUSH_DATA = 0x00;
	private static final byte PKT_PUSH_ACK = 0x01;
	private static final byte PKT_PULL_DATA = 0x02;
	private static final byte PKT_PULL_RESP = 0x03;
	private static final byte PKT_PULL_ACK = 0x04;
	private byte ack_command;
	public  byte Mtype;
	public static final byte JoinRequest = 0;
	public static final byte JoinAccept = 1;
	public static final byte UnconfiredDataUp = 2;
	public static final byte UnconfiredDataDown = 3;
	public static final byte ConfiredDataUP= 4;
	public static final byte ConfiredDataDown = 5;
	public static final byte RFU = 6;
	public static final byte Proprietary = 7;
	
	public List<String> OPMACLISTNAME = new ArrayList(); 
	
	
	
	public Socket_Server(int port){
		this.port = port;
		this.socketAddress = new InetSocketAddress("127.0.0.1", this.port);
		try {
			this.dsock = new DatagramSocket(this.socketAddress);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	public Socket_Server(){
		this(1680);
	}
	
	@Override
	public void run() {
		int num = 0;
		byte[] buffer = new byte[8192];
		OPMACLISTNAME.add("mac.OperateMacJoinRequest");
		OPMACLISTNAME.add("mac.OperateMacJoinAccept");
		OPMACLISTNAME.add("mac.OperateMacUnconfirmedDataUp");
		OPMACLISTNAME.add("mac.OperateMacUnconfirmedDataDown");
		OPMACLISTNAME.add("mac.OperateMacConfirmedDataUp");
		OPMACLISTNAME.add("mac.OperateMacConfirmedDataDown");
		OPMACLISTNAME.add("mac.OperateMacRFU");
		OPMACLISTNAME.add("mac.OperateMacProprietary");
		
		while(true){
			DatagramPacket recv_pkt = new DatagramPacket(buffer, buffer.length);
			try {
				dsock.receive(recv_pkt);
				num++;
				System.out.println("-------- " + num + " --------");
				String recv_info = new String(recv_pkt.getData(), 12, recv_pkt.getLength());
				DatagramPacket send_pkt;
				switch(buffer[3]){
				case PKT_PUSH_DATA:
					System.out.println("PUSH_DATA" + recv_info);
					// 先回 ack
					buffer[3] = PKT_PUSH_ACK;
					send_pkt = new DatagramPacket(
							buffer, 4,
							recv_pkt.getAddress(), recv_pkt.getPort());
					dsock.send(send_pkt);
					// 存到 upinfo 
					UpInfoMap = ParseJson.parseOfJson(recv_info);		// InfoMap：
					DataBaseAction.SaveData(UpInfoMap);					
					
					// 把 UpInfoMap 中的 data(mac层数据)数据解析
					// 并构造含回送 data 数据的 InfoForm, 存到 downInfo）
					InfoForm UpInfo;
					DownInfoForm DownInfo = null;
					byte[] UpData;		// 解 base64 但未解密的 phy 数据
					byte[] downData;		// 加密但未 base64 编码
					MacPktForm macpkt;
					OperateMacPkt opmacpkt;
					byte mhdr;
					for(Entry<String, InfoForm> entry : UpInfoMap.entrySet()){						
						UpInfo = entry.getValue();
						UpInfoMap.remove(entry);
						UpData = UpInfo.getData();	// 解 base64 后的
						mhdr = UpData[0];
						System.out.println("===============:" + ((mhdr & 0xff) >> 5));
						try{
							// 通过反射创建不同的对象,用于不同的 Mtype 类型
							Class<?> cls = Class.forName(OPMACLISTNAME.get( ((mhdr & 0xff) >> 5 )) );
							Constructor<?> ctr = cls.getConstructor();
							opmacpkt = (OperateMacPkt) ctr.newInstance();
							
							// 6 种不同类型的 Mtype 会调用不同的 解析和构造 操作
							// MacParseData: 先将 byte[] 解密后解析并生成 macpkt 对象. 
							// macpkt 含 mac 层各数据的对象
							macpkt = opmacpkt.MacParseData(UpData);
							
							// 构造 phy 层数据,即完整的 JSON 格式中的 data 数据部分
							// MacConstructData: 先构造回复的对象,再转为byte[]
							// 对于 accept 帧不加密,对于数据帧需要加密 frame 部分
							// accept 帧的加密操作在 Phypkt2byte 中完成，便于 MIC 的生成
							// 非确认帧的 downData 应该为 null
							downData = PhyConstruct.PhyPkt2Byte(opmacpkt.MacConstructData(macpkt), ((mhdr & 0xff) >> 5));
							
							
							// 构造回送的 Info, 并加到 DownInfoMap 中
							// 如何调用不同的 ConstructDownInfo 以构造出不同类型的 DownInfo
							// 有没有能够识别是 class A\B\C 的字段
							
							// 需要根据 downData 是否为 null , 判断是否需要构造 downInfo
							Class<?> clsInfo = Class.forName("jsonform.DownInfoA");// 这里现将其写死, 即简单实现 Class A 的封装
							Constructor<?> ctrInfo = clsInfo.getConstructor();
							DownInfo = (DownInfoForm) ctrInfo.newInstance();
							DownInfoMap.put(entry.getKey(), DownInfo.ConstructDownInfo(UpInfo, downData));							
						} catch(Exception e){
							e.printStackTrace();
						}
					}	// end for
					
					
					break;
				case PKT_PULL_DATA:
					System.out.println("PULL_DATA" + recv_info);
					buffer[3] = PKT_PULL_ACK;
					send_pkt = new DatagramPacket(
							buffer, 4,
							recv_pkt.getAddress(), recv_pkt.getPort());
					dsock.send(send_pkt);
					
					if(DownInfoMap.isEmpty())
						break;
					DownInfoForm info;
					byte[] down;
					buffer[3] = PKT_PULL_RESP;
					
					for(Entry<String, DownInfoForm> entry : DownInfoMap.entrySet()){	
						info = entry.getValue();
						// 构造 JSON 数据
						down = ConstructJson.ToJsonStr(info).getBytes();
						byte[] phyDown = new byte[down.length + 4];
						System.arraycopy(buffer, 0, phyDown, 0, buffer.length);
						System.arraycopy(down, 0, phyDown, buffer.length, down.length);
						
						send_pkt = new DatagramPacket(
								phyDown, phyDown.length,
								recv_pkt.getAddress(), recv_pkt.getPort());
						dsock.send(send_pkt);
					}
					
					break;
				default:
	                  System.out.println(", unexpected command");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		// 	break;
		}
	}
}