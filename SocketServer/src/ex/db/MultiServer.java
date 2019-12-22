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
 
/*콘솔 멀티채팅 서버 프로그램*/
public class MultiServer {
    HashMap<String,HashMap<String,ServerRecThread>> globalMap; //지역별 해쉬맵을 관리하는 해시맵
    HashMap<String,ServerRecThread> globalMember; // 전체 유저
    ServerSocket serverSocket = null; 
    Socket socket = null;
    static int connUserCount = 0; //서버에 접속된 유저 카운트
    private final char spliter = 0x11;
    
    //생성자
    public MultiServer(){
    	globalMap = new HashMap<String,HashMap<String, ServerRecThread>>();
    	globalMember = new HashMap<String, ServerRecThread>();
    	
       //clientMap = new HashMap<String,DataOutputStream>(); //클라이언트의 출력스트림을 저장할 해쉬맵 생성.
        Collections.synchronizedMap(globalMap); //해쉬맵 동기화 설정.
        Collections.synchronizedMap(globalMember);
        HashMap<String,ServerRecThread> group01 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(group01); //해쉬맵 동기화 설정.
        
        HashMap<String,ServerRecThread> newRoom1 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(newRoom1); //해쉬맵 동기화 설정.
        
        HashMap<String,ServerRecThread> newRoom2 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(newRoom2); //해쉬맵 동기화 설정.
        
        HashMap<String,ServerRecThread> newRoom3 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(newRoom3); //해쉬맵 동기화 설정.
        
        HashMap<String,ServerRecThread> newRoom4 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(newRoom4); //해쉬맵 동기화 설정.
        
        HashMap<String,ServerRecThread> newRoom5 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(newRoom5); //해쉬맵 동기화 설정.
        
        globalMap.put("Master",group01);
        globalMap.put("1",newRoom1);
        globalMap.put("2",newRoom2);
        globalMap.put("3",newRoom3);
        globalMap.put("4",newRoom4);
        globalMap.put("5",newRoom5);
    }//생성자----
   
    public void init(){
        try{
            serverSocket = new ServerSocket(9999); //9999포트로 서버소켓 객체생성.
            System.out.println("##서버가 시작되었습니다.");
           
            while(true){ //서버가 실행되는 동안 클라이언트들의 접속을 기다림.
                socket = serverSocket.accept(); //클라이언트의 접속을 기다리다가 접속이 되면 Socket객체를 생성.
                System.out.println(socket.getInetAddress()+":"+socket.getPort()); //클라이언트 정보 (ip, 포트) 출력
               
                Thread msr = new ServerRecThread(socket); //쓰레드 생성.
                msr.start(); //쓰레드 시동.
            }      
           
        }catch(Exception e){
            e.printStackTrace();
        }
    }
   
    /**해당 클라이언트가 속해있는 그룹에대해서만 메시지 전달.*/
    public void sendGroupMsg(String loc,String msg){       
       
       HashMap<String, ServerRecThread> gMap = globalMap.get(loc);       
       Iterator<String> group_it = globalMap.get(loc).keySet().iterator();
       while(group_it.hasNext()){
            try{
                  ServerRecThread st = gMap.get(group_it.next());
                  st.out.writeUTF(msg);
            }catch(Exception e){
                System.out.println("예외:"+e);
            }
        }   
    }//sendGroupMsg()-----------
    
    /**초대*/
    public void InviteGroupMsg(String loc, String name) {
    	ServerRecThread st = globalMember.get(name);
    	try {
    		st.out.writeUTF("invite" + spliter + loc);
    	} catch(Exception e) {
    		System.out.println("예외:"+e);
    	}
    }
    
    /**각그룹의 접속자수와 서버에 접속된 유저를 반환
     * 하는 메소드**/
    public String getEachMapSize(){
       return getEachMapSize(null);     
    }//getEachMapSize()-----------
    
