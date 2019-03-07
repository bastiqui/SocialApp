package com.example.gerard.socialapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;

public class WelcomeActivity extends AppCompatActivity {

    TextView tvUsername;
    ImageView ivUserAvatar;
    ImageView preview;

    static final int RC_IMAGE_PICK = 9000;
    static final int RC_VIDEO_PICK = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        tvUsername = findViewById(R.id.username);
        ivUserAvatar = findViewById(R.id.useravatar);
        preview = findViewById(R.id.preview);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            tvUsername.setText(firebaseUser.getDisplayName());
            Glide.with(this)
                    .load(firebaseUser.getPhotoUrl().toString())
                    .into(ivUserAvatar);


            findViewById(R.id.sign_out).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    File photoFile = null;
                    try {
                        photoFile = File.createTempFile("IMG", ".jpg", getCacheDir());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    assert photoFile != null;
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(WelcomeActivity.this, "fileprovider", photoFile));
                    startActivityForResult(takePicture, RC_IMAGE_PICK);

                }
            });

            findViewById(R.id.video).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, RC_VIDEO_PICK);
                }
            });
        }
    }
}
