//NIO 编写服务器
package socket;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import dao.DataBaseAction;
import jsonform.DownInfoForm;
import jsonform.InfoForm;
import mac.MacPktForm;
import mac.OperateMacPkt;
import util.ParseJson;

public class Server implements Runnable{
	private static final byte PKT_PUSH_DATA = 0x00;
	private static final byte PKT_PUSH_ACK = 0x01;
	private static final byte PKT_PULL_DATA = 0x02;
	private static final byte PKT_PULL_ACK = 0x04;
	private static final byte PULL_RESP = 0x03;
	private static final byte TX_ACK = 0x05;
	DatagramChannel channel;  
    Selector selector;
    private byte[] pullack;
	private byte[] pushack;  
	private byte[] pullresp;
	private byte[] uplinkPkts;
	private HashMap<String, InfoForm> UpInfoMap = new HashMap<String, InfoForm>();
	private HashMap<String, DownInfoForm> DownInfoMap = new HashMap<String, DownInfoForm>();
	public List<String> OPMACLISTNAME = new ArrayList<String>();
    @Override
	public void run()  
    {  
    	OPMACLISTNAME.add("mac.OperateMacJoinRequest");
		OPMACLISTNAME.add("mac.OperateMacJoinAccept");
		OPMACLISTNAME.add("mac.OperateMacUnconfiredDataUp");
		OPMACLISTNAME.add("mac.OperateMacConfiredDataUp");
		OPMACLISTNAME.add("mac.OperateMacUnconfiredDataDown");
		OPMACLISTNAME.add("mac.OperateMacConfiredDataDown");
		OPMACLISTNAME.add("mac.OperateMacRFUn");
		OPMACLISTNAME.add("mac.OperateMacProprietary");
        try  
        {  
            // 打开一个UDP Channel  
            channel = DatagramChannel.open();  
            // 设定为非阻塞通道  
            channel.configureBlocking(false);  
            // 绑定端口  
            channel.socket().bind(new InetSocketAddress(1680));  
            // 打开一个选择器  
            selector = Selector.open();  
            
            channel.register(selector, SelectionKey.OP_READ);  
        } catch (Exception e)  
        {  
            e.printStackTrace();  
        }  
        ByteBuffer byteBuffer = ByteBuffer.allocate(65536);  
        while (true)  
        {  
            try  
            {  
                // 进行选择  
                int n = selector.select();  
                if (n > 0)  
                {  
                    // 获取以选择的键的集合  
                    Iterator<?> iterator = selector.selectedKeys().iterator();  
                    while (iterator.hasNext())  
                    {  
                        SelectionKey key = (SelectionKey) iterator.next();  
                        // 必须手动删除  
                        iterator.remove();  
                        if (key.isReadable())  
                        {  
                            DatagramChannel datagramChannel = (DatagramChannel) key.channel();  
                            byteBuffer.clear();  
                            // 读取  
                            InetSocketAddress address = (InetSocketAddress) datagramChannel.receive(byteBuffer);    
                            System.out.println(new String(byteBuffer.array()));  
                            uplinkPkts = null;
                            byteBuffer.get(uplinkPkts);
                            byteBuffer.clear(); 
                            byte[] json = null ;
                    		System.arraycopy(uplinkPkts, 12, json, 0, (uplinkPkts.length)-12);
                    		switch(uplinkPkts[3])
                    		{
                    		case PKT_PUSH_DATA:
                    			pushack = null;
                    	    	System.arraycopy(uplinkPkts, 0, pushack, 0, 3);
                    	    	pushack[3] = PKT_PUSH_ACK;
                    	    	 byteBuffer.put(pushack);  
                                 byteBuffer.flip();  
                                 byte[] content = new byte[byteBuffer.limit()];
                                 byteBuffer.get(content);
                                 datagramChannel.send(byteBuffer, address);
                                 
                                 
                    		case PKT_PULL_DATA:
                    			 pullack = null ;
                    			 System.arraycopy(uplinkPkts, 0, pullack, 0, 3);
                    			 pullack[3] = PKT_PULL_ACK;
                    			 byteBuffer.put(pushack);  
                                 byteBuffer.flip();  
                                 byte[] com = new byte[byteBuffer.limit()];
                                 byteBuffer.get(com);
                                 datagramChannel.send(byteBuffer, address);
                    		
                                 pullresp = null ;
                                 System.arraycopy(uplinkPkts, 0, pullresp, 0, 3);
                                 pullack[3] = PULL_RESP;
                                 //后面是数据的封装+发送  不同数据不同封装
                                 //1.byte[] json 变为 string
                                 
                                 String Json = new String(json);
                                 // 2. 解析string变成 hashmap 并存入数据库 
                             	UpInfoMap = ParseJson.parseOfJson(Json);		// InfoMap：
            					DataBaseAction.SaveData(UpInfoMap);
                                 
                                 //3. 取出data部分 
            					InfoForm UpInfo;
            					DownInfoForm DownInfo = null;
            					byte[] UpMacData;		// 解 base64 但未解密的 mac 数据
            					byte[] downMacData;		// 加密但未 base64 编码
            					MacPktForm macpkt;
            					OperateMacPkt opmacpkt;
            					byte mhdr;
            					for(Entry<String, InfoForm> entry : UpInfoMap.entrySet()){						
            						UpInfo = entry.getValue();
            						UpMacData = UpInfo.getData();	// 解 base64 后的
            						mhdr = UpMacData[0];
            						try{
            							// 通过反射创建不同的对象,用于不同的 Mtype 类型
            							Class<?> cls = Class.forName(OPMACLISTNAME.get(mhdr & 0xe0));
            							Constructor<?> ctr = cls.getConstructor();
            							opmacpkt = (OperateMacPkt) ctr.newInstance();
            							
            							// 6 种不同类型的 Mtype 会调用不同的 解析和构造 操作
            							// MacParseData: 先将 byte[] 解密后解析并生成 macpkt 对象. 
            							// macpkt 含 mac 层各数据的对象
            							macpkt = opmacpkt.MacParseData(UpMacData);
            							
            							// MacConstructData: 先构造回复的对象,再转为byte[],再加密(mac层数据——JSON.data)
            							downMacData = opmacpkt.MacConstructData(macpkt).MacPkt2Byte();
            							
            							// 构造回送的 Info, 并加到 DownInfoMap 中
            							// 如何调用不同的 ConstructDownInfo 以构造出不同类型的 DownInfo
            							// 有没有能够识别是 class A\B\C 的字段
            							DownInfo.ConstructDownInfo(UpInfo, downMacData);
            							DownInfoMap.put(entry.getKey(), DownInfo);							
            						} catch(Exception e){
            							e.printStackTrace();
            						}
            					}
                                 
                                 // bash64 解码data部分
                                 // aes128_padding 解密 frmpayload部分
                                 // 封装自己的数据 并进行加密 bash64编码 
            					
                    		case TX_ACK:
                    			
                    			;
                    		default:
                    			System.out.println("the packet is error!");
                    		}
                          
                        }  
                    }  
                }  
            } 
            catch (Exception e)  
            {  
                e.printStackTrace();  
            }  
        }  
    }
}

