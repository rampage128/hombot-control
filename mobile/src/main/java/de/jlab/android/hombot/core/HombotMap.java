package de.jlab.android.hombot.core;

/**
 * Created by frede_000 on 07.10.2015.
 */
public class HombotMap {

    public static final String MAP_GLOBAL = "MapReuseNavi";

    public static HombotMap getInstance(String data) {
        HombotMap map = new HombotMap();
        if (data != null) {
            map.parseMap(data);
        }
        return map;
    }

    private HombotMap() {}

    private void parseMap(String data) {
        System.out.println(data);
    }

}
