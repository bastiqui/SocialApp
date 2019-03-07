package com.example.gerard.socialapp.view.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.gerard.socialapp.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ShowImageActivity extends AppCompatActivity {

    VideoView mVideoView;
    com.jsibbold.zoomage.ZoomageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        mImageView = findViewById(R.id.imageView);
        mVideoView = findViewById(R.id.videoView);

        Intent intent = getIntent();
        String mediaUrl = intent.getStringExtra("mediaUrl");
        String mediaType = intent.getStringExtra("mediaType");

        System.out.println("ABCD -> " + mediaUrl);
        System.out.println("ABCD -> " + mediaType);

        if ("video".equals(mediaType) || "audio".equals(mediaType)) {
            MediaController mc = new MediaController(this);
            mc.setAnchorView(mVideoView);
            mVideoView.setMediaController(mc);
            mVideoView.setVideoPath(mediaUrl);
            mVideoView.start();
        } else if ("image".equals(mediaType)) {
            Picasso.get().load(mediaUrl).into(mImageView);
        }
    }
}
