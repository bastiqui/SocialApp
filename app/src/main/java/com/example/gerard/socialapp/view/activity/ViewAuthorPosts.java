package com.example.gerard.socialapp.view.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gerard.socialapp.GlideApp;
import com.example.gerard.socialapp.R;
import com.example.gerard.socialapp.model.Post;
import com.example.gerard.socialapp.view.PostViewHolder;
import com.example.gerard.socialapp.view.fragment.PostsFragment;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Objects;

public class ViewAuthorPosts extends Fragment {

    public DatabaseReference mReference;
    public FirebaseUser mUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_posts, container, false);

        mReference = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getActivity().getIntent();
        String key = intent.getStringExtra("id");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
                .setIndexedQuery(setQuery(key), mReference.child("posts/user-posts").child(key), Post.class)
                .setLifecycleOwner(this)
                .build();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        RecyclerView recycler = view.findViewById(R.id.rvPosts);
        recycler.setLayoutManager(mLayoutManager);
        recycler.setAdapter(new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new PostViewHolder(inflater.inflate(R.layout.item_post, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder viewHolder, int position, @NonNull final Post post) {
                final String postKey = getRef(position).getKey();

                viewHolder.author.setText(post.author);

                GlideApp.with(getActivity()).load(post.authorPhotoUrl).circleCrop().into(viewHolder.photo);

                if (post.likes.containsKey(mUser.getUid())) {
                    viewHolder.like.setImageResource(R.drawable.heart_on);
                    viewHolder.numLikes.setTextColor(getResources().getColor(R.color.red));
                } else {
                    viewHolder.like.setImageResource(R.drawable.heart_off);
                    viewHolder.numLikes.setTextColor(getResources().getColor(R.color.grey));
                }

                viewHolder.verPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewPost = new Intent(getContext(), ViewpostActivity.class);
                        viewPost.putExtra("post", postKey);
                        startActivity(viewPost);
                    }
                });

                viewHolder.content.setText(post.content);

                if (Objects.equals(mUser.getDisplayName(), post.author)) {
                    viewHolder.deletePost.setVisibility(View.VISIBLE);
                    viewHolder.deletePost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            assert postKey != null;
                            mReference.child("posts/data").child(postKey).setValue(null);
                            mReference.child("posts/all-posts").child(postKey).setValue(null);
                            mReference.child("posts/user-posts").child(post.uid).child(postKey).setValue(null);
                            mReference.child("posts/user-likes").child(post.uid).child(postKey).setValue(null);
                        }
                    });
                } else viewHolder.deletePost.setVisibility(View.GONE);

                if (post.mediaUrl != null) {
                    viewHolder.image.setVisibility(View.VISIBLE);
                    if ("audio".equals(post.mediaType)) {
                        viewHolder.image.setImageResource(R.drawable.audio);
                    } else {
                        GlideApp.with(getActivity()).load(post.mediaUrl).centerCrop().into(viewHolder.image);

                    }
                    viewHolder.image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), ShowImageActivity.class);
                            intent.putExtra("mediaUrl", post.mediaUrl);
                            intent.putExtra("mediaType", post.mediaType);
                            startActivity(intent);
                        }
                    });
                } else {
                    viewHolder.image.setVisibility(View.GONE);
                }

                viewHolder.numLikes.setText(String.valueOf(post.likes.size()));

                viewHolder.like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (post.likes.containsKey(mUser.getUid())) {
                            mReference.child("posts/data").child(postKey).child("likes").child(mUser.getUid()).setValue(null);
                            mReference.child("posts/user-likes").child(mUser.getUid()).child(postKey).setValue(null);
                        } else {
                            mReference.child("posts/data").child(postKey).child("likes").child(mUser.getUid()).setValue(true);
                            mReference.child("posts/user-likes").child(mUser.getUid()).child(postKey).setValue(true);
                        }
                    }
                });
            }
        });

        return view;
    }

    Query setQuery(String key){
        return mReference.child("posts/user-posts").child(key);
    }
}
