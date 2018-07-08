/**
 * 某时间段内只提示一次toast信息
 */
package network.b.bnet.utils;

import android.content.Context;
import android.widget.Toast;


public class ToastTimerShow {
    // 当时记录时间
    private static long RecordTime = 0;

    public ToastTimerShow(Context context, String str) {
        // 时间间隔提示默认时间间隔为0
        long TimeInterval = 0;
        try {

            // 当时间小于时间加时不显示
            if (System.currentTimeMillis() > RecordTime + TimeInterval) {
                RecordTime = System.currentTimeMillis();
            } else {
                return;
            }
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // TODO: handle exception
        }

    }


    public ToastTimerShow(Context context, String str, long TimeInterval) {
        try {
            // 当时间小于时间加时不显示
            if (System.currentTimeMillis() > RecordTime + TimeInterval) {
                RecordTime = System.currentTimeMillis();
            } else {
                return;
            }
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // TODO: handle exception
        }

    }
}
