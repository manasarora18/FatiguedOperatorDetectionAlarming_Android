package com.project.fatigueddetection;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread timer= new Thread(){
            public void run(){
                try {
                    sleep(1000);
                }catch (InterruptedException e){
                    Log.d("Error",e.toString());
                    Thread.currentThread().interrupt();
                }
                finally {
                    applicationStart();
                }
            }
        };
        timer.start();
    }

    private void applicationStart() {
        Intent detectIntent=new Intent(MainActivity.this,DetectActivity.class);
        startActivity(detectIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}