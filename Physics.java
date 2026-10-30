import java.awt.Color;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Physics {

    List<Reaction> reactions = List.of( // every rule in the simulation, checked in this order
            new Reaction(e -> e instanceof Fire, e -> e instanceof Gas, 0.25, Explosion::new), // fire next to gas, 25% chance each frame to explode
            new Reaction(e -> e instanceof Explosion, e -> e instanceof Gas, 0.9, Explosion::new)); // explosion next to gas, 90% chance, so the blast spreads fast

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
    private final List<Long> completedFireLifespans = new ArrayList<>();

    private ScheduledExecutorService simulationExecutor;
    private static final int TICKS_PER_SECOND = 60;

    public Physics() {
        tester.set(new Sand());
        grid = new Elements[ROWS][COLS];
    }

    int frameCounter = 0;
    private boolean reverseOrder = false;

    public synchronized Map<String, Long> countPixelsByType() {
        return Arrays.stream(grid)
                .parallel()
                .flatMap(Arrays::stream)
                .filter(e -> e != null)
                .collect(Collectors.groupingByConcurrent(
                        e -> e.getClass().getSimpleName(),
                        Collectors.counting()));
    }

    public void startSimulation(Runnable onTick) {
        if (simulationExecutor != null && !simulationExecutor.isShutdown()) {
            return;
        }

        simulationExecutor = Executors.newSingleThreadScheduledExecutor();

        long tickDuration = 1_000_000_000L / TICKS_PER_SECOND;

        simulationExecutor.scheduleAtFixedRate(() -> {
            step();
            onTick.run();
        }, 0, tickDuration, TimeUnit.NANOSECONDS);
    }

    public void stopSimulation() {
        if (simulationExecutor != null) {
            simulationExecutor.shutdownNow();
            simulationExecutor = null;
        }
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

    public synchronized void clearGrid() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                grid[row][col] = null;
            }
        }
    }

    public synchronized void step() {
        frameCounter++;
        FireStep();
        reactionStep();

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
                    System.out.println("Water sideways error: " + e.getMessage());
                }
            }
        }
    }

    public synchronized void FireStep() {
        IntStream.range(0, ROWS)
                .boxed()
                .flatMap(row -> IntStream.range(0, COLS)
                        .mapToObj(col -> new int[] { row, col }))
                .filter(pos -> grid[pos[0]][pos[1]] instanceof Fire fire
                        && deadFire.test(fire))
                .forEach(pos -> {
                    Fire fire = (Fire) grid[pos[0]][pos[1]];

                    completedFireLifespans.add(fire.getAge());
                    grid[pos[0]][pos[1]] = null;
                });
    }

    public synchronized double averageFireLifespan() {
        return completedFireLifespans.stream()
                .parallel()
                .collect(Collector.of(
                        () -> new long[2],

                        (result, age) -> {
                            result[0] += age;
                            result[1]++;
                        },

                        (a, b) -> {
                            a[0] += b[0];
                            a[1] += b[1];
                            return a;
                        },

                        result -> result[1] == 0
                                ? 0.0
                                : (double) result[0] / result[1]));
    }

    public Stream<Elements> neighbours(int row, int col) { // gives back the elements touching the cell at this row and column
       
        return IntStream.rangeClosed(-1, 1)  // We start a stream of numbers, offsets for the rows        
                .boxed() // we turn the numbers into objects so flatmap is usable
                .flatMap(dRow -> IntStream.rangeClosed(-1, 1)  // we get every positon around the cell through flatmap, using each row offset and each column asset
                // getting all 9 position positions around the cell
                        .mapToObj(dCol -> new int[] { row + dRow, col + dCol })) // turns the offsets into real rows and columns
                .filter(pos -> !(pos[0] == row && pos[1] == col)) // removes the cell itself, it isn't its own neighbour
                .filter(pos -> pos[0] >= 0 && pos[0] < ROWS // Does this spot actually exist on the grid ?
                        && pos[1] >= 0 && pos[1] < COLS)
                .map(pos -> grid[pos[0]][pos[1]])  // What is actually in that spot ?
                .filter(Objects::nonNull); // Is there nothing in that spot at all ?
    }

    public void reactionStep() { // checks every rule against every cell on the grid
        for (Reaction reaction : reactions) {           // we check through each rule, one at a time (which rule am i checking ?)
            List<int[]> reacting = IntStream.range(0, ROWS) // where do I keep the cells th
                    .boxed() // Can I turn these plain numbers into objects ?
                    .flatMap(row -> IntStream.range(0, COLS) // we find what the columns are for this row
                            .mapToObj(col -> new int[] { row, col })) // we find the exact position of each cell
                    .filter(pos -> reaction.matches(this, pos[0], pos[1])) // we ask, does the cell at this position react to with something?
                    .collect(Collectors.toList()); // saves the cells that said yes in the reacting list

            reacting.forEach(pos -> grid[pos[0]][pos[1]] = reaction.produce()); // Now i know who reacts to what, so what do they become ?
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
