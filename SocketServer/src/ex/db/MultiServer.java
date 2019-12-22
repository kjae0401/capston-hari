package ex.db;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
 
/*�ܼ� ��Ƽä�� ���� ���α׷�*/
public class MultiServer {
    HashMap<String,HashMap<String,ServerRecThread>> globalMap; //������ �ؽ����� �����ϴ� �ؽø�
    HashMap<String,ServerRecThread> globalMember; // ��ü ����
    ServerSocket serverSocket = null; 
    Socket socket = null;
    static int connUserCount = 0; //������ ���ӵ� ���� ī��Ʈ
    private final char spliter = 0x11;
    
    //������
    public MultiServer(){
    	globalMap = new HashMap<String,HashMap<String, ServerRecThread>>();
    	globalMember = new HashMap<String, ServerRecThread>();
    	
       //clientMap = new HashMap<String,DataOutputStream>(); //Ŭ���̾�Ʈ�� ��½�Ʈ���� ������ �ؽ��� ����.
        Collections.synchronizedMap(globalMap); //�ؽ��� ����ȭ ����.
        Collections.synchronizedMap(globalMember);
        HashMap<String,ServerRecThread> group01 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(group01); //�ؽ��� ����ȭ ����.
        
        HashMap<String,ServerRecThread> newRoom1 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(newRoom1); //�ؽ��� ����ȭ ����.
        
        HashMap<String,ServerRecThread> newRoom2 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(newRoom2); //�ؽ��� ����ȭ ����.
        
        HashMap<String,ServerRecThread> newRoom3 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(newRoom3); //�ؽ��� ����ȭ ����.
        
        HashMap<String,ServerRecThread> newRoom4 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(newRoom4); //�ؽ��� ����ȭ ����.
        
        HashMap<String,ServerRecThread> newRoom5 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(newRoom5); //�ؽ��� ����ȭ ����.
        
        globalMap.put("Master",group01);
        globalMap.put("1",newRoom1);
        globalMap.put("2",newRoom2);
        globalMap.put("3",newRoom3);
        globalMap.put("4",newRoom4);
        globalMap.put("5",newRoom5);
    }//������----
   
    public void init(){
        try{
            serverSocket = new ServerSocket(9999); //9999��Ʈ�� �������� ��ü����.
            System.out.println("##������ ���۵Ǿ����ϴ�.");
           
            while(true){ //������ ����Ǵ� ���� Ŭ���̾�Ʈ���� ������ ��ٸ�.
                socket = serverSocket.accept(); //Ŭ���̾�Ʈ�� ������ ��ٸ��ٰ� ������ �Ǹ� Socket��ü�� ����.
                System.out.println(socket.getInetAddress()+":"+socket.getPort()); //Ŭ���̾�Ʈ ���� (ip, ��Ʈ) ���
               
                Thread msr = new ServerRecThread(socket); //������ ����.
                msr.start(); //������ �õ�.
            }      
           
        }catch(Exception e){
            e.printStackTrace();
        }
    }
   
    /**�ش� Ŭ���̾�Ʈ�� �����ִ� �׷쿡���ؼ��� �޽��� ����.*/
    public void sendGroupMsg(String loc,String msg){       
       
       HashMap<String, ServerRecThread> gMap = globalMap.get(loc);       
       Iterator<String> group_it = globalMap.get(loc).keySet().iterator();
       while(group_it.hasNext()){
            try{
                  ServerRecThread st = gMap.get(group_it.next());
                  st.out.writeUTF(msg);
            }catch(Exception e){
                System.out.println("����:"+e);
            }
        }   
    }//sendGroupMsg()-----------
    
    /**�ʴ�*/
    public void InviteGroupMsg(String loc, String name) {
    	ServerRecThread st = globalMember.get(name);
    	try {
    		st.out.writeUTF("invite" + spliter + loc);
    	} catch(Exception e) {
    		System.out.println("����:"+e);
    	}
    }
    
    /**���׷��� �����ڼ��� ������ ���ӵ� ������ ��ȯ
     * �ϴ� �޼ҵ�**/
    public String getEachMapSize(){
       return getEachMapSize(null);     
    }//getEachMapSize()-----------
    
