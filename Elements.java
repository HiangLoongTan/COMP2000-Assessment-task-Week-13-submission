import java.awt.Color;

public class Elements {
    boolean moveable;
    boolean canBurn;
    Color color;
    boolean reverseGravity;
    boolean transparent;

    public Elements(boolean moveable, boolean canBurn, Color color, boolean reverseGravity, Boolean transparent) {
        this.moveable = moveable;
        this.canBurn = canBurn;
        this.color = color;
        this.reverseGravity = reverseGravity;
        this.transparent = transparent;
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
        super(true, false, Color.YELLOW, false, false);
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
        super(true, false, Color.BLUE, false, false);
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
        super(false, true, Color.ORANGE, true, false);
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

class Explosion extends Fire { //Explosions are very short livedd, so they chain and clean up the fire
    private static final Color[] BLAST = { // it flashes colours from white to orange
        new Color(255, 255, 235),
        new Color(255, 235, 140),
        new Color(255, 190, 70),
        new Color(255, 120, 20)
    };

    private static final long BLAST_LIFETIME = 600; // the lifetime of an explosion
    private final long explodedAt = System.currentTimeMillis(); // the time/moment the explosion gets created

    @Override
    public boolean isDead() { // Firestep calls this to know when to remove the pixels due to the explosion
        return System.currentTimeMillis() - explodedAt >= BLAST_LIFETIME;
    }

    @Override
    public Color getColor() {
        long age = System.currentTimeMillis() - explodedAt; // how old this explosion is, in milliseconds
        Color flash = BLAST[(int) (Math.random() * BLAST.length)]; // picks a random flash colour, which makes it flicker

        if (age < BLAST_LIFETIME / 2) { // first half of its life
            return flash; // full bright flash/no fading
        }

        float f = (age - BLAST_LIFETIME / 2) / (BLAST_LIFETIME / 2f); // second half of its life
        f = Math.min(f, 1f); // never goes above 1, so the colour stops fading at the end

        return new Color( // the blending from a flash colour towards a dark red brown colour
            (int) (flash.getRed() + f * (90 - flash.getRed())), // red moves toward 90
            (int) (flash.getGreen() + f * (30 - flash.getGreen())), // green moves toward 30
            (int) (flash.getBlue() + f * (10 - flash.getBlue())) // blue moves toward 10
        );
    }
}

class Gas extends Elements {
    public Gas() {
        super(false, true, Color.WHITE, true, false);
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

class Glass extends Elements {
    public Glass() {
        super(false, false, new Color(180, 220, 255, 100), false, true);
    }

    @Override
    public boolean floatsUp() {
        return false;
    }
}