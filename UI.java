
import java.awt.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UI extends JFrame {

    private Physics sandbox;

    private final Color BACKGROUND = new Color(30, 33, 40);
    private final Color PANEL_COLOR = new Color(39, 43, 52);
    private final Color TEXT = new Color(165, 172, 185);

    public UI(int width, int height, Physics sandbox) {
        this.sandbox = sandbox;

        // Window
        setTitle("COMP2000 | Physics Sandbox");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 0));
        mainPanel.setBackground(BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        setContentPane(mainPanel);

        // Simulation
        SimulationPanel simPanel = new SimulationPanel(sandbox);
        simPanel.setPreferredSize(new Dimension(width, height));
        simPanel.setBackground(Color.DARK_GRAY);

        mainPanel.add(simPanel, BorderLayout.CENTER);

        sandbox.startSimulation(simPanel::repaint);

        // Sidebar
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(230, height));
        sidePanel.setBackground(PANEL_COLOR);

        // Statistics panel
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(PANEL_COLOR);
        statsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        sidePanel.add(statsPanel, BorderLayout.NORTH);

        // Statistics heading
        JLabel counterTitle = new JLabel("PIXEL COUNTS");
        counterTitle.setForeground(TEXT);
        counterTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        counterTitle.setBorder(new EmptyBorder(5, 10, 10, 10));

        statsPanel.add(counterTitle);

        // Statistics labels
        JLabel sandCount = createCountLabel("Sand: 0");
        JLabel waterCount = createCountLabel("Water: 0");
        JLabel fireCount = createCountLabel("Fire: 0");
        JLabel gasCount = createCountLabel("Gas: 0");
        JLabel glassCount = createCountLabel("Glass: 0");
        JLabel totalCount = createCountLabel("Total: 0");
        JLabel fireLifespanLabel = createCountLabel("Average Fire Lifespan:");
        JLabel fireLifespan = createCountLabel("0.00s");
        JLabel fpsLabel = createCountLabel("FPS: 0");

        statsPanel.add(sandCount);
        statsPanel.add(waterCount);
        statsPanel.add(fireCount);
        statsPanel.add(gasCount);
        statsPanel.add(glassCount);
        statsPanel.add(totalCount);
        statsPanel.add(fireLifespanLabel);
        statsPanel.add(fireLifespan);
        statsPanel.add(fpsLabel);

        // Buttons
        JButton eraserButton = createButton("Eraser", new Color(90, 90, 100));
        eraserButton.addActionListener(e -> sandbox.eraserSelected = true);

        JButton clearButton = createButton("Clear Grid", new Color(170, 65, 65));
        clearButton.addActionListener(e -> {
            sandbox.clearGrid();
            simPanel.repaint();
        });

        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        toolPanel.setBackground(PANEL_COLOR);

        toolPanel.add(eraserButton);
        toolPanel.add(clearButton);

        sidePanel.add(toolPanel, BorderLayout.SOUTH);

        mainPanel.add(sidePanel, BorderLayout.EAST);

        // Material selection bar
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomPanel.setBackground(PANEL_COLOR);

        JLabel instructions = new JLabel("SELECT MATERIAL:");
        instructions.setForeground(TEXT);
        instructions.setFont(new Font("SansSerif", Font.BOLD, 12));

        bottomPanel.add(instructions);

        JLabel brushLabel = createCountLabel("Brush Size: 1");

        JSlider brushSlider = new JSlider(1, 20, 1);
        brushSlider.setMajorTickSpacing(5);
        brushSlider.setPaintTicks(true);
        brushSlider.setPaintLabels(true);

        brushSlider.addChangeListener(e -> {
            int size = brushSlider.getValue();
            brushLabel.setText("Brush Size: " + size);
            simPanel.setBrushSize(size);
        });

        sidePanel.add(brushLabel);
        sidePanel.add(brushSlider);

        JButton sandButton = createButton("Sand", new Color(210, 174, 55));
        JButton waterButton = createButton("Water", new Color(55, 135, 220));
        JButton fireButton = createButton("Fire", new Color(220, 95, 50));
        JButton gasButton = createButton("Gas", new Color(120, 130, 145));
        JButton glassButton = createButton("Glass", new Color(180, 220, 255));

        sandButton.addActionListener(e -> selectMaterial(new Sand()));
        waterButton.addActionListener(e -> selectMaterial(new Water()));
        fireButton.addActionListener(e -> selectMaterial(new Fire()));
        gasButton.addActionListener(e -> selectMaterial(new Gas()));
        glassButton.addActionListener(e -> selectMaterial(new Glass()));

        bottomPanel.add(sandButton);
        bottomPanel.add(waterButton);
        bottomPanel.add(fireButton);
        bottomPanel.add(gasButton);
        bottomPanel.add(glassButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Update statistics
        Timer counterTimer = new Timer(200, e -> {

            Map<String, Long> counts = sandbox.countPixelsByType();

            long sand = counts.getOrDefault("Sand", 0L);
            long water = counts.getOrDefault("Water", 0L);
            long fire = counts.getOrDefault("Fire", 0L);
            long gas = counts.getOrDefault("Gas", 0L);
            long glass = counts.getOrDefault("Glass", 0L);

            double averageLifespan = sandbox.averageFireLifespan() / 1000.0;

            sandCount.setText("Sand: " + sand);
            waterCount.setText("Water: " + water);
            fireCount.setText("Fire: " + fire);
            gasCount.setText("Gas: " + gas);
            glassCount.setText("Glass: " + glass);

            totalCount.setText("Total: " +
                    (sand + water + fire + gas + glass));

            fireLifespan.setText(String.format("%.2fs", averageLifespan));
            fpsLabel.setText("FPS: " + simPanel.getFps());
        });

        counterTimer.start();

        // Window size
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Select a material
    private void selectMaterial(Elements element) {
        sandbox.eraserSelected = false;
        sandbox.tester.set(element);
    }

    // Create statistics labels
    private JLabel createCountLabel(String text) {
        JLabel label = new JLabel(text);

        label.setForeground(TEXT);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setBorder(new EmptyBorder(5, 10, 5, 10));

        return label;
    }

    // Create buttons
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 18, 9, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
}
