package com.example.user.handsahke_2.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.user.handsahke_2.Define;
import com.example.user.handsahke_2.InterestActivity;
import com.example.user.handsahke_2.R;
import com.example.user.handsahke_2.Storage;
import com.example.user.handsahke_2.chat.MessageActivity;
import com.example.user.handsahke_2.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//현재 나의상태메세지
public class AccountFragment extends Fragment {

    private ImageButton button_randomchat;  //랜챗 버튼
    private ImageButton button_interest;    //관심사 버튼
    private Switch switch_RefuseMatch;


    private FirebaseDatabase mDatabase = null;
    private DatabaseReference mRefMemberInfo;
    private DataSnapshot mSnapMemberInfo = null;

    //private ProgressDialog mProgressDlg = null;

    /**
     * 나의 관심사 리스트
     */
    private ArrayList<String> myInterestArray = new ArrayList<>();

    /**
     * 모든 회원의 리스트
     */
    private ArrayList<UserModel> mMemberArrayList = new ArrayList<>();

    /**
     * 나와 관심사가 겹치는 회원 리스트
     */
    private ArrayList<UserModel> mMatchedArrayList = new ArrayList<>();
    private String strInterest = "";

    private ArrayList<String> interestArray = new ArrayList<>();

    //private String my_uid; //자신 uid

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_account,container,false);
        final UserModel userModel = new UserModel();
        switch_RefuseMatch = (Switch)view.findViewById(R.id.accountitemitem_switch_refuse_match);

        if(Storage.MyRefuseMatch.equals("F")) {
            switch_RefuseMatch.setChecked(false);
        }
        else
            switch_RefuseMatch.setChecked(true);
        switch_RefuseMatch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(Storage.MyRefuseMatch.equals("F"))
                {
                    Storage.MyRefuseMatch="T";
                    userModel.refuseMatch="T";
                    mRefMemberInfo=mDatabase.getReference();
                    mRefMemberInfo.child(Define.FB_USERS).child(Storage.MyId).child(Define.FB_REFUSE_MATCH).setValue("T");

                }
                else
                {
                    Storage.MyRefuseMatch="F";
                    userModel.refuseMatch="F";
                    mRefMemberInfo=mDatabase.getReference();
                    mRefMemberInfo.child(Define.FB_USERS).child(Storage.MyId).child(Define.FB_REFUSE_MATCH).setValue("F");


                }
            }
        });

        button_interest = (ImageButton) view.findViewById(R.id.accountitem_button_interest);
        button_interest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), InterestActivity.class);
                startActivity(intent);
            }
        });

        //랜덤 채팅 버튼
        button_randomchat = (ImageButton) view.findViewById(R.id.accountitem_button_randomchat);
        button_randomchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(v.getContext(), MessageActivity.class);  //view.getContext() 사용자가 누른 해당 item
                //intent.putExtra("destinationUid",userModels.get(position).uid);        //상대방 uid 담아주는 부분   ->intent를 통해 MessagaActivity에 putExtra로 destinationUid라는 이름으로 uid 전달-> MessagaActivity에선 Intent intent= getIntetn();로 데이터 받을 수 있음
                getMatchedMember();
            }
        });


        //my_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();     //현재 로그인한 uid


        //DB에 쓰기
        mDatabase=FirebaseDatabase.getInstance();
        mRefMemberInfo = mDatabase.getReference(Define.FB_USERS);   //DB users부분 접근

        //DB 읽기
        mRefMemberInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mSnapMemberInfo = dataSnapshot;        //데이터 저장
                for (DataSnapshot child : mSnapMemberInfo.getChildren()) {

                    //이 부분 어떻게 수정해야할지 도저히 모르겟네요ㅠㅠ
                    UserModel userModel = child.getValue(UserModel.class);

                    String FbUserId = userModel.uid;
                    String FbInterest = userModel.interest;
                    String FbBlockedId= userModel.blockedId;
                    String FbRefuseMatch = userModel.refuseMatch;

                    /**
                     * 자신의 아이디와 일치할경우 DB정보 가져온다.
                     */
                    if(FbUserId.equals(Storage.MyId))
                    {
                        Storage.MyInterest = FbInterest;
                        Storage.MyBlockedId = FbBlockedId;
                        Storage.MyRefuseMatch = FbRefuseMatch;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w("AccountFragment", "Failed to read value.", databaseError.toException());
            }
        });


        return view;

    }

    //매칭함수
    void  getMatchedMember()
    {
        mMemberArrayList = new ArrayList<>(); //모든 회원 리스트
        mMatchedArrayList = new ArrayList<>(); //나와 괌심사가 겹치는 회원 리스트
        myInterestArray = new ArrayList<>();  //나의 관심사 리스트

        for (DataSnapshot child : mSnapMemberInfo.getChildren()) {

                //이 부분 어떻게 수정해야할지 도저히 모르겟네요ㅠㅠ
                UserModel userModel = child.getValue(UserModel.class);

                String FbUserId = userModel.uid;
                String FbUserName = userModel.userName;
                String FbInterest = userModel.interest;
                String FbRefuseMatch = userModel.refuseMatch;

                /**
                 * 내 아이디와 동일할 경우 나의 관심사를 저장한다.
                 */
                if (FbUserId.equals(Storage.MyId)) {

                    String Interest = FbInterest;
                    String[] split = Interest.split("#");
                    if (split != null) {
                        for (int i = 0; i < split.length; i++) {
                            if (split[i].equals("") == false)
                                myInterestArray.add(split[i]);
                        }
                    }
                }
                /**
                 * 나를 제외한 모든 회원의 정보를 저장한다.
                 */
                else {
                    UserModel item = new UserModel();
                    item.uid = FbUserId;
                    item.userName = FbUserName;
                    item.interest = FbInterest;
                    item.refuseMatch = FbRefuseMatch;

                    mMemberArrayList.add(item);
                }
        }

        /**
         * 나를 제외한 모든 회원의 정보를 검색한다.
         */
        for (UserModel item : mMemberArrayList){

            if(item.refuseMatch.equals("T"))
                continue;

            //회원의 관심사 정보를 가져온다.
            String interest = item.interest;
            String[] memberInterest = interest.split("#");
            int matchedCnt = 0;

            if (memberInterest != null){
                for (int i =0; i<memberInterest.length; i++)
                {
                    for (String myinterest : myInterestArray)
                    {
                        //나의 관심사와 회원의 관심사가 일치하는게 존재할 경우
                        if (myinterest.equals(memberInterest[i]))
                        {
                            matchedCnt++;
                            item.interestCnt = matchedCnt;
                            if (mMatchedArrayList.contains(item)==false)
                                mMatchedArrayList.add(item);
                        }

                    }

                }
            }

        }
        /////////////////////////////////////////////////////////////
        int MatchedMax =0;
        UserModel MatchedMember = null;  //매칭 멤버

        //관심사가 1개이상 겹치는 회원리스트를 검색
        for (UserModel item: mMatchedArrayList){
            //나의 대화상대 차단 멤버일 경우
            if (Storage.MyBlockedId.contains(item.uid))
                continue;

            //관심사가 가장 많이 겹치는 회원의 정보를 가져온다.
            if (MatchedMax==0){    //관심사가 아예 안겹칠 경우
                MatchedMax = item.interestCnt;
                MatchedMember = item;
            }
            else
            {
                if (item.interestCnt > MatchedMax)
                    MatchedMember=item;
            }
        }

        if (MatchedMember==null)
        {
            Activity root = getActivity();
            Toast.makeText(root,"매칭된 회원이 없습니다.",Toast.LENGTH_SHORT).show();
            //mProgressDlg.dismiss();
            return;
        }
        //mProgressDlg.dismiss();

        Activity root = getActivity();
        Intent intent = new Intent(root,MessageActivity.class);
        intent.putExtra("destinationUid",MatchedMember.uid);

        startActivity(intent);

    }

    void showDialog(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_comment, null);
        final EditText editText = view.findViewById(R.id.commentDialog_edittext);
        builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //상태메세지 db에 업데이트
                Map<String,Object> stringObjectMap = new HashMap<>();
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                stringObjectMap.put("comment", editText.getText().toString());
                FirebaseDatabase.getInstance().getReference().child(Define.FB_USERS).child(uid).updateChildren(stringObjectMap);
                
            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
        
    }

    /*@Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.accountfragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new AccountFragmentRecyclerViewAdapter());


        return view;
    }

    class AccountFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public  AccountFragmentRecyclerViewAdapter(){

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account,parent,false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((CustomViewHolder)holder).statebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(v.getContext());
                }
            });
            ((CustomViewHolder)holder).interestbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), InterestActivity.class);
                    startActivity(intent);
                }
            });

        }

        private void showDialog(Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.dialog_comment, null);
            final EditText editText = view.findViewById(R.id.commentDialog_edittext);
            builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //상태메세지 db에 업데이트
                    Map<String,Object> stringObjectMap = new HashMap<>();
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    stringObjectMap.put("comment", editText.getText().toString());
                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(stringObjectMap);

                }
            }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.show();
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public Button statebutton;
            public Button interestbutton;


            public CustomViewHolder(View view) {
                super(view);
                statebutton = (Button) view.findViewById(R.id.accountitem_button_comment);
                interestbutton = (Button) view.findViewById(R.id.accountitem_interrest_button);
            }
        }
    }            */
}
