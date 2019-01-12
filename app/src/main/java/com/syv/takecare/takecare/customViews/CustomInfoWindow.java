package com.syv.takecare.takecare.customViews;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.firestore.DocumentSnapshot;
import com.syv.takecare.takecare.R;

import java.io.IOException;
import java.net.URL;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {
    private Context context;

    public CustomInfoWindow(Context context){
        this.context = context;
    }
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.marker_info_window, null);
        TextView title = view.findViewById(R.id.info_window_title);
        TextView snippet = view.findViewById(R.id.info_window_snippet);
        ImageView picture = view.findViewById(R.id.info_window_picture);
        title.setText(marker.getTitle());
        snippet.setText(marker.getSnippet());
        DocumentSnapshot doc = (DocumentSnapshot) marker.getTag();
        String photo = (String) doc.get("photo");
        Glide.with(context).asBitmap().load(photo).apply(new RequestOptions().centerCrop()).listener(new RequestListener<Bitmap>(){

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                if (!dataSource.equals(DataSource.MEMORY_CACHE)) marker.showInfoWindow();
                return false;
            }
        }).into(picture);
        return view;
    }

}
