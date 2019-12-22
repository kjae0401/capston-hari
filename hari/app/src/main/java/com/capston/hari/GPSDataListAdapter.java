package com.capston.hari;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.capston.hari.R;

import java.util.ArrayList;

public class GPSDataListAdapter extends BaseAdapter {
    private ArrayList<FindGPSData> listViewgpsList = new ArrayList<FindGPSData>();

    @Override // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    public int getCount() { return listViewgpsList.size(); }

    @Override // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.gpslist, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView nameTextView = (TextView) convertView.findViewById(R.id.TextView_gpslist_name);
        TextView addressTextView = (TextView) convertView.findViewById(R.id.TextView_gpslist_address);
        TextView phoneTextView = (TextView) convertView.findViewById(R.id.TextView_gpslist_phone);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        FindGPSData listViewItem = listViewgpsList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        nameTextView.setText(listViewItem.getPlace_name());
        addressTextView.setText(listViewItem.getRoad_address_name());
        phoneTextView.setText(listViewItem.getPhone());

        return convertView;
    }

    @Override // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    public long getItemId(int position) {
        return position ;
    }

    @Override // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    public Object getItem(int position) {
        return listViewgpsList.get(position);
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String name, String address, String phone, double x, double y) {
        FindGPSData item = new FindGPSData();

        item.setPlace_name(name);
        item.setRoad_address_name(address);
        item.setPhone(phone);
        item.setX(x);
        item.setY(y);

        listViewgpsList.add(item);
    }

    public void clear() {
        int count = listViewgpsList.size();
        for(int i=0; i<count; i++) {
            listViewgpsList.remove(0);
        }
    }
}