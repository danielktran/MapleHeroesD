package net.server.channel.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import client.MapleClient;
import client.MonsterStatus;
import client.MonsterStatusEffect;
import client.SkillFactory;
import client.anticheat.CheatingOffense;
import client.character.MapleCharacter;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import tools.Triple;
import tools.data.LittleEndianAccessor;

public class AttackFamiliarHandler extends AbstractMaplePacketHandler {

	public AttackFamiliarHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		if (chr.getSummonedFamiliar() == null) {
            return;
        }
        lea.skip(6);
        int skillid = lea.readInt();

        SkillFactory.FamiliarEntry f = SkillFactory.getFamiliar(skillid);
        if (f == null) {
            return;
        }
        byte unk = lea.readByte();
        byte size = lea.readByte();
        List<Triple<Integer, Integer, List<Integer>>> attackPair = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            int oid = lea.readInt();
            int type = lea.readInt();
            lea.skip(10);
            byte si = lea.readByte();
            List attack = new ArrayList(si);
            for (int x = 0; x < si; x++) {
                attack.add(Integer.valueOf(lea.readInt()));
            }
            attackPair.add(new Triple(Integer.valueOf(oid), Integer.valueOf(type), attack));
        }
        if ((attackPair.isEmpty()) || (!chr.getCheatTracker().checkFamiliarAttack(chr)) || (attackPair.size() > f.targetCount)) {
            return;
        }
        MapleMonsterStats oStats = chr.getSummonedFamiliar().getOriginalStats();
        chr.getMap().broadcastMessage(chr, CField.familiarAttack(chr.getID(), unk, attackPair), chr.getTruePosition());
        for (Triple attack : attackPair) {
            MapleMonster mons = chr.getMap().getMonsterByOid(((Integer) attack.left).intValue());
            if ((mons != null) && (mons.isAlive()) && (!mons.getStats().isFriendly()) && (mons.getLinkCID() <= 0) && (((List) attack.right).size() <= f.attackCount)) {
                if ((chr.getTruePosition().distanceSq(mons.getTruePosition()) > 640000.0D) || (chr.getSummonedFamiliar().getTruePosition().distanceSq(mons.getTruePosition()) > GameConstants.getAttackRange(f.lt, f.rb))) {
                    chr.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER_SUMMON);
                }
                for (Iterator i$ = ((List) attack.right).iterator(); i$.hasNext();) {
                    int damage = ((Integer) i$.next()).intValue();
                    if (damage <= oStats.getPhysicalAttack() * 4) {
                        mons.damage(chr, damage, true);
                    }
                }
                if ((f.makeChanceResult()) && (mons.isAlive())) {
                    for (MonsterStatus s : f.status) {
                        mons.applyStatus(chr, new MonsterStatusEffect(s, Integer.valueOf(f.speed), MonsterStatusEffect.genericSkill(s), null, false), false, f.time * 1000, false, null);
                    }
                    if (f.knockback) {
                        mons.switchController(chr, true);
                    }
                }
            }
        }
        chr.getSummonedFamiliar().addFatigue(chr, attackPair.size());
	}

}
