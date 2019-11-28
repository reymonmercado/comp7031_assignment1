package com.example.assignment1;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.gesture.Gesture;
import android.graphics.BitmapFactory;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.Bundle;

import java.io.IOException;

import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.view.View;
import android.content.Intent;
import android.provider.MediaStore;
import java.io.File;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.Button;
import android.view.MotionEvent;
import android.hardware.SensorManager;
import android.hardware.Sensor;


import java.util.List;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.gesture.GestureLibrary;
import android.gesture.GestureLibraries;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import java.util.ArrayList;




public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener
{

    String currentImagePath = null;
    private static final int IMAGE_REQUEST = 1;
    ImageView imageView;

    private ViewFlipper viewFlipper;
    Button rightBtn;
    Button leftBtn;
    Button speak;

    private GestureDetector gestureDetector;
    private GestureLibrary gLibrary;

    private Accelerometer accelerometer;

    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rightBtn = (Button) findViewById(R.id.rightBtn);
        leftBtn = (Button) findViewById(R.id.leftBtn);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        speak = (Button) findViewById(R.id.speak);

        CustomGestureDetector customGestureDetector = new CustomGestureDetector();
        gestureDetector = new GestureDetector(this, customGestureDetector);
        gLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!gLibrary.load()) {
            finish();
        }

        GestureOverlayView gOverlay =
                (GestureOverlayView) findViewById(R.id.gOverlay);
        gOverlay.addOnGesturePerformedListener(this);

        accelerometer = new Accelerometer(this);


        rightBtn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                viewFlipper.showNext();
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                viewFlipper.showPrevious();
            }
        });

        accelerometer.setListener(new Accelerometer.Listener() {
            @Override
            public void onTranslation(float tx, float ty, float tz)
            {
                if (tx > 1.0f)
                {
                    viewFlipper.showNext();
                }
                else if (tx < -1.0f)
                {
                    viewFlipper.showPrevious();
                }
            }
        });

        initializeTextToSpeech();
        initializeSpeechRecognizer();

        speak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {


                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.RECORD_AUDIO)) {

                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    // Permission has already been granted
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
                    speechRecognizer.startListening(intent);
                }

            }
        });

    }

    private void initializeSpeechRecognizer()
    {
        if (SpeechRecognizer.isRecognitionAvailable(this))
        {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener()
            {
                @Override
                public void onReadyForSpeech(Bundle bundle) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float v) {

                }

                @Override
                public void onBufferReceived(byte[] bytes) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int i) {

                }

                @Override
                public void onResults(Bundle bundle) {
                    List<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    processResult(results.get(0));

                }

                @Override
                public void onPartialResults(Bundle bundle) {

                }

                @Override
                public void onEvent(int i, Bundle bundle) {

                }
            });
        }
    }

    private void processResult(String command)
    {
        command = command.toLowerCase();

        // right button

        if(command.indexOf("right") != -1)
        {
            viewFlipper.showNext();
        }

        // left button
        else if(command.indexOf("left") != -1)
        {
            viewFlipper.showPrevious();
        }

    }

    private void initializeTextToSpeech()
    {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i)
            {
                if(textToSpeech.getEngines().size() == 0)
                {
                    Toast.makeText(MainActivity.this, "There is no TTS engine on your device", Toast.LENGTH_LONG).show();

                    finish();
                }
                else{
                    textToSpeech.setLanguage(Locale.US);
                    speak("Hello! I am ready.");
                }
            }
        });
    }

    @SuppressLint("NewApi")
    private void speak(String message)
    {
        if(Build.VERSION.SDK_INT >= 21)
        {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else{
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void captureImage(View view)
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            // Create the File where the photo should go
            File photoFile = null;
            try
            {
                photoFile = getImageFile();
            }
            catch (IOException e)
            {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null)
            {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, IMAGE_REQUEST);
            }
        }
    }


    private File getImageFile() throws IOException
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentImagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK)
        {
            imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(BitmapFactory.decodeFile(currentImagePath));

        }
    }

    // The Gesture Detector that utilizes the onFling method that changes the image views when
    // swiping through the images
    class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {

            // Swipe left (next)
            if (e1.getX() > e2.getX())
            {
                viewFlipper.showNext();
            }

            // Swipe right (previous)
            if (e1.getX() < e2.getX())
            {
                viewFlipper.showPrevious();
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    public void onGesturePerformed(GestureOverlayView overlay, Gesture
            gesture) {
        ArrayList<Prediction> predictions =
                gLibrary.recognize(gesture);

        if (predictions.size() > 0 && predictions.get(0).score > 1.0) {

            String action = predictions.get(0).name;

            if ("next".equalsIgnoreCase(action)) {
                viewFlipper.showNext();
            } else if ("previous".equalsIgnoreCase(action)) {
                viewFlipper.showPrevious();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        gestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        accelerometer.register();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        accelerometer.unregister();

        textToSpeech.shutdown();
    }


}
