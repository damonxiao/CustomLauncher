
package com.seuic.launcher.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.seuic.launcher.R;
import com.seuic.launcher.util.ImageGetter;

import java.util.List;

public class AlbumAdapter extends BaseAdapter {

    private List<String> mImages;

    private LayoutInflater mInflater;

    public AlbumAdapter(List<String> images, Context context) {
        mImages = images;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mImages == null ? 0 : mImages.size();
    }

    @Override
    public Object getItem(int position) {
        return mImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.album_item, null);
        }
        final ImageView imageView = (ImageView) convertView;
        imageView.setLayoutParams(new Gallery.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT));
        imageView.setAdjustViewBounds(true);
        ImageGetter.loadImageFromAssets(mImages.get(position), new ImageGetter.ImageGetterCb() {
            @Override
            public void onGetterSuccess(Drawable image) {
                if (image != null) {
                    imageView.setImageDrawable(image);
                }
            }
        });
        return convertView;
    }

}
