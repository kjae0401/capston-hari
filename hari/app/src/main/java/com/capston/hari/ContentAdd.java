package com.capston.hari;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentAdd extends Activity {
    private final char spliter = 0x11;
    static SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        setContentView(R.layout.content_add_dialog);

        final EditText money = (EditText)findViewById(R.id.EditText_content_add_money);
        final EditText content = (EditText)findViewById(R.id.EditText_content_add_content);
        final CheckBox checkBox = (CheckBox)findViewById(R.id.CheckBox_content_add);
        TextView ok = (TextView)findViewById(R.id.Button_content_add_ok);
        TextView cancle = (TextView)findViewById(R.id.Button_content_add_cancle);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pattern p = Pattern.compile("^[0-9]*$");
                Matcher matcher = p.matcher(money.getText().toString());

                if (!matcher.matches() || money.getText().toString().isEmpty() || content.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "입력 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = getIntent();
                    String roomnumber = intent.getStringExtra("roomnumber");
                    String take=money.getText().toString();
                    String change="";
                    if (checkBox.isChecked()) {
                        change = String.valueOf(Integer.parseInt(CardContent.list(roomnumber).get(0)[1]) + Integer.parseInt(take));
                        finish();
                    } else {
                        change = String.valueOf(Integer.parseInt(CardContent.list(roomnumber).get(0)[1]) - Integer.parseInt(take));
                        take = "-" + take;
                        finish();
                    }
                    Date receivedDate = new Date();
                    String date = format.format(receivedDate);
                    String msg = "req_cardcontent" + spliter + roomnumber + spliter + "null" + spliter + "0" + spliter + take + spliter + content.getText().toString() + spliter + date + spliter + change;

                    ClientSenderThread clientSenderThread = new ClientSenderThread(msg);
                    clientSenderThread.start();

                    try {
                        clientSenderThread.join();
                    } catch (Exception e) { }
                    finish();
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
