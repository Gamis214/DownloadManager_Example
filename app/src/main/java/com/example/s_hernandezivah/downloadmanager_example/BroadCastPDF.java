
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import java.io.File;

/****** CLASE DE BORADCAST PARA USO CON VERSIONES MAYORES A API 27 -> N ******/
public class BroadCastPDF extends BroadcastReceiver {

    private String fileName;
    private String CHANNEL_ID = "CH_001";
    private CharSequence name = "CH_BROADCAST";
    private String Description = "D_BROADCAST";

    /**
     * Constructor
     * @param downloadFilePath nombre del archivo de descarga
     */
    public BroadCastPDF(String fileName){
        this.fileName = fileName;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //-->Generamos la ruta donde se ubica el archivo descargado
        File filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/",fileName);
        //-->Creamos el intent dependiendo de la version para agregarlo a la notificacion y abrirlo al dar click
        Intent i = new Intent();
        i.setAction(android.content.Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkURI = FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".provider", filePath);
            i.setDataAndType(apkURI, "application/pdf");
        } else {
            i.setDataAndType(Uri.fromFile(filePath), "application/pdf");
        }
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, i, 0);

        //--> Creamos la notificacion **************************************************************
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.app_icon_miclaro)
                .setContentTitle("Factura Descargada")
                .setContentText("Abrir Factura")
                .setContentIntent(pIntent)
                .setAutoCancel(true);

        mNotificationManager.notify(1001,builder.build());
    }

}
