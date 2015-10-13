package de.jlab.android.hombot.sections.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import de.jlab.android.hombot.common.core.HombotMap;

/**
 * Created by frede_000 on 06.10.2015.
 */
public class MapView extends View {

    public enum LayerType {
        FLOOR, CARPET, WALL,
        CLIMBABLE, WALL_SNEAK, LOW_CEILING, WALL_FOLLOWING, COLLISION, SLOW,
        BLOCK
    }

    private MapDrawable mMap;
    private float mZoom = 2;

    private LinkedHashMap<LayerType, MapViewLayer> mLayers = new LinkedHashMap<>();

    public MapView(Context context) {
        super(context);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void toggleLayer(LayerType type) {
        MapViewLayer layer = mLayers.get(type);
        layer.setEnabled(!layer.isEnabled());
        invalidate();
    }

    private void setLayer(LayerType layerType, ArrayList<? extends MapDrawable.MapDrawableItem> items, int color) {
        MapViewLayer layer = mLayers.get(layerType);
        if (layer == null) {
            layer = new MapViewLayer(items, color);
            mLayers.put(layerType, layer);
        } else {
            layer.setItems(items);
            layer.setPrimaryColor(color);
        }
    }

    public void setMap(final HombotMap map) {
        mMap = MapDrawable.convert(map);
        post(new Runnable() {
            @Override
            public void run() {
                // RENDER FLOOR
                setLayer(LayerType.FLOOR, mMap.floorCells, Color.LTGRAY);
                setLayer(LayerType.CARPET, mMap.carpetCells, Color.DKGRAY);
                setLayer(LayerType.WALL, mMap.wallCells, Color.WHITE);

                // RENDER ADDITIONAL INFO
                setLayer(LayerType.CLIMBABLE, mMap.climbCells, Color.YELLOW);
                setLayer(LayerType.WALL_SNEAK, mMap.wallSneakCells, Color.MAGENTA);
                setLayer(LayerType.LOW_CEILING, mMap.lowCeilingCells, Color.BLUE);
                setLayer(LayerType.WALL_FOLLOWING, mMap.wallFollowingCells, Color.GREEN);
                setLayer(LayerType.COLLISION, mMap.collisionCells, Color.RED);
                setLayer(LayerType.SLOW, mMap.slowCells, Color.BLACK);

                // RENDER GRID
                setLayer(LayerType.BLOCK, mMap.blocks, Color.DKGRAY);

                int mWidth  = map.getOffsets().xMax - map.getOffsets().xMin + 1;
                int mHeight = map.getOffsets().yMax - map.getOffsets().yMin + 1;
                float factor = Math.min(getMeasuredWidth() / mWidth, getMeasuredHeight() / mHeight);
                mZoom = factor / 10;

                invalidate();
            }
        });
    }

    public void zoomIn() {
        mZoom++;
        invalidate();
    }

    public void zoomOut() {
        if (mZoom > 1) {
            mZoom--;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(mZoom);

        canvas.drawColor(Color.TRANSPARENT);

        // canvas.drawLine(1f, 1f, 200f, 200f, paint);

        if (mMap == null)
            return;

        for (MapViewLayer layer : mLayers.values()) {
            layer.draw(canvas, paint, mZoom);
        }
    }

      ///////////////////////
     /// SCROLLING /////////
    ///////////////////////

    // TODO IMPLEMENT SCROLLING AND ZOOMING VIA https://developer.android.com/training/gestures/scroll.html

}
