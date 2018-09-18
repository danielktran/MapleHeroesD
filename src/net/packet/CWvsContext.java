package net.packet;

import client.*;
import client.MapleStat.Temp;
import client.character.MapleCharacter;
import client.inventory.*;
import constants.GameConstants;
import constants.Skills.Bishop;
import constants.Skills.IceLightningMage;
import constants.Skills.FirePoisonMage;
import net.SendPacketOpcode;
import net.server.channel.DojoRankingsData;
import net.server.channel.MapleGuildRanking;
import net.server.channel.MapleGeneralRanking.CandyRankingInfo;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.World;
import net.world.exped.MapleExpedition;
import net.world.exped.PartySearch;
import net.world.exped.PartySearchType;
import net.world.family.MapleFamily;
import net.world.family.MapleFamilyBuff;
import net.world.family.MapleFamilyCharacter;
import net.world.guild.*;

import java.awt.Point;
import java.util.*;
import java.util.Map.Entry;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.Randomizer;
import server.StructFamiliar;
import server.life.MapleMonster;
import server.life.PlayerNPC;
import server.stores.HiredMerchant;
import server.stores.MaplePlayerShopItem;
import tools.HexTool;
import tools.Pair;
import tools.StringUtil;
import tools.data.MaplePacketWriter;

public class CWvsContext {

    public static byte[] enableActions() {
        return updatePlayerStats(new EnumMap<MapleStat, Long>(MapleStat.class), true, null);
    }

    public static byte[] updatePlayerStats(Map<MapleStat, Long> stats, MapleCharacter chr) {
        return updatePlayerStats(stats, false, chr);
    }

    public static byte[] updatePlayerStats(Map<MapleStat, Long> mystats, boolean itemReaction, MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_STATS);
		mpw.write(itemReaction ? 1 : 0);
        long updateMask = 0L;
        //Added
        if (mystats.containsKey(MapleStat.MAXMP) && GameConstants.isDemonSlayer(chr.getJob())) {
            mystats.remove(MapleStat.MAXMP);
            mystats.put(MapleStat.MAXMP, (long) 10);
        }
        // End
        for (MapleStat statupdate : mystats.keySet()) {
            updateMask |= statupdate.getValue();
        }
        mpw.writeLong(updateMask);
        for (final Entry<MapleStat, Long> statupdate : mystats.entrySet()) {
            switch (statupdate.getKey()) {
                case SKIN:
                case LEVEL:
                case FATIGUE:
                case BATTLE_RANK:
                case ICE_GAGE:
                    mpw.write((statupdate.getValue()).byteValue());
                    break;
//                case JOB:
                case STR:
                case DEX:
                case INT:
                case LUK:
                case AVAILABLEAP:
                    mpw.writeShort((statupdate.getValue()).shortValue());
                    break;
                // Added
                case JOB:
                	mpw.writeShort((statupdate.getValue()).shortValue());
                    mpw.writeShort(chr.getSubcategory()); // not sure about it
                    break;
                // Ended
                case AVAILABLESP:
                    if (GameConstants.isSeparatedSp(chr.getJob())) {
                        mpw.write(chr.getRemainingSpSize());
                        for (int i = 0; i < chr.getRemainingSps().length; i++) {
                            if (chr.getRemainingSp(i) > 0) {
                                mpw.write(i + 1);
                                mpw.writeInt(chr.getRemainingSp(i));
                            }
                        }
                    } else {
                        mpw.writeShort(chr.getRemainingSp());
                    }
                    break;
                case TRAIT_LIMIT:
                    mpw.writeInt((statupdate.getValue()).intValue());
                    mpw.writeInt((statupdate.getValue()).intValue());
                    mpw.writeInt((statupdate.getValue()).intValue());
                    break;
                case EXP:
                case MESO:
                    mpw.writeLong((statupdate.getValue()).longValue());
                    break;
                case PET:
                    mpw.writeLong((statupdate.getValue()).intValue());
                    mpw.writeLong((statupdate.getValue()).intValue());
                    mpw.writeLong((statupdate.getValue()).intValue());
                    break;
                case BATTLE_POINTS:
                case VIRTUE:
                    mpw.writeLong((statupdate.getValue()).longValue());
                    break;
                default:
                    mpw.writeInt((statupdate.getValue()).intValue());
            }
        }

        if ((updateMask == 0L) && (!itemReaction)) {
            mpw.write(1);
        }
        mpw.write(-1); // nMixBaseHairColor
        mpw.write(0); // nMixAddHairColor
        mpw.write(0); // nMixHairBaseProb
        mpw.write(0); // aLevelQuest
        mpw.write(0); // battleRecoveryInfo
        
