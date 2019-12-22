package com.capston.hari;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChattingContent {
    static HashMap contentList = new HashMap();
    static int count = 0;

    static void addGroup(String groupName) {
        if (!contentList.containsKey(groupName)) {
            contentList.put(groupName, new HashMap());
        }
    }

    static void addContent(String groupName, String[] struct) {
        addGroup(groupName);
        HashMap group = (HashMap) contentList.get(groupName);
        group.put(count++, struct);
    }

    static ArrayList<String[]> list(String key) {
        ArrayList<String[]> con = new ArrayList<String[]>();
        Set set = contentList.entrySet();
        Iterator iter = set.iterator();

        while (iter.hasNext()) {
            Map.Entry e = (Map.Entry) iter.next();
            Set subSet = ((HashMap) e.getValue()).entrySet();
            Iterator subiter = subSet.iterator();

            if(e.getKey().equals(key)) {
                while (subiter.hasNext()) {
                    Map.Entry subE = (Map.Entry) subiter.next();
                    String[] name = (String[]) subE.getValue();
                    con.add(name);
                }
            }
        }
        return con;
    }
}
