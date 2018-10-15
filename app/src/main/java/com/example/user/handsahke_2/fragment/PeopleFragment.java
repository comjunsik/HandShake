package com.example.user.handsahke_2.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.handsahke_2.Define;
import com.example.user.handsahke_2.R;
import com.example.user.handsahke_2.chat.MessageActivity;
import com.example.user.handsahke_2.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


//채팅창이 만들어지는 부분
public class PeopleFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //fragment_people.xml 띄워주기
        View view = inflater.inflate(R.layout.fragment_people, container, false);      //inflate를 사용하면 xml에 씌여져 있는 view 의 정의를 실제 view 객체로 만드는 역할을 함
        /*View inflate(int resource, ViewFroup root, boolean attach ToRoot)
          xml 레이아웃을 나누어 정의 한 후 하나의 레이아웃에 동적으로 붙이고자 할 때 유용하게 사용 */
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.peoplefragment_recyclerview);     //fragment_people.xml의 recyclelerview id
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());
        //RecylerView는 ListView의 더우 향상되고 유연해전 버전, 한정된 수의 뷰를 유지함으로서 매우 효율적으로 스크롤할 수 있는 큰데이터 집합을 표시하귀 위한 컨데이너


        return view;    //화면에 띄워주기
    }


    //recyclerView 적용해줄 class
    class  PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModels;      //친구 목록에 쌓이는 arraylist
        //DB 접속 constructer추가해줘야함
       public PeopleFragmentRecyclerViewAdapter(){
            userModels = new ArrayList<>();
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //내 아이디 친구목록 리스트에서 빼기위해
            //DB 검색
            FirebaseDatabase.getInstance().getReference().child(Define.FB_USERS).addValueEventListener(new ValueEventListener() {
                @Override
                //서버에서 넘어온 데이터
                public void onDataChange(DataSnapshot dataSnapshot) {   //파라미터로 데이터 넘어옴
                    userModels.clear();    //새로 친구 추가할때 기존에 있는 데이터 없애주고 새로 추가된 데이터까지 함께 표시하기 위해
                   //친구 목록에 쌓이는 arraylist
                    for (DataSnapshot snapshot :dataSnapshot.getChildren()){  //getChildren()을 사용하여 Iterable를 가져옴  /Iterator란? 자바의 컬렉션 프레임웍에서 컬렉션에 저장되어 있는 요소들을 읽어노는 방법중 하나

                        UserModel userModel = snapshot.getValue(UserModel.class);  //데이터 쌓임

                        if(userModel.uid.equals(myUid)){
                            continue;    //내 uid는 list에 안담고 그냥 넘어가기
                        }
                        userModels.add(userModel);  //data 쌓기
                    }
                    notifyDataSetChanged(); //데이터 쌓이고 나서 새로고침 하는 애

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }


        //item view
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //아이템들 넣기
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend,parent,false);

            return new CustomViewHolder(view);  //아이템 넣기 위해 CustomViewHolder() 호출
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {    //여기서 position은 해당 아이템
            //이미지 넣기
            //implementation 'com.github.bumptech.glide:glide:4.7.1' 해줘서 이미지 넣기위해 Glide 사용
            Glide.with(holder.itemView.getContext())
                    .load(userModels.get(position).profileImageUrl)   //이미지 주소
                    .apply(new RequestOptions().circleCrop())       //어떻게 이미지를 넣을건지
                    .into(((CustomViewHolder)holder).imageView);    //into해서 이미지view 넣기
             //텍스트적용
            ((CustomViewHolder)holder).textView.setText(userModels.get(position).userName);

            //RecyclerView의 아이템을 클릭했을 때 무언가를 하고 싶으면 itemView에다 clickListener를 달아야 한다.
            //사용자 칸 클릭했을때 MessageActivity layout으로 전환
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), MessageActivity.class);  //view.getContext() 사용자가 누른 해당 item
                    intent.putExtra("destinationUid",userModels.get(position).uid);        //상대방 uid 담아주는 부분   ->intent를 통해 MessagaActivity에 putExtra로 destinationUid라는 이름으로 uid 전달-> MessagaActivity에선 Intent intent= getIntetn();로 데이터 받을 수 있음

                    //애니메이션효과
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(),R.anim.fromright,R.anim.toleft);
                    startActivity(intent,activityOptions.toBundle()); //activityoptions.toBundle() ->애니메이션 효과 옵션 파라미터로 넘겨주기
                    
                }
            });
            if(userModels.get(position).comment != null) {                                                                                                                                                                  
                ((CustomViewHolder) holder).textView_comment.setText(userModels.get(position).comment);  //상태메세지 바인딩
                
            }

        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }


        //이너 클래스 추가해준것   RecyclerView.ViewHolder에서 item 추가해주기 위해 생성
        private class CustomViewHolder extends RecyclerView.ViewHolder {       //여기서 findViewById 해줌으로써 메모리 낭비 줄임
            //아이템들 세팅
            public ImageView imageView;
            public TextView textView;
            public TextView textView_comment;   //상태메세지 불러오기

            public CustomViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.frienditem_imageview);
                textView = (TextView) view.findViewById(R.id.frienditem_textview);
                textView_comment = (TextView)view.findViewById(R.id.frienditem_textview_comment);

            }
        }
    }



}