        return mpw.getPacket();
    }

    public static byte[] setTemporaryStats(short str, short dex, short _int, short luk, short watk, short matk, short acc, short avoid, short speed, short jump) {
        Map<Temp, Integer> stats = new EnumMap<>(MapleStat.Temp.class);

        stats.put(MapleStat.Temp.STR, Integer.valueOf(str));
        stats.put(MapleStat.Temp.DEX, Integer.valueOf(dex));
        stats.put(MapleStat.Temp.INT, Integer.valueOf(_int));
        stats.put(MapleStat.Temp.LUK, Integer.valueOf(luk));
        stats.put(MapleStat.Temp.WATK, Integer.valueOf(watk));
        stats.put(MapleStat.Temp.MATK, Integer.valueOf(matk));
        stats.put(MapleStat.Temp.ACC, Integer.valueOf(acc));
        stats.put(MapleStat.Temp.AVOID, Integer.valueOf(avoid));
        stats.put(MapleStat.Temp.SPEED, Integer.valueOf(speed));
        stats.put(MapleStat.Temp.JUMP, Integer.valueOf(jump));

        return temporaryStats(stats);
    }

    public static byte[] temporaryStats_Aran() {
        Map<Temp, Integer> stats = new EnumMap<>(MapleStat.Temp.class);

        stats.put(MapleStat.Temp.STR, Integer.valueOf(999));
        stats.put(MapleStat.Temp.DEX, Integer.valueOf(999));
        stats.put(MapleStat.Temp.INT, Integer.valueOf(999));
        stats.put(MapleStat.Temp.LUK, Integer.valueOf(999));
        stats.put(MapleStat.Temp.WATK, Integer.valueOf(255));
        stats.put(MapleStat.Temp.ACC, Integer.valueOf(999));
        stats.put(MapleStat.Temp.AVOID, Integer.valueOf(999));
        stats.put(MapleStat.Temp.SPEED, Integer.valueOf(140));
        stats.put(MapleStat.Temp.JUMP, Integer.valueOf(120));

        return temporaryStats(stats);
    }

    public static byte[] temporaryStats_Balrog(MapleCharacter chr) {
        Map<Temp, Integer> stats = new EnumMap<>(MapleStat.Temp.class);

        int offset = 1 + (chr.getLevel() - 90) / 20;
        stats.put(MapleStat.Temp.STR, Integer.valueOf(chr.getStat().getTotalStr() / offset));
        stats.put(MapleStat.Temp.DEX, Integer.valueOf(chr.getStat().getTotalDex() / offset));
        stats.put(MapleStat.Temp.INT, Integer.valueOf(chr.getStat().getTotalInt() / offset));
        stats.put(MapleStat.Temp.LUK, Integer.valueOf(chr.getStat().getTotalLuk() / offset));
        stats.put(MapleStat.Temp.WATK, Integer.valueOf(chr.getStat().getTotalWatk() / offset));
        stats.put(MapleStat.Temp.MATK, Integer.valueOf(chr.getStat().getTotalMagic() / offset));

        return temporaryStats(stats);
    }

    public static byte[] temporaryStats(Map<MapleStat.Temp, Integer> mystats) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.TEMP_STATS);
        int updateMask = 0;
        for (MapleStat.Temp statupdate : mystats.keySet()) {
            updateMask |= statupdate.getValue();
        }
        mpw.writeInt(updateMask);
        for (final Entry<MapleStat.Temp, Integer> statupdate : mystats.entrySet()) {
            switch (statupdate.getKey()) {
                case SPEED:
                case JUMP:
                case UNKNOWN:
                    mpw.write((statupdate.getValue()).byteValue());
                    break;
                default:
                    mpw.writeShort((statupdate.getValue()).shortValue());
            }
        }

        return mpw.getPacket();
    }

    public static byte[] temporaryStats_Reset() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.TEMP_STATS_RESET);

        return mpw.getPacket();
    }

    public static byte[] updateSkills(Map<Skill, SkillEntry> update, boolean hyper) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_SKILLS);
		mpw.write(1);
        mpw.writeShort(0);//wasbyte142
        mpw.writeShort(update.size());
        for (Map.Entry z : update.entrySet()) {
            mpw.writeInt(((Skill) z.getKey()).getId());
            mpw.writeInt(((SkillEntry) z.getValue()).skillevel);
            mpw.writeInt(((SkillEntry) z.getValue()).masterlevel);
            PacketHelper.addExpirationTime(mpw, ((SkillEntry) z.getValue()).expiration);
        }
        mpw.write(/*hyper ? 0x0C : */4);
        
        return mpw.getPacket();
    }
    
    
    public static byte[] updateSkill(Map<Skill, SkillEntry> update, int skillid, int level, int masterlevel, long expiration) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_SKILLS);
		mpw.write(1);
        mpw.writeShort(0);//wasbyte142
        mpw.writeShort(update.size());
        for (Map.Entry z : update.entrySet()) {
            mpw.writeInt(((Skill) z.getKey()).getId());
            mpw.writeInt(((SkillEntry) z.getValue()).skillevel);
            mpw.writeInt(((SkillEntry) z.getValue()).masterlevel);
            PacketHelper.addExpirationTime(mpw, ((SkillEntry) z.getValue()).expiration);
        }
        mpw.write(/*hyper ? 0x0C : */4);
        
        return mpw.getPacket();
    }

    public static byte[] giveFameErrorResponse(int op) {
        return OnFameResult(op, null, true, 0);
    }

    public static byte[] OnFameResult(int op, String charname, boolean raise, int newFame) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FAME_RESPONSE);
		mpw.write(op);
        if ((op == 0) || (op == 5)) {
            mpw.writeMapleAsciiString(charname == null ? "" : charname);
            mpw.write(raise ? 1 : 0);
            if (op == 0) {
                mpw.writeInt(newFame);
            }
        }

        return mpw.getPacket();
    }

    public static byte[] fullClientDownload() {
        //Opens "http://maplestory.nexon.net/support/game-download"
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FULL_CLIENT_DOWNLOAD);

        return mpw.getPacket();
    }

    public static byte[] bombLieDetector(boolean error, int mapid, int channel) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LIE_DETECTOR);
		mpw.write(error ? 2 : 1);
        mpw.writeInt(mapid);
        mpw.writeInt(channel);

        return mpw.getPacket();
    }

    public static byte[] sendLieDetector(final byte[] image) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LIE_DETECTOR);
		mpw.write(6); // 1 = not attacking, 2 = tested, 3 = going through 

        mpw.write(4); // 2 give invalid pointer (suppose to be admin macro) 
        mpw.write(1); // the time >0 is always 1 minute 
        if (image == null) {
            mpw.writeInt(0);
            return mpw.getPacket();
        }
        mpw.writeInt(image.length);
        mpw.write(image);

        return mpw.getPacket();
    }

    public static byte[] LieDetectorResponse(final byte msg) {
        return LieDetectorResponse(msg, (byte) 0);
    }

    public static byte[] LieDetectorResponse(final byte msg, final byte msg2) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LIE_DETECTOR);
		mpw.write(msg); // 1 = not attacking, 2 = tested, 3 = going through 
        mpw.write(msg2);

        return mpw.getPacket();
    }

    public static byte[] getLieDetector(byte type, String tester) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LIE_DETECTOR); // 2A 00 01 00 00 00  
        mpw.write(type); // 1 = not attacking, 2 = tested, 3 = going through, 4 save screenshot 
        switch (type) {
            case 4: //save screen shot 
                mpw.write(0);
                mpw.writeMapleAsciiString(""); // file name 
                break;
            case 5:
                mpw.write(1); // 2 = save screen shot 
                mpw.writeMapleAsciiString(tester); // me or file name 
                break;
            case 6:
                mpw.write(4); // 2 or anything else, 2 = with maple admin picture, basicaly manager's skill? 
                mpw.write(1); // if > 0, then time = 60,000..maybe try < 0? 
                //mpw.writeInt(size);
                //mpw.write(byte); // bytes 
                break;
            case 7://send this if failed 
                // 2 = You have been appointed as a auto BOT program user and will be restrained. 
                mpw.write(4); // default 
                break;
            case 9:
                // 0 = passed lie detector test 
                // 1 = reward 5000 mesos for not botting. 
                // 2 = thank you for your cooperation with administrator. 
                mpw.write(0);
                break;
            case 8: // save screen shot.. it appears that you may be using a macro-assisted program
                mpw.write(0); // 2 or anything else , 2 = show msg, 0 = none 
                mpw.writeMapleAsciiString(""); // file name 
                break;
            case 10: // no save 
                mpw.write(0); // 2 or anything else, 2 = show msg 
                mpw.writeMapleAsciiString(""); // ?? // hi_You have passed the lie detector test 
                break;
            default:
                mpw.write(0);
                break;
        }
        return mpw.getPacket();
    }

    public static byte[] lieDetector(byte mode, byte action, byte[] image, String str1, String str2, String str3) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LIE_DETECTOR);
		mpw.write(mode);
        mpw.write(action); //2 = show msg/save screenshot/maple admin picture(mode 6)
        if (mode == 6) {
            mpw.write(1); //if true time is 60:00
            PacketHelper.addImageInfo(mpw, image);
        }
        if (mode == 7 || mode == 9) {
        }
        if (mode == 4) { //save screenshot
            mpw.writeMapleAsciiString(str1); //file name
        }
        if (mode != 5) {
            if (mode == 10) {
                mpw.writeMapleAsciiString(str2); //passed lie detector message
            } else {
                if (mode != 8) {
                }
                mpw.writeMapleAsciiString(str2); //failed lie detector, file name (for screenshot)
            }
        }
        mpw.writeMapleAsciiString(str3); //file name for screenshot

        return mpw.getPacket();
    }

    public static byte[] report(int mode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REPORT_RESPONSE);
		mpw.write(mode);
        if (mode == 2) {
            mpw.write(0);
            mpw.writeInt(1); //times left to report
        }

        return mpw.getPacket();
    }

    public static byte[] OnSetClaimSvrAvailableTime(int from, int to) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REPORT_TIME);
		mpw.write(from);
        mpw.write(to);

        return mpw.getPacket();
    }

    public static byte[] OnClaimSvrStatusChanged(boolean enable) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REPORT_STATUS);
		mpw.write(enable ? 1 : 0);

        return mpw.getPacket();
    }

    public static byte[] updateMount(MapleCharacter chr, boolean levelup) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_MOUNT);
		mpw.writeInt(chr.getID());
        mpw.writeInt(chr.getMount().getLevel());
        mpw.writeInt(chr.getMount().getExp());
        mpw.writeInt(chr.getMount().getFatigue());
        mpw.write(levelup ? 1 : 0);

        return mpw.getPacket();
    }

    public static byte[] showQuestCompletion(int id) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_QUEST_COMPLETION);
		mpw.writeShort(id);

        return mpw.getPacket();
    }

    public static byte[] useSkillBook(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.USE_SKILL_BOOK);
		mpw.write(0);
        mpw.writeInt(chr.getID());
        mpw.write(1);
        mpw.writeInt(skillid);
        mpw.writeInt(maxlevel);
        mpw.write(canuse ? 1 : 0);
        mpw.write(success ? 1 : 0);

        return mpw.getPacket();
    }

    public static byte[] useAPSPReset(boolean spReset, int cid) {
        MaplePacketWriter mpw = new MaplePacketWriter(spReset ? SendPacketOpcode.SP_RESET : SendPacketOpcode.AP_RESET);
		mpw.write(1);
        mpw.writeInt(cid);
        mpw.write(1);

        return mpw.getPacket();
    }

    public static byte[] expandCharacterSlots(int mode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EXPAND_CHARACTER_SLOTS);
		mpw.writeInt(mode);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] finishedGather(int type) {
        return gatherSortItem(true, type);
    }

    public static byte[] finishedSort(int type) {
        return gatherSortItem(false, type);
    }

    public static byte[] gatherSortItem(boolean gather, int type) {
        MaplePacketWriter mpw = new MaplePacketWriter(gather ? SendPacketOpcode.FINISH_GATHER : SendPacketOpcode.FINISH_SORT);
		mpw.write(1);
        mpw.write(type);

        return mpw.getPacket();
    }

    public static byte[] updateExpPotion(int mode, int id, int itemId, boolean firstTime, int level, int potionDstLevel) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EXP_POTION);
		mpw.write(mode);
        mpw.write(1); //bool for get_update_time
        mpw.writeInt(id);
        if (id != 0) {
            mpw.write(1); //not even being read how rude of nexon
            if (mode == 1) {
                mpw.writeInt(0);
            }
            if (mode == 2) {
                mpw.write(firstTime ? 1 : 0); //1 on first time then it turns 0
                mpw.writeInt(itemId);
                if (itemId != 0) {
                    mpw.writeInt(level); //level, confirmed
                    mpw.writeInt(potionDstLevel); //max level with potion
                    mpw.writeLong(384); //random, more like potion id
                }
            }
        }

        return mpw.getPacket();
    }

    public static byte[] updateGender(MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_GENDER);
		mpw.write(chr.getGender());

        return mpw.getPacket();
    }

    public static byte[] charInfo(MapleCharacter chr, boolean isSelf) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CHAR_INFO);
		mpw.writeInt(chr.getID());
        mpw.write(chr.getLevel());
        mpw.writeShort(chr.getJob());
        mpw.writeShort(chr.getSubcategory());
        mpw.write(chr.getStat().pvpRank);
        mpw.writeInt(chr.getFame());
        MapleMarriage marriage = chr.getMarriage();
        mpw.write(marriage != null && marriage.getId() != 0);
        if (marriage != null && marriage.getId() != 0) {
            mpw.writeInt(marriage.getId()); //marriage id
            mpw.writeInt(marriage.getHusbandId()); //husband char id
            mpw.writeInt(marriage.getWifeId()); //wife char id
            mpw.writeShort(3); //msg type
            mpw.writeInt(chr.getMarriageItemId()); //ring id husband
            mpw.writeInt(chr.getMarriageItemId()); //ring id wife
            mpw.writeAsciiString(marriage.getHusbandName(), 13); //husband name
            mpw.writeAsciiString(marriage.getWifeName(), 13); //wife name
        }
        List prof = chr.getProfessions();
        mpw.write(prof.size());
        for (Iterator i$ = prof.iterator(); i$.hasNext();) {
            int i = ((Integer) i$.next()).intValue();
            mpw.writeShort(i);
        }
        if (chr.getGuildId() <= 0) {
            mpw.writeMapleAsciiString("-");
            mpw.writeMapleAsciiString("");
        } else {
            MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mpw.writeMapleAsciiString(gs.getName());
                if (gs.getAllianceId() > 0) {
                    MapleGuildAlliance allianceName = World.Alliance.getAlliance(gs.getAllianceId());
                    if (allianceName != null) {
                        mpw.writeMapleAsciiString(allianceName.getName());
                    } else {
                        mpw.writeMapleAsciiString("");
                    }
                } else {
                    mpw.writeMapleAsciiString("");
                }
            } else {
                mpw.writeMapleAsciiString("-");
                mpw.writeMapleAsciiString("");
            }
        }

        mpw.write(isSelf ? 1 : 0);
        mpw.write(0);


        byte index = 1;
        for (MaplePet pet : chr.getSummonedPets()) {
            if (index == 1) {   // please test if this doesn't d/c when viewing multipets
                mpw.write(index);
            }  
            mpw.writeInt(pet.getPetItemId());
            mpw.writeMapleAsciiString(pet.getName());
            mpw.write(pet.getLevel());
            mpw.writeShort(pet.getCloseness());
            mpw.write(pet.getFullness());
            mpw.writeShort(0);
            Item inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (byte) (index == 2 ? -130 : index == 1 ? -114 : -138));
            mpw.writeInt(inv == null ? 0 : inv.getItemId());
            mpw.writeInt(-1);//new v140
            mpw.write(chr.getSummonedPets().size() > index); //continue loop
            index++;
        }
        if (index == 1) { //index no change means no pets
            mpw.write(0);
        }
        /*if ((chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) && (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -19) != null)) {
         MapleMount mount = chr.getMount();
         mpw.write(1);
         mpw.writeInt(mount.getLevel());
         mpw.writeInt(mount.getExp());
         mpw.writeInt(mount.getFatigue());
         } else {
         mpw.write(0);
         }*/
        int wishlistSize = chr.getWishlistSize();
        mpw.write(wishlistSize);
        if (wishlistSize > 0) {
            int[] wishlist = chr.getWishlist();
            for (int x = 0; x < wishlistSize; x++) {
                mpw.writeInt(wishlist[x]);
            }
        }
        Item medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -46);
        mpw.writeInt(medal == null ? 0 : medal.getItemId());
        List<Pair<Integer, Long>> medalQuests = chr.getCompletedMedals();
        mpw.writeShort(medalQuests.size());
        for (Pair x : medalQuests) {
            mpw.writeShort(((Integer) x.left).intValue());
            mpw.writeLong(((Long) x.right).longValue());
        }
        for (MapleTrait.MapleTraitType t : MapleTrait.MapleTraitType.values()) {
            mpw.write(chr.getTrait(t).getLevel());
        }

        mpw.writeInt(0); //farm id?
        PacketHelper.addFarmInfo(mpw, chr.getClient(), 0);

        mpw.writeInt(0);
        mpw.writeInt(0);

        List chairs = new ArrayList();
        for (Item i : chr.getInventory(MapleInventoryType.SETUP).newList()) {
            if ((i.getItemId() / 10000 == 301) && (!chairs.contains(Integer.valueOf(i.getItemId())))) {
                chairs.add(Integer.valueOf(i.getItemId()));
            }
        }
        mpw.writeInt(chairs.size());
        for (Iterator i$ = chairs.iterator(); i$.hasNext();) {
            int i = ((Integer) i$.next()).intValue();
            mpw.writeInt(i);
        }

        return mpw.getPacket();
    }

    public static byte[] getMonsterBookInfo(MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BOOK_INFO);
		mpw.writeInt(chr.getID());
        mpw.writeInt(chr.getLevel());
        chr.getMonsterBook().writeCharInfoPacket(mpw);

        return mpw.getPacket();
    }

    public static byte[] spawnPortal(int townId, int targetId, int skillId, Point pos) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_PORTAL);
		mpw.writeInt(townId);
        mpw.writeInt(targetId);
        if ((townId != 999999999) && (targetId != 999999999)) {
            mpw.writeInt(skillId);
            mpw.writePos(pos);
        }

        return mpw.getPacket();
    }

    public static byte[] mechPortal(Point pos) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MECH_PORTAL);
		mpw.writePos(pos);

        return mpw.getPacket();
    }

    public static byte[] echoMegaphone(String name, String message) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ECHO_MESSAGE);
		mpw.write(0);
        mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mpw.writeMapleAsciiString(name);
        mpw.writeMapleAsciiString(message);

        return mpw.getPacket();
    }

    public static byte[] showQuestMsg(String msg) {
        return broadcastMsg(5, msg);
    }

    public static byte[] Mulung_Pts(int recv, int total) {
        return showQuestMsg(new StringBuilder().append("You have received ").append(recv).append(" training points, for the accumulated total of ").append(total).append(" training points.").toString());
    }

    public static byte[] broadcastMsg(String message) {
        return broadcastMessage(4, 0, message, false);
    }

    public static byte[] broadcastMsg(int type, String message) {
        return broadcastMessage(type, 0, message, false);
    }

    public static byte[] broadcastMsg(int type, int channel, String message) {
        return broadcastMessage(type, channel, message, false);
    }

    public static byte[] broadcastMsg(int type, int channel, String message, boolean smegaEar) {
        return broadcastMessage(type, channel, message, smegaEar);
    }

    private static byte[] broadcastMessage(int type, int channel, String message, boolean megaEar) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SERVERMESSAGE);
		mpw.write(type);
        if (type == 4) {
            mpw.write(1);
        }
        if ((type != 23) && (type != 24)) {
            mpw.writeMapleAsciiString(message);
        }
        switch (type) {
            case 3:
            case 22:
            case 25:
            case 26:
                mpw.write(channel - 1);
                mpw.write(megaEar ? 1 : 0);
                break;
            case 9:
                mpw.write(channel - 1);
                break;
            case 12:
                mpw.writeInt(channel);
                break;
            case 6:
            case 11:
            case 20:
                mpw.writeInt((channel >= 1000000) && (channel < 6000000) ? channel : 0);
                break;
            /*case 24:
                mpw.writeShort(0);
                break;*/
            case 4:
            case 5:
            case 7:
            case 8:
            case 10:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 21:
            case 23:
                break;
        }
        return mpw.getPacket();
    }

  public static byte[] getGachaponMega(String name, String message, Item item, byte rareness, String gacha) {
    return getGachaponMega(name, message, item, rareness, false, gacha);
  }

  public static byte[] getGachaponMega(String name, String message, Item item, byte rareness, boolean dragon, String gacha) {
    MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SERVERMESSAGE);
		mpw.write(13);
    mpw.writeMapleAsciiString(new StringBuilder().append(name).append(message).toString());
    if (!dragon) {
      mpw.writeInt(0);
      mpw.writeInt(item.getItemId());
    }
    mpw.writeMapleAsciiString(gacha);
    PacketHelper.addItemInfo(mpw, item);

    return mpw.getPacket();
  }

    public static byte[] getEventEnvelope(int questID, int time) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SERVERMESSAGE);
		mpw.write(23);
        mpw.writeShort(questID);
        mpw.writeInt(time);

        return mpw.getPacket();
    }

    public static byte[] tripleSmega(List<String> message, boolean ear, int channel) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SERVERMESSAGE);
		mpw.write(10);
        if (message.get(0) != null) {
            mpw.writeMapleAsciiString((String) message.get(0));
        }
        mpw.write(message.size());
        for (int i = 1; i < message.size(); i++) {
            if (message.get(i) != null) {
                mpw.writeMapleAsciiString((String) message.get(i));
            }
        }
        mpw.write(channel - 1);
        mpw.write(ear ? 1 : 0);

        return mpw.getPacket();
    }

    public static byte[] itemMegaphone(String msg, boolean whisper, int channel, Item item) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SERVERMESSAGE);
		mpw.write(9);
        mpw.writeMapleAsciiString(msg);
        mpw.write(channel - 1);
        mpw.write(whisper ? 1 : 0);
        PacketHelper.addItemPosition(mpw, item, true, false);
        if (item != null) {
            PacketHelper.addItemInfo(mpw, item);
        }

        return mpw.getPacket();
    }

    public static byte[] getPeanutResult(int itemId, short quantity, int itemId2, short quantity2, int ourItem) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PIGMI_REWARD);
		mpw.writeInt(itemId);
        mpw.writeShort(quantity);
        mpw.writeInt(ourItem);
        mpw.writeInt(itemId2);
        mpw.writeInt(quantity2);
        mpw.write(0);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] getOwlOpen() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OWL_OF_MINERVA);
		mpw.write(9);
        mpw.write(GameConstants.owlItems.length);
        for (int i : GameConstants.owlItems) {
            mpw.writeInt(i);
        }

        return mpw.getPacket();
    }

    public static byte[] getOwlSearched(int itemSearch, List<HiredMerchant> hms) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OWL_OF_MINERVA);
		mpw.write(8);
        mpw.writeInt(0);
        mpw.writeInt(itemSearch);
        int size = 0;

        for (HiredMerchant hm : hms) {
            size += hm.searchItem(itemSearch).size();
        }

        mpw.writeInt(size);
        for (HiredMerchant hm : hms) {
            for (Iterator<HiredMerchant> i = hms.iterator(); i.hasNext();) {
                hm = (HiredMerchant) i.next();
                final List<MaplePlayerShopItem> items = hm.searchItem(itemSearch);
                for (MaplePlayerShopItem item : items) {
                    mpw.writeMapleAsciiString(hm.getOwnerName());
                    mpw.writeInt(hm.getMap().getId());
                    mpw.writeMapleAsciiString(hm.getDescription());
                    mpw.writeInt(item.item.getQuantity());
                    mpw.writeInt(item.bundles);
                    mpw.writeInt(item.price);
                    switch (2) {
                        case 0:
                            mpw.writeInt(hm.getOwnerId());
                            break;
                        case 1:
                            mpw.writeInt(hm.getStoreId());
                            break;
                        default:
                            mpw.writeInt(hm.getObjectId());
                    }

                    mpw.write(hm.getFreeSlot() == -1 ? 1 : 0);
                    mpw.write(GameConstants.getInventoryType(itemSearch).getType());
                    if (GameConstants.getInventoryType(itemSearch) == MapleInventoryType.EQUIP) {
                        PacketHelper.addItemInfo(mpw, item.item);
                    }
                }
            }
        }
        return mpw.getPacket();
    }

    public static byte[] getOwlMessage(int msg) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OWL_RESULT);
		mpw.write(msg);

        return mpw.getPacket();
    }

    public static byte[] sendEngagementRequest(String name, int cid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ENGAGE_REQUEST);
		mpw.write(0);
        mpw.writeMapleAsciiString(name);
        mpw.writeInt(cid);

        return mpw.getPacket();
    }

    public static byte[] sendEngagement(byte msg, int item, MapleCharacter male, MapleCharacter female) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ENGAGE_RESULT);
		mpw.write(msg);
        if (msg == 9 || msg >= 11 && msg <= 14) {
            mpw.writeInt(0);
            mpw.writeInt(male.getID());
            mpw.writeInt(female.getID());
            mpw.writeShort(1);
            mpw.writeInt(item);
            mpw.writeInt(item);
            mpw.writeAsciiString(male.getName(), 13);
            mpw.writeAsciiString(female.getName(), 13);
        } else if (msg == 10 || msg >= 15 && msg <= 16) {
            mpw.writeAsciiString("Male", 13);
            mpw.writeAsciiString("Female", 13);
            mpw.writeShort(0);
        }

        return mpw.getPacket();
    }

    public static byte[] sendWeddingGive() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WEDDING_GIFT);
		mpw.write(9);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] sendWeddingReceive() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WEDDING_GIFT);
		mpw.write(10);
        mpw.writeLong(-1L);
        mpw.writeInt(0);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] giveWeddingItem() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WEDDING_GIFT);
		mpw.write(11);
        mpw.write(0);
        mpw.writeLong(0L);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] receiveWeddingItem() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WEDDING_GIFT);
		mpw.write(15);
        mpw.writeLong(0L);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] sendCashPetFood(boolean success, byte index) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.USE_CASH_PET_FOOD);
		mpw.write(success ? 0 : 1);
        if (success) {
            mpw.write(index);
        }

        return mpw.getPacket();
    }

    public static byte[] yellowChat(String msg) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.YELLOW_CHAT);
		mpw.write(-1);
        mpw.writeMapleAsciiString(msg);

        return mpw.getPacket();
    }

    public static byte[] shopDiscount(int percent) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOP_DISCOUNT);
		mpw.write(percent);

        return mpw.getPacket();
    }

    public static byte[] catchMob(int mobid, int itemid, byte success) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CATCH_MOB);
		mpw.write(success);
        mpw.writeInt(itemid);
        mpw.writeInt(mobid);

        return mpw.getPacket();
    }

    public static byte[] spawnPlayerNPC(PlayerNPC npc) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_NPC);
		mpw.write(1);
        mpw.writeInt(npc.getId());
        mpw.writeMapleAsciiString(npc.getName());
        PacketHelper.addCharLook(mpw, npc, true, false);

        return mpw.getPacket();
    }

    public static byte[] disabledNPC(List<Integer> ids) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DISABLE_NPC);
		mpw.write(ids.size());
        for (Integer i : ids) {
            mpw.writeInt(i.intValue());
        }

        return mpw.getPacket();
    }

    public static byte[] getCard(int cardid, int cardCount) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_BOOK_SET_CARD);
		mpw.write(cardid > 0 ? 1 : 0);
        if (cardid > 0) {
            mpw.writeInt(cardid);
            mpw.writeInt(cardCount);
        }
        return mpw.getPacket();
    }

    public static byte[] changeCardSet(int cardid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_BOOK_SET_COVER);
		mpw.writeInt(cardid);

        return mpw.getPacket();
    }

    public static byte[] upgradeBook(Item book, MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BOOK_STATS);
		mpw.writeInt(book.getPosition());
        PacketHelper.addItemInfo(mpw, book, chr);

        return mpw.getPacket();
    }

    public static byte[] getCardDrops(int cardid, List<Integer> drops) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CARD_DROPS);
		mpw.writeInt(cardid);
        mpw.writeShort(drops == null ? 0 : drops.size());
        if (drops != null) {
            for (Integer de : drops) {
                mpw.writeInt(de.intValue());
            }
        }

        return mpw.getPacket();
    }

    public static byte[] getFamiliarInfo(MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FAMILIAR_INFO);
		mpw.writeInt(chr.getFamiliars().size());
        for (MonsterFamiliar mf : chr.getFamiliars().values()) {
            mf.writeRegisterPacket(mpw, true);
        }
        List<Pair<Integer, Long>> size = new ArrayList<>();
        for (Item i : chr.getInventory(MapleInventoryType.USE).list()) {
            if (i.getItemId() / 10000 == 287) {
                StructFamiliar f = MapleItemInformationProvider.getInstance().getFamiliarByItem(i.getItemId());
                if (f != null) {
                    size.add(new Pair<>(f.familiar, i.getInventoryId()));
                }
            }
        }
        mpw.writeInt(size.size());
        for (Pair<?, ?> s : size) {
            mpw.writeInt(chr.getID());
            mpw.writeInt(((Integer) s.left));
            mpw.writeLong(((Long) s.right));
            mpw.write(0);
        }
        size.clear();

        return mpw.getPacket();
    }

    public static byte[] updateWebBoard(boolean result) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WEB_BOARD_UPDATE);
		mpw.writeBoolean(result);

        return mpw.getPacket();
    }

    public static byte[] MulungEnergy(int energy) {
        return sendPyramidEnergy("energy", String.valueOf(energy));
    }

    public static byte[] sendPyramidEnergy(String type, String amount) {
        return sendString(1, type, amount);
    }

    public static byte[] sendGhostPoint(String type, String amount) {
        return sendString(2, type, amount);
    }

    public static byte[] sendGhostStatus(String type, String amount) {
        return sendString(3, type, amount);
    }

    public static byte[] sendString(int type, String object, String amount) {
        MaplePacketWriter mpw = null;

        switch (type) {
            case 1:
            	mpw = new MaplePacketWriter(SendPacketOpcode.SESSION_VALUE);
                break;
            case 2:
            	mpw = new MaplePacketWriter(SendPacketOpcode.PARTY_VALUE);
                break;
            case 3:
            	mpw = new MaplePacketWriter(SendPacketOpcode.MAP_VALUE);
        }

        mpw.writeMapleAsciiString(object);
        mpw.writeMapleAsciiString(amount);

        return mpw.getPacket();
    }

    public static byte[] fairyPendantMessage(int termStart, int incExpR) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BONUS_EXP);
		mpw.writeInt(17);
        mpw.writeInt(0);

        mpw.writeInt(incExpR);

        return mpw.getPacket();
    }

    public static byte[] potionDiscountMessage(int type, int potionDiscR) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.POTION_BONUS);
		mpw.writeInt(type);
        mpw.writeInt(potionDiscR);

        return mpw.getPacket();
    }

    public static byte[] sendLevelup(boolean family, int level, String name) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LEVEL_UPDATE);
		mpw.write(family ? 1 : 2);
        mpw.writeInt(level);
        mpw.writeMapleAsciiString(name);

        return mpw.getPacket();
    }

    public static byte[] sendMarriage(boolean family, String name) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MARRIAGE_UPDATE);
		mpw.write(family ? 1 : 0);
        mpw.writeMapleAsciiString(name);

        return mpw.getPacket();
    }

    //mark packet
     public static byte[] giveMarkOfTheif(int cid, int oid, int skillid, List<MapleMonster> monsters, Point p1, Point p2, int javelin) {
        MaplePacketWriter packet = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
        packet.write(1);
        packet.writeInt(cid);
        packet.writeInt(oid);
        packet.writeInt(11); //type
        packet.write(1);
        packet.writeInt(monsters.size());
        for (MapleMonster monster : monsters) {
            packet.writeInt(monster.getObjectId());
        }
        packet.writeInt(skillid); //skillid
        for (int i = 0; i < monsters.size(); i++) {
            packet.write(1);
            packet.writeInt(i + 2);
            packet.writeInt(1);
            packet.writeInt(Randomizer.rand(0x2A, 0x2B));
            packet.writeInt(Randomizer.rand(0x03, 0x04));
            packet.writeInt(Randomizer.rand(0x43, 0xF5));
            packet.writeInt(200);
            packet.writeLong(0);
            packet.writeInt(Randomizer.nextInt());
            packet.writeInt(0);
        }
        packet.write(0);
        //for (Point p : pos) {
        packet.writeInt(p1.x);
        packet.writeInt(p1.y);
        packet.writeInt(p2.x);
        packet.writeInt(p2.y);
        //}
        packet.writeInt(javelin);
        //System.out.println(packet.toString());
        
         packet.writeZeroBytes(69); //We might need this =p
        return packet.getPacket();
    }
    
    //
    
    
    public static byte[] sendJobup(boolean family, int jobid, String name) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.JOB_UPDATE);
		mpw.write(family ? 1 : 0);
        mpw.writeInt(jobid);
        mpw.writeMapleAsciiString(new StringBuilder().append(!family ? "> " : "").append(name).toString());

        return mpw.getPacket();
    }

    public static byte[] getAvatarMega(MapleCharacter chr, int channel, int itemId, List<String> text, boolean ear) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.AVATAR_MEGA);
		mpw.writeInt(itemId);
        mpw.writeMapleAsciiString(chr.getName());
        for (String i : text) {
            mpw.writeMapleAsciiString(i);
        }
        mpw.writeInt(channel - 1);
        mpw.write(ear ? 1 : 0);
        PacketHelper.addCharLook(mpw, chr, true, false);

        return mpw.getPacket();
    }

    public static byte[] GMPoliceMessage(boolean dc) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GM_POLICE);
		mpw.write(dc ? 10 : 0);

        return mpw.getPacket();
    }

    public static byte[] GMPoliceMessage(String msg) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MAPLE_ADMIN_MSG);
		mpw.writeMapleAsciiString(msg);

        return mpw.getPacket();
    }

    public static byte[] pendantSlot(boolean p) { //slot -59
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SLOT_UPDATE);
		mpw.write(p ? 1 : 0);
        
        return mpw.getPacket();
    }

    public static byte[] followRequest(int chrid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FOLLOW_REQUEST);
		mpw.writeInt(chrid);

        return mpw.getPacket();
    }

    public static byte[] getTopMsg(String msg) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.TOP_MSG);
		mpw.writeMapleAsciiString(msg);

        return mpw.getPacket();
    }

    public static byte[] showMidMsg(String s, int l) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MID_MSG);
		mpw.write(l);
        mpw.writeMapleAsciiString(s);
        mpw.write(s.length() > 0 ? 0 : 1);

        return mpw.getPacket();
    }

    public static byte[] getMidMsg(String msg, boolean keep, int index) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MID_MSG);
		mpw.write(index);
        mpw.writeMapleAsciiString(msg);
        mpw.write(keep ? 0 : 1);

        return mpw.getPacket();
    }

    public static byte[] clearMidMsg() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CLEAR_MID_MSG);

        return mpw.getPacket();
    }

    public static byte[] getSpecialMsg(String msg, int type, boolean show) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPECIAL_MSG);
		mpw.writeMapleAsciiString(msg);
        mpw.writeInt(type);
        mpw.writeInt(show ? 0 : 1);

        return mpw.getPacket();
    }
    
    public static byte[] getSpecialMsg(String msg, int type, int duration, boolean show) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPECIAL_MSG);
		mpw.writeMapleAsciiString(msg);
        mpw.writeInt(type);
        mpw.writeInt(duration);
        mpw.writeInt(show ? 0 : 1);

        return mpw.getPacket();
    }

    public static byte[] CakePieMsg() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CAKE_VS_PIE_MSG);

        return mpw.getPacket();
    }
    
    public static byte[] gmBoard(int increnement, String url) { //Test if it DOES work with the open_UI packet.
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_UI);
		mpw.write(SendPacketOpcode.GM_STORY_BOARD.getOpcode());//was 0x1D
        mpw.writeShort(1234); //random token        
        mpw.writeShort(Randomizer.nextInt(Short.MAX_VALUE)); //random token        
        mpw.writeMapleAsciiString(url);        
        return mpw.getPacket();    
    }  

    public static byte[] gmBoard2(int increnement, String url) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GM_STORY_BOARD);
		mpw.writeInt(increnement); //Increnement number
        mpw.writeMapleAsciiString(url);

        return mpw.getPacket();
    }

    public static byte[] updateJaguar(MapleCharacter from) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WILD_HUNTER_INFO);
        PacketHelper.addJaguarInfo(mpw, from);

        return mpw.getPacket();
    }

    public static byte[] loadInformation(byte mode, int location, int birthday, int favoriteAction, int favoriteLocation, boolean success) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.YOUR_INFORMATION);
		mpw.write(mode);
        if (mode == 2) {
            mpw.writeInt(location);
            mpw.writeInt(birthday);
            mpw.writeInt(favoriteAction);
            mpw.writeInt(favoriteLocation);
        } else if (mode == 4) {
            mpw.write(success ? 1 : 0);
        }

        return mpw.getPacket();
    }

    public static byte[] saveInformation(boolean fail) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.YOUR_INFORMATION);
		mpw.write(4);
        mpw.write(fail ? 0 : 1);

        return mpw.getPacket();
    }

    public static byte[] myInfoResult() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FIND_FRIEND);
		mpw.write(6);
        mpw.writeInt(0);
        mpw.writeInt(0);

        return mpw.getPacket();
    }

    public static byte[] findFriendResult(byte mode, List<MapleCharacter> friends, int error, MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FIND_FRIEND);
		mpw.write(mode);
        switch (mode) {
            case 6:
                mpw.writeInt(0);
                mpw.writeInt(0);
                break;
            case 8:
                mpw.writeShort(friends.size());
                for (MapleCharacter mc : friends) {
                    mpw.writeInt(mc.getID());
                    mpw.writeMapleAsciiString(mc.getName());
                    mpw.write(mc.getLevel());
                    mpw.writeShort(mc.getJob());
                    mpw.writeInt(0);
                    mpw.writeInt(0);
                }
                break;
            case 9:
                mpw.write(error);
                break;
            case 11:
                mpw.writeInt(chr.getID());
                PacketHelper.addCharLook(mpw, chr, true, false);
                break;
        }

        return mpw.getPacket();
    }

    public static byte[] showBackgroundEffect(String eff, int value) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.VISITOR);
		mpw.writeMapleAsciiString(eff);
        mpw.write(value);

        return mpw.getPacket();
    }

    public static byte[] sendPinkBeanChoco() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PINKBEAN_CHOCO);
		mpw.writeInt(0);
        mpw.write(1);
        mpw.write(0);
        mpw.write(0);
        mpw.writeInt(0);

        return mpw.getPacket();
    }

    public static byte[] changeChannelMsg(int channel, String msg) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.AUTO_CC_MSG);
		mpw.writeInt(channel);
        mpw.writeMapleAsciiString(msg);

        return mpw.getPacket();
    }

    public static byte[] pamSongUI() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PAM_SONG);
        return mpw.getPacket();
    }

    public static byte[] ultimateExplorer() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ULTIMATE_EXPLORER);

        return mpw.getPacket();
    }

    public static byte[] professionInfo(String skil, int level1, int level2, int chance) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPECIAL_STAT);
		mpw.writeMapleAsciiString(skil);
        mpw.writeInt(level1);
        mpw.writeInt(level2);
        mpw.write(1);
        mpw.writeInt((skil.startsWith("9200")) || (skil.startsWith("9201")) ? 100 : chance);

        return mpw.getPacket();
    }

    public static byte[] updateAzwanFame(int level, int fame, boolean levelup) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_HONOUR);
		mpw.writeInt(level);
        mpw.writeInt(fame);
        mpw.write(levelup ? 1 : 0);

        return mpw.getPacket();
    }

    public static byte[] showAzwanKilled() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.AZWAN_KILLED);

        return mpw.getPacket();
    }

    public static byte[] showSilentCrusadeMsg(byte type, short chapter) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SILENT_CRUSADE_MSG);
		mpw.write(type);
        mpw.writeShort(chapter - 1);

        /* type:
         * 0 - open ui (short is chapter)
         * 2 - not enough inventory space
         * 3 - failed due to unknown error
         */
        return mpw.getPacket();
    }

    public static byte[] getSilentCrusadeMsg(byte type) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SILENT_CRUSADE_SHOP);
		mpw.write(type);

        return mpw.getPacket();
    }

    public static byte[] showSCShopMsg(byte type) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SILENT_CRUSADE_SHOP);
		mpw.write(type);

        return mpw.getPacket();
    }

    public static byte[] updateImpTime() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_IMP_TIME);
		mpw.writeInt(0);
        mpw.writeLong(0L);

        return mpw.getPacket();
    }

    public static byte[] updateImp(MapleImp imp, int mask, int index, boolean login) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ITEM_POT);
		mpw.write(login ? 0 : 1);
        mpw.writeInt(index + 1);
        mpw.writeInt(mask);
        if ((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) {
            Pair<?, ?> i = MapleItemInformationProvider.getInstance().getPot(imp.getItemId());
            if (i == null) {
                return enableActions();
            }
            mpw.writeInt(((Integer) i.left).intValue());
            mpw.write(imp.getLevel());
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.STATE.getValue()) != 0)) {
            mpw.write(imp.getState());
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.FULLNESS.getValue()) != 0)) {
            mpw.writeInt(imp.getFullness());
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.CLOSENESS.getValue()) != 0)) {
            mpw.writeInt(imp.getCloseness());
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.CLOSENESS_LEFT.getValue()) != 0)) {
            mpw.writeInt(1);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.MINUTES_LEFT.getValue()) != 0)) {
            mpw.writeInt(0);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.LEVEL.getValue()) != 0)) {
            mpw.write(1);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.FULLNESS_2.getValue()) != 0)) {
            mpw.writeInt(imp.getFullness());
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.UPDATE_TIME.getValue()) != 0)) {
            mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.CREATE_TIME.getValue()) != 0)) {
            mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.AWAKE_TIME.getValue()) != 0)) {
            mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.SLEEP_TIME.getValue()) != 0)) {
            mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.MAX_CLOSENESS.getValue()) != 0)) {
            mpw.writeInt(100);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.MAX_DELAY.getValue()) != 0)) {
            mpw.writeInt(1000);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.MAX_FULLNESS.getValue()) != 0)) {
            mpw.writeInt(1000);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.MAX_ALIVE.getValue()) != 0)) {
            mpw.writeInt(1);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.MAX_MINUTES.getValue()) != 0)) {
            mpw.writeInt(10);
        }
        mpw.write(0);

        return mpw.getPacket();
    }

