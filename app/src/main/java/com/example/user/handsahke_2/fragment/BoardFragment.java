package com.example.user.handsahke_2.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.user.handsahke_2.BoardAddActivity;
import com.example.user.handsahke_2.BoardDetailActivity;
import com.example.user.handsahke_2.R;
import com.example.user.handsahke_2.model.BoardModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;


public class BoardFragment extends Fragment {

    RecyclerView recyclerView;
    BoardAdapter adapter;
    ArrayList<String> uuidList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board,container,false);
        FloatingActionButton fab = view.findViewById(R.id.board_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(inflater.getContext(), BoardAddActivity.class));
            }
        });

        adapter = new BoardAdapter();
        recyclerView = view.findViewById(R.id.board_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Board");   //DB users부분 접근

        //DB 읽기
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<BoardModel> list = new ArrayList<>();
                uuidList.clear();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String key = child.getKey();
                    uuidList.add(key);
                }

                for (int i=0; i<uuidList.size(); i++) {
                    BoardModel b = dataSnapshot.child(uuidList.get(i)).getValue(BoardModel.class);
                    list.add(b);
                }

                adapter.setData(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("AccountFragment", "Failed to read value.", databaseError.toException());
            }
        });

        super.onResume();
    }

    private class BoardAdapter extends RecyclerView.Adapter<BoardHolder> implements View.OnClickListener {

        ArrayList<BoardModel> list = new ArrayList<>();

        @NonNull
        @Override
        public BoardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BoardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.board_holder, parent, false));
        }

        @SuppressLint("RecyclerView")
        @Override
        public void onBindViewHolder(@NonNull BoardHolder holder,  final int position) {
            holder.setData(list.get(position));

            ImageView thumb = holder.itemView.findViewById(R.id.board_holder_thumb);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), BoardDetailActivity.class);
                    intent.putExtra("uuid", uuidList.get(position));
                    startActivity(intent);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new MaterialDialog.Builder(v.getContext())
                            .title("삭제")
                            .content("정말 삭제하시겠습니까?")
                            .positiveText("확인")
                            .negativeText("취소")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("Board")
                                            .child(uuidList.get(position))
                                            .removeValue();

                                    for (String s : list.get(position).imagesname) {
                                        Log.d("??", s);
                                        FirebaseStorage.getInstance().getReference()
                                                .child("BoardImages")
                                                .child(s)
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });
                                    }
                                }
                            }).show();
                    return false;
                }
            });
            thumb.setOnClickListener(this);
        }

        @Override
        public int getItemCount() {
            if(list != null) {
                return list.size();
            }

            return 0;
        }

        public void setData(ArrayList<BoardModel> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public void onClick(View v) {
        }
    }

    private class BoardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView thumbcount;

        public BoardHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.board_title);
            thumbcount = itemView.findViewById(R.id.thumb_count);
        }

        @Override
        public void onClick(View v) {

        }

        public void setData(BoardModel b) {
            title.setText(b.title);
            thumbcount.setText(String.valueOf(b.thumb));
        }
    }
}
