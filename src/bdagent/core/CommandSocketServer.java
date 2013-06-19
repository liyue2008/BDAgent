package bdagent.core;

import bdagent.util.SocketHelp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 用于Server与Client通信的CommandChannel的服务端线程
 * Thread of CommandChannel in server side
 *
 * @author Liyue
 */
public class CommandSocketServer extends Thread{
    private final static Logger logger = LoggerFactory
            .getLogger(CommandSocketServer.class);

    private int listenOnPort;
    private final static String THREAD_NAME = "CommandSocketServer";
    private final CommandChannel commandChannel = new CommandChannel();
    private int retryDelayMs;
    public CommandSocketServer(int listenOnPort,int retryDelayMs){
        super(THREAD_NAME);
        this.listenOnPort = listenOnPort;
        this.retryDelayMs = retryDelayMs;

        CommandHandlerOpenSocket commandHandler = new CommandHandlerOpenSocket();

        commandChannel.addCommandHandler(Command.CMD_C2S_OPENSOCKET,commandHandler);
    }

    public void run(){

        Socket socket;
        while(true){
            try {
                //阻塞在此处，等待客户端连接
                //Block here and waiting for connection
                socket = SocketHelp.waitForConnection(listenOnPort, false);

                //连接成功后，开始接收命令
                //Ready for command
                commandChannel.start(socket);
            } catch (IOException e) {
                logger.warn("Exception",e);
            }
            try {
                Thread.sleep(retryDelayMs);
            } catch (InterruptedException e) {
                logger.warn("Exception", e);
            }
        }
    }

    public CommandChannel getCommandChannel() {
        return commandChannel;
    }
}
