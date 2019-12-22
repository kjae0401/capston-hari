package com.capston.hari;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

public class SocketData extends Thread{
    private static Socket socket;
    private final String host = "113.198.237.95";
    private final int port = 9999;
    DataInputStream in;
    private static SocketData socketData = null;
    public Handler handler;
    private final char spliter = 0x11;

    public SocketData(final Handler handler) {
        this.handler = handler;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    socket = new Socket();
                    try {
                        socket.connect(new InetSocketAddress(host, port), 3000);
                        try {
                            final DataOutputStream outputStream = new DataOutputStream(SocketData.getSocket().getOutputStream());
                            outputStream.writeUTF("req_logon" + spliter + UserInfo.getUserID());
                            Thread read = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        String str;
                                        URL url = new URL("http://113.198.237.95:80/jspServer/loadchatting.jsp");
                                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                        conn.setConnectTimeout(3000);
                                        conn.setReadTimeout(3000);
                                        conn.setDoOutput(true);
                                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                        conn.setRequestMethod("POST");
                                        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                                        osw.write("sql=select chattingcontent.roomnumber, chattingcontent.id, content, send from chattingcontent, usertbl, roominperson where usertbl.id = roominperson.id and roominperson.roomnumber = chattingcontent.roomnumber and usertbl.id = '" + UserInfo.getUserID() + "' order by send asc");
                                        osw.flush();
                                        if (conn.getResponseCode() == conn.HTTP_OK) {
                                            InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                                            BufferedReader reader = new BufferedReader(tmp);
                                            StringBuffer buffer;

                                            while ((str = reader.readLine()) != null) {
                                                buffer = new StringBuffer();
                                                buffer.append(str);
                                                String string = buffer.toString().trim();

                                                if(!string.isEmpty()){
                                                    String[] list = string.split(String.valueOf(spliter));
                                                    String message="";
                                                    for (int i=2 ; i<list.length-1;i++){
                                                        if (i==2){
                                                            message = message + list[i];
                                                        }else {
                                                            message = message + "\r\n"+list[i];
                                                        }
                                                    }
                                                    String[] content = {list[1], message,list[list.length-1]};
                                                    ChattingContent.addContent(list[0],content);
                                                    outputStream.flush();
                                                }
                                            }
                                        } else {
                                            Log.i("통신 결과", conn.getResponseCode() + "에러");
                                        }

                                        conn.disconnect();
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        String str;
                                        URL url = new URL("http://113.198.237.95:80/jspServer/findfriends.jsp");
                                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                        conn.setConnectTimeout(3000);
                                        conn.setReadTimeout(3000);
                                        conn.setDoOutput(true);
                                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                        conn.setRequestMethod("POST");
                                        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                                        osw.write("sql=select roomnumber from roominperson where id='" + UserInfo.getUserID() + "'");
                                        osw.flush();
                                        if (conn.getResponseCode() == conn.HTTP_OK) {
                                            InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                                            BufferedReader reader = new BufferedReader(tmp);
                                            StringBuffer buffer;
                                            while ((str = reader.readLine()) != null) {
                                                buffer = new StringBuffer();
                                                buffer.append(str);
                                                String string = buffer.toString().trim();
                                                if (!string.isEmpty()) {
                                                    outputStream.writeUTF("req_enterRoom" + spliter + UserInfo.getUserID() + spliter + string);
                                                }
                                                outputStream.flush();
                                            }
                                        } else {
                                            Log.i("통신 결과", conn.getResponseCode() + "에러");
                                        }

                                        conn.disconnect();
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            Thread account = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        String str;
                                        URL url = new URL("http://113.198.237.95:80/jspServer/account.jsp");
                                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                        conn.setConnectTimeout(3000);
                                        conn.setReadTimeout(3000);
                                        conn.setDoOutput(true);
                                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                        conn.setRequestMethod("POST");
                                        OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                                        osw.write("sql=select * from chattingroom where roomnumber IN (select roomnumber from roominperson where id='" + UserInfo.getUserID() + "')");
                                        osw.flush();
                                        if(conn.getResponseCode() == conn.HTTP_OK) {
                                            InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                                            BufferedReader reader = new BufferedReader(tmp);
                                            StringBuffer buffer;
                                            while ((str = reader.readLine()) != null) {
                                                buffer = new StringBuffer();
                                                buffer.append(str);
                                                String string=null;
                                                string = buffer.toString().trim();

                                                if(!string.isEmpty()) {
                                                    String[] list = string.split(String.valueOf(spliter));
                                                    if (list[2].equals("null")) {
                                                        String[] content = { list[0], list[1], "null" };
                                                        CardContent.addContent(list[0], content);
                                                    } else {
                                                        String[] content = { list[0], list[1], list[2] };
                                                        CardContent.addContent(list[0], content);
                                                    }
                                                }
                                            }
                                        } else {
                                            Log.i("통신 결과", conn.getResponseCode()+"에러");
                                        }

                                        conn.disconnect();
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            read.start();
                            thread.start();
                            account.start();

                            try {
                                read.join();
                                thread.join();
                                account.join();
                            } catch (Exception e) { }
                        } catch (Exception e) { }

                        try {
                            in = new DataInputStream(SocketData.getSocket().getInputStream());
                        } catch (Exception e) {
                            socket = null;
                        }

                        while (in != null) { //입력스트림이 null이 아니면..반복
                            try {
                                String msg = in.readUTF(); //입력스트림을 통해 읽어온 문자열을 msg에 할당.
                                final String[] msgArr = getMsgParse(msg.substring(msg.indexOf(String.valueOf(spliter)) + 1));

                                System.out.println(msg);

                                //메세지 처리 ----------------------------------------------
                                if (msg.startsWith("say")) { //대화내용
                                    //say|아이디|대화내용
                                    NotifyData.User = msgArr[0];
                                    NotifyData.contents = msgArr[1];
                                    NotifyData.date = msgArr[2];
                                    NotifyData.Title = msgArr[3] + "번방";

                                    String[] s = { msgArr[0], msgArr[1], msgArr[2] };
                                    ChattingContent.addContent(msgArr[3], s);
                                    handler.sendEmptyMessage(0);
                                } else if (msg.startsWith("create") || msg.startsWith("invite")) {
                                    //create|방번호
                                    NotifyData.room = msgArr[0];
                                    handler.sendEmptyMessage(1);
                                } else if (msg.startsWith("invite")) {
                                    //create|방번호
                                    NotifyData.room = msgArr[0];
                                    String[] content = {msgArr[0], msgArr[1], msgArr[2]};
                                    CardContent.addContent(msgArr[0], content);
                                    handler.sendEmptyMessage(1);
                                } else if (msg.startsWith("cardadd")) {
                                    CardContent.list(msgArr[0]).get(0)[2] = msgArr[1];
                                } else if (msg.startsWith("cardcontent")) {
                                    NotifyData.room = msgArr[0];
                                    NotifyData.money = msgArr[3];
                                    NotifyData.use = msgArr[4];
                                    NotifyData.date = msgArr[5];
                                    CardContent.list(msgArr[0]).get(0)[1] = msgArr[6];
                                    handler.sendEmptyMessage(2);
                                } else if (msg.startsWith("delete")) {
                                    NotifyData.room = msgArr[0];
                                    NotifyData.money = msgArr[1];
                                    NotifyData.date = msgArr[2];
                                    CardContent.list(msgArr[0]).get(0)[1] = msgArr[3];
                                    handler.sendEmptyMessage(3);
                                }
                            } catch (SocketException e) {
                                Log.i("msg", "예외:" + e);
                                return;
                            } catch (Exception e) {
                                Log.i("msg", "Receiver:run() 예외:" + e);
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        Log.i("msg", "socket out");
                    }
                }
            }
        }).start();
    }

    public static SocketData getInstance(Handler handler) {
        if(socketData == null || socket == null) {
            socketData = new SocketData(handler);
        }
        return socketData;
    }

    public static Socket getSocket() {
        return socket;
    }

    /**메시지 파서*/
    public String[] getMsgParse(String msg){
        String[] tmpArr = msg.split(String.valueOf(spliter));
        return tmpArr;
    }
}