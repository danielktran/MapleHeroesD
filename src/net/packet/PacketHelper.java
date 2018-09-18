package net.packet;

import client.*;
import client.character.MapleCharacter;
import client.inventory.*;
import constants.GameConstants;
import constants.ServerConstants;
import net.Buffstat;
import net.world.MapleCharacterLook;

import java.util.*;
import java.util.Map.Entry;
import server.CashItem;
import server.MapleItemInformationProvider;
import server.shops.MapleShop;
import server.shops.MapleShopItem;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.stores.AbstractPlayerStore;
import server.stores.IMaplePlayerShop;
import tools.BitTools;
import tools.HexTool;
import tools.KoreanDateUtil;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.data.MaplePacketWriter;

public class PacketHelper {

    public static final long FT_UT_OFFSET = 116444592000000000L;
    public static final long MAX_TIME = 150842304000000000L;
    public static final long ZERO_TIME = 94354848000000000L;
    public static final long PERMANENT = 150841440000000000L;

    public static long getKoreanTimestamp(long realTimestamp) {
        return getTime(realTimestamp);
    }

    public static long getTime(long realTimestamp) {
        if (realTimestamp == -1L) { // 00 80 05 BB 46 E6 17 02, 1/1/2079
            return MAX_TIME;
        }
        if (realTimestamp == -2L) { // 00 40 E0 FD 3B 37 4F 01, 1/1/1900
            return ZERO_TIME;
        }
        if (realTimestamp == -3L) {
            return PERMANENT;
        }
        return realTimestamp * 10000L + 116444592000000000L;
    }

    public static long decodeTime(long fakeTimestamp) {
        if (fakeTimestamp == 150842304000000000L) {
            return -1L;
        }
        if (fakeTimestamp == 94354848000000000L) {
            return -2L;
        }
        if (fakeTimestamp == 150841440000000000L) {
            return -3L;
        }
        return (fakeTimestamp - 116444592000000000L) / 10000L;
    }

    public static long getFileTimestamp(long timeStampinMillis, boolean roundToMinutes) {
        if (SimpleTimeZone.getDefault().inDaylightTime(new Date())) {
            timeStampinMillis -= 3600000L;
        }
        long time;

        if (roundToMinutes) {
            time = timeStampinMillis / 1000L / 60L * 600000000L;
        } else {
            time = timeStampinMillis * 10000L;
        }
        return time + 116444592000000000L;
    }

    public static void addImageInfo(MaplePacketWriter mpw, byte[] image) {
        mpw.writeInt(image.length);
        mpw.write(image);
    }

    public static void addStartedQuestInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        mpw.write(1);
        final List<MapleQuestStatus> started = chr.getStartedQuests();
        mpw.writeShort(started.size());
        for (MapleQuestStatus q : started) {
            mpw.writeInt(q.getQuest().getId());
            if (q.hasMobKills()) {
                StringBuilder sb = new StringBuilder();
                for (Iterator i$ = q.getMobKills().values().iterator(); i$.hasNext();) {
                    int kills = ((Integer) i$.next()).intValue();
                    sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
                }
                mpw.writeMapleAsciiString(sb.toString());
            } else {
                mpw.writeMapleAsciiString(q.getCustomData() == null ? "" : q.getCustomData());
            }
        }
        addNXQuestInfo(mpw, chr);
    }

    public static void addNXQuestInfo(MaplePacketWriter mpw, MapleCharacter chr) {
    	mpw.writeShort(0);
        /*
         mpw.writeShort(7);
         mpw.writeMapleAsciiString("1NX5211068");
         mpw.writeMapleAsciiString("1");
         mpw.writeMapleAsciiString("SE20130619");
         mpw.writeMapleAsciiString("20130626060823");
         mpw.writeMapleAsciiString("99NX5533018");
         mpw.writeMapleAsciiString("1");
         mpw.writeMapleAsciiString("1NX1003792");
         mpw.writeMapleAsciiString("1");
         mpw.writeMapleAsciiString("1NX1702337");
         mpw.writeMapleAsciiString("1");
         mpw.writeMapleAsciiString("1NX9102857");
         mpw.writeMapleAsciiString("1");
         mpw.writeMapleAsciiString("SE20130116");
         mpw.writeMapleAsciiString("1");
         */
    }

    public static void addCompletedQuestInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        mpw.write(1);
        final List<MapleQuestStatus> completed = chr.getCompletedQuests();
        mpw.writeShort(completed.size());
        for (MapleQuestStatus q : completed) {
            mpw.writeInt(q.getQuest().getId());
            mpw.writeInt(KoreanDateUtil.getQuestTimestamp(q.getCompletionTime()));
            //v139 changed from long to int
        }
    }

    public static void addSkillInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        mpw.write(1);
        mpw.writeShort(0); // Skills Count
        /*MaplePacketLittleEndianWriter mplew1 =  new MaplePacketLittleEndianWriter();
         final Map<Skill, SkillEntry> skills = chr.getSkills();
         mplew1.write(1);
         int hyper = 0;
         //for (Skill skill : skills.keySet()) {
         //    if (skill.isHyper()) hyper++;
         //}
         mplew1.writeShort(skills.size() - hyper);
         boolean follow = false;
        
         for (Map.Entry<Skill, SkillEntry> skill : skills.entrySet()) {
         //if (((Skill) skill.getKey()).isHyper()) continue;
            
         if (follow) {
         follow = false;
         if (!GameConstants.isHyperSkill((Skill) skill.getKey()))
         mplew1.writeInt(skill.getKey().getId());
         }
         mplew1.writeInt(skill.getKey().getId());
         mplew1.writeInt(((SkillEntry) skill.getValue()).skillevel);
         addExpirationTime(mplew1, ((SkillEntry) skill.getValue()).expiration);

         if (GameConstants.isHyperSkill((Skill) skill.getKey())) {
         // mplew1.writeInt(1110009);
         follow = true;
         } else if (((Skill) skill.getKey()).isFourthJob()) {
         mplew1.writeInt(((SkillEntry) skill.getValue()).masterlevel);
         }
         //  addSingleSkill(mpw, skill.getKey(), skill.getValue());
         }
         mpw.write(mplew1.getPacket());
         System.out.println(HexTool.toString(mplew1.getPacket()));
         */
    }

