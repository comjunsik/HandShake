package com.example.user.handsahke_2.model;

import android.provider.ContactsContract;

//푸시메시지를 위한 model
public class NotificationModel {

    public String to;

    public Notification notification = new Notification();
    public Data data =new Data();

    public static class Notification {        //gcm 백그라운드
        public String title;
        public String text;
    }
    public static class Data{       // 포그라운드
        public String title;
        public String text;

    }
}
