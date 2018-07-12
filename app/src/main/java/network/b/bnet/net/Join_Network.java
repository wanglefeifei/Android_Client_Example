package network.b.bnet.net;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;

import network.b.bnet.R;
import network.b.bnet.base.BNetApplication;
import network.b.bnet.base.BaseActivity;
import network.b.bnet.utils.ToastTimerShow;
import network.b.bnet.utils.zxing.activity.CaptureActivity;

public class Join_Network extends BaseActivity {
    int ZXING_RET_BINDING_WRISTBAND = 0x1001;
    private static final int VPN_REQUEST_CODE = 0x0F;
    private View scan_qrcode, join_network;
    private EditText main_user_message_txt;

    private int maskBit = 0;
    private String nWalletAddr = "", dWalletAddr = "10.11.23.3", deviceAddr = "10.11.23.3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_join__network);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {

        setTitle_Title_Word(getResources().getString(R.string.join_in_newwork));
        scan_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScanCode();
            }
        });
        join_network.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (main_user_message_txt.getText() == null || main_user_message_txt.getText().toString().length() == 0) {
                    new ToastTimerShow(getApplicationContext(), getResources().getString(R.string.please_input_word));
                    return;
                }
                startVPN();

            }
        });
    }

    private void startVPN()
    {
        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null)
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);//wait user confirmation, will call onActivityResult
        else
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
    }

    private void startScanCode() {
        Intent intent = new Intent();
        intent.setClass(Join_Network.this, CaptureActivity.class);
        Join_Network.this.startActivityForResult(intent, ZXING_RET_BINDING_WRISTBAND);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK&&requestCode==ZXING_RET_BINDING_WRISTBAND) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            main_user_message_txt.setText(scanResult);
        }
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK)
        {
            nWalletAddr = main_user_message_txt.getText().toString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BNetApplication.getInstance().getBnetAidlInterface().join(nWalletAddr,dWalletAddr,deviceAddr,maskBit);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            try {
                BNetApplication.getInstance().getBnetAidlInterface().CStartService();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void initView() {
        scan_qrcode = findViewById(R.id.scan_qrcode);
        join_network = findViewById(R.id.join_network);
        main_user_message_txt = (EditText) findViewById(R.id.main_user_message_txt);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
