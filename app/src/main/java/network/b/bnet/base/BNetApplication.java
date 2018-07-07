package network.b.bnet.base;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import network.b.bnet.model.User;
import network.b.bnet.service.BnetAidlInterface;
import network.b.bnet.service.BnetService;

/**
 * Created by jack.ma on 2018/6/18.
 */

public class BNetApplication extends Application {
    private Context context;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private User user;
    private static BNetApplication bNetApplication = null;


    private static ServiceConnection serviceConnection;

    public BnetAidlInterface getBnetAidlInterface() {
        return bnetAidlInterface;
    }

    private BnetAidlInterface bnetAidlInterface;
    private Intent mIntentConnectorService;

    @Override
    public void onCreate() {
        super.onCreate();
        bNetApplication = this;
        context = getApplicationContext();

        Intent bnetService = new Intent(this, BnetService.class);
        startService(bnetService);


        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                bnetAidlInterface = BnetAidlInterface.Stub.asInterface(iBinder);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        mIntentConnectorService = new Intent(getApplicationContext(), BnetService.class);
        bindService(mIntentConnectorService, serviceConnection, BIND_AUTO_CREATE);
    }

    public static BNetApplication getInstance() {
        return bNetApplication;
    }

    public Context getContext() {
        return context;
    }
}
