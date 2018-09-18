package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import scripting.reactor.ReactorScriptManager;
import server.MapleInventoryManipulator;
import server.maps.MapleReactor;
import tools.data.LittleEndianAccessor;

public class TouchReactorHandler extends AbstractMaplePacketHandler {

	public TouchReactorHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final int oid = lea.readInt();
        final boolean touched = lea.available() == 0 || lea.readByte() > 0; //the byte is probably the state to set it to
        final MapleReactor reactor = c.getCharacter().getMap().getReactorByOid(oid);
        System.out.println("Touch Reactor:  " + reactor);
        if (!touched || reactor == null || !reactor.isAlive() || reactor.getTouch() == 0) {
            return;
        }
        if (reactor.getTouch() == 2) {
            ReactorScriptManager.getInstance().act(c, reactor); //not sure how touched boolean comes into play
        } else if (reactor.getTouch() == 1 && !reactor.isTimerActive()) {
            if (reactor.getReactorType() == 100) {
                final int itemid = GameConstants.getCustomReactItem(reactor.getReactorId(), reactor.getReactItem().getLeft());
                if (c.getCharacter().haveItem(itemid, reactor.getReactItem().getRight())) {
                    if (reactor.getArea().contains(c.getCharacter().getTruePosition())) {
                        MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemid), itemid, reactor.getReactItem().getRight(), true, false);
                        reactor.hitReactor(c);
                    } else {
                        c.getCharacter().dropMessage(5, "You are too far away.");
                    }
                } else {
                    c.getCharacter().dropMessage(5, "You don't have the item required.");
                }
            } else {
                //just hit it
                reactor.hitReactor(c);
            }
        }
	}

}
