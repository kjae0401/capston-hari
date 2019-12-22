package ex.db;

/*�ܼ� ��Ƽä�� Ŭ���̾�Ʈ ���α׷�*/
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.plaf.basic.BasicScrollPaneUI.HSBChangeListener;

public class MultiClient {
	static boolean chatmode = false;
	static int chatState = 0; 
	//0 : �α׿� �ȵȻ���, 1 : �α׿µȻ���, 2: ������Ϸ� (��ȭ����),
	//5 : req_fileSend (������ �������ڿ��� ���������� ������û�� ����)
    public static void main(String[] args) throws UnknownHostException, IOException {
        try{
            String ServerIP = "localhost";
            Socket socket = new Socket(ServerIP, 9999); //���� ��ü ����         
            System.out.println("[##] ������ ������ �Ǿ����ϴ�......");
            //����ڷκ��� ���� ���ڿ��� ������ �������ִ� ������ �ϴ� ������.
            Object object = (Object)socket;
            Socket test = (Socket)object;
            /////////////////////////////////////////////////////////////
            Thread sender = new Sender(test);  
            Thread receiver = new Receiver(socket);
            
            sender.start(); //������ �õ�
            receiver.start(); //������ �õ�
           
        }catch(Exception e){
            System.out.println("����[MultiClient class]:"+e);
        }
    }//main()-------
}//End class MultiClient
/////////////////////////////////////////////////////////////////////
 
//�����κ��� �޽����� �д� Ŭ����
class Receiver extends Thread{
    Socket socket;
    DataInputStream in;
    private final char spliter = 0x11;
    //Socket�� �Ű������� �޴� ������.
    public Receiver(Socket socket){
        this.socket = socket;
        
        try{
            in = new DataInputStream(this.socket.getInputStream());
        }catch(Exception e){
            System.out.println("����:"+e);
        }
    }//������ --------------------
   
    /**�޽��� �ļ�*/     
    public String[] getMsgParse(String msg){
    	//System.out.println("msgParse()=>msg?"+ msg);
    	String[] tmpArr = msg.split(String.valueOf(spliter));
    	return tmpArr;
    }
    
    @Override
    public void run(){ //run()�޼ҵ� ������
    	
        while(in!=null){ //�Է½�Ʈ���� null�� �ƴϸ�..�ݺ�
            try{
            	String msg = in.readUTF(); //�Է½�Ʈ���� ���� �о�� ���ڿ��� msg�� �Ҵ�.
                String[] msgArr = getMsgParse(msg.substring(msg.indexOf(String.valueOf(spliter))+1));
                
                //�޼��� ó�� ----------------------------------------------
                if(msg.startsWith("logon#yes")){ //�α׿� �õ� (��ȭ��)
                	MultiClient.chatState = 1; //ä�� ���¸� �α׿� �� ���·� ����.
                	//logon#yes|�׷츮��Ʈ              
                	System.out.println(msgArr[0]);
                	System.out.println("�������� �Է��� �ּ���:");
                	
                } else if(msg.startsWith("logon#no")){ //�α׿� ���� (��ȭ��)
                	
                	MultiClient.chatState = 0;                	
                	System.out.println("[##] �ߺ��� �̸��� �����մϴ�\n���̸��� �ٽ� �Է��� �ּ���:");
                	//1. �̸��� �ߺ��ɰ��(������ü or �׷�) logon#no ��Ŷ�� �����κ��� ���޵�.
                	//2. �̸��� �ߺ��ɰ�� �������� ��ü������ name(1), name(2) �̷������� �ߺ����� �ʰ� �����ϴ� ���.
                	                	
                } else if(msg.startsWith("enterRoom#yes")){ //�׷�����  
                	
                	//enterRoom#yes|����
                    System.out.println("[##] ä�ù� ("+msgArr[0]+") �� �����Ͽ����ϴ�.");
                    MultiClient.chatState = 2; //ê ���� ���� ( ä�ù����� �Ϸ�� ��ȭ���ɻ���)
                	 
                } else if(msg.startsWith("enterRoom#no")){
                	//enterRoom#no|����
                	 System.out.println("[##] �Է��Ͻ� ["+msgArr[0]+ "]�� ���������ʴ� �����Դϴ�.");
                	 System.out.println("�������� �ٽ� �Է��� �ּ���:");
                
                } else if(msg.startsWith("show")){ //�������������ϰ����ϴ� �޽��� 
                
                	//show|�޽�������     
                	System.out.println(msgArr[0]);
                	
                } else if(msg.startsWith("say")){ //��ȭ����
                	//say|���̵�|��ȭ����            		
            		System.out.println("["+msgArr[0]+"] "+msgArr[1] );	
                } else if(msg.startsWith("req_exit")){ //����
                	
				}
            
            }catch(SocketException e){            	
            	 System.out.println("����:"+e);
            	 System.out.println("##�������� ������ ������ ���������ϴ�.");
            	return;
            	 
            } catch(Exception e){            	
                System.out.println("Receiver:run() ����:"+e);
              
            }
        }//while----
    }//run()------
}//class Receiver -------
 

