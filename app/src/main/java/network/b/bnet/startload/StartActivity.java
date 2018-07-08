package network.b.bnet.startload;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import network.b.bnet.MainActivity;
import network.b.bnet.R;
import network.b.bnet.base.BNetApplication;
import network.b.bnet.base.BaseActivity;
import network.b.bnet.utils.IPermissionDialog;
import network.b.bnet.utils.PermissionUtils;
import network.b.bnet.utils.SharePreferenceMain;

public class StartActivity extends BaseActivity implements IPermissionDialog {
    PermissionUtils permissionUtils = new PermissionUtils(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_start);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        permissionUtils.checkCameraPermission(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        toMainActivity();
                    }
                });
            }
        }, this);


    }

    private void toMainActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                int count = SharePreferenceMain.getSharedPreference(
                        getApplicationContext()).getStartCount();
                if (false) {

                } else {
                    if (BNetApplication.getInstance().getUser() == null) {
                        Intent intent = new Intent(getApplicationContext(),
                                MainActivity.class);

                        startActivity(intent);
                        finish();
                        overridePendingTransition(0, 0);
                        return;
                    } else {
                        Intent intent = new Intent(StartActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0, 0);
                    }

                }
            }
        }, 2400);
    }

    @Override
    protected void initView() {

    }

    @Override
    public void showPermissionDialog(@NotNull String titleShow) {
        showPermissionAlertDialog(titleShow, "取消", "开启", new DialogClick() {
            @Override
            public void onClick(View view) {
                finish();
            }
        }, new DialogClick() {
            @Override
            public void onClick(View view) {
                permissionUtils.reRequestPermissions(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                toMainActivity();
                            }
                        });
                    }
                }, StartActivity.this);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void showPermissionAlertDialog(String msg, String leftstr, String rightstr, final DialogClick leftClick, final DialogClick rightClick) {
        final Dialog dialog = new Dialog(this, R.style.Base_Theme_AppCompat_Dialog);
        View view = View.inflate(this, R.layout.dialog_alertpermission, null);
        dialog.setContentView(view);
        TextView leftStr = (TextView) view.findViewById(R.id.tv_alert_negative);
        TextView rightStr = (TextView) view.findViewById(R.id.tv_alert_positive);
        TextView message = (TextView) view.findViewById(R.id.tv_alert_message);
        leftStr.setText(leftstr);
        rightStr.setText(rightstr);
        message.setText(msg);

        leftStr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (leftClick != null)
                    leftClick.onClick(view);
                dialog.dismiss();
            }
        });
        rightStr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rightClick != null)
                    rightClick.onClick(view);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    interface DialogClick {
        void onClick(View view);
    }
}
