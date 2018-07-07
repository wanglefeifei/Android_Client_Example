package network.b.bnet.net;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;

import network.b.bnet.R;
import network.b.bnet.base.BNetApplication;
import network.b.bnet.base.BaseActivity;

public class Net_Logout extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_net__logout);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        setTitle_Title_Word(getResources().getString(R.string.private_tee_net));
        findViewById(R.id.logout_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BNetApplication.getInstance().getBnetAidlInterface().leave();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void initView() {

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
