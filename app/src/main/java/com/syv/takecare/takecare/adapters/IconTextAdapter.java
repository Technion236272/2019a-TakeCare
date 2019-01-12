package com.syv.takecare.takecare.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.syv.takecare.takecare.R;

public class IconTextAdapter extends ArrayAdapter<String> {
    String[] spinnerTitles;
    int[] spinnerImages;
    Context mContext;
    private static class ViewHolder {
        ImageView icon;
        TextView name;
    }
    public IconTextAdapter(@NonNull Context context, String[] titles,int[] images) {
        super(context, R.layout.spinner_element);
        this.spinnerTitles = titles;
        this.spinnerImages = images;
        this.mContext = context;
    }
    @Override
    public int getCount() {
        return spinnerTitles.length;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder mViewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.spinner_element, parent, false);
            mViewHolder.icon = (ImageView) convertView.findViewById(R.id.spinner_icon);
            mViewHolder.name = (TextView) convertView.findViewById(R.id.spinner_text);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.icon.setImageResource(spinnerImages[position]);
        mViewHolder.name.setText(spinnerTitles[position]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