//    public static void addSingleSkill(MaplePacketLittleEndianWriter mpw, Skill skill, SkillEntry ske) {
//        try {
//            // if (skill.getId() != 1001008) return;
//
//            MaplePacketLittleEndianWriter mplew1 = new MaplePacketLittleEndianWriter();
//
//            mplew1.writeInt(skill.getId());
//            mplew1.writeInt(ske.skillevel);
//            addExpirationTime(mplew1, ske.expiration);
//
//            if (GameConstants.isHyperSkill(skill)) {
//                //System.out.println("HYPER: " + ((Skill) skill.getKey()).getId());
//                mplew1.writeInt(0);
//            } else if (((Skill) skill).isFourthJob()) {
//                mplew1.writeInt(((SkillEntry) ske).masterlevel);
//            }
//            if (skill.getId() == 1001008) {
//                System.out.println(HexTool.toString(mplew1.getPacket()));
//            }
//            mpw.write(mplew1.getPacket());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
    public static void addCoolDownInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        final List<MapleCoolDownValueHolder> cd = chr.getCooldowns();

        mpw.writeShort(cd.size());
        for (MapleCoolDownValueHolder cooling : cd) {
            mpw.writeInt(cooling.skillId);
            mpw.writeInt((int) (cooling.length + cooling.startTime - System.currentTimeMillis()) / 1000);
        }
        if (cd.isEmpty()) {
            mpw.writeShort(0);
        }
    }

    public static void addRocksInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        int[] mapz = chr.getRegRocks();
        for (int i = 0; i < 5; i++) {
            mpw.writeInt(mapz[i]);
        }

        int[] map = chr.getRocks();
        for (int i = 0; i < 10; i++) {
            mpw.writeInt(map[i]);
        }

        int[] maps = chr.getHyperRocks();
        for (int i = 0; i < 13; i++) {
            mpw.writeInt(maps[i]);
        }
        for (int i = 0; i < 13; i++) {
            mpw.writeInt(maps[i]);
        }
    }

    public static void addMiniGameRecordsInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        short size = 0;
        mpw.writeShort(size);
        for (int i = 0; i < size; i++) {
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
        }
    }

    public static void addRingInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> aRing = chr.getRings(true);
        List<MapleRing> cRing = aRing.getLeft();
        mpw.writeShort(cRing.size());
        for (MapleRing ring : cRing) {
            mpw.writeInt(ring.getPartnerChrId());
            mpw.writeAsciiString(ring.getPartnerName(), 13);
            mpw.writeLong(ring.getRingId());
            mpw.writeLong(ring.getPartnerRingId());
        }
        List<MapleRing> fRing = aRing.getMid();
        mpw.writeShort(fRing.size());
        for (MapleRing ring : fRing) {
            mpw.writeInt(ring.getPartnerChrId());
            mpw.writeAsciiString(ring.getPartnerName(), 13);
            mpw.writeLong(ring.getRingId());
            mpw.writeLong(ring.getPartnerRingId());
            mpw.writeInt(ring.getItemId());
        }
        List<MapleRing> mRing = aRing.getRight();
        mpw.writeShort(mRing.size());
        int marriageId = 30000;
        for (MapleRing ring : mRing) {
            mpw.writeInt(marriageId);
            mpw.writeInt(chr.getID());
            mpw.writeInt(ring.getPartnerChrId());
            mpw.writeShort(3);
            mpw.writeInt(ring.getItemId());
            mpw.writeInt(ring.getItemId());
            mpw.writeAsciiString(chr.getName(), 13);
            mpw.writeAsciiString(ring.getPartnerName(), 13);
        }
    }

    public static void addMoneyInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        mpw.writeLong(chr.getMeso()); //changed to long v139
    }

    public static void addInventoryInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        mpw.writeInt(0);
        addPotionPotInfo(mpw, chr);
        //RED stuff:
        mpw.writeInt(0);
        mpw.writeInt(chr.getID());
        mpw.writeInt(0); 
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);

        mpw.writeInt(0);

        mpw.write(0);
        mpw.write(0);
        mpw.write(0);

        mpw.write(chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit());
        mpw.write(chr.getInventory(MapleInventoryType.USE).getSlotLimit());
        mpw.write(chr.getInventory(MapleInventoryType.SETUP).getSlotLimit());
        mpw.write(chr.getInventory(MapleInventoryType.ETC).getSlotLimit());
        mpw.write(chr.getInventory(MapleInventoryType.CASH).getSlotLimit());

        MapleQuestStatus stat = chr.getQuestNoAdd(MapleQuest.getInstance(122700));
        if ((stat != null) && (stat.getCustomData() != null) && (Long.parseLong(stat.getCustomData()) > System.currentTimeMillis())) {
            mpw.writeLong(getTime(Long.parseLong(stat.getCustomData())));
        } else {
            mpw.writeLong(getTime(-2L));
        }
        mpw.write(0);
        
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        final List<Item> equipped = iv.newList();
        Collections.sort(equipped);
        
        
        for (Item item : equipped) {
            if ((item.getPosition() < 0) && (item.getPosition() > -100)) {
                addItemPosition(mpw, item, false, false);
                addItemInfo(mpw, item, chr);
            }
        }
        mpw.writeShort(0); // equipped equipment
        
        for (Item item : equipped) { 
            if ((item.getPosition() <= -100) && (item.getPosition() > -1000)) {
                System.out.println("Item Position " + item.getPosition());
                addItemPosition(mpw, item, false, false);
                addItemInfo(mpw, item, chr);
            }
        }
        mpw.writeShort(0);// cash equip
        iv = chr.getInventory(MapleInventoryType.EQUIP);
        for (Item item : iv.list()) { 
            addItemPosition(mpw, item, false, false);
            addItemInfo(mpw, item, chr);
        }
        mpw.writeShort(0);// equipment
        for (Item item : equipped) { 
            if ((item.getPosition() <= -1000) && (item.getPosition() > -1100)) {
                addItemPosition(mpw, item, false, false);
                addItemInfo(mpw, item, chr);
            }
        }
        mpw.writeShort(0);// unknown
        for (Item item : equipped) { 
            if ((item.getPosition() <= -1100) && (item.getPosition() > -1200)) {
                addItemPosition(mpw, item, false, false);
                addItemInfo(mpw, item, chr);
            }
        }
        mpw.writeShort(0); // dragon
        mpw.writeShort(0); // mech
        for (Item item : equipped) {
            if (item.getPosition() <= -1200) {
                addItemPosition(mpw, item, false, false);
                addItemInfo(mpw, item, chr);
            }
        }
        mpw.writeShort(0); // android
        mpw.writeShort(0); // unknown
        mpw.writeShort(0); // unknown
        mpw.writeShort(0); // unknown
        for (Item item : equipped) {
            if ((item.getPosition() <= -5000) && (item.getPosition() >= -5003)) {
                addItemPosition(mpw, item, false, false);
                addItemInfo(mpw, item, chr);
            }
        }      
        mpw.writeShort(0); // totem
        mpw.writeShort(0); // unknown
        mpw.writeShort(0); // unknown
        mpw.writeShort(0); // unknown
        mpw.writeShort(0); // unknown
        
        iv = chr.getInventory(MapleInventoryType.USE);
        for (Item item : iv.list()) {
            addItemPosition(mpw, item, false, false);
            addItemInfo(mpw, item, chr);
        }
        mpw.write(0);
             
        iv = chr.getInventory(MapleInventoryType.SETUP);
        for (Item item : iv.list()) {
            addItemPosition(mpw, item, false, false);
            addItemInfo(mpw, item, chr);
        }
        mpw.write(0);
        iv = chr.getInventory(MapleInventoryType.ETC);
        for (Item item : iv.list()) {
            if (item.getPosition() < 100) {
                addItemPosition(mpw, item, false, false);
                addItemInfo(mpw, item, chr);
            }
        }
        mpw.write(0);
        iv = chr.getInventory(MapleInventoryType.CASH);
        for (Item item : iv.list()) {
            addItemPosition(mpw, item, false, false);
            addItemInfo(mpw, item, chr);
        }
        mpw.write(0);
