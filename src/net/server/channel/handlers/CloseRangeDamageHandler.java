package net.server.channel.handlers;

import java.lang.ref.WeakReference;

import client.MapleBuffStat;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import client.character.MapleCharacter;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.AttackInfo;
import net.channel.handler.AttackType;
import net.channel.handler.DamageParse;
import net.channel.handler.PlayerHandler;
import net.packet.CField;
import net.packet.CWvsContext;
import net.packet.JobPacket.AngelicPacket;
import net.server.channel.ChannelServer;
import server.MapleStatEffect;
import server.Randomizer;
import server.Timer;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.events.MapleSnowball;
import tools.AttackPair;
import tools.Pair;
import tools.data.LittleEndianAccessor;

public class CloseRangeDamageHandler extends AbstractMaplePacketHandler {
	
	public CloseRangeDamageHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		final boolean energy = getRecvOpcode() == RecvPacketOpcode.PASSIVE_ENERGY;
		
		if ((chr == null) || ((energy) && (chr.getBuffedValue(MapleBuffStat.ENERGY_CHARGE) == null) && (chr.getBuffedValue(MapleBuffStat.BODY_PRESSURE) == null) && (chr.getBuffedValue(MapleBuffStat.DARK_AURA) == null) && (chr.getBuffedValue(MapleBuffStat.TORNADO) == null) && (chr.getBuffedValue(MapleBuffStat.SUMMON) == null) && (chr.getBuffedValue(MapleBuffStat.RAINING_MINES) == null) && (chr.getBuffedValue(MapleBuffStat.ASURA) == null) && (chr.getBuffedValue(MapleBuffStat.TELEPORT_MASTERY) == null))) {
            return;
        }
        if ((chr.hasBlockedInventory()) || (chr.getMap() == null)) {
            return;
        }
        AttackInfo attack = DamageParse.parseCloseRangeDamage(lea, chr);
        if (attack == null) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        final boolean mirror = chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null;
        double maxdamage = chr.getStat().getCurrentMaxBaseDamage();
        Item shield = c.getCharacter().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
        int attackCount = (shield != null) && (shield.getItemId() / 10000 == 134) ? 2 : 1;
        MapleStatEffect effect = null;
        Skill skill = null;

