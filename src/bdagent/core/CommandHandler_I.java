package bdagent.core;

/**
 * Created with IntelliJ IDEA.
 * User: liyue
 * Date: 6/14/13
 * Time: 6:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CommandHandler_I {


    public void onCommand(String serial, String command, String message, CommandChannel commandChannel);
}