//        for (int i = 0; i < chr.getExtendedSlots().size(); i++) {
//            mpw.writeInt(i);
//            mpw.writeInt(chr.getExtendedSlot(i));
//            for (Item item : chr.getInventory(MapleInventoryType.ETC).list()) {
//                if ((item.getPosition() > i * 100 + 100) && (item.getPosition() < i * 100 + 200)) {
//                    addItemPosition(mpw, item, false, true);
//                    addItemInfo(mpw, item, chr);
//                }
//            }
//            mpw.writeInt(-1);
//        }
        mpw.writeZeroBytes(21);//was17
        
    }

    public static void addPotionPotInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        if (chr.getPotionPots() == null) {
            mpw.writeInt(0);
            return;
        }
        mpw.writeInt(chr.getPotionPots().size());
        for (MaplePotionPot p : chr.getPotionPots()) {
            mpw.writeInt(p.getId());
            mpw.writeInt(p.getMaxValue());
            mpw.writeInt(p.getHp());
            mpw.writeInt(0);
            mpw.writeInt(p.getMp());

            mpw.writeLong(PacketHelper.getTime(p.getStartDate()));
            mpw.writeLong(PacketHelper.getTime(p.getEndDate()));
        }
    }

    public static void addCharStats(MaplePacketWriter mpw, MapleCharacter chr) {
    	mpw.writeInt(chr.getID());
        mpw.writeInt(chr.getID()); // dwCharacterIDForLog
        mpw.writeInt(0); // [FF FF FF 7F] dwWorldIDForLog
        mpw.writeAsciiString(chr.getName(), 13);
        mpw.write(chr.getGender());
        mpw.write(chr.getSkinColor());
        mpw.writeInt(chr.getFace());
        mpw.writeInt(chr.getHair());
        mpw.write(-1); // nMixHairBaseColor
        mpw.write(0);  // nMixAddHairColor
        mpw.write(0);  // nMixHairBaseProb
        mpw.write(chr.getLevel());
        mpw.writeShort(chr.getJob());
        chr.getStat().connectData(mpw);
        mpw.writeShort(chr.getRemainingAp());
        if (GameConstants.isSeparatedSp(chr.getJob())) {
            int size = chr.getRemainingSpSize();
            mpw.write(size);
            for (int i = 0; i < chr.getRemainingSps().length; i++) {
                if (chr.getRemainingSp(i) > 0) {
                    mpw.write(i + 1);
                    mpw.writeInt(chr.getRemainingSp(i));
                }
            }
        } else {
            mpw.writeShort(chr.getRemainingSp());
        }
        mpw.writeLong(chr.getExp());
        mpw.writeInt(chr.getFame());
        mpw.writeInt(chr.getGachExp());
        mpw.writeInt(0); // migration data nWP?
        mpw.writeInt(chr.getMapId());
        mpw.write(chr.getInitialSpawnpoint());
        mpw.writeInt(0);
        mpw.writeShort(chr.getSubcategory());
        if (GameConstants.isDemonSlayer(chr.getJob()) || GameConstants.isXenon(chr.getJob()) || GameConstants.isDemonAvenger(chr.getJob()) || GameConstants.isBeastTamer(chr.getJob())) {
            mpw.writeInt(chr.getFaceMarking());
        }
        mpw.write(chr.getFatigue());
        mpw.writeInt(GameConstants.getCurrentDate()); // nLastFatigueUpdateTime
        
        for (MapleTrait.MapleTraitType t : MapleTrait.MapleTraitType.values()) {
            mpw.writeInt(chr.getTrait(t).getTotalExp());
        }
        
        for (MapleTrait.MapleTraitType t : MapleTrait.MapleTraitType.values()) {
            mpw.writeShort(0); //today's stats
        }
        mpw.write(0);
        mpw.writeLong(getTime(System.currentTimeMillis()));
        mpw.writeInt(chr.getStat().pvpExp);
        mpw.write(chr.getStat().pvpRank);
        mpw.writeInt(chr.getBattlePoints());
        mpw.write(0); // nPvPModeLevel
        mpw.write(0); // nPvPModeType
        mpw.writeInt(0); // nEventPoint
        addPartTimeJob(mpw, MapleCharacter.getPartTime(chr.getID()));
        for (int i = 0; i < 9; i++) {
            mpw.writeInt(0);
            mpw.write(0);
            mpw.writeInt(0);
        }
        mpw.writeReversedLong(getTime(System.currentTimeMillis())); // stAccountLastLogout
        mpw.write(0); // bBurning
    }

    /**
     * 
     * @param mpw
     * @param chr
     * @param mega
     * @param second
     * @see AvatarLook::Decode()
     */
    public static void addCharLook(MaplePacketWriter mpw, MapleCharacterLook chr, boolean mega, boolean second) {
    	mpw.write(second ? chr.getSecondGender() : chr.getGender());
        mpw.write(second ? chr.getSecondSkinColor() : chr.getSkinColor());
        mpw.writeInt(second ? chr.getSecondFace() : chr.getFace());
        mpw.writeInt(chr.getJob());
        mpw.write(mega ? 0 : 1);
        mpw.writeInt(second ? chr.getSecondHair() : chr.getHair());

        final Map<Byte, Integer> myEquip = new LinkedHashMap<>();
        final Map<Byte, Integer> maskedEquip = new LinkedHashMap<>();
        final Map<Byte, Integer> totemEquip = new LinkedHashMap<>();
        final Map<Byte, Integer> equip = second ? chr.getSecondEquips(true) : chr.getEquips(true);
        for (final Entry<Byte, Integer> item : equip.entrySet()) {
            if (item.getKey() < -127) {
            	continue;
            }
        byte pos = (byte) (item.getKey() * -1);

        if ((pos < 100) && (myEquip.get(pos) == null)) {
            myEquip.put(pos, item.getValue());
        } else if ((pos > 100) && (pos != 111)) {
            pos = (byte) (pos - 100);
            if (myEquip.get(pos) != null) {
            	maskedEquip.put(pos, myEquip.get(pos));
            	totemEquip.put(pos, item.getValue());
            }
            myEquip.put(pos, item.getValue());
            totemEquip.put(pos, item.getValue());
        } else if (myEquip.get(pos) != null) {
            maskedEquip.put(pos, item.getValue());
            totemEquip.put(pos, item.getValue());
            }
        }
        for (final Entry<Byte, Integer> totem : chr.getTotems().entrySet()) {
            byte pos = (byte) (totem.getKey() * -1);
                if (pos < 0 || pos > 2) { //3 totem slots
                continue;
            }
	        if (totem.getValue() < 1200000 || totem.getValue() >= 1210000) {
	            continue;
	        }
	        totemEquip.put(pos, totem.getValue());
        }

        for (Map.Entry entry : myEquip.entrySet()) {
            int weapon = ((Integer) entry.getValue()).intValue();
	        if (GameConstants.getWeaponType(weapon) == (second ? MapleWeaponType.LONG_SWORD : MapleWeaponType.BIG_SWORD)) {
	            continue;
	        }
	        mpw.write(((Byte) entry.getKey()).byteValue());
	        mpw.writeInt(((Integer) entry.getValue()).intValue());
        }
        mpw.write(255);

        for (Map.Entry entry : maskedEquip.entrySet()) {
            mpw.write(((Byte) entry.getKey()).byteValue());
            mpw.writeInt(((Integer) entry.getValue()).intValue());
        }
        mpw.write(255);

        for (Map.Entry entry : totemEquip.entrySet()) {
            mpw.write(((Byte) entry.getKey()).byteValue());
            mpw.writeInt(((Integer) entry.getValue()).intValue());
        }
        mpw.write(255); //new v140

        Integer cWeapon = equip.get(Byte.valueOf((byte) -111));
        mpw.writeInt(cWeapon != null ? cWeapon.intValue() : 0);
        Integer Weapon = equip.get(Byte.valueOf((byte) -11));
        mpw.writeInt(Weapon != null ? Weapon.intValue() : 0); //new v139
        boolean zero = GameConstants.isZero(chr.getJob());
        Integer Shield = equip.get(Byte.valueOf((byte) -10)); // (byte) -110));
        
        mpw.writeInt(!zero && Shield != null ? Shield.intValue() : 0); //new v139
        mpw.write(/*GameConstants.isMercedes(chr.getJob()) ? 1 : */0); // Mercedes/Elf Ears
        for(int i = 0; i < 3; i++) {
        	mpw.writeInt(0);   // Pet Item IDs, add in later
        }
        if (GameConstants.isDemonSlayer(chr.getJob()) || GameConstants.isXenon(chr.getJob()) || GameConstants.isDemonAvenger(chr.getJob()) || GameConstants.isBeastTamer(chr.getJob())) {
        	mpw.writeInt(chr.getFaceMarking());
        } else if (GameConstants.isZero(chr.getJob())) {
        	mpw.write(1);
        }
        if (GameConstants.isBeastTamer(chr.getJob())) {
            mpw.write(1);
            mpw.writeInt(chr.getEars());
            mpw.write(1);
            mpw.writeInt(chr.getTail());
        }
        mpw.write(0); // nMixedHairColor
        mpw.write(0); // nMixHairPercent
    }

    public static void addExpirationTime(MaplePacketWriter mpw, long time) {
        mpw.writeLong(getTime(time));
    }

    public static void addItemPosition(MaplePacketWriter mpw, Item item, boolean trade, boolean bagSlot) {
        if (item == null) {
            mpw.write(0);
            return;
        }
        short pos = item.getPosition();
        if (pos <= -1) {
            pos = (short) (pos * -1);
            if ((pos > 100) && (pos < 1000)) {
                pos = (short) (pos - 100);
            }
        }
        if (bagSlot) {
            mpw.writeInt(pos % 100 - 1);
        } else if ((!trade) && (item.getType() == 1)) {
            mpw.writeShort(pos);
        } else {
            mpw.write(pos);
        }
        System.out.println("Cash item positon " + pos);
    }

    public static void addItemInfo(MaplePacketWriter mpw, Item item) {
        addItemInfo(mpw, item, null);
    }

    public static void addItemInfo(final MaplePacketWriter mpw, final Item item, final MapleCharacter chr) {
        mpw.write(item.getPet() != null ? 3 : item.getType());
        mpw.writeInt(item.getItemId());
        boolean hasUniqueId = item.getUniqueId() > 0 && !GameConstants.isMarriageRing(item.getItemId()) && item.getItemId() / 10000 != 166;
        //marriage rings arent cash items so dont have uniqueids, but we assign them anyway for the sake of rings
        mpw.write(hasUniqueId ? 1 : 0);
        if (hasUniqueId) {
            mpw.writeLong(item.getUniqueId());
        }
        if (item.getPet() != null) { // Pet
            addPetItemInfo(mpw, item, item.getPet(), true);
        } else {
            addExpirationTime(mpw, item.getExpiration());
            mpw.writeInt(chr == null ? -1 : chr.getExtendedSlots().indexOf(item.getItemId()));
            if (item.getType() == 1) {
                final Equip equip = Equip.calculateEquipStats((Equip) item);
                //final Equip equip = Equip.calculateEquipStatsTest((Equip) item);
                addEquipStats(mpw, equip);
                //addEquipStatsTest(mpw, equip);
                addEquipBonusStats(mpw, equip, hasUniqueId);
            } else {
                mpw.writeShort(item.getQuantity());
                mpw.writeMapleAsciiString(item.getOwner());
                mpw.writeShort(item.getFlag());
                if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId()) || item.getItemId() / 10000 == 287) {
                    mpw.writeLong(item.getInventoryId() <= 0 ? -1 : item.getInventoryId());
                }
            }
        }
    }

    public static void addEquipStatsTest(MaplePacketWriter mpw, Equip equip) {
        int mask;
        int masklength = 2;
        for (int i = 1; i <= masklength; i++) {
            mask = 0;
            if (equip.getStatsTest().size() > 0) {
                for (EquipStat stat : equip.getStatsTest().keySet()) {
                    if (stat.getPosition() == i) {
                        mask += stat.getValue();
                    }
                }
            }
            mpw.writeInt(mask);
            if (mask != 0) {
                for (EquipStat stat : equip.getStatsTest().keySet()) {
                    if (stat.getDatatype() == 8) {
                        mpw.writeLong(equip.getStatsTest().get(stat));
                    } else if (stat.getDatatype() == 4) {
                        mpw.writeInt(equip.getStatsTest().get(stat).intValue());
                    } else if (stat.getDatatype() == 2) {
                        mpw.writeShort(equip.getStatsTest().get(stat).shortValue());
                    } else if (stat.getDatatype() == 1) {
                        mpw.write(equip.getStatsTest().get(stat).byteValue());
                    }
                }
            }
        }
    }

    public static void addEquipStats(MaplePacketWriter mpw, Equip equip) {
        int head = 0;
        if (equip.getStats().size() > 0) {
            for (EquipStat stat : equip.getStats()) {
                head |= stat.getValue();
            }
        }
        mpw.writeInt(head);
        if (head != 0) {
            if (equip.getStats().contains(EquipStat.SLOTS)) {
                mpw.write(equip.getUpgradeSlots());
            }
            if (equip.getStats().contains(EquipStat.LEVEL)) {
                mpw.write(equip.getLevel());
            }
            if (equip.getStats().contains(EquipStat.STR)) {
                mpw.writeShort(equip.getStr());
            }
            if (equip.getStats().contains(EquipStat.DEX)) {
                mpw.writeShort(equip.getDex());
            }
            if (equip.getStats().contains(EquipStat.INT)) {
                mpw.writeShort(equip.getInt());
            }
            if (equip.getStats().contains(EquipStat.LUK)) {
                mpw.writeShort(equip.getLuk());
            }
            if (equip.getStats().contains(EquipStat.MHP)) {
                mpw.writeShort(equip.getHp());
            }
            if (equip.getStats().contains(EquipStat.MMP)) {
                mpw.writeShort(equip.getMp());
            }
            if (equip.getStats().contains(EquipStat.WATK)) {
                mpw.writeShort(equip.getWatk());
            }
            if (equip.getStats().contains(EquipStat.MATK)) {
                mpw.writeShort(equip.getMatk());
            }
            if (equip.getStats().contains(EquipStat.WDEF)) {
                mpw.writeShort(equip.getWdef());
            }
            if (equip.getStats().contains(EquipStat.MDEF)) {
                mpw.writeShort(equip.getMdef());
            }
            if (equip.getStats().contains(EquipStat.ACC)) {
                mpw.writeShort(equip.getAcc());
            }
            if (equip.getStats().contains(EquipStat.AVOID)) {
                mpw.writeShort(equip.getAvoid());
            }
            if (equip.getStats().contains(EquipStat.HANDS)) {
                mpw.writeShort(equip.getHands());
            }
            if (equip.getStats().contains(EquipStat.SPEED)) {
                mpw.writeShort(equip.getSpeed());
            }
            if (equip.getStats().contains(EquipStat.JUMP)) {
                mpw.writeShort(equip.getJump());
            }
            if (equip.getStats().contains(EquipStat.FLAG)) {
                mpw.writeShort(equip.getFlag());
            }
            if (equip.getStats().contains(EquipStat.INC_SKILL)) {
                mpw.write(equip.getIncSkill() > 0 ? 1 : 0);
            }
            if (equip.getStats().contains(EquipStat.ITEM_LEVEL)) {
                mpw.write(Math.max(equip.getBaseLevel(), equip.getEquipLevel())); // Item level
            }
            if (equip.getStats().contains(EquipStat.ITEM_EXP)) {
                mpw.writeLong(equip.getExpPercentage() * 100000); // Item Exp... 10000000 = 100%
            }
            if (equip.getStats().contains(EquipStat.DURABILITY)) {
                mpw.writeInt(equip.getDurability());
            }
            if (equip.getStats().contains(EquipStat.VICIOUS_HAMMER)) {
                mpw.writeInt(equip.getViciousHammer());
            }
            if (equip.getStats().contains(EquipStat.PVP_DAMAGE)) {
                mpw.writeShort(equip.getPVPDamage());
            }
            if (equip.getStats().contains(EquipStat.ENHANCT_BUFF)) {
                mpw.writeShort(equip.getEnhanctBuff());
            }
            if (equip.getStats().contains(EquipStat.DURABILITY_SPECIAL)) {
                mpw.writeInt(equip.getDurability());
            }
            if (equip.getStats().contains(EquipStat.REQUIRED_LEVEL)) {
                mpw.write(equip.getReqLevel());
            }
            if (equip.getStats().contains(EquipStat.YGGDRASIL_WISDOM)) {
                mpw.write(equip.getYggdrasilWisdom());
            }
            if (equip.getStats().contains(EquipStat.FINAL_STRIKE)) {
                mpw.write(equip.getFinalStrike());
            }
            if (equip.getStats().contains(EquipStat.BOSS_DAMAGE)) {
                mpw.write(equip.getBossDamage());
            }
            if (equip.getStats().contains(EquipStat.IGNORE_PDR)) {
                mpw.write(equip.getIgnorePDR());
            }
        } else {
            /*
             *   if ( v3 >= 0 )
             *     v36 = 0;
             *   else
             *     v36 = (unsigned __int8)CInPacket::Decode1(a2);
             */
//            mpw.write(0); //unknown
        }
        addEquipSpecialStats(mpw, equip);
    }

    public static void addEquipSpecialStats(MaplePacketWriter mpw, Equip equip) {
        int head = 0;
        if (equip.getSpecialStats().size() > 0) {
            for (EquipSpecialStat stat : equip.getSpecialStats()) {
                head |= stat.getValue();
            }
        }
        mpw.writeInt(head);
//        System.out.println("mask " + head);

        if (head != 0) {
            if (equip.getSpecialStats().contains(EquipSpecialStat.TOTAL_DAMAGE)) {
//                System.out.println("TOTAL_DAMAGE " + equip.getTotalDamage());
                mpw.write(equip.getTotalDamage());
            }
            if (equip.getSpecialStats().contains(EquipSpecialStat.ALL_STAT)) {
//                System.out.println("ALL_STAT " + equip.getAllStat());
                mpw.write(equip.getAllStat());
            }
            if (equip.getSpecialStats().contains(EquipSpecialStat.KARMA_COUNT)) {
//                System.out.println("KARMA_COUNT " + equip.getKarmaCount());
                mpw.write(equip.getKarmaCount());
            }
            if (equip.getSpecialStats().contains(EquipSpecialStat.UNK8)) {
//                System.out.println("unk8 " + System.currentTimeMillis());
                mpw.writeLong(System.currentTimeMillis());
            }
            if (equip.getSpecialStats().contains(EquipSpecialStat.UNK10)) {
//                System.out.println("unk10 " + 1);
                mpw.writeInt(0);
            }
        }
    }

