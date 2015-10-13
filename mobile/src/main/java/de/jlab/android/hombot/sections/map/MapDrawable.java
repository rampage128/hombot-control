package de.jlab.android.hombot.sections.map;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

import de.jlab.android.hombot.common.core.HombotMap;

/**
 * Created by frede_000 on 12.10.2015.
 */
public class MapDrawable {
    public ArrayList<BlockDrawable> blocks = new ArrayList<>();

    public ArrayList<CellDrawable> floorCells = new ArrayList<>();
    public ArrayList<CellDrawable> carpetCells = new ArrayList<>();
    public ArrayList<CellDrawable> wallCells = new ArrayList<>();
    public ArrayList<CellDrawable> inaccessibleCells = new ArrayList<>();
    public ArrayList<CellDrawable> climbCells = new ArrayList<>();
    public ArrayList<CellDrawable> wallSneakCells = new ArrayList<>();
    public ArrayList<CellDrawable> lowCeilingCells = new ArrayList<>();
    public ArrayList<CellDrawable> wallFollowingCells = new ArrayList<>();
    public ArrayList<CellDrawable> collisionCells = new ArrayList<>();
    public ArrayList<CellDrawable> slowCells = new ArrayList<>();

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
                    md.floorCells.add(cd);
                } else if (HombotMap.Cell.FloorType.CARPET == cell.getFloorType()) {
                    md.carpetCells.add(cd);
                } else if (HombotMap.Cell.FloorType.WALL == cell.getFloorType()) {
                    md.wallCells.add(cd);
                } else if (HombotMap.Cell.FloorType.INACCESSIBLE == cell.getFloorType()) {
                    md.inaccessibleCells.add(cd);
                }

                if (cell.isClimbable()) {
                    md.climbCells.add(cd);
                }
                if (cell.isWallSneak()) {
                    md.wallSneakCells.add(cd);
                }
                if (cell.isLowCeiling()) {
                    md.lowCeilingCells.add(cd);
                }
                if (cell.isWallFollowing()) {
                    md.wallFollowingCells.add(cd);
                }
                if (cell.isCollision()) {
                    md.collisionCells.add(cd);
                }
                if (cell.isSlow()) {
                    md.slowCells.add(cd);
                }
            }
        }

        return md;
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
