package bdagent;

import bdagent.core.Command;
import bdagent.core.CommandSocketClient;
import bdagent.core.CommandSocketServer;
import bdagent.core.LocalSocketServer;
import bdagent.util.ConfigHelp;
import bdagent.util.PortMap;
import bdagent.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 服务端启动类
 * Main class of server
 *
 * @author Liyue
 */
public class Server {
    private final static String DEFAULT_SERVER_PORT = "6336";
    private final static String DEFAULT_RETRY_DELAY = "5000";
    private final static String KEY_SERVER_PORT = "server.server_port";
    private final static String KEY_RETRY_DELAY = "server.retry_delay_ms";
    private final static Logger logger = LoggerFactory.getLogger(Server.class);
    public static void main(String[] args) throws InterruptedException {
        //TODO: 支持命令行端口
        //TODO: Add command line parameters support.
        logger.info("BDAgent Server V " + Version.getVersion() + ".");
        //启动Server段命令监听端口
        //Start command socket server and waiting connection from client.
        int serverPort = Integer.parseInt(ConfigHelp.getConifg(KEY_SERVER_PORT,DEFAULT_SERVER_PORT));
        int retryDelayMs = Integer.parseInt(ConfigHelp.getConifg(KEY_RETRY_DELAY,DEFAULT_RETRY_DELAY));
        CommandSocketServer csServer = new CommandSocketServer(serverPort,retryDelayMs);
        csServer.start();

        //读取配置文件configuration.properties中的port mapping配置，
        //然后启动本地端口监听。
        //Start local sockets which defined in the configuration.properties
        //port mapping section  and waiting for local connections.
        List<PortMap> portMaps = PortMap.getPortMaps();
        if(null!=portMaps) {
            for(PortMap portMap:portMaps){
                LocalSocketServer localSocketServer = new LocalSocketServer(portMap.getLocalPort(),
                        portMap.getDestinationHost() + String.valueOf(portMap.getDestinationPort()),
                        Command.CMD_S2C_OPENSOCKET,csServer.getCommandChannel());
                localSocketServer.start();
            }
        }




    }
}
