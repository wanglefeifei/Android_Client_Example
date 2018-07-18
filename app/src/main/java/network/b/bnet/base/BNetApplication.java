package network.b.bnet.base;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import network.b.bnet.model.BnetServiceJoinParams;
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
    private boolean serviceBind = false;

    public BnetAidlInterface getBnetAidlInterface() {
        return bnetAidlInterface;
    }

    private BnetAidlInterface bnetAidlInterface;
    private Intent mIntentConnectorService;
    private BnetServiceJoinParams bnetServiceJoinParams;

    @Override
    public void onCreate() {
        super.onCreate();
        bNetApplication = this;
        context = getApplicationContext();


    }

    public static BNetApplication getInstance() {
        return bNetApplication;
    }

    public Context getContext() {
        return context;
    }

    public void DestoryBnetService() {
        if (serviceConnection != null && serviceBind) {
            unbindService(serviceConnection);
            serviceBind = false;
        }
        if (bnetAidlInterface != null) {
            try {
                bnetAidlInterface.leave();
                stopService(mIntentConnectorService);
                bnetAidlInterface = null;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void startAndBindService() {
        Intent bnetService = new Intent(this, BnetService.class);
        startService(bnetService);


        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                bnetAidlInterface = BnetAidlInterface.Stub.asInterface(iBinder);
                serviceBind = true;
                StartBnetServiceJoin();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        mIntentConnectorService = new Intent(getApplicationContext(), BnetService.class);
        bindService(mIntentConnectorService, serviceConnection, BIND_AUTO_CREATE);
    }

    public void BnetServiceJoin(String nWalletAddr, String dWalletAddr, String deviceAddr, int maskBit) {
        bnetServiceJoinParams = new BnetServiceJoinParams(nWalletAddr, dWalletAddr, deviceAddr, maskBit);
        if (bnetAidlInterface == null || !serviceBind) {
            startAndBindService();
        } else {
            StartBnetServiceJoin();
        }
    }

    private void StartBnetServiceJoin() {
        if (bnetServiceJoinParams != null && bnetAidlInterface != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        bnetAidlInterface.join(bnetServiceJoinParams.getnWalletAddr(), bnetServiceJoinParams.getdWalletAddr(), bnetServiceJoinParams.getDeviceAddr(), bnetServiceJoinParams.getMaskBit());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            try {
                bnetAidlInterface.CStartService();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}
