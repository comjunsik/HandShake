package com.example.user.handsahke_2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.user.handsahke_2.fragment.BoardFragment;
import com.example.user.handsahke_2.model.BoardModel;
import com.example.user.handsahke_2.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BoardDetailActivity extends AppCompatActivity {

    BoardDetailAdapter adapter;
    BoardModel b;
    String uuid;
    boolean isThumb;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        adapter = new BoardDetailAdapter();
        RecyclerView recyclerView = findViewById(R.id.board_detail_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

        final TextView title = findViewById(R.id.board_detail_title);
        final TextView text = findViewById(R.id.board_detail_text);
        final LinearLayout thumbBtn = findViewById(R.id.board_detail_thumb);

        uuid = getIntent().getStringExtra("uuid");

        thumbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(isThumb) {
                    b.thumbIds = b.thumbIds.replace("#" + user.getUid(), "") ;
                    b.thumb--;
                } else {
                    b.thumbIds += "#" + user.getUid();
                    b.thumb++;
                }

                FirebaseDatabase.getInstance().getReference()
                        .child("Board")
                        .child(uuid)
                        .setValue(b)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(isThumb) {
                                    Toast.makeText(BoardDetailActivity.this, "좋아요 취소", Toast.LENGTH_SHORT).show();
                                    ImageView i = findViewById(R.id.board_detail_thumb_image);
                                    i.setImageResource(R.drawable.ic_thumb_up_grey_24dp);
                                    isThumb = false;

                                    FirebaseDatabase.getInstance().getReference()
                                            .child("users")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    UserModel u = dataSnapshot.getValue(UserModel.class);
                                                    u.thumbList = u.thumbList.replace(("#" + uuid), "");
                                                    String[] s = u.thumbList.split("#");

                                                    if(s.length > 1) {
                                                        if(!u.interest.contains("#배틀그라운드")){
                                                            u.interest = u.interest + "#배틀그라운드";
                                                        }
                                                    }

                                                    dataSnapshot.getRef().setValue(u);
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                } else {
                                    Toast.makeText(BoardDetailActivity.this, "좋아요 클릭", Toast.LENGTH_SHORT).show();
                                    ImageView i = findViewById(R.id.board_detail_thumb_image);
                                    i.setImageResource(R.drawable.ic_thumb_up_pink_24dp);
                                    isThumb = true;

                                    FirebaseDatabase.getInstance().getReference()
                                            .child("users")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    UserModel u = dataSnapshot.getValue(UserModel.class);
                                                    u.thumbList = u.thumbList + "#" + uuid;
                                                    dataSnapshot.getRef().setValue(u);
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }
                            }
                        });
            }
        });



        FirebaseDatabase.getInstance().getReference()
                .child("Board")
                .child(uuid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        b = dataSnapshot.getValue(BoardModel.class);
                        title.setText(b.title);
                        text.setText(b.text);
                        adapter.setData(b.images);

                        if(b.thumbIds.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            ImageView i = findViewById(R.id.board_detail_thumb_image);
                            i.setImageResource(R.drawable.ic_thumb_up_pink_24dp);
                            isThumb =  true;
                        } else {
                            ImageView i = findViewById(R.id.board_detail_thumb_image);
                            i.setImageResource(R.drawable.ic_thumb_up_grey_24dp);
                            isThumb =  false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private class BoardDetailAdapter extends RecyclerView.Adapter<BoardDetailHolder> {

        List<String> list = new ArrayList<>();

        @NonNull
        @Override
        public BoardDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BoardDetailHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.board_holder_detail, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull BoardDetailHolder holder, int position) {
            Log.d("??", "Image = " + list.get(position));
            ImageView i = holder.itemView.findViewById(R.id.board_detail_imageview);
            Glide.with(holder.itemView).load(list.get(position)).into(i);
        }

        @Override
        public int getItemCount() {
            if(list != null){
                return list.size();
            }
            return 0;
        }

        public void setData(List<String> list) {
            this.list = list;
            notifyDataSetChanged();
        }
    }

    private class BoardDetailHolder extends RecyclerView.ViewHolder {

        public BoardDetailHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
