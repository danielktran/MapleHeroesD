package net.server.channel.handlers.summon;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.List;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.MovementParse;
import net.packet.CField;
import net.packet.field.DragonPacket;
import server.Timer.CloneTimer;
import server.maps.MapleMap;
import server.movement.LifeMovementFragment;
import tools.data.LittleEndianAccessor;

public class MoveDragonHandler extends AbstractMaplePacketHandler {

	public MoveDragonHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		lea.skip(12);//New in v14X+
        final List<LifeMovementFragment> res = MovementParse.parseMovement(lea, 5, null, null);
        if (chr != null && chr.getDragon() != null && res.size() > 0) {
            final Point pos = chr.getDragon().getPosition();
            MovementParse.updatePosition(res, chr.getDragon(), 0);
           

            if (!chr.isHidden()) {
                chr.getMap().broadcastMessage(chr, DragonPacket.moveDragon(chr.getDragon(), pos, res), chr.getTruePosition());
            }

            WeakReference<MapleCharacter>[] clones = chr.getClones();
            for (int i = 0; i < clones.length; i++) {
                if (clones[i].get() != null) {
                    final MapleMap map = chr.getMap();
                    final MapleCharacter clone = clones[i].get();
                    CloneTimer.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (clone.getMap() == map && clone.getDragon() != null) {
                                    final Point startPos = clone.getDragon().getPosition();
                                    MovementParse.updatePosition(res, clone.getDragon(), 0);
                                    if (!clone.isHidden()) {
                                        map.broadcastMessage(clone, DragonPacket.moveDragon(clone.getDragon(), startPos, res), clone.getTruePosition());
                                    }

                                }
                            } catch (Exception e) {
                                //very rarely swallowed
                            }
                        }
                    }, 500 * i + 500);
                }
            }
        }
	}

}
