import java.io.Console;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

public class ServerExit extends Thread{
    public int delay;
    public ExecutorService pool;
	public ServerSocket serverSocket;
    public UpdateRanking updateThread;

    public ServerExit(int delay, ExecutorService pool, ServerSocket serverSocket, UpdateRanking updateThread){
        this.delay = delay;
		this.pool = pool;
		this.serverSocket = serverSocket;
        this.updateThread = updateThread;
    }

    public void run(){
        System.out.println("type 'exit' to exit");

        Console cns = System.console();
        String message;

        do{
            message = cns.readLine();
        }while(!message.equals("exit"));

        if(message.equals("exit")){
            System.out.println("exiting...");

            try{
                serverSocket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
