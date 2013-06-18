package bdagent;

import bdagent.core.Command;
import bdagent.core.CommandSocketClient;
import bdagent.core.LocalSocketServer;
import bdagent.util.PortMap;
import bdagent.util.ConfigHelp;
import bdagent.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *客户端启动类
 * Main class of client
 *
 * @author Liyue
 */
public class Client {
    private final static String DEFAULT_SERVER_HOST = "127.0.0.1";
    private final static String DEFAULT_SERVER_PORT = "6336";
    private final static String DEFAULT_RETRY_DELAY = "5000";
    private final static String KEY_SERVER_HOST = "client.server_host";
    private final static String KEY_SERVER_PORT = "client.server_port";
    private final static String KEY_RETRY_DELAY = "client.retry_delay_ms";
    private final static Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws InterruptedException {
        //TODO: 支持命令行参数
        //TODO: Add command line parameters support.
        logger.info("BDAgent Client V " + Version.getVersion() + ".");
        //连接Server端
        //Connecting to the server...
        String serverHost = ConfigHelp.getConifg(KEY_SERVER_HOST,DEFAULT_SERVER_HOST);
        int serverPort = Integer.parseInt(ConfigHelp.getConifg(KEY_SERVER_PORT,DEFAULT_SERVER_PORT));
        int retryDelayMs = Integer.parseInt(ConfigHelp.getConifg(KEY_RETRY_DELAY,DEFAULT_RETRY_DELAY));
        CommandSocketClient csClient = new CommandSocketClient(serverHost,serverPort,retryDelayMs);
        csClient.start();


        //读取配置文件configuration.properties中的port mapping配置，
        //然后启动本地端口监听。
        //Start local sockets which defined in the configuration.properties
        //port mapping section  and waiting for local connections.
        List<PortMap> portMaps = PortMap.getPortMaps();
        if(null!=portMaps) {
            for(PortMap portMap:portMaps){
                LocalSocketServer localSocketServer = new LocalSocketServer(portMap.getLocalPort(),
                        portMap.getDestinationHost() + ":" + String.valueOf(portMap.getDestinationPort()),
                        Command.CMD_C2S_OPENSOCKET,csClient.getCommandChannel(),serverHost);
                localSocketServer.start();
            }
        }




    }
}
