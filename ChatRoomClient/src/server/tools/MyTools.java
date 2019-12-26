package server.tools;

import java.io.*;
import java.util.Scanner;

public class MyTools {
    private MyTools() {
    }

    //获取输入选项（为整型）
    public static int keyNum() {
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("请输入数字选项：");
            if (sc.hasNextInt()) {
                return sc.nextInt();
            }
        }
    }

    //获得用户名；
    public static String getUserName() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("请输入用户名(格式：6~16位英文字母、数字或下划线，开头必须为字母)：");
            String name = sc.nextLine();
            String regex = "[a-zA-Z]\\w{5,15}";
            if (name.matches(regex)) {
                return name;
            } else {
                System.out.print("用户名格式错误!");
            }
        }
    }

    //获得IP地址
    public static String getIP() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("请输入IP：");
            String ip = sc.nextLine();
            String regex = "(((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?))";
            if (ip.matches(regex)) {
                return ip;
            } else {
                System.out.print("IP格式错误!");
            }
        }
    }

    //获得端口号
    public static String getPort() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("请输入端口(范围：1024~65535)：");
            String port = sc.nextLine();
            String regex = "(([1][0-9][2-9][4-9])|([2-9][0-9][0-9][0-9])|([1-6][0-5][0-5][0-3][0-5]))";
            if (port.matches(regex)) {
                return port;
            } else {
                System.out.print("端口错误!");
            }
        }
    }

    //根据源文件获得其字节数据
    public static byte[] getFileBytes(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream baOut = new ByteArrayOutputStream();
        int len;
        byte[] bytes = new byte[1024 * 8];
        while ((len = in.read(bytes)) != -1) {
            baOut.write(bytes, 0, len);
        }
        in.close();
        return baOut.toByteArray();
    }

    //获取源文件
    public static File getPathFile() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String path = sc.nextLine();
            File file = new File(path);
            if (file.isFile() && file.exists()) {
                return file;
            } else {
                System.out.println("此文件不存在，请重新输入路径：");
            }
        }
    }

    //设置文件保存路径
    public static File getPath() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String path = sc.nextLine();
            File file = new File(path);
            if (file.isDirectory() && file.exists()) {
                return file;
            } else {
                System.out.println("此路径不存在!请重新输入路径：");
            }
        }
    }

    //获取用户名：
    public static String getKeyUser() throws IOException {
        File file = new File("用户列表.txt");
        FileInputStream in = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        in.read(bytes);
        in.close();
        String list = new String(bytes);
        String[] split = list.split("-");
        Scanner sc = new Scanner(System.in);
        while (true) {
            String name = sc.nextLine();
            if (name.equals("-q")) {
                return name;
            }
            if ("all".equals(name)) {
                return name;
            }
            for (String s : split) {
                if (s.equals(name)) {
                    return name;
                }
            }
            System.out.println("你输入的用户不存在！请重新输入：");
        }
    }

}
