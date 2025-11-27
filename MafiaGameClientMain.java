
// MafiaGameClientMain.java
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Font;
import java.awt.Color;

public class MafiaGameClientMain extends JFrame {

    private JPanel contentPane;
    private JTextField txtUserName;
    private JTextField txtIpAddress;
    private JTextField txtPortNumber;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MafiaGameClientMain frame = new MafiaGameClientMain();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public MafiaGameClientMain() {
        setTitle("Mafia Game - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 550); // Increased height for better spacing

        // Theme Colors
        Color backgroundColor = new Color(18, 18, 18); // Very Dark Gray
        Color panelColor = new Color(30, 30, 30, 220); // Dark Gray with Transparency (Alpha 220)
        Color textColor = new Color(240, 240, 240); // White
        Color accentColor = new Color(192, 57, 43); // Deep Red
        Color inputColor = new Color(45, 45, 45); // Darker Gray for inputs

        // Custom Background Panel
        contentPane = new BackgroundPanel();
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);
        contentPane.setLayout(new java.awt.GridBagLayout());

        // Main Panel for centering
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new java.awt.GridBagLayout());
        mainPanel.setBackground(panelColor);
        mainPanel.setBorder(new javax.swing.border.LineBorder(new Color(60, 60, 60), 1, true));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 20, 10, 20);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Logo (Optional)
        try {
            javax.swing.ImageIcon icon = new javax.swing.ImageIcon("info/ServerImg.png");
            java.awt.Image img = icon.getImage();
            java.awt.Image newImg = img.getScaledInstance(80, 80, java.awt.Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new javax.swing.ImageIcon(newImg));
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridy = 0;
            gbc.insets = new java.awt.Insets(30, 20, 10, 20);
            mainPanel.add(logoLabel, gbc);
        } catch (Exception e) {
            // Ignore if image not found
        }

        // Title
        JLabel lblTitle = new JLabel("MAFIA GAME");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitle.setForeground(accentColor);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy++;
        gbc.insets = new java.awt.Insets(5, 20, 5, 20);
        mainPanel.add(lblTitle, gbc);

        // Subtitle
        JLabel lblSubTitle = new JLabel("Find the Mafia!");
        lblSubTitle.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblSubTitle.setForeground(new Color(180, 180, 180));
        lblSubTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy++;
        gbc.insets = new java.awt.Insets(0, 20, 30, 20);
        mainPanel.add(lblSubTitle, gbc);

        // User Name Input
        addLabel(mainPanel, "User Name", textColor, gbc);
        txtUserName = addTextField(mainPanel, "", inputColor, textColor, gbc);

        // IP Address Input
        addLabel(mainPanel, "IP Address", textColor, gbc);
        txtIpAddress = addTextField(mainPanel, "127.0.0.1", inputColor, textColor, gbc);

        // Port Number Input
        addLabel(mainPanel, "Port Number", textColor, gbc);
        txtPortNumber = addTextField(mainPanel, "30000", inputColor, textColor, gbc);

        // Connect Button
        JButton btnConnect = new JButton("Connect to Game");
        btnConnect.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnConnect.setBackground(accentColor);
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setFocusPainted(false);
        btnConnect.setBorderPainted(false);
        btnConnect.setOpaque(true);
        btnConnect.setPreferredSize(new java.awt.Dimension(200, 45));

        gbc.gridy++;
        gbc.insets = new java.awt.Insets(30, 20, 30, 20);
        mainPanel.add(btnConnect, gbc);

        contentPane.add(mainPanel);

        Myaction action = new Myaction();
        btnConnect.addActionListener(action);
        txtUserName.addActionListener(action);
        txtIpAddress.addActionListener(action);
        txtPortNumber.addActionListener(action);
    }

    // Custom Panel for Background Image
    class BackgroundPanel extends JPanel {
        private java.awt.Image backgroundImage;

        public BackgroundPanel() {
            try {
                backgroundImage = new javax.swing.ImageIcon("info/background.png").getImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private void addLabel(JPanel panel, String text, Color color, java.awt.GridBagConstraints gbc) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(color);
        gbc.gridy++;
        gbc.insets = new java.awt.Insets(10, 20, 5, 20);
        panel.add(label, gbc);
    }

    private JTextField addTextField(JPanel panel, String text, Color bg, Color fg, java.awt.GridBagConstraints gbc) {
        JTextField textField = new JTextField(text);
        textField.setHorizontalAlignment(SwingConstants.CENTER);
        textField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textField.setBackground(bg);
        textField.setForeground(fg);
        textField.setCaretColor(fg);
        textField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(80, 80, 80)),
                javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        gbc.gridy++;
        gbc.insets = new java.awt.Insets(0, 20, 5, 20);
        panel.add(textField, gbc);
        return textField;
    }

    class Myaction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = txtUserName.getText().trim();
            String ip_addr = txtIpAddress.getText().trim();
            String port_no = txtPortNumber.getText().trim();

            if (username.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(null, "사용자 이름을 입력하세요!");
                return;
            }

            MafiaGameClientView view = new MafiaGameClientView(username, ip_addr, port_no);
            setVisible(false);
        }
    }
}
