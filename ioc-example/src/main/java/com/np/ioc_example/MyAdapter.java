package com.np.ioc_example;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.np.annotation.MyBindView;
import com.np.ioc.ViewInject;

import java.util.List;

public class MyAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mDataList;
    private int layoutId;

    public MyAdapter(Context context, List<String> dataList, int item) {
        this.mContext = context;
        this.mDataList = dataList;
        this.layoutId = item;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvItem.setText(mDataList.get(position));
        return convertView;
    }

     static class ViewHolder {
        @MyBindView(R.id.item_tv)
        TextView tvItem;
        ViewHolder(View view) {
            ViewInject.injectView(this, view);
        }
    }
}
