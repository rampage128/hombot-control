package de.jlab.android.hombot.sections.map;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import de.jlab.android.hombot.common.core.HombotMap;

/**
 * Created by frede_000 on 12.10.2015.
 */
public class MapDrawable {
    public ArrayList<BlockDrawable> blocks = new ArrayList<>();

    public enum CellType {
        ABYSS,
        BUMP,
        BUMP_ABYSS,
        FIGHT,
        FLOOR,
        MOVE_OBJECT,
        SNEAKING,
        SCREWING,
        UNDETERMINED,
        VOID,
        WALL
    }

    private LinkedHashMap<CellType, ArrayList<CellDrawable>> cellMap = new LinkedHashMap<>();

    public static MapDrawable convert(HombotMap map) {
        int x = 0, y = 0;
        int cX = 0, cY = 0;
        int cc = 0;
        MapDrawable md = new MapDrawable();
        for (HombotMap.Block block : map.getBlocks()) {
            x = (block.getX() - map.getOffsets().xMin) * 10;
            y = (map.getOffsets().yMax - block.getY()) * 10;

            md.blocks.add(new BlockDrawable(x, y));

            cc = 0;
            for (HombotMap.Cell cell : block.getCells()) {

                cX = x + (cc % 10);
                cY = (int)(y + 10 - Math.floor(cc / 10));

                cc += 1;

                CellDrawable cd = new CellDrawable(cX, cY);

                // INTERPRET THE MAP AND CREATE ARRAYLISTS BASED ON INTERPRETATION

                if (HombotMap.Cell.FloorType.NORMAL == cell.getFloorType()) {
                    md.addCell(CellType.FLOOR, cd);
                } else if (HombotMap.Cell.FloorType.CARPET == cell.getFloorType()) {
                    md.addCell(CellType.UNDETERMINED, cd);
                } else if (HombotMap.Cell.FloorType.WALL == cell.getFloorType()) {
                    md.addCell(CellType.WALL, cd);
                } else if (HombotMap.Cell.FloorType.INACCESSIBLE == cell.getFloorType()) {
                    md.addCell(CellType.VOID, cd);
                }

                boolean isMoveObject = cell.isSneaking() && cell.isScrewing();

                if (cell.isSneaking()) {
                    md.addCell(CellType.SNEAKING, cd);
                }
                if (cell.isScrewing()) {
                    md.addCell(CellType.SCREWING, cd);
                }
                if (cell.isAbyss() && (cell.isScrewing() || isMoveObject)) {
                    md.addCell(CellType.FIGHT, cd);
                } else if (cell.isCollision() && cell.isAbyss()) {
                    md.addCell(CellType.BUMP_ABYSS, cd);
                } else {
                    if (cell.isAbyss()) {
                        md.addCell(CellType.ABYSS, cd);
                    }
                    if (cell.isCollision()) {
                        md.addCell(CellType.BUMP, cd);
                    }
                }
                if (isMoveObject) {
                    md.addCell(CellType.MOVE_OBJECT, cd);
                }
            }
        }

        return md;
    }

    public ArrayList<CellDrawable> getCells(CellType type) {
        return this.cellMap.get(type);
    }

    private void addCell(CellType type, CellDrawable drawable) {
        ArrayList<CellDrawable> cellList = this.cellMap.get(type);
        if (cellList == null) {
            cellList = new ArrayList<>();
            this.cellMap.put(type, cellList);
        }
        cellList.add(drawable);
    }

    public static class BlockDrawable implements MapDrawableItem {
        public int x, y;

        public BlockDrawable(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void draw(Canvas c, Paint p, float zoom) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1);
            c.drawRect(x * zoom, y * zoom, (x + 10) * zoom, (y + 10) * zoom, p);
        }
    }

    public static class CellDrawable implements MapDrawableItem {
        public int x, y;

        public CellDrawable(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void draw(Canvas c, Paint p, float zoom) {
            p.setStrokeWidth(zoom + 1);
            c.drawPoint(x * zoom, y * zoom, p);
            p.setStrokeWidth(1);
        }
    }

    public interface MapDrawableItem {
        public void draw(Canvas c, Paint p, float zoom);
    }

}