        String dmg = "";
        for (AttackPair ae : attack.allDamage) {
            for (Pair att : ae.attack) {
                dmg += att.getLeft();
                dmg += ",";
            }
        }
        if (!dmg.isEmpty()) {
//            chr.dropMessage(-1, "Damage: " + dmg);//debug mode
        }
        if (attack.skillid != 0) {
            //chr.dropMessage(-1, "Attack Skill: " + attack.skill);//debug mode
            skill = SkillFactory.getSkill(GameConstants.getLinkedAttackSkill(attack.skillid));
            if ((skill == null) || ((GameConstants.isAngel(attack.skillid)) && (chr.getStat().equippedSummon % 10000 != attack.skillid % 10000))) {
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            if (GameConstants.isDemonAvenger(chr.getJob())) {
                int exceedMax = chr.getSkillLevel(31220044) > 0 ? 20 : 20;
             //   chr.showInfo("Info", false, "exceedMax;" + exceedMax);
                if (chr.getExceed() + 1 > exceedMax) {
                    chr.setExceed((short) exceedMax);
                } else {
                    chr.gainExceed((short) 1);
                }
            }
            if (GameConstants.isExceedAttack(skill.getId())) {
                chr.handleExceedAttack(skill.getId());
            }
            switch (attack.skillid) {
                case 101001100:
                case 101101100:
                case 101111100:
                case 101121100:
                    chr.zeroChange(false);
                    break;
                case 101001200:
                case 101101200:
                case 101111200:
                case 101121200:
                    chr.zeroChange(true);
                    break;

            }
            effect = attack.getAttackEffect(chr, skill);
            if (effect == null) {
                return;
            }
            if (GameConstants.isEventMap(chr.getMapId())) {
                for (MapleEventType t : MapleEventType.values()) {
                    MapleEvent e = ChannelServer.getInstance(chr.getClient().getChannel()).getEvent(t);
                    if ((e.isRunning()) && (!chr.isGM())) {
                        for (int i : e.getType().mapids) {
                            if (chr.getMapId() == i) {
                                chr.dropMessage(5, "You may not use that here.");
                                return;
                            }
                        }
                    }
                }
            }

             
            if (GameConstants.isAngelicBuster(chr.getJob())) {
                int Recharge = effect.getOnActive();
                if (Recharge > -1) {
                    if (Randomizer.isSuccess(Recharge)) {
                        c.getSession().write(AngelicPacket.unlockSkill());
                        c.getSession().write(AngelicPacket.showRechargeEffect());
                    } else {
                        if (c.getCharacter().isGM()) {
                            c.getSession().write(AngelicPacket.unlockSkill());
//                    c.getSession().write(AngelicPacket.showRechargeEffect());
                        } else {
                            c.getSession().write(AngelicPacket.lockSkill(attack.skillid));
                        }
                    }
                } else {
                    if (c.getCharacter().isGM()) {
                        c.getSession().write(AngelicPacket.unlockSkill());
//                    c.getSession().write(AngelicPacket.showRechargeEffect());
                    } else {
                        c.getSession().write(AngelicPacket.lockSkill(attack.skillid));
                    }
                }
            }
            maxdamage *= (effect.getDamage() + chr.getStat().getDamageIncrease(attack.skillid)) / 100.0D;
            attackCount = effect.getAttackCount();

            if ((effect.getCooldown(chr) > 0) && (!chr.isGM()) && (!energy)) {
                if (chr.skillisCooling(attack.skillid)) {
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                c.getSession().write(CField.skillCooldown(attack.skillid, effect.getCooldown(chr)));
                chr.addCooldown(attack.skillid, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
            }
        }
        attack = DamageParse.Modify_AttackCrit(attack, chr, 1, effect);
        attackCount *= (mirror ? 2 : 1);
        if (!energy) {
            if (((chr.getMapId() == 109060000) || (chr.getMapId() == 109060002) || (chr.getMapId() == 109060004)) && (attack.skillid == 0)) {
                MapleSnowball.MapleSnowballs.hitSnowball(chr);
            }

            int numFinisherOrbs = 0;
            Integer comboBuff = chr.getBuffedValue(MapleBuffStat.COMBO);

            if (PlayerHandler.isFinisher(attack.skillid) > 0) {
                if (comboBuff != null) {
                    numFinisherOrbs = comboBuff.intValue() - 1;
                }
                if (numFinisherOrbs <= 0) {
                    return;
                }
                chr.handleOrbconsume(PlayerHandler.isFinisher(attack.skillid));
            }
        }
        chr.checkFollow();
        if (!chr.isHidden()) {
            //chr.getMap().broadcastMessage(chr, CField.closeRangeAttack(chr.getID(), attack.tbyte, attack.skillid, attack.skillLevel, attack.display, attack.speed, attack.allDamage, energy, chr.getLevel(), chr.getStat().getPassiveMastery(), attack.unk, attack.charge), chr.getTruePosition()); // Unnecessary parameters? Could just access them within the method
            chr.getMap().broadcastMessage(chr, CField.closeRangeAttack(chr.getID(), attack, chr.getLevel(), chr.getStat().getPassiveMastery(), energy), chr.getTruePosition());
        } else {
            //chr.getMap().broadcastGMMessage(chr, CField.closeRangeAttack(chr.getID(), attack.tbyte, attack.skillid, attack.skillLevel, attack.display, attack.speed, attack.allDamage, energy, chr.getLevel(), chr.getStat().getPassiveMastery(), attack.unk, attack.charge), false);
            chr.getMap().broadcastGMMessage(chr, CField.closeRangeAttack(chr.getID(), attack, chr.getLevel(), chr.getStat().getPassiveMastery(), energy), false);
        }
        DamageParse.applyAttack(attack, skill, c.getCharacter(), attackCount, maxdamage, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED);
        WeakReference<MapleCharacter>[] clones = chr.getClones();
        for (int i = 0; i < clones.length; i++) {
            if (clones[i].get() != null) {
                final MapleCharacter clone = clones[i].get();
                final Skill skil2 = skill;
                final int attackCount2 = attackCount;
                final double maxdamage2 = maxdamage;
                final MapleStatEffect eff2 = effect;
                final AttackInfo attack2 = DamageParse.DivideAttack(attack, chr.isGM() ? 1 : 4);
                Timer.CloneTimer.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (!clone.isHidden()) {
                            //clone.getMap().broadcastMessage(CField.closeRangeAttack(clone.getID(), attack2.tbyte, attack2.skillid, attack2.skillLevel, attack2.display, attack2.speed, attack2.allDamage, energy, clone.getLevel(), clone.getStat().getPassiveMastery(), attack2.unk, attack2.charge));
                            clone.getMap().broadcastMessage(CField.closeRangeAttack(clone.getID(), attack2, clone.getLevel(), clone.getStat().getPassiveMastery(), energy), chr.getTruePosition());
                        } else {
                            //clone.getMap().broadcastGMMessage(clone, CField.closeRangeAttack(clone.getID(), attack2.tbyte, attack2.skillid, attack2.skillLevel, attack2.display, attack2.speed, attack2.allDamage, energy, clone.getLevel(), clone.getStat().getPassiveMastery(), attack2.unk, attack2.charge), false);
                            clone.getMap().broadcastGMMessage(clone, CField.closeRangeAttack(clone.getID(), attack2, clone.getLevel(), clone.getStat().getPassiveMastery(), energy), false);
                        }
                        DamageParse.applyAttack(attack2, skil2, chr, attackCount2, maxdamage2, eff2, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED);
                    }
                }, 500 * i + 500);
            }
        }
        int bulletCount = 1;
        switch (attack.skillid) {
            case 1201011:
                bulletCount = effect.getAttackCount();
                DamageParse.applyAttack(attack, skill, chr, attack.skillLevel, maxdamage, effect, AttackType.NON_RANGED);//applyAttack(attack, skill, chr, bulletCount, effect, AttackType.RANGED);
                break;
            default:
                DamageParse.applyMagicAttack(attack, skill, chr, effect, maxdamage);//applyAttackMagic(attack, skill, c.getPlayer(), effect);
                break;
        }
	}

}
