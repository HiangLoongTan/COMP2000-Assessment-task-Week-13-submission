import java.awt.Color;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class Physics {

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

    public Physics() {
        tester.set(new Sand());
        grid = new Elements[ROWS][COLS];
        grid[20][40] = tester.get();
    }

    int frameCounter = 0;
    private boolean reverseOrder = false;

    public Map<String, Long> countPixelsByType() {
        return Arrays.stream(grid)
                .flatMap(Arrays::stream)
                .filter(e -> e != null)
                .collect(Collectors.groupingBy(
                        e -> e.getClass().getSimpleName(),
                        Collectors.counting()));
    }

    public boolean isEmpty(int row, int col) { // checks if the square is on the grid
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return false;
        }
        return grid[row][col] == null;
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
        if (grid[row][col] == null) {
            throw new Exception("Cannot move an empty cell.");
        }
        if (grid[newRow][newCol] != null) {
            throw new Exception("Cannot move element into an occupied cell.");
        }
        // Move element
        grid[newRow][newCol] = grid[row][col];
        grid[row][col] = null;
    }

    public void clearGrid() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                grid[row][col] = null;
            }
        }
    }

    public void step() {
        frameCounter++;
        FireStep();

        // Normal gravity
        for (int row = ROWS - 1; row >= 0; row--) {
            for (int i = 0; i < COLS; i++) {
                int col = reverseOrder ? COLS - 1 - i : i;

                Elements e = grid[row][col];

                if (e instanceof Water) {
                    if (!fallStraightDown(row, col)
                            && !flowDiagonally(row, col)) {
                        spreadSideways(row, col);
                    }
                } else if (e != null && falling.test(e)) {
                    e.step(this, row, col);
                }
            }
        }

        // Reverse gravity
        if (frameCounter % 2 == 0) {
            for (int row = 1; row < ROWS; row++) {
                for (int i = 0; i < COLS; i++) {
                    int col = reverseOrder ? COLS - 1 - i : i;

                    Elements e = grid[row][col];

                    if (e != null && floating.test(e)) {
                        e.step(this, row, col);
                    }
                }
            }
        }

        // Alternate scan direction each frame
        reverseOrder = !reverseOrder;
    }

    public void side_gravity(int row, int col) {
        // There is no space below the bottom row
        if (row + 1 >= ROWS) {
            return;
        }

        int direction = Math.random() < 0.5 ? -1 : 1;

        // Try the first diagonal direction
        if (col + direction >= 0 && col + direction < COLS
                && grid[row + 1][col + direction] == null) {

            grid[row + 1][col + direction] = grid[row][col];
            grid[row][col] = null;

            // Try the opposite diagonal direction
        } else if (col - direction >= 0 && col - direction < COLS
                && grid[row + 1][col - direction] == null) {

            grid[row + 1][col - direction] = grid[row][col];
            grid[row][col] = null;
        }
    }

    public void reverse_side_gravity(int row, int col) {
        int direction = Math.random() < 0.5 ? -1 : 1;
        // Otherwise, try moving horizontally
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

    // Normal gravity: particles fall down or diagonally.
    public void fallDown(int row, int col) {
        // Step 1: Try to fall straight down
        if (isEmpty(row + 1, col)) {
            try {
                moveElement(row, col, row + 1, col);
            } catch (Exception e) {
                System.out.println("Gravity error: " + e.getMessage());
            }
            return;
        }

        if (grid[row][col] instanceof Water) {
            int direction = Math.random() < 0.5 ? -1 : 1;

            for (int dir : new int[] { direction, -direction }) {
                if (isEmpty(row + 1, col + dir)) {
                    try {
                        moveElement(row, col, row + 1, col + dir);
                    } catch (Exception e) {
                        System.out.println("Water diagonal error: " + e.getMessage());
                    }
                    return;
                }
            }

            for (int dir : new int[] { direction, -direction }) {
                if (isEmpty(row, col + dir)) {
                    try {
                        moveElement(row, col, row, col + dir);
                    } catch (Exception e) {
                        System.out.println("Water sideways error: " + e.getMessage());
                    }
                    return;
                }
            }
        } else {
            // Normal gravity for sand and other falling elements
            side_gravity(row, col);
        }
    }

    // Reverse gravity: particles float up or drift sideways.
    public void floatUp(int row, int col) {
        if (Math.random() < 0.6) {
            reverse_side_gravity(row, col);
        } else {
            int above = row - 1;

            if (isEmpty(above, col)) {
                try {
                    moveElement(row, col, above, col);
                } catch (Exception e) {
                    System.out.println("Reverse gravity error: " + e.getMessage());
                }
            }
        }
    }

    private boolean fallStraightDown(int row, int col) {
        if (isEmpty(row + 1, col)) {
            try {
                moveElement(row, col, row + 1, col);
                return true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return false;
    }

    private boolean flowDiagonally(int row, int col) {
        int direction = Math.random() < 0.5 ? -1 : 1;

        for (int dir : new int[] { direction, -direction }) {
            int newCol = col + dir;

            if (isEmpty(row + 1, newCol)) {
                try {
                    moveElement(row, col, row + 1, newCol);
                    return true;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        return false;
    }

    private void spreadSideways(int row, int col) {
        int direction = Math.random() < 0.5 ? -1 : 1;

        for (int dir : new int[] { direction, -direction }) {
            int newCol = col + dir;

            if (isEmpty(row, newCol)) {
                try {
                    moveElement(row, col, row, newCol);
                    return;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void FireStep() { // BUG - FIRE IS NOT ACTUALLY DELETED
        // Remove fire that has burnt out
        Arrays.stream(grid)
                .map(Arrays::stream)
                .filter(e -> e instanceof Fire fire && deadFire.test(fire))
                .forEach(e -> {
                    for (int row = 0; row < ROWS; row++) {
                        for (int col = 0; col < COLS; col++) {
                            if (grid[row][col] == e) {
                                grid[row][col] = null;
                            }
                        }
                    }
                });
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
