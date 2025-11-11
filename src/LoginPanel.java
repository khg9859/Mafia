import javax.swing.*;
import java.awt.*;
import java.sql.*;
import database.DatabaseConnection;

public class LoginPanel extends JPanel {
    private Image backgroundImage;

    public LoginPanel(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // ✅ Retina 대응 배경 이미지 스케일링
        ImageIcon bgIcon = new ImageIcon("images/mafia42_left.png");
        Image scaled = bgIcon.getImage().getScaledInstance(880, 720, Image.SCALE_SMOOTH);
        backgroundImage = scaled;

        // ✅ 오른쪽 로그인 영역
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(25, 25, 25));
        rightPanel.setPreferredSize(new Dimension(400, 0));

        JLabel logo = new JLabel(scaleIcon("images/mafia42_logo.png", 260, 80));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(60, 0, 40, 0));
        rightPanel.add(logo);

        // ✅ 운영체제별 폰트 자동 선택
        String fontName = System.getProperty("os.name").toLowerCase().contains("mac")
                ? "Apple SD Gothic Neo" : "맑은 고딕";

        // 입력 필드
        JTextField idField = new JTextField(15);
        JPasswordField pwField = new JPasswordField(15);
        idField.setMaximumSize(new Dimension(300, 40));
        pwField.setMaximumSize(new Dimension(300, 40));

        JLabel idLabel = createLabel("아이디", fontName);
        JLabel pwLabel = createLabel("비밀번호", fontName);

        rightPanel.add(idLabel);
        rightPanel.add(idField);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(pwLabel);
        rightPanel.add(pwField);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 버튼들
        JButton loginBtn = createButton("로그인", new Color(70, 130, 180), fontName);
        JButton registerBtn = createButton("회원가입", new Color(100, 100, 100), fontName);
        JButton backBtn = createButton("뒤로가기", new Color(60, 60, 60), fontName);

        // ✅ 로그인 이벤트
        loginBtn.addActionListener(e -> {
            String username = idField.getText();
            String password = new String(pwField.getPassword());

            if (authenticate(username, password)) {
                JOptionPane.showMessageDialog(frame, "로그인 성공!");
                String nickname = getNickname(username);
                frame.showLobby(nickname);
            } else {
                JOptionPane.showMessageDialog(frame, "아이디 또는 비밀번호가 잘못되었습니다.");
            }
        });

        // 회원가입
        registerBtn.addActionListener(e -> showRegisterDialog());

        // 뒤로가기
        backBtn.addActionListener(e -> frame.switchTo("channel"));

        rightPanel.add(loginBtn);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(registerBtn);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(backBtn);

        add(rightPanel, BorderLayout.EAST);
    }

    // ✅ DB 로그인 검증
    private boolean authenticate(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
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

    // ✅ 회원가입 다이얼로그
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
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(null, "DB 연결 실패 (MySQL 실행 확인)");
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

    // ✅ 닉네임 조회
    private String getNickname(String username) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT nickname FROM user WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("nickname");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return username;
    }

    // ✅ 공통 라벨 생성
    private JLabel createLabel(String text, String fontName) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font(fontName, Font.PLAIN, 16));
        return label;
    }

    // ✅ 공통 버튼 생성
    private JButton createButton(String text, Color bgColor, String fontName) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 40));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(fontName, Font.BOLD, 15));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder());
        return button;
    }

    // ✅ 이미지 스케일 유틸
    private ImageIcon scaleIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    // ✅ 배경 이미지 그리기
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth() - 400, getHeight(), this);
    }
}