package com.example.user.handsahke_2;

public class Define {

    /**
     * Firebase 회원정보
     */
    public static final String FB_USERS = "users";
    public static final String FB_USER_NAME = "userName";
    public static final String FB_PROFILE_IMGURL = "profileImageUrl";
    public static final String FB_UID = "uid";    //채팅하고 싶은사람 uid 받아오기 위해
    public static final String FB_PUSH_TOKEN = "pushToken";
    public static final String FB_COMMENT = "comment";
    public static final String FB_INTEREST = "interest";       //관심사
    public static final String FB_BLOCKED_ID ="blockedId";       //차단회원
    public static final String FB_REFUSE_MATCH = "refuseMatch";    //매칭거부

    /**
     * Firebase 채팅정보
     */
    public static final String FB_CHAT_INFO         = "CHAT_INFO";
    public static final String FB_CHAT_FROM_ID      = "FROM_ID";           //메시지 보내는 회원의 아이디
    public static final String FB_CHAT_FROM_NAME    = "FROM_NAME";         //메시지 보내는 회원의 이름
    public static final String FB_CHAT_TO_ID        = "TO_ID";             //메시지 받는 회원의 아이디
    public static final String FB_CHAT_TO_NAME      = "TO_NAME";           //메시지 받는 회원의 이름
    public static final String FB_CHAT_MSG          = "CHAT_MSG";

    /**
     * 인텐트 정보
     */
    public static final String INTENT_FROM_ID       = "INTENT_FROM_ID";     //메시지 보내는 회원의 아이디
    public static final String INTENT_FROM_NAME     = "INTENT_FROM_NAME";   //메시지 보내는 회원의 이름
    public static final String INTENT_TO_ID         = "INTENT_TO_ID";       //메시지 받는 회원의 아이디
    public static final String INTENT_TO_NAME       = "INTENT_TO_NAME";     //메시지 받는 회원의 이름

    /**
     * 관심사 정보
     */
    public static final String INTEREST_SPORTS_SOCCER = "축구";
    public static final String INTEREST_SPORTS_BASKETBALL = "농구";
    public static final String INTEREST_SPORTS_SWIMMING = "수영";
    public static final String INTEREST_GAME_LOL = "롤";
    public static final String INTEREST_GAME_BATTLE = "배틀그라운드";
    public static final String INTEREST_GAME_OVERWATCH = "오버워치";
    public static final String INTEREST_MOVIE_ACTION = "액션";
    public static final String INTEREST_MOVIE_HORROR = "호러";
    public static final String INTEREST_MOVIE_FANTASY = "판타지";
    public static final String INTEREST_BOOK_REASONING = "추리";
    public static final String INTEREST_BOOK_HORROR = "공포";
    public static final String INTEREST_BOOK_HISTORY = "역사";
}
