package com.lh.classannotation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lh.annomations.BindView;
import com.lh.annomations.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.test)
    TextView test;

    @BindView(R.id.img)
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Butterknife.bind(this);
    }


    @OnClick(R.id.btn)
    public void OnClick(View view) {
        if (test != null) {
            test.setText("sssssssss");
        }
        if (img != null) {
            img.setImageResource(R.mipmap.ic_launcher);
        }
    }
}
