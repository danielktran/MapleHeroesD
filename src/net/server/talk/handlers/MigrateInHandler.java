package net.server.talk.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.TalkPacket;
import net.world.World;
import tools.data.LittleEndianAccessor;

public class MigrateInHandler extends AbstractMaplePacketHandler {

	public MigrateInHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final int accountID = lea.readInt();
		c.setAccID(accountID);
		lea.readInt(); // 1
		lea.readLong(); // ??
		lea.readByte(); // 0, could be a boolean
		final int charID = lea.readInt();
		final String charName = lea.readMapleAsciiString();
		lea.readInt(); // 1
		lea.readInt(); // Char Level
		lea.readInt(); // Job ID
		
		System.out.println("Talk acc id" + accountID);
		
        World.getCharacterFromPlayerStorage(charID).getClient().setTalkSession(c);
		
		c.sendPacket(TalkPacket.onMigrateResponse());
	}
	
	@Override
	public boolean validateState(final MapleClient c) {
		return true;
	}

}
