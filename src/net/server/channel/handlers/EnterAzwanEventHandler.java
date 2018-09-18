package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.CWvsContext;
import tools.data.LittleEndianAccessor;

public class EnterAzwanEventHandler extends AbstractMaplePacketHandler {

	public EnterAzwanEventHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		if (c.getCharacter() == null || c.getCharacter().getMap() == null) {
        c.getSession().write(CField.pvpBlocked(1));
        c.getSession().write(CWvsContext.enableActions());
        return;
    }
    int mapid = lea.readInt();
    c.getCharacter().changeMap(c.getChannelServer().getMapFactory().getMap(mapid));

	}

}
