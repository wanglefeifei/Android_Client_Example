package network.b.bnet;

import android.view.View;
import android.support.v7.widget.SwitchCompat;

/**
 * Created by jack.ma on 2018/6/26.
 */

public class MainActivity_LinkView {
    public MainActivity_LinkView(View baseView) {
        if (baseView != null) {
            main_net_status_switch = (SwitchCompat) baseView.findViewById(R.id.main_net_status_switch);
            private_network = baseView.findViewById(R.id.private_network);
            join_network = baseView.findViewById(R.id.join_network);
        }
    }

    public SwitchCompat main_net_status_switch = null;
    public View private_network = null;
    public View join_network = null;

}
