package bdagent.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

/**
 * 用于Server与Client通信的CommandChannel的客户端线程
 * Thread of CommandChannel in client side
 *
 * @author Liyue
 */
public class CommandSocketClient extends Thread {
    private final static Logger logger = LoggerFactory
            .getLogger(CommandSocketClient.class);
    private final static String THREAD_NAME = "CommandSocketClient";
    private String serverHostAddress = null;
    private int serverPort = 0;
    private int retryDelayMs;
    private final CommandChannel commandChannel = new CommandChannel();

    public CommandSocketClient(String serverHostAddress,int serverPort, int retryDelayMs){
        super(THREAD_NAME);
        this.serverHostAddress = serverHostAddress;
        this.serverPort = serverPort;
        this.retryDelayMs = retryDelayMs;

        //注册命令回调
        //Add command handler
        CommandHandlerOpenSocket commandHandler = new CommandHandlerOpenSocket();
        commandHandler.setServerHostAddr(serverHostAddress);
        commandChannel.addCommandHandler(Command.CMD_S2C_OPENSOCKET,commandHandler);
    }

    public void run(){
        Socket socket;



        while (true) {
            try {
                //连接Server端
                //Connect to server
                socket = SocketHelper.connect(serverHostAddress,serverPort,false);

                //连接成功后，开始接收命令
                //Ready for command
                commandChannel.start(socket);


            } catch (Exception e) {
                logger.warn("Exception", e);
            }
            logger.info("Wait " + retryDelayMs + "ms and reconnect...");
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
