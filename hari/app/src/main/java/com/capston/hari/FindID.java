package com.capston.hari;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class FindID extends AppCompatActivity {
    private EditText email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.findid_page);

        Button search = (Button) findViewById(R.id.Button_findid);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = (EditText) findViewById(R.id.EditText_findid_email);
                if (email.getText().toString() == "") { Toast.makeText(getApplicationContext(), "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show(); }
                else {
                    ConnectSERVER connectSERVER = new ConnectSERVER();
                    String sql = "sql=select id from USERTBL where email='" + email.getText().toString() + "'";
                    try {
                        String result = connectSERVER.execute("findid.jsp", sql).get();
                        AlertDialog.Builder alter = new AlertDialog.Builder(FindID.this);
                        if (result.contains("실패")) {
                            alter.setTitle("존재하지 않음.")
                                    .setMessage("입력하신 이메일로 가입된 ID가 존재하지 않습니다.")
                                    .setCancelable(false)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    });
                        } else {
                            alter.setTitle(email.getText().toString() + "로 검색된 ID :")
                                    .setMessage(result.trim() + " 입니다.")
                                    .setCancelable(false)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    });
                        }
                        AlertDialog dialog = alter.create();
                        dialog.show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                        Log.i("DB", "...........ERROR...........");
                    }
                }
            }
        });
    }
}
