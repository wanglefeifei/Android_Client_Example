package network.b.bnet;

import android.view.View;
import android.widget.TextView;


/**
 * Created by jack.ma on 2018/6/26.
 */

public class MainActivity_MyView {
    public TextView main_user_versionname = null;
    public MainActivity_MyView(View baseView) {
        if (baseView != null) {
            main_user_versionname = baseView.findViewById(R.id.main_user_versionname);
        }
    }

}
