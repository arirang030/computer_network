import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHandler {

    private Connection conn;

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
}
