import component.Coordinator;
import util.Constants;

public class Main {
    public static void main(String[] args) {
        int PORT = Constants.PORT;
        if (args.length > 0)
            PORT = Integer.parseInt(args[0]);
        new Coordinator(PORT).start();
    }
}
