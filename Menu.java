import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Menu extends JPanel {

    private final Color BACKGROUND = new Color(30, 33, 40);
    private final Color TEXT = new Color(165, 172, 185);

    public Menu(Runnable onPlay) { // When the play button is clicked

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        // Menu content
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(BACKGROUND);

        // Title
        JLabel title = new JLabel("SANDBOX");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("COMP2000 Physics Simulation");
        subtitle.setForeground(TEXT);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Play button
        JButton playButton = createButton("PLAY", new Color(65, 145, 90));
        playButton.addActionListener(e -> onPlay.run());

        // Exit button
        JButton exitButton = createButton("EXIT", new Color(170, 65, 65));
        exitButton.addActionListener(e -> System.exit(0));

        // Adds everything to the menu
        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(title);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(subtitle);
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(playButton);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(exitButton);
        menuPanel.add(Box.createVerticalGlue());

        add(menuPanel, BorderLayout.CENTER);
    }

    // Create menu buttons
    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 35, 12, 35));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
}