    /**���׷��� �����ڼ��� ������ ���ӵ� ������ ��ȯ �ϴ� �޼ҵ�
     * �߰� ������ ���޹����� �ش� ������ üũ
     * */
    public String getEachMapSize(String loc){
        Iterator global_it = globalMap.keySet().iterator();
        StringBuffer sb = new StringBuffer();
        int sum=0;
        sb.append("=== �׷� ��� ==="+System.getProperty("line.separator"));
        while(global_it.hasNext()){
            try{
               String key = (String) global_it.next();
               
               HashMap<String, ServerRecThread> it_hash = globalMap.get(key);
               int size = it_hash.size();
               sum +=size;
               sb.append(key+": ("+size+"��)"+(key.equals(loc)?"(*)":"")+"\r\n");
                
            }catch(Exception e){
                System.out.println("����:"+e);
            }
        }
        sb.append("������ ��ȭ�� �����ϰ��ִ� ������ :"+ sum+ "�� \r\n");
        return sb.toString();
    }//getEachMapSize()-----------
    
    /**���ڿ� null �� �� "" �� ��ü ���ڿ��� ���԰���.*/
    public String nVL(String str, String replace){
       String output="";
       if(str==null || str.trim().equals("")){
          output = replace;       
       }else{
          output = str;
       }
       return output;       
    }
    
    //main�޼���
    public static void main(String[] args) {
        MultiServer ms = new MultiServer(); //������ü ����.
        ms.init();//����.
    }//main()------  
   
    ////////////////////////////////////////////////////////////////////////
    //----// ���� Ŭ���� //--------//
   
    // Ŭ���̾�Ʈ�κ��� �о�� �޽����� �ٸ� Ŭ���̾�Ʈ(socket)�� ������ ������ �ϴ� �޼���
    class ServerRecThread extends Thread {
        Socket socket;
        DataInputStream in;
        DataOutputStream out;
        String name=""; //�̸� ����
        String loc="";  //���� ����
        MemberManage manage;
        
        //������.
        public ServerRecThread(Socket socket){
            this.socket = socket;
            manage = MemberManage.getInstance();
            try{
                //Socket���κ��� �Է½�Ʈ���� ��´�.
                in = new DataInputStream(socket.getInputStream());
                //Socket���κ��� ��½�Ʈ���� ��´�.
                out = new DataOutputStream(socket.getOutputStream());
            }catch(Exception e){
                System.out.println("ServerRecThread ������ ����:"+e);
            }
        }//������ ------------
        
        /**���ӵ� ��������Ʈ  ���ڿ��� ��ȯ*/        
        public String showUserList(){
            StringBuilder output = new StringBuilder("==�����ڸ��==\r\n");
            Iterator it = globalMap.get(loc).keySet().iterator(); //�ؽ��ʿ� ��ϵ� ������̸��� ������.
            while(it.hasNext()){ //�ݺ��ϸ鼭 ������̸��� StringBuilder�� �߰�
                 try{
                    String key= (String) it.next();                                   
                    //out.writeUTF(output);
                    if(key.equals(name)){ //�������� üũ
                       key += " (*) ";
                    }
                    output.append(key+"\r\n");                    
                 }catch(Exception e){
                     System.out.println("����:"+e);
                 }
             }//while---------
            output.append("=="+ globalMap.get(loc).size()+"�� ������==\r\n");
            System.out.println(output.toString());
           return output.toString();
        }//showUserList()-----------
       
       /**�޽��� �ļ� */     
        public String[] getMsgParse(String msg){
           System.out.println("msgParse():msg?   "+ msg);           
           String[] tmpArr = msg.split(String.valueOf(spliter));           
           return tmpArr;
        }
        
