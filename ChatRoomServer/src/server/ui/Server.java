package server.ui;

import server.tools.MyTools;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(MyTools.getPort());
            ServerSocket ss = new ServerSocket(port);
            System.out.println("服务端已开启...");

            HashMap<String, Socket> map = new HashMap<>();

            ArrayList<String> list = new ArrayList<>();
            int i=1;
            while (true){
                Socket sk = ss.accept();
                System.out.println("第"+(i++)+"个客户端已连接！");
                new LoginThread(sk,map,list,i-1).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