//    public static void addEquipBonusStats(MaplePacketLittleEndianWriter mpw, Equip equip, boolean hasUniqueId) {
//        mpw.writeMapleAsciiString(equip.getOwner());
//        mpw.write(equip.getState()); // 17 = rare, 18 = epic, 19 = unique, 20 = legendary, potential flags. special grade is 14 but it crashes
//        mpw.write(equip.getEnhance());
//        mpw.writeShort(equip.getPotential1());
//        mpw.writeShort(equip.getPotential2());
//        mpw.writeShort(equip.getPotential3());
//        mpw.writeShort(equip.getBonusPotential1());
//        mpw.writeShort(equip.getBonusPotential2());
//        mpw.writeShort(equip.getBonusPotential3());
//        mpw.writeShort(equip.getFusionAnvil() % 100000);
//        mpw.writeShort(equip.getSocketState());
//        mpw.writeShort(equip.getSocket1() % 10000); // > 0 = mounted, 0 = empty, -1 = none.
//        mpw.writeShort(equip.getSocket2() % 10000);
//        mpw.writeShort(equip.getSocket3() % 10000);
//        if (!hasUniqueId) {
//            mpw.writeLong(equip.getInventoryId() <= 0 ? -1 : equip.getInventoryId()); //some tracking ID
//        }
//        mpw.writeLong(getTime(-2));
//        mpw.writeInt(-1); //?
//        
//    }
    public static void addEquipBonusStats(MaplePacketWriter mpw, Equip equip, boolean hasUniqueId) {
        mpw.writeMapleAsciiString(equip.getOwner());
        mpw.write(equip.getState()); // 17 = rare, 18 = epic, 19 = unique, 20 = legendary, potential flags. special grade is 14 but it crashes
        mpw.write(equip.getEnhance());
        mpw.writeShort(equip.getPotential1());
        mpw.writeShort(equip.getPotential2());
        mpw.writeShort(equip.getPotential3());
        mpw.writeShort(equip.getBonusPotential1());
        mpw.writeShort(equip.getBonusPotential2());
        mpw.writeShort(equip.getBonusPotential3());
        mpw.writeShort(equip.getFusionAnvil() % 100000);
        mpw.writeShort(equip.getSocketState());
        mpw.writeShort(equip.getSocket1() % 10000); // > 0 = mounted, 0 = empty, -1 = none.
        mpw.writeShort(equip.getSocket2() % 10000);
        mpw.writeShort(equip.getSocket3() % 10000);
        if (!hasUniqueId) {
            mpw.writeLong(equip.getInventoryId() <= 0 ? -1 : equip.getInventoryId()); //some tracking ID
        }
        mpw.writeLong(getTime(-2));
        mpw.writeInt(-1); //?
        // new 142
        mpw.writeLong(0);
        mpw.writeLong(getTime(-2));
        mpw.writeLong(0);
        mpw.writeLong(0);
        mpw.writeZeroBytes(6);
    }

    public static void serializeMovementList(MaplePacketWriter mlew, List<LifeMovementFragment> moves) {
        mlew.write(moves.size());
        for (LifeMovementFragment move : moves) {
            move.serialize(mlew);
        }
    }

    public static void addAnnounceBox(MaplePacketWriter mpw, MapleCharacter chr) {
        if ((chr.getPlayerShop() != null) && (chr.getPlayerShop().isOwner(chr)) && (chr.getPlayerShop().getShopType() != 1) && (chr.getPlayerShop().isAvailable())) {
            addInteraction(mpw, chr.getPlayerShop());
        } else {
            mpw.write(0);
        }
    }

    public static void addInteraction(MaplePacketWriter mpw, IMaplePlayerShop shop) {
        mpw.write(shop.getGameType()); // nMiniRoomType
        mpw.writeInt(((AbstractPlayerStore) shop).getObjectId()); // dwMiniRoomSN
        mpw.writeMapleAsciiString(shop.getDescription()); // sMiniRoomTitle
        if (shop.getShopType() != 1) {
            mpw.write(shop.getPassword().length() > 0 ? 1 : 0); // bPrivate
        }
        mpw.write(shop.getItemId() % 10); // nGameKind
        mpw.write(shop.getSize()); // nCurUsers
        mpw.write(shop.getMaxSize()); // nMaxUsers
        if (shop.getShopType() != 1) {
            mpw.write(shop.isOpen() ? 0 : 1); // bGameOn
        }
    }

    public static void addCharacterInfo(MaplePacketWriter mpw, MapleCharacter chr) {
    	long flag = -1;
    	
    	mpw.writeLong(flag);
        mpw.write(0); // nCombatOrders
        
        for(int i = 0; i < 3; i++) { // Seeds
        	mpw.writeInt(-4); // FC FF FF FF //aPetActiveSkillCoolTime
        }
        mpw.write(0); // nPvPExp_CS
        mpw.write(0); // nKey
        mpw.writeInt(0); // unk bool
        mpw.write(0);
        
        if ((flag & 1) != 0) {
            addCharStats(mpw, chr);
            mpw.write(chr.getBuddylist().getCapacity());
            mpw.write(chr.getBlessOfFairyOrigin() != null);
            if (chr.getBlessOfFairyOrigin() != null) {
                mpw.writeMapleAsciiString(chr.getBlessOfFairyOrigin());
            }
            mpw.write(chr.getBlessOfEmpressOrigin() != null);
            if (chr.getBlessOfEmpressOrigin() != null) {
                mpw.writeMapleAsciiString(chr.getBlessOfEmpressOrigin());
            }
            MapleQuestStatus ultExplorer = chr.getQuestNoAdd(MapleQuest.getInstance(GameConstants.ULT_EXPLORER));
            mpw.write((ultExplorer != null) && (ultExplorer.getCustomData() != null));
            if ((ultExplorer != null) && (ultExplorer.getCustomData() != null)) {
                mpw.writeMapleAsciiString(ultExplorer.getCustomData());
            }
        }
        
        if ((flag & 2) != 0) {
            addMoneyInfo(mpw, chr);
        }
        if ((flag & 8) != 0) {
            addInventoryInfo(mpw, chr);
        }
        if ((flag & 0x100) != 0) {
            addSkillInfo(mpw, chr);
        }
        if ((flag & 0x8000) != 0) {
            addCoolDownInfo(mpw, chr);
        }
        if ((flag & 0x200) != 0) {
            addStartedQuestInfo(mpw, chr);
        }
        if ((flag & 0x4000) != 0) {
            addCompletedQuestInfo(mpw, chr);
        }
        if ((flag & 0x400) != 0) {
            addMiniGameRecordsInfo(mpw, chr);
        }
        if ((flag & 0x800) != 0) {
            addRingInfo(mpw, chr);
        }
        if ((flag & 0x1000) != 0) {
            addRocksInfo(mpw, chr);
        }
        if ((flag & 0x20000) != 0) {
            mpw.writeInt(0); //nSlotHyper
        }
        if ((flag & 0x10000) != 0) {
            addMonsterBookInfo(mpw, chr);
        }

        if ((flag & 0x80000) != 0) {
	        mpw.writeShort(0);
	        int count = 0;
	        mpw.writeInt(count);
	        for(int i = 0; i < count; i++) {
	        	mpw.writeInt(chr.getID());
	        	mpw.writeInt(0);
	        	mpw.writeMapleAsciiString("");
	        	mpw.write(1);
	        	mpw.writeLong(0);
	        	mpw.writeInt(0);
	        	mpw.writeMapleAsciiString("");
	        	mpw.write(0);
	        	mpw.write(0);
	        	mpw.writeLong(0);
	        	mpw.writeMapleAsciiString("");
	        }
        }
        
        mpw.writeShort(0);
        
        if ((flag & 0x40000) != 0) {
            chr.QuestInfoPacket(mpw);
        }
        
        if ((flag & 0x2000) != 0) { // Androids
        	int count = 0;
        	mpw.writeShort(count);
	        for(int i = 0; i < count; i++) {
	        	mpw.writeInt(0);
	        	addCharLook(mpw, chr, false, false);
	        }
        }
        
        if ((flag & 0x1000) != 0) { // nWillEXPCS
        	int count = 0;
        	mpw.writeInt(count);
        	for(int i = 0; i < count; i++) {
        		mpw.writeInt(0);
        		mpw.writeInt(0);
        	}
        }
        
        if ((flag & 0x200000) != 0) {
            if ((chr.getJob() >= 3300) && (chr.getJob() <= 3312)) {
                addJaguarInfo(mpw, chr);
            }
        }
        
        if (GameConstants.isZero(chr.getJob())) {
            addZeroInfo(mpw, chr);
        }
        
        if ((flag & 0x4000000) != 0) {
        	mpw.writeShort(0); // NPCShopBuyLimit
        	// TODO
        }
        
        
        if ((flag & 0x20000000) != 0) {
            //addStealSkills(mpw, chr);
        	mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
        	mpw.writeInt(0);
        }
        
        if ((flag & 0x10000000) != 0) {
        	mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
        }
        
        //addStealSkills(mpw, chr);
        mpw.writeInt(0); // doesn't belong 
        if ((flag & 0x80000000) != 0) {
            addAbilityInfo(mpw, chr);
        }
        
        if ((flag & 0x10000) != 0) { // GW_SoulCollection
        	int count = 0;
        	mpw.writeShort(count);
        	for(int i = 0; i < count; i++) {
        		mpw.writeInt(0);
        		mpw.writeInt(0);
        	}
        }
        
        int mlcount = 0;
        mpw.writeInt(mlcount); // Monsterlife
        for(int i = 0; i < mlcount; i++) {
        	mpw.writeMapleAsciiString("");
        	mpw.writeInt(0);
        	mpw.writeMapleAsciiString("");
        	int count = 0;
        	mpw.writeInt(count);
        	for(int j = 0; j < count; j++) {
        		mpw.write(0);
        	}
        	
        }
        
        //addHonorInfo(mpw, chr);
        mpw.writeLong(0); // 1
        mpw.writeZeroBytes(3); // 2
        
        mpw.write(0); // Return effect info

        if (GameConstants.isAngelicBuster(chr.getJob())) {
	        mpw.writeInt(1);
	        mpw.writeInt(21173); //face
	        mpw.writeInt(37141); //hair
	        mpw.writeInt(1051291); // dressup suit cant unequip
	        mpw.writeInt(0);
	        mpw.writeInt(0);
	        mpw.write(0);
        } else {
	        mpw.writeZeroBytes(25);
        }
        
        mpw.writeInt(1);
        mpw.writeInt(47);
        mpw.writeLong(1);
        mpw.writeShort(0);
        mpw.writeInt(0);
        
        mpw.write(0);
        mpw.writeShort(0);
        
        
        mpw.writeInt(-1);
        mpw.writeLong(0);
        mpw.writeLong(1);
        mpw.writeLong(System.currentTimeMillis());
        mpw.writeZeroBytes(14);
        
        //addEvolutionInfo(mpw, chr);
        //mpw.writeZeroBytes(3);//new 144
        //mpw.write(0); //farm monsters length

        if((flag & 0x40) != 0) {
        	addFarmInfo(mpw, chr.getClient(), 0);
        	mpw.writeInt(-1); //v146 can be 5 tho...
            mpw.writeInt(0);
        }

        mpw.write(0); // Memorial Cube Info
       
        mpw.writeInt(0); // nEXP64
        mpw.writeLong(getTime(-2));
        mpw.writeInt(0);
        
        // Runner Game Record
        mpw.writeInt(chr.getID());
        mpw.writeInt(0); // Last Score
        mpw.writeInt(0); // High Score
        mpw.writeInt(0); // Runner Point
        mpw.writeLong(getTime(-2)); // Last Played
        mpw.writeInt(10); // Total Left
        int runnerCount = 0;
        mpw.writeShort(runnerCount); // Count
        for (int i = 0; i < runnerCount; i++) {
        	mpw.writeInt(9); // Slot
        	mpw.writeMapleAsciiString("check1=0;cDate=16/06/24");
        }
        
        if ((flag & 0x40000) != 0) {
            mpw.writeShort(0); //v174
        }
        mpw.write(0);
        
        mpw.writeInt(0); // Decode Text Equip Info
        
        mpw.write(HexTool.getByteArrayFromHexString("01 00 01 00 00 00 00 00 00 00 64 00 00 00 D0 25 8A A2 3B CE D1 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A0 F1 E7 C0 04 CF D1 01 00 01 00 00"));
        
        /*
        if((flag & 0x4000000) != 0) {
        	mpw.writeShort(1);
            mpw.writeInt(1);
            mpw.writeInt(0);
            mpw.writeInt(100);
            mpw.writeLong(getTime(-2));
            mpw.writeShort(0);
            mpw.writeShort(0);
        }
        mpw.write(0);
        */
        /*
        //mpw.writeZeroBytes(9);//v146
        if ((flag & 0x2000) != 0) {
            addCoreAura(mpw, chr);
        }
        */
        
        mpw.writeInt(chr.getClient().getAccountID());
        mpw.writeInt(chr.getID());
        mpw.writeInt(4);
        mpw.writeInt(0);
        addRedLeafInfo(mpw, chr);
        
    }

    public static int getSkillBook(final int i) {
        switch (i) {
            case 1:
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 2;
        }
        return 0;
    }

    public static void addAbilityInfo(final MaplePacketWriter mpw, MapleCharacter chr) {
        final List<InnerSkillValueHolder> skills = chr.getInnerSkills();
        mpw.writeShort(skills.size());
        for (int i = 0; i < skills.size(); ++i) {
            mpw.write(i + 1); // key
            mpw.writeInt(skills.get(i).getSkillId()); //d 7000000 id ++, 71 = char cards
            mpw.write(skills.get(i).getSkillLevel()); // level
            mpw.write(skills.get(i).getRank()); //rank, C, B, A, and S
        }

    }

    public static void addHonorInfo(final MaplePacketWriter mpw, MapleCharacter chr) {
        mpw.writeInt(chr.getHonorLevel()); //honor lvl
        mpw.writeInt(chr.getHonourExp()); //honor exp
    }

    public static void addEvolutionInfo(final MaplePacketWriter mpw, MapleCharacter chr) {
        mpw.writeShort(0);
        mpw.writeShort(0);
    }

    public static void addCoreAura(MaplePacketWriter mpw, MapleCharacter chr) {
        MapleCoreAura aura = chr.getCoreAura();
        mpw.writeInt(aura.getId()); //nvr change
        mpw.writeInt(chr.getID());
        int level = chr.getSkillLevel(80001151) > 0 ? chr.getSkillLevel(80001151) : chr.getSkillLevel(1214);
        mpw.writeInt(level);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(aura.getExpire());//timer
        mpw.writeInt(0);
        mpw.writeInt(aura.getAtt());//wep att
        mpw.writeInt(aura.getDex());//dex
        mpw.writeInt(aura.getLuk());//luk
        mpw.writeInt(aura.getMagic());//magic att
        mpw.writeInt(aura.getInt());//int
        mpw.writeInt(aura.getStr());//str
        mpw.writeInt(0);
        mpw.writeInt(aura.getTotal());//max
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeLong(getTime(System.currentTimeMillis() + 86400000L));
        mpw.write(0);
        mpw.write(GameConstants.isJett(chr.getJob()) ? 1 : 0);
    }

    public static void addStolenSkills(MaplePacketWriter mpw, MapleCharacter chr, int jobNum, boolean writeJob) {
        if (writeJob) {
            mpw.writeInt(jobNum);
        }
        int count = 0;
        if (chr.getStolenSkills() != null) {
            for (Pair<Integer, Boolean> sk : chr.getStolenSkills()) {
                if (GameConstants.getJobNumber(sk.left / 10000) == jobNum) {
                    mpw.writeInt(sk.left);
                    count++;
                    if (count >= GameConstants.getNumSteal(jobNum)) {
                        break;
                    }
                }
            }
        }
        while (count < GameConstants.getNumSteal(jobNum)) { //for now?
            mpw.writeInt(0);
            count++;
        }
    }

    public static void addChosenSkills(MaplePacketWriter mpw, MapleCharacter chr) {
        for (int i = 1; i <= 5; i++) {
            boolean found = false;
            if (chr.getStolenSkills() != null) {
                for (Pair<Integer, Boolean> sk : chr.getStolenSkills()) {
                    if (GameConstants.getJobNumber(sk.left / 10000) == i && sk.right) {
                        mpw.writeInt(sk.left);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                mpw.writeInt(0);
            }
        }
    }

    public static void addStealSkills(final MaplePacketWriter mpw, final MapleCharacter chr) {
        for (int i = 1; i <= 5; i++) {
            addStolenSkills(mpw, chr, i, false); // 52
        }
        addChosenSkills(mpw, chr); // 16
    }

    public static void addMonsterBookInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        if (chr.getMonsterBook().getSetScore() > 0) {
            chr.getMonsterBook().writeFinished(mpw);
        } else {
            chr.getMonsterBook().writeUnfinished(mpw);
        }

        mpw.writeInt(chr.getMonsterBook().getSet());
    }

    public static void addPetItemInfo(MaplePacketWriter mpw, Item item, MaplePet pet, boolean active) {
        if (item == null) {
            mpw.writeLong(PacketHelper.getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
        } else {
            addExpirationTime(mpw, item.getExpiration() <= System.currentTimeMillis() ? -1L : item.getExpiration());
        }
        mpw.writeInt(-1);
        mpw.writeAsciiString(pet.getName(), 13);
        mpw.write(pet.getLevel());
        mpw.writeShort(pet.getCloseness());
        mpw.write(pet.getFullness());
        if (item == null) {
            mpw.writeLong(PacketHelper.getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
        } else {
            addExpirationTime(mpw, item.getExpiration() <= System.currentTimeMillis() ? -1L : item.getExpiration());
        }
        mpw.writeShort(0);
        mpw.writeShort(pet.getFlags());
        mpw.writeInt((pet.getPetItemId() == 5000054) && (pet.getSecondsLeft() > 0) ? pet.getSecondsLeft() : 0);
        mpw.writeShort(0);
        mpw.write(active ? 0 : pet.getSummoned() ? pet.getSummonedValue() : 0);
        for (int i = 0; i < 4; i++) {
            mpw.write(0);
        }
        mpw.writeInt(-1); //new v140
        mpw.writeShort(100); //new v140
    }

    public static void addShopInfo(MaplePacketWriter mpw, MapleShop shop, MapleClient c) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        mpw.writeInt(0); // nSelectNPCItemID
        mpw.writeInt(shop.getNpcId()); // dwNpcTemplateID
        mpw.writeInt(0); // nStarCoin
        
        mpw.write(shop.getRanks().size() > 0 ? 1 : 0);
        if (shop.getRanks().size() > 0) {
            mpw.write(shop.getRanks().size());  
            for (Pair<Integer, String> s : shop.getRanks()) {
                mpw.writeInt(((Integer) s.left).intValue());
                mpw.writeMapleAsciiString((String) s.right);
            }
        }

        mpw.writeInt(0); // nShopVerNo 
        
        mpw.writeShort(shop.getItems().size() + c.getCharacter().getRebuy().size()); // nCount
        for (MapleShopItem item : shop.getItems()) {
            addShopItemInfo(mpw, item, shop, ii, null, c.getCharacter());
        }
        for (Item i : c.getCharacter().getRebuy()) {
            addShopItemInfo(mpw, new MapleShopItem(i.getItemId(), (int) ii.getPrice(i.getItemId()), i.getQuantity(), i.getPosition()), shop, ii, i, c.getCharacter());
        }
    }

    /*
     * Categories:
     * 0 - No Tab
     * 1 - Equip
     * 2 - Use
     * 3 - Setup
     * 4 - Etc
     * 5 - Recipe
     * 6 - Scroll
     * 7 - Special
     * 8 - 8th Anniversary
     * 9 - Button
     * 10 - Invitation Ticket
     * 11 - Materials
     * 12 - Maple
     * 13 - Homecoming
     * 14 - Cores
     * 80 - JoeJoe
     * 81 - Hermoninny
     * 82 - Little Dragon
     * 83 - Ika
     */
    public static void addShopItemInfo(MaplePacketWriter mpw, MapleShopItem shopItem, MapleShop shop, MapleItemInformationProvider itemInfo, Item item, MapleCharacter chr) {
        mpw.writeInt(shopItem.getItemId());
        mpw.writeInt(shopItem.getPrice());
        mpw.write(ServerConstants.SHOP_DISCOUNT); //Discount
        mpw.writeInt(shopItem.getReqItem()); // nTokenItemID
        mpw.writeInt(shopItem.getReqItemQ()); // nTokenPrice
        mpw.writeInt(1440 * shopItem.getExpiration()); // nPointQuestID
        mpw.writeInt(shopItem.getMinLevel()); // nPointPrice
        mpw.writeInt(0); // nStarCoin
        mpw.writeInt(0); // nQuestExID
        mpw.writeMapleAsciiString(""); // sQuestExKey
        mpw.writeInt(0); // nQuestExValue
        mpw.writeInt(0); // nItemPeriod
        mpw.writeInt(0); // nLevelLimited
        mpw.writeShort(0); // nShowLevMin
        mpw.writeShort(0); // nShowLevMax
        mpw.writeInt(0); // nQuestID
        mpw.writeLong(getTime(-2L)); // ftSellStart
        mpw.writeLong(getTime(-1L)); // ftSellEnd
        
        mpw.writeInt(shopItem.getCategory()); // nTabIndex
        if (GameConstants.isEquip(shopItem.getItemId())) { // bWorldBlock
            mpw.write(shopItem.hasPotential() ? 1 : 0);
        } else {
            mpw.write(0);
        }
        //mpw.writeInt(item.getExpiration() > 0 ? 1 : 0);
        mpw.writeInt(0); // nPotentialGrade
        mpw.writeInt(0); // nBuyLimit
        int nType = 0;
        mpw.write(nType); // nType
        if (nType == 1 || nType == 2 || nType == 3) {
        	int v3 = 0;
        	mpw.writeInt(v3);
        	for (int j = 0; j < v3; j++) {
        		mpw.writeLong(0); // time
        	}
        }
        if ((!GameConstants.isThrowingStar(shopItem.getItemId())) && (!GameConstants.isBullet(shopItem.getItemId()))) {
        	//mpw.writeShort(item.getBuyable()); //buyable
        	mpw.writeShort(shopItem.getQuantity()); //quantity of item to buy
        } else {
            //mpw.writeAsciiString("333333");
            //mpw.writeShort(BitTools.doubleToShortBits(ii.getPrice(item.getItemId())));
			//mpw.writeShort(ItemInformation.getInstance().getSlotMax(c, item.getItemId()));
            //mpw.writeShort(ii.getSlotMax(item.getItemId()));
        	mpw.writeLong(0); // dUnitPrice
        
        /*
             mpw.writeInt(0);
             mpw.writeShort(0);
             mpw.writeShort(BitTools.doubleToShortBits(ii.getPrice(item.getItemId())));
             */
//            mpw.writeZeroBytes(8);
//            mpw.writeShort(ii.getSlotMax(item.getItemId()));
        }
    
        mpw.writeShort(shopItem.getQuantity()); // nMaxPerSlot
        mpw.write(item == null ? 0 : 1);
        if (item != null) {
            addItemInfo(mpw, item);
        }
        /*
        if (shop.getRanks().size() > 0) {
            mpw.write(shopItem.getRank() >= 0 ? 1 : 0);
            if (shopItem.getRank() >= 0) {
                mpw.write(shopItem.getRank());
            }
        }
        */
        for (int j = 0; j < 4; j++) {
            mpw.writeInt(0); //red leaf high price probably
        }
        
        addRedLeafInfo(mpw, chr);
    }

    public static void addJaguarInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        mpw.write(chr.getIntNoRecord(GameConstants.JAGUAR));
        for (int i = 0; i < 5; i++) {
            mpw.writeInt(0);
        }
    }

    public static void addZeroInfo(MaplePacketWriter mpw, MapleCharacter chr) {
        short mask = 0;
        mpw.writeShort(mask);
        if ((mask & 1) != 0) {
            mpw.write(0); //bool
        }
        if ((mask & 2) != 0) {
            mpw.writeInt(0);
        }
        if ((mask & 4) != 0) {
            mpw.writeInt(0);
        }
        if ((mask & 8) != 0) {
            mpw.write(0);
        }
        if ((mask & 10) != 0) {
            mpw.writeInt(0);
        }
        if ((mask & 20) != 0) {
            mpw.writeInt(0);
        }
        if ((mask & 40) != 0) {
            mpw.writeInt(0);
        }
        if (mask < 0) {
            mpw.writeInt(0);
        }
        if ((mask & 100) != 0) {
            mpw.writeInt(0);
        }
    }

    public static void addFarmInfo(MaplePacketWriter mpw, MapleClient c, int idk) {
        mpw.writeMapleAsciiString(c.getFarm().getName());
        mpw.writeInt(c.getFarm().getWaru());
        mpw.writeInt(c.getFarm().getLevel());
        mpw.writeInt(c.getFarm().getExp());
        mpw.writeInt(c.getFarm().getAestheticPoints());
        mpw.writeInt(0); //gems

        mpw.write((byte) idk);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(1);
    }

    public static void addRedLeafInfo(MaplePacketWriter mpw, MapleCharacter chr) {      
    	for (int i = 0; i < 4; i++) {
            mpw.writeInt(9410165 + i);
            mpw.writeInt(chr.getFriendShipPoints()[i]);
        }

    }

    public static void addLuckyLogoutInfo(MaplePacketWriter mpw, boolean enable, CashItem item0, CashItem item1, CashItem item2) {
        mpw.writeInt(enable ? 1 : 0);
        if (enable) {
            CSPacket.addCSItemInfo(mpw, item0);
            CSPacket.addCSItemInfo(mpw, item1);
            CSPacket.addCSItemInfo(mpw, item2);
        }
    }

    public static void addPartTimeJob(MaplePacketWriter mpw, PartTimeJob parttime) {
        mpw.write(parttime.getJob());
        if (parttime.getJob() > 0 && parttime.getJob() <= 5) {
            mpw.writeReversedLong(parttime.getTime());
        } else {
            mpw.writeReversedLong(System.currentTimeMillis());
        }
        mpw.writeInt(parttime.getReward());
        mpw.write(parttime.getReward() > 0);
    }

    public static <E extends Buffstat> void writeMobMask(MaplePacketWriter mpw, E statup) {
        for (int i = 1; i <= 3; i++) {
            mpw.writeInt(i == statup.getPosition() ? statup.getValue() : 0);
        }
    }
    
    /**
     * Writes a long exp mask to {@code mpw} from {@code expStats}.
     * @param mpw
     * @param expStats
     */
    public static void writeExpMask(MaplePacketWriter mpw, Map<MapleExpStatus, Integer> expStats) {
    	long mask = 0;
    	
    	for (MapleExpStatus expStat : expStats.keySet()) {
    		mask += expStat.getFlag();
    	}
    	
    	mpw.writeLong(mask);
    }
    
    public static <E extends Buffstat> void writeSingleMask(MaplePacketWriter mpw, E statup) {
        for (int i = GameConstants.MAX_BUFFSTAT; i >= 1; i--) {
            mpw.writeInt(i == statup.getPosition() ? statup.getValue() : 0);
        }
    }
    
    /*
    public static <E extends Buffstat> void writeMobStatFlag(MaplePacketLittleEndianWriter mpw, MonsterStatus mse) {
    	int[] flag = new int[GameConstants.MAX_MOBSTAT];
    	
    	for (Buffstat statup : mse) {
            flag[(statup.getPosition() - 1)] += statup.getValue();
        }
        for(int i = 0; i < flag.length; i++) {
        	mpw.writeInt(flag[i]);
        }
    }
    */

   public static <E extends Buffstat> void writeMask(MaplePacketWriter mpw, Collection<E> statups) {
        int[] mask = new int[GameConstants.MAX_BUFFSTAT];
        /*
        if (!statups.contains(MapleBuffStat.MONSTER_RIDING)) {
            mask = new int[12];
        }
        */
        for (Buffstat statup : statups) {
            mask[(statup.getPosition() - 1)] += statup.getValue();
            System.out.println(statup.getValue());
        }
        for(int i = 0; i < mask.length; i++) {
        	mpw.writeInt(mask[i]);
        }
    }

   /* Original v146
    public static <E extends Buffstat> void writeBuffMask(MaplePacketLittleEndianWriter mpw, Collection<Pair<E, Integer>> statups) {
        int[] mask = new int[10];
        if (!statups.contains(MapleBuffStat.MONSTER_RIDING)) {
            mask = new int[12];
        }
        for (Pair statup : statups) {
            mask[(((Buffstat) statup.left).getPosition() - 1)] |= ((Buffstat) statup.left).getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mpw.writeInt(mask[(i - 1)]);
        }
    }
    */

   public static <E extends Buffstat> void writeBuffMask(MaplePacketWriter mpw, Collection<Pair<E, Integer>> statups) {
	   int[] mask = new int[GameConstants.MAX_BUFFSTAT];
	   /*
       if (!statups.contains(MapleBuffStat.MONSTER_RIDING)) {
           mask = new int[12];
       }
       */
       for (Pair statup : statups) {
           mask[(((Buffstat) statup.left).getPosition() - 1)] |= ((Buffstat) statup.left).getValue();
       }
       for(int i = 0; i < mask.length; i++) {
    	   mpw.writeInt(mask[i]);
       }
   }
   
    /* Original v146
    public static <E extends Buffstat> void writeBuffMask(MaplePacketLittleEndianWriter mpw, Map<E, Integer> statups) {
        int[] mask = new int[10];
        if (!statups.containsKey(MapleBuffStat.MONSTER_RIDING)) {
            mask = new int[12];
        }
        for (Buffstat statup : statups.keySet()) {
            mask[(statup.getPosition() - 1)] |= statup.getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mpw.writeInt(mask[(i - 1)]);
        }
    }
    */
    
    public static <E extends Buffstat> void writeBuffMask(MaplePacketWriter mpw, Map<E, Integer> statups) {
        int[] mask = new int[GameConstants.MAX_BUFFSTAT];
        /*
        if (!statups.containsKey(MapleBuffStat.MONSTER_RIDING)) {
            flag = new int[12];
        }
        */
        for (Buffstat statup : statups.keySet()) {
            mask[(statup.getPosition() - 1)] += statup.getValue();
        }
        for(int i = 0; i < mask.length; i++) {
        	mpw.writeInt(mask[i]);
        }
    }
}
