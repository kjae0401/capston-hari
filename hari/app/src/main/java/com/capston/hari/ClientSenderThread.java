package com.capston.hari;

import java.io.DataOutputStream;

public class ClientSenderThread extends Thread {
    String msg;
    public ClientSenderThread(String msg) {
        this.msg = msg;
    }

    public void run() {
        try {
            DataOutputStream outputStream = new DataOutputStream(SocketData.getSocket().getOutputStream());
            outputStream.writeUTF(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
