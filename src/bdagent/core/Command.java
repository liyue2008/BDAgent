package bdagent.core;

/**
 * 消息枚举类
 * Enums for commands
 *
 * @author Liyue
 */
public class Command {

    //参数格式：destHost:destPort:serverPort
    //Example message: destHost:destPort:serverPort
    public static final String CMD_S2C_OPENSOCKET="S2C_OPENSOCKET";
    //参数格式：destHost:destPort
    //Example message: destHost:destPort
    //回复消息格式：serverSocket
    //Reply：serverSocket
    public static final String CMD_C2S_OPENSOCKET="C2S_OPENSOCKET";

    public static final String REP_OK="REP_OK";
    public static final String REP_ERROR="REP_ERROR";
}
