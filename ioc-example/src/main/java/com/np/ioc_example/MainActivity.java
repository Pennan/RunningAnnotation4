package com.np.ioc_example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.np.annotation.MyBindView;
import com.np.ioc.ViewInject;

import java.util.ArrayList;
import java.util.List;

// 在 build->generated->source->debug->包名下可以找到注解处理器在编译时生成的代码
/**
 * auto-service: 注解在自定义的注解处理器类上,
 *  使该注解处理器类能够被 javac 启动的虚拟机识别到并运行起来。
 * javaPoet: 帮助生成 java 源文件.
 * android-apt: 使注解处理器只在编译时器工作,并且使 AS 能够引用注解处理器动态生成的代码。
 */
public class MainActivity extends AppCompatActivity {

    @MyBindView(R.id.tv_main)
    ListView lvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewInject.injectView(this);
        List<String> dataList = initData();
        lvMain.setAdapter(new MyAdapter(this, dataList, R.layout.item));
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> initData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            data.add("哈哈" + i);
        }
        return data;
    }
}
