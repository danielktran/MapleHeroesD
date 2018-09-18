package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import server.quest.MapleQuest;
import tools.data.LittleEndianAccessor;

public class AllowPartyInviteHandler extends AbstractMaplePacketHandler {

	public AllowPartyInviteHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, final MapleCharacter chr) {
		if (lea.readByte() > 0) {
            c.getCharacter().getQuestRemove(MapleQuest.getInstance(122901));
        } else {
            c.getCharacter().getQuestNAdd(MapleQuest.getInstance(122901));
        }
	}

}
