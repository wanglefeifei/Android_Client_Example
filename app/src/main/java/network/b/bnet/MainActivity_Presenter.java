package network.b.bnet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import network.b.bnet.base.BNetApplication;
import network.b.bnet.net.Join_Network;
import network.b.bnet.net.Net_Logout;
import network.b.bnet.service.BnetService;
import network.b.bnet.utils.SharePreferenceMain;
import network.b.bnet.utils.Utils;

/**
 * Created by jack.ma on 2018/6/27.
 */

public class MainActivity_Presenter implements View.OnClickListener {


    public static MainActivity_LinkView mainActivity_linkView;
    private MainActivity_MyView mainActivity_myView;
    private MainActivity mainActivity;
    private int ret;
    public static boolean isclick = false;
    private Message msg = Message.obtain();
    public static int COLOR_CHANGE = 1;
    static int[] colors = new int[] { Color.BLUE,Color.GRAY ,Color.RED};
    static int index = 0;
    @SuppressLint("HandlerLeak")
    public static Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == COLOR_CHANGE) {
                index += 1;
                if (index == colors.length) {
                    index = 0;
                }
                mainActivity_linkView.join_forum_txt.setTextColor(colors[index]);
                mHandler.sendEmptyMessageDelayed(COLOR_CHANGE, 2000);
            }
        }
    };
    public MainActivity_Presenter(MainActivity_LinkView linkView, MainActivity_MyView myView, MainActivity Activity) {
        mainActivity_linkView = linkView;
        mainActivity_myView = myView;
        mainActivity = Activity;
        initEvent();
    }

    private void initEvent() {
        mainActivity_linkView.private_network.setOnClickListener(this);
        mainActivity_linkView.join_network.setOnClickListener(this);
        if (Utils.isServiceRunning(mainActivity.getApplicationContext(), BnetService.class.getName())) {
            mainActivity_linkView.main_net_status_switch.setChecked(true);
        }
        mainActivity_linkView.main_net_status_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mainActivity == null) {
                    return;
                }
                if (b) {
                    if (mainActivity != null)
                        mainActivity.startVPN();
                    if (!mHandler.hasMessages(MainActivity_Presenter.COLOR_CHANGE)) {
                        mHandler.sendEmptyMessage(MainActivity_Presenter.COLOR_CHANGE);
                    }
                    mHandler.sendEmptyMessageDelayed(MainActivity_Presenter.COLOR_CHANGE, 2000);
                    isclick = true;

                } else {
                    BNetApplication.getInstance().DestoryBnetService();
                    isclick = false;
                }
            }
        });
        mainActivity_myView.main_user_versionname.setText(getVersionName(mainActivity));
        loadWebView();
    }
    private void loadWebView() {
        mainActivity_linkView.join_forum_network.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Runtime runtime = Runtime.getRuntime();
                            try {
                                Process p = runtime.exec("ping -c 3 10.50.0.24");
                                ret = p.waitFor();
                                Log.i("wanglf", "Process:"+ret);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }catch(Exception e){
                            e.printStackTrace();

                        }
                    }
                }).start();
                if (!isclick) {
                    Toast.makeText(mainActivity, "请打开VPN按钮才能访问此网址", Toast.LENGTH_SHORT).show();
                } else {
                    if (ret == 0) {
                        Uri uri = Uri.parse("http://10.50.0.24");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        mainActivity.startActivity(intent);
                    } else {
                        Toast.makeText(mainActivity, "请稍等,正在连接", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }
    public void StartVpvJoin() {
        String dWalletAddr = SharePreferenceMain.getSharedPreference(mainActivity.getApplicationContext()).getdWalletAddr();
        if (dWalletAddr == null) {
            dWalletAddr = UUID.randomUUID().toString();
            SharePreferenceMain.getSharedPreference(mainActivity.getApplicationContext()).savedWalletAddr(dWalletAddr);
        }
        BNetApplication.getInstance().BnetServiceJoin(null, dWalletAddr, "", 32);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.private_network:
                intent = new Intent(mainActivity,
                        Net_Logout.class);
                mainActivity.startActivity(intent);
                mainActivity.overridePendingTransition(0, 0);
                break;
            case R.id.join_network:
                intent = new Intent(mainActivity,
                        Join_Network.class);
                mainActivity.startActivity(intent);
                mainActivity.overridePendingTransition(0, 0);
                break;
        }
    }
}
