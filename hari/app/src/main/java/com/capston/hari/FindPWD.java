package com.capston.hari;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class FindPWD extends AppCompatActivity {
    private EditText email, id;
    ConnectSERVER connectSERVER;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.findpwd_page);

        findViewById(R.id.Button_findpwd).setOnClickListener(onClickListener);
        findViewById(R.id.Button_findpwd_change).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            email = (EditText) findViewById(R.id.EditText_findpwd_email);
            id = (EditText) findViewById(R.id.EditText_findpwd_id);

            switch (view.getId()) {
                case R.id.Button_findpwd:
                    connectSERVER = new ConnectSERVER();
                    String sql = "sql=select id from USERTBL where id = '" + id.getText().toString() + "'and email='" + email.getText().toString() + "'";
                    try {
                        String result = connectSERVER.execute("member.jsp",sql).get();
                        AlertDialog.Builder alter = new AlertDialog.Builder(FindPWD.this);
                        if (result.contains("성공")) {
                            email.setVisibility(View.INVISIBLE);
                            id.setVisibility(View.INVISIBLE);
                            findViewById(R.id.Button_findpwd).setVisibility(View.INVISIBLE);
                            findViewById(R.id.EditText_findpwd_changepwd).setVisibility(View.VISIBLE);
                            findViewById(R.id.EditText_findpwd_changepwdconfirm).setVisibility(View.VISIBLE);
                            findViewById(R.id.Button_findpwd_change).setVisibility(View.VISIBLE);
                            findViewById(R.id.EditText_findpwd_changepwd).requestFocus();
                        } else if (result.contains("실패")) {
                            alter.setTitle("")
                                    .setMessage("존재하는 계정이 없습니다.")
                                    .setCancelable(false)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    });
                            AlertDialog dialog = alter.create();
                            dialog.show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                        Log.i("DB", "...........ERROR...........");
                    }
                    break;

                case R.id.Button_findpwd_change:
                    EditText editTextpwd = (EditText) findViewById(R.id.EditText_findpwd_changepwd);
                    EditText editTextpwdconfirm = (EditText) findViewById(R.id.EditText_findpwd_changepwdconfirm);

                    if (editTextpwd.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(),"변경할 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                        editTextpwd.requestFocus();
                    }
                    else if (!Pattern.matches("^[a-zA-Z0-9]+$", editTextpwd.getText().toString()) || editTextpwd.getText().toString().length() < 8 || editTextpwd.getText().toString().length() > 16) {
                        Toast.makeText(getApplicationContext(),"비밀번호는 8~16자의 영문 대 소문자, 숫자만 사용 가능합니다.", Toast.LENGTH_SHORT).show();
                        editTextpwd.requestFocus();
                    }
                    else {
                        if(editTextpwd.getText().toString().equals(editTextpwdconfirm.getText().toString())) {
                            connectSERVER = new ConnectSERVER();
                            String changesql = "sql=update USERTBL set PWD  = '" + editTextpwd.getText().toString() + "' where id= '" + id.getText().toString() + "'";
                            try {
                                String result = connectSERVER.execute("member.jsp",changesql).get();
                                AlertDialog.Builder alter = new AlertDialog.Builder(FindPWD.this);
                                if (result.contains("성공")) {
                                    alter.setTitle("비밀번호 변경 성공")
                                            .setMessage("비밀번호 변경이 완료되었습니다.")
                                            .setCancelable(false)
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    finish();
                                                }
                                            });
                                } else if (result.contains("실패")) {
                                    alter.setTitle("비밀번호 변경 실패")
                                            .setMessage("비밀번호 변경에 실패하였습니다.")
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

                        } else {
                            Toast.makeText(getApplicationContext(),"비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                            editTextpwdconfirm.requestFocus();
                        }
                    }
                    break;
            }
        }
    };
}
