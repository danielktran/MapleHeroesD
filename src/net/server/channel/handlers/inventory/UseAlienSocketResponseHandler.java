package net.server.channel.handlers.inventory;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CSPacket;
import tools.data.LittleEndianAccessor;

public class UseAlienSocketResponseHandler extends AbstractMaplePacketHandler {

	public UseAlienSocketResponseHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		lea.skip(4); // all 0
        c.getSession().write(CSPacket.useAlienSocket(false));
	}
	
}
