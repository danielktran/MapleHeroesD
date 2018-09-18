package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import tools.data.LittleEndianAccessor;

public class CharInfoRequestHandler extends AbstractMaplePacketHandler {

	public CharInfoRequestHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
        chr.updateTick(lea.readInt());
        final int objectid = lea.readInt();
        
        if (c.getCharacter() == null || c.getCharacter().getMap() == null) {
            return;
        }
        MapleCharacter player = c.getCharacter().getMap().getCharacterById(objectid);
        c.getSession().write(CWvsContext.enableActions());
        if (player != null/* && (!player.isGM() || c.getPlayer().isGM())*/) {
            c.getSession().write(CWvsContext.charInfo(player, c.getCharacter().getID() == objectid));
        }
	}

}
