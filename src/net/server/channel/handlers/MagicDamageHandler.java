package net.server.channel.handlers;

import java.lang.ref.WeakReference;

import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import client.character.MapleCharacter;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.AttackInfo;
import net.channel.handler.AttackType;
import net.channel.handler.DamageParse;
import net.packet.CField;
import net.packet.CWvsContext;
import net.packet.JobPacket;
import net.server.channel.ChannelServer;
import server.MapleStatEffect;
import server.Timer;
import server.events.MapleEvent;
import server.events.MapleEventType;
import tools.data.LittleEndianAccessor;

public class MagicDamageHandler extends AbstractMaplePacketHandler {

	public MagicDamageHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null)) {
            return;
        }
        AttackInfo attack = DamageParse.parseMagicDamage(lea, chr);
        if (attack == null) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        
        Skill skill = SkillFactory.getSkill(GameConstants.getLinkedAttackSkill(attack.skillid));
        if ((skill == null) || ((GameConstants.isAngel(attack.skillid)) && (chr.getStat().equippedSummon % 10000 != attack.skillid % 10000))) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        MapleStatEffect effect = attack.getAttackEffect(chr, skill);
        if (effect == null) {
            return;
        }
        
        switch (attack.skillid) {
           case 27001100: // Flash Shower
           case 27101100: //Sylvan Lance
           case 27111100: //Spectral Light
           case 27121100: //Reflection
                 if (chr.getLuminousState() <= 20040000 && chr.getLevel() > 30) {
                chr.getClient().getSession().write(JobPacket.LuminousPacket.giveLuminousState(20040216, chr.getLightGauge(), chr.getDarkGauge(), 2000000000));
                chr.setLuminousState(20040216);
                 } else {
                //donothing
                 }
            break;
           case 27001201:
           case 27101202:
           case 27111202:
           case 27121202:
                if (chr.getLuminousState() <= 20040000 && chr.getLevel() > 30) {
                chr.getClient().getSession().write(JobPacket.LuminousPacket.giveLuminousState(20040217, chr.getLightGauge(), chr.getDarkGauge(), 2000000000));
                chr.setLuminousState(20040217);
                 } else {
                //donothing
                 }

        }
        double maxdamage = chr.getStat().getCurrentMaxBaseDamage() * (effect.getDamage() + chr.getStat().getDamageIncrease(attack.skillid)) / 100.0D;
        int bulletCount = 1;
        switch (attack.skillid) {
        	case 27101100: // Sylvan Lance
            case 27101202: // Pressure Void
            case 27111100: // Spectral Light
            case 27111202: // Moonlight Spear
            case 27121100: // Reflection
            case 27001100:
            case 27121202: // Apocalypse
            case 2121006: // Paralyze
            case 2221003: // 
            case 2221006: // Chain Lightning
            case 2221007: // Blizzard
            case 2221012: // Frozen Orb
            case 2111003: // Poison Mist
            case 12111005: //Flame Gear?
            case 2121003: // Myst Eruption
            case 22181002: // Dark Fog
            case 2321054:
            case 27121303:
            case 27111303:
            case 36121013:
         //   case 36101009:
       //     case 36111010:
                bulletCount = effect.getAttackCount();
                DamageParse.applyAttack(attack, skill, chr, attack.skillLevel, maxdamage, effect, AttackType.RANGED);//applyAttack(attack, skill, chr, bulletCount, effect, AttackType.RANGED);
                break;
            default:
                DamageParse.applyMagicAttack(attack, skill, chr, effect, maxdamage);//applyAttackMagic(attack, skill, c.getPlayer(), effect);
                break;
        }
        
     /*   if (skill.getId() >= 27100000 && skill.getId() < 27120400 && attack.targets > 0 && chr.getLuminousState() < 20040000) {
            //chr.changeSkillLevel(SkillFactory.getSkill(20040216), (byte) 1, (byte) 1);
            //chr.changeSkillLevel(SkillFactory.getSkill(20040217), (byte) 1, (byte) 1);
            //chr.changeSkillLevel(SkillFactory.getSkill(20040220), (byte) 1, (byte) 1);
            //chr.changeSkillLevel(SkillFactory.getSkill(20041239), (byte) 1, (byte) 1);
            chr.setLuminousState(GameConstants.getLuminousSkillMode(skill.getId()));
            c.getSession().write(JobPacket.LuminousPacket.giveLuminousState(GameConstants.getLuminousSkillMode(skill.getId()), chr.getLightGauge(), chr.getDarkGauge(), 10000));
            SkillFactory.getSkill(GameConstants.getLuminousSkillMode(skill.getId())).getEffect(1).applyTo(chr);
        }*/
        attack = DamageParse.Modify_AttackCrit(attack, chr, 3, effect);
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
      //  double maxdamage = chr.getStat().getCurrentMaxBaseDamage() * (effect.getDamage() + chr.getStat().getDamageIncrease(attack.skill)) / 100.0D;
        if (GameConstants.isPyramidSkill(attack.skillid)) {
            maxdamage = 1.0D;
        } else if ((GameConstants.isBeginnerJob(skill.getId() / 10000)) && (skill.getId() % 10000 == 1000)) {
            maxdamage = 40.0D;
        }
        if ((effect.getCooldown(chr) > 0) && (!chr.isGM())) {
            if (chr.skillisCooling(attack.skillid)) {
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            c.getSession().write(CField.skillCooldown(attack.skillid, effect.getCooldown(chr)));
            chr.addCooldown(attack.skillid, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
        }
        chr.checkFollow();
        if (!chr.isHidden()) {
            //chr.getMap().broadcastMessage(chr, CField.magicAttack(chr.getID(), attack.tbyte, attack.skillid, attack.skillLevel, attack.display, attack.speed, attack.allDamage, attack.charge, chr.getLevel(), attack.unk), chr.getTruePosition());
            chr.getMap().broadcastMessage(chr, CField.magicAttack(chr.getID(), attack, chr.getLevel(), chr.getStat().getPassiveMastery()), chr.getTruePosition());
        } else {
            //chr.getMap().broadcastGMMessage(chr, CField.magicAttack(chr.getID(), attack.tbyte, attack.skillid, attack.skillLevel, attack.display, attack.speed, attack.allDamage, attack.charge, chr.getLevel(), attack.unk), false);
            chr.getMap().broadcastGMMessage(chr, CField.magicAttack(chr.getID(), attack, chr.getLevel(), chr.getStat().getPassiveMastery()), false);
        }
        DamageParse.applyMagicAttack(attack, skill, c.getCharacter(), effect, maxdamage);
        WeakReference<MapleCharacter>[] clones = chr.getClones();
        for (int i = 0; i < clones.length; i++) {
            if (clones[i].get() != null) {
                final MapleCharacter clone = clones[i].get();
                final Skill skil2 = skill;
                final MapleStatEffect eff2 = effect;
                final double maxd = maxdamage;
                final AttackInfo attack2 = DamageParse.DivideAttack(attack, chr.isGM() ? 1 : 4);
                Timer.CloneTimer.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (!clone.isHidden()) {
                            //clone.getMap().broadcastMessage(CField.magicAttack(clone.getID(), attack2.tbyte, attack2.skillid, attack2.skillLevel, attack2.display, attack2.speed, attack2.allDamage, attack2.charge, clone.getLevel(), attack2.unk));
                            clone.getMap().broadcastMessage(CField.magicAttack(clone.getID(), attack2, clone.getLevel(), clone.getStat().getPassiveMastery()));
                        } else {
                            //clone.getMap().broadcastGMMessage(clone, CField.magicAttack(clone.getID(), attack2.tbyte, attack2.skillid, attack2.skillLevel, attack2.display, attack2.speed, attack2.allDamage, attack2.charge, clone.getLevel(), attack2.unk), false);
                            clone.getMap().broadcastGMMessage(clone, CField.magicAttack(clone.getID(), attack2, clone.getLevel(), clone.getStat().getPassiveMastery()), false);
                        }
                        DamageParse.applyMagicAttack(attack2, skil2, chr, eff2, maxd);
                    }
                }, 500 * i + 500);
            }
        }
	}

}
