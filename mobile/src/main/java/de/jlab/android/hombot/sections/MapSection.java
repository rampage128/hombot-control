package de.jlab.android.hombot.sections;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.SectionFragment;
import de.jlab.android.hombot.common.core.HombotMap;
import de.jlab.android.hombot.sections.map.MapView;
import de.jlab.android.hombot.utils.ColorableArrayAdapter;

/**
 * A {@link SectionFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SectionFragment.SectionInteractionListener} interface
 * to handle interaction events.
 * Use the {@link JoySection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapSection extends SectionFragment {

    private static class ViewHolder {
        MapView map;
        Spinner mapSelect;
        View legend;
        ViewGroup legendLeft;
        ViewGroup legendRight;
        FloatingActionButton legendFab;
    }
    private ViewHolder mViewHolder;

    public static MapSection newInstance(int sectionNumber) {
        MapSection fragment = new MapSection();
        fragment.register(sectionNumber);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_section_map, container, false);

        mViewHolder = new ViewHolder();
        mViewHolder.map = (MapView) view.findViewById(R.id.map);
        mViewHolder.legend = view.findViewById(R.id.legend);
        mViewHolder.legendLeft = (ViewGroup)view.findViewById(R.id.legend_left);
        mViewHolder.legendRight = (ViewGroup)view.findViewById(R.id.legend_right);
        mViewHolder.legendFab = (FloatingActionButton)view.findViewById(R.id.legend_toggle);

        ((Button)view.findViewById(R.id.zoomin_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHolder.map.zoomIn();
            }
        });

        ((Button)view.findViewById(R.id.zoomout_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHolder.map.zoomOut();
            }
        });

        mViewHolder.mapSelect = (Spinner)view.findViewById(R.id.map_select);
        mViewHolder.mapSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                readMap(((MapName) parent.getAdapter().getItem(position)).getFileName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        View.OnClickListener legendToggleListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (View.VISIBLE == mViewHolder.legend.getVisibility()) {
                    mViewHolder.legend.setVisibility(View.GONE);
                } else {
                    mViewHolder.legend.setVisibility(View.VISIBLE);
                }
            }
        };

        mViewHolder.legendFab.setOnClickListener(legendToggleListener);
        mViewHolder.legend.setOnClickListener(legendToggleListener);
        mViewHolder.legend.setVisibility(View.GONE);

        initLegendItem(R.id.legend_0, R.color.map_type_floor, R.string.map_type_floor, MapView.LayerType.FLOOR, mViewHolder.legend);
        initLegendItem(R.id.legend_1, R.color.map_type_wall, R.string.map_type_wall, MapView.LayerType.WALL, mViewHolder.legend);

        initLegendItem(R.id.legend_2, R.color.map_flag_bump, R.string.map_flag_bump, MapView.LayerType.BUMP, mViewHolder.legend);
        initLegendItem(R.id.legend_3, R.color.map_flag_abyss, R.string.map_flag_abyss, MapView.LayerType.ABYSS, mViewHolder.legend);
        initLegendItem(R.id.legend_4, R.color.map_flag_bump_abyss, R.string.map_flag_bump_abyss, MapView.LayerType.BUMP_ABYSS, mViewHolder.legend);
        initLegendItem(R.id.legend_5, R.color.map_flag_sneak, R.string.map_flag_sneak, MapView.LayerType.SNEAKING, mViewHolder.legend);
        initLegendItem(R.id.legend_6, R.color.map_flag_screw, R.string.map_flag_screw, MapView.LayerType.SCREWING, mViewHolder.legend);
        initLegendItem(R.id.legend_7, R.color.map_flag_move_object, R.string.map_flag_move_object, MapView.LayerType.MOVE_OBJECT, mViewHolder.legend);
        initLegendItem(R.id.legend_8, R.color.map_flag_fight, R.string.map_flag_fight, MapView.LayerType.FIGHT, mViewHolder.legend);
        initLegendItem(R.id.legend_9, R.color.map_flag_undetermined, R.string.map_flag_undetermined, MapView.LayerType.UNDETERMINED, mViewHolder.legend);

        initLegendItem(R.id.legend_10, R.color.map_blocks, R.string.map_blocks, MapView.LayerType.BLOCK, mViewHolder.legend);

        // FIXME: DIRTY HACK BECAUSE MAP WONT SHOW WHEN ACTIVITY IS RESUMED ... I HATE ANDROID FRAGMENT PERSISTENCE AND LIFECYLCE
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                readMapList();
            }
        }, 500);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getColorizer().colorizeDrawable(mViewHolder.mapSelect.getBackground(), getColorizer().getColorText());
        getColorizer().colorizeDrawable(mViewHolder.legendFab.getBackground(), getColorizer().getColorPrimary());
        getColorizer().colorizeDrawable(mViewHolder.legendFab.getDrawable(), getColorizer().getContrastingTextColor(getColorizer().getColorPrimary()));
    }

    private void initLegendItem(int id, final int color, int caption, final MapView.LayerType layerType, View container) {
        ViewGroup legendItem = (ViewGroup)container.findViewById(id);
        final FloatingActionButton fab = (FloatingActionButton)legendItem.findViewById(R.id.fab);
        TextView text = (TextView)legendItem.findViewById(R.id.text);

        //fab.setBackgroundTintList(ColorStateList.valueOf(color));
        fab.getBackground().setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_ATOP);
        fab.setImageDrawable(null);
        text.setText(caption);


        View.OnClickListener legendClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHolder.map.toggleLayer(layerType);
                if (mViewHolder.map.isLayerVisible(layerType)) {
                    fab.getBackground().setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_ATOP);
                } else {
                    fab.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                }
            }
        };

        fab.setOnClickListener(legendClick);
        ((View)text.getParent()).setOnClickListener(legendClick);
    }

    private void readMapList() {
        new Thread(new Runnable() {
            public void run() {
                List<String> mapList = ((SectionInteractionListener) getActivity()).requestMapList();
                mapList.add(0, HombotMap.MAP_GLOBAL);
                final ArrayList<MapName> mapNameList = new ArrayList<>();
                for (String mapFileName : mapList) {
                    mapNameList.add(new MapName(mapFileName, getContext()));
                }
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {

                        mViewHolder.mapSelect.setAdapter(new ColorableArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, mapNameList.toArray(), getColorizer(), getColorizer().getColorBackground()));
                    }
                });
            }
        }).start();


    }

    private void readMap(final String mapName) {
        new Thread(new Runnable() {
            public void run() {
                HombotMap map = ((SectionInteractionListener) getActivity()).requestMap(mapName);
                if (map != null) {
                    mViewHolder.map.setMap(map);
                }
            }
        }).start();
    }

    private class MapName {

        private String mFileName;
        private String mDisplayName = "";

        public MapName(String fileName, Context context) {
            mFileName = fileName;
            mDisplayName = mFileName;

            if (HombotMap.MAP_GLOBAL.equalsIgnoreCase(fileName)) {
                mDisplayName = context.getString(R.string.global_map);
            } else if (fileName.indexOf("MAPDATA") == 0) { // EXAMPLE: MAPDATA20151012162355_994904_134.blk
                String dateString = fileName.substring(7, fileName.indexOf("_"));

                try {
                    Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(dateString);
                    mDisplayName = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss").format(date);
                } catch (Exception ignored) {}

            }
        }

        public String getFileName() {
            return this.mFileName;
        }

        public String toString() {
            return mDisplayName;
        }

    }

}
