package com.example.user.handsahke_2.chat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.handsahke_2.Define;
import com.example.user.handsahke_2.R;
import com.example.user.handsahke_2.Storage;
import com.example.user.handsahke_2.model.ChatModel;
import com.example.user.handsahke_2.model.NotificationModel;
import com.example.user.handsahke_2.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;     //메시지 받을 상대방 uid
    private Button button;    //채팅방 메시지 전송버튼
    private EditText editText;    //채팅창 채팅 입력 editText

    private String uid;
    private String chatRoomUid;

    private RecyclerView recyclerView;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");  //시간 데이터 포맷 설정해주기

    private UserModel destinationUserModel;  //유저 정보
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    int peopleCount =0;  //몇명 읽었는지

    private Button btnBlock;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //채팅을 요구 하는 아이디 즉 로그인된 uid 자신
        uid = Storage.MyId;
        destinationUid = getIntent().getStringExtra("destinationUid");  //채팅 받는 아이디 -> PeopleFragment에서 putExtra() 해준 데이터 받아오기 상대방 uid
        button = (Button)findViewById(R.id.messageActivity_button);
        editText = (EditText)findViewById(R.id.messageActivity_editText);

        btnBlock=findViewById(R.id.btn_block);
        btnBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String BlockedId ="";
                BlockedId=Storage.MyBlockedId;
                if(BlockedId.contains(destinationUid)==false){
                    BlockedId+='#'+destinationUid;
                }
                FirebaseDatabase.getInstance().getReference()
                        .child(Define.FB_USERS).child(Storage.MyId).child(Define.FB_BLOCKED_ID).setValue(BlockedId);
                Toast.makeText(v.getContext(),"상대방이 차단되었습니다.", Toast.LENGTH_SHORT).show();

            }
        });

        recyclerView = (RecyclerView)findViewById(R.id.messageActivity_recyclerview);
        button.setOnClickListener(new View.OnClickListener() {   //채팅방이 기존에 있으면 메시지 전송만, 채팅방 없으면 채팅방에 대한 db구조까지 생성
            @Override
            public void onClick(View v) {


                ChatModel chatModel = new ChatModel();   //chatModel 불러오기
                chatModel.users.put(uid,true);        //자신 uid
                chatModel.users.put(destinationUid, true);      //상대방 uid

                //버튼 누르면 대화방 생성
                //DB에 집어넣기 push() -->일종의 primary키 push 구별할 임의의 id라 보면됨
                if (chatRoomUid == null) {     //채팅방이 없을경우 => 채팅방 생성
                    button.setEnabled(false);  //방만들어지는 중간에 전송버튼 연타하면 checkChatRoom()으로 넘어가기 전에 방계속 생성되는거 방지하기 위해 disable
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel)    //여기서 push()는 채팅방 이름 만들어 주는것, 일종의 채팅방에 대한 primary key
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {        //전송 버튼 누를때 마다 채팅방 중복으로 만들어 지는거 방지
                            checkChatRoom();    //메시지 전송후 바로 방이름이 무엇인지 체크하기 위해
                        }
                    });
                }else{      //채팅방이 있을 겨우
                    //메시지 보내는 부분
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = editText.getText().toString();
                    comment.timestamp = ServerValue.TIMESTAMP;  //파이어 베이스에서 제공하는 현재시간
                    FirebaseDatabase.getInstance().getReference()
                            .child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       //채팅방아이디                      //여기서 push()는 채팅방 이름 만들어 주는것, 일종의 채팅방에 대한 primary key
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {  //콜백을 이용해 데이터가 입력된거 확인
                            sendGcm();     //푸시 메시지
                            editText.setText("");  //데이터 입력된거 확인후 editText 초기화
                        }
                    });
                }


            }
        });
        checkChatRoom();    //oncreate() 안에 checkChatRoom 은 방이 만들어지자 마자 방이 있는지 체크하기 위해 -->이거 없음 어플 다시시작하면 채팅방 새로 만들어짐
    }
    //push 메시지를 위한 메서드
    void sendGcm(){
        Gson gson = new Gson();

        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = destinationUserModel.pushToken;
        notificationModel.notification.title = userName;   //보낸이 아이디
        notificationModel.notification.text=editText.getText().toString();
        notificationModel.data.title = userName;   // 메시질 보낼때 data 부분이 만들어져 푸시가 감
        notificationModel.data.text=editText.getText().toString();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"),gson.toJson(notificationModel));

        okhttp3.Request request =new okhttp3.Request.Builder()
                .header("Content-Type","application/json")
                .addHeader("Authorization","key=AIzaSyBLRcz08QXPYlNOHixl1mbteVGFut6fWL0")    //token 값
                .url("https://gcm-http.googleapis.com/gcm/send") //서버 url
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
        
    }

    //users 데이터 중복확인 하는 코드를 통해     ->채팅방 중복 생성 방지
    void checkChatRoom(){
        //orderByChild --> 중복체크
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid)  //orderByChild 중복 체크를 위해 받아오기 -> 데이터베이스 chatrooms->채팅방 id->users의 uid 받아오기
                .equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {                //orderByChild() ->모든 chatrooms를 users로 정렬하여 읽기
            @Override                                                                  //addListenerForSingleValueEvent()    이 방법은 한 번 로드된 후 자주 변경되지 않거나 능동적으로 수신 대기할 필요가 없는 데이터에 유용합니다. 
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()){    //getChildren 지정된 곳의 모든 파일 및 폴더의 목록을 검색      -> getChildren() 을 사용하여 users의 uid Iterable을 가져옴
                    ChatModel chatModel = item.getValue(ChatModel.class);     //database의 chatrooms -> users ->의 uid들 (내uid, destinationUid) 받아옴                                                                                               //(Iterable이란 member를 하나씩 차례로 반환 가능한 object를 말한다)
                    if (chatModel.users.containsKey(destinationUid)){    //dictionary.containsKey() 함수를 통해 지정된 문자열이 사전에 있는 키인지 파악할 수 있음.
                        //destinationUid가 있을 겨우
                        chatRoomUid = item.getKey(); //채팅방 아이디
                        button.setEnabled(true);  //채팅방 아이디 받고난 이후에 전송 버튼 살려주기
                        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));  //activity_message.xml의 recyclerView 에 binding
                        recyclerView.setAdapter(new RecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    //adpater 만들기
    class  RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        //comment 불러오는 코드
        List<ChatModel.Comment> comments;  //comment 담아오기 위해 ChatModel.list 생성
        
        public RecyclerViewAdapter(){
            comments = new ArrayList<>();  //선언

            FirebaseDatabase.getInstance().getReference().child(Define.FB_USERS)
                    .child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    destinationUserModel = dataSnapshot.getValue(UserModel.class);  //DB에 있는 유저정보를 UserModel.class로 캐스팅 해서 저장
                    getMessageList();  //유저 정보 불러온후 메시지 내용 불러오기 함수 호출
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        
        }
        //메시지 리스트 받아오는 함수 (채팅 내용)
        void getMessageList(){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid)
                    .child("comments");
            valueEventListener= databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) { //comments 내용 읽어오기
                    comments.clear();  //이거 안해주면 채팅layout에 이때까지 채팅했던 대화내용 전송할때마다 다 보내줌

                    Map<String,Object> readUsersMap = new HashMap<>();

                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class); //기존 채팅 내용
                        ChatModel.Comment comment_motify = item.getValue(ChatModel.Comment.class); //읽었다는 commnet
                        comment_motify.readUsers.put(uid,true);   //채팅 내용 읽었다는 태그 달아주기 ->서버가 내 uid 보고 읽었다는 것을 인지

                        readUsersMap.put(key,comment_motify);  //readUserMap에 읽은 내용이 들어가 있음
                        comments.add(comment_origin); //내용 DB에 저장
                    }
                    if(comments.size()==0){                           //기존의 대화내용이 없다면 return시켜서 함수 종료
                        return;
                    }

                    if(!comments.get(comments.size()-1).readUsers.containsKey(uid)) {  //comments의 마지막에 내가 없다면


                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments")
                                .updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            //onCompleteLisner를 달아줘서 서버가 정보가 들어갔다는 것을 확인한 후 event 실행
                            public void onComplete(@NonNull Task<Void> task) {
                                notifyDataSetChanged(); //list 새로 갱신 (메시지 새로고침)
                                recyclerView.scrollToPosition(comments.size() - 1);   //recyclerView의 마지막 제일 하단(comments.size() -1)으로 이동

                            }
                        });
                    }else{
                        notifyDataSetChanged(); //list 새로 갱신 (메시지 새로고침)
                        recyclerView.scrollToPosition(comments.size() - 1);   //recyclerView의 마지막 제일 하단(comments.size() -1)으로 이동 -->가장 최근에 보낸 메세지 볼수 있게 표시해주기 위해서 제일 하단으로 이동

                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            
        }
            
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false);  //item_message.xml 추가
            
            return new MessageViewHolder(view);   //MessageViewHolder -> 메시지 재사용할때 사용하는 class 만들어서 사용
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {  //data binding 해주는 함수
             MessageViewHolder messageViewHolder = ((MessageViewHolder)holder);  //-> holder 달고 MessageViewholder로 캐스팅 해줘야 MessageViewHolder 내의 데이터 접근 가능
            
            //내가 보낸 메시지     comments의 uid   맨위에 선언해준 내 uid 변수
            if (comments.get(position).uid.equals(uid)){       //comments에 들어있는 uid가 내 uid이면
                messageViewHolder.textView_message.setText(comments.get(position).message);    //채팅내용
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);   //rigthbubble 말풍선 넣기
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);   //내가 말하는 경우 프포필과 이름 보여주는 layout 감춰주기
                messageViewHolder.textView_message.setTextSize(25);
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);  //내가 보낸 메시지는 오른쪽에 정렬
                setReadCounter(position,messageViewHolder.textView_readCounter_left);  //내가 보낸 메시지 일때는 안 읽은 사람 수 왼쪽에 표시
            //상대방이 보낸 메시지
            }else{  //대화 상대
                //이미지 넣어주기
                Glide.with(holder.itemView.getContext())
                        .load(destinationUserModel.profileImageUrl)   //loadt(주소) 불러오기
                        .apply(new RequestOptions().circleCrop())  //원형 이미지로 profile
                        .into(messageViewHolder.imageView_profile);
                messageViewHolder.textView_name.setText(destinationUserModel.userName);  //유저 이름
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE); //profile 이미지와 유저 name 보여주는 layout Visible
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);  //leftbubble 이미지 입혀주기
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setTextSize(25);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);  //상대방이 보낸 메시지는 왼쪽에 정렬
                setReadCounter(position,messageViewHolder.textView_readCounter_right);  //상대방이 보낸 메시지 일때는 안 읽은 사람 수 오른쪽에 표시
                
            }
            //현재 시간 보내기 설정
            long unixTime = (long) comments.get(position).timestamp;  //ChatModel.comments의 timestamp가졍괴
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/seoul")); //시간 지역
            String time = simpleDateFormat.format(date);           //포맷 바꿔주기
            messageViewHolder.textView_timestamp.setText(time);

        }
        void setReadCounter(final int position, final TextView textView){      //채팅방에 총 몇명있는지 몇명이 읽었는지 표시해주는 메서드
            if(peopleCount==0){        //인원수가 0이냐
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String,Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();  //db안의 users의 uid와 true 값을 HashMap으로 받아온다.
                        peopleCount = users.size();  //peopleCount에 사람 수 넣기

                        int count = peopleCount - comments.get(position).readUsers.size();  //comments를 읽지 않은 사람들의 수
                        if(count>0){
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(String.valueOf(count));
                        }else{
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }else {
                int count = peopleCount - comments.get(position).readUsers.size();  //comments를 읽지 않은 사람들의 수
                if(count>0){
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(String.valueOf(count));
                }else{
                    textView.setVisibility(View.INVISIBLE);
                }
            }

        }

        @Override
        public int getItemCount() {
            return comments.size();   //getItemCount에 size()로 리턴 해줘야 정확히 몇번 돌아가는지 확인 가능하므로 필수
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {  //view를 재사용할때 사용하는 class ->메모리 낭비 줄임
            //채팅방 profile 메시지 이름, layout 불러와주기
            public TextView textView_message;   //메시지 ㄴ내용
            public TextView textView_name;        //메세지 보낸 사람 이름
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination; //대화 상대 정보(프로필,이름) 보여주는 linearLayout
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;  //메세지 전송시간
            public TextView textView_readCounter_left;   //메세지 읽음 표시 개수
            public TextView textView_readCounter_right;
            

            public MessageViewHolder(View view) {
                super(view);
                textView_message = (TextView) view.findViewById(R.id.messageItem_textView_message);
                textView_name = (TextView)view.findViewById(R.id.messageItem_textview_name);
                imageView_profile =(ImageView)view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination = (LinearLayout)view.findViewById(R.id.messageItem_linearlayout_destination);
                linearLayout_main = (LinearLayout)view.findViewById(R.id.messageItem_linearlayout_main);    //채팅방에서 메세지를 왼쪽에 표시할것인지 오른쪽에 표시할 것인지 구분해 주기 위해
                textView_timestamp = (TextView)view.findViewById(R.id.messageItem_textview_timestamp);
                textView_readCounter_left = (TextView)view.findViewById(R.id.messageItem_textView_readCounter_left);
                textView_readCounter_right = (TextView)view.findViewById(R.id.messageItem_textView_readCounter_right);
            }
        }
    }
    //벡 키에 애니메이션 넣기 , 백 키 작동부분
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(valueEventListener != null){
            databaseReference.removeEventListener(valueEventListener);  //채팅방 나가면 이제 read표시 안해줌
            
        }
        finish();
        overridePendingTransition(R.anim.fromleft,R.anim.toright);        //finish() 직후 명시적으로 전환 애니매이션 지정할수 있는 함수  ->없어 질때 애니매이션 넣기
    }
}
