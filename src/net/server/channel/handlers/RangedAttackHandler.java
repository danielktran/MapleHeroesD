package net.server.channel.handlers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import client.MapleBuffStat;
import client.MapleClient;
import client.MonsterStatus;
import client.MonsterStatusEffect;
import client.PlayerStats;
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
import net.packet.AdventurerPacket;
import net.packet.CField;
import net.packet.CWvsContext;
import net.packet.JobPacket;
import net.packet.JobPacket.AngelicPacket;
import net.server.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.Randomizer;
import server.Timer;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleMonster;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.AttackPair;
import tools.data.LittleEndianAccessor;

public class RangedAttackHandler extends AbstractMaplePacketHandler {

	public RangedAttackHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		
		if (chr == null) {
            return;
        }
        if ((chr.hasBlockedInventory()) || (chr.getMap() == null)) {
            return;
        }
        AttackInfo attack = DamageParse.parseRangeDamage(lea, chr);
        if (attack == null) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        int bulletCount = 1;
        MapleStatEffect effect = null;
        Skill skill = null;
        boolean AOE = attack.skillid == 4111004;
        boolean noBullet = (chr.getJob() >= 300 && chr.getJob() <= 322) || (chr.getJob() >= 3500 && chr.getJob() <= 3512) || GameConstants.isCannon(chr.getJob()) || GameConstants.isXenon(chr.getJob()) || GameConstants.isJett(chr.getJob()) || GameConstants.isPhantom(chr.getJob()) || GameConstants.isMercedes(chr.getJob()) || GameConstants.isZero(chr.getJob()) || GameConstants.isBeastTamer(chr.getJob()) || GameConstants.isLuminous(chr.getJob());
        if (attack.skillid != 0) {
            skill = SkillFactory.getSkill(GameConstants.getLinkedAttackSkill(attack.skillid));
            if ((skill == null) || ((GameConstants.isAngel(attack.skillid)) && (chr.getStat().equippedSummon % 10000 != attack.skillid % 10000))) {
            	c.getSession().write(CWvsContext.enableActions());
                return;
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
            if (GameConstants.isWindArcher(chr.getJob())) {
	            int percent = 0, count = 0, skillid = 0, type = 0;
	            if (c.getCharacter().getSkillLevel(SkillFactory.getSkill(13120003)) > 0) {
	                if (Randomizer.nextInt(100) < 85) {
	                    skillid = 13120003;
	                    type = 1;
	                } else {
	                    skillid = 13120010;
	                    type = 1;
	                }
	                count = Randomizer.rand(1, 5);
	                percent = 20;
	            } else if (c.getCharacter().getSkillLevel(SkillFactory.getSkill(13110022)) > 0) {
	                if (Randomizer.nextInt(100) < 90) {
	                    skillid = 13110022;
	                    type = 1;
	                } else {
	                    skillid = 13110027;
	                    type = 1;
	                }
	                count = Randomizer.rand(1, 4);
	                percent = 10;
	            } else if (c.getCharacter().getSkillLevel(SkillFactory.getSkill(13100022)) > 0) {
	                if (Randomizer.nextInt(100) < 95) {
	                    skillid = 13100022;
	                    type = 1;
	                } else {
	                    skillid = 13100027;
	                    type = 1;
	                }
	                count = Randomizer.rand(1, 3);
	                percent = 5;
	            }
	            for (AttackPair at : attack.allDamage) {
	                
	                MapleMonster mob = chr.getMap().getMonsterByOid(at.objectid);
	                if (Randomizer.nextInt(100) < percent) {
	                    if (mob != null) {
	                        c.getCharacter().getMap().broadcastMessage(c.getCharacter(), JobPacket.WindArcherPacket.TrifleWind(c.getCharacter().getID(), skillid, count, mob.getObjectId(), type), false);
	                        c.getSession().write(JobPacket.WindArcherPacket.TrifleWind(c.getCharacter().getID(), skillid, count, mob.getObjectId(), type));
	                    }
	                }
	            }
	            }      
            if ((chr.getJob() >= 410 && chr.getJob() <= 412)) {
            int percent = 0, count = 0, skillid = 0, type = 0;
            if (c.getCharacter().getSkillLevel(SkillFactory.getSkill(4100011)) > 0) {
                if (Randomizer.nextInt(100) < 99) {
                    skillid = 4100011;
                    //count = Randomizer.rand(1, 5);
                    percent = 99;
                }
                final List<MapleMapObject> objs = c.getCharacter().getMap().getMapObjectsInRange(c.getCharacter().getPosition(), 500000, Arrays.asList(MapleMapObjectType.MONSTER));
                final List<MapleMonster> monsters = new ArrayList<>();
                for (int i = 0; i < bulletCount; i++) {
                	int rand = Randomizer.rand(0, objs.size() - 1);
                	if (objs.size() < bulletCount) {
                		if (i < objs.size()) {
                			monsters.add((MapleMonster) objs.get(i));
                			}
                		} else {
                			monsters.add((MapleMonster) objs.get(rand));
                			objs.remove(rand);
                			}
                	}
                if (monsters.size() <= 0) {
                	c.getCharacter().getClient().getSession().write(CWvsContext.enableActions());
                	return;
                	}
                for (AttackPair at : attack.allDamage) {
                
	                MapleMonster mob = chr.getMap().getMonsterByOid(at.objectid);
	                if (Randomizer.nextInt(100) < percent) {
	                    if (mob != null) {
	                    	final Item star = c.getCharacter().getInventory(MapleInventoryType.USE).getItem(attack.slot);
	                    	final MapleMonster source = c.getCharacter().getMap().getMonsterByOid(at.objectid);
	                    	final MonsterStatusEffect check = source.getBuff(MonsterStatus.POISON);
	                    	c.getCharacter().getMap().broadcastMessage(AdventurerPacket.AssassinPacket.giveMarkOfTheif(c.getCharacter().getID(), source.getObjectId(), (skill.getId() + 1), monsters, c.getCharacter().getPosition(), monsters.get(0).getPosition(), star.getItemId()));
	                    	//     if (star != null) { // TODO: check for quantity
	                    	System.out.println("Star " + star.getItemId());
	                    	c.getCharacter().getMap().broadcastMessage(AdventurerPacket.AssassinPacket.giveMarkOfTheif(c.getCharacter().getID(), source.getObjectId(), (skill.getId() + 1), monsters, c.getCharacter().getPosition(), monsters.get(0).getPosition(), star.getItemId()));
	                    }
	                }
	            }
            }
            }
            switch (attack.skillid) {
                case 13101005:
                case 21110004: // Ranged but uses attackcount instead
                case 14101006: // Vampure
                case 21120006:
                case 11101004:
                // MIHILE
                case 51001004: //Soul Blade
                case 51111007:
                case 51121008:
                // END MIHILE
                case 1077:
                case 1078:
                case 1079:
                case 11077:
                case 11078:
                case 11079:
                case 15111007:
                case 13111007: //Wind Shot
                case 33101007:
                case 13101020://Fary Spiral
                case 33101002:
                case 33121002:
                case 33121001:
                case 21100004:
                case 21110011:
                case 21100007:
                case 21000004:
                case 5121002:
                case 5921002:
                case 4121003:
                case 4221003:
                case 5221017:
                case 5721007:
                case 5221016:
                case 5721006:
                case 5211008:
                case 5201001:
                case 5721003:
                case 5711000:
                case 4111013:
                case 5121016:
                case 5121013:
                case 5221013:
                case 5721004:
                case 5721001:
                case 5321001:
                case 14111008:
                case 60011216://Soul Buster
                case 65001100://Star Bubble
               // case 2321054:
                    AOE = true;
                    bulletCount = effect.getAttackCount();
                    break;
                    
                 
                case 35121005:
                case 35111004:
                case 35121013:
                    AOE = true;
                    bulletCount = 6;
                    break;
                default:
                    bulletCount = effect.getBulletCount();
                    break;
            }
            if (noBullet && effect.getBulletCount() < effect.getAttackCount()) {
                bulletCount = effect.getAttackCount();
            }
            if ((noBullet) && (effect.getBulletCount() < effect.getAttackCount())) {
                bulletCount = effect.getAttackCount();
            }
            if ((effect.getCooldown(chr) > 0) && (!chr.isGM()) && (((attack.skillid != 35111004) && (attack.skillid != 35121013)) || (chr.getBuffSource(MapleBuffStat.MECH_CHANGE) != attack.skillid))) {
                if (chr.skillisCooling(attack.skillid)) {
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                c.getSession().write(CField.skillCooldown(attack.skillid, effect.getCooldown(chr)));
                chr.addCooldown(attack.skillid, System.currentTimeMillis(), effect.getCooldown(chr) * 1000);
            }
        }
        attack = DamageParse.Modify_AttackCrit(attack, chr, 2, effect);
        Integer ShadowPartner = chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER);
        if (ShadowPartner != null) {
            bulletCount *= 2;
        }
        int projectile = 0;
        attack.projectileItemID = 0;
        if ((!AOE) && (chr.getBuffedValue(MapleBuffStat.SOULARROW) == null) && (!noBullet)) {
            Item ipp = chr.getInventory(MapleInventoryType.USE).getItem((short) attack.slot);
            if (ipp == null) {
                return;
            }
            projectile = ipp.getItemId();
            
            if (attack.csstar > 0) {
                if (chr.getInventory(MapleInventoryType.CASH).getItem((short) attack.csstar) == null) {
                    return;
                }
                attack.projectileItemID = chr.getInventory(MapleInventoryType.CASH).getItem((short) attack.csstar).getItemId();
            } else {
                attack.projectileItemID = projectile;
            }

            if (chr.getBuffedValue(MapleBuffStat.SPIRIT_CLAW) == null) {
                int bulletConsume = bulletCount;
                if ((effect != null) && (effect.getBulletConsume() != 0)) {
                    bulletConsume = effect.getBulletConsume() * (ShadowPartner != null ? 2 : 1);
                }
                if ((chr.getJob() == 412) && (bulletConsume > 0) && (ipp.getQuantity() < MapleItemInformationProvider.getInstance().getSlotMax(projectile))) {
                    Skill expert = SkillFactory.getSkill(4120010);
                    if (chr.getTotalSkillLevel(expert) > 0) {
                        MapleStatEffect eff = expert.getEffect(chr.getTotalSkillLevel(expert));
                        if (eff.makeChanceResult()) {
                            ipp.setQuantity((short) (ipp.getQuantity() + 1));
                            c.getSession().write(CWvsContext.InventoryPacket.updateInventorySlot(MapleInventoryType.USE, ipp, false));
                            bulletConsume = 0;
                            c.getSession().write(CWvsContext.InventoryPacket.getInventoryStatus());
                        }
                    }
                }
                if ((bulletConsume > 0) && (!MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, projectile, bulletConsume, false, true))) {
                    chr.dropMessage(5, "You do not have enough arrows/bullets/stars.");
                    return;
                }
            }
        } else if ((chr.getJob() >= 3500) && (chr.getJob() <= 3512)) {
            attack.projectileItemID = 2333000;
        } else if (GameConstants.isCannon(chr.getJob())) {
            attack.projectileItemID = 2333001;
        }
        int projectileWatk = 0;
        if (projectile != 0) {
            projectileWatk = MapleItemInformationProvider.getInstance().getWatkForProjectile(projectile);
        }
        PlayerStats statst = chr.getStat();
        double basedamage;
        switch (attack.skillid) {
            case 4001344:
            case 4121007:
            case 14001004:
            case 14111005:
                basedamage = Math.max(statst.getCurrentMaxBaseDamage(), statst.getTotalLuk() * 5.0F * (statst.getTotalWatk() + projectileWatk) / 100.0F);
                break;
            case 4111004:
                basedamage = 53000.0D;
                break;
            default:
                basedamage = statst.getCurrentMaxBaseDamage();
                switch (attack.skillid) {
                    case 3101005:
                        basedamage *= effect.getX() / 100.0D;
                        break;
                }
        }

