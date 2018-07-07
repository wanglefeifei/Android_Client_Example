package network.b.bnet.service;//package network.b;

/*
Reference:
	https://www.jb51.net/article/118050.htm

Low-Level API:
	VPN data I/O:
		FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
		FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();

	WAN data I/O:
		protect(my_UDPSocket);

Process:
	Start()
		Get configuration via https://d.vin/t.php?w=xxxxxx
		Register H-Node via UDP

	Run()
		Forward UDP to tun
		Forword tun to UDP
		Send heartbeat via UDP
*/

import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

///// This is callback API for UdpRecvThread
interface UdpSocketEvent {
    void onUdpMessage(byte[] data);
}

///// This is UDP receiving thread.
class UdpRecvThread {
    boolean m_IsReceiving = false;   //Keep receiving status

    public void startRecv(final DatagramSocket sock, final UdpSocketEvent evt) {
        m_IsReceiving = true;
        new Thread(new Runnable() {
            public void run() {
                while (m_IsReceiving) {
                    try {
                        byte[] inBuff = new byte[2000];  //The max size for packet is 2000
                        DatagramPacket inPacket = new DatagramPacket(inBuff, inBuff.length);
                        sock.receive(inPacket);  //block operation
                        evt.onUdpMessage(inBuff);
                    } catch (IOException e) {
                        System.out.println("recv UDP socket failed");
                    }
                }
                System.out.println("UdpRecvThread is stopped.");
            }
        }).start();
    }

    public void stopRecv() {
        m_IsReceiving = false;
    }
}

///// This is callback API for TunRecvThread
interface TunSocketEvent {
    void onTunMessage(byte[] data);
}

///// This is Tun receiving thread.
class TunRecvThread extends VpnService {
    boolean m_IsReceiving = false;   //Keep receiving status
    ParcelFileDescriptor m_vpnInterface;
    FileInputStream m_in = null;
    FileOutputStream m_out = null;

    public void startRecv(String lanAddr, String dns, final TunSocketEvent evt) {
        m_IsReceiving = true;
        //setup VpnService
        Builder builder = new Builder();
        builder.setMtu(1300);
        builder.addAddress(lanAddr, 32);
        builder.addDnsServer(dns);
        builder.addRoute("0.0.0.0", 0);
        m_vpnInterface = builder.establish();
        //get VpnService I/O API
        m_in = new FileInputStream(m_vpnInterface.getFileDescriptor());
        m_out = new FileOutputStream(m_vpnInterface.getFileDescriptor());
        //start receiving
        new Thread(new Runnable() {
            public void run() {
                while (m_IsReceiving) {
                    try {
                        byte[] inBuff = new byte[2000];  //The max size for packet is 2000
                        // Allocate the buffer for a single packet.
                        int length = m_in.read(inBuff);
                        evt.onTunMessage(inBuff);
                    } catch (IOException e) {
                        System.out.println("recv TUN socket failed");
                    }
                }
                System.out.println("TunRecvThread is stopped.");
            }
        }).start();
    }

    public void stopRecv() {
        m_IsReceiving = false;
        //m_in.close();
        //m_out.close();
    }

    public void send(byte[] data) {
        if (m_out != null) {
            try {
                m_out.write(data);
            } catch (IOException e) {
                System.out.println("send TUN socket failed");
            }
        }
    }
}

///// This is Main Thread.
public class T implements Runnable, UdpSocketEvent, TunSocketEvent {
    //Wallet
    private String m_nWalletAddr = "";  //Network wallet ID
    private String m_dWalletAddr = "";  //Device wallet ID
    //UDP
    private DatagramSocket m_udpSocket;    //socket
    private UdpRecvThread m_udpRecvThread = new UdpRecvThread();  //thread
    //Tun
    private TunRecvThread m_tunRecvSocket = new TunRecvThread();  //Tun socket
    //Status
    private String m_status = "Inited";  //hefer status
    //Config
    private String m_hnodeIP = "139.162.103.83";     //IP of H-Node
    private int m_hnodePort1 = 15555;  //Port 1 of H-Node
    private int m_hnodePort2 = 17777;  //Port 2 of H-Node
    private String m_lanAddr = "10.200.0.1";  //LAN addr get from H-Node
    private String m_dns = "8.8.8.8";         //DNS get from H-Node

    //create a new network named as nWalletAddr, and the master's IP is masterAddr/maskBit.
    //return value: if succeed, return 0.
    public int create(String nWalletAddr, InetAddress masterAddr, int maskBit) {
        //create a new network
        return 0;
    }

