package net.packet;

import client.MonsterStatus;
import client.MonsterStatusEffect;
import client.character.MapleCharacter;
import net.SendPacketOpcode;

import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import server.Randomizer;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.maps.MapleMap;
import server.maps.MapleNodes;
import server.movement.LifeMovementFragment;
import tools.HexTool;
import tools.Pair;
import tools.data.MaplePacketWriter;

public class MobPacket {

    public static byte[] damageMonster(int oid, long damage) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DAMAGE_MONSTER);
		mpw.writeInt(oid);
        mpw.write(0);
        mpw.writeLong(damage);

        return mpw.getPacket();
    }

    public static byte[] damageFriendlyMob(MapleMonster mob, long damage, boolean display) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DAMAGE_MONSTER);
		mpw.writeInt(mob.getObjectId());
        mpw.write(display ? 1 : 2);
        if (damage > 2147483647L) {
            mpw.writeInt(2147483647);
        } else {
            mpw.writeInt((int) damage);
        }
        if (mob.getHp() > 2147483647L) {
            mpw.writeInt((int) (mob.getHp() / mob.getMobMaxHp() * 2147483647.0D));
        } else {
            mpw.writeInt((int) mob.getHp());
        }
        if (mob.getMobMaxHp() > 2147483647L) {
            mpw.writeInt(2147483647);
        } else {
            mpw.writeInt((int) mob.getMobMaxHp());
        }

        return mpw.getPacket();
    }

    public static byte[] killMonster(int oid, int animation, boolean azwan) {
        MaplePacketWriter mpw;

        if (azwan) {
        	mpw = new MaplePacketWriter(SendPacketOpcode.AZWAN_KILL_MONSTER);
        } else {
        	mpw = new MaplePacketWriter(SendPacketOpcode.KILL_MONSTER);
        }
        boolean a = false; //idk
        boolean b = false; //idk
        if (azwan) {
            mpw.write(a ? 1 : 0);
            mpw.write(b ? 1 : 0);
        }
        mpw.writeInt(oid);
        if (azwan) {
            if (a) {
                mpw.write(0);
                if (b) {
                    //set mob temporary stat
                } else {
                    //set mob temporary stat
                }
            } else {
                if (b) {
                    //idk
                } else {
                    //idk
                }
            }
            return mpw.getPacket();
        }
        mpw.write(animation);
        if (animation == 4) {
            mpw.writeInt(-1);
        }

        return mpw.getPacket();
    }

    public static byte[] suckMonster(int oid, int chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.KILL_MONSTER);
		mpw.writeInt(oid);
        mpw.write(4);
        mpw.writeInt(chr);

        return mpw.getPacket();
    }

    public static byte[] healMonster(int oid, int heal) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DAMAGE_MONSTER);
		mpw.writeInt(oid);
        mpw.write(0);
        mpw.writeInt(-heal);

        return mpw.getPacket();
    }
    
    public static byte[] ForbidMonsterAttack(int objectId, List<Byte> attacks) {
	    MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MOB_REACTION);
		mpw.writeInt(objectId);
	    mpw.writeInt(attacks.size());
	    for (byte b = 0; b < attacks.size(); b++) {
	        mpw.writeInt(1);
	    }
	    return mpw.getPacket();
	}

    public static byte[] MobToMobDamage(int oid, int dmg, int mobid, boolean azwan) {
        MaplePacketWriter mpw;

        if (azwan) {
        	mpw = new MaplePacketWriter(SendPacketOpcode.AZWAN_MOB_TO_MOB_DAMAGE);
        } else {
        	mpw = new MaplePacketWriter(SendPacketOpcode.MOB_TO_MOB_DAMAGE);
        }
        mpw.writeInt(oid);
        mpw.write(0);
        mpw.writeInt(dmg);
        mpw.writeInt(mobid);
        mpw.write(1);

        return mpw.getPacket();
    }

    public static byte[] getMobSkillEffect(int oid, int skillid, int cid, int skilllevel) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SKILL_EFFECT_MOB);
		mpw.writeInt(oid);
        mpw.writeInt(skillid);
        mpw.writeInt(cid);
        mpw.writeShort(skilllevel);

        return mpw.getPacket();
    }

    public static byte[] getMobCoolEffect(int oid, int itemid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ITEM_EFFECT_MOB);
		mpw.writeInt(oid);
        mpw.writeInt(itemid);

        return mpw.getPacket();
    }

    public static byte[] showMonsterHP(int oid, int remhppercentage) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_MONSTER_HP);
		mpw.writeInt(oid);
        mpw.write(remhppercentage);

        return mpw.getPacket();
    }

    public static byte[] showCygnusAttack(int oid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CYGNUS_ATTACK);
		mpw.writeInt(oid);

        return mpw.getPacket();
    }

    public static byte[] showMonsterResist(int oid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_RESIST);
		mpw.writeInt(oid);
        mpw.writeInt(0);
        mpw.writeShort(1);
        mpw.writeInt(0);

        return mpw.getPacket();
    }

    public static byte[] showBossHP(MapleMonster mob) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BOSS_ENV);
		mpw.write(6);
        mpw.writeInt(mob.getId() == 9400589 ? 9300184 : mob.getId());
        if (mob.getHp() > 2147483647L) {
            mpw.writeInt((int) (mob.getHp() / mob.getMobMaxHp() * 2147483647.0D));
        } else {
            mpw.writeInt((int) mob.getHp());
        }
        if (mob.getMobMaxHp() > 2147483647L) {
            mpw.writeInt(2147483647);
        } else {
            mpw.writeInt((int) mob.getMobMaxHp());
        }
      //  SmartMobnotice(8840000, 1, 0, "Fuck", (byte) 1);
        mpw.write(mob.getStats().getTagColor());
        mpw.write(mob.getStats().getTagBgColor());

        return mpw.getPacket();
    }
    
    public static byte[] SmartMobnotice(int mobId, int type, int number, String message, byte color) {
	    MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SMART_MOB_NOTICE);
		mpw.writeInt(color);//0:white, 1:yellow, 2:blue
	    mpw.writeInt(mobId);
	    mpw.writeInt(type);//1:attack, 2:skill, 3:change controller, 5:mobzone?
	    mpw.writeInt(number);//attack 1+, skill 0+
	    mpw.writeMapleAsciiString(message);
	
	    return mpw.getPacket();
    }

    public static byte[] showBossHP(int monsterId, long currentHp, long maxHp) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BOSS_ENV);
		mpw.write(6);
        mpw.writeInt(monsterId);
        if (currentHp > 2147483647L) {
            mpw.writeInt((int) (currentHp / maxHp * 2147483647.0D));
        } else {
            mpw.writeInt((int) (currentHp <= 0L ? -1L : currentHp));
        }
        if (maxHp > 2147483647L) {
            mpw.writeInt(2147483647);
        } else {
            mpw.writeInt((int) maxHp);
        }
        mpw.write(6);
        mpw.write(5);

        return mpw.getPacket();
    }
    
    /*
    public static byte[] moveMonster(boolean useskill, int skill, int skillID, int skillLv, int option, int oid, Point startPos, List<LifeMovementFragment> moves, List<Integer> unk2, List<Pair<Integer, Integer>> unk3) {
        MaplePacketLittleEndianWriter mpw = new MaplePacketLittleEndianWriter();

        mpw.writeShort(SendPacketOpcode.MOVE_MONSTER);
		mpw.writeInt(oid);
        mpw.write(useskill ? 1 : 0);
        mpw.write(skill);
        mpw.write(skillID);
        mpw.write(skillLv);
        mpw.writeShort(option);
        mpw.write(unk3 == null ? 0 : unk3.size());
        if (unk3 != null) {
            for (Pair i : unk3) {
                mpw.writeShort(((Integer) i.left).intValue());
                mpw.writeShort(((Integer) i.right).intValue());
            }
        }
        mpw.write(unk2 == null ? 0 : unk2.size());
        if (unk2 != null) {
            for (Integer i : unk2) {
                mpw.writeShort(i.intValue());
            }
        }
        mpw.writeInt(0);
        mpw.writePos(startPos);
        mpw.writeShort(0);
        mpw.writeShort(0);
        PacketHelper.serializeMovementList(mpw, moves);
        mpw.write(0); // new
        return mpw.getPacket();
    } */

    public static byte[] moveMonster(boolean useskill, int skill, int skill1, int skill2, int skill3, int skill4, int oid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MOVE_MONSTER);
		mpw.writeInt(oid);
        mpw.write(useskill ? 1 : 0);
        mpw.write(skill);
        mpw.write(skill1);
        mpw.write(skill2);
        mpw.write(skill3);
        mpw.write(skill4);
        mpw.write(0); // nSkillID
        mpw.write(0); // v21
        mpw.writeInt(0);
        mpw.writePos(startPos);
        mpw.writeInt(0); // v171
        //mpw.writeInt(Randomizer.nextInt());
        PacketHelper.serializeMovementList(mpw, moves);
        mpw.write(0); // v171
        
        return mpw.getPacket();
    }

    /*
    public static byte[] moveMonster(boolean useskill, int skill, int unk, int oid, Point startPos, List<LifeMovementFragment> moves) {
        return moveMonster(useskill, skill, unk, oid, startPos, moves, null, null);
    }
    
/*
    public static byte[] moveMonster(boolean useskill, int skill, int unk, int oid, Point startPos, List<LifeMovementFragment> moves, List<Integer> unk2, List<Pair<Integer, Integer>> unk3) {
        MaplePacketLittleEndianWriter mpw = new MaplePacketLittleEndianWriter();

        mpw.writeShort(SendPacketOpcode.MOVE_MONSTER);
		mpw.writeInt(oid);
        mpw.write(useskill ? 1 : 0);
        mpw.write(skill);
        mpw.writeInt(unk);
        mpw.write(unk3 == null ? 0 : unk3.size());
        if (unk3 != null) {
            for (Pair i : unk3) {
                mpw.writeShort(((Integer) i.left));
                mpw.writeShort(((Integer) i.right));
            }
        }
        mpw.write(unk2 == null ? 0 : unk2.size());
        if (unk2 != null) {
            for (Integer i : unk2) {
                mpw.writeShort(i);
            }
        }

        mpw.writeInt(0);
        mpw.writePos(startPos);
        mpw.writeInt(0);

        PacketHelper.serializeMovementList(mpw, moves);

        return mpw.getPacket();
    }*/

    public static byte[] moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MOVE_MONSTER_RESPONSE);
		mpw.writeInt(objectid);
        mpw.writeShort(moveid);
        mpw.write(useSkills ? 1 : 0);
        mpw.writeInt(currentMp);
        mpw.write(skillId);
        mpw.write(skillLevel);
        mpw.writeInt(0);
        mpw.writeShort(0); // New: v174
        mpw.write(0);      // New: v174

        return mpw.getPacket();
    }
    
    public static byte[] spawnMonster(MapleMonster life, int spawnType, int link, boolean azwan) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_MONSTER);
		mpw.write(0); // bSealedInsteadDead
        mpw.writeInt(life.getObjectId());
        mpw.write(1); // nCalcDamageIndex
        mpw.writeInt(life.getId());
        
        mpw.write(0); // Forced Mob Stat boolean
        
        int[] flag = new int[3];
        flag[1] |= 0x60000000;
        flag[2] |= 0x600000;
        flag[2] |= 0x400000;
        flag[2] |= 0x70000;
        flag[2] |= 0xFF00;
        flag[2] |= 0x80;
        
        for (int i = 0; i < flag.length; i++) {
            mpw.writeInt(flag[i]);
        }

        short monstergen = (short) (life.getObjectId() / 2); // who knows
        for (int i = 0; i < 4; i++) {
            mpw.writeLong(0);
            mpw.writeShort(monstergen);
        }
        mpw.writeZeroBytes(119);
        
        //addMonsterStatus(mpw, life);
        mpw.writePos(life.getTruePosition());
        mpw.write(life.getStance());
        if (life.getId() == 8910000 || life.getId() == 8910100) {
            mpw.write(0);
        }
        mpw.writeShort(life.getFh());//was0
        mpw.writeShort(life.getFh());
        
        mpw.writeShort(-2); // -1 if used in controlMonster summonType
        mpw.write(-1); // team
        mpw.writeInt(life.getHp() > 2147483647 ? 2147483647 : (int) life.getHp());
        mpw.writeZeroBytes(21);
        
        mpw.writeInt(-1);
        mpw.writeInt(-1);
        mpw.writeInt(0);
        mpw.write(0);
        
        mpw.writeInt(100);
        mpw.writeInt(-1);
        
        mpw.writeInt(0);
        mpw.write(0);
        mpw.writeZeroBytes(6); //v174
        
        /*
        mpw.write(spawnType);
        if ((spawnType == -3) || (spawnType >= 0)) {
            mpw.writeInt(link);
        }
        */
        /*
        mpw.write(life.getCarnivalTeam());
        mpw.writeInt(life.getHp() > 2147483647 ? 2147483647 : (int) life.getHp());
        mpw.writeInt(0);//new 142
        mpw.writeZeroBytes(16);
        mpw.write(0);
        mpw.writeInt(-1);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.write(0);
        mpw.write(-2);
        */
               
        return mpw.getPacket();
    }

    public static void addMonsterStatus(MaplePacketWriter mpw, MapleMonster life) {
        mpw.write(life.getChangedStats() != null);
        if (life.getChangedStats() != null) {
            mpw.writeInt(life.getChangedStats().hp > 2147483647L ? 2147483647 : (int) life.getChangedStats().hp);
            mpw.writeInt(life.getChangedStats().mp);
            mpw.writeInt(life.getChangedStats().exp);
            mpw.writeInt(life.getChangedStats().watk);
            mpw.writeInt(life.getChangedStats().matk);
            mpw.writeInt(life.getChangedStats().PDRate);
            mpw.writeInt(life.getChangedStats().MDRate);
            mpw.writeInt(life.getChangedStats().acc);
            mpw.writeInt(life.getChangedStats().eva);
            mpw.writeInt(life.getChangedStats().pushed);
            mpw.writeInt(life.getChangedStats().speed);//new 141?
            mpw.writeInt(life.getChangedStats().level);
        }
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);

        mpw.writeShort(5088); // E0 13
        mpw.write(72);//0x48
        mpw.writeInt(0);
        mpw.write(0x88); // flag maybe
        short monstergen = (short) (life.getId() / 2); // who knows
        for (int i = 0; i < 4; ++i) {
            mpw.writeLong(0);
            mpw.writeShort(monstergen);
        }
        mpw.writeZeroBytes(19);
    }

    public static void addMonsterInformation(MaplePacketWriter mpw, MapleMonster life, boolean newSpawn, boolean summon, byte spawnType, int link) {
        mpw.writePos(life.getTruePosition());
        mpw.write(life.getStance());
        mpw.writeShort(0);
        mpw.writeShort(life.getFh());
        if (summon) {
            mpw.write(spawnType);
            if ((spawnType == -3) || (spawnType >= 0)) {
                mpw.writeInt(link);
            }
        } else {
            mpw.write(newSpawn ? -2 : life.isFake() ? -4 : -1);
        }
        mpw.write(life.getCarnivalTeam());
        mpw.writeInt(63000);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.write(-1);
    }

    public static byte[] controlMonster(MapleMonster life, boolean newSpawn, boolean aggro, boolean azwan) {
        MaplePacketWriter mpw;

        if (azwan) {
        	mpw = new MaplePacketWriter(SendPacketOpcode.AZWAN_SPAWN_MONSTER_CONTROL);
        } else {
        	mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_MONSTER_CONTROL);
        }
        if (!azwan) {
            mpw.write(aggro ? 2 : 1);
        }

        mpw.writeInt(life.getObjectId());
        mpw.write(1);// 1 = Control normal, 5 = Control none?
        mpw.writeInt(life.getId());
        
        mpw.write(0); // Forced Mob Stat boolean
        
        int[] flag = new int[3];
        flag[1] |= 0x60000000;
        flag[2] |= 0x600000;
        flag[2] |= 0x400000;
        flag[2] |= 0x70000;
        flag[2] |= 0xFF00;
        flag[2] |= 0x80;
        
        for (int i = 0; i < flag.length; i++) {
            mpw.writeInt(flag[i]);
        }

        short monstergen = (short) (life.getObjectId() / 2); // who knows
        for (int i = 0; i < 4; i++) {
            mpw.writeLong(0);
            mpw.writeShort(monstergen);
        }
        mpw.writeZeroBytes(119);
        
        //addMonsterStatus(mpw, life);
        mpw.writePos(life.getTruePosition());
        mpw.write(life.getStance());
        if (life.getId() == 8910000 || life.getId() == 8910100) {
            mpw.write(0);
        }
        mpw.writeShort(life.getFh());//was0
        mpw.writeShort(life.getFh());
        
        mpw.writeShort(-2); // -1 if used in controlMonster summonType
        mpw.write(-1); // team
        mpw.writeInt(life.getHp() > 2147483647 ? 2147483647 : (int) life.getHp());
        mpw.writeZeroBytes(21);
        
        mpw.writeInt(-1);
        mpw.writeInt(-1);
        mpw.writeInt(0);
        mpw.write(0);
        
        mpw.writeInt(100);
        mpw.writeInt(-1);
        
        mpw.writeInt(0);
        mpw.write(0);
        mpw.writeZeroBytes(6); //v174
        /*
        mpw.write(spawnType);
        if ((spawnType == -3) || (spawnType >= 0)) {
            mpw.writeInt(link);
        }
        */
        /*
        mpw.write(life.getCarnivalTeam());
        mpw.writeInt(life.getHp() > 2147483647 ? 2147483647 : (int) life.getHp());
        mpw.writeInt(0);//new 142
        mpw.writeZeroBytes(16);
        mpw.write(0);
        mpw.writeInt(-1);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.write(0);
        mpw.write(-2);
        */
        return mpw.getPacket();
    }

    public static byte[] stopControllingMonster(MapleMonster life, boolean azwan) {
        MaplePacketWriter mpw;

        if (azwan) {
        	mpw = new MaplePacketWriter(SendPacketOpcode.AZWAN_SPAWN_MONSTER_CONTROL);
        } else {
        	mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_MONSTER_CONTROL);
        }
        if (!azwan) {
            mpw.write(0);
        }
        mpw.writeInt(life.getObjectId());
        if (azwan) {
            mpw.write(0);
            mpw.writeInt(0);
            mpw.write(0);
            addMonsterStatus(mpw, life);

            mpw.writePos(life.getTruePosition());
            mpw.write(life.getStance());
            mpw.writeShort(0);
            mpw.writeShort(life.getFh());
            mpw.write(life.isFake() ? -4 : -1);
            mpw.write(life.getCarnivalTeam());
            mpw.writeInt(63000);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.write(-1);
        }

        return mpw.getPacket();
    }

    public static byte[] makeMonsterReal(MapleMonster life, boolean azwan) {
        return spawnMonster(life, -1, 0, azwan);
    }

    public static byte[] makeMonsterFake(MapleMonster life, boolean azwan) {
        return spawnMonster(life, -4, 0, azwan);
    }

    public static byte[] makeMonsterEffect(MapleMonster life, int effect, boolean azwan) {
        return spawnMonster(life, effect, 0, azwan);
    }

    public static byte[] getMonsterSkill(int objectid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_SKILL);
		mpw.writeInt(objectid);
        mpw.writeLong(0);

        return mpw.getPacket();
    }

    public static byte[] getMonsterTeleport(int objectid, int x, int y) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.TELE_MONSTER);
		mpw.writeInt(objectid);
        mpw.writeInt(x);
        mpw.writeInt(y);

        return mpw.getPacket();
    }

    private static void getLongMask_NoRef(MaplePacketWriter mpw, Collection<MonsterStatusEffect> ss, boolean ignore_imm) {
        int[] mask = new int[12];
        for (MonsterStatusEffect statup : ss) {
            if ((statup != null) && (statup.getStati() != MonsterStatus.WEAPON_DAMAGE_REFLECT) && (statup.getStati() != MonsterStatus.MAGIC_DAMAGE_REFLECT) && ((!ignore_imm) || ((statup.getStati() != MonsterStatus.WEAPON_IMMUNITY) && (statup.getStati() != MonsterStatus.MAGIC_IMMUNITY) && (statup.getStati() != MonsterStatus.DAMAGE_IMMUNITY)))) {
                mask[(statup.getStati().getPosition() - 1)] |= statup.getStati().getValue();
            }
        }
        for (int i = mask.length; i >= 1; i--) {
            mpw.writeInt(mask[(i - 1)]);
        }
    }

    public static byte[] applyMonsterStatus(int oid, MonsterStatus mse, int x, MobSkill skil) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.APPLY_MONSTER_STATUS);
		mpw.writeInt(oid);
        PacketHelper.writeSingleMask(mpw, mse);

        mpw.writeInt(x);
        mpw.writeShort(skil.getSkillId());
        mpw.writeShort(skil.getSkillLevel());
        mpw.writeShort(mse.isEmpty() ? 1 : 0);

        mpw.writeShort(0);
        mpw.write(2);//was 1
        mpw.writeZeroBytes(30);

        System.out.println("ams 2");
        return mpw.getPacket();
    }
   

    public static byte[] applyMonsterStatus(MapleMonster mons, MonsterStatusEffect ms, MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.APPLY_MONSTER_STATUS);
		mpw.writeInt(mons.getObjectId());
        
        PacketHelper.writeMobMask(mpw, ms.getStati());
        
        
        mpw.writeInt(ms.getX().intValue());
        if (ms.isMonsterSkill()) {
            mpw.writeShort(ms.getMobSkill().getSkillId());
            mpw.writeShort(ms.getMobSkill().getSkillLevel());
        } else if (ms.getSkill() > 0) {
            mpw.writeInt(ms.getSkill());
        }
        mpw.writeShort((short) ((ms.getCancelTask() - System.currentTimeMillis()) / 1000));

        mpw.writeInt(chr.getID()); // Char ID
        /*
        mpw.writeInt(20000);
        mpw.writeInt(1000);
        mpw.writeInt(0); // Update tick? or CRC
        mpw.writeInt(7468);
        mpw.writeInt(6); //Duration?
        mpw.writeZeroBytes(20);
        mpw.writeInt(7850);
        mpw.writeShort(468);
        mpw.write(1);
        */
        
        mpw.write(HexTool.getByteArrayFromHexString("70 13 00 00 00 00 00 00 00 00 0B"));

        //mpw.write(HexTool.getByteArrayFromHexString("01 00 00 00 9B BA 3E 00 0C 00 01 61 87 9D 00 9B BA 3E 00 AA 1E 00 00 E8 03 00 00 1A 46 B7 15 2C 1D 00 00 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AA 1E 00 00 D4 01 0A"));
        System.out.println("ams 3");
        System.out.println(mpw.toString());
        return mpw.getPacket();
    }

    public static byte[] applyMonsterStatus(MapleMonster mons, List<MonsterStatusEffect> mse) {
        if ((mse.size() <= 0) || (mse.get(0) == null)) {
            return CWvsContext.enableActions();
        }
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.APPLY_MONSTER_STATUS);
		mpw.writeInt(mons.getObjectId());
        MonsterStatusEffect ms = (MonsterStatusEffect) mse.get(0);
        if (ms.getStati() == MonsterStatus.POISON) {
            PacketHelper.writeMobMask(mpw, MonsterStatus.EMPTY);
            mpw.write(mse.size());
            for (MonsterStatusEffect m : mse) {
                mpw.writeInt(m.getFromID());
                if (m.isMonsterSkill()) {
                    mpw.writeShort(m.getMobSkill().getSkillId());
                    mpw.writeShort(m.getMobSkill().getSkillLevel());
                } else if (m.getSkill() > 0) {
                    mpw.writeInt(m.getSkill());
                }
                mpw.writeInt(m.getX().intValue());
                mpw.writeInt(1000);
                mpw.writeInt(0);
                mpw.writeInt(8000);//new v141
                mpw.writeInt(6);
                mpw.writeInt(0);
                mpw.writeZeroBytes(20);
                mpw.writeInt(7850);  
            }
            mpw.writeShort(1000);//was 300
            mpw.write(11);//was 1
            //mpw.write(1);
        } else {
            PacketHelper.writeMobMask(mpw, ms.getStati());

            mpw.writeInt(ms.getX().intValue());
            if (ms.isMonsterSkill()) {
                mpw.writeShort(ms.getMobSkill().getSkillId());
                mpw.writeShort(ms.getMobSkill().getSkillLevel());
            } else if (ms.getSkill() > 0) {
                mpw.writeInt(ms.getSkill());
            }
            mpw.writeShort((short) ((ms.getCancelTask() - System.currentTimeMillis()) / 1000));
            mpw.writeLong(0L);
            mpw.writeShort(0);
            mpw.write(1);
        }