        if (effect != null) {
            basedamage *= (effect.getDamage() + statst.getDamageIncrease(attack.skillid)) / 100.0D;

            long money = effect.getMoneyCon();
            if (money != 0) {
                if (money > chr.getMeso()) {
                    money = chr.getMeso();
                }
                chr.gainMeso(-money, false);
            }
        }
        chr.checkFollow();
        if (!chr.isHidden()) {
            if (attack.skillid == 3211006) {
            	attack.hyperLevel = chr.getTotalSkillLevel(3220010);
                //chr.getMap().broadcastMessage(chr, CField.strafeAttack(chr.getID(), attack.tbyte, attack.skillid, attack.skillLevel, attack.display, attack.speed, attack.projectileItemID, attack.allDamage, attack.position, chr.getLevel(), chr.getStat().getPassiveMastery(), attack.unk, chr.getTotalSkillLevel(3220010)), chr.getTruePosition());
                chr.getMap().broadcastMessage(chr, CField.strafeAttack(chr.getID(), attack, chr.getLevel(), chr.getStat().getPassiveMastery()), chr.getTruePosition());
            } else {
                //chr.getMap().broadcastMessage(chr, CField.rangedAttack(chr.getId(), attack.tbyte, attack.skillid, attack.skillLevel, attack.display, attack.speed, visProjectile, attack.allDamage, attack.position, chr.getLevel(), chr.getStat().passive_mastery(), attack.unk), chr.getTruePosition());
                chr.getMap().broadcastMessage(chr, CField.rangedAttack(chr.getID(), attack, chr.getLevel(), chr.getStat().getPassiveMastery()), chr.getTruePosition());
            }
        } else if (attack.skillid == 3211006) {
            //chr.getMap().broadcastGMMessage(chr, CField.strafeAttack(chr.getID(), attack.tbyte, attack.skillid, attack.skillLevel, attack.display, attack.speed, attack.projectileItemID, attack.allDamage, attack.position, chr.getLevel(), chr.getStat().getPassiveMastery(), attack.unk, chr.getTotalSkillLevel(3220010)), false);
            chr.getMap().broadcastGMMessage(chr, CField.strafeAttack(chr.getID(), attack, chr.getLevel(), chr.getStat().getPassiveMastery()), false);
        } else {
            //chr.getMap().broadcastGMMessage(chr, CField.rangedAttack(chr.getID(), attack.tbyte, attack.skillid, attack.skillLevel, attack.display, attack.speed, attack.projectileItemID, attack.allDamage, attack.position, chr.getLevel(), chr.getStat().getPassiveMastery(), attack.unk), false);
            chr.getMap().broadcastGMMessage(chr, CField.rangedAttack(chr.getID(), attack, chr.getLevel(), chr.getStat().getPassiveMastery()), false);

        }

