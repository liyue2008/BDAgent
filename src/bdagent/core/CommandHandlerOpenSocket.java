package bdagent.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: liyue
 * Date: 6/14/13
 * Time: 8:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandHandlerOpenSocket implements CommandHandler_I {
    private final static Logger logger = LoggerFactory
            .getLogger(CommandHandlerOpenSocket.class);


    public String getServerHostAddr() {
        return serverHostAddr;
    }

    public void setServerHostAddr(String serverHostAddr) {
        this.serverHostAddr = serverHostAddr;
    }

    private String serverHostAddr;
    @Override
    public void onCommand(String serial, String command, String message, CommandChannel commandChannel) {
        try {
            if(Command.CMD_C2S_OPENSOCKET.equals(command)){
                c2sOpenSocket(serial,command,message, commandChannel);
            }else if(Command.CMD_S2C_OPENSOCKET.equals(command)){
                s2cOpenSocket(serial,command,message, commandChannel);
            }

        } catch (IOException e) {
            logger.warn("Exception",e);
            commandChannel.sendReply(serial,command,Command.REP_ERROR);
        }

    }

    private void s2cOpenSocket(String serial, String command, String message, CommandChannel commandChannel) throws IOException {
        long t0 = new Date().getTime();
        String [] sp;

        //参数格式：destHost:destPort:serverPort
        sp = message.split(":");
        if(sp.length !=3){
            logger.warn("Invalid message: [CMD=" + command + "][MSG=" + message + "].");
        }
        String destHostAddr=sp[0];
        int destPort = Integer.parseInt(sp[1]);
        int serverPort = Integer.parseInt(sp[2]);
        //连接目的端口
        Socket destSocket=SocketHelper.connect(destHostAddr, destPort, true);
        //连接Server
        Socket serverSocket= SocketHelper.connect(serverHostAddr,serverPort,false);
        //桥接
        PipeThread.pipeSockets(destSocket,serverSocket);
        //回复消息
        commandChannel.sendReply(serial,command,Command.REP_OK);
        long t1 = new Date().getTime();
        logger.info("[s2cOpenSocket:" + (t1-t0) + "].");

    }

    private void c2sOpenSocket(String serial, String command, String message, CommandChannel commandChannel) throws IOException {
        String [] sp;
        long t0 = new Date().getTime();
        //参数格式：destHost:destPort
        sp = message.split(":");
        if(sp.length !=2){
            logger.warn("Invalid message: [CMD=" + command + "][MSG=" + message + "].");
            return;
        }
        String destHostAddr=sp[0];
        int destPort = Integer.parseInt(sp[1]);
        int serverPort;
        //创建一个ServerSocket用于等待Client连入，端口随机。
        //注意：此处还没有打开这个ServerSocket
        ServerSocket serverSocket= SocketHelper.getServerSocket(0,false);
        serverPort = serverSocket.getLocalPort();

        //连接目的端口
        Socket destSocket=SocketHelper.connect(destHostAddr, destPort, true);
        //在一个新线程中打开这个ServerSocket，等待Client连入。
        new PipeCreationThread(serverSocket,destSocket).start();

//        //等一秒，确保端口已经打开
//        try {
//            Thread.sleep(1000L);
//        } catch (InterruptedException e) {
//            logger.info("Exception",e);
//        }
        //回复消息给Client
        commandChannel.sendReply(serial,command,String.valueOf(serverPort));

        long t1 = new Date().getTime();
        logger.info("[C2SOpenSocket:" + (t1-t0) + "].");
    }


}
