import java.awt.*;
import java.awt.image.BufferedImage;

// [NEW FILE] Draws the simulation with realistic colours, fire glow and a brush outline.
// SimulationPanel only needs one extra line to use this, so the team's code stays the same.
public class RealisticRenderer {

    // one pixel per grid cell, stretched up to the panel size when drawn
    private static final BufferedImage particles =
            new BufferedImage(Physics.COLS, Physics.ROWS, BufferedImage.TYPE_INT_ARGB);

    public static void draw(Graphics graphics, Component panel, Physics sandbox, int brushSize) {
        Graphics2D g = (Graphics2D) graphics.create();
        int s = Physics.CELL_SIZE, w = Physics.COLS * s, h = Physics.ROWS * s;
        double t = System.nanoTime() / 1e9; // time in seconds, makes water shimmer and gas pulse
        BufferedImage glow = new BufferedImage(Physics.COLS / 8 + 1, Physics.ROWS / 8 + 1, BufferedImage.TYPE_INT_ARGB);
        float[][] heat = new float[glow.getHeight()][glow.getWidth()]; // how much fire is in each 8x8 area

        g.setPaint(new GradientPaint(0, 0, new Color(18, 22, 34), 0, h, new Color(46, 42, 44)));
        g.fillRect(0, 0, w, h); // dark gradient background

        synchronized (sandbox) {
            for (int col = 0; col < Physics.COLS; col++) {
                int depth = 0; // water cells stacked in this column, deeper water is darker
                for (int row = 0; row < Physics.ROWS; row++) {
                    Elements e = sandbox.grid[row][col];
                    depth = e instanceof Water ? depth + 1 : 0;
                    if (e instanceof Fire) heat[row / 8][col / 8] += e instanceof Explosion ? 3 : 1;
                    particles.setRGB(col, row, colourOf(sandbox, e, row, col, depth, t));
                }
            }
        }
        for (int r = 0; r < heat.length; r++) // turn heat into a soft orange glow
            for (int c = 0; c < heat[0].length; c++)
                glow.setRGB(c, r, argb(Math.min(140, heat[r][c] * 2.5), 255, 150, 40));

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(particles, 0, 0, w, h, null); // sharp pixels
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(glow, -4 * s, -4 * s, glow.getWidth() * 8 * s, glow.getHeight() * 8 * s, null); // smooth glow

        Point m = panel.getMousePosition(); // brush preview outline (null when the mouse is outside)
        if (m != null && !sandbox.LinesSelected) {
            g.setColor(sandbox.eraserSelected ? new Color(255, 90, 90, 180) : new Color(255, 255, 255, 140));
            g.drawRect((m.x / s - brushSize / 2) * s, (m.y / s - brushSize / 2) * s, brushSize * s - 1, brushSize * s - 1);
        }
        g.dispose();
    }

    // Picks a realistic colour for one cell
    private static int colourOf(Physics sandbox, Elements e, int row, int col, int depth, double t) {
        if (e == null) return 0; // empty, see-through
        int hash = (row * 73856093 ^ col * 19349663) * 0x9E3779B1;
        float v = ((hash ^ hash >>> 16) & 255) / 255f; // fixed 0-1 value per cell, varies shades
        Elements above = row > 0 ? sandbox.grid[row - 1][col] : null;
        Elements below = row + 1 < Physics.ROWS ? sandbox.grid[row + 1][col] : null;

        if (e instanceof Sand) { // grainy, darker when wet
            double k = v * 36 - 18, wet = (above instanceof Water || below instanceof Water) ? 0.65 : 1;
            return argb(255, (214 + k) * wet, (182 + k) * wet, (118 + k / 2) * wet);
        }
        if (e instanceof Water) { // light surface, darker when deep, gentle shimmer
            double d = Math.min(depth, 30) / 30.0, sh = Math.sin(t * 3 + col * 0.35 + row * 0.15) * 8 + (depth == 1 ? 40 : 0);
            return argb(215, 70 - 55 * d + sh, 150 - 90 * d + sh, 225 - 85 * d + sh);
        }
        if (e instanceof Gas) { // soft see-through cloud
            double c = 200 + v * 30;
            return argb(60 + v * 50 + Math.sin(t * 2 + v * 10) * 15, c, c + 4, c + 10);
        }
        if (e instanceof Glass) { // bright top edge, diagonal shine lines
            if (!(above instanceof Glass)) return argb(190, 235, 248, 255);
            return (row + col) % 9 == 0 ? argb(150, 225, 242, 255) : argb(90, 170, 215, 250);
        }
        if (e instanceof Wall) { // stone, lit on top and shadowed underneath
            double c = 94 + v * 28 + (above == null ? 30 : 0) - (row + 1 < Physics.ROWS && below == null ? 30 : 0);
            return argb(255, c, c, c + 6);
        }
        return e.getColor().getRGB(); // Fire and Explosion keep their own flickering colours
    }

    // Packs alpha, red, green, blue (each kept between 0 and 255) into one colour number
    private static int argb(double... parts) {
        int out = 0;
        for (double p : parts) out = (out << 8) | (int) Math.max(0, Math.min(255, p));
        return out;
    }
}