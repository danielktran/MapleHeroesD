package net.server.login.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.MaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.LoginPacket;
import tools.data.LittleEndianAccessor;

public class ClientRequestHandler implements MaplePacketHandler {
	private RecvPacketOpcode recv;
	
	public ClientRequestHandler(RecvPacketOpcode recv) {
		this.recv = recv;
	}
	
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		c.getSession().write(LoginPacket.getClientResponse(lea.readInt()));
	}

	public boolean validateState(MapleClient c) {
		return true;
	}

	public RecvPacketOpcode getRecvOpcode() {
		return recv;
	}

}