        DamageParse.applyAttack(attack, skill, chr, bulletCount, basedamage, effect, ShadowPartner != null ? AttackType.RANGED_WITH_SHADOWPARTNER : AttackType.RANGED);
        WeakReference<MapleCharacter>[] clones = chr.getClones();
        for (int i = 0; i < clones.length; i++) {
            if (clones[i].get() != null) {
                final MapleCharacter clone = clones[i].get();
                final Skill skil2 = skill;
                final MapleStatEffect eff2 = effect;
                final double basedamage2 = basedamage;
                final int bulletCount2 = bulletCount;
                final int visProjectile2 = attack.projectileItemID;
                final AttackInfo attack2 = DamageParse.DivideAttack(attack, chr.isGM() ? 1 : 4);
                Timer.CloneTimer.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (!clone.isHidden()) {
                            if (attack2.skillid == 3211006) {
                            	attack2.hyperLevel = chr.getTotalSkillLevel(3220010);
                                //clone.getMap().broadcastMessage(CField.strafeAttack(clone.getID(), attack2.tbyte, attack2.skillid, attack2.skillLevel, attack2.display, attack2.speed, visProjectile2, attack2.allDamage, attack2.position, clone.getLevel(), clone.getStat().getPassiveMastery(), attack2.unk, chr.getTotalSkillLevel(3220010)));
                                clone.getMap().broadcastMessage(CField.strafeAttack(clone.getID(), attack2, clone.getLevel(), clone.getStat().getPassiveMastery()), clone.getTruePosition());
                            } else {
                                //clone.getMap().broadcastMessage(CField.rangedAttack(clone.getID(), attack2.tbyte, attack2.skillid, attack2.skillLevel, attack2.display, attack2.speed, visProjectile2, attack2.allDamage, attack2.position, clone.getLevel(), clone.getStat().getPassiveMastery(), attack2.unk));
                                clone.getMap().broadcastMessage(CField.rangedAttack(clone.getID(), attack2, clone.getLevel(), clone.getStat().getPassiveMastery()), clone.getTruePosition());

                            }
                        } else {
                            if (attack2.skillid == 3211006) {
                                //clone.getMap().broadcastGMMessage(clone, CField.strafeAttack(clone.getID(), attack2.tbyte, attack2.skillid, attack2.skillLevel, attack2.display, attack2.speed, visProjectile2, attack2.allDamage, attack2.position, clone.getLevel(), clone.getStat().getPassiveMastery(), attack2.unk, chr.getTotalSkillLevel(3220010)), false);
                                clone.getMap().broadcastGMMessage(clone, CField.strafeAttack(clone.getID(), attack2, clone.getLevel(), clone.getStat().getPassiveMastery()), false);
                            } else {
                                //clone.getMap().broadcastGMMessage(clone, CField.rangedAttack(clone.getID(), attack2.tbyte, attack2.skillid, attack2.skillLevel, attack2.display, attack2.speed, visProjectile2, attack2.allDamage, attack2.position, clone.getLevel(), clone.getStat().getPassiveMastery(), attack2.unk), false);
                                clone.getMap().broadcastGMMessage(clone, CField.rangedAttack(clone.getID(), attack2, clone.getLevel(), clone.getStat().getPassiveMastery()), false);
                            }
                        }
                        DamageParse.applyAttack(attack2, skil2, chr, bulletCount2, basedamage2, eff2, AttackType.RANGED);
                    }
                }, 500 * i + 500);
            }
        }
	}

}
