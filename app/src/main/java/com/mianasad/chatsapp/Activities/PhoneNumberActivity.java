package com.mianasad.chatsapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.mianasad.chatsapp.databinding.ActivityPhoneNumberBinding;

import java.util.Locale;

public class PhoneNumberActivity extends AppCompatActivity implements SensorEventListener {

    ActivityPhoneNumberBinding binding;
    SensorManager  sensorManager;
    Sensor sensor;
    Context context;
    FirebaseAuth auth;
    boolean success;
TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());

        //by this binding.get root we don't need to use find view by id in every method
        setContentView(binding.getRoot());

        //setting the sensor for light and adjusting the brightness
        sensorManager=(SensorManager) getSystemService(Service.SENSOR_SERVICE);
        sensor=sensorManager.getDefaultSensor(sensor.TYPE_LIGHT);


        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS)
                {
                    tts.setLanguage(Locale.US);
                    tts.setSpeechRate(1.0f);
                    tts.speak("Welcome to WhatsApp Chatting Application Please write your phone number Here",TextToSpeech.QUEUE_ADD,null );


                }
            }
        });
        //getting the firebase auth instance
        auth = FirebaseAuth.getInstance();


        //checking wether the current user is null or not means wether the user is already logged in
        //or not if yes it logged it will redirected towards the Main Activity
        if(auth.getCurrentUser() != null) {
            Intent intent = new Intent(PhoneNumberActivity.this, MainActivity.class);


            startActivity(intent);


            finish();
        }
//hiding the Action bar for phone Activity
        getSupportActionBar().hide();
//putting focus on the phone Activity box
        binding.phoneBox.requestFocus();
//when the button is pressed
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhoneNumberActivity.this, OTPActivity.class);

                //picking up the phone number and going for the next Activity OTP Activity
                intent.putExtra("phoneNumber", binding.phoneBox.getText().toString());

                startActivity(intent);
            }
        });





    }

    private  void permission(){

        boolean value;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

            value= Settings.System.canWrite(getApplicationContext());
            if(value){

                success=true;
            }
            else{

                Intent intent =new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:"+getApplicationContext().getPackageName()));

                startActivityForResult(intent,100);


            }

        }
    }
    protected  void onActivityResult(int requestcode,Intent intent)
    {
        if(requestcode==100)
        {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            {
                boolean values=Settings.System.canWrite(getApplicationContext());
                if(values)
                {
                    success=true;
                }
else{
    Toast.makeText(getApplicationContext(),"Permission is not granted",Toast.LENGTH_LONG).show();

                }
            }
        }
    }
    protected  void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(this);

    }
    protected  void onResume(){

        super.onResume();
        sensorManager.registerListener(this,sensor,sensorManager.SENSOR_DELAY_NORMAL);

    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        context=getApplicationContext();
        if(sensorEvent.sensor.getType()==Sensor.TYPE_LIGHT)
        {
if(sensorEvent.values[0]<15)
{

    Toast.makeText(getApplicationContext(),"Sensor value is "+sensorEvent.values[0],Toast.LENGTH_LONG).show();
    permission();
    setBrightness(240);
}
else if(sensorEvent.values[0]>80)
{
    permission();

    Toast.makeText(getApplicationContext(),"Sensor value is down"+sensorEvent.values[0],Toast.LENGTH_LONG).show();
    setBrightness(50);
}
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    //function for adjusting brightness
    private  void setBrightness(int bright){

        if(bright<0)
        {
            bright=0;
        }
        else if(bright>255){
            bright=255;
        }
        ContentResolver contentResolver =getApplicationContext().getContentResolver();
        Settings.System.putInt(contentResolver,Settings.System.SCREEN_BRIGHTNESS,bright);
    }

}