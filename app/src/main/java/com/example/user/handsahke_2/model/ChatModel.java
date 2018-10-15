package com.example.user.handsahke_2.model;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {



    //내 uid랑 상대방 uid 둘다 가지고 있는 변수
    public Map<String,Boolean> users = new HashMap<>();  //채팅방의 유저들
    public Map<String,Comment> comments = new HashMap<>(); //채팅방의 내용


    public static class Comment{
        public String uid;
        public String message;
        public Object timestamp; //메세지 전송시간
        public Map<String,Object> readUsers = new HashMap<>();  //메시지 읽음 표시
    }
}
