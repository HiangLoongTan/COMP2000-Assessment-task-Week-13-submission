import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SimulationPanel extends JPanel {

    Sandbox sandbox;

    public SimulationPanel(Sandbox sandbox) {
        this.sandbox = sandbox;

        // Converting mouse to pixels and allows us to spawn our material
        MouseAdapter mouse = new MouseAdapter() {

            void place(MouseEvent e) {

                int col = e.getX() / Sandbox.CELL_SIZE;
                int row = e.getY() / Sandbox.CELL_SIZE;

                // Collision detection to ensure that we cant click outside of the simulated
                // enviroment/grid
                if (row >= 0 && row < Sandbox.ROWS &&
                        col >= 0 && col < Sandbox.COLS) {

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
            sandbox.step();
            repaint();
        });

        timer.start();
    }

    // called whenever the panel needs to redraw
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int s = Sandbox.CELL_SIZE;

        for (int row = 0; row < Sandbox.ROWS; row++) {
            for (int col = 0; col < Sandbox.COLS; col++) {

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