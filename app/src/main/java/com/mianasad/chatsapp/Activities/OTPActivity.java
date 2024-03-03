package com.mianasad.chatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mianasad.chatsapp.R;
import com.mianasad.chatsapp.databinding.ActivityOTPBinding;
import com.mukesh.OnOtpCompletionListener;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {




    ActivityOTPBinding binding;
    FirebaseAuth auth;

    String verificationId;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOTPBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//Notification
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
        {
            NotificationChannel Chanel=new NotificationChannel("My Notification","My Notification",NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            manager.createNotificationChannel(Chanel);

        }





//In the start showing the dialog box to the user

        Toast.makeText(getApplicationContext(),"yes i am ready to serve you here ",Toast.LENGTH_LONG).show();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();
//getting auth from firebase
        auth = FirebaseAuth.getInstance();
//hidng the action bar
        getSupportActionBar().hide();


//getting the phone  number from previous activity that is passed through the intent
        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        //Toast message for debugging
Toast.makeText(getApplicationContext(),phoneNumber.toString()+"gotted this",Toast.LENGTH_SHORT);

//showing the user phone number in textbox
        binding.phoneLbl.setText("Verify " + phoneNumber);


//getting the phone number authentication form firebase and setting up the required details
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                //passing user phone number
                .setPhoneNumber(phoneNumber)

                //timeout
                .setTimeout(60L, TimeUnit.SECONDS)
                //refernce of Current Activity
                .setActivity(OTPActivity.this)
                //different call back methods based on funcationality
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    // if verification is failed
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(getApplicationContext(), "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("OTPActivity", "onVerificationFailed: " + e.getMessage(), e);
                        dialog.dismiss();

                    }

                    @Override
                    //when the code is sent
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                     //   verifyId;: Stores the verification ID for later use. The verification ID is a
                        //   unique identifier associated with the current phone number verification session.

                        Toast.makeText(getApplicationContext(),"your code is sent ",Toast.LENGTH_LONG).show();
                        super.onCodeSent(verifyId, forceResendingToken);
                        dialog.dismiss();
                        verificationId = verifyId;
                       // Shows the soft keyboard forcefully. This is done to ensure that the user can
                        // immediately start entering the received verification code.
                        InputMethodManager imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        binding.otpView.requestFocus();
                    }
                }).build();


        //instructing Firebase to start the process of sending a verification code to the specified
        // phone number, and the verification
        //process will proceed based on the configuration provided in the options object
        PhoneAuthProvider.verifyPhoneNumber(options);


        //otp viewer binding
        binding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                //when the otp is completed by the user then we have the verificationId it is same not sames as otp
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);


                //trying to login user with their inserted otp
                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    //if successfull
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            //Notification
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(OTPActivity.this, "My Notification");
                            builder.setContentTitle("Insta talk");
                            builder.setContentText("You are sucessfully verified");
                            builder.setSmallIcon(R.drawable.ic_launcher_background);

                            Toast.makeText(OTPActivity.this, "Yes calling this ", Toast.LENGTH_SHORT).show();

                            builder.setAutoCancel(true);

                            NotificationManagerCompat nm = NotificationManagerCompat.from(OTPActivity.this);



                            nm.notify(1, builder.build());


//going to the profile Activity
                            Intent intent = new Intent(OTPActivity.this, SetupProfileActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            Toast.makeText(OTPActivity.this, "Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });





    }
}