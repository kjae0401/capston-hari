package com.capston.hari;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HoseKeeping extends AppCompatActivity {
    private final char spliter = 0x11;
    ListView Card_ListView;
    CardListAdapter Card_Adapter;
    private final String CHANGE_MESSAGE = "com.exaple.change";
    private final String DELETE_MESSAGE = "com.exaple.delete";
    private BroadcastReceiver mReceiver = null;
    String roomnumber;

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hose_keeping);

        Card_Adapter = new CardListAdapter();
        Card_ListView = (ListView)findViewById(R.id.ListView_hosekeeping);
        Card_ListView.setAdapter(Card_Adapter);
        Intent intent = getIntent();
        roomnumber = intent.getStringExtra("roomnumber");
        Button card = (Button)findViewById(R.id.Button_hosekeeping_card);
        Button content = (Button)findViewById(R.id.Button_hosekeeping_content);

        Thread account = new Thread() {
            @Override
            public void run() {
                try {
                    String str;
                    URL url = new URL("http://113.198.237.95:80/jspServer/account.jsp");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestMethod("POST");
                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                    osw.write("sql=select use, money, send from housekeeping where roomnumber='" + roomnumber + "'");
                    osw.flush();
                    if(conn.getResponseCode() == conn.HTTP_OK) {
                        InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                        BufferedReader reader = new BufferedReader(tmp);
                        StringBuffer buffer;
                        while ((str = reader.readLine()) != null) {
                            buffer = new StringBuffer();
                            buffer.append(str);
                            String string=null;
                            string = buffer.toString().trim();

                            if(!string.isEmpty()) {
                                String[] list = string.split(String.valueOf(spliter));
                                Card_Adapter.addItem(roomnumber, list[0], list[1], list[2]);
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

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CardAdd.class);
                intent.putExtra("roomnumber", roomnumber);
                startActivity(intent);
            }
        });

        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ContentAdd.class);
                intent.putExtra("roomnumber", roomnumber);
                startActivity(intent);
            }
        });

        account.start();
        try {
            account.join();
        }catch (Exception e) { }
        Card_Adapter.datesort();
        Card_Adapter.notifyDataSetChanged();

        Card_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                alert.setTitle("삭제 확인");
                alert.setMessage("해당 내용을 목록에서 지우겠습니까?");
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            String change = String.valueOf(Integer.parseInt(CardContent.list(roomnumber).get(0)[1]) - Integer.parseInt(((CardData)Card_Adapter.getItem(position)).getMoney()));
                            String msg = "req_delete" +spliter + ((CardData)Card_Adapter.getItem(position)).getRoom_number() + spliter + ((CardData)Card_Adapter.getItem(position)).getMoney() + spliter + ((CardData)Card_Adapter.getItem(position)).getDate() + spliter + change;
                            ClientSenderThread clientSenderThread = new ClientSenderThread(msg);
                            clientSenderThread.start();
                            try {
                                clientSenderThread.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                        }
                        dialogInterface.dismiss();
                    }
                });
                alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.show();

                return true;
            }
        });
    }

    private void registerReceiver() {
        if (mReceiver != null) return;

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CHANGE_MESSAGE);
        intentFilter.addAction(DELETE_MESSAGE);

        this.mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(CHANGE_MESSAGE)) {
                    String room = intent.getStringExtra("room");

                    if (roomnumber.equals(room)) {
                        Card_Adapter.addItem(roomnumber, intent.getStringExtra("use"), intent.getStringExtra("money"), intent.getStringExtra("date"));
                        Card_Adapter.datesort();
                        Card_Adapter.notifyDataSetChanged();
                    }
                } else if (intent.getAction().equals(DELETE_MESSAGE)) {
                    String room = intent.getStringExtra("room");
                    String money = intent.getStringExtra("money");
                    String date = intent.getStringExtra("date");
                    for (int i=0; i<Card_Adapter.size(); i++) {
                        if (((CardData)Card_Adapter.getItem(i)).getRoom_number().equals(room) && ((CardData)Card_Adapter.getItem(i)).getMoney().equals(money) && ((CardData)Card_Adapter.getItem(i)).getDate().equals(date)) {
                            Card_Adapter.clear(i);
                            Card_Adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }
        };

        registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}
