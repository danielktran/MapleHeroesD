package net.server.channel.handlers.summon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import client.MapleBuffStat;
import client.MapleClient;
import client.MonsterStatus;
import client.MonsterStatusEffect;
import client.Skill;
import client.SkillFactory;
import client.SummonSkillEntry;
import client.anticheat.CheatingOffense;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.MobPacket;
import net.packet.CField.SummonPacket;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import tools.Pair;
import tools.data.LittleEndianAccessor;

public class SummonAttackHandler extends AbstractMaplePacketHandler {

	public SummonAttackHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, final MapleCharacter chr) {
		if (chr == null || !chr.isAlive() || chr.getMap() == null) {
            return;
        }
        final MapleMap map = chr.getMap();
        final MapleMapObject obj = map.getMapObject(lea.readInt(), MapleMapObjectType.SUMMON);
        if (obj == null || !(obj instanceof MapleSummon)) {
            chr.dropMessage(5, "The summon has disappeared.");
            return;
        }
        final MapleSummon summon = (MapleSummon) obj;
        if (summon.getOwnerId() != chr.getID() || summon.getSkillLevel() <= 0) {
            chr.dropMessage(5, "Error.");
            return;
        }
        
        int tick = lea.readInt();
        final int skillid = lea.readInt();
        final SummonSkillEntry sse = SkillFactory.getSummonData(skillid);
        if (skillid / 1000000 != 35 && skillid != 33101008 && sse == null) {
            chr.dropMessage(5, "Error in processing attack.");
            return;
        }
        if (sse != null && sse.delay > 0) {
            chr.updateTick(tick);
            //summon.CheckSummonAttackFrequency(chr, tick);
            //chr.getCheatTracker().checkSummonAttack();
        }
        lea.skip(4);
        final byte animation = lea.readByte();
        byte tbyte = (byte) (lea.readByte());
        byte numAttacked = (byte) ((tbyte >>> 4) & 0xF);
        if (sse != null && numAttacked > sse.mobCount) {
            chr.dropMessage(5, "Warning: Attacking more monster than summon can do");
            chr.getCheatTracker().registerOffense(CheatingOffense.SUMMON_HACK_MOBS);
            //AutobanManager.getInstance().autoban(c, "Attacking more monster that summon can do (Skillid : "+summon.getSkill()+" Count : " + numAttacked + ", allowed : " + sse.mobCount + ")");
            return;
        }
        lea.skip(summon.getSkill() == 35111002 ? 24 : 12); //some pos stuff
        lea.skip(10);
        final List<Pair<Integer, Integer>> allDamage = new ArrayList<>();
        for (int i = 0; i < numAttacked; i++) {
            int oid = lea.readInt();
            MapleMonster mob = map.getMonsterByOid(oid);

            if (mob == null) {
                continue;
            }
            lea.skip(24); // who knows
            final int damage = lea.readInt();
            allDamage.add(new Pair<>(mob.getObjectId(), damage));
            lea.skip(4);
        }
        lea.skip(4);
        //if (!summon.isChangedMap()) {
        map.broadcastMessage(chr, SummonPacket.summonAttack(summon.getOwnerId(), summon.getObjectId(), animation, allDamage, chr.getLevel(), false), summon.getTruePosition());
        //}
        final Skill summonSkill = SkillFactory.getSkill(summon.getSkill());
        final MapleStatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());
        if (summonEffect == null) {
            chr.dropMessage(5, "Error in attack.");
            return;
        }
        for (Pair<Integer, Integer> attackEntry : allDamage) {
            final int toDamage = attackEntry.right;
            final MapleMonster mob = map.getMonsterByOid(attackEntry.left);
            if (mob == null) {
                continue;
            }
            if (sse != null && sse.delay > 0 && summon.getMovementType() != SummonMovementType.STATIONARY && summon.getMovementType() != SummonMovementType.CIRCLE_STATIONARY && summon.getMovementType() != SummonMovementType.WALK_STATIONARY && chr.getTruePosition().distanceSq(mob.getTruePosition()) > 400000.0) {
                //chr.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER_SUMMON);
            }
            if (toDamage > 0 && summonEffect.getMonsterStati().size() > 0) {
                if (summonEffect.makeChanceResult()) {
                    for (Map.Entry<MonsterStatus, Integer> z : summonEffect.getMonsterStati().entrySet()) {
                        mob.applyStatus(chr, new MonsterStatusEffect(z.getKey(), z.getValue(), summonSkill.getId(), null, false), summonEffect.isPoison(), 4000, true, summonEffect);
                    }
                }
            }
            if (chr.isGM() || toDamage < (chr.getStat().getCurrentMaxBaseDamage() * 5.0 * (summonEffect.getSelfDestruction() + summonEffect.getDamage() + chr.getStat().getDamageIncrease(summonEffect.getSourceId())) / 100.0)) { //10 x dmg.. eh
                mob.damage(chr, toDamage, true);
                chr.checkMonsterAggro(mob);
                if (!mob.isAlive()) {
                    chr.getClient().getSession().write(MobPacket.killMonster(mob.getObjectId(), 1, false));
                }
            } else {
                //chr.dropMessage(5, "Warning - high damage.");
                //AutobanManager.getInstance().autoban(c, "High Summon Damage (" + toDamage + " to " + attackEntry.right + ")");
                // TODO : Check player's stat for damage checking.
                break;
            }
        }
        if (!summon.isMultiAttack()) {
            chr.getMap().broadcastMessage(SummonPacket.removeSummon(summon, true));
            chr.getMap().removeMapObject(summon);
            chr.removeVisibleMapObject(summon);
            chr.removeSummon(summon);
            if (summon.getSkill() != 35121011) {
                chr.cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
            }
        }
	}

}
