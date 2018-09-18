package net.server.farm.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import tools.data.LittleEndianAccessor;

public class FarmCheckQuestHandler extends AbstractMaplePacketHandler {

	public FarmCheckQuestHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		int farmId = lea.readInt();
        //TODO code farm quests
        if (c.getFarm().getName().equals("Creating...")) {
            //c.getSession().write(FarmPacket.updateQuestInfo(1111, 1, "A1/Z/"));
        }
	}

}
