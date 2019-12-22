package com.capston.hari;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class PersonInvite extends AppCompatActivity {
    String string;
    ArrayList<String> items = new ArrayList<String>();
    ArrayAdapter<String> Friends_Adapter;
    ListView Friends_ListView;
    private final char spliter = 0x11;
    String roomnumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_chatting_room);

        Intent intent = getIntent();
        roomnumber = intent.getStringExtra("roomnumber");
        Friends_Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, items);
        Friends_ListView = (ListView) findViewById(R.id.ListView_CreateChattingRoom_FriendsList);
        Friends_ListView.setAdapter(Friends_Adapter);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String str;
                    URL url = new URL("http://113.198.237.95:80/jspServer/findfriends.jsp");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestMethod("POST");
                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                    osw.write("sql=select friendid from friends where myid='" + UserInfo.getUserID() + "' and friendid NOT IN (select id from roominperson where id!='" + UserInfo.getUserID() + "' and roomnumber='" + roomnumber + "')");
                    osw.flush();
                    if(conn.getResponseCode() == conn.HTTP_OK) {
                        InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                        BufferedReader reader = new BufferedReader(tmp);
                        StringBuffer buffer;

                        while ((str = reader.readLine()) != null) {
                            buffer = new StringBuffer();
                            buffer.append(str);
                            string = buffer.toString().trim();

                            if(!string.isEmpty()) {
                                items.add(string);
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

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Friends_Adapter.notifyDataSetChanged();

        Button create = (Button) findViewById(R.id.Button_CreateChattingRoom_Create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SparseBooleanArray checkedItems = Friends_ListView.getCheckedItemPositions();
                int count = Friends_Adapter.getCount() ;
                String member = "req_invite" + spliter + roomnumber + spliter + CardContent.list(roomnumber).get(0)[1] + spliter + CardContent.list(roomnumber).get(0)[2]; // 1 = 잔액 2 = 계좌
                for (int i = count-1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        member = member + spliter + items.get(i);
                    }
                }

                ClientSenderThread clientSenderThread = new ClientSenderThread(member);
                clientSenderThread.start();
                try {
                    clientSenderThread.join();
                } catch (Exception e) { }

                finish();
            }
        });
    }
}