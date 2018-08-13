package network.b.bnet;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

import network.b.bnet.base.BNetApplication;
import network.b.bnet.base.BaseActivity;
import network.b.bnet.protect.OnePixelReceiver;
import network.b.bnet.service.BnetAidlInterface;
import network.b.bnet.service.BnetService;
import network.b.bnet.service.LocalVPNService;
import network.b.bnet.service.LogService;
import network.b.bnet.utils.Utils;
import network.b.bnet.utils.parts.MainPagerAdapter;

public class MainActivity extends BaseActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    // bottom menu
    private LinearLayout ll_home;
    private LinearLayout ll_setting;

    // bottom menu
    private ImageView iv_home;
    private ImageView iv_setting;

    // bottom menu text
    private TextView tv_home;
    private TextView tv_setting;

    private ViewPager viewPager;

    private MainPagerAdapter adapter;

    private List<View> views;

    private BnetAidlInterface bnetAidlInterface;

    private MainActivity_LinkView mainActivity_linkView;
    private MainActivity_MyView mainActivity_myView;
    private MainActivity_Presenter mainActivity_presenter;
    private OnePixelReceiver mOnepxReceiver;


    private static final int VPN_REQUEST_CODE = 0x0F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getApplicationContext(), LogService.class);
        startService(intent);
    }

    @Override
    protected void initData() {
        ServiceConnection bNetService = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                bnetAidlInterface = BnetAidlInterface.Stub.asInterface(iBinder);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };

        ll_home.setOnClickListener(this);
        ll_setting.setOnClickListener(this);
        setTitle_Left_Img(0);
        restartBotton();
        selectPageIndex(0);
        viewPager.addOnPageChangeListener(this);
        mainActivity_presenter = new MainActivity_Presenter(mainActivity_linkView, mainActivity_myView, this);
        onepxRecevier();
    }

    protected void onepxRecevier() {
        mOnepxReceiver = new OnePixelReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        registerReceiver(mOnepxReceiver, intentFilter);
    }
    @Override
    protected void initView() {
        this.ll_home = (LinearLayout) findViewById(R.id.ll_home);
        this.ll_setting = (LinearLayout) findViewById(R.id.ll_setting);

        this.iv_home = (ImageView) findViewById(R.id.iv_home);
        this.iv_setting = (ImageView) findViewById(R.id.iv_setting);

        this.tv_home = (TextView) findViewById(R.id.tv_home);
        this.tv_setting = (TextView) findViewById(R.id.tv_setting);

        this.viewPager = (ViewPager) findViewById(R.id.vp_content);

        View pageLink = View.inflate(MainActivity.this, R.layout.main_page_link, null);
        View pageMy = View.inflate(MainActivity.this, R.layout.main_page_userinfo, null);
        //        View pageMy = View.inflate(MainActivity.this, R.layout.main_page_link, null);
        mainActivity_linkView = new MainActivity_LinkView(pageLink);
        mainActivity_myView = new MainActivity_MyView(pageMy);
        views = new ArrayList<View>();
        views.add(pageLink);
        views.add(pageMy);

        this.adapter = new MainPagerAdapter(views);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        restartBotton();
        switch (v.getId()) {
            case R.id.ll_home:
                selectPageIndex(0);
                viewPager.setCurrentItem(0);
                break;

            case R.id.ll_setting:
                selectPageIndex(1);
                viewPager.setCurrentItem(3);
                break;

            default:
                break;
        }

    }

    public void selectPageIndex(int index) {
        switch (index) {
            case 0:
                setTitle_Title_Word(getResources().getString(R.string.link));
                iv_home.setImageResource(R.mipmap.main_link_light_icon);
                tv_home.setTextColor(getResources().getColor(R.color.base_blue_color));
                break;
            case 1:
                setTitle_Title_Word(getResources().getString(R.string.my));
                iv_setting.setImageResource(R.mipmap.main_my_new_light_icon);
                tv_setting.setTextColor(getResources().getColor(R.color.base_blue_color));
                break;
        }
    }

    private void restartBotton() {
        iv_home.setImageResource(R.mipmap.main_link_gray_icon);
        iv_setting.setImageResource(R.mipmap.main_my_gray_icon);

        tv_home.setTextColor(getResources().getColor(R.color.base_gray_color));
        tv_setting.setTextColor(getResources().getColor(R.color.base_gray_color));
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        restartBotton();

        selectPageIndex(arg0);

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (BNetApplication.isChecked && !Utils.isServiceRunning(getApplicationContext(),LocalVPNService.class.getName())) {
            mainActivity_linkView.main_net_status_switch.setChecked(false);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void startVPN() {
        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null)
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);//wait user confirmation, will call onActivityResult
        else
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            if (mainActivity_presenter != null) {
                mainActivity_presenter.StartVpvJoin();
            }
        }
    }

}
