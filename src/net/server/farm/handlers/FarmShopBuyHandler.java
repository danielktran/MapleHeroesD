package net.server.farm.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import tools.data.LittleEndianAccessor;

public class FarmShopBuyHandler extends AbstractMaplePacketHandler {

	public FarmShopBuyHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		int itemId = lea.readInt();
        //c.getFarm().getFarmInventory().gainWaru(-price);
        //c.getFarm().getFarmInventory().updateItemQuantity(itemId, 1);
        //c.getFarm().gainAestheticPoints(aesthetic); //rewarded from building
	}

}
