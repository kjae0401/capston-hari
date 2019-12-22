package com.capston.hari;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardAdd extends Activity {
    EditText editText;
    private final char spliter = 0x11;
    Boolean check=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        setContentView(R.layout.card_add_dialog);

        Intent intent = getIntent();
        final String roomnumber = intent.getStringExtra("roomnumber");
        editText = (EditText)findViewById(R.id.EditText_card_add);
        TextView ok = (TextView)findViewById(R.id.Button_card_add_ok);
        TextView cancle = (TextView)findViewById(R.id.Button_card_add_cancle);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pattern p = Pattern.compile("([0-9]{4})");
                Matcher matcher = p.matcher(editText.getText().toString());

                if (matcher.matches()) {
                    //소켓 -> DB 저장 -> 리시브 -> 카드데이터 저장
                    Thread read = new Thread() {
                        @Override
                        public void run() {
                            try {
                                String str;
                                URL url = new URL("http://113.198.237.95:80/jspServer/member.jsp");
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                conn.setRequestMethod("POST");
                                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                                osw.write("sql=select roomnumber from chattingroom where account='" + editText.getText().toString() + "'");
                                osw.flush();
                                if (conn.getResponseCode() == conn.HTTP_OK) {
                                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                                    BufferedReader reader = new BufferedReader(tmp);
                                    StringBuffer buffer;

                                    while ((str = reader.readLine()) != null) {
                                        buffer = new StringBuffer();
                                        buffer.append(str);
                                        String string = buffer.toString().trim();

                                        if (string.contains("성공")) {
                                            check = true;
                                        } else {
                                            check = false;
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

                    read.start();
                    try {
                        read.join();
                    } catch (Exception e) { }

                    if (check) {
                        Toast.makeText(getApplicationContext(), "카드 정보 추가에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        String msg = "req_cardadd" + spliter + roomnumber + spliter + editText.getText().toString();
                        ClientSenderThread clientSenderThread = new ClientSenderThread(msg);
                        clientSenderThread.start();
                        try {
                            clientSenderThread.join();
                            Toast.makeText(getApplicationContext(), "카드 정보가 추가되었습니다.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) { Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해 주세요.", Toast.LENGTH_SHORT).show(); }
                    }
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "입력 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                }
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
