package stonesmoving.state;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Data
@Slf4j
public class StoneState implements Cloneable {

    public static final int[][] INITIAL = {
            {0, 1, 2, 1, 1, 2, 1, 2},
            {1, 1, 1, 1, 1, 1, 1, 2},
            {1, 1, 1, 2, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1},
            {2, 1, 1, 1, 1, 1, 1, 1},
            {1, 2, 1, 1, 1, 1, 1, 2},
            {1, 1, 1, 2, 1, 1, 1, 1},
            {1, 2, 1, 2, 1, 1, 2, 1}
    };

    public static final int[][] GOAL = {
            {1, 1, 2, 1, 1, 2, 1, 2},
            {1, 1, 1, 1, 1, 1, 1, 2},
            {1, 1, 1, 2, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1},
            {2, 1, 1, 1, 1, 1, 1, 1},
            {1, 2, 1, 1, 1, 1, 1, 2},
            {1, 1, 1, 2, 1, 1, 1, 1},
            {1, 2, 1, 2, 1, 1, 2, 0}
    };

    public static final int[][] CURRENT = {
            {1, 1, 2, 1, 1, 2, 1, 2},
            {1, 1, 1, 1, 1, 1, 1, 2},
            {1, 1, 1, 2, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1},
            {2, 1, 1, 1, 1, 1, 1, 1},
            {1, 2, 1, 1, 1, 1, 1, 2},
            {1, 1, 1, 2, 1, 1, 1, 1},
            {1, 2, 1, 2, 1, 1, 2, 1}
    };

    @Setter(AccessLevel.NONE)
    private Board[][] matrix;

    @Setter(AccessLevel.NONE)
    private int stoneRow;

    @Setter(AccessLevel.NONE)
    private int stoneCol;

    public StoneState() {
        this(INITIAL);
    }

    public StoneState(int[][] a) {
        if (!isValidMatrix(a)) {
            throw new IllegalArgumentException();
        }
        gameInitial(a);
    }

    private boolean isValidMatrix(int[][] a) {
        if (a == null || a.length != 8) {
            return false;
        }
        boolean foundText = false;
        for (int[] row : a) {
            if (row == null || row.length != 8) {
                return false;
            }
            for (int space : row) {
                if (space < 0 || space >= Board.values().length) {
                    return false;
                }
                if (space == Board.STONE.getValue()) {
                    if (foundText) {
                        return false;
                    }
                    foundText = true;
                }
            }
        }
        return foundText;
    }

    private void gameInitial(int[][] a) {
        this.matrix = new Board[8][8];
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if ((this.matrix[i][j] = Board.of(a[i][j])) == Board.STONE) {
                    stoneRow = i;
                    stoneCol = j;
                }
            }
        }
    }

    public boolean isSolved() {
        if ((matrix[7][7] = Board.of(GOAL[7][7])) == Board.STONE)
            return true;
        return false;
    }

    public boolean canBeMoved(int row, int col) {
        if (matrix[row][col] == Board.FRAMED)
            return 0 <= row && row <= 7 && 0 <= col && col <= 7 &&
                    Math.abs(stoneRow - row) == 1 && Math.abs(stoneCol - col) == 1;
        else if (matrix[row][col] == Board.UNFRAMED)
            return 0 <= row && row <= 7 && 0 <= col && col <= 7 &&
                    Math.abs(stoneRow - row) + Math.abs(stoneCol - col) == 1;
        else
            return false;
    }

    public void moveToNext(int row, int col) {
        try {
            if (matrix[row][col] != Board.STONE)
                matrix[row][col] = Board.STONE;
            for (int i = row - 1; i <= row + 1; i++)
                for (int j = col - 1; j <= col + 1; j++){
                    if(i<0||i>7||j<0||j>7);
                    else if(row==i && col==j);
                        else if (matrix[row][col] == Board.STONE && matrix[i][j] == Board.STONE )
                             {  matrix[i][j] = Board.of(CURRENT[i][j]); break;}

                }
        } catch (Exception e) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public StoneState clone() {
        StoneState copy = null;
        try {
            copy = (StoneState) super.clone();
        } catch (CloneNotSupportedException e) {
        }
        copy.matrix = new Board[matrix.length][];
        for (int i = 0; i < matrix.length; ++i) {
            copy.matrix[i] = matrix[i].clone();
        }
        return copy;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Board[] row : matrix) {
            for (Board format : row) {
                sb.append(format).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        StoneState state = new StoneState();
        System.out.println(state);
        state.moveToNext(0, 1);
        System.out.println(state);
    }

}
