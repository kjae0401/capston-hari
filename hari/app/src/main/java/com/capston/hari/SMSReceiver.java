package com.capston.hari;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSReceiver extends BroadcastReceiver {
    String office="";
    String account="";
    String money="";
    String use="";
    String inout="";
    static SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
    static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    static final String BOOT_RECEIVED = "android.intent.action.BOOT_COMPLETED";
    private final char spliter = 0x11;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals(BOOT_RECEIVED)) {
                Intent i = new Intent(context, ClientSocketService.class);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent foregroundServiceIntent = null;
                    foregroundServiceIntent = new Intent(context, ClientSocketService.class);
                    context.startForegroundService(foregroundServiceIntent);
                }
            } else if (intent.getAction().equals(SMS_RECEIVED)) {
                Bundle bundle = intent.getExtras();
                SmsMessage[] messages = parseSmsMessage(bundle);

            // 메세지가 있을 경우 내용을 로그로 출력해 봄
                if (messages.length > 0) {
                    Date receivedDate = new Date();
                    String sms1 = messages[0].getMessageBody();
                    String sms = sms1.replace(" ","");

                    Pattern 정규식1 = Pattern.compile("(.+)(신한|삼성|우리|하나|롯데|현대|KB|국민|BC|카카오)([0-9]*\\/[0-9][0-9])([0-9]*\\:[0-9][0-9])([0-9]*\\-\\*\\*\\*\\-[0-9][0-9]([0-9][0-9][0-9][0-9]))(출금|입금)([0-9]*)(.+)");
                    Pattern 정규식2 = Pattern.compile("(.+)(신한|삼성|우리|하나|롯데|현대|KB|국민|BC|카카오)([0-9]*\\/[0-9][0-9])([0-9]*\\:[0-9][0-9])([0-9]*\\-\\*\\*\\*\\-[0-9][0-9]([0-9][0-9][0-9][0-9]))(출금|입금)(([0-9]*)\\,([0-9]*))(.+)");
                    Pattern 정규식3 = Pattern.compile("(.+)(신한|삼성|우리|하나|롯데|현대|KB|국민|BC|카카오)([0-9]*\\/[0-9][0-9])([0-9]*\\:[0-9][0-9])([0-9]*\\-\\*\\*\\*\\-[0-9][0-9]([0-9][0-9][0-9][0-9]))(출금|입금)(([0-9]*)\\,([0-9]*)\\,([0-9]*))(.+)");

                    Matcher mat1 = 정규식1.matcher(sms);
                    Matcher mat2 = 정규식2.matcher(sms);
                    Matcher mat3 = 정규식3.matcher(sms);


                    if (mat3.matches() == true) {
                        office = mat3.group(2);
                        account = mat3.group(6);
                        money = mat3.group(9)+mat3.group(10)+mat3.group(11);
                        use = mat3.group(12);
                    } else if (mat2.matches() == true){
                        office = mat2.group(2);
                        account = mat2.group(6);
                        money = mat2.group(9) + mat2.group(10);
                        use = mat2.group(11);
                    } else if (mat1.matches() == true){
                        office = mat1.group(2);
                        account = mat1.group(6);
                        account = mat1.group(8);
                        use = mat1.group(9);
                    }

                    if (mat3.matches() == true) {
                        office = mat3.group(2); //카드사
                        account = mat3.group(6);
                        inout = mat3.group(7); //출금 or 입금
                        money = mat3.group(9)+mat3.group(10)+mat3.group(11); //결제 금액
                        use = mat3.group(12); //결제 장소
                    } else if (mat2.matches() == true){
                        office = mat2.group(2); //카드사
                        account = mat2.group(6);
                        inout = mat2.group(7); //출금 or 입금
                        money = mat2.group(9)+mat2.group(10); //결제 금액
                        use = mat2.group(11); //결제 장소
                    } else if (mat1.matches() == true){
                        office = mat1.group(2); //카드사
                        account = mat1.group(6);
                        inout = mat1.group(7); //출금 or 입금
                        money = mat1.group(8); //결제 금액
                        use = mat1.group(9); //결제 장소
                    }

                    String date = format.format(receivedDate);
                    System.out.println(office + " " + account + " " + money + " " + use + " " + date);
                    String change=null;
                    if (!CardContent.isbool(account).equals("null") && office!=null && account!=null && money!=null && use!=null) {
                        if (inout.equals("출금")) {
                            change = String.valueOf(Integer.parseInt(CardContent.list(CardContent.isbool(account)).get(0)[1]) - Integer.parseInt(money));
                            money = "-" + money;
                        } else {
                            change = String.valueOf(Integer.parseInt(CardContent.list(CardContent.isbool(account)).get(0)[1]) + Integer.parseInt(money));
                        }
                        String msg = "req_cardcontent" + spliter + CardContent.isbool(account) + spliter + office + spliter + account + spliter + money + spliter + use + spliter + date + spliter + change;
                        System.out.println(msg);
                        ClientSenderThread clientSenderThread = new ClientSenderThread(msg);
                        clientSenderThread.start();
                        try {
                            clientSenderThread.join();
                        } catch (Exception e) { }
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private SmsMessage[] parseSmsMessage(Bundle bundle) {
        Object[] objs = (Object[]) bundle.get("pdus");
        SmsMessage[] messages = new SmsMessage[objs.length];
        for (int i = 0; i < objs.length; i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String format = bundle.getString("format");
                messages[i] = SmsMessage.createFromPdu((byte[]) objs[i], format);
            } else {
                messages[i] = SmsMessage.createFromPdu((byte[]) objs[i]);
            }
        }
        return messages;
    }
}