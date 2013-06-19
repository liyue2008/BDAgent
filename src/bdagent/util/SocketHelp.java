package bdagent.util;

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
 * Help class for socket.
 *
 *
 * @author Liyue
 */
public class SocketHelp {
    private final static Logger logger = LoggerFactory.getLogger(SocketHelp.class);

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
        socket= accept(server);
        server.close();
        return socket;
    }

    public static Socket accept(ServerSocket serverSocket) throws IOException{
        Socket socket;
        socket = serverSocket.accept();

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


    public static void closeServerSocket(ServerSocket serverSocket) {
        if(null != serverSocket){
            try {
                serverSocket.close();
            } catch (IOException e1) {
                logger.warn("Exception",e1);

            }
        }
    }

    public static void closeSocket(Socket socket) {
        if(null != socket){
            try {
                socket.close();
            } catch (IOException e1) {
                logger.warn("Exception",e1);

            }
        }
    }
}
