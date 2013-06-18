package bdagent.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class PipeThread implements Runnable {
	private final static Logger logger = LoggerFactory
			.getLogger(PipeThread.class);
	private InputStream in;
	private OutputStream out;

	public PipeThread(InputStream in, OutputStream out){
		this.in=in;
		this.out=out;

	}
	@Override
	public void run() {
		try {
			byte [] buffer = new  byte [4096];
			int len;
			while((len = in.read(buffer)) >=0){
				out.write(buffer,0,len);
				out.flush();
			}
		} catch (IOException e) {
			//do nothing

        }finally {
            doSomeClean();
        }


	}

    private void doSomeClean(){
        logger.info("Pipe socket broken, close pipe.");
        try {
            in.close();
        } catch (Exception e1) {
        }
        try {
            out.close();
        } catch (Exception e1) {
        }

//        try {
//            socket1.close();
//        } catch (IOException e1) {
//
//        }
//
//        try {
//            socket2.close();
//        } catch (IOException e1) {
//
//        }
    }
	public static  void pipeSockets(Socket socket1, Socket socket2) throws IOException {
		
		InputStream lanIn=socket1.getInputStream();
		InputStream wanIn=socket2.getInputStream();
		OutputStream lanOut = socket1.getOutputStream();
		OutputStream wanOut = socket2.getOutputStream();
		
		Thread come=new Thread(new PipeThread(lanIn, wanOut));
		Thread go=new Thread(new PipeThread(wanIn, lanOut));
		come.start();
		go.start();



        String s1,s2;
        if(null!=socket1.getInetAddress()){
            s1 = socket1.getInetAddress() + ":" + socket1.getPort() ;
        }else{
            s1 = "Unknown host:" + socket1.getPort();
        }

        if(null!=socket2.getInetAddress()){
            s2 = socket2.getInetAddress() + ":" + socket2.getPort() ;
        }else{
            s2 = "Unknown host:" + socket1.getPort();
        }


        logger.info("Pipe created: [" + s1 + "]<-->[" +
                socket1.getLocalPort() + ":localhost:" +
                socket2.getLocalPort() + "]<-->[" + s2 + "].");
	}
	
}
