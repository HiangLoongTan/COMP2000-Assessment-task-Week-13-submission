import java.awt.Color;
import java.util.function.Predicate;
import java.util.function.Function;

public class Sandbox {

    // Checks whether an element is a dead fire particle
    Predicate<Fire> deadFire = Fire::isDead;

    // Checks whether an element falls down
    Predicate<Elements> falling = Elements::fallsDown;

    // Checks whether an element floats up
    Predicate<Elements> floating = Elements::floatsUp;

    // Converts an element into its colour
    Function<Elements, Color> elementColor = Elements::getColor;

    static int WIDTH = 800;
    static int HEIGHT = 600;
    static int CELL_SIZE = 4;

    static int COLS = WIDTH / CELL_SIZE;
    static int ROWS = HEIGHT / CELL_SIZE;

    Elements[][] grid;

    Tester<Elements> tester = new Tester<>();

    public Sandbox() {
        tester.set(new Sand());
        grid = new Elements[ROWS][COLS];
        grid[20][40] = tester.get();
    }

    int frameCounter = 0;

    public boolean isEmpty(int row, int col) { // checks if the square is on the grid
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return false; // this position is outside of the grid and treated as not empty
        }
        return grid[row][col] == null; // this cell has no element and is treated as empty
    }

    // Checks that a position is inside the grid
    public void checkPosition(int row, int col) throws Exception {
        if (row < 0 || row >= ROWS ||
                col < 0 || col >= COLS) {
            throw new Exception(
                    "Invalid grid position: [" + row + "][" + col + "]");
        }
    }

    public void moveElement(int row, int col, int newRow, int newCol) throws Exception {
        // Check both positions
        checkPosition(row, col);
        checkPosition(newRow, newCol);
        // Check that there is actually an element to move
        if (grid[row][col] == null) {
            throw new Exception("Cannot move an empty cell.");
        }
        // Check that the destination is empty
        if (grid[newRow][newCol] != null) {
            throw new Exception("Cannot move element into an occupied cell.");
        }
        // Move element
        grid[newRow][newCol] = grid[row][col];
        grid[row][col] = null;
    }

    public void step() {
        frameCounter++;
        // Remove fire that has burnt out, otherwise it stays in the grid and piles up
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Elements e = grid[row][col];

                if (e instanceof Fire fire && deadFire.test(fire)) {
                    grid[row][col] = null;
                }
            }
        }

        for (int row = ROWS - 2; row >= 0; row--) {
            for (int col = 0; col < COLS; col++) {
                Elements e = grid[row][col];

                if (e != null && falling.test(e)) {
                    e.step(this, row, col);
                }
            }
        }

        //Reverse Gravity - frameCounter exists as the pixels insta-teleport to the top otherwise
        if (frameCounter % 2 == 0) {
            for (int row = 1; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    Elements e = grid[row][col];

                    if (e != null && floating.test(e)) {
                        e.step(this, row, col);
                    }
                }
            }
        }
    }

    public void side_gravity(int row, int col) {
        int direction = Math.random() < 0.5 ? -1 : 1;
        if (col + direction >= 0 && col + direction < COLS
                && grid[row + 1][col + direction] == null) {
            grid[row + 1][col + direction] = grid[row][col];
            grid[row][col] = null;
        } else if (col - direction >= 0 && col - direction < COLS
                && grid[row + 1][col - direction] == null) {
            grid[row + 1][col - direction] = grid[row][col];
            grid[row][col] = null;
        }
    }

    public void reverse_side_gravity(int row, int col) {
        int direction = Math.random() < 0.5 ? -1 : 1;
        if (col + direction >= 0 && col + direction < COLS
                && grid[row][col + direction] == null) {
            grid[row][col + direction] = grid[row][col];
            grid[row][col] = null;
        } else if (col - direction >= 0 && col - direction < COLS
                && grid[row][col - direction] == null) {
            grid[row][col - direction] = grid[row][col];
            grid[row][col] = null;
        }
    }

    // Generic tester class
    class Tester<T> {

        T value;

        void set(T value) {
            this.value = value;
        }

        T get() {
            return value;
        }
    }
}