//    public static byte[] getMulungRanking(MapleClient c, List<DojoRankingInfo> all) {
//        final MaplePacketLittleEndianWriter mpw = new MaplePacketLittleEndianWriter(SendPacketOpcode.MULUNG_DOJO_RANKING);
//        MapleDojoRanking data = MapleDojoRanking.getInstance();
//        mpw.writeInt(all.size()); // size
//        for (DojoRankingInfo info : all) {
//            mpw.writeShort(info.getRank());
//            mpw.writeMapleAsciiString(info.getName());
//            mpw.writeLong(info.getTime());
//        }
//        return mpw.getPacket();
//    }
    public static byte[] getMulungRanking() {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MULUNG_DOJO_RANKING);
        DojoRankingsData data = DojoRankingsData.loadLeaderboard();
        mpw.writeInt(data.totalCharacters); // size
        for (int i = 0; i < data.totalCharacters; i++) {
            mpw.writeShort(data.ranks[i]); // rank
            mpw.writeMapleAsciiString(data.names[i]); // Character name
            mpw.writeLong(data.times[i]); // time in seconds
        }
        return mpw.getPacket();
    }

    public static byte[] getMulungMessage(boolean dc, String msg) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MULUNG_MESSAGE);
		mpw.write(dc ? 1 : 0);
        mpw.writeMapleAsciiString(msg);

        return mpw.getPacket();
    }

    public static byte[] getCandyRanking(MapleClient c, List<CandyRankingInfo> all) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANDY_RANKING);
		mpw.writeInt(all.size());
        for (CandyRankingInfo info : all) {
            mpw.writeShort(info.getRank());
            mpw.writeMapleAsciiString(info.getName());
        }
        return mpw.getPacket();
    }

    public static class AlliancePacket {

        public static byte[] getAllianceInfo(MapleGuildAlliance alliance) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(12);
            mpw.write(alliance == null ? 0 : 1);
            if (alliance != null) {
                addAllianceInfo(mpw, alliance);
            }

            return mpw.getPacket();
        }

        private static void addAllianceInfo(MaplePacketWriter mpw, MapleGuildAlliance alliance) {
            mpw.writeInt(alliance.getId());
            mpw.writeMapleAsciiString(alliance.getName());
            for (int i = 1; i <= 5; i++) {
                mpw.writeMapleAsciiString(alliance.getRank(i));
            }
            mpw.write(alliance.getNoGuilds());
            for (int i = 0; i < alliance.getNoGuilds(); i++) {
                mpw.writeInt(alliance.getGuildId(i));
            }
            mpw.writeInt(alliance.getCapacity());
            mpw.writeMapleAsciiString(alliance.getNotice());
        }

        public static byte[] getGuildAlliance(MapleGuildAlliance alliance) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(13);
            if (alliance == null) {
                mpw.writeInt(0);
                return mpw.getPacket();
            }
            int noGuilds = alliance.getNoGuilds();
            MapleGuild[] g = new MapleGuild[noGuilds];
            for (int i = 0; i < alliance.getNoGuilds(); i++) {
                g[i] = World.Guild.getGuild(alliance.getGuildId(i));
                if (g[i] == null) {
                    return CWvsContext.enableActions();
                }
            }
            mpw.writeInt(noGuilds);
            for (MapleGuild gg : g) {
                CWvsContext.GuildPacket.getGuildInfo(mpw, gg);
            }
            return mpw.getPacket();
        }

        public static byte[] allianceMemberOnline(int alliance, int gid, int id, boolean online) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(14);
            mpw.writeInt(alliance);
            mpw.writeInt(gid);
            mpw.writeInt(id);
            mpw.write(online ? 1 : 0);

            return mpw.getPacket();
        }

        public static byte[] removeGuildFromAlliance(MapleGuildAlliance alliance, MapleGuild expelledGuild, boolean expelled) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(16);
            addAllianceInfo(mpw, alliance);
            CWvsContext.GuildPacket.getGuildInfo(mpw, expelledGuild);
            mpw.write(expelled ? 1 : 0);

            return mpw.getPacket();
        }

        public static byte[] addGuildToAlliance(MapleGuildAlliance alliance, MapleGuild newGuild) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(18);
            addAllianceInfo(mpw, alliance);
            mpw.writeInt(newGuild.getId());
            CWvsContext.GuildPacket.getGuildInfo(mpw, newGuild);
            mpw.write(0);

            return mpw.getPacket();
        }

        public static byte[] sendAllianceInvite(String allianceName, MapleCharacter inviter) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(3);
            mpw.writeInt(inviter.getGuildId());
            mpw.writeMapleAsciiString(inviter.getName());
            mpw.writeMapleAsciiString(allianceName);

            return mpw.getPacket();
        }

        public static byte[] getAllianceUpdate(MapleGuildAlliance alliance) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(23);
            addAllianceInfo(mpw, alliance);

            return mpw.getPacket();
        }

        public static byte[] createGuildAlliance(MapleGuildAlliance alliance) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(15);
            addAllianceInfo(mpw, alliance);
            int noGuilds = alliance.getNoGuilds();
            MapleGuild[] g = new MapleGuild[noGuilds];
            for (int i = 0; i < alliance.getNoGuilds(); i++) {
                g[i] = World.Guild.getGuild(alliance.getGuildId(i));
                if (g[i] == null) {
                    return CWvsContext.enableActions();
                }
            }
            for (MapleGuild gg : g) {
                CWvsContext.GuildPacket.getGuildInfo(mpw, gg);
            }
            return mpw.getPacket();
        }

        public static byte[] updateAlliance(MapleGuildCharacter mgc, int allianceid) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(24);
            mpw.writeInt(allianceid);
            mpw.writeInt(mgc.getGuildId());
            mpw.writeInt(mgc.getId());
            mpw.writeInt(mgc.getLevel());
            mpw.writeInt(mgc.getJobId());

            return mpw.getPacket();
        }

        public static byte[] updateAllianceLeader(int allianceid, int newLeader, int oldLeader) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(25);
            mpw.writeInt(allianceid);
            mpw.writeInt(oldLeader);
            mpw.writeInt(newLeader);

            return mpw.getPacket();
        }

        public static byte[] allianceRankChange(int aid, String[] ranks) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(26);
            mpw.writeInt(aid);
            for (String r : ranks) {
                mpw.writeMapleAsciiString(r);
            }

            return mpw.getPacket();
        }

        public static byte[] updateAllianceRank(MapleGuildCharacter mgc) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(27);
            mpw.writeInt(mgc.getId());
            mpw.write(mgc.getAllianceRank());

            return mpw.getPacket();
        }

        public static byte[] changeAllianceNotice(int allianceid, String notice) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(28);
            mpw.writeInt(allianceid);
            mpw.writeMapleAsciiString(notice);

            return mpw.getPacket();
        }

        public static byte[] disbandAlliance(int alliance) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(29);
            mpw.writeInt(alliance);

            return mpw.getPacket();
        }

        public static byte[] changeAlliance(MapleGuildAlliance alliance, boolean in) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(1);
            mpw.write(in ? 1 : 0);
            mpw.writeInt(in ? alliance.getId() : 0);
            int noGuilds = alliance.getNoGuilds();
            MapleGuild[] g = new MapleGuild[noGuilds];
            for (int i = 0; i < noGuilds; i++) {
                g[i] = World.Guild.getGuild(alliance.getGuildId(i));
                if (g[i] == null) {
                    return CWvsContext.enableActions();
                }
            }
            mpw.write(noGuilds);
            for (int i = 0; i < noGuilds; i++) {
                mpw.writeInt(g[i].getId());

                Collection<MapleGuildCharacter> members = g[i].getMembers();
                mpw.writeInt(members.size());
                for (MapleGuildCharacter mgc : members) {
                    mpw.writeInt(mgc.getId());
                    mpw.write(in ? mgc.getAllianceRank() : 0);
                }
            }

            return mpw.getPacket();
        }

        public static byte[] changeAllianceLeader(int allianceid, int newLeader, int oldLeader) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(2);
            mpw.writeInt(allianceid);
            mpw.writeInt(oldLeader);
            mpw.writeInt(newLeader);

            return mpw.getPacket();
        }

        public static byte[] changeGuildInAlliance(MapleGuildAlliance alliance, MapleGuild guild, boolean add) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(4);
            mpw.writeInt(add ? alliance.getId() : 0);
            mpw.writeInt(guild.getId());
            Collection<MapleGuildCharacter> members = guild.getMembers();
            mpw.writeInt(members.size());
            for (MapleGuildCharacter mgc : members) {
                mpw.writeInt(mgc.getId());
                mpw.write(add ? mgc.getAllianceRank() : 0);
            }

            return mpw.getPacket();
        }

        public static byte[] changeAllianceRank(int allianceid, MapleGuildCharacter player) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ALLIANCE_OPERATION);
            mpw.write(5);
            mpw.writeInt(allianceid);
            mpw.writeInt(player.getId());
            mpw.writeInt(player.getAllianceRank());

            return mpw.getPacket();
        }
    }

    public static class FamilyPacket {

        public static byte[] getFamilyData() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FAMILY);
            MapleFamilyBuff[] entries = MapleFamilyBuff.values();
            mpw.writeInt(entries.length);

            for (MapleFamilyBuff entry : entries) {
                mpw.write(entry.type);
                mpw.writeInt(entry.rep);
                mpw.writeInt(1);
                mpw.writeMapleAsciiString(entry.name);
                mpw.writeMapleAsciiString(entry.desc);
            }
            return mpw.getPacket();
        }

        public static byte[] getFamilyInfo(MapleCharacter chr) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_FAMILY);
            mpw.writeInt(chr.getCurrentRep());
            mpw.writeInt(chr.getTotalRep());
            mpw.writeInt(chr.getTotalRep());
            mpw.writeShort(chr.getNoJuniors());
            mpw.writeShort(2);
            mpw.writeShort(chr.getNoJuniors());
            MapleFamily family = World.Family.getFamily(chr.getFamilyId());
            if (family != null) {
                mpw.writeInt(family.getLeaderId());
                mpw.writeMapleAsciiString(family.getLeaderName());
                mpw.writeMapleAsciiString(family.getNotice());
            } else {
                mpw.writeLong(0L);
            }
            List<?> b = chr.usedBuffs();
            mpw.writeInt(b.size());
            for (Iterator<?> i$ = b.iterator(); i$.hasNext();) {
                int ii = ((Integer) i$.next()).intValue();
                mpw.writeInt(ii);
                mpw.writeInt(1);
            }

            return mpw.getPacket();
        }

        public static void addFamilyCharInfo(MapleFamilyCharacter ldr, MaplePacketWriter mpw) {
            mpw.writeInt(ldr.getId());
            mpw.writeInt(ldr.getSeniorId());
            mpw.writeShort(ldr.getJobId());
            mpw.writeShort(0);
            mpw.write(ldr.getLevel());
            mpw.write(ldr.isOnline() ? 1 : 0);
            mpw.writeInt(ldr.getCurrentRep());
            mpw.writeInt(ldr.getTotalRep());
            mpw.writeInt(ldr.getTotalRep());
            mpw.writeInt(ldr.getTotalRep());
            mpw.writeInt(Math.max(ldr.getChannel(), 0));
            mpw.writeInt(0);
            mpw.writeMapleAsciiString(ldr.getName());
        }

        public static byte[] getFamilyPedigree(MapleCharacter chr) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SEND_PEDIGREE);
            mpw.writeInt(chr.getID());
            MapleFamily family = World.Family.getFamily(chr.getFamilyId());

            int descendants = 2;
            int gens = 0;
            int generations = 0;
            if (family == null) {
                mpw.writeInt(2);
                addFamilyCharInfo(new MapleFamilyCharacter(chr, 0, 0, 0, 0), mpw);
            } else {
                mpw.writeInt(family.getMFC(chr.getID()).getPedigree().size() + 1);
                addFamilyCharInfo(family.getMFC(family.getLeaderId()), mpw);

                if (chr.getSeniorId() > 0) {
                    MapleFamilyCharacter senior = family.getMFC(chr.getSeniorId());
                    if (senior != null) {
                        if (senior.getSeniorId() > 0) {
                            addFamilyCharInfo(family.getMFC(senior.getSeniorId()), mpw);
                        }
                        addFamilyCharInfo(senior, mpw);
                    }
                }
            }
            addFamilyCharInfo(chr.getMFC() == null ? new MapleFamilyCharacter(chr, 0, 0, 0, 0) : chr.getMFC(), mpw);
            if (family != null) {
                if (chr.getSeniorId() > 0) {
                    MapleFamilyCharacter senior = family.getMFC(chr.getSeniorId());
                    if (senior != null) {
                        if ((senior.getJunior1() > 0) && (senior.getJunior1() != chr.getID())) {
                            addFamilyCharInfo(family.getMFC(senior.getJunior1()), mpw);
                        } else if ((senior.getJunior2() > 0) && (senior.getJunior2() != chr.getID())) {
                            addFamilyCharInfo(family.getMFC(senior.getJunior2()), mpw);
                        }

                    }

                }

                if (chr.getJunior1() > 0) {
                    MapleFamilyCharacter junior = family.getMFC(chr.getJunior1());
                    if (junior != null) {
                        addFamilyCharInfo(junior, mpw);
                    }
                }
                if (chr.getJunior2() > 0) {
                    MapleFamilyCharacter junior = family.getMFC(chr.getJunior2());
                    if (junior != null) {
                        addFamilyCharInfo(junior, mpw);
                    }
                }
                if (chr.getJunior1() > 0) {
                    MapleFamilyCharacter junior = family.getMFC(chr.getJunior1());
                    if (junior != null) {
                        if ((junior.getJunior1() > 0) && (family.getMFC(junior.getJunior1()) != null)) {
                            gens++;
                            addFamilyCharInfo(family.getMFC(junior.getJunior1()), mpw);
                        }
                        if ((junior.getJunior2() > 0) && (family.getMFC(junior.getJunior2()) != null)) {
                            gens++;
                            addFamilyCharInfo(family.getMFC(junior.getJunior2()), mpw);
                        }
                    }
                }
                if (chr.getJunior2() > 0) {
                    MapleFamilyCharacter junior = family.getMFC(chr.getJunior2());
                    if (junior != null) {
                        if ((junior.getJunior1() > 0) && (family.getMFC(junior.getJunior1()) != null)) {
                            gens++;
                            addFamilyCharInfo(family.getMFC(junior.getJunior1()), mpw);
                        }
                        if ((junior.getJunior2() > 0) && (family.getMFC(junior.getJunior2()) != null)) {
                            gens++;
                            addFamilyCharInfo(family.getMFC(junior.getJunior2()), mpw);
                        }
                    }
                }
                generations = family.getMemberSize();
            }
            mpw.writeLong(gens);
            mpw.writeInt(0);
            mpw.writeInt(-1);
            mpw.writeInt(generations);

            if (family != null) {
                if (chr.getJunior1() > 0) {
                    MapleFamilyCharacter junior = family.getMFC(chr.getJunior1());
                    if (junior != null) {
                        if ((junior.getJunior1() > 0) && (family.getMFC(junior.getJunior1()) != null)) {
                            mpw.writeInt(junior.getJunior1());
                            mpw.writeInt(family.getMFC(junior.getJunior1()).getDescendants());
                        } else {
                            mpw.writeInt(0);
                        }
                        if ((junior.getJunior2() > 0) && (family.getMFC(junior.getJunior2()) != null)) {
                            mpw.writeInt(junior.getJunior2());
                            mpw.writeInt(family.getMFC(junior.getJunior2()).getDescendants());
                        } else {
                            mpw.writeInt(0);
                        }
                    }
                }
                if (chr.getJunior2() > 0) {
                    MapleFamilyCharacter junior = family.getMFC(chr.getJunior2());
                    if (junior != null) {
                        if ((junior.getJunior1() > 0) && (family.getMFC(junior.getJunior1()) != null)) {
                            mpw.writeInt(junior.getJunior1());
                            mpw.writeInt(family.getMFC(junior.getJunior1()).getDescendants());
                        } else {
                            mpw.writeInt(0);
                        }
                        if ((junior.getJunior2() > 0) && (family.getMFC(junior.getJunior2()) != null)) {
                            mpw.writeInt(junior.getJunior2());
                            mpw.writeInt(family.getMFC(junior.getJunior2()).getDescendants());
                        } else {
                            mpw.writeInt(0);
                        }
                    }
                }
            }

            List<?> b = chr.usedBuffs();
            mpw.writeInt(b.size());
            for (Iterator<?> i$ = b.iterator(); i$.hasNext();) {
                int ii = ((Integer) i$.next()).intValue();
                mpw.writeInt(ii);
                mpw.writeInt(1);
            }
            mpw.writeShort(2);

            return mpw.getPacket();
        }

        public static byte[] getFamilyMsg(byte type, int meso) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FAMILY_MESSAGE);
            mpw.writeInt(type);
            mpw.writeInt(meso);

            return mpw.getPacket();
        }

        public static byte[] sendFamilyInvite(int cid, int otherLevel, int otherJob, String inviter) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FAMILY_INVITE);
            mpw.writeInt(cid);
            mpw.writeInt(otherLevel);
            mpw.writeInt(otherJob);
            mpw.writeInt(0);
            mpw.writeMapleAsciiString(inviter);
            return mpw.getPacket();
        }

        public static byte[] sendFamilyJoinResponse(boolean accepted, String added) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FAMILY_INVITE_RESPONSE);
            mpw.write(accepted ? 1 : 0);
            mpw.writeMapleAsciiString(added);

            return mpw.getPacket();
        }

        public static byte[] getSeniorMessage(String name) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SENIOR_MESSAGE);
            mpw.writeMapleAsciiString(name);

            return mpw.getPacket();
        }

        public static byte[] changeRep(int r, String name) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REP_INCREASE);
            mpw.writeInt(r);
            mpw.writeMapleAsciiString(name);

            return mpw.getPacket();
        }

        public static byte[] familyLoggedIn(boolean online, String name) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FAMILY_LOGGEDIN);
            mpw.write(online ? 1 : 0);
            mpw.writeMapleAsciiString(name);

            return mpw.getPacket();
        }

        public static byte[] familyBuff(int type, int buffnr, int amount, int time) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FAMILY_BUFF);
            mpw.write(type);
            if ((type >= 2) && (type <= 4)) {
                mpw.writeInt(buffnr);

                mpw.writeInt(type == 3 ? 0 : amount);
                mpw.writeInt(type == 2 ? 0 : amount);
                mpw.write(0);
                mpw.writeInt(time);
            }
            return mpw.getPacket();
        }

        public static byte[] cancelFamilyBuff() {
            return familyBuff(0, 0, 0, 0);
        }

        public static byte[] familySummonRequest(String name, String mapname) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FAMILY_USE_REQUEST);
            mpw.writeMapleAsciiString(name);
            mpw.writeMapleAsciiString(mapname);

            return mpw.getPacket();
        }
    }

    public static class BuddylistPacket {

        public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist) {
            return updateBuddylist(buddylist, 7);
        }

        public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist, int deleted) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BUDDYLIST);
            mpw.write(deleted);
            mpw.write(buddylist.size());
            for (BuddylistEntry buddy : buddylist) {
                mpw.writeInt(buddy.getCharacterId());
                mpw.writeAsciiString(buddy.getName(), 13);
                mpw.write(buddy.isVisible() ? 0 : 1);//if adding = 2
                mpw.writeInt(buddy.getChannel() == -1 ? -1 : buddy.getChannel());
                mpw.writeAsciiString(buddy.getGroup(), 17);
            }
            for (int x = 0; x < buddylist.size(); x++) {
                mpw.writeInt(0);
            }

            return mpw.getPacket();
        }

        public static byte[] updateBuddyChannel(int characterid, int channel) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BUDDYLIST);
            mpw.write(20);
            mpw.writeInt(characterid);
            mpw.write(0);
            mpw.writeInt(channel);

            return mpw.getPacket();
        }

        public static byte[] requestBuddylistAdd(int cidFrom, String nameFrom, int levelFrom, int jobFrom) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BUDDYLIST);
            mpw.write(9);
            mpw.writeInt(cidFrom);
            mpw.writeMapleAsciiString(nameFrom);
            mpw.writeInt(levelFrom);
            mpw.writeInt(jobFrom);
            mpw.writeInt(0);//v115
            mpw.writeInt(cidFrom);
            mpw.writeAsciiString(nameFrom, 13);
            mpw.write(1);
            mpw.writeInt(0);
            mpw.writeAsciiString("ETC", 16);
            mpw.writeShort(0);//was1

            return mpw.getPacket();
        }

        public static byte[] updateBuddyCapacity(int capacity) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BUDDYLIST);
            mpw.write(21);
            mpw.write(capacity);

            return mpw.getPacket();
        }

        public static byte[] buddylistMessage(byte message) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BUDDYLIST);
            mpw.write(message);

            return mpw.getPacket();
        }
    }

    public static byte[] giveKilling(int x) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
        PacketHelper.writeSingleMask(mpw, MapleBuffStat.KILL_COUNT);
