package com.chen.scangon.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.chen.scangon.R;

import java.util.List;


public class ScanAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mScanList;

    public ScanAdapter(Context context, List<String> list) {
        mContext = context;
        mScanList = list;
    }

    @Override
    public int getCount() {
        return mScanList.size();
    }

    @Override
    public Object getItem(int position) {
        return mScanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_main, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mTextView = (TextView) convertView.findViewById(R.id.tv_item_main_ordernumber);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mTextView.setText(mScanList.get(position));

        return convertView;
    }

    private class ViewHolder {
        TextView mTextView;
    }

}
