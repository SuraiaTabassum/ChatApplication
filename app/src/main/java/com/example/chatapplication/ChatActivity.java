package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import android.net.Uri;
import android.os.Bundle;
import android.view.textclassifier.TextLanguage;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapplication.adapter.ChatRecyclerAdapter;
import com.example.chatapplication.adapter.SearchUserRecyclerAdapter;
import com.example.chatapplication.model.ChatMessageModel;
import com.example.chatapplication.model.ChatRoomModel;
import com.example.chatapplication.model.UserModel;
import com.example.chatapplication.utils.AndroidUtil;
import com.example.chatapplication.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.auth.User;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
UserModel otherUser;
String chatroomId;
EditText messageInput;
ImageButton sendMessageBtn;
ImageButton backBtn;
TextView otherUserName;
RecyclerView recyclerView;
ChatRoomModel chatRoomModel;
ChatRecyclerAdapter adapter;
ImageView imageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get userModel
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        chatroomId = FirebaseUtil.getChatRoomId(FirebaseUtil.currentUserId(),otherUser.getUserId());

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUserName = findViewById(R.id.other_user_name);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imageView = findViewById(R.id.profile_pic_image_view);


        FirebaseUtil.getOtherCurrentProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if(t.isSuccessful()){
                        Uri uri = t.getResult();
                        AndroidUtil.setprofilePic(this,uri,imageView);
                    }
                });
        
        backBtn.setOnClickListener((v) ->{
        onBackPressed();

        });
        otherUserName.setText(otherUser.getUsername());
        sendMessageBtn.setOnClickListener((v ->{
         String message = messageInput.getText().toString().trim();
         if(message.isEmpty())
             return;
         sendMessageToUser(message);
        }));

        getOrCreateChatRoomModel();

        setupChatRecyclerView();

    }
void setupChatRecyclerView(){

    Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
            .orderBy("timestamp",Query.Direction.DESCENDING);



    FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query,ChatMessageModel.class).build();


    adapter = new ChatRecyclerAdapter(options,getApplicationContext());
    LinearLayoutManager manager = new LinearLayoutManager(this);
    manager.setReverseLayout(true);
    recyclerView.setLayoutManager(manager);
    recyclerView.setAdapter(adapter);
    adapter.startListening();
    adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            recyclerView.smoothScrollToPosition(0);
        }
    });

}
    void sendMessageToUser(String message){

        chatRoomModel.setLastMessageTimestamp(Timestamp.now());
        chatRoomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatRoomModel.setLastMessage(message);
        FirebaseUtil.getChatRoomReference(chatroomId).set(chatRoomModel);


        ChatMessageModel chatMessageModel = new ChatMessageModel(message,FirebaseUtil.currentUserId(),Timestamp.now());

        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                       if(task.isSuccessful()){
                           messageInput.setText("");
                           sendNotification(message);
                       }
                    }
                });
    }
    void getOrCreateChatRoomModel(){
        FirebaseUtil.getChatRoomReference(chatroomId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                chatRoomModel = task.getResult().toObject(ChatRoomModel.class);
                if(chatRoomModel == null){
                    //chat first time
                    chatRoomModel = new ChatRoomModel(
                            chatroomId,
                            Arrays.asList(FirebaseUtil.currentUserId(),otherUser.getUserId()),
                            Timestamp.now(),
                            ""
                    );
                    FirebaseUtil.getChatRoomReference(chatroomId).set(chatRoomModel);

                }
            }
        });
    }
    void sendNotification(String message){
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                UserModel currentUser =  task.getResult().toObject(UserModel.class);
                try {
                    JSONObject jsonObject = new JSONObject();
                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title",currentUser.getUsername());
                    notificationObj.put("body",message);

                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId",currentUser.getUserId());
                    jsonObject.put("notification",notificationObj);
                    jsonObject.put("data",dataObj);
                    jsonObject.put("to",otherUser.getFcmToken());

                    callApi(jsonObject);

                }catch (Exception e){

                }
            }
        });

    }
    void callApi(JSONObject jsonObject){
         MediaType JSON = MediaType.parse("application/json");

        OkHttpClient client = new OkHttpClient();

        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(),JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization","Bearer AAAA0vUbbRE:APA91bGhZwHNDBfi6Bv2DFv8hUdDpLi8RbOA9zRiB8yBwle3fjJCAy9LEQ1wFSSFNEkeydlc_uTOHbAxfIz_phY3iJBG43Wqap20rUNn00VRTF5_n7xo16LY327s035k1Xyn_76Ff8IP")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }
}