    /**각그룹의 접속자수와 서버에 접속된 유저를 반환 하는 메소드
     * 추가 지역을 전달받으면 해당 지역을 체크
     * */
    public String getEachMapSize(String loc){
        Iterator global_it = globalMap.keySet().iterator();
        StringBuffer sb = new StringBuffer();
        int sum=0;
        sb.append("=== 그룹 목록 ==="+System.getProperty("line.separator"));
        while(global_it.hasNext()){
            try{
               String key = (String) global_it.next();
               
               HashMap<String, ServerRecThread> it_hash = globalMap.get(key);
               int size = it_hash.size();
               sum +=size;
               sb.append(key+": ("+size+"명)"+(key.equals(loc)?"(*)":"")+"\r\n");
                
            }catch(Exception e){
                System.out.println("예외:"+e);
            }
        }
        sb.append("⊙현재 대화에 참여하고있는 유저수 :"+ sum+ "명 \r\n");
        return sb.toString();
    }//getEachMapSize()-----------
    
    /**문자열 null 값 및 "" 은 대체 문자열로 삽입가능.*/
    public String nVL(String str, String replace){
       String output="";
       if(str==null || str.trim().equals("")){
          output = replace;       
       }else{
          output = str;
       }
       return output;       
    }
    
    //main메서드
    public static void main(String[] args) {
        MultiServer ms = new MultiServer(); //서버객체 생성.
        ms.init();//실행.
    }//main()------  
   
    ////////////////////////////////////////////////////////////////////////
    //----// 내부 클래스 //--------//
   
    // 클라이언트로부터 읽어온 메시지를 다른 클라이언트(socket)에 보내는 역할을 하는 메서드
    class ServerRecThread extends Thread {
        Socket socket;
        DataInputStream in;
        DataOutputStream out;
        String name=""; //이름 저장
        String loc="";  //지역 저장
        MemberManage manage;
        
        //생성자.
        public ServerRecThread(Socket socket){
            this.socket = socket;
            manage = MemberManage.getInstance();
            try{
                //Socket으로부터 입력스트림을 얻는다.
                in = new DataInputStream(socket.getInputStream());
                //Socket으로부터 출력스트림을 얻는다.
                out = new DataOutputStream(socket.getOutputStream());
            }catch(Exception e){
                System.out.println("ServerRecThread 생성자 예외:"+e);
            }
        }//생성자 ------------
        
        /**접속된 유저리스트  문자열로 반환*/        
        public String showUserList(){
            StringBuilder output = new StringBuilder("==접속자목록==\r\n");
            Iterator it = globalMap.get(loc).keySet().iterator(); //해쉬맵에 등록된 사용자이름을 가져옴.
            while(it.hasNext()){ //반복하면서 사용자이름을 StringBuilder에 추가
                 try{
                    String key= (String) it.next();                                   
                    //out.writeUTF(output);
                    if(key.equals(name)){ //현재사용자 체크
                       key += " (*) ";
                    }
                    output.append(key+"\r\n");                    
                 }catch(Exception e){
                     System.out.println("예외:"+e);
                 }
             }//while---------
            output.append("=="+ globalMap.get(loc).size()+"명 접속중==\r\n");
            System.out.println(output.toString());
           return output.toString();
        }//showUserList()-----------
       
       /**메시지 파서 */     
        public String[] getMsgParse(String msg){
           System.out.println("msgParse():msg?   "+ msg);           
           String[] tmpArr = msg.split(String.valueOf(spliter));           
           return tmpArr;
        }
        
