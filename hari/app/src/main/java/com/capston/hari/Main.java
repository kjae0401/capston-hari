package com.capston.hari;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Main extends AppCompatActivity {
    private BackPressCloseHandler backPressCloseHandler;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private Chatting menu1Fragment = new Chatting();
    private List menu2Fragment = new List();
    private Etc menu3Fragment = new Etc();
    Intent foregroundServiceIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        backPressCloseHandler = new BackPressCloseHandler(this);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navi);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, menu1Fragment).commitAllowingStateLoss();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (menuItem.getItemId()) {
                    case R.id.chat:
                        transaction.replace(R.id.frame_layout, menu1Fragment).commitAllowingStateLoss();
                        break;

                    case R.id.list:
                        transaction.replace(R.id.frame_layout, menu2Fragment).commitAllowingStateLoss();
                        break;

                    case R.id.etc:
                        transaction.replace(R.id.frame_layout, menu3Fragment).commitAllowingStateLoss();
                        break;
                }
                return true;
            }
        });

        if (null == ClientSocketService.service) {
            foregroundServiceIntent = new Intent(this, ClientSocketService.class);
            startService(foregroundServiceIntent);
        } else {
            foregroundServiceIntent = ClientSocketService.service;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(foregroundServiceIntent != null) {
            stopService(foregroundServiceIntent);
            foregroundServiceIntent = null;
        }
    }

    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }
}