//        mpw.writeInt(0);
//        mpw.write(0);
//        mpw.writeInt(x);
//        mpw.writeZeroBytes(6);
        mpw.writeShort(0);
        mpw.write(0);
        mpw.writeInt(x);
        return mpw.getPacket();
    }

    public static class ExpeditionPacket {

        public static byte[] expeditionStatus(MapleExpedition me, boolean created, boolean silent) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EXPEDITION_OPERATION);
            mpw.write(created ? 86 : silent ? 72 : 76);//74
            mpw.writeInt(me.getType().exped);
            mpw.writeInt(0);
            for (int i = 0; i < 6; i++) {
                if (i < me.getParties().size()) {
                    MapleParty party = World.Party.getParty((me.getParties().get(i)).intValue());

                    CWvsContext.PartyPacket.addPartyStatus(-1, party, mpw, false, true);
                } else {
                    CWvsContext.PartyPacket.addPartyStatus(-1, null, mpw, false, true);
                }

            }

            return mpw.getPacket();
        }

        public static byte[] expeditionError(int errcode, String name) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EXPEDITION_OPERATION);
            mpw.write(100);//88
            mpw.writeInt(errcode);
            mpw.writeMapleAsciiString(name);

            return mpw.getPacket();
        }

        public static byte[] expeditionMessage(int code) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EXPEDITION_OPERATION);
            mpw.write(code);

            return mpw.getPacket();
        }

        public static byte[] expeditionJoined(String name) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EXPEDITION_OPERATION);
            mpw.write(87);//75
            mpw.writeMapleAsciiString(name);

            return mpw.getPacket();
        }

        public static byte[] expeditionLeft(String name) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EXPEDITION_OPERATION);
            mpw.write(92);//79
            mpw.writeMapleAsciiString(name);

            return mpw.getPacket();
        }

        public static byte[] expeditionLeaderChanged(int newLeader) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EXPEDITION_OPERATION);
            mpw.write(96);//84
            mpw.writeInt(newLeader);

            return mpw.getPacket();
        }

        public static byte[] expeditionUpdate(int partyIndex, MapleParty party) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EXPEDITION_OPERATION);
            mpw.write(97);//85
            mpw.writeInt(0);
            mpw.writeInt(partyIndex);

            CWvsContext.PartyPacket.addPartyStatus(-1, party, mpw, false, true);

            return mpw.getPacket();
        }

        public static byte[] expeditionInvite(MapleCharacter from, int exped) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EXPEDITION_OPERATION);
            mpw.write(99);//87
            mpw.writeInt(from.getLevel());
            mpw.writeInt(from.getJob());
            mpw.writeInt(0);
            mpw.writeMapleAsciiString(from.getName());
            mpw.writeInt(exped);

            return mpw.getPacket();
        }
    }

    public static class PartyPacket {

        public static byte[] partyCreated(int partyid, String partyName) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PARTY_OPERATION);
            mpw.write(16); // This value updates with versions
            mpw.writeInt(partyid);
            mpw.writeInt(999999999);
            mpw.writeInt(999999999);
            mpw.writeInt(0);
            mpw.writeShort(0);
            mpw.writeShort(0);
            mpw.write(0);
            mpw.write(1);
            mpw.writeMapleAsciiString(partyName);

            return mpw.getPacket();
        }
        
        public static byte[] partyCreated(int partyid) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PARTY_OPERATION);
            mpw.write(16); // This value updates with versions
            mpw.writeInt(partyid);
            mpw.writeInt(999999999);
            mpw.writeInt(999999999);
            mpw.writeInt(0);
            mpw.writeShort(0);
            mpw.writeShort(0);
            mpw.write(0);
            mpw.write(1);

            return mpw.getPacket();
        }

        public static byte[] partyInvite(MapleCharacter from) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PARTY_OPERATION);
            mpw.write(4);
            //mpw.writeInt(from.getParty() == null ? 0 : from.getParty().getId());
            mpw.writeInt(from.getParty() == null? 0 : from.getID());
            mpw.writeMapleAsciiString(from.getName());
            mpw.writeInt(from.getLevel());
            mpw.writeInt(from.getJob());
            mpw.writeShort(0);
            mpw.writeInt(0);
            
            return mpw.getPacket();
        }

        public static byte[] partyRequestInvite(MapleCharacter to) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PARTY_OPERATION);
            mpw.write(33);
            mpw.writeMapleAsciiString(to.getName());

            return mpw.getPacket();
        }

        /**
         * 10: A beginner can't create a party. 
         * 1/11/14/19: Your request for a party didn't work due to an unexpected error. 
         * 13: You have yet to join a party.
         * 16: Already have joined a party. 
         * 17: The party you're trying to join is already in full capacity. 
         * 19: Unable to find the requested character in this channel.
         *
         * @param message
         * @return
         */
        public static byte[] partyStatusMessage(int message, String charname) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PARTY_OPERATION);
            mpw.write(message);
            if ((message == 30) || (message == 52)) {
                mpw.writeMapleAsciiString(charname);
            } else if (message == 45) {
                mpw.write(0);
            }

            System.out.println("[PartyStatusMessage] " + mpw.toString());
            return mpw.getPacket();
        }

        public static void addPartyStatus(int forchannel, MapleParty party, MaplePacketWriter mpw, boolean leaving) {
            addPartyStatus(forchannel, party, mpw, leaving, false);
        }

        public static void addPartyStatus(int forchannel, MapleParty party, MaplePacketWriter mpw, boolean leaving, boolean exped) {
            List<MaplePartyCharacter> partymembers;
            if (party == null) {
                partymembers = new ArrayList();
            } else {
                partymembers = new ArrayList(party.getMembers());
            }
            while (partymembers.size() < 6) {
                partymembers.add(new MaplePartyCharacter());
            }
            for (MaplePartyCharacter partychar : partymembers) {
                mpw.writeInt(partychar.getId());
            }
            for (MaplePartyCharacter partychar : partymembers) {
                mpw.writeAsciiString(partychar.getName(), 13);
            }
            for (MaplePartyCharacter partychar : partymembers) {
                mpw.writeInt(partychar.getJobId());
            }
            for (MaplePartyCharacter partychar : partymembers) {
                mpw.writeInt(0);
            }
            for (MaplePartyCharacter partychar : partymembers) {
                mpw.writeInt(partychar.getLevel());
            }
            for (MaplePartyCharacter partychar : partymembers) {
                mpw.writeInt(partychar.isOnline() ? partychar.getChannel() - 1 : -2);
            }
            for (MaplePartyCharacter partychar : partymembers) {
                mpw.writeInt(0);
            }

            mpw.writeInt(party == null ? 0 : party.getLeader().getId());
            if (exped) {
                return;
            }
            for (MaplePartyCharacter partychar : partymembers) {
                mpw.writeInt(partychar.getChannel() == forchannel ? partychar.getMapid() : 0);
            }
            for (MaplePartyCharacter partychar : partymembers) {
                if ((partychar.getChannel() == forchannel) && (!leaving)) {
                    mpw.writeInt(partychar.getDoorTown());
                    mpw.writeInt(partychar.getDoorTarget());
                    mpw.writeInt(partychar.getDoorSkill());
                    mpw.writeInt(partychar.getDoorPosition().x);
                    mpw.writeInt(partychar.getDoorPosition().y);
                } else {
                    mpw.writeInt(leaving ? 999999999 : 0);
                    mpw.writeLong(leaving ? 999999999L : 0L);
                    mpw.writeLong(leaving ? -1L : 0L);
                }
            }
            mpw.write(1);
            mpw.writeMapleAsciiString(party.getName());
        }

        public static byte[] updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PARTY_OPERATION);
            switch (op) {
                case DISBAND:
                case EXPEL:
                case LEAVE:
                    mpw.write(21);
                    mpw.writeInt(party.getId());
                    mpw.writeInt(target.getId());
                    mpw.write(op == PartyOperation.DISBAND ? 0 : 1);
                    if (op == PartyOperation.DISBAND) {
                    	mpw.writeInt(target.getId());
                        break;
                    }
                    mpw.write(op == PartyOperation.EXPEL ? 1 : 0);
                    mpw.writeMapleAsciiString(target.getName());
                    addPartyStatus(forChannel, party, mpw, op == PartyOperation.LEAVE);
                    break;
                case JOIN:
                    mpw.write(24);
                    mpw.writeInt(party.getId());
                    mpw.writeMapleAsciiString(target.getName());
                    addPartyStatus(forChannel, party, mpw, false);
                    break;
                case SILENT_UPDATE:
                case LOG_ONOFF:
                    mpw.write(15);
                    mpw.writeInt(party.getId());
                    addPartyStatus(forChannel, party, mpw, op == PartyOperation.LOG_ONOFF);
                    break;
                case CHANGE_LEADER:
                case CHANGE_LEADER_DC:
                    mpw.write(48);
                    mpw.writeInt(target.getId());
                    mpw.write(op == PartyOperation.CHANGE_LEADER_DC ? 1 : 0);
                    break;
                case CHANGE_LEADER_DC_2:
                	mpw.write(57);
                	mpw.write(party.getId());
                	addPartyStatus(forChannel, party, mpw, op == PartyOperation.CHANGE_LEADER_DC_2);
            }
            
            System.out.println("[UpdateParty " + op + "] " + mpw.toString());
            return mpw.getPacket();
        }

        public static byte[] partyPortal(int townId, int targetId, int skillId, Point position, boolean animation) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PARTY_OPERATION);
            mpw.write(61);
            mpw.write(animation ? 0 : 1);
            mpw.writeInt(townId);
            mpw.writeInt(targetId);
            mpw.writeInt(skillId);
            mpw.writePos(position);

            return mpw.getPacket();
        }

        public static byte[] getPartyListing(PartySearchType pst) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PARTY_OPERATION);
            mpw.write(GameConstants.GMS ? 147 : 77);
            mpw.writeInt(pst.id);
            final List<PartySearch> parties = World.Party.searchParty(pst);
            mpw.writeInt(parties.size());
            for (PartySearch party : parties) {
                mpw.writeInt(0);
                mpw.writeInt(2);
                if (pst.exped) {
                    MapleExpedition me = World.Party.getExped(party.getId());
                    mpw.writeInt(me.getType().maxMembers);
                    mpw.writeInt(party.getId());
                    mpw.writeAsciiString(party.getName(), 48);
                    for (int i = 0; i < 5; i++) {
                        if (i < me.getParties().size()) {
                            MapleParty part = World.Party.getParty((me.getParties().get(i)).intValue());
                            if (part != null) {
                                addPartyStatus(-1, part, mpw, false, true);
                            } else {
                                mpw.writeZeroBytes(202);
                            }
                        } else {
                            mpw.writeZeroBytes(202);
                        }
                    }
                } else {
                    mpw.writeInt(0);
                    mpw.writeInt(party.getId());
                    mpw.writeAsciiString(party.getName(), 48);
                    addPartyStatus(-1, World.Party.getParty(party.getId()), mpw, false, true);
                }

                mpw.writeShort(0);
            }

            return mpw.getPacket();
        }

        public static byte[] partyListingAdded(PartySearch ps) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PARTY_OPERATION);
            mpw.write(93);
            mpw.writeInt(ps.getType().id);
            mpw.writeInt(0);
            mpw.writeInt(1);
            if (ps.getType().exped) {
                MapleExpedition me = World.Party.getExped(ps.getId());
                mpw.writeInt(me.getType().maxMembers);
                mpw.writeInt(ps.getId());
                mpw.writeAsciiString(ps.getName(), 48);
                for (int i = 0; i < 5; i++) {
                    if (i < me.getParties().size()) {
                        MapleParty party = World.Party.getParty((me.getParties().get(i)).intValue());
                        if (party != null) {
                            addPartyStatus(-1, party, mpw, false, true);
                        } else {
                            mpw.writeZeroBytes(202);
                        }
                    } else {
                        mpw.writeZeroBytes(202);
                    }
                }
            } else {
                mpw.writeInt(0);
                mpw.writeInt(ps.getId());
                mpw.writeAsciiString(ps.getName(), 48);
                addPartyStatus(-1, World.Party.getParty(ps.getId()), mpw, false, true);
            }
            mpw.writeShort(0);

            return mpw.getPacket();
        }

        public static byte[] showMemberSearch(List<MapleCharacter> chr) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MEMBER_SEARCH);
            mpw.write(chr.size());
            for (MapleCharacter c : chr) {
                mpw.writeInt(c.getID());
                mpw.writeMapleAsciiString(c.getName());
                mpw.writeShort(c.getJob());
                mpw.write(c.getLevel());
            }
            return mpw.getPacket();
        }

        public static byte[] showPartySearch(List<MapleParty> chr) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PARTY_SEARCH);
            mpw.write(chr.size());
            for (MapleParty c : chr) {
                mpw.writeInt(c.getId());
                mpw.writeMapleAsciiString(c.getLeader().getName());
                mpw.write(c.getLeader().getLevel());
                mpw.write(c.getLeader().isOnline() ? 1 : 0);
                mpw.write(c.getMembers().size());
                for (MaplePartyCharacter ch : c.getMembers()) {
                    mpw.writeInt(ch.getId());
                    mpw.writeMapleAsciiString(ch.getName());
                    mpw.writeShort(ch.getJobId());
                    mpw.write(ch.getLevel());
                    mpw.write(ch.isOnline() ? 1 : 0);
                }
            }
            return mpw.getPacket();
        }
    }

    public static class GuildPacket {
    	
    	public static byte[] approveGuildName(String guildName) {
    		MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
    		mpw.write(5);  // Last updated: v172.2
    		mpw.writeMapleAsciiString(guildName);
    		
    		return mpw.getPacket();
    	}

        public static byte[] guildInvite(int gid, String charName, int levelFrom, int jobFrom) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(7);  // Last updated: v172.2
            mpw.writeInt(gid);
            mpw.writeMapleAsciiString(charName);
            mpw.writeInt(levelFrom);
            mpw.writeInt(jobFrom);
            mpw.writeInt(0);
            
            return mpw.getPacket();
        }

        public static byte[] showGuildInfo(MapleCharacter chr) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(49); // Last updated: v172.2
            if ((chr == null) || (chr.getMGC() == null)) {
                mpw.write(0);
                return mpw.getPacket();
            }
            MapleGuild g = World.Guild.getGuild(chr.getGuildId());
            if (g == null) {
                mpw.write(0);
                return mpw.getPacket();
            }
            mpw.write(0);
            mpw.write(1);
            getGuildInfo(mpw, g);

            return mpw.getPacket();
        }

        public static void getGuildInfo(MaplePacketWriter mpw, MapleGuild guild) {
            mpw.writeInt(guild.getId());
            mpw.writeMapleAsciiString(guild.getName());
            for (int i = 1; i <= 5; i++) {
                mpw.writeMapleAsciiString(guild.getRankTitle(i));
            }
            guild.addMemberData(mpw);
            mpw.writeInt(guild.getCapacity());
            mpw.writeShort(guild.getLogoBG());
            mpw.write(guild.getLogoBGColor());
            mpw.writeShort(guild.getLogo());
            mpw.write(guild.getLogoColor());
            mpw.writeMapleAsciiString(guild.getNotice());
            mpw.writeInt(guild.getHonorExp());
            mpw.writeInt(guild.getHonorExp()); // This honor exp value is a little higher than the previous one.
            mpw.writeInt(guild.getAllianceId() > 0 ? guild.getAllianceId() : 0);
            mpw.write(guild.getLevel());
            mpw.writeShort(0); // Guild Ranking
            mpw.writeInt(guild.getGP());
            mpw.write(0); // idk
            mpw.writeShort(0); // idk
            mpw.writeInt(guild.getSkills().size());
            for (MapleGuildSkill i : guild.getSkills()) {
                mpw.writeInt(i.skillID);
                //mpw.writeShort(i.level);
                //mpw.writeLong(PacketHelper.getTime(i.timestamp));
                //mpw.writeMapleAsciiString(i.purchaser);
                //mpw.writeMapleAsciiString(i.activator);
            }
        }

        public static byte[] newGuildInfo(MapleCharacter c) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(38);
            if ((c == null) || (c.getMGC() == null)) {
                return genericGuildMessage((byte) 37);
            }
            MapleGuild g = World.Guild.getGuild(c.getGuildId());
            if (g == null) {
                return genericGuildMessage((byte) 37);
            }
            getGuildInfo(mpw, g);

            return mpw.getPacket();
        }

        public static byte[] newGuildMember(MapleGuildCharacter mgc) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(63); // Last updated: v174.3
            mpw.writeInt(mgc.getGuildId());
            mpw.writeInt(mgc.getId());
            mpw.writeAsciiString(mgc.getName(), 13);
            mpw.writeInt(mgc.getJobId());
            mpw.writeInt(mgc.getLevel());
            mpw.writeInt(mgc.getGuildRank());
            mpw.writeInt(mgc.isOnline() ? 1 : 0);
            mpw.writeInt(mgc.getAllianceRank());
            mpw.writeInt(mgc.getGuildContribution());
            mpw.writeInt(mgc.getGuildContribution());
            mpw.writeInt(0); // IGP
            mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));

            return mpw.getPacket();
        }

        public static byte[] memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(bExpelled ? 78 : 75); // Last updated: v172.2
            mpw.writeInt(mgc.getGuildId());
            mpw.writeInt(mgc.getId());
            mpw.writeMapleAsciiString(mgc.getName());

            return mpw.getPacket();
        }

        public static byte[] guildDisband(int gid) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(81); // Last updated: v172.2
            mpw.writeInt(gid);
            mpw.write(1);

            return mpw.getPacket();
        }

        public static byte[] guildCapacityChange(int gid, int capacity) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(64);
            mpw.writeInt(gid);
            mpw.write(capacity);

            return mpw.getPacket();
        }

        public static byte[] guildContribution(int guildid, int charid, int contribution, int individualGP) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(100); // Last updated: v174.3
            mpw.writeInt(guildid);
            mpw.writeInt(charid);
            mpw.writeInt(contribution); // Contribution
            mpw.writeInt((int)(contribution * 0.3)); // Guild Point    // Sidenote: Not sure if we need to create an actual GP column for the characters in the SQL database.
            mpw.writeInt(individualGP); // Individual Guild Point (IGP)
            mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));

            return mpw.getPacket();
        }

        public static byte[] changeRank(MapleGuildCharacter mgc) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(98); // Last updated: v174.3
            mpw.writeInt(mgc.getGuildId());
            mpw.writeInt(mgc.getId());
            mpw.write(mgc.getGuildRank());

            return mpw.getPacket();
        }

        public static byte[] rankTitleChange(int gid, String[] ranks) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(96); // Last updated: v174.3
            mpw.writeInt(gid);
            for (String r : ranks) {
                mpw.writeMapleAsciiString(r);
            }

            return mpw.getPacket();
        }

        public static byte[] guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(101); // Last updated: v174.3 guess
            mpw.writeInt(gid);
            mpw.writeShort(bg);
            mpw.write(bgcolor);
            mpw.writeShort(logo);
            mpw.write(logocolor);

            return mpw.getPacket();
        }

        public static byte[] updateGP(int guildId, int honorexp, int GP, int guildLevel) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(107); // Last updated: v174.3
            mpw.writeInt(guildId);
            mpw.writeInt(honorexp);
            mpw.writeInt(guildLevel);
            mpw.writeInt(GP);

            return mpw.getPacket();
        }

        public static byte[] guildNotice(int gid, String notice) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(75);
            mpw.writeInt(gid);
            mpw.writeMapleAsciiString(notice);

            return mpw.getPacket();
        }

        public static byte[] guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(92); // Last updated: v172.2
            mpw.writeInt(mgc.getGuildId());
            mpw.writeInt(mgc.getId());
            mpw.writeInt(mgc.getLevel());
            mpw.writeInt(mgc.getJobId());

            return mpw.getPacket();
        }

        public static byte[] guildMemberOnline(int guildId, int charId, boolean bOnline) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(95); // Last updated: v174.3
            mpw.writeInt(guildId);
            mpw.writeInt(charId);
            mpw.write(bOnline ? 1 : 0);
            mpw.write(1); // Unknown boolean

            return mpw.getPacket();
        }

        public static byte[] showGuildRanks(int npcid, List<MapleGuildRanking.GuildRankingInfo> all) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(80);
            mpw.writeInt(npcid);
            mpw.writeInt(all.size());
            for (MapleGuildRanking.GuildRankingInfo info : all) {
                mpw.writeShort(0);
                mpw.writeMapleAsciiString(info.getName());
                mpw.writeInt(info.getGP());
                mpw.writeInt(info.getLogo());
                mpw.writeInt(info.getLogoColor());
                mpw.writeInt(info.getLogoBg());
                mpw.writeInt(info.getLogoBgColor());
            }

            return mpw.getPacket();
        }

        public static byte[] guildSkillPurchased(int gid, int sid, int level, long expiration, String purchase, String activate) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(85);
            mpw.writeInt(gid);
            mpw.writeInt(sid);
            mpw.writeShort(level);
            mpw.writeLong(PacketHelper.getTime(expiration));
            mpw.writeMapleAsciiString(purchase);
            mpw.writeMapleAsciiString(activate);

            return mpw.getPacket();
        }

        public static byte[] guildLeaderChanged(int gid, int oldLeader, int newLeader, int allianceId) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(89);
            mpw.writeInt(gid);
            mpw.writeInt(oldLeader);
            mpw.writeInt(newLeader);
            mpw.write(1);
            mpw.writeInt(allianceId);

            return mpw.getPacket();
        }

        public static byte[] denyGuildInvitation(String charname) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(61);
            mpw.writeMapleAsciiString(charname);

            return mpw.getPacket();
        }

        public static byte[] genericGuildMessage(byte code) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILD_OPERATION);
            mpw.write(code);//30 = cant find in ch
            if (code == 87) {
                mpw.writeInt(0);
            }
            /*
            if ((code == 3) || (code == 59) || (code == 60) || (code == 61) || (code == 84) || (code == 87)) {
                mpw.writeMapleAsciiString("");
            }
            */
            
            return mpw.getPacket();
        }

        public static byte[] BBSThreadList(List<MapleBBSThread> bbs, int start) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BBS_OPERATION);
            mpw.write(6);
            if (bbs == null) {
                mpw.write(0);
                mpw.writeLong(0L);
                return mpw.getPacket();
            }
            int threadCount = bbs.size();
            MapleBBSThread notice = null;
            for (MapleBBSThread b : bbs) {
                if (b.isNotice()) {
                    notice = b;
                    break;
                }
            }
            mpw.write(notice == null ? 0 : 1);
            if (notice != null) {
                addThread(mpw, notice);
            }
            if (threadCount < start) {
                start = 0;
            }
            mpw.writeInt(threadCount);
            int pages = Math.min(10, threadCount - start);
            mpw.writeInt(pages);
            for (int i = 0; i < pages; i++) {
                addThread(mpw, (MapleBBSThread) bbs.get(start + i));
            }

            return mpw.getPacket();
        }

        private static void addThread(MaplePacketWriter mpw, MapleBBSThread rs) {
            mpw.writeInt(rs.localthreadID);
            mpw.writeInt(rs.ownerID);
            mpw.writeMapleAsciiString(rs.name);
            mpw.writeLong(PacketHelper.getKoreanTimestamp(rs.timestamp));
            mpw.writeInt(rs.icon);
            mpw.writeInt(rs.getReplyCount());
        }

        public static byte[] showThread(MapleBBSThread thread) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BBS_OPERATION);
            mpw.write(7);
            mpw.writeInt(thread.localthreadID);
            mpw.writeInt(thread.ownerID);
            mpw.writeLong(PacketHelper.getKoreanTimestamp(thread.timestamp));
            mpw.writeMapleAsciiString(thread.name);
            mpw.writeMapleAsciiString(thread.text);
            mpw.writeInt(thread.icon);
            mpw.writeInt(thread.getReplyCount());
            for (MapleBBSThread.MapleBBSReply reply : thread.replies.values()) {
                mpw.writeInt(reply.replyid);
                mpw.writeInt(reply.ownerID);
                mpw.writeLong(PacketHelper.getKoreanTimestamp(reply.timestamp));
                mpw.writeMapleAsciiString(reply.content);
            }

            return mpw.getPacket();
        }
    }

    public static class InfoPacket {

        public static byte[] showMesoGain(long gain, boolean inChat) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            if (!inChat) {
                mpw.write(0);
                mpw.write(1);
                mpw.write(0);
                mpw.writeLong(gain);
                mpw.writeShort(0);
            } else {
                mpw.write(6);
                mpw.writeLong(gain);
                mpw.writeInt(-1);
            }

            return mpw.getPacket();
        }

        public static byte[] getShowInventoryStatus(int mode) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(0);
            mpw.write(mode);
            mpw.writeInt(0);
            mpw.writeInt(0);

            return mpw.getPacket();
        }

        public static byte[] getShowItemGain(int itemId, short quantity) {
            return getShowItemGain(itemId, quantity, false);
        }

        public static byte[] getShowItemGain(int itemId, short quantity, boolean inChat) {
            MaplePacketWriter mpw;

            if (inChat) {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
                mpw.write(7);
                mpw.write(1);
                mpw.writeInt(itemId);
                mpw.writeInt(quantity);
            } else {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
                mpw.writeShort(0);
                mpw.writeInt(itemId);
                mpw.writeInt(quantity);
            }

            return mpw.getPacket();
        }

        public static byte[] updateQuest(MapleQuestStatus quest, String status) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(1);
            mpw.writeInt(quest.getQuest().getId());
            mpw.write(quest.getStatus());
            switch (quest.getStatus()) {
                case 0:
                    mpw.write(0);
                    break;
                case 1:
                    mpw.writeMapleAsciiString(quest.getCustomData() != null ? quest.getCustomData() : status);
                    break;
                case 2:
                    mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
            }

            return mpw.getPacket();
        }
        
        public static byte[] updateQuest(int questid, String status) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(1);
            mpw.writeInt(questid);
            mpw.write(1);
            mpw.writeMapleAsciiString(status);

            return mpw.getPacket();
        }

        public static byte[] updateQuestMobKills(MapleQuestStatus status) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(1);
            mpw.writeShort(status.getQuest().getId());
            mpw.write(1);
            StringBuilder sb = new StringBuilder();
            for (Iterator<?> i$ = status.getMobKills().values().iterator(); i$.hasNext();) {
                int kills = ((Integer) i$.next()).intValue();
                sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
            }
            mpw.writeMapleAsciiString(sb.toString());
            mpw.writeLong(0L);

            return mpw.getPacket();
        }

        public static byte[] itemExpired(int itemid) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(2);
            mpw.writeInt(itemid);

            return mpw.getPacket();
        }

        /**
         * Sends a gain experience message with additional exp status lines based on the exp mask. The type of exp stats that can be used
         * are found in the class {@code MapleExpStatus}.
         * @param exp
         * @return
         * 
         * @see CWvsContext::OnIncEXPMessage()
         */
        public static byte[] gainExpMessage(MapleExp exp) {
        	MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
        	mpw.write(3);
        	mpw.write(exp.isLastHit());
        	mpw.writeInt(exp.getExp());
        	mpw.write(exp.isQuest());
        	
        	PacketHelper.writeExpMask(mpw, exp.getExpStats());
        	
        	/* not implemented yet, just a place holder
        	if (flag & 1 != 0) {
        		mpw.writeInt(0); // nSelectedMobBonusExp
        	}
        	if (flag & 4 != 0) {
        		mpw.write(0); // nPartyBonusPercentage
        	}
        	*/
        	if (exp.isPartyBonus()) { // if (flag & 4 != 0) { i think
        		mpw.write(exp.getPartyBonusExpRate()); // nPartyBonusPercentage
        	}
        	if (exp.isQuest()) {
        		mpw.write(exp.getQuestBonusExpRate()); // nQuestBonusRate
        	}
        	if (exp.getQuestBonusExpRate() > 0) {
        		mpw.write(0); // nQuestBonusRemainCount
        	}
        	for (Map.Entry<MapleExpStatus, Integer> expStat : exp.getExpStats().entrySet()) {
        		if (expStat.getKey() == MapleExpStatus.BURNING_FIELD) {
        			mpw.writeInt(exp.getBurningFieldBonusExp());
        			mpw.writeInt(exp.getBurningFieldExpRate());
        		} else {
        			mpw.writeInt(expStat.getValue());
        		}
        	}

        	System.out.println("[Exp Packet] " + mpw.toString());
        	return mpw.getPacket();
        }
        
        // Old dysfunctional one
        public static byte[] gainExpMessage(int gain, boolean white, int partyinc, int Class_Bonus_EXP, int Equipment_Bonus_EXP, int Premium_Bonus_EXP) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(3);
            mpw.write(white ? 1 : 0);
            mpw.writeInt(gain);
            mpw.write(0);
            mpw.writeInt(0);
            mpw.write(0);
            mpw.write(0);
            mpw.writeInt(0);
            mpw.write(0);
            mpw.writeInt(partyinc);

            mpw.writeInt(Equipment_Bonus_EXP);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.write(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);

            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(Premium_Bonus_EXP);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            
            return mpw.getPacket();
        }

        public static byte[] GainEXP_Others(long gain, boolean inChat, boolean white) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(3);
            mpw.write(white ? 1 : 0);
            mpw.writeLong(gain);
            mpw.write(inChat ? 1 : 0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            if (inChat) {
                mpw.writeLong(0L);
            } else {
                mpw.writeShort(0);
                mpw.write(0);
            }
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.write(0);
            return mpw.getPacket();
        }

        public static byte[] getSPMsg(byte sp, short job) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(4);
            mpw.writeShort(job);
            mpw.write(sp);

            return mpw.getPacket();
        }

        public static byte[] getShowFameGain(int gain) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(5);
            mpw.writeInt(gain);

            return mpw.getPacket();
        }

        public static byte[] getGPMsg(int itemid) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(7);
            mpw.writeInt(itemid);

            return mpw.getPacket();
        }

        public static byte[] getGPContribution(int itemid) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(8);
            mpw.writeInt(itemid);

            return mpw.getPacket();
        }

        public static byte[] getStatusMsg(int itemid) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(9);
            mpw.writeInt(itemid);

            return mpw.getPacket();
        }

        public static byte[] showInfo(String info) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(11);
            mpw.writeMapleAsciiString(info);

            return mpw.getPacket();
        }

        public static byte[] updateInfoQuest(int quest, String data) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(12);
            mpw.writeInt(quest);
            mpw.writeMapleAsciiString(data);