/////////////////////////////////////////////////////////////////////

//������ �޽����� �����ϴ� Ŭ���� 
class Sender extends Thread {
    Socket socket;
    DataOutputStream out;
    String name;
    private final char spliter = 0x11;
    //������ ( �Ű������� ���ϰ� ����� �̸� �޽��ϴ�. )
    public Sender(Socket socket){ //���ϰ� ����� �̸��� �޴´�.
        this.socket = socket;
        
        try{
            out = new DataOutputStream(this.socket.getOutputStream());        
        }catch(Exception e){
            System.out.println("����:"+e);
        }
    }
   
    @Override
    public void run(){ //run()�޼ҵ� ������
        Scanner s = new Scanner(System.in);
        //Ű����κ��� �Է��� �ޱ����� ��ĳ�� ��ü ����
   
        System.out.println("���̸��� �Է��� �ּ���:");
        
        while(out!=null){ //��½�Ʈ���� null�� �ƴϸ�..�ݺ�
        	try { //while�� �ȿ� try-catch���� ����� ������ while�� ���ο��� ���ܰ� �߻��ϴ���
                  //��� �ݺ��Ҽ��ְ� �ϱ����ؼ��̴�.                   
            	String msg = s.nextLine();
            	
            	if(msg==null||msg.trim().equals("")){
            		
            	   msg=" ";
            	   //continue; //�ֿܼ��� �������� �ѱ�°��� ���� ȿ������.
            	   //System.out.println("����");
            	}
            	
            	if(MultiClient.chatState == 0){     		
            		//���� ��ȭ�� ���� ó���� ���.
            		 if(!msg.trim().equals("")){				 
            			 name=msg;
                 		 out.writeUTF("req_logon"+spliter+msg);
    				 }else{
    					 System.out.println("[##] �̸����� ������ �Է��Ҽ������ϴ�.\r\n" + "���̸��� �ٽ� �Է��� �ּ���:");
    				 }
            	} else if(MultiClient.chatState == 1) {//�α׿µ� �����̸� �׷���� �Է¹ޱ����� ����
            		//req_enterRoom|��ȭ��|������
            		
            		 if(!msg.trim().equals("")){				 
            			 out.writeUTF("req_enterRoom"+spliter+name+spliter+msg);    					
    				 }else{
    					 System.out.println("[##] ������ �Է��Ҽ������ϴ�.\r\n" + "�������� �ٽ� �Է��� �ּ���:");
    				 }
            	} else{
            		//req_say|���̵�|��ȭ����
            		out.writeUTF("req_say"+spliter+name+spliter+5+spliter+msg);
            		//out.writeUTF("req_create|"+name+"|"+msg);
            	}
            }catch(SocketException e){            	
	           	 System.out.println("Sender:run()����:"+e);
	           	 System.out.println("##�������� ������ ������ ���������ϴ�.");
	           	return;           	 
           } catch (IOException e) {
                System.out.println("����:"+e);
           }
        }//while------
    }//run()------
}//class Sender-------
/////////////////////////////////////////////////////////////////////