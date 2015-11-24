package com.example.rpakitin.omnipotentmagicball;

import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    float[] gravity = new float[3];
    float[] accel = new float[3];
    private static final float ALPHA = 0.80f; //weighing factor used by the low pass filter
    private static final float VERTICAL_TOL = 0.3F;
    private static final String TAG = "OMNI";
    private SensorManager manager;
    private long lastUpdate;
    private MediaPlayer popPlayer;
    private MediaPlayer backgroundPlayer;
    private TextToSpeech tts;
    ArrayList<String> answers= new ArrayList<String>();
    Random rSpeech;
    String rResult;

    private boolean isDown = false;
    private boolean isUp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
        popPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor afd = getAssets().openFd("ballpointpenclick.mp3");
            popPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            popPlayer.prepare();
            afd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        backgroundPlayer = MediaPlayer.create(this, R.raw.blacklawnfinale);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.US);
            }
        }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);

        answers.add("Yes");
        answers.add("No");
        answers.add("Maybe");
        answers.add("I don't think so");
        answers.add("Only if you try harder");
        answers.add("Lord no");
        answers.add("That's very unlikely");
        answers.add("I'm not sure");
        answers.add("Of course");
        answers.add("No way");
        answers.add("Try again");
        answers.add("Ask me later");
        answers.add("You wish");
        answers.add("Definitely");
        answers.add("Stop asking me questions");
        answers.add("What do you think?");
        answers.add("I will bet you twenty dollars that it will happen");
        answers.add("That is not happening anytime soon");
        answers.add("Keep dreaming and it will happen");
        answers.add("Are you tired of asking me questions yet?");

        Random rSpeech = new Random();
        int random = rSpeech.nextInt(20);
        rResult = "";

        backgroundPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
        backgroundPlayer.pause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        gravity[0] = lowPass(event.values[0], gravity[0]);
        gravity[1] = lowPass(event.values[1], gravity[1]);
        gravity[2] = lowPass(event.values[2], gravity[2]);

        // TODO confirm that the high pass filtering works before using
        accel[0] = highPass(event.values[0], accel[0]);
        accel[1] = highPass(event.values[1], accel[1]);
        accel[2] = highPass(event.values[2], accel[2]);

//        Log.i(TAG, "gravity[]=" + gravity[0] + ' ' + gravity[1] + ' ' + gravity[2]);

        long actualTime = System.currentTimeMillis();
        if (actualTime - lastUpdate > 100) {
            if (inRange(gravity[2], -9.81f, VERTICAL_TOL)) {

                Log.i(TAG, "Down");

                if (!isDown) {
                    backgroundPlayer.setVolume(0.1f, 0.1f);
                    popPlayer.start();
                    tts.speak("Ask me anything", TextToSpeech.QUEUE_FLUSH, null);
                    backgroundPlayer.setVolume(0.1f, 0.1f);
                }
                isDown = true;
                isUp = false;
            } else if (inRange(gravity[2], 9.81f, VERTICAL_TOL)) {
                if (!isUp) {
                    backgroundPlayer.setVolume(0.1f, 0.1f);
                    Log.i(TAG, "Up");
                    tts.speak(answers.get(20), TextToSpeech.QUEUE_FLUSH, null);
                    backgroundPlayer.setVolume(0.1f, 0.1f);
                }
                isUp = true;
                isDown = false;
            } else {
                Log.i(TAG, "In Between");
                isDown = false;
                isUp = false;
            }
            lastUpdate = actualTime;
        }
    }
    private boolean inRange(float value, float target, float tol) {
        return value >= target - tol && value <= target + tol;
    }

    private float lowPass(float current, float gravity) {
        return current * (1-ALPHA) + gravity * ALPHA;
    }

    private float highPass(float current, float gravity) {
        return current - gravity;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
