import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;

public class DBHandler {

    private Connection conn;
    private ResultSet rs;
    private PreparedStatement stmt;

    public DBHandler() {
        connect();
    }

    public void connect() {
        Dotenv dotenv = Dotenv.load();
        String db_url = dotenv.get("DB_URL");
        String db_user = dotenv.get("DB_USER");
        String db_password = dotenv.get("DB_PASSWORD");
        try {
            conn = DriverManager.getConnection(db_url, db_user, db_password);
            System.out.println("DB 연결 성공");
        } catch (SQLException e) {
            System.out.println("DB 연결 실패");
        }
    }

    // 이름, 학번으로 사용자 추가
    public boolean insertUser(String name, int userId) {
        // 학번 중복 확인
        User user = selectUser(userId);
        if (user != null) {
            // 사용자 추가 실패
            return false;
        }
        // 중복된 학번이 없다면 사용자 추가
        try {
            stmt = conn.prepareStatement("INSERT INTO users (name, userId) VALUES (?, ?)");
            stmt.setString(1, name);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("유저 삽입 실패");
            // 추가 성공
            return false;
        }
    }

    // 학번으로 사용자 검색
    public User selectUser(int userId) {
        try {
            stmt = conn.prepareStatement("SELECT * FROM users WHERE userId = ?");
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("userId")
                );
            }
        } catch (SQLException e) {
            System.out.println("유저 조회 실패");
        }
        // 사용자가 없는 경우 null 반환
        return null;
    }

    // 해당 학번에 해당하는 사용자의 이름 수정
    public boolean updateUser(String name, int userId) {
        // 학번으로 사용자 조회
        User user = selectUser(userId);
        // 해당 학번에 해당하는 사용자가 없는 경우
        if (user == null) {
            return false;
        }
        try {
            stmt = conn.prepareStatement("UPDATE users SET name = ? WHERE userId = ?");
            stmt.setString(1, name);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("유저 수정 실패");
        }
        return false;
    }
}
