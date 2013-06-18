package bdagent.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: liyue
 * Date: 6/14/13
 * Time: 8:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class SocketHelper {
    private final static Logger logger = LoggerFactory.getLogger(SocketHelper.class);

    public static Socket connect(String hostAddr,int port,boolean disableSSL) throws IOException {
        Socket socket;
        logger.info("Connecting to " + hostAddr + ":" + port + "...");
        if(disableSSL || !"true".equals(System.getProperty("ssl")))
        {
            //不加密方式
            logger.info("SSL disabled.");
            socket = new Socket(hostAddr, port);
        }else{
            //加密方式
            logger.info("SSL enabled.");
            SocketFactory factory = SSLSocketFactory.getDefault();
            socket = factory.createSocket(hostAddr, port);
        }

        logger.info("Connected!");
        return  socket;
    }


    public static Socket waitForConnection(int listenOnPort,boolean disableSSL) throws IOException {
        ServerSocket server;
        Socket socket;
        logger.info("Starting socket server on port " + listenOnPort + "...");
        server = getServerSocket(listenOnPort,disableSSL);
        socket= waitForConnection(server);
        return socket;
    }

    public static Socket waitForConnection(ServerSocket serverSocket) throws IOException{
        Socket socket;
        socket = serverSocket.accept();
        serverSocket.close();
        logger.info("Connection accepted from: " + socket.getInetAddress().getHostAddress() + ".");
        return socket;
    }

    public static ServerSocket getServerSocket(int listenOnPort,boolean disableSSL) throws IOException {
        ServerSocket server;
        if( disableSSL || !"true".equals(System.getProperty("ssl")))
        {
            //不加密的方式
            logger.info("SSL disabled.");
            server=new ServerSocket(listenOnPort);
        }else{
            //加密方式
            logger.info("SSL enabled.");
            ServerSocketFactory factory = SSLServerSocketFactory.getDefault();
            server = factory.createServerSocket(listenOnPort);
        }
        return server;
    }
}