        @Override
        public void run(){ //쓰레드를 사용하기 위해서 run()메서드 재정의
           HashMap<String, ServerRecThread> clientMap=null;   //현재 클라이언트가 저장되어있는 해쉬맵        
           
           try{   
                while(in!=null){ //입력스트림이 null이 아니면 반복.
                    String msg = in.readUTF(); //입력스트림을 통해 읽어온 문자열을 msg에 할당.                   
                    String[] msgArr = getMsgParse(msg.substring(msg.indexOf(String.valueOf(spliter))+1));
                    
                    //메세지 처리 ----------------------------------------------
                    if(msg.startsWith("req_logon")){ //로그온 시도 (대화명)                       
                       //req_logon|대화명                       
                          name = msgArr[0]; //넘어온 대화명은 전역변수 name에 저장
                          MultiServer.connUserCount++; //접속자수 증가. (스택틱변수를 사용하기에 어울려서 한번 사용해봄.)
                          out.writeUTF("logon#yes|"+getEachMapSize()); //접속된 클라이언트에게 그룹목록 제공
                          globalMember.put(name, this);
                    }else if(msg.startsWith("req_enterRoom")){ //그룹입장을 시도
                       //req_enterRoom|대화명|지역
                        loc = msgArr[1]; //메시지에서 지역부분만 추출하여 전역변수에 저장
                        
                        sendGroupMsg(loc, "show|[##] "+name + "님이 입장하셨습니다.");
                        clientMap = globalMap.get(loc); //현재그룹의 해쉬맵을 따로 저장.
                        clientMap.put(name, this); //현재 MultiServerRec인스턴스를 클라이언트맵에 저장.
                        System.out.println(getEachMapSize()); //서버에 그룹리스트 출력.
                        out.writeUTF("enterRoom#yes|"+loc); //접속된 클라이언트에게 그룹목록 제공
                        
                    }else if(msg.startsWith("req_say")){ //대화내용 전송
                    	//req_say|아이디|방번호|내용
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
                    	//req_create|아이디들 -> 아이디 분리해서 방에 입장시키는거 까지 해줘야됨
                    	HashMap<String,ServerRecThread> newRoom = new HashMap<String,ServerRecThread>();
                        Collections.synchronizedMap(newRoom); //해쉬맵 동기화 설정.
                        //방번호와 사용자 아이디를 DB에 저장한다.
                        String size = Integer.toString(globalMap.size());
                        
                        //sendGroupMsg(String.valueOf(size), create_msg);
                        //System.out.println(create_msg);
                        globalMap.put(size, newRoom);
                        
                        clientMap = globalMap.get(size); //현재그룹의 해쉬맵을 따로 저장.
                        String sql= "insert into chattingroom(roomnumber) values('" + size + "')";
                        manage.connectionDB(sql);
                        for(int i=0; i<=msgArr.length-2; i++) {
                        	clientMap.put(msgArr[i], globalMember.get(msgArr[i])); //현재 MultiServerRec인스턴스를 클라이언트맵에 저장.
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
                	   sendGroupMsg(msgArr[0], msgArr[1] + "님이 퇴장하셨습니다.");
                	   String del = "delete from roominperson where roomnumber='" + msgArr[0] + "' and id='" + msgArr[1] + "'";
                	   clientMap=globalMap.get(msgArr[0]);
                	   clientMap.remove(msgArr[1]);
                	   manage.connectionDB(del);
                   } else if (msg.startsWith("req_invite")) {
                	   for(int i=1; i<msgArr.length; i++) {
                		   String invite = "insert into roominperson values('" + msgArr[0] + "', '" + msgArr[i] + "')";
                		   manage.connectionDB(invite);
                		   clientMap = globalMap.get(msgArr[0]); //현재그룹의 해쉬맵을 따로 저장.
                           clientMap.put(msgArr[i], globalMember.get(msgArr[i])); //현재 MultiServerRec인스턴스를 클라이언트맵에 저장.
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
                    //------------------------------------------------- 메세지 처리
                }//while()---------
            }catch(Exception e){
                System.out.println("MultiServerRec:run():"+e.getMessage() + "----> ");
                //e.printStackTrace();
            }finally{
                //예외가 발생할때 퇴장. 해쉬맵에서 해당 데이터 제거.
                //보통 종료하거나 나가면 java.net.SocketException: 예외발생
            	Iterator<String> group_it = globalMap.keySet().iterator();
            	while(group_it.hasNext()){
            		String key = group_it.next();
            		clientMap=globalMap.get(key);
            		clientMap.remove(name);
            	}
            	System.out.println("##현재 서버에 접속된 유저는 "+(--MultiServer.connUserCount)+"명 입니다.");            
            }
        }//run()------------
    }//class MultiServerRec-------------
    //////////////////////////////////////////////////////////////////////
}