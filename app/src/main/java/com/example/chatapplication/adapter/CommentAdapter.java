package com.example.chatapplication.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;



public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final String currentUserId;
    private final ArrayList<Comments> commentList;
    private final OnClickListener likeClickListener;
    private final OnClickListener dislikeClickListener;

    public CommentAdapter(Context context, String currentUserId,
                          ArrayList<Comments> commentList,
                          OnClickListener likeClickListener,
                          OnClickListener dislikeClickListener) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.commentList = commentList;
        this.likeClickListener = likeClickListener;
        this.dislikeClickListener = dislikeClickListener;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).bind(commentList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        TextView commentedUserName, userComments, commentTime, likeCount, dislikeCount;
        ImageButton likeButton, dislikeButton;
        CircleImageView ProfileUserImage;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentedUserName = itemView.findViewById(R.id.Commented_userName);
            commentTime = itemView.findViewById(R.id.comment_time);
            likeCount = itemView.findViewById(R.id.like_count);
            dislikeCount = itemView.findViewById(R.id.dislike_count);
            likeButton = itemView.findViewById(R.id.like_button);
            dislikeButton = itemView.findViewById(R.id.dislike_button);
            ProfileUserImage = itemView.findViewById(R.id.iv_profile_user_image);
            userComments = itemView.findViewById(R.id.User_comments);
        }

        public void bind(Comments model) {
            if (model == null) {
                return;
            }

            Glide.with(context)
                    .load(model.getUserProfileUri())
                    .centerCrop()
                    .placeholder(R.drawable.no_profile_pic)
                    .into(ProfileUserImage);

            commentedUserName.setText(model.getCommentBy());
            userComments.setText(model.getComment());

            long commentTimestamp = model.getTimeStamp();
            long currentTime = System.currentTimeMillis();
            long timeDifference = currentTime - commentTimestamp;

            long secondsDifference = timeDifference / 1000;
            long minutesDifference = secondsDifference / 60;
            long hoursDifference = minutesDifference / 60;
            long daysDifference = hoursDifference / 24;
            long weeksDifference = daysDifference / 7;
            long yearsDifference = daysDifference / 365;

            String timeAgoText = "";
            if (yearsDifference > 0)
                timeAgoText = yearsDifference + "y";
            else if (weeksDifference > 0)
                timeAgoText = weeksDifference + "w";
            else if (daysDifference > 0)
                timeAgoText = daysDifference + "d";
            else if (hoursDifference > 0)
                timeAgoText = hoursDifference + "h";
            else if (minutesDifference > 0)
                timeAgoText = minutesDifference + "m";
            else
                timeAgoText = secondsDifference + "s";

            commentTime.setText(timeAgoText);

            ArrayList<String> likedByList = model.getLikedBy();
            ArrayList<String> dislikedByList = model.getDislikedBy();

            int likeCountVal = likedByList != null ? likedByList.size() : 0;
            int dislikeCountVal = dislikedByList != null ? dislikedByList.size() : 0;

            if (likedByList != null && likedByList.contains(currentUserId)) {
                likeButton.setImageResource(R.drawable.thum_up_after_liked);
            }
            if (dislikedByList != null && dislikedByList.contains(currentUserId)) {
                dislikeButton.setImageResource(R.drawable.baseline_thumb_down_after_dislike_24);
            }
            if (likeCountVal > 0) {
                likeCount.setText(String.valueOf(likeCountVal));
                likeCount.setVisibility(View.VISIBLE);
            }
            if (dislikeCountVal > 0) {
                dislikeCount.setText(String.valueOf(dislikeCountVal));
                dislikeCount.setVisibility(View.VISIBLE);
            }

            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    likeClickListener.onLikeClick(getAdapterPosition(), likeCount, likeButton);
                }
            });

            dislikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dislikeClickListener.onDisLikeClick(getAdapterPosition(), dislikeCount, dislikeButton);
                }
            });
        }

    }

    public interface OnClickListener {
        void onLikeClick(int position, TextView likeCountTextView, ImageButton likeButton);

        void onDisLikeClick(int position, TextView disLikeCountTextView, ImageButton dislikeButton);
    }
}
