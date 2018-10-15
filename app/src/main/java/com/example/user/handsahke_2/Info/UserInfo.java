package com.example.user.handsahke_2.Info;

/**
 * 사용자 정보
 */
public class UserInfo {

    public UserInfo() {
    }

    //회원 아이디
    private String Id = "";

    //회원 이름
    private String Name = "";

    //회원 관심사
    private String Interest = "";

    //회원 매칭거부 유.무
    private String RefuseMatch = "";

    //관심사 매칭 갯수
    private int InterestCnt = 0;

    public void setId(String Id) {
        this.Id = Id;
    }
    public void setName(String name) {
        this.Name = name;
    }
    public void setInterest(String interest) {
        this.Interest = interest;
    }
    public void setRefuseMatch(String refuseMatch) {
        this.RefuseMatch = refuseMatch;
    }
    public void setInterestCnt(int interestcnt) {
        this.InterestCnt = interestcnt;
    }

    public String getId() {
        return Id;
    }
    public String getName() {
        return Name;
    }
    public String getInterest() {
        return Interest;
    }
    public String getRefuseMatch() {
        return RefuseMatch;
    }
    public int getInterestCnt() {
        return InterestCnt;
    }
}