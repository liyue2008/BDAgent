package bdagent.core;

import bdagent.util.SocketHelp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 本地端口类，根据配置文件的port_mapping开启本地端口，等待用户连接。
 * 接收到用户连接后，发消息给对端创建连接。
 * <p/>
 * Local socket server defined in the port mapping file for user application.
 *
 * @author Liyue
 */
public class LocalSocketServer extends Thread {
    private final static Logger logger = LoggerFactory
            .getLogger(LocalSocketServer.class);
    private final static String THREAD_NAME = "LocalSocketServer";
    private String serverHostAddress;
    private int listenOnPort;
    private String destination;
    private String command;
    private CommandChannel commandChannel;

    /**
     * @param listenOnPort   local listen port
     * @param destination    Format: [destination host address]:[destination port], e.g: 192.168.111.23:80
     * @param command        If running in server mode: Command.CMD_S2C_OPENSOCKET.
     *                       If running in client mode: Command.CMD_C2S_OPENSOCKET.
     * @param commandChannel Instance of CommandChannel, can be used for sending command message.
     */
    public LocalSocketServer(int listenOnPort, String destination, String command, CommandChannel commandChannel) {
        super(THREAD_NAME);
        this.listenOnPort = listenOnPort;
        this.destination = destination;
        this.command = command;
        this.commandChannel = commandChannel;
    }

    public LocalSocketServer(int listenOnPort, String destination, String command, CommandChannel commandChannel, String serverHostAddress) {
        this(listenOnPort, destination, command, commandChannel);
        this.serverHostAddress = serverHostAddress;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        Socket localSocket = null;
        try {
            serverSocket = SocketHelp.getServerSocket(listenOnPort, true);
            while (true) {
                try {
                    localSocket = serverSocket.accept();

                    if (Command.CMD_S2C_OPENSOCKET.equals(command)) {
                        new S2CChannelCreationThread
                                (localSocket, destination, commandChannel).start();
                    } else if (Command.CMD_C2S_OPENSOCKET.equals(command)) {
                        new C2SChannelCreationThread
                                (localSocket, destination, commandChannel, serverHostAddress).start();
                    } else {
                        logger.warn("Unknown command: " + command + ".");
                        SocketHelp.closeSocket(localSocket);
                    }

                } catch (IOException e) {
                    logger.warn("Exception", e);
                    SocketHelp.closeSocket(localSocket);
                } finally {
                    sleepSafe(500L);
                }

            }

//
        } catch (Exception e) {
            logger.warn("Exception", e);
            SocketHelp.closeServerSocket(serverSocket);
        }
    }

    private void sleepSafe(long millis) {
        //防死循环占满CPU
        //sleep a while to avoid 100% cpu usage.
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.warn("Exception", e);
        }
    }

    /**
     * 服务端创建通道临时线程
     * Pipe creation temp thread on server side.
     */
    private class S2CChannelCreationThread extends Thread {
        private String destination;
        private Socket localSocket;
        private CommandChannel commandChannel;

        /**
         *
         * @param localSocket       Socket of local connection from user application.
         * @param destination       Format: [destination host address]:[destination port], e.g: 192.168.111.23:80
         * @param commandChannel    Instance of CommandChannel, can be used for sending command message.
         */
        public S2CChannelCreationThread(Socket localSocket, String destination, CommandChannel commandChannel) {
            super();
            this.localSocket = localSocket;
            this.destination = destination;
            this.commandChannel = commandChannel;
        }

        public void run() {
            ServerSocket serverSocket = null;
            Socket clientSocket = null;
            try {
                String message = destination;

                //Create a ServerSocket for the client on a random port
                serverSocket = SocketHelp.getServerSocket(0, false);
                int port = serverSocket.getLocalPort();

                //Accept the incoming connection from client side in a new thread..
                //new PipeCreationThread(serverSocket, localSocket).start();


                //Notify server to create a pipe then wait for a reply message.
                message += ":" + String.valueOf(port);
                String reply = commandChannel.sendSyncCommand(Command.CMD_S2C_OPENSOCKET, message);

                if (Command.REP_OK.equals(reply)) {
                    //打开端口，等待Client连接
                    //Open socket and waiting from incoming connection
                    clientSocket = SocketHelp.accept(serverSocket);
                    //连接上后，桥接
                    //Create pipes when the connection established.
                    PipeThread.pipeSockets(localSocket,clientSocket);
                }else{
                    logger.info("Create channel failed!");
                    SocketHelp.closeSocket(localSocket);
                }


            } catch (Exception e) {
                logger.warn("Exception", e);
                SocketHelp.closeSocket(localSocket);
                SocketHelp.closeSocket(clientSocket);

            }finally {
                SocketHelp.closeServerSocket(serverSocket);
            }
        }
    }
    /**
     * 客户端创建通道临时线程
     * Pipe creation temp thread on client side.
     */
    private class C2SChannelCreationThread extends Thread {
        private String destination;
        private Socket localSocket;
        private String serverHostAddress;
        private CommandChannel commandChannel;

        /**
         *
         * @param localSocket       Socket of local connection from user application.
         * @param destination       Format: [destination host address]:[destination port], e.g: 192.168.111.23:80
         * @param commandChannel    Instance of CommandChannel, can be used for sending command message.
         * @param serverHostAddress  Host address of the server.
         */
        public C2SChannelCreationThread(Socket localSocket, String destination, CommandChannel commandChannel, String serverHostAddress) {
            super();
            this.localSocket = localSocket;
            this.destination = destination;
            this.commandChannel = commandChannel;
            this.serverHostAddress = serverHostAddress;
        }

        public void run() {
            Socket channelSocket = null;
            try {
                //Notify client to create a pipe then wait for a reply message.
                String message = destination;
                String reply = commandChannel.sendSyncCommand(Command.CMD_C2S_OPENSOCKET, message);

                if (Command.REP_ERROR.equals(reply)) {
                    logger.info("Create channel failed!");
                }else{
                    //连接Server端的Pipe端口
                    //Connect to pipe port on the server.
                    int serverPort = Integer.parseInt(reply);
                    channelSocket = SocketHelp.connect(serverHostAddress, serverPort, false);
                    //连接上后，桥接
                    //Create pipes when the connection established.
                    PipeThread.pipeSockets(localSocket, channelSocket);
                }

            } catch (Exception e) {
                logger.warn("Exception", e);
                SocketHelp.closeSocket(localSocket);
                SocketHelp.closeSocket(channelSocket);
            }

        }
    }

}
