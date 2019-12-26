package client.ui;

import server.config.MsgType;
import server.tools.MyTools;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {

    private static OutputStream out;
    private static InputStream in;
    private static ClientThread th;
    private volatile String user;

    public static void main(String[] args) {
        Socket sk = null;
        boolean flag = true;
        boolean stop = true;
        try {
            String ip = MyTools.getIP();
            int port = Integer.parseInt(MyTools.getPort());
            sk = new Socket(ip, port);
            stop = false;
            out = sk.getOutputStream();
            Scanner sc;
            while (flag) {
                in = sk.getInputStream();
                System.out.println("1.登录 2.注册");
                int num = MyTools.keyNum();
                if (num != 1 && num != 2) {
                    System.out.println("你输入的选项不存在！");
                    continue;
                }
                byte[] bytes = new byte[1024];
                while (true) {
                    String userName = MyTools.getUserName();

                    sc = new Scanner(System.in);
                    System.out.println("请输入密码：");
                    String userPass = sc.nextLine();
                    if (num == 1) {
                        System.out.println("正在登陆中...");
                        out.write((userName + "###" + userPass + "###" + MsgType.MSG_LAND).getBytes());
                        int len = in.read(bytes);
                        String fk = new String(bytes, 0, len);
                        if (fk.equals("no")) {
                            System.out.println("登录失败！用户名或密码错误！或者当前用户已在其他客户端登录！");
                            System.out.println("是否还要继续尝试登录：继续：y  退出：其他任意键");
                            sc = new Scanner(System.in);
                            String s = sc.nextLine();
                            if (!s.equals("y")) {
                                break;
                            }
                        } else if (fk.equals("yes")) {
                            System.out.println("登陆成功！\n");
                            flag = false;
                            break;
                        }
                    } else {
                        System.out.println("正在注册...");
                        String message = userName + "###" + userPass + "###" + MsgType.MSG_REGISTERED;
                        out.write((message).getBytes());
                        int len = in.read(bytes);
                        String fk = new String(bytes, 0, len);
                        if (fk.equals("no")) {
                            System.out.println("用户名已存在，请重新注册！");
                            System.out.println("是否还要继续尝试注册：继续：y  退出：其他任意键");
                            sc = new Scanner(System.in);
                            String s = sc.nextLine();
                            if (!s.equals("y")) {
                                break;
                            }
                        } else if (fk.equals("yes")) {
                            System.out.println("注册成功并已成功登陆!\n");
                            flag = false;
                            break;
                        }
                    }
                }
            }
            //启动子线程读取消息
            th = new ClientThread(in);
            th.start();

            flag = true;
            boolean func = true;
            while (flag) {
                if (func) {
                    System.out.println("请选择：1.私聊 2.群聊 3.在线列表 4.下线 5.隐身/上线 6.在线发送文件 7.设置文件保存位置 8.查询聊天记录 9.发送大文件\n");
                }
                int num2 = MyTools.keyNum();
                switch (num2) {

                    case 1:
                        privateTalk();
                        break;
                    case 2:
                        publicTalk();
                        break;
                    case 3:
                        getOnlineList();
                        Thread.sleep(100);
                        break;
                    case 4:
                        flag = false;
                        offline();
                        break;
                    case 5:
                        //隐身/上线
                        stealth();
                        Thread.sleep(100);
                        break;
                    case 6:
                        sendFile();
                        Thread.sleep(100);
                        break;
                    case 7:
                        setPath();
                        System.out.println("设置成功！\n");
                        break;
                    case 8:
                        //查询聊天记录
                        inquire();
                        Thread.sleep(100);
                        break;
                    case 9:

                        sendBigfile(sk,th);
                        Thread.sleep(100);
                        break;

                    case 100:
                        th.setF(true);
                        Thread.sleep(1000);
                        break;
                    case 200:
                        th.setSave(false);
                        Thread.sleep(100);
                        break;
                    default:
                        func = false;
                        System.out.println("你输入选项不存在！请重新输入：");
                        break;
                }
            }
        } catch (SocketException e) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            if (stop) {
                System.err.println("IP或端口有误或服务器未开启，连接超时！！！");
            } else {
                System.err.println("服务器异常，你已被强制下线！！！");
            }
            System.exit(-1);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                assert sk != null;
                sk.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendBigfile(Socket sk,ClientThread th) throws IOException, InterruptedException {
        getOnlineList();
        Thread.sleep(100);
        System.out.println("请输入发送对象(退出请输入:-q)：");
        String receiver = MyTools.getKeyUser();
        if (receiver.equals("-q")) {
            return;
        }
        System.out.println("请输入文件路径：");
        File file = MyTools.getPathFile();
        String msg = receiver + "###" + file.getName() + "===" + file.length() + "###" + MsgType.MSG_FILE;
        byte[] msgBytes = msg.getBytes();
        byte[] emptyBytes = new byte[1024 * 10 - msgBytes.length];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(msgBytes);
        bos.write(emptyBytes);
        byte[] infobytes = bos.toByteArray();

        FileInputStream fis = new FileInputStream(file);
        int len=0;
        byte[] bytes = new byte[1024 * 16];
        out.write(infobytes);
        while ((len=fis.read(bytes))!=-1){
            out.write(bytes,0,len);
            out.flush();
        }
        out.close();
        System.out.println("文件发送完成，等待接收...");




    }

    private static void inquire() throws IOException, InterruptedException {
        System.out.println("查询(群聊请输入：1  私聊请输入：2)");
        int num = MyTools.keyNum();
        while (true) {
            if (num == 1) {
                String msg = "null" + "###" + "null" + "###" + MsgType.MSG_INQUIRE_PUBLIC;
                out.write(msg.getBytes());
                break;
            } else if (num == 2) {
                getOnlineList();
                Thread.sleep(100);
                System.out.println("你要查询跟谁的聊天记录?请输入对方用户名(查询全部聊天记录请输入：all 退出查询请输入：-q)：");
                String userName = MyTools.getKeyUser();
                if (userName.equals("-q")) {
                    return;
                }
                //要获得与指定人的聊天记录，就把此线程的user改为指定人；
                th.setUserName(userName);
                String msg = "null" + "###" + "null" + "###" + MsgType.MSG_INQUIRE_PRIVATE;
                out.write(msg.getBytes());
                break;
            } else {
                System.out.println("你输入的选项不存在！请重新输入：");
                num = MyTools.keyNum();
            }
        }
    }

    //设置文件保存路径
    private static void setPath() {
        System.out.println("请设置文件保存位置(默认在G:\\):");
        File path = MyTools.getPath();
        th.setPath(path.toString());
    }

    //隐身/上线
    private static void stealth() throws IOException {
        String msg = "null" + "###" + "null" + "###" + MsgType.MSG_CHANGE;
        out.write(msg.getBytes());
    }

    //发文件
    private static void sendFile() throws IOException, InterruptedException {
        getOnlineList();
        Thread.sleep(100);
        System.out.println("请输入发送对象(退出请输入:-q)：");
        String receiver = MyTools.getKeyUser();
        if (receiver.equals("-q")) {
            return;
        }
        System.out.println("请输入文件路径：");
        File file = MyTools.getPathFile();
        String msg = receiver + "###" + file.getName() + "===" + file.length() + "###" + MsgType.MSG_FILE;
        byte[] msgBytes = msg.getBytes();
        byte[] emptyBytes = new byte[1024 * 10 - msgBytes.length];
        byte[] fileBytes = MyTools.getFileBytes(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(msgBytes);
        bos.write(emptyBytes);
        bos.write(fileBytes);
        byte[] allBytes = bos.toByteArray();
        out.write(allBytes);
    }

    //下线
    private static void offline() throws IOException {
        String msg = "null" + "###" + "null" + "###" + MsgType.MSG_OFFLINE;
        out.write(msg.getBytes());
    }

    //获取在线列表
    public static void getOnlineList() throws IOException {
        //null在此处作用：占位置，可以用任意字符串替换，
        String msg = "null" + "###" + "null" + "###" + MsgType.MSG_ONLINELIST;
        out.write(msg.getBytes());
    }

    private static void publicTalk() throws IOException {
        System.out.println("已进入群聊模式...");
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("请输入聊天内容(退出请输入:-q)：");
            String s = sc.nextLine();
            if ("-q".equals(s)) {
                return;
            }
            //接收者:消息内容:消息类型
            String msg = "null" + "###" + s + "###" + MsgType.MSG_PUBLIC;
            out.write(msg.getBytes());
        }
    }

    private static void privateTalk() throws IOException, InterruptedException {
        System.out.println("已进入私聊模式...");
        getOnlineList();
        Thread.sleep(100);
        System.out.println("请输入私聊对象(退出请输入: -q)：");
        String name = MyTools.getKeyUser();
        if (name.equals("-q")) {
            return;
        }
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("请输入聊天内容(退出请输入: -q)：");
            String s = sc.nextLine();
            if ("-q".equals(s)) {
                return;
            }
            //接收者:消息内容:消息类型
            String msg = name + "###" + s + "###" + MsgType.MSG_PRIVATE;
            out.write(msg.getBytes());
            Thread.sleep(100);
        }
    }
}
