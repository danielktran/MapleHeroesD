package net.server.farm.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.FarmPacket;
import tools.data.LittleEndianAccessor;

public class RenameMonsterHandler extends AbstractMaplePacketHandler {

	public RenameMonsterHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		int monsterIndex = lea.readInt();
        String name = lea.readMapleAsciiString();
        //c.getFarm().getMonster(monsterIndex).setName(name);
        c.getSession().write(FarmPacket.renameMonster(monsterIndex, name));
	}

}
