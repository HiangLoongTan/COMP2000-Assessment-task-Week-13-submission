import java.awt.*;
import javax.swing.*;

//Encapsulation Example

public class UI extends JFrame {

        //Private prevents other classes from directly 
        //accessing the sandbox variable inside UI.
        private Physics sandbox;


        //Now the UI class is hiding its internal data
        public UI(int width, int height, Physics sandbox) {
                this.sandbox = sandbox;


                // Frame Creation
                setTitle("COMP2000 Project");
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Main Layout
                setLayout(new BorderLayout());
                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.setBackground(Color.GRAY);

                add(mainPanel, BorderLayout.CENTER);

                // Left Area
                JPanel leftPanel = new JPanel(new BorderLayout());
                leftPanel.setBackground(Color.GRAY);

                mainPanel.add(leftPanel, BorderLayout.CENTER);

                // Simulation Area
                JPanel simWrapper = new JPanel(new FlowLayout(
                                FlowLayout.LEFT,
                                0,
                                0));

                simWrapper.setBackground(Color.GRAY);

                JPanel simPanel = new SimulationPanel(sandbox);

                simPanel.setPreferredSize(new Dimension(
                                Physics.WIDTH,
                                Physics.HEIGHT));

                simPanel.setBackground(Color.DARK_GRAY);

                simWrapper.add(simPanel);

                leftPanel.add(simWrapper, BorderLayout.NORTH);

                // Side Bar
                JPanel sidePanel = new JPanel();
                sidePanel.setPreferredSize(new Dimension(150, 0));
                sidePanel.setBackground(Color.GRAY);

                mainPanel.add(sidePanel, BorderLayout.EAST);

                // Bottom Bar
                JPanel bottomPanel = new JPanel();
                bottomPanel.setBackground(Color.GRAY);

                // Sand Button
                JButton sandButton = new JButton("Sand");
                sandButton.addActionListener(e -> {
                        sandbox.tester.set(new Sand());
                });

                // Water Button
                JButton waterButton = new JButton("Water");
                waterButton.addActionListener(e -> {
                        sandbox.tester.set(new Water());
                });

                // Fire Button
                JButton fireButton = new JButton("Fire");
                fireButton.addActionListener(e -> {
                        sandbox.tester.set(new Fire());
                });

                // Gas Button
                JButton gasButton = new JButton("Gas");
                gasButton.addActionListener(e -> {
                        sandbox.tester.set(new Gas());
                });

                // Add buttons to bottom bar
                bottomPanel.add(sandButton);
                bottomPanel.add(waterButton);
                bottomPanel.add(fireButton);
                bottomPanel.add(gasButton);

                leftPanel.add(bottomPanel, BorderLayout.CENTER);

                setSize(
                                Physics.WIDTH + 150,
                                Physics.HEIGHT + 150);

                setLocationRelativeTo(null);
                setVisible(true);
                setResizable(false);
        }
}