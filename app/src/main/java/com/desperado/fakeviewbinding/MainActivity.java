package com.desperado.fakeviewbinding;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.desperado.annotations.BindView;
import com.desperado.viewbinding.ViewBinding;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_tv_hello)
    TextView mTvTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewBinding.bind(this);
        mTvTextView.setText("xuixi");
    }
}
