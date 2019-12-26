package server.tools;

import java.util.Scanner;

public class MyTools {
    private MyTools(){}
    public static String getPort(){
        Scanner sc = new Scanner(System.in);
        while (true){
            System.out.println("请输入端口(范围：1024~65535)：");
            String port = sc.nextLine();
            String regex="(([1][0-9][2-9][4-9])|([2-9][0-9][0-9][0-9])|([1-6][0-5][0-5][0-3][0-5]))";
            if (port.matches(regex)) {
                return port;
            }else {
                System.out.print("端口格式错误!");
            }
        }
    }
}
