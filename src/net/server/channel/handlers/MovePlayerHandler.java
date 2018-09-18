package net.server.channel.handlers;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.List;

import client.MapleBuffStat;
import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.MovementParse;
import net.packet.CField;
import server.Timer;
import server.maps.MapleMap;
import server.movement.LifeMovementFragment;
import tools.data.LittleEndianAccessor;

public class MovePlayerHandler extends AbstractMaplePacketHandler {

	public MovePlayerHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		
		lea.skip(14);
        lea.readInt(); // position
        lea.readInt();

        if (chr == null) {
            return;
        }
        final Point Original_Pos = chr.getPosition();
        List res;
        try {
            res = MovementParse.parseMovement(lea, 1, null, chr);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(new StringBuilder().append("AIOBE Type1:\n").append(lea.toString(true)).toString());
            return;
        }

        if ((res != null) && (c.getCharacter().getMap() != null)) {
        	/*
            if ((slea.available() < 11L) || (slea.available() > 26L)) {
            	// if (slea.available() != 18L) {
                return;
            }
            */
            final MapleMap map = c.getCharacter().getMap();

            if (chr.isHidden()) {
                chr.setLastRes(res);
                c.getCharacter().getMap().broadcastGMMessage(chr, CField.movePlayer(chr.getID(), res, Original_Pos), false);
            } else {
                c.getCharacter().getMap().broadcastMessage(c.getCharacter(), CField.movePlayer(chr.getID(), res, Original_Pos), false);
            }

            MovementParse.updatePosition(res, chr, 0);
            final Point pos = chr.getTruePosition();
            map.movePlayer(chr, pos);
            if ((chr.getFollowId() > 0) && (chr.isFollowOn()) && (chr.isFollowInitiator())) {
                MapleCharacter fol = map.getCharacterById(chr.getFollowId());
                if (fol != null) {
                    Point original_pos = fol.getPosition();
                    fol.getClient().getSession().write(CField.moveFollow(Original_Pos, original_pos, pos, res));
                    MovementParse.updatePosition(res, fol, 0);
                    map.movePlayer(fol, pos);
                    map.broadcastMessage(fol, CField.movePlayer(fol.getID(), res, original_pos), false);
                } else {
                    chr.checkFollow();
                }
            }
            WeakReference<MapleCharacter>[] clones = chr.getClones();
            for (int i = 0; i < clones.length; i++) {
                if (clones[i].get() != null) {
                    final MapleCharacter clone = clones[i].get();
                    final List<LifeMovementFragment> res3 = res;
                    Timer.CloneTimer.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (clone.getMap() == map) {
                                    if (clone.isHidden()) {
                                        map.broadcastGMMessage(clone, CField.movePlayer(clone.getID(), res3, Original_Pos), false);
                                    } else {
                                        map.broadcastMessage(clone, CField.movePlayer(clone.getID(), res3, Original_Pos), false);
                                    }
                                    MovementParse.updatePosition(res3, clone, 0);
                                    map.movePlayer(clone, pos);
                                }
                            } catch (Exception e) {
                                //very rarely swallowed
                            }
                        }
                    }, 500 * i + 500);
                }
            }

            int count = c.getCharacter().getFallCounter();
            boolean samepos = (pos.y > c.getCharacter().getOldPosition().y) && (Math.abs(pos.x - c.getCharacter().getOldPosition().x) < 5);
            if ((samepos) && ((pos.y > map.getBottom() + 250) || (map.getFootholds().findBelow(pos) == null))) {
                if (count > 5) {
                    c.getCharacter().changeMap(map, map.getPortal(0));
                    c.getCharacter().setFallCounter(0);
                } else {
                    count++;
                    c.getCharacter().setFallCounter(count);
                }
            } else if (count > 0) {
                c.getCharacter().setFallCounter(0);
            }
            c.getCharacter().setOldPosition(pos);
            if ((!samepos) && (c.getCharacter().getBuffSource(MapleBuffStat.DARK_AURA) == 32120000)) {
                c.getCharacter().getStatForBuff(MapleBuffStat.DARK_AURA).applyMonsterBuff(c.getCharacter());
            } else if ((!samepos) && (c.getCharacter().getBuffSource(MapleBuffStat.YELLOW_AURA) == 32120001)) {
                c.getCharacter().getStatForBuff(MapleBuffStat.YELLOW_AURA).applyMonsterBuff(c.getCharacter());
            }
        }
	}

}
