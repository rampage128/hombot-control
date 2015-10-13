package de.jlab.android.hombot.sections;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.jlab.android.hombot.R;
import de.jlab.android.hombot.SectionFragment;
import de.jlab.android.hombot.common.core.HombotMap;
import de.jlab.android.hombot.sections.map.MapView;

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



        ((Button)view.findViewById(R.id.vt_f)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHolder.map.toggleLayer(MapView.LayerType.FLOOR);
            }
        });
        ((Button)view.findViewById(R.id.vt_c)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHolder.map.toggleLayer(MapView.LayerType.CARPET);
            }
        });
        ((Button)view.findViewById(R.id.vt_w)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHolder.map.toggleLayer(MapView.LayerType.WALL);
            }
        });
        ((Button)view.findViewById(R.id.vt_cl)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHolder.map.toggleLayer(MapView.LayerType.CLIMBABLE);
            }
        });
        ((Button)view.findViewById(R.id.vt_ws)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHolder.map.toggleLayer(MapView.LayerType.WALL_SNEAK);
            }
        });
        ((Button)view.findViewById(R.id.vt_lc)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHolder.map.toggleLayer(MapView.LayerType.LOW_CEILING);
            }
        });
        ((Button)view.findViewById(R.id.vt_wf)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHolder.map.toggleLayer(MapView.LayerType.WALL_FOLLOWING);
            }
        });
        ((Button)view.findViewById(R.id.vt_co)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHolder.map.toggleLayer(MapView.LayerType.COLLISION);
            }
        });
        ((Button)view.findViewById(R.id.vt_sl)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewHolder.map.toggleLayer(MapView.LayerType.SLOW);
            }
        });


        // FIXME: DIRTY HACK BECAUSE MAP WONT SHOW WHEN ACTIVITY IS RESUMED ... I HATE ANDROID FRAGMENT PERSISTENCE AND LIFECYLCE
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                readMapList();
            }
        }, 500);

        return view;
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

                        mViewHolder.mapSelect.setAdapter(new ArrayAdapter<MapName>(getContext(), android.R.layout.simple_spinner_dropdown_item, mapNameList));
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
