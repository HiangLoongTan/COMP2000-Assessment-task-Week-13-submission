import java.awt.Color;

public class Elements {
    boolean moveable;
    boolean canBurn;
    Color color;
    boolean reverseGravity;

    public Elements(boolean moveable, boolean canBurn,
            Color color, boolean reverseGravity) {
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

    public void step(Physics physics, int row, int col) {
        // Default: stay still.
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
    public void step(Physics physics, int row, int col) {
        physics.fallDown(row, col);
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
            new Color(255, 240, 180),
            new Color(255, 190, 60),
            new Color(255, 140, 20),
            new Color(230, 80, 10),
            new Color(180, 35, 0)
    };

    private static final long LIFETIME = 3400;
    private final long bornAt = System.currentTimeMillis();

    public Fire() {
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
    public void step(Physics physics, int row, int col) {
        physics.floatUp(row, col);
    }

    @Override
    public Color getColor() {
        long age = System.currentTimeMillis() - bornAt;

        if (age >= LIFETIME) {
            return Color.DARK_GRAY;
        }

        Color flame = FLAMES[(int) (Math.random() * FLAMES.length)];

        if (age > LIFETIME - 1000) {
            float f = (age - (LIFETIME - 1000)) / 1000f;

            return new Color(
                    (int) (flame.getRed() + f * (64 - flame.getRed())),
                    (int) (flame.getGreen() + f * (64 - flame.getGreen())),
                    (int) (flame.getBlue() + f * (64 - flame.getBlue())));
        }

        return flame;
    }

    public long getAge() {
        return System.currentTimeMillis() - bornAt;
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
    public void step(Physics physics, int row, int col) {
        physics.floatUp(row, col);
    }
}