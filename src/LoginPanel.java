import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*; //테스트

public class LoginPanel extends JPanel {
    private Image backgroundImage;

    public LoginPanel(MainFrame frame) {
        setLayout(new BorderLayout());
        backgroundImage = new ImageIcon("images/mafia42_left.png").getImage();

        // 오른쪽 로그인 패널
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(25, 25, 25));
        rightPanel.setPreferredSize(new Dimension(400, 0));

        JLabel logo = new JLabel(new ImageIcon("images/mafia42_logo.png"));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(60, 0, 40, 0));
        rightPanel.add(logo);

        // 로그인 폼
        JTextField idField = new JTextField(15);
        JPasswordField pwField = new JPasswordField(15);
        idField.setMaximumSize(new Dimension(300, 40));
        pwField.setMaximumSize(new Dimension(300, 40));

        JLabel idLabel = createLabel("아이디");
        JLabel pwLabel = createLabel("비밀번호");

        rightPanel.add(idLabel);
        rightPanel.add(idField);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(pwLabel);
        rightPanel.add(pwField);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 버튼 영역
        JButton loginBtn = createButton("로그인", new Color(70, 130, 180));
        JButton registerBtn = createButton("회원가입", new Color(100, 100, 100));
        JButton backBtn = createButton("뒤로가기", new Color(60, 60, 60));

        loginBtn.addActionListener(e -> {
            String username = idField.getText();
            String password = new String(pwField.getPassword());

            if (authenticate(username, password)) {
                JOptionPane.showMessageDialog(frame, "로그인 성공!");

                // ✅ DB에서 닉네임 가져오기 (Lobby에 표시하기 위함)
                String nickname = getNickname(username);

                // ✅ 로그인 성공 시 LobbyPanel로 전환
                frame.showLobby(nickname);

            } else {
                JOptionPane.showMessageDialog(frame, "아이디 또는 비밀번호가 잘못되었습니다.");
            }
        });


        registerBtn.addActionListener(e -> showRegisterDialog());

        backBtn.addActionListener(e -> frame.switchTo("channel"));

        rightPanel.add(loginBtn);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(registerBtn);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(backBtn);

        add(rightPanel, BorderLayout.EAST);
    }

    // DB 로그인 검증
    private boolean authenticate(String username, String password) {
        try (Connection conn = database.DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM user WHERE username=? AND password=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 회원가입 다이얼로그
    private void showRegisterDialog() {
        JTextField idField = new JTextField();
        JTextField nicknameField = new JTextField();
        JPasswordField pwField = new JPasswordField();

        Object[] message = {
                "아이디:", idField,
                "비밀번호:", pwField,
                "닉네임:", nicknameField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "회원가입", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = database.DatabaseConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(null, "DB 연결 실패 (MySQL 확인 필요)");
                    return;
                }
                String sql = "INSERT INTO user (username, password, nickname) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, idField.getText());
                pstmt.setString(2, new String(pwField.getPassword()));
                pstmt.setString(3, nicknameField.getText());
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "회원가입 완료!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "이미 존재하는 아이디입니다. (" + e.getMessage() + ")");
            }

        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        return label;
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 40));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    // 로그인 후 닉네임 가져오기
    private String getNickname(String username) {
        try (Connection conn = database.DatabaseConnection.getConnection()) {
            String sql = "SELECT nickname FROM user WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return username; // 예외 시 아이디를 대신 표시
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth() - 400, getHeight(), this);
    }
}
