import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

//Encapsulation Example

public class UI extends JFrame {

        // Private prevents other classes from directly
        // accessing the sandbox variable inside UI.
        private Physics sandbox;

        private final Color BACKGROUND = new Color(30, 33, 40);
        private final Color PANEL_COLOR = new Color(39, 43, 52);
        private final Color TEXT = new Color(165, 172, 185);

        // Now the UI class is hiding its internal data
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
                JPanel simWrapper = new JPanel(new FlowLayout(
                                FlowLayout.LEFT, 0, 0));

                simWrapper.setBackground(PANEL_COLOR);
                simWrapper.setBorder(new LineBorder(
                                new Color(65, 71, 83), 1));

                JPanel simPanel = new SimulationPanel(sandbox);
                simPanel.setPreferredSize(new Dimension(
                                Physics.WIDTH, Physics.HEIGHT));
                simPanel.setBackground(Color.DARK_GRAY);

                simWrapper.add(simPanel);
                leftPanel.add(simWrapper, BorderLayout.CENTER);

                // Side Bar
                JPanel sidePanel = new JPanel();
                sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
                sidePanel.setPreferredSize(new Dimension(190, 0));
                sidePanel.setBackground(PANEL_COLOR);

                // Bottom
                JPanel bottomPanel = new JPanel(new FlowLayout(
                                FlowLayout.LEFT, 10, 10));

                bottomPanel.setBackground(PANEL_COLOR);
                bottomPanel.setBorder(new EmptyBorder(0, 5, 0, 5));

                JLabel instructions = new JLabel("SELECT MATERIAL:");
                instructions.setForeground(TEXT);
                instructions.setFont(new Font("SansSerif", Font.BOLD, 12));

                bottomPanel.add(instructions);

                // Sand Button
                JButton sandButton = createButton(
                                "Sand", new Color(210, 174, 55));
                sandButton.addActionListener(e -> sandbox.tester.set(new Sand()));

                // Water Button
                JButton waterButton = createButton(
                                "Water", new Color(55, 135, 220));
                waterButton.addActionListener(e -> sandbox.tester.set(new Water()));

                // Fire Button
                JButton fireButton = createButton(
                                "Fire", new Color(220, 95, 50));
                fireButton.addActionListener(e -> sandbox.tester.set(new Fire()));

                // Gas Button
                JButton gasButton = createButton(
                                "Gas", new Color(120, 130, 145));
                gasButton.addActionListener(e -> sandbox.tester.set(new Gas()));

                // Add buttons to bottom bar
                bottomPanel.add(sandButton);
                bottomPanel.add(waterButton);
                bottomPanel.add(fireButton);
                bottomPanel.add(gasButton);

                leftPanel.add(bottomPanel, BorderLayout.SOUTH);

                setSize(
                                Physics.WIDTH + 150,
                                Physics.HEIGHT + 150);

                setLocationRelativeTo(null);
                setVisible(true);
                setResizable(false);
        }

        // Creates buttons with the same style
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