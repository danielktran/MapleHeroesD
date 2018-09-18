package net.server.login.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.MaplePacketHandler;
import net.RecvPacketOpcode;
import tools.data.LittleEndianAccessor;

public class ViewServerListHandler extends AbstractMaplePacketHandler {

	public ViewServerListHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		if(lea.readByte() == 0) {
			MaplePacketHandler handler = new ServerlistRequestHandler(recv);
			handler.handlePacket(lea, c, chr);
		}

	}

}
