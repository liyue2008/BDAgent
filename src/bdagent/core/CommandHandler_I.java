package bdagent.core;

/**
 * Command reply 消息回调接口
 *
 * Interface definition for command reply handler.
 *
 * @author Liyue
 */
public interface CommandHandler_I {


    /**
     * This method will be invoked specified message received.
     * @param serial
     * Message serial number.
     * @param command
     * Message command. See bdagent.core.Command
     * @param message
     * Message body.
     * @param commandChannel
     * CommandChannel instance which can be used to send a reply message.
     */
    public void onCommand(String serial, String command, String message, CommandChannel commandChannel);
}
