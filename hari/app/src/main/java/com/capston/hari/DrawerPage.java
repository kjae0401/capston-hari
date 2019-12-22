package com.capston.hari;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DrawerPage extends Activity {
    FriendsListAdapter Friends_Adapter;
    ListView Friends_ListView;
    String roomnumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer__page);

        Intent data = getIntent();
        roomnumber = data.getStringExtra("roomnumber");

        TextView keeping = (TextView)findViewById(R.id.TextView_drawer_keeping);
        TextView gps = (TextView)findViewById(R.id.TextView_drawer_gps);
        TextView person = (TextView)findViewById(R.id.Button_drawer_personadd);
        Friends_Adapter = new FriendsListAdapter();
        Friends_ListView = (ListView) findViewById(R.id.ListView_drawer_friends);
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
                    osw.write("sql=select id from roominperson where roomnumber='" + roomnumber + "'");
                    osw.flush();
                    if(conn.getResponseCode() == conn.HTTP_OK) {
                        InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                        BufferedReader reader = new BufferedReader(tmp);
                        StringBuffer buffer;
                        String string;

                        while ((str = reader.readLine()) != null) {
                            buffer = new StringBuffer();
                            buffer.append(str);
                            string = buffer.toString().trim();

                            if(!string.isEmpty()) {
                                Friends_Adapter.addItem(string);
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

        keeping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HoseKeeping.class);
                intent.putExtra("roomnumber", roomnumber);
                startActivity(intent);
                finish();
            }
        });

        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GPS.class);
                startActivity(intent);
                finish();
            }
        });

        person.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PersonInvite.class);
                intent.putExtra("roomnumber", roomnumber);
                startActivity(intent);
                finish();
            }
        });
    }
}
