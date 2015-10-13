package de.jlab.android.hombot.common.core;

import android.graphics.Point;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by frede_000 on 07.10.2015.
 */
public class HombotMap {

    private static final int BYTE_LENGTH_HEADER = 44;
    private static final int BYTE_LENGTH_BLOCK  = 16;
    private static final int BLOCK_CELL_COUNT  = 100;

    public static final String MAP_GLOBAL = "MapReuseNavi";

    private static class MapHeader {
        public int mapCount;
        public int ceilMin;
        public int ceilMax;
        public int climbMax;
        public int unknown;
        public int cellBytes;
        public int blockMax;
        public int flagMax;
        public int usedBlocks;
        public int cellDim;
        public int cellCount;
    }
    private MapHeader mapHeader = new MapHeader();

    public static class Offsets {
        public int xMin = Integer.MAX_VALUE;
        public int yMin = Integer.MAX_VALUE;
        public int xMax = Integer.MIN_VALUE;
        public int yMax = Integer.MIN_VALUE;
    }
    private Offsets mOffsets = new Offsets();

    private ArrayList<Block> blockList = new ArrayList<>();

    public static HombotMap getInstance(ByteBuffer in) throws IOException {
        HombotMap map = new HombotMap();
        if (in != null) {
            map.parseMap(in);
        }
        return map;
    }

    public static HombotMap getInstance(String data) {
        HombotMap map = new HombotMap();
        if (data != null) {
            map.parseMap(data);
        }
        return map;
    }

    private HombotMap() {}

    private void parseMap(ByteBuffer in) throws IOException {

        mapHeader.mapCount = in.getInt();
        mapHeader.ceilMin = in.getInt();
        mapHeader.ceilMax = in.getInt();
        mapHeader.climbMax = in.getInt();
        mapHeader.unknown = in.getInt();
        mapHeader.cellBytes = in.getInt();
        mapHeader.blockMax = in.getInt();
        mapHeader.flagMax = in.getInt();
        mapHeader.usedBlocks = in.getInt();
        mapHeader.cellDim = in.getInt();
        mapHeader.cellCount = in.getInt();

        System.out.println("read header");

        for (int i = 0; i < mapHeader.usedBlocks; i++) {
            Block block = Block.read(in);
            mOffsets.xMin = Math.min(mOffsets.xMin, block.getX());
            mOffsets.xMax = Math.max(mOffsets.xMax, block.getX());
            mOffsets.yMin = Math.min(mOffsets.yMin, block.getY());
            mOffsets.yMax = Math.max(mOffsets.yMax, block.getY());
            blockList.add(block);
        }

        // JUMP TO CELL POSITION
        in.position(BYTE_LENGTH_HEADER + mapHeader.blockMax * BYTE_LENGTH_BLOCK);

        for (Block block : blockList) {
            block.readCells(in);
        }

    }

    private void parseMap(String data) {
        System.out.println(data);
        throw new UnsupportedOperationException("Cannot parse map via String! Please use parseMap(DataInputStream)");
    }

    public Offsets getOffsets() {
        return mOffsets;
    }

    public ArrayList<Block> getBlocks() {
        return blockList;
    }

    public static class Block {

        private int turn;
        private int move;
        private int x;
        private int y;
        private int distance;

        private ArrayList<Cell> cellList = new ArrayList<>();

        private Block() {
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public static Block read(ByteBuffer in) {

            Block block = new Block();

            block.turn = in.getInt();
            block.move = in.getInt();
            int posxy = in.getInt();
            if (posxy == -1) {
                block.x = block.y = posxy;
            } else {
                block.x = posxy % 100;
                block.y = posxy / 100;
            }
            block.distance = in.getInt();

            return block;
        }

        public void readCells(ByteBuffer in) {
            for (int i = 0; i < BLOCK_CELL_COUNT; i++) {
                Cell cell = Cell.read(in);
                cellList.add(cell);
            }
        }

        public ArrayList<Cell> getCells() {
            return this.cellList;
        }

    }

    public static class Cell {

        public static enum FloorType {
            INACCESSIBLE,
            WALL,
            CARPET,
            NORMAL
        }

        private boolean climbable;
        private boolean wallSneak;
        private boolean lowCeiling;
        private boolean followWall;
        private boolean collision;
        private boolean slow;
        private FloorType floorType;

        private Cell() {
        }

        public FloorType getFloorType() {
            return floorType;
        }

        public static Cell read(ByteBuffer in) {

            Cell cell = new Cell();

            byte b = in.get();

            cell.climbable = (b & ((byte) 1 << 7)) != 0;
            cell.wallSneak = (b & ((byte) 1 << 6)) != 0;
            cell.lowCeiling = (b & ((byte) 1 << 5)) != 0;
            cell.followWall = (b & ((byte) 1 << 4)) != 0;
            cell.collision = (b & ((byte) 1 << 3)) != 0;
            cell.slow = (b & ((byte) 1 << 2)) != 0;

            int val = ((b & ((byte) 1 << 1)) != 0 ? 2 : 0) + ((b & ((byte) 1 << 0)) != 0 ? 1 : 0);
            cell.floorType = FloorType.values()[val];

            return cell;

        }

        public boolean isClimbable() {
            return climbable;
        }

        public boolean isWallSneak() {
            return this.wallSneak;
        }

        public boolean isLowCeiling() {
            return this.lowCeiling;
        }

        public boolean isWallFollowing() {
            return this.followWall;
        }

        public boolean isCollision() {
            return this.collision;
        }

        public boolean isSlow() {
            return this.slow;
        }

    }

}