//System.out.println("Monsterstatus3");
        System.out.println("ams 4");
        System.out.println(mpw.toString());
        return mpw.getPacket();
    }

    public static byte[] applyMonsterStatus(int oid, Map<MonsterStatus, Integer> stati, List<Integer> reflection, MobSkill skil) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.APPLY_MONSTER_STATUS);
		mpw.writeInt(oid);
        PacketHelper.writeMask(mpw, stati.keySet());

        for (Map.Entry mse : stati.entrySet()) {
            mpw.writeInt(((Integer) mse.getValue()).intValue());
            mpw.writeInt(skil.getSkillId());
            mpw.writeShort((short) skil.getDuration());
        }

        for (Integer ref : reflection) {
            mpw.writeInt(ref.intValue());
        }
        mpw.writeLong(0L);
        mpw.writeShort(0);

        int size = stati.size();
        if (reflection.size() > 0) {
            size /= 2;
        }
        mpw.write(size);
        
        System.out.println("ams 1");
        return mpw.getPacket();
    }

    public static byte[] applyPoison(MapleMonster mons, List<MonsterStatusEffect> mse) {
        if ((mse.size() <= 0) || (mse.get(0) == null)) {
            return CWvsContext.enableActions();
        }
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.APPLY_MONSTER_STATUS);
		mpw.writeInt(mons.getObjectId());
        PacketHelper.writeSingleMask(mpw, MonsterStatus.EMPTY);
        mpw.write(mse.size());
        for (MonsterStatusEffect m : mse) {
            mpw.writeInt(m.getFromID());
            if (m.isMonsterSkill()) {
                mpw.writeShort(m.getMobSkill().getSkillId());
                mpw.writeShort(m.getMobSkill().getSkillLevel());
            } else if (m.getSkill() > 0) {
                mpw.writeInt(m.getSkill());
            }
            mpw.writeInt(m.getX().intValue());
            mpw.writeInt(1000);
            mpw.writeInt(0);//600574518?
            mpw.writeInt(8000);//war 7000
            mpw.writeInt(6);//was 5
            mpw.writeInt(0);
        }
        mpw.writeShort(1000);//was 300
        mpw.write(2);//was 1
        //mpw.write(1);