//            System.err.println("infoquest " + mpw.toString());
            return mpw.getPacket();
        }

        public static byte[] showItemReplaceMessage(List<String> message) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(14);
            mpw.write(message.size());
            for (String x : message) {
                mpw.writeMapleAsciiString(x);
            }

            return mpw.getPacket();
        }

        public static byte[] showTraitGain(MapleTrait.MapleTraitType trait, int amount) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(18); // Last updated: v173.1
            mpw.writeLong(trait.getStat().getValue());
            mpw.writeInt(amount);

            return mpw.getPacket();
        }

        public static byte[] showTraitMaxed(MapleTrait.MapleTraitType trait) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(17);
            mpw.writeLong(trait.getStat().getValue());

            return mpw.getPacket();
        }

        public static byte[] getBPMsg(int amount) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(21);
            mpw.writeInt(amount);
            mpw.writeInt(0);

            return mpw.getPacket();
        }

        public static byte[] showExpireMessage(byte type, List<Integer> item) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(type);
            mpw.write(item.size());
            for (Integer it : item) {
                mpw.writeInt(it.intValue());
            }

            return mpw.getPacket();
        }

        public static byte[] showStatusMessage(int mode, String info, String data) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(mode);
            if (mode == 22) {
                mpw.writeMapleAsciiString(info);
                mpw.writeMapleAsciiString(data);
            }

            return mpw.getPacket();
        }

        public static byte[] showReturnStone(int act) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(23);
            mpw.write(act);

            return mpw.getPacket();
        }

        public static byte[] showItemBox() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(1);
            mpw.write(0x7A);
            mpw.write(0x1C);
            mpw.write(1);
            mpw.write(1);
            mpw.write(0);
            mpw.write(0x30);
            return mpw.getPacket();
        }

        public static byte[] getShowCoreGain(int core, int quantity) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(30);
            mpw.write(22);
            mpw.writeInt(core);
            mpw.writeInt(quantity);

            return mpw.getPacket();
        }
    }

    public static class BuffPacket {

        public static byte[] giveDice(int buffid, int skillid, int duration, Map<MapleBuffStat, Integer> statups) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            PacketHelper.writeBuffMask(mpw, statups);

            mpw.writeShort(Math.max(buffid / 100, Math.max(buffid / 10, buffid % 10))); // 1-6

            mpw.writeInt(skillid); // skillid
            mpw.writeInt(duration);
            mpw.writeShort(0);
            mpw.write(0);
            mpw.writeInt(GameConstants.getDiceStat(buffid, 3));
            mpw.writeInt(GameConstants.getDiceStat(buffid, 3));
            mpw.writeInt(GameConstants.getDiceStat(buffid, 4));
            mpw.writeZeroBytes(20); //idk
            mpw.writeInt(GameConstants.getDiceStat(buffid, 2));
            mpw.writeZeroBytes(12); //idk
            mpw.writeInt(GameConstants.getDiceStat(buffid, 5));
            mpw.writeZeroBytes(16); //idk
            mpw.writeInt(GameConstants.getDiceStat(buffid, 6));
            mpw.writeZeroBytes(16);
            mpw.writeZeroBytes(6);//new 143
            mpw.writeInt(1000);//new 143
            mpw.write(1);
            mpw.writeInt(0);//new143
//            mpw.write(4); // Total buffed times
//            mpw.write(0);//v112
            return mpw.getPacket();
        }

        public static byte[] giveHoming(int skillid, int mobid, int x) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            PacketHelper.writeSingleMask(mpw, MapleBuffStat.HOMING_BEACON);
            mpw.writeShort(0);
            mpw.write(0);
            mpw.writeInt(1);
            mpw.writeLong(skillid);
            mpw.write(0);
            mpw.writeLong(mobid);
            mpw.writeShort(0);
            mpw.writeShort(0);
            mpw.write(0);
            mpw.write(0);//v112
            return mpw.getPacket();
        }

      public static byte[] giveMount(int buffid, int skillid, Map<MapleBuffStat, Integer> statups) {
        MaplePacketWriter packet = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
        packet.writeLong(MapleBuffStat.MONSTER_RIDING.getValue());
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0); //v192 //144
        packet.writeInt(0);
        packet.writeInt(10);
        packet.writeInt(10);
        packet.writeInt(skillid); // skillid
        packet.write(HexTool.getByteArrayFromHexString("00 C2 EB 0B"));
        packet.writeInt(10);
        packet.writeInt(skillid); // skillid
        packet.write(HexTool.getByteArrayFromHexString("00 C2 EB 0B"));
        packet.writeInt(0);
        packet.writeInt(0);
        packet.write(0);
        packet.writeInt(buffid); // 1902000 saddle
        packet.writeInt(skillid); // skillid
        packet.write(0);
        packet.writeInt(0);
        packet.writeInt(0);
        packet.write(1);
        packet.writeInt(4);
        packet.write(0);
        return packet.getPacket();
    }
        
      
    public static byte[] showMonsterRiding(int cid, Map<MapleBuffStat, Integer> statups, int buffid, int skillId) {
    	MaplePacketWriter packet = new MaplePacketWriter(SendPacketOpcode.GIVE_FOREIGN_BUFF);
        packet.writeInt(cid);
        packet.writeLong(MapleBuffStat.MONSTER_RIDING.getValue());
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeZeroBytes(39); //v192 4byte. /144
        packet.writeInt(buffid); // 1902000 saddle
        packet.writeInt(skillId); // skillid
        packet.writeZeroBytes(7);
        return packet.getPacket();
    }

        public static byte[] givePirate(Map<MapleBuffStat, Integer> statups, int duration, int skillid) {
            return giveForeignPirate(statups, duration, -1, skillid);
        }

        public static byte[] giveForeignPirate(Map<MapleBuffStat, Integer> statups, int duration, int cid, int skillid) {
            final boolean infusion = skillid == 5121009 || skillid == 15111005;
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_FOREIGN_BUFF);
            mpw.writeInt(cid);
            PacketHelper.writeBuffMask(mpw, statups);
            mpw.writeShort(0);
            mpw.write(0);
            for (Integer stat : statups.values()) {
                mpw.writeInt(stat.intValue());
                mpw.writeLong(skillid);
                mpw.writeZeroBytes(infusion ? 6 : 1);
                mpw.writeShort(duration);//duration... seconds
            }
            mpw.writeShort(0);
            mpw.writeShort(0);
            mpw.write(1);
            mpw.write(1);
            return mpw.getPacket();
        }

        public static byte[] giveArcane(int skillid, Map<Integer, Integer> statups) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            PacketHelper.writeSingleMask(mpw, MapleBuffStat.MANY_USES);
            mpw.writeShort(statups.size());
            mpw.writeInt(skillid);
            mpw.writeInt(5000);
            mpw.writeShort(0);
            mpw.write(0);
            mpw.writeShort(0);
            mpw.writeShort(0);
            mpw.write(0);
            mpw.write(0);
            mpw.writeZeroBytes(9);
            return mpw.getPacket();
        }

        public static byte[] giveEnergyChargeTest(int bar, int bufflength) {
            return giveEnergyChargeTest(-1, bar, bufflength);
        }

        public static byte[] giveEnergyChargeTest(int cid, int bar, int bufflength) {
            if (true) {
                return CWvsContext.enableActions();
            }
            MaplePacketWriter mpw;

            if (cid == -1) {
            	mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            } else {
            	mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_FOREIGN_BUFF);
                mpw.writeInt(cid);
            }
            PacketHelper.writeSingleMask(mpw, MapleBuffStat.ENERGY_CHARGE);
            mpw.writeShort(0);
            mpw.write(0);
            mpw.writeInt(Math.min(bar, 10000));
            mpw.writeLong(0L);
            mpw.write(0);

            mpw.writeInt(bar >= 10000 ? bufflength : 0);
            mpw.write(0);
            mpw.write(6);
            return mpw.getPacket();
        }
        
        public static byte[] giveBuff(int buffid, int bufflength, Map<MapleBuffStat, Integer> statups, MapleStatEffect effect) {
        	MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
        	PacketHelper.writeBuffMask(mpw, statups);

            for (Map.Entry<MapleBuffStat, Integer> stat : statups.entrySet()) {
                if (!stat.getKey().canStack()) {
                    if (GameConstants.isSpecialBuff(buffid)) {
                        mpw.writeInt(stat.getValue());
                    } else {
                        mpw.writeShort(stat.getValue());
                    }
                    mpw.writeInt(buffid);
                    mpw.writeInt(bufflength);
                }
            }

            mpw.writeShort(0); // Size for a for loop
            mpw.write(0); // nDefenseAtt
            mpw.write(0); // nDefenseState
            mpw.write(0); // nPVPDamage
            
            if (buffid == Bishop.DIVINE_PROTECTION) {
            	mpw.write(1);
            } else if (buffid == FirePoisonMage.ELEMENTAL_ADAPATION || buffid == IceLightningMage.ELEMENTAL_ADAPTATION) {
            	mpw.write(0);
            }
            
            mpw.writeLong(0);
            
            for (Map.Entry<MapleBuffStat, Integer> stat : statups.entrySet()) {
                if (stat.getKey().canStack()) {
                    mpw.writeInt(1); // stacks size
                    mpw.writeInt(buffid);
                    mpw.writeInt(stat.getValue());
                    mpw.writeInt((int) (System.currentTimeMillis() % 1000000000)); // ?
                    mpw.writeInt(1); 
                    mpw.writeInt(bufflength);
                }
            }
            
            if (statups.containsKey(MapleBuffStat.MAPLE_WARRIOR) || statups.containsKey(MapleBuffStat.SPEED)) {
            	mpw.write(0);
            }
            if (statups.containsKey(MapleBuffStat.DARKSIGHT) || statups.containsKey(MapleBuffStat.ADVANCED_BLESSING)) {
            	mpw.writeInt(0);
            }
     
            mpw.writeShort(1); // Buff count. Used 1 as a placeholder for now.
            mpw.write(0); // nSubID
            mpw.write(0); // bJustBuffCheck
            mpw.write(0); // bFirstSet
            
            System.out.println(mpw.toString());
        	return mpw.getPacket();
        }
        
        
        /* Original
        public static byte[] giveBuff(int buffid, int bufflength, Map<MapleBuffStat, Integer> statups, MapleStatEffect effect) {
            MaplePacketLittleEndianWriter mpw = new MaplePacketLittleEndianWriter(SendPacketOpcode.GIVE_BUFF);
            
            PacketHelper.writeBuffMask(mpw, statups);//48
            boolean stacked = false;
            boolean isAura = false;
            boolean iscombo = false;
            
            for (Map.Entry<MapleBuffStat, Integer> stat : statups.entrySet()) {
                isAura = GameConstants.isAuraBuff(stat.getKey());
                if (stat.getKey() == MapleBuffStat.COMBO) {
                    iscombo = true;
                }
                if (!stat.getKey().canStack()) {
                    boolean specialBuff = GameConstants.isSpecialBuff(stat.getKey());
                    if (specialBuff) {
                        mpw.writeInt(stat.getValue());
                    } else {
                        mpw.writeShort(stat.getValue());
                    }
                    mpw.writeInt(buffid);
                    mpw.writeInt(bufflength);
                    if (stat.getKey() == MapleBuffStat.HOLY_SHIELD) {
                        mpw.writeInt(0);
                    }
                    if (stat.getKey() == MapleBuffStat.TEMPEST_BLADES) {
                        mpw.writeZeroBytes(5);
                        mpw.writeInt(buffid == 61101002 ? 1 : 2);
                        mpw.writeInt(buffid == 61101002 ? 3 : 5);
                        mpw.writeInt(effect.getWeapon()); //weapon
                        mpw.writeInt(buffid == 61101002 ? 3 : 5);
                        if (buffid == 61120007) {
                            mpw.writeZeroBytes(8);
                        }
                    }
                }
            }

            for (Map.Entry<MapleBuffStat, Integer> stat : statups.entrySet()) {
                if (stat.getKey().canStack()) {
                    if (!stacked) {
                        mpw.writeZeroBytes(5);
                        mpw.writeZeroBytes(4); //new v143?
                        if (GameConstants.isSpecialStackBuff(stat.getKey())) {
                            mpw.writeZeroBytes(1); //not sure where this part comes
                        }
                        stacked = true;
                    }
                    mpw.writeInt(1); //amount of the same buffstat
                    //for each of the same buffstats:
                    mpw.writeInt(buffid);
                    mpw.writeInt(stat);
		mpw.writeInt(Integer.MAX_VALUE); //some kind of time
                    mpw.writeInt(0);
                    mpw.writeInt(bufflength);
                    if (stat.getKey() == MapleBuffStat.DAMAGE_CAP_INCREASE) {
                        mpw.writeInt(1000);
                    }
                }
            }
            if (buffid == 24121004) {// Priere D'Aria
                mpw.writeZeroBytes(3);
                mpw.writeShort(0);
                mpw.write(0);
            }  
           if (buffid == 2321054) {
            mpw.writeInt(0);
            }
            if (buffid == 32001003 || buffid == 32120013 || buffid == 32101003 || buffid == 32120014 || buffid == 32111012 || buffid == 32120015 || buffid == 2221054 || buffid == 36121003 || buffid == 11101022 || buffid == 11111022 || buffid == 2311012 || buffid == 100001263 || buffid == 100001264) {
            mpw.write(1);
            }
            if (!isAura) {
                mpw.writeShort(0);
                if (effect != null) {
                    if (effect.isDivineShield()) {
                        mpw.writeInt(effect.getEnhancedWatk());
                    } else if (effect.getCharColor() > 0) {
                        mpw.writeInt(effect.getCharColor());
                    } else if (effect.isInflation()) {
                        mpw.writeInt(effect.getInflation());
                    }
                }
            }
            mpw.writeShort(0);
            if (buffid == 32110000 || buffid == 32111012 || buffid == 2221054 || buffid == 11101022 || buffid == 11111022 || buffid == 2311012) {
                mpw.write(1);
            }  else if (buffid == 27121005) {
            mpw.writeInt(effect.getX());
        }      if (buffid == 15001022 && effect.getY() > 0) {
            mpw.writeInt(effect.getY());
        }  
            
            //mpw.writeZeroBytes((buffid != 36111003 && buffid != 101120109 && buffid != 27121005) ? 3 : 0); // 197

        //       if (buffid == 31011001) {// Overload Release?? idk
        //          mpw.writeInt(effect.getDuration());
        //         JobPacket.AvengerPacket.cancelExceed();
        //        }
            if (buffid == 31211004) {// Recovery
                mpw.writeInt(effect.getDuration());
                JobPacket.AvengerPacket.cancelExceed();
                CWvsContext.enableActions();
            }
     /*       if (buffid == 27111004) {
                mpw.write(0);
                mpw.writeShort(1000);
                mpw.writeShort(0);
            }*/
        /*
            if (buffid == 27110007) {
                mpw.write(0);
                mpw.writeShort(25);
            }
            if (buffid == 27101202) {
                mpw.writeZeroBytes(10);
            }
            if (iscombo) {
                mpw.writeShort(258);
                mpw.writeShort(600);
            } else {
                mpw.write(0);
                mpw.write((effect != null) && (effect.isShadow()) ? 1 : 2);
            }
            if (isAura) {
                mpw.writeInt(0);
            }
            if ((statups.containsKey(MapleBuffStat.JUMP)) || (statups.containsKey(MapleBuffStat.SPEED)) || (statups.containsKey(MapleBuffStat.MORPH)) || (statups.containsKey(MapleBuffStat.GHOST_MORPH)) /*|| (statups.containsKey(MapleBuffStat.MAPLE_WARRIOR))*/ /*|| (statups.containsKey(MapleBuffStat.MONSTER_RIDING)) || (statups.containsKey(MapleBuffStat.DASH_SPEED)) || (statups.containsKey(MapleBuffStat.DASH_JUMP)) || (statups.containsKey(MapleBuffStat.SOARING)) || (statups.containsKey(MapleBuffStat.YELLOW_AURA)) || (statups.containsKey(MapleBuffStat.SNATCH)) || (statups.containsKey(MapleBuffStat.INDIE_SPEED)) || (statups.containsKey(MapleBuffStat.ANGEL_JUMP)) || (statups.containsKey(MapleBuffStat.ENERGY_CHARGE)) || (statups.containsKey(MapleBuffStat.MECH_CHANGE))) {
                mpw.write(4);
            }
            if (buffid == 23111004) {//ignis roar
                mpw.write(0);
                mpw.writeShort(1000);
            } else {
                mpw.writeShort(0);

            }
            if (statups.containsKey(MapleBuffStat.MAPLE_WARRIOR)) {
                mpw.write(HexTool.getByteArrayFromHexString("00 E8 03 00 00 00 13 00 00 00 00"));
                return mpw.getPacket();
            }
            if (statups.containsKey(MapleBuffStat.KAISER_COMBO)) { //this too
                mpw.writeZeroBytes(8);
            }
            mpw.writeZeroBytes(69); //make sure no dc incase not enough length
            
            //new v142
            mpw.writeShort(1);
            mpw.write(0);
            mpw.write(0);
            mpw.write(0);
            System.out.println("sent Buff with buffid: " + buffid +"  as packet: "+ mpw.toString());
            return mpw.getPacket();
        } */
        
        public static byte[] giveDebuff(MapleDisease statups, int x, int skillid, int level, int duration) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            PacketHelper.writeSingleMask(mpw, statups);
            mpw.writeShort(x);
            mpw.writeShort(skillid);
            mpw.writeShort(level);
            mpw.writeInt(duration);
            mpw.writeShort(0);
            mpw.writeShort(0);
            //mpw.write(1);
            mpw.write(0);
            //mpw.write(1);
            mpw.writeZeroBytes(30);
            //System.out.println(HexTool.toString(mpw.getPacket()));
            return mpw.getPacket();
        }

   /*     public static byte[] cancelBuff(List<MapleBuffStat> statups) {
            MaplePacketLittleEndianWriter mpw = new MaplePacketLittleEndianWriter();

            mpw.writeShort(SendPacketOpcode.CANCEL_BUFF);

            PacketHelper.writeMask(mpw, statups);
            for (MapleBuffStat z : statups) {
                if (z.canStack()) {
                    mpw.writeInt(0); //amount of buffs still in the stack? dunno mans
                }
            }
            mpw.write(3);
            mpw.write(1);
            mpw.writeLong(0);
            mpw.writeLong(0);
            mpw.writeLong(0);
            mpw.write(0);
            return mpw.getPacket();
        }*/
        
        public static byte[] cancelBuff(List<MapleBuffStat> statups) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANCEL_BUFF);

            PacketHelper.writeMask(mpw, statups);
            for (MapleBuffStat stat : statups) {
                if (stat.canStack()) {
                    mpw.writeInt(0);
                }
            }
            /*
            if ((statups.contains(MapleBuffStat.JUMP)) || (statups.contains(MapleBuffStat.SPEED))) {
            	mpw.write(1);
            }
            if ((statups.contains(MapleBuffStat.HOLY_SHIELD)) || (statups.contains(MapleBuffStat.MP_BOOST))) {
            	mpw.writeLong(0);
            }
            if ((statups.contains(MapleBuffStat.MAPLE_WARRIOR))) {
            	mpw.write(0);
            }
            */
            mpw.writeLong(0);
            mpw.writeLong(0);
            
            System.out.println(mpw.toString());
            return mpw.getPacket();
        }

        public static byte[] cancelDebuff(MapleDisease mask) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANCEL_BUFF);

            PacketHelper.writeSingleMask(mpw, mask);
            mpw.write(3);
            mpw.write(1);
            mpw.writeLong(0);
            mpw.write(0);//v112
            return mpw.getPacket();
        }

        public static byte[] cancelHoming() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANCEL_BUFF);

            PacketHelper.writeSingleMask(mpw, MapleBuffStat.HOMING_BEACON);
            mpw.write(0);//v112

            return mpw.getPacket();
        }
        
        public static byte[] giveAriaBuff(Map<MapleBuffStat, Integer> statups, int bufflevel, int buffid, int bufflength) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            PacketHelper.writeBuffMask(mpw, statups);
            // mpw.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 80 00 00 00 00 00 00 00 00 00 00 40 00 00 00 00 00 00 00 00 00 00 00 00 00"));         
            for (Map.Entry stat : statups.entrySet()) {
                mpw.writeShort(((Integer) stat.getValue()).intValue());
                mpw.writeInt(buffid);
                mpw.writeInt(bufflength);
            }
            mpw.writeZeroBytes(3);
            mpw.writeShort(0); // not sure..
            mpw.write(0);
            mpw.writeShort(0);
            return mpw.getPacket();
        }
        
     public static byte[] giveForeignBuff(int cid, Map<MapleBuffStat, Integer> statups, MapleStatEffect effect) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_FOREIGN_BUFF);
		mpw.writeInt(cid);
        PacketHelper.writeBuffMask(mpw, statups);
        for (Entry<MapleBuffStat, Integer> statup : statups.entrySet()) {
            if (statup.getKey() == MapleBuffStat.SHADOWPARTNER || statup.getKey() == MapleBuffStat.MECH_CHANGE || statup.getKey() == MapleBuffStat.DARK_AURA || statup.getKey() == MapleBuffStat.YELLOW_AURA || statup.getKey() == MapleBuffStat.BLUE_AURA || statup.getKey() == MapleBuffStat.GIANT_POTION || statup.getKey() == MapleBuffStat.SPIRIT_LINK || statup.getKey() == MapleBuffStat.PYRAMID_PQ || statup.getKey() == MapleBuffStat.WK_CHARGE || statup.getKey() == MapleBuffStat.DAMAGE_R || statup.getKey() == MapleBuffStat.MORPH || statup.getKey() == MapleBuffStat.WATER_SHIELD || statup.getKey() == MapleBuffStat.DARK_METAMORPHOSIS) {
                mpw.writeShort(statup.getValue().shortValue());
                mpw.writeInt(effect.isSkill() ? effect.getSourceId() : -effect.getSourceId());
            } else if (statup.getKey() == MapleBuffStat.FAMILIAR_SHADOW) {
                mpw.writeInt(statup.getValue());
                mpw.writeInt(effect.getCharColor());
            } else {
                mpw.writeShort(statup.getValue().shortValue());
            }
        }
        mpw.writeShort(0);
        mpw.write(0);
        if (effect.getSourceId() == 13101024) {
            mpw.writeLong(0);
            mpw.writeLong(0);
            mpw.writeLong(0);
            mpw.writeZeroBytes(6);
            mpw.write(1);
            mpw.writeZeroBytes(22);
        } else if (effect.getSourceId() == 4001003) { // Dark Sight
            mpw.writeLong(0);
            mpw.writeLong(0);
            mpw.writeZeroBytes(4);
        } else if (/*effect.getSourceId() == 1101013 || effect.getSourceId() == 1120003 ||*/ effect.getSourceId() == 11111001 || effect.getSourceId() == 11110005) { 
            mpw.writeLong(1);
            mpw.writeLong(0);
            mpw.writeZeroBytes(4);
         } else if (effect.getSourceId() == 15001004) {
            mpw.writeInt(0);
            mpw.write(0);
            mpw.writeShort(23);
            mpw.writeShort(20);
            mpw.write(0);
            mpw.write(HexTool.getByteArrayFromHexString("AB E5 E4 00"));
            mpw.writeInt(0);
            mpw.write(0);
            mpw.writeShort(23);
            mpw.writeShort(0);
        } else if (effect.getSourceId() == 61120008 || effect.getSourceId() == 61111008 || effect.getSourceId() == 61121053) {// KAISER BUFFS!
           /* mpw.writeLong(0); // old kaiser
            mpw.writeLong(0);
            mpw.writeZeroBytes(5);*/
            mpw.writeInt(2);
            mpw.writeZeroBytes(13);
            mpw.writeShort(600);
            mpw.writeZeroBytes(20);//ourstory method
        } else if (effect.getSourceId() == 21101006) {
            mpw.writeShort(0);
            mpw.write(7);
            mpw.writeLong(0);
            mpw.writeLong(0);
            mpw.write(208);
            mpw.write(2);
        }/* else if (effect.getSourceId() == 3101004 || effect.getSourceId() == 3201004 || effect.getSourceId() == 13101003 || effect.getSourceId() == 33101003) {
            mpw.writeLong(0);
            mpw.writeLong(0);
        }*/ else if (effect.getSourceId() == 30001001 || effect.getSourceId() == 30011001 || effect.getSourceId() == 2311009) {
            mpw.writeLong(0);
            mpw.writeLong(0);
            mpw.write(0);
        } else if (effect.getSourceId() == 1221004 || effect.getSourceId() == 1211006 || effect.getSourceId() == 1211008 || effect.getSourceId() == 1211004) {
            mpw.writeShort(0);
            mpw.writeLong(4);
            mpw.writeLong(0);
            mpw.write(0);
            mpw.writeShort(602);
        } else if (effect.getSourceId() == 32120000 || effect.getSourceId() == 32001003 || effect.getSourceId() == 32110000 || effect.getSourceId() == 32111012 || effect.getSourceId() == 32120001 || effect.getSourceId() == 32101003) { //ì˜¤ë�¼
            mpw.writeLong(0);
            mpw.writeLong(0);
            mpw.writeZeroBytes(5);
        } else {
            mpw.writeLong(0);
            mpw.writeLong(0);
            mpw.writeZeroBytes(6);
        }
        System.out.println("Sent foreign Efftect: "+effect.getSourceId()+" as packet: "+mpw.toString());
        return mpw.getPacket();
    }

        public static byte[] giveForeignDebuff(int cid, final MapleDisease statups, int skillid, int level, int x) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_FOREIGN_BUFF);
		mpw.writeInt(cid);

            PacketHelper.writeSingleMask(mpw, statups);
            if (skillid == 125) {
                mpw.writeShort(0);
                mpw.write(0); //todo test
            }
            mpw.writeShort(x);
            mpw.writeShort(skillid);
            mpw.writeShort(level);
            mpw.writeShort(0); // same as give_buff
            mpw.writeShort(0); //Delay
            mpw.write(1);
            mpw.write(1);
            mpw.write(0);//v112
            mpw.writeZeroBytes(20);
            return mpw.getPacket();
        }
        
        public static byte[] cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANCEL_FOREIGN_BUFF);
		mpw.writeInt(cid);
            PacketHelper.writeMask(mpw, statups);
            //mpw.write(3);
            mpw.write(1);
            //mpw.write(0);
            //mpw.writeZeroBytes(20);

            return mpw.getPacket();
        }
        
        public static byte[] cancelForeignRiding(int cid, List<MapleBuffStat> statups) {
        MaplePacketWriter packet = new MaplePacketWriter(SendPacketOpcode.CANCEL_FOREIGN_BUFF);
        packet.writeInt(cid);
        packet.writeLong(MapleBuffStat.MONSTER_RIDING.getValue());
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0); // v181
        packet.write(1);
        return packet.getPacket();
    }

        public static byte[] cancelForeignDebuff(int cid, MapleDisease mask) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANCEL_FOREIGN_BUFF);
		mpw.writeInt(cid);

            PacketHelper.writeSingleMask(mpw, mask);//48 bytes
            //mpw.write(3);
            mpw.write(1);
            //mpw.write(0);//v112
            return mpw.getPacket();
        }

        public static byte[] giveCard(int cid, int oid, int skillid) {
            MaplePacketWriter writer = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
            writer.write(0);
            writer.writeInt(cid);
            writer.writeInt(1);
            writer.writeInt(oid);
            writer.writeInt(skillid);
            writer.write(1);
            writer.writeInt(2);
            writer.writeInt(1);
            writer.writeInt(21);
            writer.writeInt(8);
            writer.writeInt(8);
            writer.write(0);
            return writer.getPacket();
        }
    }

    public static class InventoryPacket {

        public static byte[] addInventorySlot(MapleInventoryType type, Item item) {
            return addInventorySlot(type, item, false);
        }

        public static byte[] addInventorySlot(MapleInventoryType type, Item item, boolean fromDrop) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(fromDrop ? 1 : 0);
            mpw.write(1);
            mpw.write(0);

            mpw.write(GameConstants.isInBag(item.getPosition(), type.getType()) ? 9 : 0);
            mpw.write(type.getType());
            mpw.writeShort(item.getPosition());
            PacketHelper.addItemInfo(mpw, item);
            return mpw.getPacket();
        }

        public static byte[] updateInventorySlot(MapleInventoryType type, Item item, boolean fromDrop) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(fromDrop ? 1 : 0);
            mpw.write(1);
            mpw.write(0);

            mpw.write(GameConstants.isInBag(item.getPosition(), type.getType()) ? 6 : 1);
            mpw.write(type.getType());
            mpw.writeShort(item.getPosition());
            mpw.writeShort(item.getQuantity());

            return mpw.getPacket();
        }

        public static byte[] moveInventoryItem(MapleInventoryType type, short src, short dst, boolean bag, boolean bothBag) {
            return moveInventoryItem(type, src, dst, (byte) -1, bag, bothBag);
        }

        public static byte[] moveInventoryItem(MapleInventoryType type, short src, short dst, short equipIndicator, boolean bag, boolean bothBag) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(1);
            mpw.write(1);
            mpw.write(0);

            mpw.write(bag ? 5 : bothBag ? 8 : 2);
            mpw.write(type.getType());
            mpw.writeShort(src);
            mpw.writeShort(dst);
            if (bag) {
                mpw.writeShort(0);
            }
            if (equipIndicator != -1) {
                mpw.write(equipIndicator);
            }

            return mpw.getPacket();
        }

        public static byte[] moveAndMergeInventoryItem(MapleInventoryType type, short src, short dst, short total, boolean bag, boolean switchSrcDst, boolean bothBag) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(1);
            mpw.write(2);
            mpw.write(0);

            mpw.write((bag) && ((switchSrcDst) || (bothBag)) ? 7 : 3);
            mpw.write(type.getType());
            mpw.writeShort(src);

            mpw.write((bag) && ((!switchSrcDst) || (bothBag)) ? 6 : 1);
            mpw.write(type.getType());
            mpw.writeShort(dst);
            mpw.writeShort(total);

            return mpw.getPacket();
        }

        public static byte[] moveAndMergeWithRestInventoryItem(MapleInventoryType type, short src, short dst, short srcQ, short dstQ, boolean bag, boolean switchSrcDst, boolean bothBag) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(1);
            mpw.write(2);
            mpw.write(0);

            mpw.write((bag) && ((switchSrcDst) || (bothBag)) ? 6 : 1);
            mpw.write(type.getType());
            mpw.writeShort(src);
            mpw.writeShort(srcQ);

            mpw.write((bag) && ((!switchSrcDst) || (bothBag)) ? 6 : 1);
            mpw.write(type.getType());
            mpw.writeShort(dst);
            mpw.writeShort(dstQ);

            return mpw.getPacket();
        }

        public static byte[] clearInventoryItem(MapleInventoryType type, short slot, boolean fromDrop) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(fromDrop ? 1 : 0);
            mpw.write(1);
            mpw.write(0);

            mpw.write((slot > 100) && (type == MapleInventoryType.ETC) ? 7 : 3);
            mpw.write(type.getType());
            mpw.writeShort(slot);

            return mpw.getPacket();
        }

        public static byte[] updateSpecialItemUse(Item item, byte invType, MapleCharacter chr) {
            return updateSpecialItemUse(item, invType, item.getPosition(), false, chr);
        }

        public static byte[] updateSpecialItemUse(Item item, byte invType, short pos, boolean theShort, MapleCharacter chr) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(0);
            mpw.write(2);
            mpw.write(0);

            mpw.write(GameConstants.isInBag(pos, invType) ? 7 : 3);
            mpw.write(invType);
            mpw.writeShort(pos);

            mpw.write(0);
            mpw.write(invType);
            if ((item.getType() == 1) || (theShort)) {
                mpw.writeShort(pos);
            } else {
                mpw.write(pos);
            }
            PacketHelper.addItemInfo(mpw, item, chr);
            if (pos < 0) {
                mpw.write(2);
            }

            return mpw.getPacket();
        }

        public static byte[] updateSpecialItemUse_(Item item, byte invType, MapleCharacter chr) {
            return updateSpecialItemUse_(item, invType, item.getPosition(), chr);
        }

        public static byte[] updateSpecialItemUse_(Item item, byte invType, short pos, MapleCharacter chr) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(0);
            mpw.write(1);
            mpw.write(0);

            mpw.write(0);
            mpw.write(invType);
            if (item.getType() == 1) {
                mpw.writeShort(pos);
            } else {
                mpw.write(pos);
            }
            PacketHelper.addItemInfo(mpw, item, chr);
            if (pos < 0) {
                mpw.write(1);
            }

            return mpw.getPacket();
        }

        public static byte[] updateEquippedItem(MapleCharacter chr, Equip eq, short pos) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(0);
            mpw.write(1);
            mpw.write(0);

            mpw.write(0);
            mpw.write(1);
            mpw.writeShort(pos);
            PacketHelper.addItemInfo(mpw, eq, chr);

            return mpw.getPacket();
        }

        public static byte[] scrolledItem(Item scroll, MapleInventoryType inv, Item item, boolean destroyed, boolean potential, boolean equipped) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(1);
            mpw.write(destroyed ? 2 : 3);
            mpw.write(0);

            mpw.write(scroll.getQuantity() > 0 ? 1 : 3);
            mpw.write(GameConstants.getInventoryType(scroll.getItemId()).getType());
            mpw.writeShort(scroll.getPosition());
            if (scroll.getQuantity() > 0) {
                mpw.writeShort(scroll.getQuantity());
            }

            mpw.write(3);
            mpw.write(inv.getType());
            mpw.writeShort(item.getPosition());
            if (!destroyed) {
                mpw.write(0);
                mpw.write(inv.getType());
                mpw.writeShort(item.getPosition());
                PacketHelper.addItemInfo(mpw, item);
            }
            if (!potential) {
                mpw.write(1);
            }
            if (equipped) {
                mpw.write(8);
            }

            return mpw.getPacket();
        }

        public static byte[] moveAndUpgradeItem(MapleInventoryType type, Item item, short oldpos, short newpos, MapleCharacter chr) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(1);
            mpw.write(3);
            mpw.write(0);

            mpw.write(GameConstants.isInBag(newpos, type.getType()) ? 7 : 3);
            mpw.write(type.getType());
            mpw.writeShort(oldpos);

            mpw.write(0);
            mpw.write(1);
            mpw.writeShort(oldpos);
            PacketHelper.addItemInfo(mpw, item, chr);

            mpw.write(2);
            mpw.write(type.getType());
            mpw.writeShort(oldpos);
            mpw.writeShort(newpos);
            mpw.write(0);

            return mpw.getPacket();
        }

        public static byte[] dropInventoryItem(MapleInventoryType type, short src) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(1);
            mpw.write(1);
            mpw.write(0);

            mpw.write(3);
            mpw.write(type.getType());
            mpw.writeShort(src);
            if (src < 0) {
                mpw.write(1);
            }

            return mpw.getPacket();
        }

        public static byte[] dropInventoryItemUpdate(MapleInventoryType type, Item item) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(1);
            mpw.write(1);
            mpw.write(0);

            mpw.write(1);
            mpw.write(type.getType());
            mpw.writeShort(item.getPosition());
            mpw.writeShort(item.getQuantity());

            return mpw.getPacket();
        }

        public static byte[] getInventoryFull() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(1);
            mpw.write(0);
            mpw.write(0);

            return mpw.getPacket();
        }

        public static byte[] getInventoryStatus() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_OPERATION);
            mpw.write(0);
            mpw.write(0);
            mpw.write(0);

            return mpw.getPacket();
        }

        public static byte[] getSlotUpdate(byte invType, byte newSlots) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INVENTORY_GROW);
            mpw.write(invType);
            mpw.write(newSlots);

            return mpw.getPacket();
        }

        public static byte[] getShowInventoryFull() {
            return CWvsContext.InfoPacket.getShowInventoryStatus(255);
        }

        public static byte[] showItemUnavailable() {
            return CWvsContext.InfoPacket.getShowInventoryStatus(254);
        }
    }

    public static byte[] updateHyperSp(int mode, int remainSp) {
        return updateSpecialStat("hyper", 0x1C, mode, remainSp);
    }

    public static byte[] updateSpecialStat(String stat, int array, int mode, int amount) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPECIAL_STAT);
		mpw.writeMapleAsciiString(stat);
        mpw.writeInt(array);
        mpw.writeInt(mode);
        mpw.write(1);
        mpw.writeInt(amount);

        return mpw.getPacket();
    }

    public static byte[] updateMaplePoint(int mp) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MAPLE_POINT);
		mpw.writeInt(mp);

        return mpw.getPacket();
    }

    public static byte[] updateCrowns(int[] titles) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EVENT_CROWN);
        for (int i = 0; i < 5; i++) {
            mpw.writeMapleAsciiString("");
            if (titles.length < i + 1) {
                mpw.write(-1);
            } else {
                mpw.write(titles[i]);
            }
        }

        return mpw.getPacket();
    }

    public static byte[] magicWheel(int type, List<Integer> items, String data, int endSlot) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MAGIC_WHEEL);
		mpw.write(type);
        switch (type) {
            case 3:
                mpw.write(items.size());
                for (int item : items) {
                    mpw.writeInt(item);
                }
                mpw.writeMapleAsciiString(data); // nexon encrypt the item and then send the string
                mpw.write(endSlot);
                break;
            case 5:
                //<Character Name> got <Item Name>.
                break;
            case 6:
                //You don't have a Magic Gachapon Wheel in your Inventory.
                break;
            case 7:
                //You don't have any Inventory Space.\r\n You must have 2 or more slots available\r\n in each of your tabs.
                break;
            case 8:
                //Please try this again later.
                break;
            case 9:
                //Failed to delete Magic Gachapon Wheel item.
                break;
            case 0xA:
                //Failed to receive Magic Gachapon Wheel item.
                break;
            case 0xB:
                //You cannot move while Magic Wheel window is open.
                break;
        }

        return mpw.getPacket();
    }

    public static class Reward {

        public static byte[] receiveReward(int id, byte mode, int quantity) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REWARD);
		mpw.write(mode); // mode
            switch (mode) { // mode
                case 9:
                    mpw.writeInt(0);
                    break;
                case 0x0B:
                    mpw.writeInt(id);
                    mpw.writeInt(quantity); //quantity
                    //Popup: You have received the Maple Points.\r\n( %d maple point )
                    break;
                case 0x0C:
                    mpw.writeInt(id);
                    //Popup You have received the Game item.
                    break;
                case 0x0E:
                    mpw.writeInt(id);
                    mpw.writeInt(quantity); //quantity
                    //Popup: You have received the Mesos.\r\n( %d meso )
                    break;
                case 0x0F:
                    mpw.writeInt(id);
                    mpw.writeInt(quantity); //quantity
                    //Popup: You have received the Exp.\r\n( %d exp )
                    break;
                case 0x14:
                    //Popup: Failed to receive the Maple Points.
                    break;
                case 0x15:
                    mpw.write(0);
                    //Popup: Failed to receive the Game Item.
                    break;
                case 0x16:
                    mpw.write(0);
                    //Popup: Failed to receive the Game Item.
                    break;
                case 0x17:
                    //Popup: Failed to receive the Mesos.
                    break;
                case 0x18:
                    //Popup: Failed to receive the Exp.
                    break;
                case 0x21:
                    mpw.write(0); //66
                    //No inventory space
                    break;
            }

            return mpw.getPacket();
        }

        public static byte[] updateReward(int id, byte mode, List<MapleReward> rewards, int option) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REWARD);
		mpw.write(mode); // mode
            switch (mode) { // mode
                case 9:
                    mpw.writeInt(rewards.size());
                    if (rewards.size() > 0) {
                        for (int i = 0; i < rewards.size(); i++) {
                            MapleReward reward = rewards.get(i);
                            boolean empty = reward.getId() < 1;
                            mpw.writeInt(empty ? 0 : reward.getId()); // 0 = blank 1+ = gift
                            if (!empty) {
                                if ((option & 1) != 0) {
                                    mpw.writeLong(reward.getReceiveDate()); //start time
                                    mpw.writeLong(reward.getExpireDate()); //end time
                                    mpw.writeLong(reward.getReceiveDate()); //start time
                                    mpw.writeLong(reward.getExpireDate()); //end time
                                }
                                if ((option & 2) != 0) { //nexon do here a3 & 2 when a3 is 9
                                    mpw.writeInt(0);
                                    mpw.writeInt(0);
                                    mpw.writeInt(0);
                                    mpw.writeInt(0);
                                    mpw.writeInt(0);
                                    mpw.writeInt(0);
                                    mpw.writeMapleAsciiString("");
                                    mpw.writeMapleAsciiString("");
                                    mpw.writeMapleAsciiString("");
                                }
                                mpw.writeInt(reward.getType()); //type 3 = maple point 4 = mesos 5 = exp
                                mpw.writeInt(reward.getItem()); // item id
                                mpw.writeInt(/*itemQ*/reward.getItem() > 0 ? 1 : 0); // item quantity (?)
                                mpw.writeInt(0);
                                mpw.writeLong(0L);
                                mpw.writeInt(0);
                                mpw.writeInt(reward.getMaplePoints()); // maple point amount
                                mpw.writeInt(reward.getMeso()); // mesos amount
                                mpw.writeInt(reward.getExp()); // exp amount
                                mpw.writeInt(0);
                                mpw.writeInt(0);
                                mpw.writeMapleAsciiString("");
                                mpw.writeMapleAsciiString("");
                                mpw.writeMapleAsciiString("");
                                mpw.writeMapleAsciiString(reward.getDesc());
                            }
                        }
                    }
                    break;
                case 0x0B:
                    mpw.writeInt(id);
                    mpw.writeInt(0); //quantity
                    //Popup: You have received the Maple Points.\r\n( %d maple point )
                    break;
                case 0x0C:
                    mpw.writeInt(id);
                    //Popup You have received the Game item.
                    break;
                case 0x0E:
                    mpw.writeInt(id);
                    mpw.writeInt(0); //quantity
                    //Popup: You have received the Mesos.\r\n( %d meso )
                    break;
                case 0x0F:
                    mpw.writeInt(id);
                    mpw.writeInt(0); //quantity
                    //Popup: You have received the Exp.\r\n( %d exp )
                    break;
                case 0x14:
                    //Popup: Failed to receive the Maple Points.
                    break;
                case 0x15:
                    mpw.write(0);
                    //Popup: Failed to receive the Game Item.
                    break;
                case 0x16:
                    mpw.write(0);
                    //Popup: Failed to receive the Game Item.
                    break;
                case 0x17:
                    //Popup: Failed to receive the Mesos.
                    break;
                case 0x18:
                    //Popup: Failed to receive the Exp.
                    break;
                case 0x21:
                    mpw.write(0); //66
                    //No inventory space
                    break;
            }

            return mpw.getPacket();
        }
    }
}