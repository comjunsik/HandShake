package com.example.user.handsahke_2;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.example.user.handsahke_2.model.BoardModel;
import com.example.user.handsahke_2.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BoardAddActivity extends AppCompatActivity implements View.OnClickListener {

    private List<String> images = new ArrayList<>();

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_add);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        AppCompatButton addBtn = findViewById(R.id.btn_add_board);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialDialog dialog = new MaterialDialog.Builder(v.getContext())
                        .title("등록중")
                        .content("게시글 등록중입니다")
                        .cancelable(false)
                        .show();

                EditText title = findViewById(R.id.input_title);
                EditText text = findViewById(R.id.input_text);

                final String titleString = title.getText().toString();
                final String textString = text.getText().toString();



                final List<String> downUrls = new ArrayList<>();
                final List<String> imagesnames = new ArrayList<>();

                for (String s : images) {
                    final String uuid = UUID.randomUUID().toString();
                    final String boardUuid = UUID.randomUUID().toString();
                    imagesnames.add(uuid);
                    FirebaseStorage.getInstance().getReference()
                            .child("BoardImages")
                            .child(uuid)
                            .putFile(Uri.fromFile(new File(s)))
                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    String downloadUrl = task.getResult().getDownloadUrl().toString();
                                    downUrls.add(downloadUrl);

                                    if (images.size() == downUrls.size()) {
                                        BoardModel boardModel = new BoardModel();
                                        boardModel.title = titleString;
                                        boardModel.text = textString;
                                        boardModel.images = downUrls;
                                        boardModel.imagesname = imagesnames;
                                        boardModel.userName = "TTEST"; //todo UserName 싱글톤으로 혹은 인텐트로 추가
                                        boardModel.thumb = 0;
                                        boardModel.thumbIds = "";

                                        FirebaseDatabase.getInstance().getReference()
                                                .child("Board")
                                                .child(boardUuid)
                                                .setValue(boardModel)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        dialog.dismiss();
                                                        finish();
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });

        AppCompatButton addPhoto = findViewById(R.id.btn_add_photo);
        addPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        ImagePicker.create(this)
                .multi()// Activity or Fragment
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            List<Image> images = ImagePicker.getImages(data);

            for (Image i : images) {
                this.images.add(i.getPath());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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


