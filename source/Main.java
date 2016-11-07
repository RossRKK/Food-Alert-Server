import java.net.ServerSocket;

public class Main {
	
	private static ServerSocket ss;

	public static void main(String args[]) {
		try {
			// Get the port to listen on
			int port = Integer.parseInt(args[0]);
			// Create a ServerSocket to listen on that port.
			ss = new ServerSocket(port);

			// loop until the server is terminated
			boolean done = false;
			while (!done) {
				//open a new thread to handle the request
				(new Thread(new Request(ss.accept()))).start();
			}
			ss.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}