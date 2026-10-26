
import java.awt.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

// Encapsulation Example
public class UI extends JFrame {

    private Physics sandbox;

    private final Color BACKGROUND = new Color(30, 33, 40);
    private final Color PANEL_COLOR = new Color(39, 43, 52);
    private final Color TEXT = new Color(165, 172, 185);

    public UI(int width, int height, Physics sandbox) {
        this.sandbox = sandbox;

        // Frame Creation
        setTitle("COMP2000 | Physics Sandbox");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        // Main Layout
        JPanel mainPanel = new JPanel(new BorderLayout(12, 0));
        mainPanel.setBackground(BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(mainPanel, BorderLayout.CENTER);

        // Left Area
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.GRAY);

        mainPanel.add(leftPanel, BorderLayout.CENTER);

        // Simulation Area
        JPanel simWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        simWrapper.setBackground(PANEL_COLOR);
        simWrapper.setBorder(new LineBorder(new Color(65, 71, 83), 1));

        JPanel simPanel = new SimulationPanel(sandbox);
        simPanel.setPreferredSize(new Dimension(Physics.WIDTH, Physics.HEIGHT));
        simPanel.setBackground(Color.DARK_GRAY);

        simWrapper.add(simPanel);
        leftPanel.add(simWrapper, BorderLayout.CENTER);

        // Sidebar
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(190, 0));
        sidePanel.setBackground(PANEL_COLOR);

        // Pixel Counter Heading
        JLabel counterTitle = new JLabel("PIXEL COUNTS");
        counterTitle.setForeground(TEXT);
        counterTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        counterTitle.setBorder(new EmptyBorder(15, 10, 10, 10));

        sidePanel.add(counterTitle);

        // Pixel Counter Labels
        JLabel sandCount = createCountLabel("Sand: 0");
        JLabel waterCount = createCountLabel("Water: 0");
        JLabel fireCount = createCountLabel("Fire: 0");
        JLabel gasCount = createCountLabel("Gas: 0");
        JLabel totalCount = createCountLabel("Total: 0");

        sidePanel.add(sandCount);
        sidePanel.add(waterCount);
        sidePanel.add(fireCount);
        sidePanel.add(gasCount);
        sidePanel.add(totalCount);

        // Add Sidebar to Main Layout
        mainPanel.add(sidePanel, BorderLayout.EAST);

        // Automatically Update Pixel Counts
        Timer counterTimer = new Timer(200, e -> {
            Map<String, Long> counts = sandbox.countPixelsByType();

            long sand = counts.getOrDefault("Sand", 0L);
            long water = counts.getOrDefault("Water", 0L);
            long fire = counts.getOrDefault("Fire", 0L);
            long gas = counts.getOrDefault("Gas", 0L);

            sandCount.setText("Sand: " + sand);
            waterCount.setText("Water: " + water);
            fireCount.setText("Fire: " + fire);
            gasCount.setText("Gas: " + gas);

            totalCount.setText("Total: " + (sand + water + fire + gas));
        });

        counterTimer.start();

        // Bottom Material Selection Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomPanel.setBackground(PANEL_COLOR);
        bottomPanel.setBorder(new EmptyBorder(0, 5, 0, 5));

        JLabel instructions = new JLabel("SELECT MATERIAL:");
        instructions.setForeground(TEXT);
        instructions.setFont(new Font("SansSerif", Font.BOLD, 12));

        bottomPanel.add(instructions);

        JButton sandButton = createButton("Sand", new Color(210, 174, 55));
        sandButton.addActionListener(e -> sandbox.tester.set(new Sand()));
        JButton waterButton = createButton("Water", new Color(55, 135, 220));
        waterButton.addActionListener(e -> sandbox.tester.set(new Water()));
        JButton fireButton = createButton("Fire", new Color(220, 95, 50));
        fireButton.addActionListener(e -> sandbox.tester.set(new Fire()));
        JButton gasButton = createButton("Gas", new Color(120, 130, 145));
        gasButton.addActionListener(e -> sandbox.tester.set(new Gas()));

        // Add Buttons to Bottom Bar
        bottomPanel.add(sandButton);
        bottomPanel.add(waterButton);
        bottomPanel.add(fireButton);
        bottomPanel.add(gasButton);

        leftPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Frame Size and Display
        setSize(
                Physics.WIDTH + 250,
                Physics.HEIGHT + 150);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    // Creates Pixel Counter Labels
    private JLabel createCountLabel(String text) {
        JLabel label = new JLabel(text);

        label.setForeground(TEXT);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setBorder(new EmptyBorder(5, 10, 5, 10));

        return label;
    }

    // Creates Material Selection Buttons
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
