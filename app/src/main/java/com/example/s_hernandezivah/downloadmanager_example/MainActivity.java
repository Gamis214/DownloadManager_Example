package com.example.s_hernandezivah.downloadmanager_example;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Uri image_uri,music_uri;
    private DownloadManager downloadManager;
    private long musicDownloadId,imageDownloadId;
    private Button btnImage, btnMusic, btnStatus, btnCancel;
    private static final int IMAGE = 0, MUSIC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnImage    =  (Button)findViewById(R.id.btnImage);
        btnMusic    =  (Button)findViewById(R.id.btnMusic);
        btnStatus   =  (Button)findViewById(R.id.btnCheckStatus);
        btnCancel   =  (Button)findViewById(R.id.btnCancel);
        btnImage.setOnClickListener(this);
        btnMusic.setOnClickListener(this);
        btnStatus.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnStatus.setEnabled(false);
        btnCancel.setEnabled(false);
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver,intentFilter);

    }

    private long DownloadData (Uri uri, View view){
        long downloadreferences;

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        if(view.getId() == R.id.btnImage){
            btnImage.setEnabled(false);
            request.setTitle("Image Download");
            request.setDescription("Descargando Imagen");
            request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS,"Test.jpg");
        }else if(view.getId() == R.id.btnMusic){
            btnMusic.setEnabled(false);
            request.setTitle("Music Download");
            request.setDescription("Descargando Track");
            request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS,"Test.mp3");
        }

        btnStatus.setEnabled(true);
        btnCancel.setEnabled(true);
        downloadreferences = downloadManager.enqueue(request);

        return downloadreferences;
    }

    private void checkDownloadStatus(long downloadID,int type){

        DownloadManager.Query downloadQuery = new DownloadManager.Query();

        switch (type){
            case IMAGE:
                downloadQuery.setFilterById(downloadID);
                break;
            case MUSIC:
                downloadQuery.setFilterById(downloadID);
                break;
        }

        Cursor cursor = downloadManager.query(downloadQuery);
        if(cursor.moveToFirst()){
            DownloadStatus(cursor, downloadID);
        }

    }

    private void DownloadStatus(Cursor cursor, long downloadID) {
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);

        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);

        //int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
        String downloadFilePath = null;
        String downloadFileLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
        if (downloadFileLocalUri != null) {
            File mFile = new File(Uri.parse(downloadFileLocalUri).getPath());
            downloadFilePath = mFile.getAbsolutePath();
        }

        String filename = downloadFilePath;

        String statusText = null, reasonText = null;

        switch (status){
            case DownloadManager.STATUS_FAILED:
                statusText = "STATUS_FAILED";
                switch (reason){
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "ERROR_UNKNOWN";
                        break;
                }
                break;
            case DownloadManager.STATUS_PAUSED:
                statusText = "STATUS_PAUSED";
                switch (reason){
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                break;
            case DownloadManager.STATUS_PENDING:
                statusText = "STATUS_PENDING";
                break;
            case DownloadManager.STATUS_RUNNING:
                statusText = "STATUS_RUNNING";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                statusText = "STATUS_SUCCESSFUL";
                reasonText = "Filename: \n" + filename;
                break;
        }

        if(downloadID == musicDownloadId){
            Toast toast = Toast.makeText(MainActivity.this,
                    "Music Download Status:" + "\n" + statusText + "\n" +
                            reasonText,
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 25, 400);
            toast.show();
        }else{
            Toast toast = Toast.makeText(MainActivity.this,
                    "Image Download Status:"+ "\n" + statusText + "\n" +
                            reasonText,
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 25, 400);
            toast.show();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                }
            }, 3000);
        }

    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            int notifyID = 0;

            NotificationCompat.Builder builderNotification =
                    new NotificationCompat.Builder(MainActivity.this);

            builderNotification.setSmallIcon(R.drawable.ic_download);

            if(referenceId == imageDownloadId) {
                notifyID = 1;
                btnImage.setEnabled(true);
                builderNotification.setContentTitle("Imagen descargada")
                        .setContentText("Presiona para abrir la imagen")
                        .setSubText("Subtexto")
                        .setTicker("DESCARGA -- DESCARGA")
                        .setAutoCancel(true);
                /*Toast toast = Toast.makeText(MainActivity.this,
                        "Image Download Complete", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();*/
            } else if(referenceId == musicDownloadId) {
                notifyID = 2;
                btnMusic.setEnabled(true);
                builderNotification.setContentTitle("Track descargado")
                        .setContentText("Presiona para abrir el track")
                        .setSubText("Subtexto")
                        .setTicker("DESCARGA -- DESCARGA")
                        .setAutoCancel(true);
                /*Toast toast = Toast.makeText(MainActivity.this,
                        "Music Download Complete", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();*/
            }
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(notifyID,builderNotification.build());
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnImage:
                image_uri = Uri.parse("https://www.androidtutorialpoint.com/wp-content/uploads/2016/09/Beauty.jpg");
                imageDownloadId = DownloadData(image_uri,v);
                break;
            case R.id.btnMusic:
                music_uri = Uri.parse("https://www.androidtutorialpoint.com/wp-content/uploads/2016/09/AndroidDownloadManager.mp3");
                musicDownloadId = DownloadData(music_uri,v);
                break;
            case R.id.btnCheckStatus:
                checkDownloadStatus(musicDownloadId,MUSIC);
                checkDownloadStatus(imageDownloadId,IMAGE);
                break;
            case R.id.btnCancel:
                downloadManager.remove(imageDownloadId);
                downloadManager.remove(musicDownloadId);
                break;
        }
    }
}