    //join an existed network named as nWalletAddr, device wallet is dWalletAddr,
    //	and the expected's IP is deviceAddr/maskBit.
    //return value: if succeed, return 0.
    public int join(String nWalletAddr, String dWalletAddr, InetAddress deviceAddr, int maskBit) {
        //join an existed network
        try {
            // read configuratin from H-Node
            String config = getConfiguration("dsfvadsiuhfia");
            System.out.println(config);
            //save to m_hnodeIP ...
            m_status = "Configed";
            // Start listening to H-node
            m_udpSocket = new DatagramSocket(0);
            m_udpRecvThread.startRecv(m_udpSocket, this);
            // Register to H-node
            byte dns[] = new byte[]{(byte) 0xb2, 0x28, 0x01, 0x00, 0x00, 0x01,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x31, 0x34, 0x32, 0x02,
                    0x33, 0x31, 0x03, 0x32, 0x31, 0x37, 0x03, 0x31, 0x37, 0x32, 0x07,
                    0x69, 0x6e, 0x2d, 0x61, 0x64, 0x64, 0x72, 0x04, 0x61, 0x72, 0x70,
                    0x61, 0x00, 0x00, 0x0c, 0x00, 0x01};
            byte ip[] = new byte[]{8, 8, 8, 8};
            InetAddress google = InetAddress.getByAddress(ip);
            sendUdpMessage(dns, google, 53);
            m_status = "Connecting";
            // start forwording
            m_tunRecvSocket.startRecv(m_lanAddr, m_dns, this);
            m_tunRecvSocket.protect(m_udpSocket);
        } catch (IOException e) {
            System.out.println("create UDP socket failed");
            return 1;
        }
        return 0;
    }

    //master accept a request, and give the device with deviceAddr/maskBit.
    //return value: if succeed, return 0.
    public int accept(InetAddress deviceAddr, int maskBit) {
        //master accept a request
        return 0;
    }

    //master reject the request
    //return value: if succeed, return 0.
    public int reject() {
        //master reject the request
        return 0;
    }

    //the device leave the network.
    //return value: if succeed, return 0.
    public int leave() {
        //the device leave the network.
        m_udpRecvThread.stopRecv();
        m_udpSocket.close();
        m_tunRecvSocket.stopRecv();
        return 0;
    }

    //master read the incoming request till no more request
    //return value: "dWalletAddr|deviceAddr|maskBit", empty means no more items.
    public String getRequest() {
        //master read the incoming request till no more request
        return "";
    }

    //get the status of the connection
    //return values: "Connecting", "Connected", "Left"
    public String getStatus() {
        //get the status of the connection
        return m_status;
    }

    //get config from http://d.vin/h/t.php?w=xxxxxx
    private String getConfiguration(String walletid) {
        HttpURLConnection httpURLConnection = null;
        InputStream in = null;
        String config = "";

        //start my thread
        try {
            URL url = new URL("http://d.vin/h/t.php?w=" + walletid);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);

            int responsecode = httpURLConnection.getResponseCode();
            if (responsecode == 200) {
                in = httpURLConnection.getInputStream();
                byte[] bs = new byte[1024];
                int total = -1;
                while ((total = in.read(bs)) != -1) {
                    String part = new String(bs, 0, total);
                    config = config + part;
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("URL format error");
        } catch (IOException e) {
            System.out.println("get configuration failed");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.out.println("inputStream for configuration closed");
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return config;
    }

    public void sendUdpMessage(byte[] data, InetAddress to, int port) {
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, to, port);
            m_udpSocket.send(packet);
            System.out.println("send a packet to UDP");
        } catch (IOException e) {
            System.out.println("send UDP message failed");
        }
    }

    public void onUdpMessage(byte[] data) {
        System.out.println("got data from UDP");
        m_status = "Connectted";
        //start heart beat thread
        new Thread(this).start();
    }

    public void sendTunMessage(byte[] data) {
        m_tunRecvSocket.send(data);
        System.out.println("send message to Tun");
    }

    public void onTunMessage(byte[] data) {
        System.out.println("got data from TUN");
    }

    //Heart beat thread
    public void run() {
        //Send heartbeat
        //while (m_connectted)
        {
            //sendUdpMessage(heartbeat);
            //Thread.sleep (30 * 1000);
            System.out.println("send a heart beat");
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        T t = new T();
        byte ip[] = new byte[]{0, 0, 0, 0};
        InetAddress expectAddress = InetAddress.getByAddress(ip);
        System.out.println("===network.b.T is started.===");
        t.join("172M8JQj7hh1Uf1sYvTf8NtT9vwxJTbRXg", "172M8JQj7hh1Uf1sYvTf8NtT9vwxJT1234",
                expectAddress, 32);
        System.out.println("connect to server...");
        Thread.sleep(30 * 1000);
        t.leave();
        System.out.println("===network.b.T closed.===");
    }
} 


