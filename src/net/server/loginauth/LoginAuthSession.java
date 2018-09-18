package net.server.loginauth;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import net.packet.LoginAuthPacket;
import tools.data.ByteArrayByteStream;
import tools.data.LittleEndianAccessor;

public class LoginAuthSession extends IoHandlerAdapter {

	public LoginAuthSession() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void messageReceived(IoSession session, Object message) {
		final LittleEndianAccessor lea = new LittleEndianAccessor(new ByteArrayByteStream((byte[]) message));
		short packetHeader = lea.readShort();
		
		switch(packetHeader) {
        case 0x33:
        	lea.readInt();
    		String username = lea.readLoginAuthString();
    		session.write(LoginAuthPacket.handleLogin(username));
        	break;
        case 0x2D:
        	session.write(LoginAuthPacket.handleLogin2());
        	break;
        case 0x35:
        	session.write(LoginAuthPacket.handleLogin3());
        	break;
		}
	}

}
