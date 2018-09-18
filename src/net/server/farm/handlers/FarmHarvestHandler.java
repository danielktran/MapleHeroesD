package net.server.farm.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import tools.data.LittleEndianAccessor;

public class FarmHarvestHandler extends AbstractMaplePacketHandler {

	public FarmHarvestHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		lea.readInt(); //position
        //c.getFarm().getFarmInventory().updateItemQuantity(oid, -1);
        //c.getFarm().gainAestheticPoints(aesthetic); //rewarded from building
	}

}
