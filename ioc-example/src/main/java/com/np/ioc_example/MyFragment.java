package com.np.ioc_example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.np.annotation.MyBindView;
import com.np.ioc.ViewInject;

public class MyFragment extends Fragment {

    @MyBindView(R.id.fragment_tv)
    TextView tvFragment;
    @MyBindView(R.id.fragment_iv)
    ImageView ivFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_layout, container, false);
        ViewInject.injectView(this, view);
        tvFragment.setText("哈哈，我在 Fragment 中.");
        ivFragment.setBackgroundResource(R.mipmap.ic_launcher);
        return view;
    }
}
