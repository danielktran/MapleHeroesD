package net.server.channel.handlers.inventory;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.InventoryHandler;
import tools.data.LittleEndianAccessor;

public class UsePotentialScrollHandler extends AbstractMaplePacketHandler {

	public UsePotentialScrollHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		c.getCharacter().updateTick(lea.readInt());
        InventoryHandler.UseUpgradeScroll(lea.readShort(), lea.readShort(), lea.readShort(), c, c.getCharacter(), lea.readByte() > 0);
	}

}
