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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class List extends Fragment {
    ListView Friends_ListView;
    FriendsListAdapter Friends_Adapter;
    String string;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.list_fragment, container,false);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, new IntentFilter("Friendadd"));

        Friends_Adapter = new FriendsListAdapter();
        Friends_ListView = (ListView)view.findViewById(R.id.ListView_friends_list);
        Friends_ListView.setAdapter(Friends_Adapter);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String str;
                    URL url = new URL("http://113.198.237.95:80/jspServer/findfriends.jsp");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestMethod("POST");
                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                    osw.write("sql=select FRIENDID from FRIENDS where MYID='" + UserInfo.getUserID() + "'");
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
                                Friends_Adapter.addItem(string);
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
        Friends_Adapter.notifyDataSetChanged();

        Button friends_add = (Button) view.findViewById(R.id.Button_Add_Friend);
        friends_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addintent = new Intent(getContext(), FriendAdd.class);
                startActivity(addintent);
            }
        });

        Friends_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long i) {
                AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                alert.setTitle("삭제 확인");
                alert.setMessage("삭제하시겠습니까?");
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    String str;
                                    URL url = new URL("http://113.198.237.95:80/jspServer/member.jsp");
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                    conn.setRequestMethod("POST");
                                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                                    osw.write("sql=delete from friends where myid='" + UserInfo.getUserID() + "' and friendid='" + ((FriendsData)Friends_Adapter.getItem(position)).getName() + "'");
                                    osw.flush();
                                    if(conn.getResponseCode() == conn.HTTP_OK) {
                                        InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                                        BufferedReader reader = new BufferedReader(tmp);
                                        StringBuffer buffer;
                                        while ((str = reader.readLine()) != null) {
                                            buffer = new StringBuffer();
                                            buffer.append(str);
                                            string = buffer.toString().trim();
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
                        Friends_Adapter.clear(position);
                        Friends_Adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
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

                return false;
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("add");

            Friends_Adapter.addItem(data);
            Friends_Adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "친구 추가 완료.", Toast.LENGTH_LONG).show();
        }
    };
}