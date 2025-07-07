import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {

        String[] requests = {
            // 본문에 user_id가 없는 경우
            "GET /findUser HTTP/1.1\r\nHost: localhost\r\nContent-Length: 0\r\n\r\n",
            // user_id가 있을 때 DB에 사용자가 있을 경우
            "GET /findUser HTTP/1.1\r\nHost: localhost\r\nContent-Length: 8\r\n\r\n20223056",
            // user_id가 있을 때 DB에 사용자가 없을 경우
            "GET /findUser HTTP/1.1\r\nHost: localhost\r\nContent-Length: 8\r\n\r\n12345678",
            // user_id가 있을 때 DB에 사용자가 있을 경우 (HEAD 요청)
            "HEAD /findUser HTTP/1.1\r\nHost: localhost\r\nContent-Length: 8\r\n\r\n20223056",
            // user_id가 있을 때 DB에 사용자가 없을 경우 (HEAD 요청)
            "HEAD /findUser HTTP/1.1\r\nHost: localhost\r\nContent-Length: 8\r\n\r\n12345678",
            // POST 요청 본문에 Json 데이터가 없는 경우
            "POST /addUser HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/json\r\nContent-Length: 0\r\n\r\n",
            // POST 요청 본문에 Json 데이터가 있는 경우 (중복 데이터 없어서 DB에 추가 성공)
            "POST /addUser HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/json\r\nContent-Length: 49\r\n\r\n{\"userId\": \"20229999\", \"name\": \"test_user\"}",
            // POST 요청 본문에 Json 데이터가 있는 경우 (중복 데이터 있어서 DB에 추가 실패)
            "POST /addUser HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/json\r\nContent-Length: 52\r\n\r\n{\"userId\": \"20229999\", \"name\": \"changed_user\"}",
            // PUT 요청에서 user_id가 DB에 없는 경우
            "PUT /updateUser HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/json\r\nContent-Length: 52\r\n\r\n{\"userId\": \"12345678\", \"name\": \"changed_user\"}",
            // PUT 요청 본문에 Json 데이터가 있고 user_id가 DB에 있는 경우
            "PUT /updateUser HTTP/1.1\r\nHost: localhost\r\nContent-Type: application/json\r\nContent-Length: 52\r\n\r\n{\"userId\": \"20229999\", \"name\": \"changed_user\"}",
        };

        for (String request : requests) {
            try {
                Socket server = new Socket();
                server.connect(new InetSocketAddress("127.0.0.1", 7777));

                // 서버에게 요청 보내기
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
                System.out.println("-----------------------");
                System.out.println("[보내는 요청]:");
                System.out.println(request);
                bw.write(request);
                bw.flush();

                // 서버로부터 온 응답 받기
                BufferedReader br = new BufferedReader(new InputStreamReader(server.getInputStream()));
                System.out.println("[서버 응답]:");
                String line;
                boolean isBody = false;
                StringBuilder body = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    // 헤더 출력
                    if (!isBody) {
                        System.out.println(line);
                        // 빈 줄을 만나면 헤더 끝 -> 바디 시작
                        if (line.isEmpty()) {
                            isBody = true;
                        }
                    } else {
                        // 바디 출력
                        body.append(line);
                    }
                }
                System.out.println(body);
                System.out.println("-----------------------");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
