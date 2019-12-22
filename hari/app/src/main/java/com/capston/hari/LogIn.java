package com.capston.hari;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class LogIn extends AppCompatActivity {
    private BackPressCloseHandler backPressCloseHandler;
    private SharedPreferences LoginInformation;
    private EditText ID;
    private EditText PWD;
    private final int MY_PERMISSIONS_REQUEST=1001;
    private String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECEIVE_SMS
    };

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MY_PERMISSIONS_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        backPressCloseHandler = new BackPressCloseHandler(this);

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        }

        LoginInformation = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        if (!LoginInformation.getString("id", "").equals("")) {
            Intent intent = new Intent(getApplicationContext(), Main.class);
            intent.putExtra("id", LoginInformation.getString("id", ""));
            startActivity(intent);
            finish();
        }

        findViewById(R.id.Button_login_login).setOnClickListener(onClickListener);
        findViewById(R.id.Button_page_addmember).setOnClickListener(onClickListener);
        findViewById(R.id.TextView_login_findid).setOnClickListener(onClickListener);
        findViewById(R.id.TextView_login_findpwd).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ID = (EditText) findViewById(R.id.EditText_login_id);
            PWD = (EditText) findViewById(R.id.EditText_login_pwd);

            switch (view.getId()) {
                case R.id.Button_login_login:
                    signUp(ID.getText().toString(), PWD.getText().toString());
                    break;

            case R.id.Button_page_addmember:
                    ID.setText("");
                    PWD.setText("");
                    Intent joinintent = new Intent(getApplicationContext(), MemberJoin.class);
                    startActivity(joinintent);
                    break;

            case R.id.TextView_login_findid:
                    Intent findidintent = new Intent(getApplicationContext(), FindID.class);
                    startActivity(findidintent);
                    break;

            case R.id.TextView_login_findpwd:
                    Intent findpwdintent = new Intent(getApplicationContext(), FindPWD.class);
                    startActivity(findpwdintent);
                    break;
            }
        }
    };

    private void signUp(String id, String pwd) {
        ConnectSERVER connectSERVER = new ConnectSERVER();

        try {
            String sql = "sql=select * from USERTBL where id = '" + id + "'and pwd = '" + pwd + "'";
            String result = connectSERVER.execute("member.jsp", sql).get();
            if (result.contains("성공")) {
                if(email_auth(id, pwd)) {
                    SharedPreferences.Editor editor = LoginInformation.edit();

                    editor.putString("id", id);
                    editor.putString("pwd", pwd);
                    editor.commit();
                    Intent intent = new Intent(getApplicationContext(), Main.class);
                    intent.putExtra("id", id);
                    startActivity(intent);
                    finish();
                } else { Toast.makeText(getApplicationContext(), "이메일 인증 후 사용 가능합니다.", Toast.LENGTH_SHORT).show(); }
            }
            else if (result.contains("실패")) {
                Toast.makeText(getApplicationContext(), "아이디 또는 비밀번호를 다시 확인하세요.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
            Log.i("DB", "...........ERROR...........");
        }
    }

    private boolean email_auth(String id, String pwd) {
        boolean auth = false;
        ConnectSERVER connectSERVER = new ConnectSERVER();

        try {
            String sql = "sql=select * from USERTBL where id = '" + id + "'and pwd = '" + pwd + "'and auth = 'TRUE'";
            String result = connectSERVER.execute("member.jsp", sql).get();

            if(result.contains("성공")) {
                auth = true;
            } else if(result.contains("실패")) { auth = false; }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
            Log.i("DB", "...........ERROR...........");
            auth = false;
        }
        return auth;
    }

    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this,"권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.",Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}
