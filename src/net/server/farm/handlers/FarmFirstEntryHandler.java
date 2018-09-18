package net.server.farm.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.FarmPacket;
import tools.data.LittleEndianAccessor;

public class FarmFirstEntryHandler extends AbstractMaplePacketHandler {

	public FarmFirstEntryHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		//give random waru consume item
        c.getSession().write(FarmPacket.farmMessage("Find your reward for logging in today \r\nin your inventory."));
	}

}
