package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import server.maps.MapleReactor;
import tools.data.LittleEndianAccessor;

public class DamageReactorHandler extends AbstractMaplePacketHandler {

	public DamageReactorHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final int oid = lea.readInt();
        final int charPos = lea.readInt();
        final short stance = lea.readShort();
        final MapleReactor reactor = c.getCharacter().getMap().getReactorByOid(oid);
        System.out.println("Hit Reactor:  " + reactor);

        if (reactor == null || !reactor.isAlive()) {
            return;
        }
        reactor.hitReactor(charPos, stance, c);
	}

}
