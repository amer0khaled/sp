package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author Amer Khaled
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;


    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */



    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.


        for (int colIterator = 0; colIterator < board.size(); ++colIterator){
            if(tiltUpSingleCol(colIterator, board)){
                changed = true;
            }
        }

        board.setViewingPerspective(side.NORTH);
        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }


    /*true is a tile at row2 or row3 come from merging*/
    private boolean row3Merged = false, row2Merged = false, row1Merged = false;
    private boolean tiltUpSingleCol(int col, Board b){

        boolean change2 = tiltUpSIngleTileRow2(col, b);
        boolean change1 = tiltUpSingleTileRow1(col, b);
        boolean change0 = tiltUpSingleTileRow0(col, b);

        row1Merged = false;
        row2Merged = false;
        row3Merged = false;

        return change2 || change1 || change0;

    }


    /** Helper method that processes a single tile at (COL, 2) in board B
     * when tilt up North. Return true if this changes the board. */
    private boolean tiltUpSIngleTileRow2(int col, Board b){

        boolean change2 = false;
        Tile tileRow2 = b.tile(col, 2);
        Tile tileRow3 = b.tile(col, 3);

        if (tileRow2 != null){
            if(tileRow3 == null){
                b.move(col, 3, tileRow2);
                change2 = true;
            }
            else if (row3Merged == false && tileRow3.value() == tileRow2.value()){
                b.move(col, 3, tileRow2);
                score += (2 * tileRow3.value());
                row3Merged = true;
                change2 = true;
            }
        }

        return change2;
    }



    /** Helper method that processes a single tile at (COL, 1) in board B
     * when tilt up North. Return true if this changes the board. */
    private boolean tiltUpSingleTileRow1(int col, Board b){

        boolean change1    = false;
        Tile    tileRow1   = b.tile(col, 1);
        Tile    tileRow2   = b.tile(col, 2);
        Tile    tileRow3   = b.tile(col, 3);

        if (tileRow1 != null){
            if(tileRow2 == null && tileRow3 == null){
                b.move(col, 3, tileRow1);
                change1 = true;
            }
            else if (tileRow2 == null && tileRow3 != null) {
                if (row3Merged == false && tileRow1.value() == tileRow3.value()){
                    b.move(col, 3, tileRow1);
                    score += (2 * tileRow3.value());
                    row3Merged = true;
                    change1 = true;
                }
                else {
                    b.move(col, 2, tileRow1);
                    change1 = true;
                }

            }
            else if (tileRow2 != null){
                if(row2Merged == false && tileRow1.value() == tileRow2.value()){
                    b.move(col, 2, tileRow1);
                    score += (2 * tileRow2.value());
                    row2Merged = true;
                    change1 = true;
                }

            }

        }

        return change1;

    }


    /** Helper method that processes a single tile at (COL, 0) in board B
     * when tilt up North. Return true if this changes the board. */
    private boolean tiltUpSingleTileRow0(int col, Board b){
        boolean change0    = false;
        Tile    tileRow0   = b.tile(col, 0);
        Tile    tileRow1   = b.tile(col, 1);
        Tile    tileRow2   = b.tile(col, 2);
        Tile    tileRow3   = b.tile(col, 3);

        if(tileRow0 != null){

            if (tileRow1 == null && tileRow2 == null && tileRow3 == null){
                b.move(col, 3, tileRow0);
                change0 = true;
            }
            else if (tileRow1 == null && tileRow2 == null && tileRow3 != null){
                if(row3Merged == false && tileRow3.value() == tileRow0.value()){
                    b.move(col, 3, tileRow0);
                    score += (2 * tileRow3.value());
                    change0 = true;
                    row3Merged = true;
                }
                else {
                    b.move(col, 2, tileRow0);
                    change0 = true;
                }
            }
            else if (tileRow1 == null && tileRow2 != null ){
                if(row2Merged == false && tileRow2.value() == tileRow0.value()){
                    b.move(col, 2, tileRow0);
                    score += (2 * tileRow2.value());
                    change0 = true;
                    row2Merged = true;
                }
                else {
                    b.move(col, 2, tileRow0);
                    change0 = true;
                }
            }
            else if (tileRow1 != null){
                if(row1Merged == false && tileRow1.value() == tileRow0.value()){
                    b.move(col, 1, tileRow0);
                    score += (2 * tileRow1.value());
                    change0 = true;
                    row1Merged = true;
                }
                else {
                    b.move(col, 1, tileRow0);
                    change0 = true;
                }
            }


        }

        return change0;

    }





    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.

        //get grid size(number of rows and cols)
        int gridSize = b.size();

        //loop on Rows
        for(int colIterator = 0; colIterator < gridSize; ++colIterator){

            //loop on Cols
            for(int rowIterator = 0; rowIterator < gridSize; ++rowIterator){

                //check if any tile in the grid is empty
                if(b.tile(colIterator, rowIterator) == null){
                    return true;
                }
            }
        }

        //if there is no empty tile
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.

        //get grid size(number of rows and cols)
        int gridSize = b.size();

        //loop on Rows
        for(int colIterator = 0; colIterator < gridSize; ++colIterator){

            //loop on Cols
            for(int rowIterator = 0; rowIterator < gridSize; ++rowIterator){

                //check if any tile value reach the max value that equal 2048
                if(b.tile(colIterator, rowIterator) != null && b.tile(colIterator, rowIterator).value() == MAX_PIECE){
                    return true;
                }
            }
        }

        //if there is no tiles reach max value
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.

        //directional array
        //right - left - up - down
        int[] rowDirection = {0, 0, -1, 1};
        int[] colDirection = {1, -1, 0, 0};


        /*check if there is any free space in the board*/
        if (emptySpaceExists(b)){
            return true;
        }

        /*check if there is no space exist and There are two adjacent tiles with the same value*/

        //get grid size(number of rows and cols)
        int gridSize = b.size();

        //loop on Rows
        for (int colIterator = 0; colIterator < gridSize; ++colIterator){

            //loop on Cols
            for (int rowIterator = 0; rowIterator < gridSize; ++rowIterator){

                //get the value of the current tile
                int currentTileValue = b.tile(colIterator, rowIterator).value();

                //check four neighbours of the current tile
                for (int directions = 0; directions < 4; ++directions){

                    //get index of the neighbour
                    int rowIndex = rowIterator + rowDirection[directions];
                    int colIndex = colIterator + colDirection[directions];

                    //check if the rowIndex or colIndex is out of grid bounds
                    if(rowIndex < 0 || colIndex < 0 || rowIndex >= gridSize || colIndex >= gridSize){
                        continue;
                    }
                    int neighbourTileValue = b.tile(rowIndex, colIndex).value();

                    if(currentTileValue == neighbourTileValue){
                        return true;
                    }

                }

            }
        }

        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
