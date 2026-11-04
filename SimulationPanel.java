
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SimulationPanel extends JPanel {

    Physics sandbox;

    private long lastFpsUpdate = System.nanoTime();
    private int framesSinceUpdate = 0;
    private int fps = 0;
    private int brushSize = 1;

    public SimulationPanel(Physics sandbox) {
        this.sandbox = sandbox;

        // Converting mouse to pixels and allows us to spawn our material
        MouseAdapter mouse = new MouseAdapter() {
            void place(MouseEvent e) {
                int col = e.getX() / Physics.CELL_SIZE - brushSize / 2;
                int row = e.getY() / Physics.CELL_SIZE - brushSize / 2;
                synchronized (sandbox) {
                    for (int r = 0; r < brushSize; r++) {
                        for (int c = 0; c < brushSize; c++) {
                            int newRow = row + r;
                            int newCol = col + c;
                            if (newRow >= 0 && newRow < Physics.ROWS && newCol >= 0 && newCol < Physics.COLS) {
                                if (sandbox.eraserSelected) {
                                    sandbox.grid[newRow][newCol] = null;
                                } else {
                                    Elements selected = sandbox.tester.get();
                                    sandbox.grid[newRow][newCol] = selected instanceof Fire ? new Fire() : selected;
                                }
                            }
                        }
                    }
                }
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                place(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                place(e);
            }
        };

        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        // Simulation Timer
        Timer timer = new Timer(16, e -> {
            try {
                sandbox.step();
                repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        timer.start();
    }

    // called whenever the panel needs to redraw
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int s = Physics.CELL_SIZE;

        // Draw all particles
        synchronized (sandbox) {
            for (int row = 0; row < Physics.ROWS; row++) {
                for (int col = 0; col < Physics.COLS; col++) {

                    Elements element = sandbox.grid[row][col];

                    if (element != null) {
                        if (element.transparent) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setComposite(AlphaComposite.SrcOver);
                            g2.setColor(element.getColor());
                            g2.fillRect(col * s, row * s, s, s);
                            g2.dispose();
                        } else {
                            g.setColor(element.getColor());
                            g.fillRect(col * s, row * s, s, s);
                        }
                    }
                }
            }
        }

        // Count rendered frames
        framesSinceUpdate++;

        long currentTime = System.nanoTime();
        long elapsed = currentTime - lastFpsUpdate;

        // Update FPS approximately once per second
        if (elapsed >= 1_000_000_000L) {
            fps = (int) (framesSinceUpdate
                    * 1_000_000_000L / elapsed);

            framesSinceUpdate = 0;
            lastFpsUpdate = currentTime;
        }
    }

    public int getFps() {
        return fps;
    }

    public void setBrushSize(int size) {
        brushSize = size;
    }

}
