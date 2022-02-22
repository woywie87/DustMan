package com.example.pitcairn.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.Math.toIntExact;
import com.example.pitcairn.myapplication.Utils;
public class MainActivity extends AppCompatActivity
{


    private static final int REQUEST_WRITE_STORAGE_REQUEST_CODE = 0 ;
    private int percent;
    private long totalSize;
    private long avSize;
    private long finalFileSize;
    private static File dirToSave =  new File(Environment.getExternalStorageDirectory(), "DustMan");
    private ProgressBar progressbar;
    private int a = 0;
    private int m = 0;
    private int i = 0;
    private  Long tsLong;
    private String ts;
    private Button btnGenerate;
    private Button btnDelete;
    private NumberPicker np;
    private TextView txtFileSize;
    private TextView txtTotalSize;
    private TextView txtAvailableSize;

    volatile boolean running = false;
    private SaveFile mTask;



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestAppPermissions();
        running = false;
        mTask = new SaveFile();



        btnGenerate = findViewById(R.id.btnGenerate);
        btnDelete = findViewById(R.id.btnDelete);
        progressbar = findViewById(R.id.progressBarSave);
        np = findViewById(R.id.nmbPercent);
        txtFileSize = findViewById(R.id.txtFileSize);
        txtTotalSize = findViewById(R.id.txtTotal);
        txtAvailableSize= findViewById(R.id.txtAvailable);

        np.setMinValue(1);

        View.OnClickListener listener = new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void onClick(View view)
            {
                if (running == false)
                {
                    mTask = new SaveFile();
                    mTask.execute(finalFileSize);
                }
                else
                {
                    mTask.cancel(true);
                    btnGenerate.setText("CANCELLING....");
                    btnGenerate.setClickable(false);
                }
            }
        };
        btnGenerate.setOnClickListener(listener);


        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                percent = newVal;
                long tmp = ((100 - percent) * totalSize)/100;
                finalFileSize = tmp-(totalSize-avSize);
                txtFileSize.setText(Utils.formatSize(finalFileSize));
            }
        });

        CalculateSize();


    }





    private class SaveFile  extends AsyncTask<Long, Integer, Void> {

        @Override
        protected void onPreExecute()
        {
            running = true;
            btnGenerate.setText("STOP");
            btnDelete.setClickable(false);
            np.setEnabled(false);

        }

        @TargetApi(Build.VERSION_CODES.N)
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(Long...params) {
           Long size2 = params[0];

            try {

                dirToSave.mkdir();

                a = 0;
                m = 0;
                long test;

                a =  (int)Math.ceil(size2/50000000.0);
                test = size2/a;
                m= toIntExact(test);

                tsLong = System.currentTimeMillis()/1000;
                ts = tsLong.toString();
                float b = 100/a;

                for (i = 0; i < a; i++)
                {
                    File f2= new File(dirToSave , "DUMP_" + ts  + Integer.toString(i) + ".dat");
                    f2.createNewFile();
                    int x= (int)((float)i*b);
                    publishProgress(x);

                    try (FileOutputStream out = new FileOutputStream(f2)) {
                        byte[] bytes = new byte[m];
                        out.write(bytes);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                // Error Log
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
                progressbar.setProgress(values[0]);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        protected void onPostExecute(Void args) {

            progressbar.setProgress(100);
            CalculateSize();
            Toast.makeText(getApplicationContext(),"Files generated",   Toast.LENGTH_LONG ).show();
            btnGenerate.setText("GENERATE");
            btnDelete.setClickable(true);
            np.setEnabled(true);
            running = false;
        }


        @Override
        protected void onCancelled() {

            running = false;
            btnGenerate.setText("GENERATE");
            btnGenerate.setClickable(true);
            btnDelete.setClickable(true);
            np.setEnabled(true);
            progressbar.setProgress(0);

        }


    }


    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestAppPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (hasReadPermissions() && hasWritePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_WRITE_STORAGE_REQUEST_CODE); // your request code
    }







    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    void CalculateSize()
    {
        totalSize = Utils.getTotalExternalMemorySize();
        avSize = Utils.getAvailableExternalMemorySize();

        txtTotalSize.setText(Utils.formatSize(totalSize));
        txtAvailableSize.setText(Utils.formatSize(avSize));

        double c = ((double) avSize / totalSize) * 100;
        percent = (int)c;

        np.setMaxValue(percent);
        np.setValue(percent);

        long tmp = ((100 - percent) * totalSize)/100;
        finalFileSize = tmp-(totalSize-avSize);
        txtFileSize.setText(Utils.formatSize(finalFileSize));

    }

    public void CleanDirectory()
    {
        for(File tempFile : dirToSave.listFiles()) {
            tempFile.delete();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void btnDeleteClick(View v)
    {
        CleanDirectory();
        Toast.makeText(getApplicationContext(),"Deleted",   Toast.LENGTH_LONG ).show();
        CalculateSize();

    }



}
