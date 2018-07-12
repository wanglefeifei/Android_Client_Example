package network.b.bnet.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import network.b.bnet.R;

public class BnetService extends VpnService {
    private static final String TAG = BnetService.class.getSimpleName();
    private static final String VPN_ADDRESS = "10.208.0.1"; // Only IPv4 support for now
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
    // private static final String VPN_ROUTE = "10.0.0.0"; // Intercept everything
    public static final String BROADCAST_VPN_STATE = "network.b.VPN_STATE";
    private static boolean isRunning = false;
    private ParcelFileDescriptor vpnInterface = null;
    private PendingIntent pendingIntent;
    private ConcurrentLinkedQueue<Packet> deviceToNetworkUDPQueue;
    private ConcurrentLinkedQueue<Packet> deviceToNetworkTCPQueue;
    private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;
    private ExecutorService executorService;
    private Selector udpSelector;
    private Selector tcpSelector;


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

    @Override
    public void onDestroy() {
        isRunning = false;
        executorService.shutdownNow();
        cleanup();
        super.onDestroy();
    }

    private void startVpn() {
        //protect m_udpSocket not be block by tun
        protect(Global.m_udpSocket);
        //start VPN
        isRunning = true;
        setupVPN();
        try {
            udpSelector = Selector.open();
            tcpSelector = Selector.open();
            deviceToNetworkUDPQueue = new ConcurrentLinkedQueue<Packet>();
            deviceToNetworkTCPQueue = new ConcurrentLinkedQueue<Packet>();
            networkToDeviceQueue = new ConcurrentLinkedQueue<ByteBuffer>();
            executorService = Executors.newFixedThreadPool(5);
            executorService.submit(new UDPInput(networkToDeviceQueue, udpSelector));
            executorService.submit(new UDPOutput(deviceToNetworkUDPQueue, udpSelector, this));
            executorService.submit(new TCPInput(networkToDeviceQueue, tcpSelector));
            executorService.submit(new TCPOutput(deviceToNetworkTCPQueue, networkToDeviceQueue, tcpSelector, this));
            //build vpn
            Global.vpnFileDescriptor = vpnInterface.getFileDescriptor();
            executorService.submit(new LocalVPNService.VPNRunnable(Global.vpnFileDescriptor,
                    deviceToNetworkUDPQueue, deviceToNetworkTCPQueue, networkToDeviceQueue));
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_VPN_STATE).putExtra("running", true));
            Log.i(TAG, "Started");
            //protect(t.m_udpSocket);
        } catch (IOException e) {
            // TODO: Here and elsewhere, we should explicitly notify the user of any errors
            // and suggest that they stop the service, since we can't do it ourselves
            Log.e(TAG, "Error starting service", e);
            cleanup();
        }
    }

    private void cleanup() {
        deviceToNetworkTCPQueue = null;
        deviceToNetworkUDPQueue = null;
        networkToDeviceQueue = null;
        ByteBufferPool.clear();
        closeResources(udpSelector, tcpSelector, vpnInterface);
    }

    // TODO: Move this to a "utils" class for reuse
    private static void closeResources(Closeable... resources) {
        for (Closeable resource : resources) {
            try {
                resource.close();
            } catch (IOException e) {
                // Ignore
            }
        }
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
            byte ip[] = new byte[]{0, 0, 0, 0};
            InetAddress expectAddress = null;
            try {
                expectAddress = InetAddress.getByAddress(ip);
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            nWalletAddr = "172M8JQj7hh1Uf1sYvTf8NtT9vwxJTbRXg";
            dWalletAddr = "172M8JQj7hh1Uf1sYvTf8NtT9vwxJT1234";
            maskBit = 32;
            return getTInstance().join(nWalletAddr, dWalletAddr, expectAddress, maskBit);
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
        public int getStatus() throws RemoteException {
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

        @Override
        public void CStartService() throws RemoteException {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3 * 1000);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    startVpn();
                }
            }).start();
        }
    }


    private void setupVPN() {
        if (vpnInterface == null) {
            Builder builder = new Builder();
            builder.addAddress(VPN_ADDRESS, 32);
            builder.addRoute(VPN_ROUTE, 0);
            builder.setMtu(1300);
            builder.addDnsServer("8.8.8.8");//need read from config msg
            vpnInterface = builder.setSession(getString(R.string.app_name)).setConfigureIntent(pendingIntent).establish();
        }
        // protect(1);
    }

}
