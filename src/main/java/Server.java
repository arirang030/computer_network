import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

public class Server {
    public static void main(String[] args) {

        ServerSocket serverSocket = null;

        DBHandler dbHandler = new DBHandler();

        try {
            // 서버 소켓 생성, 7777 포트와 binding
            serverSocket = new ServerSocket(7777);
            System.out.println("7777번 포트에서 서버 실행 중...");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                // 클라이언트의 연결 요청이 올 때까지 실행 대기
                // 연결 요청이 오면 클라이언트 소켓과 통신할 새로운 소켓 생성
                Socket client = serverSocket.accept();
                System.out.println(client.getInetAddress() + "로부터 연결 요청이 들어왔습니다.");

                // 입력 스트림으로 HTTP 요청 읽기
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

                // 클라이언트로부터 요청받은 후 파싱
                String requestLine = br.readLine();
                if (requestLine == null)
                    continue;
                // 1. header 읽기
                String[] requestParts = requestLine.split(" ");
                String method = requestParts[0];
                String path = requestParts[1];
                // 2. contentLength 구하기
                String line;
                int contentLength = 0;
                while ((line = br.readLine()) != null && !line.isEmpty()) {
                    if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.split(":")[1].trim());
                    }
                }
                // 3. body 읽기
                char[] bodyChars = new char[contentLength];
                br.read(bodyChars);
                String body = new String(bodyChars);

                // OutputStream 얻기
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                String response = null;

                // GET 요청 처리
                if (method.equals("GET")) {
                    if (path.equals("/findUser")) {
                        // userId가 없는 경우
                        if (body == null || body.isEmpty()) {
                            String content = "User ID is missing in the request body.";
                            response = String.format(
                                "HTTP/1.1 400 Bad Request\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: %d\r\n" +
                                "Date: %s\r\n" +
                                "\r\n" +
                                "%s",
                                content.length(), Calendar.getInstance().getTime(), content
                            );
                        } else {
                            // DB에서 사용자 조회
                            int userId = Integer.parseInt(body);
                            User user = dbHandler.selectUser(userId);

                            // 사용자가 있는 경우
                            if (user != null) {
                                String content = String.format("Searched user: %s", user.getName());
                                response = String.format(
                                    "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: %d\r\n" +
                                    "Date: %s\r\n" +
                                    "\r\n" +
                                    "%s",
                                    content.length(), Calendar.getInstance().getTime(), content
                                );
                            }
                            // 사용자가 없는 경우
                            else {
                                String content = "Could not find user with that ID.";
                                response = String.format(
                                    "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: %d\r\n" +
                                    "Date: %s\r\n" +
                                    "\r\n" +
                                    "%s",
                                    content.length(), Calendar.getInstance().getTime(), content
                                );
                            }
                        }
                    }
                } else if (method.equals("HEAD")) {
                    if (path.equals("/findUser")) {
                        // DB에서 사용자 조회
                        int userId = Integer.parseInt(body);
                        User user = dbHandler.selectUser(userId);
                        // 사용자가 있는 경우
                        if (user != null) {
                            response = String.format(
                                "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: 0\r\n" +
                                "Date: %s\r\n" +
                                "\r\n",
                                Calendar.getInstance().getTime()
                            );
                        }
                        // 사용자가 없는 경우
                        else {
                            response = String.format(
                                "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: 0\r\n" +
                                "Date: %s\r\n" +
                                "\r\n",
                                Calendar.getInstance().getTime()
                            );
                        }
                    }
                } else if (method.equals("POST")) {
                    if (path.equals("/addUser")) {
                        // 요청 본문에 json 데이터가 없는 경우
                        if (body == null || body.isEmpty()) {
                            String content = "JSON data is missing in the request body.";
                            response = String.format(
                                "HTTP/1.1 400 Bad Request\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: %d\r\n" +
                                "Date: %s\r\n" +
                                "\r\n" +
                                "%s",
                                content.length(), Calendar.getInstance().getTime(), content
                            );
                        } else {
                            // JSON 데이터 파싱
                            JSONObject json = new JSONObject(body);
                            int userId = json.getInt("userId");
                            String name = json.getString("name");
                            // 사용자 추가
                            boolean result = dbHandler.insertUser(name, userId);
                            // 추가 성공
                            if (result) {
                                String content = "User added successfully.";
                                response = String.format(
                                    "HTTP/1.1 201 Created\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: %d\r\n" +
                                    "Date: %s\r\n" +
                                    "\r\n" +
                                    "%s",
                                    content.length(), Calendar.getInstance().getTime(), content
                                );
                            } else { // 중복 학번 존재 -> 사용자 추가 실패
                                String content = "User already exists.";
                                response = String.format(
                                    "HTTP/1.1 409 Conflict\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "Content-Length: %d\r\n" +
                                    "Date: %s\r\n" +
                                    "\r\n" +
                                    "%s",
                                    content.length(), Calendar.getInstance().getTime(), content
                                );
                            }
                        }
                    }
                } else if (method.equals("PUT")) {
                    if (path.equals("/updateUser")) {
                        // JSON 데이터 파싱
                        JSONObject json = new JSONObject(body);
                        int userId = json.getInt("userId");
                        String name = json.getString("name");
                        // 사용자 수정
                        boolean result = dbHandler.updateUser(name, userId);
                        if (result) {
                            String content = "User updated successfully.";
                            response = String.format(
                                "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: %d\r\n" +
                                "Date: %s\r\n" +
                                "\r\n" +
                                "%s",
                                content.length(), Calendar.getInstance().getTime(), content
                            );
                        }
                        // DB에 userId가 없는 경우
                        else {
                            String content = "User not found";
                            response = String.format(
                                "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: %d\r\n" +
                                "Date: %s\r\n" +
                                "\r\n" +
                                "%s",
                                content.length(), Calendar.getInstance().getTime(), content
                            );
                        }
                    }
                }

                // 클라이언트에게 데이터 전송
                bw.write(response);
                bw.flush();

                // 자원 정리
                br.close();
                bw.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
