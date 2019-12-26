package client.ui;

import server.config.MsgType;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientThread extends Thread {
    private InputStream in;
    private volatile boolean isSave = true;
    private volatile boolean f = false;
    private volatile String path="G:\\";
    private volatile String user;

    public void setUserName(String userName) {
        this.user = userName.trim();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setF(boolean f) {
        this.f = f;
    }

    public void setSave(boolean save) {
        isSave = save;
    }

    public ClientThread(InputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            byte[] bytes = new byte[1024 * 10];
            while (true) {
                int len = in.read(bytes);
                String s = new String(bytes, 0, len).trim();
                String[] msgs = s.split("###");
                String sender = msgs[0];
                String content = msgs[1];
                int type = Integer.parseInt(msgs[2]);
                long l = Long.parseLong(msgs[3]);
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(l));
                if (type == MsgType.MSG_PRIVATE) {
                    System.out.println(time);
                    System.out.println(sender + "给你发来消息：" + content);
                    System.out.println();
                }else if (type == MsgType.MSG_INQUIRE_PRIVATE) {

                    if(user.equals("all")){
                        System.out.println("你与所有人私聊记录如下：");
                        System.out.println(content);
                    }else {
                        //私聊记录查询
                        String[] split = content.split("\n");
                        boolean pd=true;
                        //遍历集合所有记录行
                        for (String msg : split) {
                            if(msg.substring(20, msg.indexOf('给')).contains(user)|msg.substring(msg.indexOf('给'),msg.indexOf('发')).contains(user)){
                                if(pd){
                                    System.out.println("你与"+user+"聊天记录如下:");
                                    pd=false;
                                }
                                System.out.println(msg);
                            }
                        }
                        if(pd){
                            System.out.println("暂无聊天记录...");
                        }
                    }
                    System.out.println();
                } else if (type == MsgType.MSG_PUBLIC) {
                    System.out.println(time);
                    System.out.println(sender + "给大家发来消息：" + content);
                    System.out.println();
                }else if (type == MsgType.MSG_INQUIRE_PUBLIC) {
                    System.out.println("所有群聊消息如下：");
                    System.out.println(content);
                    System.out.println();
                } else if (type == MsgType.MSG_ONLINE) {
                    System.out.println(time);
                    System.out.println(sender + "：" + content);
                    System.out.println();
                } else if (type == MsgType.MSG_ONLINELIST) {
                    System.out.println(time);
                    String[] split = content.split("=");
                    String str1=split[0];
                    String str2=split[1];
                    System.out.println(str1);
                    synchronized (Client.class) {
                        //获取在线列表之后，把所有用户名存入文本文档，用"-"隔开的；
                        FileOutputStream outputStream = new FileOutputStream("用户列表.txt");
                        outputStream.write(str2.getBytes());
                        outputStream.flush();
                        outputStream.close();
                    }
                    System.out.println();

                    //指定人不在线，给自己发
                } else if (type == MsgType.MSG_FULFILL) {
                    System.out.println(content);
                    System.out.println();

                } else if (type == MsgType.MSG_BIGFILE) {
                    System.out.println(time);
                    String[] split = content.split("===");
                    String fileName = split[0];
                    Long fileLen = Long.parseLong(split[1]);
                    System.out.println(sender + "给你发送了一个文件：" + "大小：" + fileLen / 1024.0 + "kb\t存储路径:"+path + fileName);
                    System.out.println("是否接收该文件(接收输入：100  拒绝输入：200)：");
                    while (isSave) {
                        if (f) {
                            break;
                        }
                    }
                    int count = 0;
                    if (isSave) {//接收
                        System.out.println("正在接收...");
                        FileOutputStream out = new FileOutputStream(path+fileName);
                        int biglen=0;
                        byte[] bigbytes = new byte[1024 * 16];
                        while ((biglen=in.read(bigbytes))!=-1){
                            out.write(bigbytes,0,biglen);
                            out.flush();
                        }
                        System.out.println("接收完成！");

                    } else {//拒绝
                        System.out.println("已拒绝接收...");
                        break;
                    }
                    isSave = true;
                    f = false;
                    System.out.println();
                }
                else if (type == MsgType.MSG_FILE) {
                    System.out.println(time);
                    String[] split = content.split("===");
                    String fileName = split[0];
                    Long fileLen = Long.parseLong(split[1]);
                    System.out.println(sender + "给你发送了一个文件：" + "大小：" + fileLen / 1024.0 + "kb\t存储路径:"+path + fileName);
                    System.out.println("是否接收该文件(接收输入：100  拒绝输入：200)：");
                    while (isSave) {
                        if (f) {
                            break;
                        }
                    }
                    int count = 0;
                    if (isSave) {//接收
                        System.out.println("正在接收...");
                        FileOutputStream out = new FileOutputStream(path+fileName);
                        while (true) {
                            int read = in.read(bytes);
                            out.write(bytes, 0, read);
                            count += read;
                            if (count == fileLen) {
                                System.out.println("接收完成！");
                                break;
                            }
                        }
                        out.close();
                    } else {//拒绝
                        System.out.println("已拒绝接收...");
                        while (true) {
                            int read = in.read(bytes);
                            count += read;
                            if (count == fileLen) {
                                break;
                            }
                        }
                    }
                    isSave = true;
                    f = false;
                    System.out.println();
                }
                else if (type == MsgType.MSG_CHANGE) {
                    System.out.println(time);
                    System.out.println(content);
                    System.out.println();
                }
            }
        } catch (SocketException e) {
            System.err.println("你已下线！");

            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
