package com.example.user.handsahke_2.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;

import android.os.Bundle;
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
import com.example.user.handsahke_2.model.ChatModel;
import com.example.user.handsahke_2.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class ChatFragment extends Fragment {

    public SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm"); //사람이 알아볼수 있게 데이터 포맷 정해주기

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat,container,false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.chatfragment_recyclerview);  //fragment_chat.xml의 recyclerview id
        recyclerView.setAdapter(new ChatRecyclerViewAdapter()); //바인딩
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));  //어떤 형식으로 보여줄꺼냐 list

        return view;
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ChatModel> chatModels = new ArrayList<>();    //채팅에 대한정보
        private String uid;
        private ArrayList<String > destinationUsers= new ArrayList<>();  //대화 상대들의 data
        //채팅몽록가져오기
        public ChatRecyclerViewAdapter() {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //uid 정보

            //채팅에 대한 정보
            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatModels.clear();
                    for (DataSnapshot item : dataSnapshot.getChildren()){      //데이터 쌓기
                        chatModels.add(item.getValue(ChatModel.class));
                    }
                    notifyDataSetChanged();      //새로고침
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {          //보여주기
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) { //view 바인딩

            final CustomViewHolder customViewHolder = (CustomViewHolder)holder;
            String destinationUid = null; //상대 uid
            //챗방에 있는 유저를 일일이 다 체크
            for(String user: chatModels.get(position).users.keySet()){ //keySet() -> HashMap에 저장된 모든 키가 저장된 Set을 반환
                if(!user.equals(uid)){     //내가 아닌 사람들
                      destinationUid = user;
                      destinationUsers.add(destinationUid);
                }
            }
            FirebaseDatabase.getInstance().getReference().child(Define.FB_USERS).child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);  //받아온 destinationUid를 userModel 에 담기
                    Glide.with(customViewHolder.itemView.getContext())    //userModel의 image 가져오기
                            .load(userModel.profileImageUrl)
                            .apply(new RequestOptions().circleCrop())
                            .into(customViewHolder.imageView);
                    customViewHolder.textView_title.setText(userModel.userName);   //타이틀을 상대방 이름으로
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            //마지막 메세지 띄워주기   (메시지를 내림 차순으로 정렬 후 마지막 메시지의 키값을 가져옴)
            Map<String,ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder()); //Collections.reverseOrder -> 내림차순
            commentMap.putAll(chatModels.get(position).comments);   //putAll() -> 채팅에대한 내용(comment) 를 commentMap에 복사해서 넣어주기
            if(commentMap.keySet().toArray().length>0) {


                String lastMessageKey = (String) commentMap.keySet().toArray()[0]; //채팅에 대한 첫번째 값 뽑아오기  (마지막으로 보낸 메시지) / keySet()은 map안의 값을 가져오는 함수
                customViewHolder.textView_last_message.setText(chatModels.get(position).comments.get(lastMessageKey).message);//바인딩 해주기

                customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), MessageActivity.class);  //채팅방으로 화면전환 intent생성
                        intent.putExtra("destinationUid", destinationUsers.get(position));   //putExtra()->누구랑 대화할지  intent로 MessageActivity에 destinationUid라는 이름으로 데이터 전달

                        //애니메이션 적용
                        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft); //오른쪽부터 채팅방들어가는 애니메이션
                        startActivity(intent, activityOptions.toBundle());

                    }
                });
                //채팅방 목록에서 마지막 메세지 타임스탬프 보여주기
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));  //서울 시간으로 타임포맷 설정
                long unixTime = (long) chatModels.get(position).comments.get(lastMessageKey).timestamp;
                Date date = new Date(unixTime);
                customViewHolder.textView_timestamp.setText(simpleDateFormat.format(date));

            }
        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }                                                                                              

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView textView_title;
            public TextView textView_last_message;
            public TextView textView_timestamp;  //채팅방 마지막 메시지 시간
            public CustomViewHolder(View view) {
                super(view);

                imageView =(ImageView)  view.findViewById(R.id.chatitem_imageview);
                textView_title=(TextView)view.findViewById(R.id.chatitem_textview_title);
                textView_last_message = (TextView)view.findViewById(R.id.chatitem_textview_lastMessage);
                textView_timestamp = (TextView)view.findViewById(R.id.chatitem_textview_timestamp);

            }
        }
    }
}
