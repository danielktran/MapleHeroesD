package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import tools.data.LittleEndianAccessor;

public class OwlHandler extends AbstractMaplePacketHandler {

	public OwlHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		if (c.getCharacter().haveItem(5230000, 1, true, false) || c.getCharacter().haveItem(2310000, 1, true, false)) {
            if (c.getCharacter().getMapId() >= 910000000 && c.getCharacter().getMapId() <= 910000022) {
                c.getSession().write(CWvsContext.getOwlOpen());
            } else {
                c.getCharacter().dropMessage(5, "This can only be used inside the Free Market.");
                c.getSession().write(CWvsContext.enableActions());
            }
        }
	}

}
