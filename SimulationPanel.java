import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SimulationPanel extends JPanel {

    Physics sandbox;

    public SimulationPanel(Physics sandbox) {
        this.sandbox = sandbox;

        // Converting mouse to pixels and allows us to spawn our material
        MouseAdapter mouse = new MouseAdapter() {

            void place(MouseEvent e) {

                int col = e.getX() / Physics.CELL_SIZE;
                int row = e.getY() / Physics.CELL_SIZE;

                // Collision detection to ensure that we cant click outside of the simulated
                // enviroment/grid
                if (row >= 0 && row < Physics.ROWS &&
                        col >= 0 && col < Physics.COLS) {

                    Elements selected = sandbox.tester.get();

                    // fire needs its own object per pixel so each one burns out on its own timer
                    sandbox.grid[row][col] = (selected instanceof Fire) ? new Fire() : selected;
                    repaint();
                }
            }

            // mous events for click and click+drag
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

        synchronized (sandbox) {
            for (int row = 0; row < Physics.ROWS; row++) {
                for (int col = 0; col < Physics.COLS; col++) {

                    Elements element = sandbox.grid[row][col];

                    if (element != null) {

                        g.setColor(element.getColor());

                        g.fillRect(
                                col * s,
                                row * s,
                                s,
                                s);
                    }
                }
            }
        }
    }
}