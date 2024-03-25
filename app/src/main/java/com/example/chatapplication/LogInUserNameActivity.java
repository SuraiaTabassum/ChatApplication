package com.example.chatapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapplication.model.UserModel;
import com.example.chatapplication.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Calendar;

public class LogInUserNameActivity extends AppCompatActivity {
    EditText userNameInput;
    Button letMeInBtn;
    ProgressBar progressBar;
    String phoneNumber;
    UserModel userModel;
    Object binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_user_name);

        userNameInput = findViewById(R.id.login_userName);
        letMeInBtn = findViewById(R.id.login_letmeIn_btn);
        progressBar = findViewById(R.id.login_progress_bar);

        phoneNumber = getIntent().getExtras().getString("phone");
        getUserName();

        letMeInBtn.setOnClickListener((v -> {
            setUserName();
        }));


    }
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, monthOfYear, dayOfMonth) -> {
                    String dateOfBirth = dayOfMonth + "/" + (monthOfYear + 1) + "/" + selectedYear;

                },
                year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }


    void setUserName(){

        String userName = userNameInput.getText().toString();
        if(userName.isEmpty() || userName.length() <3){
            userNameInput.setError("User name length should be at least 3 characters");
            return;
        }
        if(userModel != null){
            userModel.setUsername(userName);
        }else {
            userModel = new UserModel(phoneNumber,userName, Timestamp.now(),FirebaseUtil.currentUserId());
        }
        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               setInProgress(false);
               if(task.isSuccessful()){
                   Intent intent = new Intent(LogInUserNameActivity.this, MainActivity.class);
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   startActivity(intent);
               }
            }
        });
    }
    void getUserName(){
        setInProgress(true);
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
               setInProgress(false);
               if(task.isSuccessful()){
                userModel =   task.getResult().toObject(UserModel.class);
               if(userModel != null){
                 userNameInput.setText(userModel.getUsername());
               }
               }
            }
        });

    }
    void setInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            letMeInBtn.setVisibility(View.GONE);
        }else {
            progressBar.setVisibility(View.GONE);
            letMeInBtn.setVisibility(View.VISIBLE);
        }
    }
}