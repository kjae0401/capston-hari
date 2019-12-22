package com.capston.hari;

import java.util.HashMap;

public class UserInfo {
    private static String userID;
    private static UserInfo userInfo = null;
    public static String currentRoom = null;

    private UserInfo(String userID) { this.userID = userID; }

    public static UserInfo getInstance(String userID) {
        if (userInfo == null) {
            userInfo = new UserInfo(userID);
        }
        return userInfo;
    }

    public static String getUserID() { return userID; }
}
