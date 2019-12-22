package com.capston.hari;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;

public class Chatting extends Fragment {
    ListView Chatting_ListView;
    ChattingRoomListAdapter Chatting_Adapter;
    private SharedPreferences LoginInformation;
    String string;
    private final String BROADCAST_MESSAGE = "com.example.broadcastreceiver";
    private final String CREATE_MESSAGE = "com.example.create";
    private final String CHANGE_MESSAGE = "com.exaple.change";
    private final String DELETE_MESSAGE = "com.exaple.delete";
    private BroadcastReceiver mReceiver = null;
    private final char spliter = 0x11;

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

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.chatting_fragment, container,false);
        LoginInformation = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

        Chatting_Adapter = new ChattingRoomListAdapter();
        Chatting_ListView = (ListView)view.findViewById(R.id.ListView_chatting_list);
        Chatting_ListView.setAdapter(Chatting_Adapter);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String str;
                    URL url = new URL("http://113.198.237.95:80/jspServer/findchattingroom.jsp");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestMethod("POST");
                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                    osw.write("sql=select roominperson.roomnumber, max(content)keep(DENSE_RANK LAST ORDER BY send)as content, max(change)keep(DENSE_RANK LAST ORDER BY send)as change, max(send)keep(DENSE_RANK LAST ORDER BY send)as time from usertbl, roominperson, chattingcontent, chattingroom where usertbl.id = '" + LoginInformation.getString("id", "") + "' and usertbl.id = roominperson.id and chattingcontent.roomnumber = roominperson.roomnumber and chattingroom.roomnumber = roominperson.roomnumber group by roominperson.roomnumber");
                    osw.flush();
                    if(conn.getResponseCode() == conn.HTTP_OK) {
                        InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                        BufferedReader reader = new BufferedReader(tmp);
                        StringBuffer buffer;
                        while ((str = reader.readLine()) != null) {
                            buffer = new StringBuffer();
                            buffer.append(str);
                            string = buffer.toString().trim();

                            if(!string.isEmpty()) {
                                String[] list = string.split(String.valueOf(spliter));
                                String message="";
                                for (int i=1 ; i<list.length-2; i++){
                                    if(i==1){
                                        message = message + list[i];
                                    }else {
                                        message = message + "\r\n" + list[i];
                                    }
                                }
                                Chatting_Adapter.addItem(list[0]+"번방",message,"잔액 : "+list[list.length-2]+"원",list[0],list[list.length-1]);
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
        Chatting_Adapter.datesort();
        Chatting_Adapter.notifyDataSetChanged();

        Chatting_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ChattingRoom.class);
                intent.putExtra("number", ((ChattingData)Chatting_Adapter.getItem(position)).getRoom_number());
                UserInfo.currentRoom = ((ChattingData)Chatting_Adapter.getItem(position)).getRoom_number();
                startActivity(intent);
            }
        });

        Button createButton = (Button) view.findViewById(R.id.Button_chatting_create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent create = new Intent(getContext(), CreateChattingRoom.class);
                startActivity(create);
            }
        });

        Chatting_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                alert.setTitle("삭제 확인");
                alert.setMessage("채팅방에서 나가겠습니까?");
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            ClientSenderThread clientSenderThread = new ClientSenderThread("req_out"+ spliter + ((ChattingData)Chatting_Adapter.getItem(position)).getRoom_number() + spliter + UserInfo.getUserID());
                            clientSenderThread.start();
                            try {
                                clientSenderThread.join();
                                Chatting_Adapter.clear(position);
                                Chatting_Adapter.notifyDataSetChanged();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "네트워크 상태를 확인해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
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

        return view;
    }

    private void registerReceiver() {
        if (mReceiver != null) return;

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_MESSAGE);
        intentFilter.addAction(CREATE_MESSAGE);
        intentFilter.addAction(CHANGE_MESSAGE);
        intentFilter.addAction(DELETE_MESSAGE);

        this.mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(BROADCAST_MESSAGE)) {
                    String title = intent.getStringExtra("title");
                    String content = intent.getStringExtra("content");
                    String date = intent.getStringExtra("date");
                    Chatting_Adapter.modify(title, content, date);
                    Chatting_Adapter.datesort();
                    Chatting_Adapter.notifyDataSetChanged();

                } else if (intent.getAction().equals(CREATE_MESSAGE)) {
                    String room = intent.getStringExtra("room");
                    Chatting_Adapter.addItem(room + "번방", "", "잔액 : " + CardContent.list(room).get(0)[1] +"원", room, null);
                    Chatting_Adapter.notifyDataSetChanged();
                } else if (intent.getAction().equals(CHANGE_MESSAGE) || intent.getAction().equals(DELETE_MESSAGE)) {
                    String room = intent.getStringExtra("room");
                    String change = CardContent.list(room).get(0)[1];
                    Chatting_Adapter.modify(room, change);
                    Chatting_Adapter.notifyDataSetChanged();
                }
            }
        };

        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}