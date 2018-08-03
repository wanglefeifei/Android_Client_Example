package network.b.bnet.protect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnePixelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("wanglf", "onReceive: ,,,,,,,,,,,,,,");
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {    //屏幕关闭启动1像素Activity
            Intent it = new Intent(context, LiveActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {   //屏幕打开 结束1像素
            context.sendBroadcast(new Intent("finish"));
            Intent main = new Intent(Intent.ACTION_MAIN);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            main.addCategory(Intent.CATEGORY_HOME);
            context.startActivity(main);
        }
    }
}
