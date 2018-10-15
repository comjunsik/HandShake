package com.example.user.handsahke_2.model;

public class UserModel {

    public UserModel() {

    }

    public String userName= "";
    public String profileImageUrl= "";
    public String uid= "";              //채팅하고 싶은사람 uid 받아오기 위해
    public String pushToken= "";
    public String comment= "";
    public String interest = "";        //관심사
    public int interestCnt = 0;         //관심사 매칭 카운트
    public String blockedId = "";       //차단회원
    public String refuseMatch = "";     //매칭거부
    public String thumbList = "";       //좋아요 게시글 리스트
}
