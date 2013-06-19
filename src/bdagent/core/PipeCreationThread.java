package bdagent.core;

import bdagent.util.SocketHelp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 创建Socket管道的临时线程，管道创建完成即退出。
 * Temp thread for socket pipe creation. the thread will terminate after pipe creation.
 *
 * @author Liyue
 */
public class PipeCreationThread extends Thread {
    private final static Logger logger = LoggerFactory
            .getLogger(PipeCreationThread.class);
    private ServerSocket channelServerSocket = null;
    private Socket localSocket;
    public PipeCreationThread(ServerSocket channelServerSocket, Socket localSocket){
        super();
        this.channelServerSocket = channelServerSocket;
        this.localSocket = localSocket;
    }
    public void run(){

        try {
            //打开端口，等待Client连接
            //Open socket and waiting from incoming connection
            Socket clientSocket = SocketHelp.accept(channelServerSocket);
            //连接上后，桥接
            //Create pipes when the connection established.
            PipeThread.pipeSockets(localSocket,clientSocket);
        } catch (IOException e) {
            logger.warn("Exception", e);
        }

    }


}
