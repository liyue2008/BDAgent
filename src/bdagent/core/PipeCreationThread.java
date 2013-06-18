package bdagent.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: liyue
 * Date: 6/15/13
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
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
            Socket clientSocket = SocketHelper.waitForConnection(channelServerSocket);
            //连接上后，桥接
            PipeThread.pipeSockets(localSocket,clientSocket);
        } catch (IOException e) {
            logger.warn("Exception", e);
        }

    }


}
