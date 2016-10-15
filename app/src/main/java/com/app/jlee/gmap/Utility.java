package com.app.jlee.gmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class Utility {
    public static BitmapDescriptor getMarkerIconFromDrawable(Context context, int resource) {
        Drawable drawable = context.getDrawable(resource);

        int width = 24, height = 24;
        Bitmap bitmap;
        if (drawable != null) {
            width = drawable.getIntrinsicWidth();
            height = drawable.getIntrinsicHeight();

            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, width, height);
            drawable.setColorFilter(context.getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
            drawable.draw(canvas);
        } else {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
