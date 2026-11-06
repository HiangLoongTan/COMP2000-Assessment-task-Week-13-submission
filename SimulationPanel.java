
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SimulationPanel extends JPanel {

    Physics sandbox;

    private long lastFpsUpdate = System.nanoTime();
    private int framesSinceUpdate = 0;
    private int fps = 0;
    private int brushSize = 1;
    private int startRow;
    private int startCol;
    private boolean drawingLine = false;

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

       private void drawLine(int row1, int col1, int row2, int col2) {

    int dx = Math.abs(col2 - col1);
    int dy = Math.abs(row2 - row1);

    int sx = col1 < col2 ? 1 : -1;
    int sy = row1 < row2 ? 1 : -1;

    int error = dx - dy;

    synchronized (sandbox) {

        while (true) {

            if (row1 >= 0 && row1 < Physics.ROWS &&
                col1 >= 0 && col1 < Physics.COLS) {

                sandbox.grid[row1][col1] = new Wall();
            }

            if (row1 == row2 && col1 == col2) {
                break;
            }

            int e2 = 2 * error;

            if (e2 > -dy) {
                error -= dy;
                col1 += sx;
            }

            if (e2 < dx) {
                error += dx;
                row1 += sy;
            }
        }
    }

    repaint();

    
}
          @Override
public void mousePressed(MouseEvent e) {

    if (sandbox.LinesSelected) {

        startCol = e.getX() / Physics.CELL_SIZE;
        startRow = e.getY() / Physics.CELL_SIZE;

        drawingLine = true;

        drawLine(startRow, startCol, startRow, startCol);

    } else {
        place(e);
    }
}

@Override
public void mouseDragged(MouseEvent e) {

    if (drawingLine) {

        int currentCol = e.getX() / Physics.CELL_SIZE;
        int currentRow = e.getY() / Physics.CELL_SIZE;

        drawLine(startRow, startCol, currentRow, currentCol);

        startRow = currentRow;
        startCol = currentCol;

    } else {
        place(e);
    }
}

@Override
public void mouseReleased(MouseEvent e) {
    drawingLine = false;
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
