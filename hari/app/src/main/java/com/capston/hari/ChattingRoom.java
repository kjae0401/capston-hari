package com.capston.hari;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChattingRoom extends AppCompatActivity {
    private String msg;
    public static String roomnumber;
    ListView listView;

    String ID;

    ChattingListAdapter chattingListAdapter;
    private final String BROADCAST_MESSAGE = "com.example.broadcastreceiver";
    private final String CREATE_MESSAGE = "com.example.create";
    private BroadcastReceiver mReceiver = null;
    boolean state;
    String member;
    TextView drawerbtn, backbtn;
    EditText contents;
    Button Send_button;
    private final char spliter = 0x11;

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatting_room_page);
        drawerbtn = (TextView) findViewById(R.id.Button_chatting_room_page_menu);
        backbtn = (TextView) findViewById(R.id.Button_chatting_room_page_back);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        roomnumber = null; state = false; msg = null; roomnumber = null;
        Intent intent = getIntent();

        String chatterID = getIntent().getStringExtra("Name");

        ID = UserInfo.getUserID();

        chattingListAdapter = new ChattingListAdapter(ID);

        listView = (ListView)findViewById(R.id.ListView_chattingroom_contents);
        listView.setAdapter(chattingListAdapter);

        if (intent.getStringExtra("member") != null) {
            member = intent.getStringExtra("member");
            state = intent.getBooleanExtra("state", false);
        }

        else {
            roomnumber = intent.getStringExtra("number");
            ArrayList<String[]> chatcon = ChattingContent.list(roomnumber);

            for (int i = 0; i < chatcon.size(); i++) {
                //chattingListAdapter.setChatterID(chatcon.get(i)[0]);
                chattingListAdapter.addItem(chatcon.get(i)[0], chatcon.get(i)[1], chatcon.get(i)[2]);
            }
            chattingListAdapter.datesort();
            chattingListAdapter.notifyDataSetChanged();
            listView.setSelection(chattingListAdapter.getCount() - 1);
        }

        contents = (EditText)findViewById(R.id.EditText_chatting_room_page_contents);
        Send_button = (Button)findViewById(R.id.Button_chatting_room_page_send);
        Send_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ClientSenderThread sender;
                if (state == true) {
                    member = "req_create"+ spliter + member + spliter + contents.getText().toString();
                    contents.setText("");
                    sender = new ClientSenderThread(member);
                    sender.start();
                } else {    // 오른쪽으로 보내기?
                    msg = "req_say"+ spliter + UserInfo.getUserID() + spliter + roomnumber + spliter + contents.getText().toString();
                    contents.setText("");
                    sender = new ClientSenderThread(msg);
                    sender.start();

                    //Toast.makeText(getApplicationContext(), UserInfo.getUserID(), Toast.LENGTH_LONG).show();
                }
            }
        });

        drawerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (roomnumber.isEmpty()) {

                } else {
                    Intent drawer = new Intent(getApplicationContext(), DrawerPage.class);
                    drawer.putExtra("roomnumber", roomnumber);
                    startActivity(drawer);
                }
            }
        });

        contents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (contents.getText().toString().isEmpty()) {
                    Send_button.setEnabled(false);
                } else {
                    Send_button.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void registerReceiver() {
        if (mReceiver != null) return;

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_MESSAGE);
        intentFilter.addAction(CREATE_MESSAGE);


        this.mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(BROADCAST_MESSAGE)) {
                    String compare = roomnumber + "번방";
                    if (compare.equals(NotifyData.Title)) {
                        chattingListAdapter.addItem(NotifyData.User, NotifyData.contents, NotifyData.date);
                        chattingListAdapter.datesort();
                        chattingListAdapter.notifyDataSetChanged();
                        listView.setSelection(chattingListAdapter.getCount() - 1);
                    }
                } else if (intent.getAction().equals(CREATE_MESSAGE)) {
                    state = new Boolean(false);
                    roomnumber = intent.getStringExtra("room");
                    String[] content = {"0", "null"};
                    CardContent.addContent(roomnumber, content);
                }
            }
        };

        this.registerReceiver(this.mReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        if (mReceiver != null) {
            this.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}