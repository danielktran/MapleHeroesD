package net.server.loginauth;

import java.net.InetSocketAddress;

import org.apache.mina.common.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

public class LoginAuthServer {
	
	private static final int PORT = 47611;
	private static final LoginAuthServer instance = new LoginAuthServer();
	
	private static boolean finishedShutdown = true;
	private static IoAcceptor acceptor;
	
	private LoginAuthServer() {
		
	}
	
	public static LoginAuthServer getInstance() {
		return instance;
	}
	
	public void run() {
			acceptor = new SocketAcceptor();
			final SocketAcceptorConfig cfg = new SocketAcceptorConfig();
	        cfg.getSessionConfig().setTcpNoDelay(true);
	        cfg.setDisconnectOnUnbind(true);
	        cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(new LoginAuthCodecFactory()));
    	try { 
    		acceptor.bind(new InetSocketAddress(PORT), new LoginAuthSession(), cfg);
            System.out.println("Login Authentication Server is listening on port " + PORT + ".");
		} catch (Exception e) {
            System.err.println("Could not bind to port " + PORT + ": " + e);
		}
	}
	
	public static final void shutdown() {
        if (finishedShutdown) {
            return;
        }
        System.out.println("Shutting down Login Authentication Server.");
        acceptor.unbindAll();
        finishedShutdown = true;
    }

}
