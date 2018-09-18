package net.server.channel.handlers.monster;

import java.awt.Point;
import java.util.List;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.MovementParse;
import net.packet.MobPacket;
import server.Randomizer;
import server.Timer.WorldTimer;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import tools.Pair;
import tools.data.LittleEndianAccessor;

public class MoveLifeHandler extends AbstractMaplePacketHandler {

	public MoveLifeHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		final int oid = lea.readInt();
        final MapleMonster monster = chr.getMap().getMonsterByOid(oid);

        if (monster == null) { // movin something which is not a monster
            return;
        }
        //String originalrh = rh.toString();
        lea.skip(1); // Azwan boolean
        final short moveid = lea.readShort();
        final boolean useSkill = lea.readByte() == 1;
        final byte skill = lea.readByte();
        final int skill1 = lea.readByte() & 0xFF; // unsigned?
        final int skill2 = lea.readByte() & 0xFF;
        final int skill3 = lea.readByte();
        final int skill4 = lea.readByte();
        int realskill = 0;
        int level = 0;
        /*if (!useSkill && skill != 0) {
         chr.send(MobPacket.talkMonster(oid, moveid));
         chr.message("몬스터톡" + moveid);
         }
         */
        if (useSkill) {// &&  { useSkill
            final byte size = monster.getNoSkills();
            boolean used = false;
            //chr.send(MainPacketCreator.serverNotice(6, "size" + size));
            if (size > 0) {
                final Pair<Integer, Integer> skillToUse = monster.getSkills().get((byte) Randomizer.nextInt(size));
                realskill = skillToUse.getLeft();
                level = skillToUse.getRight();
                // Skill ID and Level
                final MobSkill mobSkill = MobSkillFactory.getMobSkill(realskill, level);
                if (!mobSkill.checkCurrentBuff(chr, monster)) {
                    final long now = System.currentTimeMillis();
                    final long ls = monster.getLastSkillUsed(realskill);

                    if (ls == 0 || ((now - ls) > mobSkill.getCoolTime())) {
                        monster.setLastSkillUsed(realskill, now, mobSkill.getCoolTime());

                        final int reqHp = (int) (((float) monster.getHp() / monster.getMobMaxHp()) * 100); // In case this monster have 2.1b and above HP
                        if (reqHp <= mobSkill.getHP()) {
                            used = true;
                            if (mobSkill.getCoolTime() == 0) {
                                mobSkill.applyEffect(chr, monster, true);
                            } else {
                                WorldTimer.getInstance().schedule(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (monster != null) {
                                            mobSkill.applyEffect(c.getCharacter(), monster, true);
                                        }
                                    }
                                }, 1000L); // TODO delay
                            }
                        }
                    }
                }
            }
            if (!used) {
                realskill = 0;
                level = 0;
            }
        }
        lea.skip(32);

        final Point startPos = monster.getPosition();
        List res;
        res = MovementParse.parseMovement(lea, 2, startPos);

        if (monster != null && c != null) {
            c.getSession().write(MobPacket.moveMonsterResponse(oid, moveid, monster.getMp(), monster.isControllerHasAggro(), realskill, level));
        }
        if (res != null) {
            final MapleMap map = c.getCharacter().getMap();
            MovementParse.updatePosition(res, monster, -1);
            map.moveMonster(monster, monster.getPosition());
            //chr.dropMessage(0, monster.getPosition().toString());
            map.broadcastMessage(chr, MobPacket.moveMonster(useSkill, skill, skill1, skill2, skill3, skill4, oid, startPos, res), monster.getPosition());

   //         chr.getCheatTracker().checkMoveMonster(monster);
            //map.checkClockContact(chr, monster);
        }
	}

}
