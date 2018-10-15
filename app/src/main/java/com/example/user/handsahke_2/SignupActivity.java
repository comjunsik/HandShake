package com.example.user.handsahke_2;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.user.handsahke_2.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

//회원가입
public class SignupActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 10;    //프로필 사진 이벤트에서 request 코드값 10으로 임의로 정했음
    private EditText email;
    private EditText name;
    private EditText password;
    private Button signup;
    private String splash_background;
    private ImageView profile;  //프로필 이미지
    private Uri imageUri;   //업로드 할때 이미지를 담아줄 uri  ->이미지 경로 원본

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //스테이트바 테마 적용
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        splash_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {    //Lollipop버전 부터 적용 가능하다.
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }

        profile = (ImageView)findViewById(R.id.signupActivity_imageview_profile);
        //사진 클릭하면 앨범이 열리는 이벤트
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);  //ACTION_PICK == 이미지 가져오기
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent,PICK_FROM_ALBUM);    //request code 값을 받아와서 원하는 위치로 이동(switch 문의 case 처럼) 여기선 상수 PICK_FROM_ALBUM가 request code
            }                                                         //startActivity와 달리 시작한 액티비티를 통해 어떠한 결과값을 받기 위해 사용되는것
        });

        email = (EditText)findViewById(R.id.signupActivity_edittext_email);
        name = (EditText)findViewById(R.id.signupActivity_edittext_name);
        password = (EditText)findViewById(R.id.signupActivity_edittext_password);
        signup = (Button)findViewById(R.id.signupActivity_button_signup);
        signup.setBackgroundColor(Color.parseColor(splash_background));    //버튼에다가 테마 적용

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /*
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("message");

                myRef.setValue("Hello, World!");
                 */


                //회원가입시 입력 전부 안했을때
                if(email.getText().toString()==null || name.getText().toString()==null || password.getText().toString()==null || imageUri == null){
                    Toast.makeText(SignupActivity.this, "모든 항목을 채워주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                //파이버베이스 회원가입
                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())   //email과 password를 통한 신규 계정 생성 function
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {     //addComploteListener() 로 성공유무의 값 확인
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {                  //회원가입 성공하면 onComplete로 넘어옴

                                final String uid = task.getResult().getUser().getUid();   //task에 담겨 있는 Auth정보 중 uid 가져오기
                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name.getText().toString()).build();  //회원가입 할때 자신 이름 넣는 코드
                                task.getResult().getUser().updateProfile(userProfileChangeRequest);       //회원가입 할때 이름 담김.

                                //이미지 저장해서 데이터베이스에 넘겨주기
                                FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        //파일이 저장된 경로를 다시 보내줌
                                        String imageUrl = task.getResult().getDownloadUrl().toString();    //이미지 Url 받아오기

                                        UserModel userModel = new UserModel();     //생성한 UserModel 객체 동적할당해서 접근
                                        userModel.userName = name.getText().toString();
                                        userModel.profileImageUrl = imageUrl;
                                        userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        userModel.interest = "";         //회원가입시 관심사 초기화
                                        userModel.blockedId = "";//회원가입시 대화상대 차단 초기화
                                        userModel.refuseMatch = "F";    //회원가입시 매칭거부 FALSE

                                        //데이터베이스에 이름& 프로필 이미지 & uid DB에 저장                                                 //userModel 객체의 값들 set
                                        FirebaseDatabase.getInstance().getReference().child(Define.FB_USERS).child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) { //DB에 정보 저장하고 창 닫아 주기위해 addOnSuccessListener()
                                                SignupActivity.this.finish();  //회원가입하고 창 닫기
                                            }                  
                                        });
                                    }
                                });
                            }
                        });        
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {    //startActivityForResult로 실행된 Activity로부터 결과를 받아오기 위해서는 이 함수 필요
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK){    //requestCode 값이 맞는지 && resultCode로 결과가 제대로 넘어왔는지 확인
            profile.setImageURI(data.getData());   //profile 에 이미지 세팅 (이미지 바꿔주기 setImageURI)   ->회원가입시 뜨는 프로필 이미지 뷰를 바꿔주는것
            imageUri = data.getData();              //이미지 경로 원본 -> 이미지 원본을 저장하고 있다고 보면 됨
        }
    }
}
