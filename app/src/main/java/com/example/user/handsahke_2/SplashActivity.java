package com.example.user.handsahke_2;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

//로딩화면   -> remoteconfig 사용
public class SplashActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;   //remoteconfig

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //로딩 화면 스테이트 바 없애기 (맨위에 배터리 시간 이런거 표시되는 부분)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        linearLayout = (LinearLayout)findViewById(R.id.splashactivity_linearlayout);

        //파이어베이스 기본 세팅

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)      //BuildConfig 디버그 해주기 안해 주면 요청에 제한이 있음
                .build();                                         //캐시를 빈번하게 사용 가능
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.default_config); //xml 파일에서 인앱 기본값 설정

        mFirebaseRemoteConfig.fetch(0)  //시간 요청 0초로 설정했음
                /*/원격 구성서버로 부터 매개 변수값을 가져올때 fetch()함수 호출, 서버 연동은 앱에 영향을 주게 되므로 0초로 (기본은 12시간으로 되어잇음) 바꾸어 활성화 되지 않도(연동되지 않도록)한다.*/

                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {

                        }
                        displayMessage();
                    }
                });

    }

    void displayMessage() {
        String splash_background = mFirebaseRemoteConfig.getString("splash_background");
        boolean caps = mFirebaseRemoteConfig.getBoolean("splash_message_caps");
        String splash_message = mFirebaseRemoteConfig.getString("splash_message");

        linearLayout.setBackgroundColor(Color.parseColor(splash_background));

        if(caps){     //서버 점검중이라는 remote 컨트롤 해줄때
            AlertDialog.Builder builder = new AlertDialog.Builder(this);           //대화 상자를 팝업해주기 위해 AlertDialog.Builder 클래스 동적할당 생성
            builder.setMessage(splash_message).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            builder.create().show();              //create()로 알림창 객체 생성하고 show 보여주기

        }else {
            /* intent = new Intent(MainActivity.this, SecondActivity.class);
               startActivitiy(intent); */

            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }

    }
}
