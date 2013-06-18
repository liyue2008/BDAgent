package bdagent.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * 内网
 * Created with IntelliJ IDEA.
 * User: liyue
 * Date: 6/15/13
 * Time: 10:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocalSocketServer extends Thread{
    private final static Logger logger = LoggerFactory
            .getLogger(LocalSocketServer.class);
    private  String serverHostAddress;
    private int listenOnPort;
    private String destination;
    private String command;
    private CommandChannel commandChannel;
    private final static String THREAD_NAME = "LocalSocketServer";
    public LocalSocketServer(int listenOnPort,String destination,String command,CommandChannel commandChannel){
        super(THREAD_NAME);
        this.listenOnPort = listenOnPort;
        this.destination = destination;
        this.command = command;
        this.commandChannel = commandChannel;
    }

    public LocalSocketServer(int listenOnPort,String destination,String command,CommandChannel commandChannel,String serverHostAddress){
        this(listenOnPort,destination,command,commandChannel);
        this.serverHostAddress = serverHostAddress;
    }
    @Override
    public void run(){
        ServerSocket serverSocket = null;
        while(true){
            try{
                serverSocket = SocketHelper.getServerSocket(listenOnPort,true);
                while(true){
                    try {
                        Socket localSocket = serverSocket.accept();

                        if(Command.CMD_S2C_OPENSOCKET.equals(command)){
                            new S2CChannelCreationTempThread(localSocket,destination,commandChannel).start();
                        }else if(Command.CMD_C2S_OPENSOCKET.equals(command)){
                            new C2SChannelCreationTempThread(localSocket,destination,commandChannel,serverHostAddress).start();
                        }else{
                            logger.warn("Unknown command: " + command + ".");
                            localSocket.close();
                        }
                    } catch (IOException e) {
                        logger.warn("Exception", e);
                        //防死循环占满CPU
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {
                            logger.warn("Exception", e1);
                        }
                    }
                }

//
            }catch (Exception e){

                logger.warn("Exception",e);
                if(null != serverSocket){
                    try {
                        serverSocket.close();
                    } catch (IOException e1) {
                        logger.warn("Exception",e1);

                    }
                }

            }

            //防死循环占满CPU
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.warn("Exception", e);
            }


        }
    }

    private class S2CChannelCreationTempThread extends Thread{
        private String destination;
        private Socket localSocket;

        private CommandChannel commandChannel;
        public S2CChannelCreationTempThread(Socket localSocket,String destination,CommandChannel commandChannel){
            super();
            this.localSocket = localSocket;
            this.destination = destination;
            this.commandChannel = commandChannel;
        }

        public void run(){
            ServerSocket serverSocket = null;
            try {
                String message = destination;



                serverSocket = SocketHelper.getServerSocket(0,false);
                int port = serverSocket.getLocalPort();
                message += ":" + String.valueOf(port);
                new PipeCreationThread(serverSocket,localSocket).start();
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    logger.warn("Exception", e);
                }

                String reply = commandChannel.sendSyncCommand(Command.CMD_S2C_OPENSOCKET,message);
                if(!Command.REP_OK.equals(reply)){
                    logger.info("Create channel failed!");
                    serverSocket.close();
                    localSocket.close();
                }



            } catch (Exception e) {
                logger.warn("Exception", e);
                try {
                    if(null != serverSocket) serverSocket.close();
                    if(null != localSocket ) localSocket.close();
                } catch (IOException e1) {
                    logger.warn("Exception", e1);
                }

            }
        }
    }
    private class C2SChannelCreationTempThread extends Thread{
        private String destination;
        private Socket localSocket;
        private String serverHostAddress;
        private CommandChannel commandChannel;
        public C2SChannelCreationTempThread(Socket localSocket,String destination,CommandChannel commandChannel,String serverHostAddress){
            super();
            this.localSocket = localSocket;
            this.destination = destination;
            this.commandChannel = commandChannel;
            this.serverHostAddress = serverHostAddress;
        }

        public void run(){
            try {
                String message = destination;
                long t0 = new Date().getTime();
                String reply = commandChannel.sendSyncCommand(Command.CMD_C2S_OPENSOCKET,message);

                if(Command.REP_ERROR.equals(reply)){
                    logger.info("Create channel failed!");
                    return;
                }
                long t1 = new Date().getTime();
                int serverPort = Integer.parseInt(reply);
                Socket channelSocket = SocketHelper.connect(serverHostAddress,serverPort,false);
                long t2 = new Date().getTime();
                logger.info("[MSG:" + (t1-t0) + "][CNT:" + (t2 -t1) + "].");
                PipeThread.pipeSockets(localSocket,channelSocket);




            } catch (Exception e) {
                logger.warn("Exception", e);
                try {

                    if(null != localSocket ) localSocket.close();
                } catch (IOException e1) {
                    logger.warn("Exception", e1);
                }

            }
        }
    }

}
