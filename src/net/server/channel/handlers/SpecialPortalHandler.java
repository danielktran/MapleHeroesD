package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import server.MaplePortal;
import tools.data.LittleEndianAccessor;

public class SpecialPortalHandler extends AbstractMaplePacketHandler {

	public SpecialPortalHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, final MapleCharacter chr) {
		if ((c.getCharacter() == null) || (c.getCharacter().getMap() == null)) {
            return;
        }
		lea.readByte();
		final String portalName = lea.readMapleAsciiString();
        final MaplePortal portal = c.getCharacter().getMap().getPortal(portalName);

        // if (chr.getGMLevel() > ServerConstants.PlayerGMRank.GM.getLevel()) {
        //  chr.dropMessage(6, new StringBuilder().append(portal.getScriptName()).append(" accessed").toString());
        //  }
        if ((portal != null) && (!c.getCharacter().hasBlockedInventory())) {
            portal.enterPortal(c);
        } else {
            c.getSession().write(CWvsContext.enableActions());
        }
	}

}
