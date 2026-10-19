import java.awt.Color;

public class Elements {
    boolean moveable;
    boolean canBurn;
    Color color;
    boolean reverseGravity;

    public Elements(boolean moveable, boolean canBurn, Color color, boolean reverseGravity) {
        this.moveable = moveable;
        this.canBurn = canBurn;
        this.color = color;
        this.reverseGravity = reverseGravity;
    }

    public Color getColor() {
        return color;
    }

    public boolean fallsDown() {
        return false;
    }

    public boolean floatsUp() {
        return false;
    }

    public void step(Physics sandbox, int row, int col) {
        // Default: stay still (Fire).
    }

    protected void fallDown(Physics sandbox, int row, int col) { //This method is about one particle, in this row and this column
        
        int below = row + 1;
        if (sandbox.isEmpty(below, col)) { //If the particle below us is empty then we move down.
            
            try {
                // we ask sandbox to move the particle from its current position to the one below it.
                // this will empty the pixel in its current position and fill the one below it.
                sandbox.moveElement(row, col, below, col);
            } catch (Exception e) { // move element might refuse
                System.out.println("Gravity error: " + e.getMessage());
                // we catch it so the entire program doesnt crash, rather we just print the reason.
            }
        } else { // the square beneath ISNT empty so we remain in place and try to move the particle to the side.
            sandbox.side_gravity(row, col);
        }
    }

    protected void floatUp(Physics sandbox, int row, int col) { // This method is about one particle, in this row and this column. Gases use it to go up.
        if (Math.random() < 0.6) { // 60% of the time we dont go straight, but drift apart
            try { // try and move this particle diagonally above
                sandbox.reverse_side_gravity(row, col);
            } catch (Exception e) { // move wasnt allowed so we print the reason.
                System.out.println("Reverse side gravity error: " + e.getMessage());
            }
        } else {
            int above = row - 1;
            if (sandbox.isEmpty(above, col)) {
                try { // try and move this particle up.
                    sandbox.moveElement(row, col, above, col);
                } catch (Exception e) { // move element might refuse
                    System.out.println("Reverse gravity error: " + e.getMessage());
                }
            }
        }
    }
}

class Sand extends Elements {
    public Sand() {
        super(true, false, Color.YELLOW, false);
    }

    @Override
    public boolean fallsDown() {
        return true;
    }

    @Override
    public void step(Physics sandbox, int row, int col) {
        fallDown(sandbox, row, col);
    }
}

class Water extends Elements {
    public Water() {
        super(true, false, Color.BLUE, false);
    }

    @Override
    public boolean fallsDown() {
        return true;
    }

    @Override
    public void step(Physics sandbox, int row, int col) {
        int below = row + 1; // the cell directly below

        if (sandbox.isEmpty(below, col)) { // empty below, so fall straight down
            fallDown(sandbox, row, col);
            return; // already moved, don't also slide this frame
        }

        sandbox.side_gravity(row, col); // below is blocked, so try down-left or down-right

        if (sandbox.grid[row][col] == null) { // diagonal move worked, this cell is now empty
            return;
        }

        boolean tryRight = sandbox.frameCounter % 2 == 0; // even tick go right, odd tick go left
        int side;
        if (tryRight) {
            side = col + 1; // column to the right
        } else {
            side = col - 1; // column to the left
        }

        if (sandbox.isEmpty(row, side)) { // that neighbour on the same row is empty
            try {
                sandbox.moveElement(row, col, row, side); // slide this water into it
            } catch (Exception e) { // move was refused, so print why instead of crashing
                System.out.println("Water flow error: " + e.getMessage());
            }
        }
    }
}

class Fire extends Elements {

    private static final Color[] FLAMES = {
        new Color(255, 240, 180), // white hot
        new Color(255, 190, 60),  // yellow
        new Color(255, 140, 20),  // orange
        new Color(230, 80, 10),   // deep orange
        new Color(180, 35, 0)     // red
    };

    private static final long LIFETIME = 3400;

    private final long bornAt = System.currentTimeMillis();

    public Fire() {
        // reverseGravity = true -> fire rises like a real flame instead of piling up like sand
        super(false, true, Color.ORANGE, true);
    }

    public boolean isDead() {
        return System.currentTimeMillis() - bornAt >= LIFETIME;
    }

    @Override
    public boolean floatsUp() {
        return true;
    }

    @Override
    public void step(Physics sandbox, int row, int col) {
        floatUp(sandbox, row, col);
    }

    @Override
    public Color getColor() {
        long age = System.currentTimeMillis() - bornAt;

        if (age >= LIFETIME) {
            return Color.DARK_GRAY;
        }

        Color flame = FLAMES[(int) (Math.random() * FLAMES.length)];

        // fade out over the last second of its life
        if (age > LIFETIME - 1000) {
            float f = (age - (LIFETIME - 1000)) / 1000f;
            return new Color(
                (int) (flame.getRed() + f * (64 - flame.getRed())),
                (int) (flame.getGreen() + f * (64 - flame.getGreen())),
                (int) (flame.getBlue() + f * (64 - flame.getBlue())));
        }

        return flame;
    }
}

class Gas extends Elements {
    public Gas() {
        super(false, true, Color.WHITE, true);
    }

    @Override
    public boolean floatsUp() {
        return true;
    }

    @Override
    public void step(Physics sandbox, int row, int col) {
        floatUp(sandbox, row, col);
    }
}
