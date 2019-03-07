package com.example.gerard.socialapp.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gerard.socialapp.GlideApp;
import com.example.gerard.socialapp.R;
import com.example.gerard.socialapp.model.Post;
import com.example.gerard.socialapp.view.PostViewHolder;
import com.example.gerard.socialapp.view.activity.NewPostActivity;
import com.example.gerard.socialapp.view.activity.ShowImageActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class PostsFragment extends Fragment {
    public DatabaseReference mReference;
    public FirebaseUser mUser;
    public FloatingActionButton fab;
    public RecyclerView recycler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_posts, container, false);

        mReference = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        fab = view.findViewById(R.id.fab);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
                .setIndexedQuery(setQuery(), mReference.child("posts/data"), Post.class)
                .setLifecycleOwner(this)
                .build();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.findFirstVisibleItemPosition();

        recycler = view.findViewById(R.id.rvPosts);

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) fab.hide();
                else if (dy < 0 && fab.getVisibility() != View.VISIBLE) fab.show();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), NewPostActivity.class));
            }
        });

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

                GlideApp.with(PostsFragment.this).load(post.authorPhotoUrl).circleCrop().into(viewHolder.photo);

                if (post.likes.containsKey(mUser.getUid())) {
                    viewHolder.like.setImageResource(R.drawable.heart_on);
                    viewHolder.numLikes.setTextColor(getResources().getColor(R.color.red));
                } else {
                    viewHolder.like.setImageResource(R.drawable.heart_off);
                    viewHolder.numLikes.setTextColor(getResources().getColor(R.color.grey));
                }

                /*viewHolder.verPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewPost = new Intent(getContext(), ViewpostActivity.class);
                        viewPost.putExtra("post", postKey);
                        startActivity(viewPost);
                    }
                });*/

                viewHolder.content.setText(post.content);

                if (post.uid.equals(mUser.getUid())) {
                    viewHolder.deletePost.setVisibility(View.VISIBLE);
                    viewHolder.deletePost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mReference.child("posts/data").child(postKey).setValue(null);
                            mReference.child("posts/all-posts").child(postKey).setValue(null);
                            mReference.child("posts/user-posts").child(post.uid).child(postKey).setValue(null);
                            mReference.child("posts/user-likes").child(post.uid).child(postKey).setValue(null);
                        }
                    });
                } else {
                    viewHolder.deletePost.setVisibility(View.GONE);
                }

                if (post.mediaUrl != null) {
                    viewHolder.image.setVisibility(View.VISIBLE);
                    if ("audio".equals(post.mediaType)) {
                        viewHolder.image.setImageResource(R.drawable.audio);
                    } else {
                        GlideApp.with(PostsFragment.this).load(post.mediaUrl).centerCrop().into(viewHolder.image);

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

    Query setQuery(){
        return mReference.child("posts/data");
    }
}
