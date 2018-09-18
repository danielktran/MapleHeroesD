package net.server.channel.handlers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import client.MapleBuffStat;
import client.MapleClient;
import client.MonsterStatus;
import client.MonsterStatusEffect;
import client.Skill;
import client.SkillFactory;
import client.character.MapleCharacter;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.Skills;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.CWvsContext;
import net.packet.JobPacket;
import net.packet.MobPacket;
import net.packet.JobPacket.AngelicPacket;
import net.server.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.MapleStatEffect;
import server.Randomizer;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleMonster;
import server.maps.FieldLimitType;
import server.quest.MapleQuest;
import tools.data.LittleEndianAccessor;

public class SpecialMoveHandler extends AbstractMaplePacketHandler {

	public SpecialMoveHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, final MapleCharacter chr) {
		if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getMap() == null) || (lea.available() < 9L)) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        lea.skip(4);
        int skillid = lea.readInt();
        System.out.println("skill id: " + skillid);
        if (skillid >= 91000000 && skillid < 100000000) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (skillid == 23111008) {
            skillid += Randomizer.nextInt(2);
        }
        int xy1 = 0;
        int xy2 = 0;
        if (skillid == 65111100) {
            xy1 = lea.readShort();
            xy2 = lea.readShort();
            int soulnum = lea.readByte();
            int scheck = 0;
            int scheck2 = 0;
            if (soulnum == 1) {
                scheck = lea.readInt();
            } else if (soulnum == 2) {
                scheck = lea.readInt();
                scheck2 = lea.readInt();
            }
            c.getSession().write(JobPacket.AngelicPacket.SoulSeeker(chr, skillid, soulnum, scheck, scheck2));
            c.getSession().write(JobPacket.AngelicPacket.unlockSkill());
            c.getSession().write(JobPacket.AngelicPacket.RechargeEffect());
            c.getSession().write(CWvsContext.enableActions());
            return;
        } 
        //if (skillid == 13107200) { // Devine Force
        //}
        if (skillid >= 100000000) {
            lea.readByte(); //zero
        }
        int skillLevel = lea.readByte();
