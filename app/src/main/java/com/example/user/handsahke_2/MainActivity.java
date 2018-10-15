package com.example.user.handsahke_2;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.user.handsahke_2.fragment.AccountFragment;
import com.example.user.handsahke_2.fragment.BoardFragment;
import com.example.user.handsahke_2.fragment.ChatFragment;
import com.example.user.handsahke_2.fragment.PeopleFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;


//로그인 엑티비티
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView =(BottomNavigationView) findViewById(R.id.mainactivity_bottomnavigationview);    //activity_main의 밑에 네비게이션 바 id
        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new AccountFragment()).commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override        //네비게이션 아티엠 셀렉리스너
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_board:
                        /*activity에서 프래그먼트를 다루기 위해서는 FragmentMangaer을 이용해야한다.
                          FragementManager의 객체를 얻으려면 getFragmentManager()을 호출
                          액티비티에 적용한 각각의 변경 셋트를 Transaction이라 부른데, 이를 위해 beginTransaction()사용
                          replace()를 통해 동적으로 Fragment 교체
                          마지막에 commit() 써서 적용
                         */
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new BoardFragment()).commit();
                             //activity_main.xml의 친구창 보여주는 framelayout id
                        return true;
                    case R.id.action_chat:
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new ChatFragment()).commit();
                        return true;

                    case R.id.action_account:
                        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new AccountFragment()).commit();
                        return true;
                        
                }


                return false;
            }
        });
        passPushTokenToServer();

    }
    //푸시 메시지 토큰 생성 메서드 , 서버 db에 넣기
    void passPushTokenToServer(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();  //토큰 생성
        Map<String,Object> map = new HashMap<>();//푸시 토큰 넣어주는 방법은 firebase상에서 hashMap 밖에 없음
        map.put("pushToken", token);
        FirebaseDatabase.getInstance().getReference().child(Define.FB_USERS).child(uid).updateChildren(map);  //update말고 setValue 하면 기존 데이터 지워지고 덮어쓰기가 됨

    }
}
