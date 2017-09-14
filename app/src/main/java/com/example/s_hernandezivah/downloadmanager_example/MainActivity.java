package com.example.s_hernandezivah.downloadmanager_example;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private long musicDownloadId,imageDownloadId;
    private DownloadManager downloadManager;
    private Button btnImage, btnMusic, btnStatus, btnCancel;
    private String downloadFilePath = "";
    private static final String URL_IMAGE = "https://www.androidtutorialpoint.com/wp-content/uploads/2016/09/Beauty.jpg";
    private static final String URL_MUSIC = "https://www.androidtutorialpoint.com/wp-content/uploads/2016/09/AndroidDownloadManager.mp3";
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

        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE },100);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    private long DownloadData (Uri uri, View view){
        long downloadreferences;

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        if(view.getId() == R.id.btnImage){
            btnImage.setEnabled(false);
            request.setTitle("Image Download");
            request.setDescription("Descargando Imagen");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"Test.jpg");
        }else if(view.getId() == R.id.btnMusic){
            btnMusic.setEnabled(false);
            request.setTitle("Music Download");
            request.setDescription("Descargando Track");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"Test.mp3");
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
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 25, 400);
            toast.show();
        }else{
            Toast toast = Toast.makeText(MainActivity.this,
                    "Image Download Status:"+ "\n" + statusText + "\n" +
                            reasonText,
                    Toast.LENGTH_SHORT);
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
            PendingIntent pIntent = null;

            NotificationCompat.Builder builderNotification =
                    new NotificationCompat.Builder(MainActivity.this);

            builderNotification.setSmallIcon(R.drawable.ic_download);

            if(referenceId == imageDownloadId) {
                notifyID = 1;
                btnImage.setEnabled(true);

                checkDownloadStatus(imageDownloadId,IMAGE);

                //File file = new File("storage/emulated/0/Download/","TestGama.jpg");
                File file = new File(downloadFilePath);
                Intent i = new Intent();
                i.setAction(android.content.Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.setDataAndType(Uri.fromFile(file), "image/*");
                /*Uri fileArchive = FileProvider.getUriForFile(MainActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider", file);*/
                //i.setDataAndType(fileArchive, "image/*");

                pIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);

                builderNotification.setContentTitle("Imagen descargada")
                        .setContentText("Presiona para abrir la imagen")
                        .setSubText("Subtexto")
                        .setContentIntent(pIntent)
                        .setAutoCancel(true);

            } else if(referenceId == musicDownloadId) {
                notifyID = 2;
                btnMusic.setEnabled(true);

                checkDownloadStatus(musicDownloadId,MUSIC);

                File file = new File(downloadFilePath);
                Intent i = new Intent();
                i.setAction(android.content.Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.setDataAndType(Uri.fromFile(file), "audio/*");

                pIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);

                builderNotification.setContentTitle("Track descargado")
                        .setContentText("Presiona para abrir el track")
                        .setSubText("Subtexto")
                        .setContentIntent(pIntent)
                        .setAutoCancel(true);
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
                Uri image_uri = Uri.parse(URL_IMAGE);
                imageDownloadId = DownloadData(image_uri,v);
                break;
            case R.id.btnMusic:
                Uri music_uri = Uri.parse(URL_MUSIC);
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
