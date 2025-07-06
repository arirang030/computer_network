public class Main {
    public static void main(String[] args) {
        DBHandler dbHandler = new DBHandler();
        User user = dbHandler.selectUser(1);
        System.out.println(user);
    }
}