package network.b.bnet.base;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import network.b.bnet.R;

/**
 * Created by jack.ma on 2018/6/24.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // getWindow().addFlags(
            // WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        initView();
        setTitle_Left_Img(R.mipmap.base_back);
        initData();
    }

    protected abstract void initData();

    protected abstract void initView();

    protected void setTitle_Left_Img(int id) {
        ImageView img = (ImageView) findViewById(R.id.title_left_img);
        if (img != null) {
            img.setBackgroundResource(id);
            View back = findViewById(R.id.title_left_ll);
            if (back != null) {
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
            }
        }
    }

    protected void setTitle_Left_Word(String text) {
        TextView textView = (TextView) findViewById(R.id.title_left_word);
        if (textView != null) {
            textView.setText(text);
        }
    }

    protected void setTitle_Title_Word(String text) {
        TextView textView = (TextView) findViewById(R.id.title_title_word);
        if (textView != null) {
            textView.setText(text);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
