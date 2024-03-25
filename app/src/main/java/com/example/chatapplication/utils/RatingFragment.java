package com.example.chatapplication.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.adapter.CommentAdapter;
import com.example.chatapplication.adapter.Comments;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class RatingFragment extends Fragment implements CommentAdapter.OnClickListener {

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyPrefs";
    private static final String PREF_LIKED_COMMENTS = "likedComments";

    private DatabaseReference commentsRef;
    private CommentAdapter commentAdapter;
    private ArrayList<Comments> mCommentList = new ArrayList<>();
    private TextView comment;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rating, container, false);

        // Initialize SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Initialize Firebase Database
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://quizzapp-2390a-default-rtdb.firebaseio.com/");
        commentsRef = db.getReference("comments");

        getAllComments();

        // RecyclerView setup
        RecyclerView recyclerView = view.findViewById(R.id.RecyclerviewComments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter(getContext(), getCurrentUserID(), mCommentList, this, this);
        recyclerView.setAdapter(commentAdapter);
        FloatingActionButton postComment = view.findViewById(R.id.post_comment);
        comment = view.findViewById(R.id.comment);

        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        listenForCommentUpdates(updatedCommentList -> {
            updateCommentListUI(updatedCommentList);
        });

        // Rating functionality
        RatingBar mRating = view.findViewById(R.id.rating);
        Button mSubmit = view.findViewById(R.id.submit);
        final TextView mThank = view.findViewById(R.id.thank);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = mRating.getRating();
                mThank.setVisibility(View.VISIBLE);
                mSubmit.setVisibility(View.INVISIBLE);

                if (rating == 5) {
                    mThank.setText("Thank you");
                } else if (rating == 0) {
                    mThank.setText("very disappointing");
                } else {
                    mThank.setText("thank you for your feedback");
                }
            }
        });

        return view;
    }

    private void listenForCommentUpdates(Consumer<ArrayList<Comments>> callback) {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Comments> updatedCommentList = new ArrayList<>();
                for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                    Comments comment = commentSnapshot.getValue(Comments.class);
                    if (comment != null) {
                        updatedCommentList.add(comment);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    callback.accept(updatedCommentList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event if needed
            }
        });
    }

    private void postComment() {
        String commentText = comment.getText().toString();

        if (!commentText.isEmpty()) {
            long currentTime = new Date().getTime();
            Toast.makeText(getContext(), commentText, Toast.LENGTH_SHORT).show();

            // If user is not logged in, use a default name
            String userName = FirebaseAuth.getInstance().getCurrentUser() != null ?
                    FirebaseAuth.getInstance().getCurrentUser().getDisplayName() :
                    "Anonymous";

            Comments instanceOfComment = new Comments(commentText, userName, currentTime, "Registered users");
            mCommentList.add(instanceOfComment);
            comment.setText("");
            commentAdapter.notifyDataSetChanged();
            updateCommentListInDatabase(mCommentList);
        } else {
            Toast.makeText(getContext(), "Enter a comment.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCommentListInDatabase(ArrayList<Comments> commentList) {
        commentsRef.setValue(commentList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "Comment list updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "Error updating comment list: " + e.getMessage());
                    }
                });
    }

    private void getAllComments() {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mCommentList.clear(); // Clear the existing list before adding new comments
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comments comment = snapshot.getValue(Comments.class);
                    if (comment != null) {
                        mCommentList.add(comment);
                    }
                }
                commentAdapter.notifyDataSetChanged(); // Notify the adapter that the dataset has changed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that may occur
                Toast.makeText(getContext(), "Failed to retrieve comments: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLikeClick(int position, TextView likeCountTextView, ImageButton likeButton) {
        Comments comment = mCommentList.get(position);
        if (comment == null) {
            return;
        }

        String userId = getCurrentUserID();
        if (userId == null) {
            return;
        }

        ArrayList<String> likedBy = comment.getLikedBy();
        ArrayList<String> dislikedBy = comment.getDislikedBy();

        if (!dislikedBy.contains(userId)) {
            if (likedBy.contains(userId)) {
                likedBy.remove(userId);
                likeButton.setImageResource(R.drawable.baseline_thumb_up_24);
            } else {
                likedBy.add(userId);
                likeButton.setImageResource(R.drawable.thum_up_after_liked);
                // Save liked comment locally
                saveLikedComment(comment.getCommentBy()); // Assuming getId() returns unique identifier for comments
            }
        }

        int likeCount = likedBy.size();
        likeCountTextView.setText(String.valueOf(likeCount));
        likeCountTextView.setVisibility(likeCount > 0 ? View.VISIBLE : View.INVISIBLE);

        updateCommentListInDatabase(mCommentList);
        commentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDisLikeClick(int position, TextView disLikeCountTextView, ImageButton dislikeButton) {
        Comments comment = mCommentList.get(position);
        if (comment == null) {
            return;
        }

        String userId = getCurrentUserID();
        if (userId == null) {
            return;
        }

        ArrayList<String> likedBy = comment.getLikedBy();
        ArrayList<String> dislikedBy = comment.getDislikedBy();

        if (!likedBy.contains(userId)) {
            if (dislikedBy.contains(userId)) {
                dislikedBy.remove(userId);
                dislikeButton.setImageResource(R.drawable.baseline_thumb_down_24);
            } else {
                dislikedBy.add(userId);
                dislikeButton.setImageResource(R.drawable.baseline_thumb_down_after_dislike_24);
            }

            int dislikeCount = dislikedBy.size();
            disLikeCountTextView.setText(String.valueOf(dislikeCount));
            disLikeCountTextView.setVisibility(dislikeCount > 0 ? View.VISIBLE : View.INVISIBLE);

            updateCommentListInDatabase(mCommentList);
            commentAdapter.notifyDataSetChanged();
        }
    }

    private void saveLikedComment(String commentId) {
        // Retrieve existing liked comments
        Set<String> likedComments = sharedPreferences.getStringSet(PREF_LIKED_COMMENTS, new HashSet<>());

        // Add the new liked comment
        likedComments.add(commentId);

        // Save the updated liked comments set
        sharedPreferences.edit().putStringSet(PREF_LIKED_COMMENTS, likedComments).apply();
    }

    private boolean isCommentLiked(String commentId) {
        Set<String> likedComments = sharedPreferences.getStringSet(PREF_LIKED_COMMENTS, new HashSet<>());
        return likedComments.contains(commentId);
    }

    private String getCurrentUserID() {
        return FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().toString() : null;
    }

    private void updateCommentListUI(ArrayList<Comments> updatedCommentList) {
        mCommentList.clear();
        mCommentList.addAll(updatedCommentList);
        commentAdapter.notifyDataSetChanged();
    }
}
