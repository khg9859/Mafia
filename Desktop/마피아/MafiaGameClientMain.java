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
        setBounds(100, 100, 400, 450);
        contentPane = new JPanel();
        contentPane.setBackground(new Color(240, 240, 240));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        
        JLabel lblTitle = new JLabel("MAFIA GAME");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 32));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(12, 30, 360, 50);
        contentPane.add(lblTitle);
        
        JLabel lblSubTitle = new JLabel("Find the Mafia!");
        lblSubTitle.setFont(new Font("Arial", Font.ITALIC, 16));
        lblSubTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblSubTitle.setBounds(12, 80, 360, 30);
        contentPane.add(lblSubTitle);
        
        JLabel lblNewLabel = new JLabel("User Name");
        lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        lblNewLabel.setBounds(40, 150, 100, 33);
        contentPane.add(lblNewLabel);
        
        txtUserName = new JTextField();
        txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
        txtUserName.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUserName.setBounds(150, 150, 200, 33);
        contentPane.add(txtUserName);
        txtUserName.setColumns(10);
        
        JLabel lblIpAddress = new JLabel("IP Address");
        lblIpAddress.setFont(new Font("Arial", Font.PLAIN, 14));
        lblIpAddress.setBounds(40, 210, 100, 33);
        contentPane.add(lblIpAddress);
        
        txtIpAddress = new JTextField();
        txtIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
        txtIpAddress.setText("127.0.0.1");
        txtIpAddress.setFont(new Font("Arial", Font.PLAIN, 14));
        txtIpAddress.setColumns(10);
        txtIpAddress.setBounds(150, 210, 200, 33);
        contentPane.add(txtIpAddress);
        
        JLabel lblPortNumber = new JLabel("Port Number");
        lblPortNumber.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPortNumber.setBounds(40, 270, 100, 33);
        contentPane.add(lblPortNumber);
        
        txtPortNumber = new JTextField();
        txtPortNumber.setText("30000");
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPortNumber.setColumns(10);
        txtPortNumber.setBounds(150, 270, 200, 33);
        contentPane.add(txtPortNumber);
        
        JButton btnConnect = new JButton("Connect to Game");
        btnConnect.setFont(new Font("Arial", Font.BOLD, 16));
        btnConnect.setBackground(new Color(100, 149, 237));
        btnConnect.setForeground(Color.WHITE);
        btnConnect.setBounds(40, 340, 310, 50);
        contentPane.add(btnConnect);
        
        Myaction action = new Myaction();
        btnConnect.addActionListener(action);
        txtUserName.addActionListener(action);
        txtIpAddress.addActionListener(action);
        txtPortNumber.addActionListener(action);
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
