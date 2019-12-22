package com.capston.hari;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FriendAdd extends Activity {
    boolean result1 = false;
    boolean result2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        setContentView(R.layout.friend_add_dialog);

        TextView okay = (TextView)findViewById(R.id.Button_friend_add_ok);
        TextView cancle = (TextView)findViewById(R.id.Button_friend_add_cancle);
        final EditText editText = (EditText)findViewById(R.id.EditText_friend_add_id);

        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread1 = new Thread() {
                    @Override
                    public void run() {
                        try {
                            String str;
                            URL url = new URL("http://113.198.237.95:80/jspServer/member.jsp");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            conn.setRequestMethod("POST");
                            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                            osw.write("sql=select id from usertbl where id='" + editText.getText().toString() + "'");
                            osw.flush();
                            if(conn.getResponseCode() == conn.HTTP_OK) {
                                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                                BufferedReader reader = new BufferedReader(tmp);
                                StringBuffer buffer;

                                while ((str = reader.readLine()) != null) {
                                    buffer = new StringBuffer();
                                    buffer.append(str);

                                    if(buffer.toString().trim().contains("실패")) {
                                        result1 = false;
                                    } else { result1 = true; }
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

                Thread thread2 = new Thread() {
                    @Override
                    public void run() {
                        try {
                            String str;
                            URL url = new URL("http://113.198.237.95:80/jspServer/member.jsp");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            conn.setRequestMethod("POST");
                            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                            osw.write("sql=select FRIENDID from FRIENDS where MYID='" + UserInfo.getUserID() + "' and friendid = '" + editText.getText().toString() + "'");
                            osw.flush();
                            if(conn.getResponseCode() == conn.HTTP_OK) {
                                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                                BufferedReader reader = new BufferedReader(tmp);
                                StringBuffer buffer;

                                while ((str = reader.readLine()) != null) {
                                    buffer = new StringBuffer();
                                    buffer.append(str);

                                    if(buffer.toString().trim().contains("성공")) {
                                        result2 = false;
                                    } else { result2 = true; }
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

                if(!UserInfo.getUserID().equals(editText.getText().toString())) {
                    thread1.start();
                    thread2.start();

                    try {
                        thread1.join();
                        thread2.join();
                    } catch (Exception e) { }
                }

                if (result1 == true && result2 == true) {
                    Thread thread3 = new Thread() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL("http://113.198.237.95:80/jspServer/member.jsp");
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                conn.setRequestMethod("POST");
                                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                                osw.write("sql=insert into friends values('" + UserInfo.getUserID() + "','" + editText.getText().toString() + "')");
                                osw.flush();

                                if(conn.getResponseCode() == conn.HTTP_OK) {
                                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                                    BufferedReader reader = new BufferedReader(tmp);
                                    StringBuffer buffer;
                                    String str;
                                    while ((str = reader.readLine()) != null) {
                                        buffer = new StringBuffer();
                                        buffer.append(str);

                                        if(buffer.toString().trim().contains("성공")) {
                                            result2 = false;
                                        } else { result2 = true; }
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

                    thread3.start();

                    try {
                        thread3.join();
                    } catch (Exception e) { }

                    Intent intent = new Intent("Friendadd");
                    intent.putExtra("add", editText.getText().toString());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }

                finish();
            }
        });

        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
