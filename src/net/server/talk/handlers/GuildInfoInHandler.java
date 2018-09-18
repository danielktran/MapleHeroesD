package net.server.talk.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.TalkPacket;
import tools.data.LittleEndianAccessor;

public class GuildInfoInHandler extends AbstractMaplePacketHandler {

	public GuildInfoInHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final int guildID = lea.readInt();
		final int charID = lea.readInt();
		
		c.sendPacket(TalkPacket.onTalkSessionID(c.getAccountID()));
	}
	
	@Override
	public boolean validateState(final MapleClient c) {
		return true;
	}

}
