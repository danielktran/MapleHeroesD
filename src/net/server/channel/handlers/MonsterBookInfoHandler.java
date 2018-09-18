package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import tools.data.LittleEndianAccessor;

public class MonsterBookInfoHandler extends AbstractMaplePacketHandler {

	public MonsterBookInfoHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		if (c.getCharacter() == null || c.getCharacter().getMap() == null) {
            return;
        }
        lea.readInt(); // tick
        final MapleCharacter player = c.getCharacter().getMap().getCharacterById(lea.readInt());
        c.getSession().write(CWvsContext.enableActions());
        if (player != null && !player.isClone()) {
            if (!player.isGM() || c.getCharacter().isGM()) {
                c.getSession().write(CWvsContext.getMonsterBookInfo(player));
            }
        }
	}

}
