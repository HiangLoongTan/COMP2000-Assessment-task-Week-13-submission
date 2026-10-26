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
                (int) (flame.getBlue() + f * (64 - flame.getBlue()))
            );
        }

        return flame;
    }
}

class Explosion extends Fire {
    private static final Color[] BLAST = {
        new Color(255, 255, 235),
        new Color(255, 235, 140),
        new Color(255, 190, 70),
        new Color(255, 120, 20)
    };

    private static final long BLAST_LIFETIME = 600;
    private final long explodedAt = System.currentTimeMillis();

    @Override
    public boolean isDead() {
        return System.currentTimeMillis() - explodedAt >= BLAST_LIFETIME;
    }

    @Override
    public Color getColor() {
        long age = System.currentTimeMillis() - explodedAt;
        Color flash = BLAST[(int) (Math.random() * BLAST.length)];

        if (age < BLAST_LIFETIME / 2) {
            return flash;
        }

        float f = (age - BLAST_LIFETIME / 2) / (BLAST_LIFETIME / 2f);
        f = Math.min(f, 1f);

        return new Color(
            (int) (flash.getRed() + f * (90 - flash.getRed())),
            (int) (flash.getGreen() + f * (30 - flash.getGreen())),
            (int) (flash.getBlue() + f * (10 - flash.getBlue()))
        );
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