package com.capston.hari;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

public class MemberJoin extends AppCompatActivity {
    private boolean IDoverlab, EMAILoverlab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memberjoin_page);

        final Button button = (Button) findViewById(R.id.Button_memberjoin_join);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editTextid = (EditText) findViewById(R.id.EditText_memberjoin_id);
                final EditText editTextpwd = (EditText) findViewById(R.id.EditText_memberjoin_pwd);
                final EditText editTextpwd_confirm = (EditText) findViewById(R.id.EditText_memberjoin_pwd_confirm);
                final EditText editTextemail = (EditText) findViewById(R.id.EditText_memberjoin_email);
                editTextid.requestFocus(); editTextpwd.requestFocus(); editTextpwd_confirm.requestFocus(); editTextemail.requestFocus();

                if (editTextid.getText().toString().equals("")) { editTextid.requestFocus(); }
                else if (editTextpwd.getText().toString().equals("")) { editTextpwd.requestFocus(); }
                else if (!editTextpwd.getText().toString().equals(editTextpwd_confirm.getText().toString())) { editTextpwd_confirm.requestFocus(); }
                else if (!Pattern.matches("^[a-z0-9]+$", editTextid.getText().toString()) || editTextid.getText().toString().length() < 5 || editTextid.getText().toString().length() > 12) { editTextid.requestFocus(); }
                else if (!Pattern.matches("^[a-zA-Z0-9]+$", editTextpwd.getText().toString()) || editTextpwd.getText().toString().length() < 8 || editTextpwd.getText().toString().length() > 16){ editTextpwd.requestFocus(); }
                else if (!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-z0-9.-]+\\.[a-zA-Z]{2,6}$", editTextemail.getText().toString())){ editTextemail.requestFocus(); }
                else if (!IDoverlab) { editTextid.requestFocus(); }
                else if (!EMAILoverlab) { editTextemail.requestFocus(); } else {
                    try {
                        ConnectSERVER connectSERVER = new ConnectSERVER();
                        String AddMemberSQL = "sql=insert into USERTBL(id, pwd, email) values('" + editTextid.getText().toString() +
                                "','" + editTextpwd.getText().toString() + "','" + editTextemail.getText().toString() + "')";

                        String result = connectSERVER.execute("member.jsp",AddMemberSQL).get();


                        if (result.contains("성공")) {
                            AlertDialog.Builder alter = new AlertDialog.Builder(MemberJoin.this);
                            if (sendEmail_Auth(editTextemail.getText().toString())) {
                                alter.setTitle("회원가입에 성공하였습니다.")
                                        .setMessage("입력하신 이메일로 인증 메일을 보냈으며,\n이메일 인증 후 계정 사용 가능합니다.")
                                        .setCancelable(false)
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                finish();
                                            }
                                        });
                            } else {

                            }
                            AlertDialog dialog = alter.create();
                            dialog.show();
                        }
                        else if (result.contains("실패")) {
                            Toast.makeText(getApplicationContext(), "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.i("DB", "...........ERROR...........");
                        Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        findViewById(R.id.EditText_memberjoin_id).setOnFocusChangeListener(onFocusChangeListener);
        findViewById(R.id.EditText_memberjoin_pwd).setOnFocusChangeListener(onFocusChangeListener);
        findViewById(R.id.EditText_memberjoin_pwd_confirm).setOnFocusChangeListener(onFocusChangeListener);
        findViewById(R.id.EditText_memberjoin_email).setOnFocusChangeListener(onFocusChangeListener);
    }

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            switch (view.getId()) {
                case R.id.EditText_memberjoin_id:
                    if(!hasFocus){
                        EditText editTextid = (EditText) findViewById(R.id.EditText_memberjoin_id);
                        TextView idck = (TextView)findViewById(R.id.TextView_memberjoin_idck);

                        if (editTextid.getText().toString().equals("")) {
                            idck.setText("필수 정보입니다.");
                            idck.setTextColor(Color.RED);
                            break;
                        } else if (!Pattern.matches("^[a-z0-9]+$", editTextid.getText().toString()) || editTextid.getText().toString().length() < 5 || editTextid.getText().toString().length() > 12) {
                            idck.setText("5~12자의 영문 소문자, 숫자만 사용 가능합니다.");
                            idck.setTextColor(Color.RED);
                            break;
                        }

                        ConnectSERVER connectSERVER = new ConnectSERVER();
                        String sql = "sql=select id from USERTBL where id = '" + editTextid.getText().toString() + "'";
                        try {
                            String result = connectSERVER.execute("member_overlap.jsp",sql).get();

                            if (result.contains("true")) {
                                idck.setText("사용 가능한 아이디 입니다.");
                                idck.setTextColor(Color.GREEN);
                                IDoverlab = true;
                            } else if (result.contains("false")) {
                                idck.setText("이미 가입된 아이디 입니다.");
                                idck.setTextColor(Color.RED);
                                IDoverlab = false;
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                            Log.i("DB", "...........ERROR...........");
                            IDoverlab = false;
                        }
                    }
                    break;

                case R.id.EditText_memberjoin_pwd:
                    EditText editTextpwd = (EditText) findViewById(R.id.EditText_memberjoin_pwd);
                    TextView pwdck = (TextView)findViewById(R.id.TextView_memberjoin_pwdck);

                    if(!hasFocus) {
                        if (editTextpwd.getText().toString().equals("")) {
                            pwdck.setText("필수 정보입니다.");
                            pwdck.setTextColor(Color.RED);
                        } else if (!Pattern.matches("^[a-zA-Z0-9]+$", editTextpwd.getText().toString()) || editTextpwd.getText().toString().length() < 8 || editTextpwd.getText().toString().length() > 16) {
                            pwdck.setText("8~16자의 영문 대 소문자, 숫자만 사용 가능합니다.");
                            pwdck.setTextColor(Color.RED);
                        } else {
                            pwdck.setText("사용 가능한 비밀번호 입니다.");
                            pwdck.setTextColor(Color.GREEN);
                        }
                    }
                    break;

                case R.id.EditText_memberjoin_pwd_confirm:
                    EditText pwd_compare = (EditText) findViewById(R.id.EditText_memberjoin_pwd);
                    EditText editTextpwd_confirm = (EditText) findViewById(R.id.EditText_memberjoin_pwd_confirm);
                    TextView pwdcinfirmck = (TextView)findViewById(R.id.TextView_memberjoin_pwdconfirmck);

                    if(!hasFocus) {
                        if (!pwd_compare.getText().toString().equals(editTextpwd_confirm.getText().toString())) {
                            pwdcinfirmck.setText("비밀번호가 일치하지 않습니다.");
                            pwdcinfirmck.setTextColor(Color.RED);
                        } else {
                            pwdcinfirmck.setText("");
                        }
                    }
                    break;

                case R.id.EditText_memberjoin_email:
                    EditText editTextemail = (EditText) findViewById(R.id.EditText_memberjoin_email);
                    TextView emailck = (TextView)findViewById(R.id.TextView_memberjoin_emailck);

                    if(!hasFocus) {
                        if (editTextemail.getText().toString().equals("")) {
                            emailck.setText("필수 정보입니다.");
                            emailck.setTextColor(Color.RED);
                        } else if (!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-z0-9.-]+\\.[a-zA-Z]{2,6}$", editTextemail.getText().toString())) {
                            emailck.setText("잘못된 이메일 형식입니다.");
                            emailck.setTextColor(Color.RED);
                        } else {
                            ConnectSERVER connectSERVER = new ConnectSERVER();
                            String sql = "sql=select * from USERTBL where email = '" + editTextemail.getText().toString() + "'";
                            try {
                                String result = connectSERVER.execute("member_overlap.jsp",sql).get();

                                if (result.contains("true")) {
                                    emailck.setText("사용 가능한 이메일 입니다.");
                                    emailck.setTextColor(Color.GREEN);
                                    EMAILoverlab = true;
                                } else if (result.contains("false")) {
                                    emailck.setText("이미 가입된 이메일 입니다.");
                                    emailck.setTextColor(Color.RED);
                                    EMAILoverlab = false;
                                }
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
                                Log.i("DB", "...........ERROR...........");
                                EMAILoverlab = false;
                            }
                        }
                    }
                    break;
            }
        }
    };

    private boolean sendEmail_Auth(String email) {
        boolean send = false;
        ConnectSERVER msg = new ConnectSERVER();
        try {
            String result = msg.execute("emailSendAction.jsp", "email=" + email).get();
            Log.i("123", result);
            if(result.contains("성공")) { send = true; }
            else if(result.contains("실패")) {
                Log.e("EMAIL SEND", email + " 인증메일이 안보내졌음.");
                send = false;
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
            send = false;
        }
        return send;
    }
}