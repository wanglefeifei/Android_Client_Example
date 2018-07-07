package network.b.bnet.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class BnetService extends Service {
    IBnetBinder iBnetBinder;
    private static T bNetT;

    private synchronized static T getTInstance() {
        if (bNetT == null) {
            bNetT = new T();
        }
        return bNetT;
    }

    public BnetService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        if (iBnetBinder == null) {
            iBnetBinder = new IBnetBinder();
        }
        return iBnetBinder;
        //        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    class IBnetBinder extends BnetAidlInterface.Stub {

        @Override
        public int create(String nWalletAddr, String masterAddr, int maskBit) throws RemoteException {
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getByName(masterAddr);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return getTInstance().create(nWalletAddr, inetAddress, maskBit);
        }

        @Override
        public int join(String nWalletAddr, String dWalletAddr, String deviceAddr, int maskBit) throws RemoteException {
            InetAddress inetAddressDevice = null;
            try {
                inetAddressDevice = InetAddress.getByName(deviceAddr);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return getTInstance().join(nWalletAddr, dWalletAddr, inetAddressDevice, maskBit);
        }

        @Override
        public int accept(String deviceAddr, int maskBit) throws RemoteException {
            InetAddress inetAddressDevice = null;
            try {
                inetAddressDevice = InetAddress.getByName(deviceAddr);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return getTInstance().accept(inetAddressDevice, maskBit);
        }

        @Override
        public int reject() throws RemoteException {
            return getTInstance().reject();
        }

        @Override
        public int leave() throws RemoteException {
            return getTInstance().leave();
        }

        @Override
        public String getRequest() throws RemoteException {
            return getTInstance().getRequest();
        }

        @Override
        public String getStatus() throws RemoteException {
            return getTInstance().getStatus();
        }

        @Override
        public void sendUdpMessage(byte[] data, String to, int port) throws RemoteException {
            InetAddress inetAddressTo = null;
            try {
                inetAddressTo = InetAddress.getByName(to);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            getTInstance().sendUdpMessage(data, inetAddressTo, port);
        }

        @Override
        public void onUdpMessage(byte[] data) throws RemoteException {
            getTInstance().onUdpMessage(data);
        }

        @Override
        public void sendTunMessage(byte[] data) throws RemoteException {
            getTInstance().sendTunMessage(data);
        }

        @Override
        public void onTunMessage(byte[] data) throws RemoteException {
            getTInstance().onTunMessage(data);
        }
    }

}
