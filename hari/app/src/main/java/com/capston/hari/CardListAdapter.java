package com.capston.hari;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CardListAdapter extends BaseAdapter {
    private ArrayList<CardData> listViewChattingList = new ArrayList<CardData>();

    @Override // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    public int getCount() { return listViewChattingList.size(); }

    @Override // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cardlist, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView titleTextView = (TextView) convertView.findViewById(R.id.TextView_cardlist_room);
        TextView contentTextView = (TextView) convertView.findViewById(R.id.TextView_cardlist_use);
        TextView changeTextView = (TextView) convertView.findViewById(R.id.TextView_cardlist_money);
        TextView dateTextView = (TextView) convertView.findViewById(R.id.TextView_cardlist_date);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        CardData listViewItem = listViewChattingList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        titleTextView.setText(listViewItem.getRoom_number());
        contentTextView.setText(listViewItem.getUse());
        changeTextView.setText(listViewItem.getMoney());
        dateTextView.setText(listViewItem.getDate());

        return convertView;
    }

    @Override // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    public long getItemId(int position) {
        return position ;
    }

    @Override // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    public Object getItem(int position) {
        return listViewChattingList.get(position);
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String room, String content, String change, String date) {
        CardData item = new CardData();

        item.setRoom_number(room);
        item.setUse(content);
        item.setMoney(change);
        item.setDate(date);

        listViewChattingList.add(item);
    }

    public void clear(int position) {
        listViewChattingList.remove(position);
    }
    public int size() { return listViewChattingList.size(); }

    public void datesort() {
        Comparator<CardData> dateDesc = new Comparator<CardData>() {
            @Override
            public int compare(CardData itme1, CardData item2) {
                int ret;

                if (itme1.getDate().compareTo(item2.getDate()) > 0) {
                    ret = 1;
                } else if (itme1.getDate().compareTo(item2.getDate()) == 0) {
                    ret = 0;
                } else { ret = -1; }

                return ret;
            }
        };

        Collections.sort(listViewChattingList, dateDesc);
    }
}