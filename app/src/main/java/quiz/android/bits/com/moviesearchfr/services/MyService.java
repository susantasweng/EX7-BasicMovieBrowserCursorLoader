package quiz.android.bits.com.moviesearchfr.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(this, "MyService oncreate", Toast.LENGTH_SHORT);
        Log.d("", "MyService oncreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "onStartCommend of MyService", Toast.LENGTH_LONG);
        Log.d("", "MyService onStartCommand" + "intent = " + intent.getStringExtra("number"));
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "MyService is destroyed", Toast.LENGTH_SHORT);
        Log.d("", "MyService is destroyed");
    }

    public class LocalBinder extends Binder {

        public MyService getService() {
            return MyService.this;
        }
    }
}
