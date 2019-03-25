package com.eprs.carchecker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ArrayList<String[]> categoryList = new ArrayList<String[]>();
    private List<CarTag> tagLists = new ArrayList<CarTag>();
    private List<Integer> carNumList = new ArrayList<>();
    private EditText txtCarNum;
    private ImageView imgResualt;
    private Button btnCheckNum;
    List<String[]> list = new ArrayList<String[]>();

    // camera recognize google api
    SurfaceView cameraView;
    TextView txtRec;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setPointer();

        txtCarNum = findViewById(R.id.txtCarNum);
        btnCheckNum = findViewById(R.id.btnCheckNum);
        imgResualt = findViewById(R.id.imgResualt);
        btnCheckNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    public void run() {
                        getTypedNum();

                    }
                }).start();
            }
        });
    }
    // Amikooo pushpush

    private void getTypedNum() {
        Log.e(TAG, "getTypedNum: " + "called");
        int inputName = Integer.parseInt(txtCarNum.getText().toString());
        for (int i = 0; i < carNumList.size(); i++) {
            int finalNum = carNumList.get(i);
            if (inputName == finalNum) {
                Log.e(TAG, "getTypedNum: " + "found!!! " + inputName + "  and  " + finalNum);
                txtCarNum.setText(inputName);
                imgResualt.setImageResource(R.drawable.v);
                imgResualt.setVisibility(View.VISIBLE);

                break;
            }
            if (inputName < finalNum) {
                Log.e(TAG, "getTypedNum: " + "NOT FOUND");
                imgResualt.setImageResource(R.drawable.x);
                imgResualt.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void setPointer() {
        new ReadFromUrl().execute();

        getCameraRecognaze();
        //erez
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case RequestCameraPermissionID:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }

    }

    private void getCameraRecognaze() {
        cameraView = findViewById(R.id.surfaceView);
        txtRec = findViewById(R.id.lblCarRec);
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.e(TAG, "getCameraRecognaze: " + "detector dependencies are not available yet now");
        } else {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA,},
                                RequestCameraPermissionID);
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {
                        txtRec.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuffer stringBuffer = new StringBuffer();
                                for (int i = 0; i < items.size(); ++i) {
                                    TextBlock item = items.valueAt(i);
                                    String strRec = item.getValue().replaceAll("\\D+", "");
//                                    stringBuffer.append(item.getValue());
                                    if (strRec.length() < 9 && strRec.length() > 6) {
                                        stringBuffer.append(strRec);
                                        stringBuffer.append("\n");
                                        txtRec.setText(strRec);
                                        int intRec = Integer.parseInt(strRec);
                                        Log.e(TAG, "run: " + intRec);
                                        // search the car number.
                                        txtRec.setText(intRec);
                                        new Thread(new Runnable() {public void run() { getTypedNum(); }}).start();
                                        cameraSource.stop();
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }
    }


    private class ReadFromUrl extends AsyncTask<String, Void, Void> {
        private static final String TAG = "ReadFromUrl";

        @Override
        protected Void doInBackground(String... strings) {
            String next[] = {"|"};
            URL url = null;

            try {
                url = new URL("https://data.gov.il/dataset/11369651-1c70-4d8f-8090-ee49354a7c52/resource/c8b9f9c8-4612-4068-934f-d4acd2e3c06e/download/rechev_nachimmot.gov.il.csv");
                InputStream input = url.openStream();
                InputStreamReader csvStreamReader = new InputStreamReader(input);
                CSVReader reader = new CSVReader(csvStreamReader);
                for (; ; ) {
                    next = reader.readNext();
                    if (next != null) {
                        list.add(next);
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < list.size(); i++) {
                categoryList.add((list.get(i)[0]).split("\\|"));
                CarTag carTag = new CarTag(categoryList.get(i)[0], categoryList.get(i)[1], categoryList.get(i)[2]);
                tagLists.add(carTag);
                if (categoryList.size() > 1) {
                    carNumList.add(Integer.valueOf(categoryList.get(i)[0]));
                }
            }

//            Log.e(TAG, "setPointer: " + tagLists.get(280000).getCarNumber());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e(TAG, "onPostExecute: " + carNumList);
            Collections.sort(carNumList);

        }


    }
}
