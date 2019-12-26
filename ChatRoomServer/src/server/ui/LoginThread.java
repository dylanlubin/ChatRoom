package server.ui;

import server.config.MsgType;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

public class LoginThread extends Thread{
    Socket sk;
    HashMap<String, Socket> map;
    static BufferedWriter bw;
    int i;
    ArrayList<String> list;
    //在静态代码块里面创建配置文件存用户信息
    static {
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Properties.properties",true)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LoginThread(Socket sk, HashMap<String, Socket> map, ArrayList<String> list, int i) {
        this.sk=sk;
        this.map=map;
        this.i=i;
        this.list=list;
    }

    @Override
    public void run() {
        try {
            InputStream in = sk.getInputStream();
            OutputStream out = sk.getOutputStream();
            byte[] bytes = new byte[1024];
            String userName;
            while (true) {
                //String message = userName + "###" + userPass + "###" + MsgType.MSG_REGISTERED;注册
                // out.write((userName + "###" + userPass + "###" + MsgType.MSG_LAND).getBytes());登陆
                int len = in.read(bytes);
                String s = new String(bytes, 0, len);
                String[] split = s.split("###");
                int type=Integer.parseInt(split[2]);
                userName=split[0];
                String userPass=split[1];
                if (type== MsgType.MSG_REGISTERED) {
                    Properties properties = new Properties();
                    properties.load(new FileReader("Properties.properties"));
                    Set<String> strings = properties.stringPropertyNames();
                    if(strings.contains(userName)){
                        out.write("no".getBytes());
                    }else {
                        map.put(userName,sk);
                        list.add(userName);
                        synchronized (Server.class) {
                            bw.write(userName+"="+userPass);
                            bw.newLine();
                            bw.flush();
                        }
                        out.write("yes".getBytes());
                        break;
                    }
                }else if(type==MsgType.MSG_LAND){
                    Properties properties = new Properties();
                    properties.load(new FileReader("Properties.properties"));
                    String pass = (String) properties.get(userName);
                    if((!map.containsKey(userName))&&userPass.equals(pass)){
                        map.put(userName,sk);
                        list.add(userName);
                        out.write("yes".getBytes());
                        break;
                    }else {
                        out.write("no".getBytes());
                    }
                }
            }

            Set<String> keySet = map.keySet();
            for (String s : keySet) {
                if(s.equals(userName)){
                    continue;
                }
                //发送者:消息内容:消息类型:时间
                map.get(s).getOutputStream().write((userName+"###已上线"+"###"+ MsgType.MSG_ONLINE+"###"+System.currentTimeMillis()).getBytes());
            }
            System.out.println("第"+i+"个客户端已登录！");

            new ServerThread(sk,map,userName,list,i).start();

        } catch (SocketException e) {
            System.out.println("第"+i+"个客户端已下线！");

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