//        System.err.println(skillLevel);
        Skill skill = SkillFactory.getSkill(skillid);
        if ((skill == null) || ((GameConstants.isAngel(skillid)) && (chr.getStat().equippedSummon % 10000 != skillid % 10000)) || ((chr.inPVP()) && (skill.isPVPDisabled()))) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        int levelCheckSkill = 0;
        if ((GameConstants.isPhantom(chr.getJob())) && (!GameConstants.isPhantom(skillid / 10000))) {
            int skillJob = skillid / 10000;
            if (skillJob % 100 == 0) {
                levelCheckSkill = 24001001;
            } else if (skillJob % 10 == 0) {
                levelCheckSkill = 24101001;
            } else if (skillJob % 10 == 1) {
                levelCheckSkill = 24111001;
            } else {
                levelCheckSkill = 24121001;
            }
        }
        if ((levelCheckSkill == 0) && ((chr.getTotalSkillLevel(GameConstants.getLinkedAttackSkill(skillid)) <= 0) || (chr.getTotalSkillLevel(GameConstants.getLinkedAttackSkill(skillid)) != skillLevel))) {
            if ((!GameConstants.isMulungSkill(skillid)) && (!GameConstants.isPyramidSkill(skillid)) && (chr.getTotalSkillLevel(GameConstants.getLinkedAttackSkill(skillid)) <= 0) && !GameConstants.isAngel(skillid)) {
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            if (GameConstants.isMulungSkill(skillid)) {
                if (chr.getMapId() / 10000 != 92502) {
                    return;
                }
                if (chr.getMulungEnergy() < 10000) {
                    return;
                }
                chr.mulung_EnergyModify(false);
            } else if ((GameConstants.isPyramidSkill(skillid))
                    && (chr.getMapId() / 10000 != 92602) && (chr.getMapId() / 10000 != 92601)) {
                return;
            }
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
        skillLevel = chr.getTotalSkillLevel(GameConstants.getLinkedAttackSkill(skillid));
        MapleStatEffect effect = chr.inPVP() ? skill.getPVPEffect(skillLevel) : skill.getEffect(skillLevel);
        if ((effect.isMPRecovery()) && (chr.getStat().getHp() < chr.getStat().getMaxHp() / 100 * 10)) {
            c.getCharacter().dropMessage(5, "You do not have the HP to use this skill.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if ((effect.getCooldown(chr) > 0) && (!chr.isGM())) {
            if (chr.skillisCooling(skillid) && skillid != 24121005) {
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            if ((skillid != 5221006) && (skillid != 35111002)) {
                c.getSession().write(CField.skillCooldown(skillid, effect.getCooldown(chr)));
                chr.addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
            }
        }
        int mobID;
        MapleMonster mob;
        switch (skillid) {
            case 1121001:
            case 1221001:
            case 1321001:
            case 9001020:
            case 9101020:
            case 31111003:
                byte number_of_mobs = lea.readByte();
                lea.skip(3);
                for (int i = 0; i < number_of_mobs; i++) {
                    int mobId = lea.readInt();

                    mob = chr.getMap().getMonsterByOid(mobId);
                    if (mob == null) {
                        continue;
                    }
                    mob.switchController(chr, mob.isControllerHasAggro());
                    mob.applyStatus(chr, new MonsterStatusEffect(MonsterStatus.STUN, Integer.valueOf(1), skillid, null, false), false, effect.getDuration(), true, effect);
                }

                chr.getMap().broadcastMessage(chr, CField.EffectPacket.showBuffEffect(chr.getID(), skillid, 1, chr.getLevel(), skillLevel, lea.readByte()), chr.getTruePosition());
                c.getSession().write(CWvsContext.enableActions());
                break;
           case 5201008: { //infinite blast Handler
                int itemid = lea.readInt();
                MapleStatEffect effectp = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid));
                MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemid), itemid, effectp.getBulletConsume(), true, false);
                
                break;
            }

            case 30001061:
                mobID = lea.readInt();
                mob = chr.getMap().getMonsterByOid(mobID);
                if (mob != null) {
                    boolean success = (mob.getHp() <= mob.getMobMaxHp() / 2L) && (mob.getId() >= 9304000) && (mob.getId() < 9305000);
                    chr.getMap().broadcastMessage(chr, CField.EffectPacket.showBuffEffect(chr.getID(), skillid, 1, chr.getLevel(), skillLevel, (byte) (success ? 1 : 0)), chr.getTruePosition());
                    if (success) {
                        chr.getQuestNAdd(MapleQuest.getInstance(GameConstants.JAGUAR)).setCustomData(String.valueOf((mob.getId() - 9303999) * 10));
                        chr.getMap().killMonster(mob, chr, true, false, (byte) 1);
                        chr.cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
                        c.getSession().write(CWvsContext.updateJaguar(chr));
                    } else {
                        chr.dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
                    }
                }
                c.getSession().write(CWvsContext.enableActions());
                break;
            case 30001062:
                chr.dropMessage(5, "No monsters can be summoned. Capture a monster first.");
                c.getSession().write(CWvsContext.enableActions());
                break; 
            case 31221001:
            //case 36100010:
            //case 36110012:
            //case 36120015:
            case 36001005:
            case 2121052: {
                List<Integer> moblist = new ArrayList<Integer>();
                byte count = lea.readByte();
                for (byte i = 1; i <= count; i++) {
                    moblist.add(lea.readInt());
                }
                if (skillid == 31221001) {
                    c.getSession().write(JobPacket.XenonPacket.ShieldChacing(chr.getID(), moblist, 31221014));
                } else if (skillid == 36001005) {
                    c.getSession().write(JobPacket.XenonPacket.PinPointRocket(chr.getID(), moblist));
                } else if (skillid == 2121052) {
                   c.getSession().write(JobPacket.XenonPacket.MegidoFlameRe(chr.getID(), moblist.get(0)));
                }
                break;
            }
            case 33101005:
                mobID = chr.getFirstLinkMid();
                mob = chr.getMap().getMonsterByOid(mobID);
                chr.setKeyDownSkill_Time(0L);
                chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, skillid), false);
                if (mob != null) {
                    boolean success = (mob.getStats().getLevel() < chr.getLevel()) && (mob.getId() < 9000000) && (!mob.getStats().isBoss());
                    if (success) {
                        chr.getMap().broadcastMessage(MobPacket.suckMonster(mob.getObjectId(), chr.getID()));
                        chr.getMap().killMonster(mob, chr, false, false, (byte) -1);
                    } else {
                        chr.dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
                    }
                } else {
                    chr.dropMessage(5, "No monster was sucked. The skill failed.");
                }
                c.getSession().write(CWvsContext.enableActions());
                break;
            case 20040216:
            case 20040217:
            case 20040220:
          //  case 20041239:
               chr.changeLuminousMode(skillid);
            //    chr.HandleOtherluminous();
                c.getSession().write(CWvsContext.enableActions());
                break;
            case 11101022:
            case 11111022:
            case 11121005:
            case 11121011:
            case 11121012:
                chr.changeWarriorStance(skillid);
                c.getSession().write(CWvsContext.enableActions());
                break;
                   case 36121054:
                  //  chr.setXenonSurplus((short) 20);
                    c.getSession().write(CWvsContext.enableActions());
                    c.getSession().write(JobPacket.XenonPacket.giveAmaranthGenerator());
                    break;
            case 110001500:
                c.getSession().write(JobPacket.BeastTamerPacket.ModeCancel());
                c.getSession().write(CWvsContext.enableActions());
                System.out.println(skillid + " ModeCancel has been started from PlayerHandler");
                break;
            case 110001501:
                lea.skip(3);
                c.getSession().write(JobPacket.BeastTamerPacket.BearMode());
                c.getSession().write(CWvsContext.enableActions());
                System.out.println(skillid + " BearMode has been started from PlayerHandler");
                break;
            case 110001502:
                lea.skip(3);
                c.getSession().write(JobPacket.BeastTamerPacket.LeopardMode());
                c.getSession().write(CWvsContext.enableActions());
                System.out.println(skillid + " LeopardMode has been started from PlayerHandler");
                break;
            case 110001503:
                lea.skip(3);
                c.getSession().write(JobPacket.BeastTamerPacket.HawkMode());
                c.getSession().write(CWvsContext.enableActions());
                System.out.println(skillid + " HawkMode has been started from PlayerHandler");
                break;
            case 110001504:
                lea.skip(3);
                c.getSession().write(JobPacket.BeastTamerPacket.CatMode());
                c.getSession().write(CWvsContext.enableActions());
                System.out.println(skillid + " CatMode has been started from PlayerHandler");
                break;
            case 4341003:
                chr.setKeyDownSkill_Time(0L);
                chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, skillid), false);
            default:
                
                Point pos = null;
                if ((lea.available() == 5L) || (lea.available() == 7L)) {
                    pos = lea.readPos();
                }
               
                if (effect.isMagicDoor()) {
                    if (!FieldLimitType.MysticDoor.check(chr.getMap().getFieldLimit())) {
                        effect.applyTo(c.getCharacter(), pos);
                    } else {
                        c.getSession().write(CWvsContext.enableActions());
                    }
                } else {
                    int mountid = MapleStatEffect.parseMountInfo(c.getCharacter(), skill.getId());
                    if ((mountid != 0) && (mountid != GameConstants.getMountItem(skill.getId(), c.getCharacter())) && (!c.getCharacter().isIntern()) && (c.getCharacter().getBuffedValue(MapleBuffStat.MONSTER_RIDING) == null) && (c.getCharacter().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -122) == null)
                            && (!GameConstants.isMountItemAvailable(mountid, c.getCharacter().getJob()))) {
                        c.getSession().write(CWvsContext.enableActions());
                        return;
                    }
//                     System.err.println("pos " + pos);
//                     System.err.println("effect " + effect.getSourceId());
                    effect.applyTo(c.getCharacter(), pos);
                }
        }
        if (GameConstants.isAngelicBuster(chr.getJob())) {

            int Recharge = effect.getOnActive();
            if (Recharge > -1) {
                if (Randomizer.isSuccess(Recharge)) {
                    c.getSession().write(AngelicPacket.unlockSkill());
                    c.getSession().write(AngelicPacket.showRechargeEffect());
                    if (c.getCharacter().isGM()) {
                        c.getSession().write(AngelicPacket.unlockSkill());
//                        c.getSession().write(AngelicPacket.showRechargeEffect());
                    } else {
                        c.getSession().write(AngelicPacket.lockSkill(skillid));
                    }
                }
                c.getSession().write(CWvsContext.enableActions());
            } else {
                if (c.getCharacter().isGM()) {
                    c.getSession().write(AngelicPacket.unlockSkill());
//                    c.getSession().write(AngelicPacket.showRechargeEffect());
                } else {
                    c.getSession().write(AngelicPacket.lockSkill(skillid));
                }
            }
        }

	}

}
