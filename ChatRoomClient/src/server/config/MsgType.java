package server.config;

public interface MsgType {
    int MSG_PRIVATE=100;
    int MSG_INQUIRE_PRIVATE=110;
    int MSG_PUBLIC=200;
    int MSG_INQUIRE_PUBLIC=210;
    int MSG_ONLINE=300;
    int MSG_LAND=400;//登陆
    int MSG_REGISTERED=500;//注册
    int MSG_ONLINELIST=600;
    int MSG_OFFLINE=700;
    int MSG_FULFILL=800;//给自己回复消息类型
    int MSG_FILE=900;
    int MSG_BIGFILE=910;//发大文件
    int MSG_CHANGE=1000;////隐身/上线
}
