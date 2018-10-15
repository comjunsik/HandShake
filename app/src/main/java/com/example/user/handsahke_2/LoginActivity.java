package com.example.user.handsahke_2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

//로그인 화면
public class LoginActivity extends AppCompatActivity {

    private EditText id;
    private EditText password;

    private Button login;
    private Button signup;
    private FirebaseRemoteConfig firebaseRemoteConfig; //원격으로 테마 받을려면 사용해야함. remoteconfig 사용
    private FirebaseAuth firebaseAuth;  //로그인 회원 정보
    private FirebaseAuth.AuthStateListener authStateListener; //로그인이 됬는지 안됬는지 체크    입력을하고 로그인버튼을 누르면 다음 화면으로 넘어가게 처리하기 위한 리스너

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //로그인,회원가입 버튼 테마 적용  (default_config.xml의 splash_background 색깔로 적용->파이어베이스 remoteconfig에서 원격 조작가능)
        firebaseRemoteConfig = firebaseRemoteConfig.getInstance();  //getInstance() Singleton 패턴으로 객체 관리한다(인스턴스 복사해서 사용하는 것이 아닌 동일한 인스턴스 사용)
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut(); //임의의 로그아웃 코드       -->앱껏을때 로그아웃 되게/ 이거 지우면 앱 꺼도 다시 켰을때 로그인 상태 유지

        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_color));        //remoteconfig의 테마 색상적용을 위해
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {    //Lollipop버전 부터 적용 가능하다.
            getWindow().setStatusBarColor(Color.parseColor(splash_background));   //getwindow()는 지정한 특정 window로 부터 관계된 window의 handle을 반환
        }                                                                         //parseColor -> 색상 변경 function
        id = (EditText)findViewById(R.id.loginActivitiy_edittext_id);
        password = (EditText)findViewById(R.id.loginActivitiy_edittext_password);

        login = (Button)findViewById(R.id.loginActivitiy_button_login);
        signup = (Button)findViewById(R.id.loginActivitiy_button_signup);
        login.setBackgroundColor(Color.parseColor(splash_background));
        signup.setBackgroundColor(Color.parseColor(splash_background));

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEvent();         //로그인 버튼 눌르면 loginEvent() 실행 ->로그인 되었는지 판단여부를 확인하는 메서드
            }
        });

        //signup(회원가입)버튼 누르면 SignupActivity 실행
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,SignupActivity.class));
                
            }
        });

        //로그인 인터페이스 리스너     ->로그인 되었는지 안되었는지 확인하여 다음 화면으로 넘어가는 리스너
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {  //상태가 바뀌었을때

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){   //로그인 됬을때

                    Storage.MyId = user.getUid();
                    //로그인
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class); //MainActivity로 전환
                    startActivity(intent);
                    finish();
                    
                }else{
                    //로그아웃
                }

            }
        };
    }

    void loginEvent(){
        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {  //로그인 완료되면 성공했는지 안됬는지 판단여부
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //로그인 성공하면 일로  로그인 판단여부만 확인

                if(!task.isSuccessful()){ //로그인 실패한 부분
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                 }
                
            }
        });
    }


    //로그인 되었는지 안됬는지 확인해주는 authSatateListener를 현재 엑티비티인 LoginActivity에 붙여주기
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
    //멈췄을때 때어주기
    @Override
    protected void onStop() {      //앱 끝냈을때 리스너 때어주기
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