        @Override
        public void run(){ //�����带 ����ϱ� ���ؼ� run()�޼��� ������
           HashMap<String, ServerRecThread> clientMap=null;   //���� Ŭ���̾�Ʈ�� ����Ǿ��ִ� �ؽ���        
           
           try{   
                while(in!=null){ //�Է½�Ʈ���� null�� �ƴϸ� �ݺ�.
                    String msg = in.readUTF(); //�Է½�Ʈ���� ���� �о�� ���ڿ��� msg�� �Ҵ�.                   
                    String[] msgArr = getMsgParse(msg.substring(msg.indexOf(String.valueOf(spliter))+1));
                    
                    //�޼��� ó�� ----------------------------------------------
                    if(msg.startsWith("req_logon")){ //�α׿� �õ� (��ȭ��)                       
                       //req_logon|��ȭ��                       
                          name = msgArr[0]; //�Ѿ�� ��ȭ���� �������� name�� ����
                          MultiServer.connUserCount++; //�����ڼ� ����. (����ƽ������ ����ϱ⿡ ������ �ѹ� ����غ�.)
                          out.writeUTF("logon#yes|"+getEachMapSize()); //���ӵ� Ŭ���̾�Ʈ���� �׷��� ����
                          globalMember.put(name, this);
                    }else if(msg.startsWith("req_enterRoom")){ //�׷������� �õ�
                       //req_enterRoom|��ȭ��|����
                        loc = msgArr[1]; //�޽������� �����κи� �����Ͽ� ���������� ����
                        
                        sendGroupMsg(loc, "show|[##] "+name + "���� �����ϼ̽��ϴ�.");
                        clientMap = globalMap.get(loc); //����׷��� �ؽ����� ���� ����.
                        clientMap.put(name, this); //���� MultiServerRec�ν��Ͻ��� Ŭ���̾�Ʈ�ʿ� ����.
                        System.out.println(getEachMapSize()); //������ �׷츮��Ʈ ���.
                        out.writeUTF("enterRoom#yes|"+loc); //���ӵ� Ŭ���̾�Ʈ���� �׷��� ����
                        
                    }else if(msg.startsWith("req_say")){ //��ȭ���� ����
                    	//req_say|���̵�|���ȣ|����
                    	long time = System.currentTimeMillis();
                    	SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    	SimpleDateFormat dayTime1 = new SimpleDateFormat("yy/MM/dd HH:mm:ss.SSS");
                    	String strDT = dayTime.format(new Date(time));
                    	String strDT1 = dayTime1.format(new Date(time));
                    	
                    	sendGroupMsg(msgArr[1], "say"+spliter+name+spliter+msgArr[2] + spliter + strDT + spliter + msgArr[1]);
                    	String storage = "insert into chattingcontent values('" +
                    			msgArr[1] + "','" + name + "','" + msgArr[2] + "','" +
                    			strDT1 + "')";
                    	System.out.println(manage.connectionDB(storage));
                    }else if(msg.startsWith("req_create")) {
                    	//req_create|���̵�� -> ���̵� �и��ؼ� �濡 �����Ű�°� ���� ����ߵ�
                    	HashMap<String,ServerRecThread> newRoom = new HashMap<String,ServerRecThread>();
                        Collections.synchronizedMap(newRoom); //�ؽ��� ����ȭ ����.
                        //���ȣ�� ����� ���̵� DB�� �����Ѵ�.
                        String size = Integer.toString(globalMap.size());
                        
                        //sendGroupMsg(String.valueOf(size), create_msg);
                        //System.out.println(create_msg);
                        globalMap.put(size, newRoom);
                        
                        clientMap = globalMap.get(size); //����׷��� �ؽ����� ���� ����.
                        String sql= "insert into chattingroom(roomnumber) values('" + size + "')";
                        manage.connectionDB(sql);
                        for(int i=0; i<=msgArr.length-2; i++) {
                        	clientMap.put(msgArr[i], globalMember.get(msgArr[i])); //���� MultiServerRec�ν��Ͻ��� Ŭ���̾�Ʈ�ʿ� ����.
                        	sql = "insert into roominperson values('" + size + "','" + msgArr[i] + "')";
                        	manage.connectionDB(sql);
                        }
                        
                        String create_msg = "create"+ spliter + size;
                        sendGroupMsg(size, create_msg);
                        
                        String say_msg = "say" + spliter + name + spliter + msgArr[msgArr.length-1];
                        
                        long time = System.currentTimeMillis();
                        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    	SimpleDateFormat dayTime1 = new SimpleDateFormat("yy/MM/dd HH:mm:ss.SSS");
                    	String strDT = dayTime.format(new Date(time));
                    	String strDT1 = dayTime1.format(new Date(time));
                    	sendGroupMsg(size, say_msg + spliter + strDT + spliter + size);
                    	String storage = "insert into chattingcontent values('" +
                    			size + "','" + name + "','" + msgArr[msgArr.length-1] + "','" +
                    			strDT1 + "')";
                    	manage.connectionDB(storage);
                   } else if (msg.startsWith("req_out")) {
                	   sendGroupMsg(msgArr[0], msgArr[1] + "���� �����ϼ̽��ϴ�.");
                	   String del = "delete from roominperson where roomnumber='" + msgArr[0] + "' and id='" + msgArr[1] + "'";
                	   clientMap=globalMap.get(msgArr[0]);
                	   clientMap.remove(msgArr[1]);
                	   manage.connectionDB(del);
                   } else if (msg.startsWith("req_invite")) {
                	   for(int i=1; i<msgArr.length; i++) {
                		   String invite = "insert into roominperson values('" + msgArr[0] + "', '" + msgArr[i] + "')";
                		   manage.connectionDB(invite);
                		   clientMap = globalMap.get(msgArr[0]); //����׷��� �ؽ����� ���� ����.
                           clientMap.put(msgArr[i], globalMember.get(msgArr[i])); //���� MultiServerRec�ν��Ͻ��� Ŭ���̾�Ʈ�ʿ� ����.
                		   InviteGroupMsg(msgArr[0], msgArr[i]);
                	   }
                   } else if (msg.startsWith("req_cardadd")) {
                	   String cardadd = "update chattingroom set account='" + msgArr[1] + "' where roomnumber='" + msgArr[0] + "'";
                	   manage.connectionDB(cardadd);
                	   sendGroupMsg(msgArr[0], "cardadd" + spliter + msgArr[0] +spliter + msgArr[1]);
                   } else if (msg.startsWith("req_cardcontent")) {
                	   String room = msgArr[0];
                	   String office = msgArr[1];
                	   String account = msgArr[2];
                	   int money = Integer.parseInt(msgArr[3]);
                	   String use = msgArr[4];
                	   String date = msgArr[5];
                	   
                	   String card = "insert into housekeeping values('" + room + "', '" + office + "', " + account + "," + money + ", '" + use + "', '" + date + "')";
                	   manage.connectionDB(card);
                	   System.out.println(card);
                	   String update = "update chattingroom set change='" + msgArr[6] + "' where roomnumber='" + msgArr[0] + "'";
                	   manage.connectionDB(update);
                	   sendGroupMsg(msgArr[0], "cardcontent" + spliter + msgArr[0] + spliter + msgArr[1] + spliter + msgArr[2] + spliter + msgArr[3] + spliter + msgArr[4] + spliter + msgArr[5] + spliter + msgArr[6]);
                   } else if (msg.startsWith("req_delete")) {
                	   String delete = "delete from housekeeping where roomnumber='" + msgArr[0] + "' and money='" + msgArr[1] + "' and send='" + msgArr[2] + "'";
                	   manage.connectionDB(delete);
                	   String update = "update chattingroom set change='" + msgArr[3] + "' where roomnumber='" + msgArr[0] + "'";
                	   manage.connectionDB(update);
                	   sendGroupMsg(msgArr[0], "delete"+spliter+msgArr[0]+spliter+msgArr[1]+spliter+msgArr[2]+spliter+msgArr[3]);
                   }
                    out.flush();
                    //------------------------------------------------- �޼��� ó��
                }//while()---------
            }catch(Exception e){
                System.out.println("MultiServerRec:run():"+e.getMessage() + "----> ");
                //e.printStackTrace();
            }finally{
                //���ܰ� �߻��Ҷ� ����. �ؽ��ʿ��� �ش� ������ ����.
                //���� �����ϰų� ������ java.net.SocketException: ���ܹ߻�
            	Iterator<String> group_it = globalMap.keySet().iterator();
            	while(group_it.hasNext()){
            		String key = group_it.next();
            		clientMap=globalMap.get(key);
            		clientMap.remove(name);
            	}
            	System.out.println("##���� ������ ���ӵ� ������ "+(--MultiServer.connUserCount)+"�� �Դϴ�.");            
            }
        }//run()------------
    }//class MultiServerRec-------------
    //////////////////////////////////////////////////////////////////////
}