//System.out.println("Monsterstatus5");
        return mpw.getPacket();
    }

    public static byte[] cancelMonsterStatus(int oid, MonsterStatus stat) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANCEL_MONSTER_STATUS);
		mpw.writeInt(oid);
        PacketHelper.writeMobMask(mpw, stat);
        mpw.writeInt(1);
        mpw.writeInt(1);
        mpw.writeInt(0); // Char ID
        mpw.write(HexTool.getByteArrayFromHexString("B0 A9 EB 03 0C"));

        System.out.println("cancel status");
        return mpw.getPacket();
    }

    public static byte[] cancelPoison(int oid, MonsterStatusEffect m) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANCEL_MONSTER_STATUS);
		mpw.writeInt(oid);
        PacketHelper.writeMobMask(mpw, MonsterStatus.EMPTY);
        mpw.writeInt(0);
        mpw.writeInt(1);
        mpw.writeInt(m.getFromID());
        if (m.isMonsterSkill()) {
            mpw.writeShort(m.getMobSkill().getSkillId());
            mpw.writeShort(m.getMobSkill().getSkillLevel());
        } else if (m.getSkill() > 0) {
            //mpw.writeInt(m.getSkill());
        }
        mpw.write(HexTool.getByteArrayFromHexString("B0 DC ED 03")); // Update tick?
        mpw.write(1); // This is just a counter. It increments by 1 each time APPLY or CANCEL_MONSTER_STATUS is sent.

        System.out.println("cancelpoison");
        return mpw.getPacket();
    }

    public static byte[] talkMonster(int oid, int itemId, String msg) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.TALK_MONSTER);
		mpw.writeInt(oid);
        mpw.writeInt(500);
        mpw.writeInt(itemId);
        mpw.write(itemId <= 0 ? 0 : 1);
        mpw.write((msg == null) || (msg.length() <= 0) ? 0 : 1);
        if ((msg != null) && (msg.length() > 0)) {
            mpw.writeMapleAsciiString(msg);
        }
        mpw.writeInt(1);

        return mpw.getPacket();
    }

    public static byte[] removeTalkMonster(int oid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_TALK_MONSTER);
		mpw.writeInt(oid);

        return mpw.getPacket();
    }

    public static final byte[] getNodeProperties(MapleMonster objectid, MapleMap map) {
        if (objectid.getNodePacket() != null) {
            return objectid.getNodePacket();
        }
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_PROPERTIES);
		mpw.writeInt(objectid.getObjectId());
        mpw.writeInt(map.getNodes().size());
        mpw.writeInt(objectid.getPosition().x);
        mpw.writeInt(objectid.getPosition().y);
        for (MapleNodes.MapleNodeInfo mni : map.getNodes()) {
            mpw.writeInt(mni.x);
            mpw.writeInt(mni.y);
            mpw.writeInt(mni.attr);
            if (mni.attr == 2) {
                mpw.writeInt(500);
            }
        }
        mpw.writeInt(0);
        mpw.write(0);
        mpw.write(0);

        objectid.setNodePacket(mpw.getPacket());
        return objectid.getNodePacket();
    }

    public static byte[] showMagnet(int mobid, boolean success) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_MAGNET);
		mpw.writeInt(mobid);
        mpw.write(success ? 1 : 0);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] catchMonster(int mobid, int itemid, byte success) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CATCH_MONSTER);
		mpw.writeInt(mobid);
        mpw.writeInt(itemid);
        mpw.write(success);

        return mpw.getPacket();
    }
}
