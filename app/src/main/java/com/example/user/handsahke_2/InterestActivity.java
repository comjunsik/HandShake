package com.example.user.handsahke_2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.user.handsahke_2.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class InterestActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    public static String TAG = "InterestActivity";

    private CheckBox chk1, chk2, chk3, chk4, chk5, chk6, chk7, chk8, chk9, chk10, chk11, chk12;
    private Button btnSetInterest;         //관심 설정 버튼

    /**
     * FiraBase DB
     */
    private FirebaseDatabase mDatabase = null;
    private DatabaseReference mRefMemberInfo;
    private DataSnapshot mSnapMemberInfo = null;
    private ValueEventListener valueEventListener;
    private ArrayList<String> interestArray = new ArrayList<>();

    /**
     * 나의 관심사 셋팅 유.무 판별
     */
    private boolean bSetInterest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest);

        chk1 = (CheckBox)findViewById(R.id.interestActivity_chk_soccer);
        chk1.setOnCheckedChangeListener(this);
        chk1.setChecked(false);

        chk2 = findViewById(R.id.interestActivity_chk_basketball);
        chk2.setOnCheckedChangeListener(this);
        chk2.setChecked(false);

        chk3 = findViewById(R.id.interestActivity_chk_swim);
        chk3.setOnCheckedChangeListener(this);
        chk3.setChecked(false);

        chk4 = findViewById(R.id.interestActivity_chk_roll);
        chk4.setOnCheckedChangeListener(this);
        chk4.setChecked(false);

        chk5 = findViewById(R.id.interestActivity_chk_battleground);
        chk5.setOnCheckedChangeListener(this);
        chk5.setChecked(false);

        chk6 = findViewById(R.id.interestActivity_chk_overwatch);
        chk6.setOnCheckedChangeListener(this);
        chk6.setChecked(false);

        chk7 = findViewById(R.id.interestActivity_chk_action);
        chk7.setOnCheckedChangeListener(this);
        chk7.setChecked(false);

        chk8 = findViewById(R.id.interestActivity_chk_horror);
        chk8.setOnCheckedChangeListener(this);
        chk8.setChecked(false);

        chk9 = findViewById(R.id.interestActivity_chk_fantasy);
        chk9.setOnCheckedChangeListener(this);
        chk9.setChecked(false);

        chk10 = findViewById(R.id.interestActivity_chk_detective);
        chk10.setOnCheckedChangeListener(this);
        chk10.setChecked(false);

        chk11 = findViewById(R.id.interestActivity_chk_horrorbook);
        chk11.setOnCheckedChangeListener(this);
        chk11.setChecked(false);

        chk12 = findViewById(R.id.interestActivity_chk_historybook);
        chk12.setOnCheckedChangeListener(this);
        chk12.setChecked(false);

        btnSetInterest = findViewById(R.id.btn_set_interest);         //관심설정 버튼
        btnSetInterest.setOnClickListener(this);

        // Write a message to the database
        mDatabase = FirebaseDatabase.getInstance();
        mRefMemberInfo = mDatabase.getReference();

        // Read from the database
        valueEventListener = mRefMemberInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                mSnapMemberInfo = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        String strInterest = Storage.MyInterest;
        String [] splitInterest = strInterest.split("#");
        if(splitInterest != null)
        {
            for(int i=0; i<splitInterest.length; i++)
            {
                if(splitInterest[i].equals("") == false)
                {
                    if(splitInterest[i].equals(Define.INTEREST_SPORTS_SOCCER)) {
                        chk1.setChecked(true);
                    }
                    else if(splitInterest[i].equals(Define.INTEREST_SPORTS_BASKETBALL)) {
                        chk2.setChecked(true);
                    }
                    else if(splitInterest[i].equals(Define.INTEREST_SPORTS_SWIMMING)) {
                        chk3.setChecked(true);
                    }
                    else if(splitInterest[i].equals(Define.INTEREST_GAME_LOL)) {
                        chk4.setChecked(true);
                    }
                    else if(splitInterest[i].equals(Define.INTEREST_GAME_BATTLE)) {
                        chk5.setChecked(true);
                    }
                    else if(splitInterest[i].equals(Define.INTEREST_GAME_OVERWATCH)) {
                        chk6.setChecked(true);
                    }
                    else if(splitInterest[i].equals(Define.INTEREST_MOVIE_ACTION)) {
                        chk7.setChecked(true);
                    }
                    else if(splitInterest[i].equals(Define.INTEREST_MOVIE_HORROR)) {
                        chk8.setChecked(true);
                    }
                    else if(splitInterest[i].equals(Define.INTEREST_MOVIE_FANTASY)) {
                        chk9.setChecked(true);
                    }
                    else if(splitInterest[i].equals(Define.INTEREST_BOOK_REASONING)) {
                        chk10.setChecked(true);
                    }
                    else if(splitInterest[i].equals(Define.INTEREST_BOOK_HORROR)) {
                        chk11.setChecked(true);
                    }
                    else if(splitInterest[i].equals(Define.INTEREST_BOOK_HISTORY)) {
                        chk12.setChecked(true);
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        mRefMemberInfo.removeEventListener(valueEventListener);
    }

    @Override
    public void onClick(View view) {

        Intent intent = null;
        boolean bChecked = false;

        switch (view.getId())
        {
            case R.id.btn_set_interest:
                if(mSnapMemberInfo == null)
                {
                    Toast.makeText(this, "회원정보 검색중 입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String strInterest = "";

                for(String item : interestArray)
                    strInterest += "#" + item;

                Storage.MyInterest = strInterest;

                /**
                 * 관심사 설정이 1개이상 되어있지 않을 경우
                 */
                if(chk1.isChecked() == false && chk2.isChecked() == false && chk3.isChecked() == false && chk4.isChecked() == false && chk5.isChecked() == false && chk6.isChecked() == false &&
                        chk7.isChecked() == false && chk8.isChecked() == false && chk9.isChecked() == false && chk10.isChecked() == false && chk11.isChecked() == false && chk12.isChecked() == false)
                {
                    Toast.makeText(this, "관심사를 1개이상 선택해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //final String uid; //자신 uid
                //uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                //final UserModel userModel = new UserModel();
                //userModel.interest = strInterest;

                mRefMemberInfo.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mRefMemberInfo.child(Define.FB_USERS).child(Storage.MyId).child(Define.FB_INTEREST).setValue(Storage.MyInterest);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel)
                Toast.makeText(this, "관심사 설정 완료", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        CheckBox chkBtn = (CheckBox) compoundButton;
        String strInterest = chkBtn.getText().toString();

        switch (chkBtn.getId())
        {
            case R.id.interestActivity_chk_soccer:
                if(b == true) {
                    if(interestArray.contains(strInterest) == false)
                        interestArray.add(strInterest);
                }
                else
                    interestArray.remove(strInterest);
                break;
            case R.id.interestActivity_chk_basketball:
                if(b == true) {
                    if(interestArray.contains(strInterest) == false)
                        interestArray.add(strInterest);
                }
                else
                    interestArray.remove(strInterest);
                break;
            case R.id.interestActivity_chk_swim:
                if(b == true) {
                    if (interestArray.contains(strInterest) == false)
                        interestArray.add(strInterest);
                }
                else
                    interestArray.remove(strInterest);
                break;
            case R.id.interestActivity_chk_roll:
                if(b == true) {
                    if (interestArray.contains(strInterest) == false)
                        interestArray.add(strInterest);
                }
                else
                    interestArray.remove(strInterest);
                break;
            case R.id.interestActivity_chk_battleground:
                if(b == true) {
                    if (interestArray.contains(strInterest) == false)
                        interestArray.add(strInterest);
                }
                else
                    interestArray.remove(strInterest);
                break;
            case R.id.interestActivity_chk_overwatch:
                if(b == true) {
                    if (interestArray.contains(strInterest) == false)
                        interestArray.add(strInterest);
                }
                else
                    interestArray.remove(strInterest);
                break;
            case R.id.interestActivity_chk_action:
                if(b == true) {
                    if (interestArray.contains(strInterest) == false)
                        interestArray.add(strInterest);
                }
                else
                    interestArray.remove(strInterest);
                break;
            case R.id.interestActivity_chk_horror:
                if(b == true) {
                    if (interestArray.contains(strInterest) == false)
                        interestArray.add(strInterest);
                }
                else
                    interestArray.remove(strInterest);
                break;
            case R.id.interestActivity_chk_fantasy:
                if(b == true) {
                    if (interestArray.contains(strInterest) == false)
                        interestArray.add(strInterest);
                }
                else
                    interestArray.remove(strInterest);
                break;
            case R.id.interestActivity_chk_detective:
                if(b == true) {
                    if (interestArray.contains(strInterest) == false)
                        interestArray.add(strInterest);
                }
                else
                    interestArray.remove(strInterest);
                break;
            case R.id.interestActivity_chk_horrorbook:
                if(b == true) {
                    if (interestArray.contains(strInterest) == false)
                        interestArray.add(strInterest);
                }
                else
                    interestArray.remove(strInterest);
                break;
            case R.id.interestActivity_chk_historybook:
                if(b == true) {
                    if (interestArray.contains(strInterest) == false)
                        interestArray.add(strInterest);
                }
                else
                    interestArray.remove(strInterest);
                break;
        }
    }
}
