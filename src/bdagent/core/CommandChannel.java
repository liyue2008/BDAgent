package bdagent.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command 消息处理类，实现了简单的同步和异步消息封装。
 * 用于在Server和Client之间传递命令消息。
 * Command message service class.
 * Simple sync and async message service over tcp between server and client.
 *
 * @author Liyue
 */
public class CommandChannel{
    private final static Logger logger = LoggerFactory
            .getLogger(CommandChannel.class);
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Map<String,List<CommandHandler_I>> handlers = new HashMap<String, List<CommandHandler_I>>();
    private final static String CMD_SEPARATOR = "&&";
    private final static int CMD_TYPE_REPLY = 0;
    private final static int CMD_TYPE_MSG = 1;
//    private final static String CMD_TYPE_HEARTBEAT = "HB";
    private long serial = 0L;
    private Map<String,Object> mutexMap = new HashMap<String, Object>();
    private Map<String,String> replyMessageMap = new HashMap<String, String>();


    private boolean checkConnection(){
        if(socket==null) return false;
        return socket.isConnected();
    }

    public void start(Socket socket) throws IOException {

        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream());

        String buffLine,cmdCmd,cmdParm,cmdSerial;
        int cmdType;
        String [] splt;
        while(!((buffLine=in.readLine())==null)){
            logger.info("[RCV]" + buffLine);
            splt = buffLine.split(CMD_SEPARATOR);
            if(splt.length!=4){
                logger.warn("Invalid message: " + buffLine);
                continue;
            }
            cmdSerial = splt[0];
            cmdType = Integer.parseInt(splt[1]);
            cmdCmd = splt[2];
            cmdParm = splt[3];

            switch (cmdType){
                case CMD_TYPE_REPLY:
                    handleReplay(cmdSerial,cmdCmd,cmdParm);
                    break;
                case CMD_TYPE_MSG:
                    handleMessage(cmdSerial,cmdCmd,cmdParm);
                    break;
                default:
                    logger.warn("Unknown command type: " + cmdType + " in message [" + buffLine + "].");
                
            }


        }
    }

    private void handleMessage(String serial, String command, String message) {
        List<CommandHandler_I> matchedHandlers = handlers.get(command);
        if(null!=matchedHandlers){
            logger.info(matchedHandlers.size() + "handlers found for command: " + command + ".");
            for(CommandHandler_I commandHandler:matchedHandlers){
                if(null!=commandHandler) try {
                    commandHandler.onCommand(serial,command,message,this);
                } catch (Exception e) {
                    logger.info("Exception",e);
                }
            }
        }else{
            logger.info("0 handlers found for command: " + command + ".");
        }

    }

    private void handleReplay(String cmdSerial, String cmdCmd, String cmdParm) {
        Object mutex = mutexMap.remove(cmdSerial);

        if(null!=mutex){
            replyMessageMap.put(cmdSerial,cmdParm);
            synchronized (mutex){
                mutex.notify();
            }
        }

    }


    public void addCommandHandler(String command , CommandHandler_I commandHandler ){

        List<CommandHandler_I> handlerList;
        handlerList = handlers.get(command);
        if(null==handlerList){
            handlerList =new  ArrayList <CommandHandler_I>();
            handlers.put(command,handlerList);
        }

        handlerList.add(commandHandler);
    }

    public void removeCommandHandler(String command, CommandHandler_I commandHandler){
        List<CommandHandler_I> handlerList;
        handlerList = handlers.get(command);
        if(null!=handlerList){
            handlerList.remove(commandHandler);
        }


    }

    public String sendAsyncCommand(String command,String message){
        if(!checkConnection()){
            logger.warn("Send message failed: [" + command + "|" + message  + "|" + message +  "].");
            return null;
        }
        StringBuilder cmdBuilder=new StringBuilder();
        String serial = getSerial();
        cmdBuilder.append(serial);
        cmdBuilder.append(CMD_SEPARATOR);
        cmdBuilder.append(CMD_TYPE_MSG);
        cmdBuilder.append(CMD_SEPARATOR);
        cmdBuilder.append(command);
        cmdBuilder.append(CMD_SEPARATOR);
        cmdBuilder.append(message);
        logger.info("[SND]"+ cmdBuilder.toString());
        out.println(cmdBuilder.toString());
        out.flush();
        return serial;
    }
    //TODO: 后续考虑支持Timeout
    public  String sendSyncCommand(String command,String message) throws InterruptedException {
        logger.info("Send command and wait: [" + command + "|" + message + "]...");

        Object mutex = Thread.currentThread();
        String serial;
        synchronized (mutex){
            serial = sendAsyncCommand(command,message);
            if(null==serial) return null;
            mutexMap.put(serial,mutex);
            mutex.wait();
        }
        String replyMessage = replyMessageMap.remove(serial);
        logger.info("Got reply: [" + command + "|" + message  + "|" + replyMessage +  "].");
        return replyMessage;
    }


    private String getSerial(){
        if(++serial == Long.MAX_VALUE) serial = 0L;
        return String.valueOf(serial);
    }


    public boolean sendReply(String serial, String command, String message) {
        if(!checkConnection()){
            logger.warn("Send reply failed: [" + command + "|" + message  + "|" + message +  "].");
            return false;
        }
        StringBuilder cmdBuilder=new StringBuilder();
        cmdBuilder.append(serial);
        cmdBuilder.append(CMD_SEPARATOR);
        cmdBuilder.append(CMD_TYPE_REPLY);
        cmdBuilder.append(CMD_SEPARATOR);
        cmdBuilder.append(command);
        cmdBuilder.append(CMD_SEPARATOR);
        cmdBuilder.append(message);
        logger.info("[SND]"+ cmdBuilder.toString());
        out.println(cmdBuilder.toString());
        out.flush();
        return true;
    }
}
