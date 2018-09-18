package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CSPacket;
import tools.data.LittleEndianAccessor;

public class CloseChalkboardHandler extends AbstractMaplePacketHandler {

	public CloseChalkboardHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		c.getCharacter().setChalkboard(null);
	}

}
