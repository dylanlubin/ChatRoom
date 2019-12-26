package server.ui;

import server.config.MsgType;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;


public class ServerThread extends Thread {
    Socket sk;
    HashMap<String, Socket> map;
    String userName;
    int i;
    ArrayList<String> list;
    volatile boolean f = true;//true为在线  false为隐身
    static BufferedWriter bw;

    //创建静态代码块保存聊天记录
    static {
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("群聊信息.txt", true)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static BufferedWriter bw2;

    static {
        try {
            bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("私聊信息.txt", true)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ServerThread(Socket sk, HashMap<String, Socket> map, String userName, ArrayList<String> list, int i) {
        this.sk = sk;
        this.map = map;
        this.userName = userName;
        this.i = i;
        this.list = list;
    }

    @Override
    public void run() {
        try {
            InputStream in = sk.getInputStream();
            byte[] bytes = new byte[1024 * 10];
            boolean flag = true;
            while (flag) {

                //要转发格式： 发送者:消息内容:消息类型:时间

                //接受客户端发来的消息：接收者:消息内容:消息类型
                int len = in.read(bytes);
                String msg = new String(bytes, 0, len).trim();
                String[] msgs = msg.split("###");
                String receiver = msgs[0];
                String content = msgs[1];
                int type = Integer.parseInt(msgs[2]);

                //私聊：
                if (type == MsgType.MSG_PRIVATE) {
                    if (map.containsKey(receiver)) {
                        String inquire = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ":" + userName + "-" + "给" + receiver + "发送消息：" + content;
                        synchronized (Server.class) {
                            bw2.write(inquire);
                            bw2.newLine();
                            bw2.flush();
                        }
                        //要发送的消息：发送者:消息内容:消息类型:时间
                        msg = userName + "###" + content + "###" + type + "###" + System.currentTimeMillis();
                        //发送给接受者
                        map.get(receiver).getOutputStream().write(msg.getBytes());
                    } else {
                        //没在线的话，发消息自己提醒；
                        msg = userName + "###" + receiver + "现在没有在线，请稍后联系！" + "###" + MsgType.MSG_FULFILL + "###" + System.currentTimeMillis();
                        sk.getOutputStream().write(msg.getBytes());
                    }

                    //获取私聊聊天记录
                } else if (type == MsgType.MSG_INQUIRE_PRIVATE) {
                    BufferedReader br2 = new BufferedReader(new FileReader("私聊信息.txt"));
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = br2.readLine()) != null) {
                        if (line.substring(20, line.indexOf('给')).contains(userName) | line.substring(line.indexOf('给'), line.indexOf('发')).contains(userName)) {
                            sb.append(line).append("\n");
                        }
                    }

                    br2.close();
                    msg = null + "###" + sb.toString() + "###" + type + "###" + System.currentTimeMillis();
                    sk.getOutputStream().write(msg.getBytes());


                    //群聊
                } else if (type == MsgType.MSG_PUBLIC) {
                    String inquire = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ":" + userName + "-" + content;
                    synchronized (Server.class) {
                        bw.write(inquire);
                        bw.newLine();
                        bw.flush();
                    }
                    Set<String> keySet = map.keySet();
                    for (String s : keySet) {
                        if (s.equals(userName)) {
                            continue;
                        }
                        //要发送的消息：发送者:消息内容:消息类型:时间
                        msg = userName + "###" + content + "###" + type + "###" + System.currentTimeMillis();
                        map.get(s).getOutputStream().write(msg.getBytes());
                    }
                    //获取群聊聊天记录
                } else if (type == MsgType.MSG_INQUIRE_PUBLIC) {
                    BufferedReader br = new BufferedReader(new FileReader("群聊信息.txt"));
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    //要发送的消息：发送者:消息内容:消息类型:时间
                    msg = null + "###" + sb.toString() + "###" + type + "###" + System.currentTimeMillis();
                    sk.getOutputStream().write(msg.getBytes());
                    //在线列表
                } else if (type == MsgType.MSG_ONLINELIST) {
                    Properties properties = new Properties();
                    properties.load(new FileReader("Properties.properties"));

                    //获取配置文件中的用户信息 中的键即用户名集合；
                    Set<String> strings = properties.stringPropertyNames();
                    StringBuilder sbon = new StringBuilder("在线列表：\n");
                    StringBuilder sboff = new StringBuilder("\t\n不在线列表：\n");
                    StringBuilder sball = new StringBuilder();
                    int i = 1;
                    int j = 1;
                    //遍历取出的用户名集合
                    for (String user : strings) {
                        //获取所有用户
                        sball.append(user).append("-");
                        //获取其他在线 或者 不在线的用户
                        if (!user.equals(userName)) {
                            if (list.contains(user)) {
                                //在线
                                sbon.append(i++).append("、").append(user).append("\r\n");
                            } else {
                                //不在线的
                                sboff.append(j++).append("、").append(user).append("\r\n");
                            }
                        }
                    }
                    String msgstr = sbon.append(sboff).append("=").append(sball.deleteCharAt(sball.length() - 1)).toString();
                    //要发送的消息：发送者:消息内容:消息类型:时间
                    sk.getOutputStream().write((userName + "###" + msgstr + "###" + type + "###" + System.currentTimeMillis()).getBytes());
                }
                //下线，map和list都删除
                else if (type == MsgType.MSG_OFFLINE) {
                    flag = false;
                    Set<String> keySet = map.keySet();
                    for (String s : keySet) {
                        if (s.equals(userName)) {
                            continue;
                        }
                        //发送者:消息内容:消息类型:时间
                        map.get(s).getOutputStream().write((userName + "###已下线" + "###" + MsgType.MSG_ONLINE + "###" + System.currentTimeMillis()).getBytes());
                    }

                    //发送大文件
                } else if (type == MsgType.MSG_BIGFILE) {
                    String[] split = content.split("===");
                    String fileName = split[0];
                    long fileLen = Long.parseLong(split[1]);
                    if (map.containsKey(receiver)) {
                        //要发送的消息：发送者:消息内容:消息类型:时间
                        sk.getOutputStream().write((userName + "###" + fileName + "已发送完成！" + "###" + MsgType.MSG_FULFILL + "###" + System.currentTimeMillis()).getBytes());
                        msg = userName + "###" + content + "###" + type + "###" + System.currentTimeMillis();

                        byte[] msgBytes = msg.getBytes();
                        byte[] empBytes = new byte[1024 * 10 - msgBytes.length];
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bos.write(msgBytes);
                        bos.write(empBytes);
                        byte[] infobytes = bos.toByteArray();

                        FileOutputStream fos = new FileOutputStream(new File("D:\\"+"\\"+fileName));
                        int biglen=0;
                        byte[] bigbytes = new byte[1024 * 16];
                        while ((biglen=in.read(bigbytes))!=-1){
                            fos.write(bigbytes,0,biglen);
                            fos.flush();
                        }
                        fos.close();

                        FileInputStream fis = new FileInputStream("D:\\" + "\\" + fileName);
                        int read=0;
                        byte[] readbytes = new byte[1024 * 16];
                        OutputStream out = map.get(receiver).getOutputStream();
                        out.write(infobytes);
                        while ((read=in.read(readbytes))!=-1){
                            out.write(bigbytes,0,biglen);
                            out.flush();
                        }
                        fis.close();
                        out.close();
                        /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        int count = 0;
                        while (true) {
                            int read = in.read(bytes);
                            bos.write(bytes, 0, read);
                            count += read;
                            if (count == fileLen) {
                                break;
                            }
                        }

                        byte[] fileBytes = bos.toByteArray();
                        bos.reset();
                        bos.write(msgBytes);
                        bos.write(airBytes);
                        bos.write(fileBytes);
                        byte[] allBytes = bos.toByteArray();
                        map.get(receiver).getOutputStream().write(allBytes);*/

                    } else {
                        //要发送的消息：发送者:消息内容:消息类型:时间
                        msg = userName + "###" + receiver + "现在没在线，请稍后给他发文件！" + "###" + MsgType.MSG_FULFILL + "###" + System.currentTimeMillis();
                        sk.getOutputStream().write(msg.getBytes());

                    }

                } else if (type == MsgType.MSG_FILE) {


                    String[] split = content.split("===");
                    String fileName = split[0];
                    long fileLen = Long.parseLong(split[1]);
                    if(map.containsKey(receiver)){
                        //要发送的消息：发送者:消息内容:消息类型:时间
                        sk.getOutputStream().write((userName + "###" + fileName + "已发送完成！" + "###" + MsgType.MSG_FULFILL + "###" + System.currentTimeMillis()).getBytes());
                        msg = userName + "###" + content + "###" + type + "###" + System.currentTimeMillis();
                        byte[] msgBytes = msg.getBytes();
                        byte[] empBytes = new byte[1024 * 10 - msgBytes.length];
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        int count = 0;
                        while (true) {
                            int read = in.read(bytes);
                            bos.write(bytes, 0, read);
                            count += read;
                            if (count == fileLen) {
                                break;
                            }
                        }

                        byte[] fileBytes = bos.toByteArray();
                        bos.reset();
                        bos.write(msgBytes);
                        bos.write(empBytes);
                        bos.write(fileBytes);
                        byte[] allBytes = bos.toByteArray();
                        map.get(receiver).getOutputStream().write(allBytes);

                    }else {
                        //要发送的消息：发送者:消息内容:消息类型:时间
                        msg = userName + "###" + receiver+"现在没在线，请稍后给他发文件！" + "###" + MsgType.MSG_FULFILL + "###" + System.currentTimeMillis();
                        sk.getOutputStream().write(msg.getBytes());
                        int count = 0;
                        while (true) {
                            int read = in.read(bytes);
                            count += read;
                            if (count == fileLen) {
                                break;
                            }
                        }
                    }


                } else if (type == MsgType.MSG_CHANGE) {//切换状态
                    if (f) {
                        list.remove(userName);
                        //发送者:消息内容:消息类型:时间
                        msg = null + "###" + "你已进入隐身状态！" + "###" + type + "###" + System.currentTimeMillis();
                        sk.getOutputStream().write(msg.getBytes());
                    } else {
                        list.add(userName);
                        msg = null + "###" + "已切换为在线状态！" + "###" + type + "###" + System.currentTimeMillis();
                        sk.getOutputStream().write(msg.getBytes());

                        Set<String> keySet = map.keySet();
                        for (String s : keySet) {
                            if (s.equals(userName)) {
                                continue;
                            }
                            map.get(s).getOutputStream().write((userName + "###已上线" + "###" + MsgType.MSG_ONLINE + "###" + System.currentTimeMillis()).getBytes());
                        }
                    }
                    f = !f;
                }
            }
        } catch (SocketException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            map.remove(userName);
            list.remove(userName);
            try {
                sk.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("第" + i + "个客户端已下线！");
            Thread.currentThread().stop();
        }
    }
}
