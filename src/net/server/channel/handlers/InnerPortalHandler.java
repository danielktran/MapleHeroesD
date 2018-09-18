package net.server.channel.handlers;

import java.awt.Point;

import client.MapleClient;
import client.anticheat.CheatingOffense;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import server.MaplePortal;
import tools.data.LittleEndianAccessor;

public class InnerPortalHandler extends AbstractMaplePacketHandler {

	public InnerPortalHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, final MapleCharacter chr) {
		lea.skip(1);
		if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MaplePortal portal = chr.getMap().getPortal(lea.readMapleAsciiString());
        int toX = lea.readShort();
        int toY = lea.readShort();

        if (portal == null) {
            return;
        }
        if ((portal.getPosition().distanceSq(chr.getTruePosition()) > 22500.0D) && (!chr.isGM())) {
            chr.getCheatTracker().registerOffense(CheatingOffense.USING_FARAWAY_PORTAL);
            return;
        }
        chr.getMap().movePlayer(chr, new Point(toX, toY));
        chr.checkFollow();
	}

}
