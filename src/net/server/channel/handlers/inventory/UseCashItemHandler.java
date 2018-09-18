package net.server.channel.handlers.inventory;

import java.awt.Rectangle;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleStat;
import client.MonsterFamiliar;
import client.PlayerStats;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.MapleTrait.MapleTraitType;
import client.character.MapleCharacter;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MaplePet.PetFlag;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import net.MaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.InventoryHandler;
import net.packet.CField;
import net.packet.CSPacket;
import net.packet.CWvsContext;
import net.packet.PetPacket;
import net.packet.field.UserPacket;
import net.packet.CField.EffectPacket;
import net.packet.CField.NPCPacket;
import net.packet.CWvsContext.InventoryPacket;
import net.server.channel.ChannelServer;
import net.world.World;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.RandomRewards;
import server.Randomizer;
import server.StructFamiliar;
import server.StructItemOption;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleLifeFactory;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.MapleMist;
import server.quest.MapleQuest;
import server.shops.MapleShopFactory;
import server.stores.HiredMerchant;
import tools.FileoutputUtil;
import tools.data.LittleEndianAccessor;

public class UseCashItemHandler extends AbstractMaplePacketHandler implements MaplePacketHandler {

	public UseCashItemHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		if (c.getCharacter() == null || c.getCharacter().getMap() == null || c.getCharacter().inPVP()) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        c.getCharacter().updateTick(lea.readInt());
        c.getCharacter().setScrolledPosition((short) 0);
        final byte slot = (byte) lea.readShort();
        final int itemId = lea.readInt();

        final Item toUse = c.getCharacter().getInventory(MapleInventoryType.CASH).getItem(slot);
        if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1 || c.getCharacter().hasBlockedInventory()) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }

        boolean used = false, cc = false;

        switch (itemId) {
            case 5222060: {
                c.getCharacter().dropMessage(1, "Unknown error has occurred.");
                System.out.println("slea..." + lea.toString());
                break;
            }
            case 5043001: // NPC Teleport Rock
            case 5043000: { // NPC Teleport Rock
                final short questid = lea.readShort();
                final int npcid = lea.readInt();
                final MapleQuest quest = MapleQuest.getInstance(questid);

                if (c.getCharacter().getQuest(quest).getStatus() == 1 && quest.canComplete(c.getCharacter(), npcid)) {
                    final int mapId = MapleLifeFactory.getNPCLocation(npcid);
                    if (mapId != -1) {
                        final MapleMap map = c.getChannelServer().getMapFactory().getMap(mapId);
                        if (map.containsNPC(npcid) && !FieldLimitType.VipRock.check(c.getCharacter().getMap().getFieldLimit()) && !FieldLimitType.VipRock.check(map.getFieldLimit()) && !c.getCharacter().isInBlockedMap()) {
                            c.getCharacter().changeMap(map, map.getPortal(0));
                        }
                        used = true;
                    } else {
                        c.getCharacter().dropMessage(1, "Unknown error has occurred.");
                    }
                }
                break;
            }
            case 5041001:
            case 5040004:
            case 5040003:
            case 5040002:
            case 2320000: // The Teleport Rock
            case 5041000: // VIP Teleport Rock
            case 5040000: // The Teleport Rock
            case 5040001: { // Teleport Coke
                used = InventoryHandler.UseTeleRock(lea, c, itemId);
                break;
            }
            case 5450005: {
                c.getCharacter().setConversation(4);
                c.getCharacter().getStorage().sendStorage(c, 1022005);
                break;
            }
            case 5050000: { // AP Reset
                Map<MapleStat, Long> statupdate = new EnumMap<>(MapleStat.class);
                final int apto = (int) lea.readLong();
                final int apfrom = (int) lea.readLong();

                if (apto == apfrom) {
                    break; // Hack
                }
                final int job = c.getCharacter().getJob();
                final PlayerStats playerst = c.getCharacter().getStat();
                used = true;

                switch (apto) { // AP to
                    case 0x40: // str
                        if (playerst.getStr() >= 999) {
                            used = false;
                        }
                        break;
                    case 0x80: // dex
                        if (playerst.getDex() >= 999) {
                            used = false;
                        }
                        break;
                    case 0x100: // int
                        if (playerst.getInt() >= 999) {
                            used = false;
                        }
                        break;
                    case 0x200: // luk
                        if (playerst.getLuk() >= 999) {
                            used = false;
                        }
                        break;
                    case 0x800: // hp
                        if (playerst.getMaxHp() >= 500000) {
                            used = false;
                        }
                        break;
                    case 0x2000: // mp
                        if (playerst.getMaxMp() >= 500000) {
                            used = false;
                        }
                        break;
                }
                switch (apfrom) { // AP to
                    case 0x40: // str
                        if (playerst.getStr() <= 4 || (c.getCharacter().getJob() % 1000 / 100 == 1 && playerst.getStr() <= 35)) {
                            used = false;
                        }
                        break;
                    case 0x80: // dex
                        if (playerst.getDex() <= 4 || (c.getCharacter().getJob() % 1000 / 100 == 3 && playerst.getDex() <= 25) || (c.getCharacter().getJob() % 1000 / 100 == 4 && playerst.getDex() <= 25) || (c.getCharacter().getJob() % 1000 / 100 == 5 && playerst.getDex() <= 20)) {
                            used = false;
                        }
                        break;
                    case 0x100: // int
                        if (playerst.getInt() <= 4 || (c.getCharacter().getJob() % 1000 / 100 == 2 && playerst.getInt() <= 20)) {
                            used = false;
                        }
                        break;
                    case 0x200: // luk
                        if (playerst.getLuk() <= 4) {
                            used = false;
                        }
                        break;
                    case 0x800: // hp
                        if (/*playerst.getMaxMp() < ((c.getPlayer().getLevel() * 14) + 134) || */c.getCharacter().getHpApUsed() <= 0 || c.getCharacter().getHpApUsed() >= 10000) {
                            used = false;
                            c.getCharacter().dropMessage(1, "You need points in HP or MP in order to take points out.");
                        }
                        break;
                    case 0x2000: // mp
                        if (/*playerst.getMaxMp() < ((c.getPlayer().getLevel() * 14) + 134) || */c.getCharacter().getHpApUsed() <= 0 || c.getCharacter().getHpApUsed() >= 10000) {
                            used = false;
                            c.getCharacter().dropMessage(1, "You need points in HP or MP in order to take points out.");
                        }
                        break;
                }
                if (used) {
                    switch (apto) { // AP to
                        case 0x40: { // str
                            final long toSet = playerst.getStr() + 1;
                            playerst.setStr((short) toSet, c.getCharacter());
                            statupdate.put(MapleStat.STR, toSet);
                            break;
                        }
                        case 0x80: { // dex
                            final long toSet = playerst.getDex() + 1;
                            playerst.setDex((short) toSet, c.getCharacter());
                            statupdate.put(MapleStat.DEX, toSet);
                            break;
                        }
                        case 0x100: { // int
                            final long toSet = playerst.getInt() + 1;
                            playerst.setInt((short) toSet, c.getCharacter());
                            statupdate.put(MapleStat.INT, toSet);
                            break;
                        }
                        case 0x200: { // luk
                            final long toSet = playerst.getLuk() + 1;
                            playerst.setLuk((short) toSet, c.getCharacter());
                            statupdate.put(MapleStat.LUK, toSet);
                            break;
                        }
                        case 0x800: // hp
                            int maxhp = playerst.getMaxHp();
                            maxhp += GameConstants.getHpApByJob((short) job);
                            c.getCharacter().setHpApUsed((short) (c.getCharacter().getHpApUsed() + 1));
                            playerst.setMaxHp(maxhp, c.getCharacter());
                            statupdate.put(MapleStat.MAXHP, (long) maxhp);
                            break;

                        case 0x2000: // mp
                            int maxmp = playerst.getMaxMp();
                            if (GameConstants.isDemonSlayer(job) || GameConstants.isAngelicBuster(job) || GameConstants.isDemonAvenger(job)) {
                                break;
                            }
                            maxmp += GameConstants.getMpApByJob((short) job);
                            maxmp = Math.min(500000, Math.abs(maxmp));
                            c.getCharacter().setHpApUsed((short) (c.getCharacter().getHpApUsed() + 1));
                            playerst.setMaxMp(maxmp, c.getCharacter());
                            statupdate.put(MapleStat.MAXMP, (long) maxmp);
                            break;
                    }
                    switch (apfrom) { // AP from
                        case 0x40: { // str
                            final long toSet = playerst.getStr() - 1;
                            playerst.setStr((short) toSet, c.getCharacter());
                            statupdate.put(MapleStat.STR, toSet);
                            break;
                        }
                        case 0x80: { // dex
                            final long toSet = playerst.getDex() - 1;
                            playerst.setDex((short) toSet, c.getCharacter());
                            statupdate.put(MapleStat.DEX, toSet);
                            break;
                        }
                        case 0x100: { // int
                            final long toSet = playerst.getInt() - 1;
                            playerst.setInt((short) toSet, c.getCharacter());
                            statupdate.put(MapleStat.INT, toSet);
                            break;
                        }
                        case 0x200: { // luk
                            final long toSet = playerst.getLuk() - 1;
                            playerst.setLuk((short) toSet, c.getCharacter());
                            statupdate.put(MapleStat.LUK, toSet);
                            break;
                        }
                        case 0x800: // HP
                            int maxhp = playerst.getMaxHp();
                            maxhp -= GameConstants.getHpApByJob((short) job);
                            c.getCharacter().setHpApUsed((short) (c.getCharacter().getHpApUsed() - 1));
                            playerst.setMaxHp(maxhp, c.getCharacter());
                            statupdate.put(MapleStat.MAXHP, (long) maxhp);
                            break;
                        case 0x2000: // MP
                            int maxmp = playerst.getMaxMp();
                            if (GameConstants.isDemonSlayer(job) || GameConstants.isAngelicBuster(job) || GameConstants.isDemonAvenger(job)) {
                                break;
                            }
                            maxmp -= GameConstants.getMpApByJob((short) job);
                            c.getCharacter().setHpApUsed((short) (c.getCharacter().getHpApUsed() - 1));
                            playerst.setMaxMp(maxmp, c.getCharacter());
                            statupdate.put(MapleStat.MAXMP, (long) maxmp);
                            break;
                    }
                    c.getSession().write(CWvsContext.updatePlayerStats(statupdate, true, c.getCharacter()));
                }
                break;
            }
            //case 5051001: {
            //    
            //    break;
            //}
            case 5220083: {//starter pack
                used = true;
                for (Entry<Integer, StructFamiliar> f : MapleItemInformationProvider.getInstance().getFamiliars().entrySet()) {
                    if (f.getValue().itemid == 2870055 || f.getValue().itemid == 2871002 || f.getValue().itemid == 2870235 || f.getValue().itemid == 2870019) {
                        MonsterFamiliar mf = c.getCharacter().getFamiliars().get(f.getKey());
                        if (mf != null) {
                            if (mf.getVitality() >= 3) {
                                mf.setExpiry(Math.min(System.currentTimeMillis() + 90 * 24 * 60 * 60000L, mf.getExpiry() + 30 * 24 * 60 * 60000L));
                            } else {
                                mf.setVitality(mf.getVitality() + 1);
                                mf.setExpiry(mf.getExpiry() + 30 * 24 * 60 * 60000L);
                            }
                        } else {
                            mf = new MonsterFamiliar(c.getCharacter().getID(), f.getKey(), System.currentTimeMillis() + 30 * 24 * 60 * 60000L);
                            c.getCharacter().getFamiliars().put(f.getKey(), mf);
                        }
                        c.getSession().write(CField.registerFamiliar(mf));
                    }
                }
                break;
            }
            case 5220084: {//booster pack
                if (c.getCharacter().getInventory(MapleInventoryType.USE).getNumFreeSlot() < 3) {
                    c.getCharacter().dropMessage(5, "Make 3 USE space.");
                    break;
                }
                used = true;
                int[] familiars = new int[3];
                while (true) {
                    for (int i = 0; i < familiars.length; i++) {
                        if (familiars[i] > 0) {
                            continue;
                        }
                        for (Map.Entry<Integer, StructFamiliar> f : MapleItemInformationProvider.getInstance().getFamiliars().entrySet()) {
                            if (Randomizer.nextInt(500) == 0 && ((i < 2 && f.getValue().grade == 0 || (i == 2 && f.getValue().grade != 0)))) {
                                MapleInventoryManipulator.addById(c, f.getValue().itemid, (short) 1, "Booster Pack");
                                //c.getSession().write(CField.getBoosterFamiliar(c.getPlayer().getId(), f.getKey(), 0));
                                familiars[i] = f.getValue().itemid;
                                break;
                            }
                        }
                    }
                    if (familiars[0] > 0 && familiars[1] > 0 && familiars[2] > 0) {
                        break;
                    }
                }
                c.getSession().write(CSPacket.getBoosterPack(familiars[0], familiars[1], familiars[2]));
                c.getSession().write(CSPacket.getBoosterPackClick());
                c.getSession().write(CSPacket.getBoosterPackReveal());
                break;
            }
            case 5050001: // SP Reset (1st job)
            case 5050002: // SP Reset (2nd job)
            case 5050003: // SP Reset (3rd job)
            case 5050004:  // SP Reset (4th job)
            case 5050005: //evan sp resets
            case 5050006:
            case 5050007:
            case 5050008:
            case 5050009: {
                if (itemId >= 5050005 && !GameConstants.isEvan(c.getCharacter().getJob())) {
                    c.getCharacter().dropMessage(1, "This reset is only for Evans.");
                    break;
                } //well i dont really care other than this o.o
                if (itemId < 5050005 && GameConstants.isEvan(c.getCharacter().getJob())) {
                    c.getCharacter().dropMessage(1, "This reset is only for non-Evans.");
                    break;
                } //well i dont really care other than this o.o
                int skill1 = lea.readInt();
                int skill2 = lea.readInt();
                for (int i : GameConstants.blockedSkills) {
                    if (skill1 == i) {
                        c.getCharacter().dropMessage(1, "You may not add this skill.");
                        return;
                    }
                }

                Skill skillSPTo = SkillFactory.getSkill(skill1);
                Skill skillSPFrom = SkillFactory.getSkill(skill2);

                if (skillSPTo.isBeginnerSkill() || skillSPFrom.isBeginnerSkill()) {
                    c.getCharacter().dropMessage(1, "You may not add beginner skills.");
                    break;
                }
                if (GameConstants.getSkillBookForSkill(skill1) != GameConstants.getSkillBookForSkill(skill2)) { //resistance evan
                    c.getCharacter().dropMessage(1, "You may not add different job skills.");
                    break;
                }
                //if (GameConstants.getJobNumber(skill1 / 10000) > GameConstants.getJobNumber(skill2 / 10000)) { //putting 3rd job skillpoints into 4th job for example
                //    c.getPlayer().dropMessage(1, "You may not add skillpoints to a higher job.");
                //    break;
                //}
                if ((c.getCharacter().getSkillLevel(skillSPTo) + 1 <= skillSPTo.getMaxLevel()) && c.getCharacter().getSkillLevel(skillSPFrom) > 0 && skillSPTo.canBeLearnedBy(c.getCharacter().getJob())) {
                    if (skillSPTo.isFourthJob() && (c.getCharacter().getSkillLevel(skillSPTo) + 1 > c.getCharacter().getMasterLevel(skillSPTo))) {
                        c.getCharacter().dropMessage(1, "You will exceed the master level.");
                        break;
                    }
                    if (itemId >= 5050005) {
                        if (GameConstants.getSkillBookForSkill(skill1) != (itemId - 5050005) * 2 && GameConstants.getSkillBookForSkill(skill1) != (itemId - 5050005) * 2 + 1) {
                            c.getCharacter().dropMessage(1, "You may not add this job SP using this reset.");
                            break;
                        }
                    } else {
                        int theJob = GameConstants.getJobNumber(skill2 / 10000);
                        switch (skill2 / 10000) {
                            case 430:
                                theJob = 1;
                                break;
                            case 432:
                            case 431:
                                theJob = 2;
                                break;
                            case 433:
                                theJob = 3;
                                break;
                            case 434:
                                theJob = 4;
                                break;
                        }
                        if (theJob != itemId - 5050000) { //you may only subtract from the skill if the ID matches Sp reset
                            c.getCharacter().dropMessage(1, "You may not subtract from this skill. Use the appropriate SP reset.");
                            break;
                        }
                    }
                    final Map<Skill, SkillEntry> sa = new HashMap<>();
                    sa.put(skillSPFrom, new SkillEntry((byte) (c.getCharacter().getSkillLevel(skillSPFrom) - 1), c.getCharacter().getMasterLevel(skillSPFrom), SkillFactory.getDefaultSExpiry(skillSPFrom)));
                    sa.put(skillSPTo, new SkillEntry((byte) (c.getCharacter().getSkillLevel(skillSPTo) + 1), c.getCharacter().getMasterLevel(skillSPTo), SkillFactory.getDefaultSExpiry(skillSPTo)));
                    c.getCharacter().changeSkillsLevel(sa);
                    used = true;
                }
                break;
            }
            case 5500000: { // Magic Hourglass 1 day
                final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIPPED).getItem(lea.readShort());
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final int days = 1;
                if (item != null && !GameConstants.isAccessory(item.getItemId()) && item.getExpiration() > -1 && !ii.isCash(item.getItemId()) && System.currentTimeMillis() + (100 * 24 * 60 * 60 * 1000L) > item.getExpiration() + (days * 24 * 60 * 60 * 1000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getCharacter().getName().indexOf(z) != -1 || item.getOwner().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + (days * 24 * 60 * 60 * 1000));
                        c.getCharacter().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getCharacter().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5500001: { // Magic Hourglass 7 day
                final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIPPED).getItem(lea.readShort());
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final int days = 7;
                if (item != null && !GameConstants.isAccessory(item.getItemId()) && item.getExpiration() > -1 && !ii.isCash(item.getItemId()) && System.currentTimeMillis() + (100 * 24 * 60 * 60 * 1000L) > item.getExpiration() + (days * 24 * 60 * 60 * 1000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getCharacter().getName().indexOf(z) != -1 || item.getOwner().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + (days * 24 * 60 * 60 * 1000));
                        c.getCharacter().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getCharacter().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5500002: { // Magic Hourglass 20 day
                final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIPPED).getItem(lea.readShort());
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final int days = 20;
                if (item != null && !GameConstants.isAccessory(item.getItemId()) && item.getExpiration() > -1 && !ii.isCash(item.getItemId()) && System.currentTimeMillis() + (100 * 24 * 60 * 60 * 1000L) > item.getExpiration() + (days * 24 * 60 * 60 * 1000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getCharacter().getName().indexOf(z) != -1 || item.getOwner().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + (days * 24 * 60 * 60 * 1000));
                        c.getCharacter().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getCharacter().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5500005: { // Magic Hourglass 50 day
                final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIPPED).getItem(lea.readShort());
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final int days = 50;
                if (item != null && !GameConstants.isAccessory(item.getItemId()) && item.getExpiration() > -1 && !ii.isCash(item.getItemId()) && System.currentTimeMillis() + (100 * 24 * 60 * 60 * 1000L) > item.getExpiration() + (days * 24 * 60 * 60 * 1000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getCharacter().getName().indexOf(z) != -1 || item.getOwner().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + (days * 24 * 60 * 60 * 1000));
                        c.getCharacter().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getCharacter().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5500006: { // Magic Hourglass 99 day
                final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIPPED).getItem(lea.readShort());
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final int days = 99;
                if (item != null && !GameConstants.isAccessory(item.getItemId()) && item.getExpiration() > -1 && !ii.isCash(item.getItemId()) && System.currentTimeMillis() + (100 * 24 * 60 * 60 * 1000L) > item.getExpiration() + (days * 24 * 60 * 60 * 1000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getCharacter().getName().indexOf(z) != -1 || item.getOwner().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + (days * 24 * 60 * 60 * 1000));
                        c.getCharacter().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getCharacter().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5060000: { // Item Tag
                final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIPPED).getItem(lea.readShort());

                if (item != null && item.getOwner().equals("")) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getCharacter().getName().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setOwner(c.getCharacter().getName());
                        c.getCharacter().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    }
                }
                break;
            }
            case 5680015: {
                if (c.getCharacter().getFatigue() > 0) {
                    c.getCharacter().setFatigue(0);
                    used = true;
                }
                break;
            }
            case 5534000: { //tims lab
                final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem((byte) lea.readInt());
                if (item != null) {
                    final Equip eq = (Equip) item;
                    if (eq.getState() == 0) {
                        eq.resetPotential();
                        c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), true, itemId));
                        c.getSession().write(InventoryPacket.scrolledItem(toUse, MapleInventoryType.EQUIP, item, false, true, false));
                        c.getCharacter().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                        c.getSession().write(CField.enchantResult(1));
                        used = true;
                    } else {
                        c.getCharacter().dropMessage(5, "This item's Potential cannot be reset.");
                    }
                } else {
                    c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), false, itemId));
                    c.getSession().write(CField.enchantResult(0));
                }
                break;
            }
            case 5062009:
            case 5062000: { //miracle cube
                final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem((byte) lea.readInt());
                if (item != null && c.getCharacter().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                    final Equip eq = (Equip) item;
                    if (eq.getState() >= 17 && eq.getState() != 20) {
                        boolean potLock = c.getCharacter().getInventory(MapleInventoryType.CASH).findById(5067000) != null;
                        int line = potLock ? lea.readInt() : 0;
                        int toLock = potLock ? lea.readShort() : 0;
                        potLock = checkPotentialLock(c.getCharacter(), eq, line, toLock);
                        if (potLock) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, c.getCharacter().getInventory(MapleInventoryType.CASH).findById(5067000).getPosition(), (short) 1, false);
                        }
                        eq.renewPotential(0, line, toLock, false);
                        c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), true, itemId));
                        c.getSession().write(InventoryPacket.scrolledItem(toUse, MapleInventoryType.EQUIP, item, false, true, false));
                        c.getCharacter().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                        int tofind = 0;
                        if (chr.itemQuantity(2460003) > 0) {
                            tofind = 2460003;
                        } else if (chr.itemQuantity(2460002) > 0) {
                            tofind = 2460002;
                        } else if (chr.itemQuantity(2460001) > 0) {
                            tofind = 2460001;
                        } else if (chr.itemQuantity(2460000) > 0) {
                            tofind = 2460000;
                        }
                        if (tofind != 0) {
                            Item magnify = c.getCharacter().getInventory(MapleInventoryType.USE).findById(tofind);
                            if (magnifyEquip(c, magnify, item, (byte) item.getPosition())) {
                                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, magnify.getPosition(), (short) 1, false);
                                c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass (Premium) has been used."));
                            } else {
                                c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass was not found. The equipment will stay as Hidden Potential."));
                            }
                        } else {
                            c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass was not found. The equipment will stay as Hidden Potential."));
                        }
                        MapleInventoryManipulator.addById(c, 2430112, (short) 1, "Cube" + " on " + FileoutputUtil.CurrentReadable_Date());
                        c.getSession().write(CField.enchantResult(tofind == 0 ? 1 : 1));//3
                        c.getSession().write(CWvsContext.enableActions());
                        used = true;
                    } else {
                        c.getCharacter().dropMessage(5, "This item's Potential cannot be reset.");
                    }
                } else {
                    c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), false, itemId));
                    c.getSession().write(CField.enchantResult(0));
                }
                break;
            }
            case 5062100:
            case 5062001: { //premium cube
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(1, "You may not use this until level 10.");
                } else {
                    final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem((byte) lea.readInt());
                    if (item != null && c.getCharacter().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                        final Equip eq = (Equip) item;
                        if (eq.getState() >= 17 && eq.getState() != 20) {
                            eq.renewPotential(1, 0, (short) 0, false);
                            c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), true, itemId));
                            c.getSession().write(InventoryPacket.scrolledItem(toUse, MapleInventoryType.EQUIP, item, false, true, false));
                            c.getCharacter().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                            int tofind = 0;
                            if (chr.itemQuantity(2460003) > 0) {
                                tofind = 2460003;
                            } else if (chr.itemQuantity(2460002) > 0) {
                                tofind = 2460002;
                            } else if (chr.itemQuantity(2460001) > 0) {
                                tofind = 2460001;
                            } else if (chr.itemQuantity(2460000) > 0) {
                                tofind = 2460000;
                            }
                            if (tofind != 0) {
                                Item magnify = c.getCharacter().getInventory(MapleInventoryType.USE).findById(tofind);
                                if (magnifyEquip(c, magnify, item, (byte) item.getPosition())) {
                                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, magnify.getPosition(), (short) 1, false);
                                    c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass (Premium) has been used."));
                                } else {
                                    c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass was not found. The equipment will stay as Hidden Potential."));
                                }
                            } else {
                                c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass was not found. The equipment will stay as Hidden Potential."));
                            }
                            MapleInventoryManipulator.addById(c, 2430112, (short) 1, "Cube" + " on " + FileoutputUtil.CurrentReadable_Date());
                            c.getSession().write(CField.enchantResult(tofind == 0 ? 1 : 1));//3
                            used = true;
                        } else {
                            c.getCharacter().dropMessage(5, "This item's Potential cannot be reset.");
                        }
                    } else {
                        c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), false, itemId));
                        c.getSession().write(CField.enchantResult(0));
                    }
                }
                break;
            }
            case 5062002: { //super miracle cube
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(1, "You may not use this until level 10.");
                } else {
                    final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem((byte) lea.readInt());
                    if (item != null && c.getCharacter().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                        final Equip eq = (Equip) item;
                        if (eq.getState() >= 17) {
                            eq.renewPotential(3, 0, (short) 0, false);
                            c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), true, itemId));
                            c.getSession().write(InventoryPacket.scrolledItem(toUse, MapleInventoryType.EQUIP, item, false, true, false));
                            c.getCharacter().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                            int tofind = 0;
                            if (chr.itemQuantity(2460003) > 0) {
                                tofind = 2460003;
                            } else if (chr.itemQuantity(2460002) > 0) {
                                tofind = 2460002;
                            } else if (chr.itemQuantity(2460001) > 0) {
                                tofind = 2460001;
                            } else if (chr.itemQuantity(2460000) > 0) {
                                tofind = 2460000;
                            }
                            if (tofind != 0) {
                                Item magnify = c.getCharacter().getInventory(MapleInventoryType.USE).findById(tofind);
                                if (magnifyEquip(c, magnify, item, (byte) item.getPosition())) {
                                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, magnify.getPosition(), (short) 1, false);
                                    c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass (Premium) has been used."));
                                } else {
                                    c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass was not found. The equipment will stay as Hidden Potential."));
                                }
                            } else {
                                c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass was not found. The equipment will stay as Hidden Potential."));
                            }
                            MapleInventoryManipulator.addById(c, 2430481, (short) 1, "Cube" + " on " + FileoutputUtil.CurrentReadable_Date());
                            c.getSession().write(CField.enchantResult(tofind == 0 ? 1 : 1));//3
                            used = true;
                        } else {
                            c.getCharacter().dropMessage(5, "This item's Potential cannot be reset.");
                        }
                    } else {
                        c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), false, itemId));
                        c.getSession().write(CField.enchantResult(0));
                    }
                }
                break;
            }
            case 5062003: { //revolutionary cube
                final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem((byte) lea.readInt());
                if (item != null && c.getCharacter().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                    final Equip eq = (Equip) item;
                    if (eq.getState() >= 17) {
                        boolean potLock = c.getCharacter().getInventory(MapleInventoryType.CASH).findById(5067000) != null;
                        int line = potLock ? lea.readInt() : 0;
                        short toLock = potLock ? lea.readShort() : 0;
                        potLock = checkPotentialLock(c.getCharacter(), eq, line, toLock);
                        if (potLock) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, c.getCharacter().getInventory(MapleInventoryType.CASH).findById(5067000).getPosition(), (short) 1, false);
                        }
                        eq.renewPotential(4, line, toLock, false);
                        c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), true, itemId));
                        c.getSession().write(InventoryPacket.scrolledItem(toUse, MapleInventoryType.EQUIP, item, false, true, false));
                        c.getCharacter().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                        int tofind = 0;
                        if (chr.itemQuantity(2460003) > 0) {
                            tofind = 2460003;
                        } else if (chr.itemQuantity(2460002) > 0) {
                            tofind = 2460002;
                        } else if (chr.itemQuantity(2460001) > 0) {
                            tofind = 2460001;
                        } else if (chr.itemQuantity(2460000) > 0) {
                            tofind = 2460000;
                        }
                        if (tofind != 0) {
                            Item magnify = c.getCharacter().getInventory(MapleInventoryType.USE).findById(tofind);
                            if (magnifyEquip(c, magnify, item, (byte) item.getPosition())) {
                                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, magnify.getPosition(), (short) 1, false);
                                c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass (Premium) has been used."));
                            } else {
                                c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass was not found. The equipment will stay as Hidden Potential."));
                            }
                        } else {
                            c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass was not found. The equipment will stay as Hidden Potential."));
                        }
                        MapleInventoryManipulator.addById(c, 2430481, (short) 1, "Cube" + " on " + FileoutputUtil.CurrentReadable_Date());
                        c.getSession().write(CField.enchantResult(tofind == 0 ? 1 : 1));//3
                        used = true;
                    } else {
                        c.getCharacter().dropMessage(5, "This item's Potential cannot be reset.");
                    }
                } else {
                    c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), false, itemId));
                    c.getSession().write(CField.enchantResult(0));
                }
                break;
            }
            case 5062500:
            case 5062005: { //enlightening cube
                final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem((byte) lea.readInt());
                if (item != null && c.getCharacter().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                    final Equip eq = (Equip) item;
                    if (eq.getState() >= 17) {
                        eq.renewPotential(5, 0, (short) 0, false);
                        c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), true, itemId));
                        c.getSession().write(InventoryPacket.scrolledItem(toUse, MapleInventoryType.EQUIP, item, false, true, false));
                        c.getCharacter().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                        int tofind = 0;
                        if (chr.itemQuantity(2460003) > 0) {
                            tofind = 2460003;
                        } else if (chr.itemQuantity(2460002) > 0) {
                            tofind = 2460002;
                        } else if (chr.itemQuantity(2460001) > 0) {
                            tofind = 2460001;
                        } else if (chr.itemQuantity(2460000) > 0) {
                            tofind = 2460000;
                        }
                        if (tofind != 0) {
                            Item magnify = c.getCharacter().getInventory(MapleInventoryType.USE).findById(tofind);
                            if (magnifyEquip(c, magnify, item, (byte) item.getPosition())) {
                                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, magnify.getPosition(), (short) 1, false);
                                c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass (Premium) has been used."));
                            } else {
                                c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass was not found. The equipment will stay as Hidden Potential."));
                            }
                        } else {
                            c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass was not found. The equipment will stay as Hidden Potential."));
                        }
                        MapleInventoryManipulator.addById(c, 2430759, (short) 1, "Cube" + " on " + FileoutputUtil.CurrentReadable_Date());
                        c.getSession().write(CField.enchantResult(tofind == 0 ? 1 : 1));//3
                        used = true;
                    } else {
                        c.getCharacter().dropMessage(5, "This item's Potential cannot be reset.");
                    }
                } else {
                    c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), false, itemId));
                    c.getSession().write(CField.enchantResult(0));
                }
                break;
            }
            case 5062006: {
                final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem((byte) lea.readInt());
                if (item != null && c.getCharacter().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                    final Equip eq = (Equip) item;
                    if (eq.getState() >= 17) {
                        eq.renewPotential(6, 0, (short) 0, false);
                        c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), true, itemId));
                        c.getSession().write(InventoryPacket.scrolledItem(toUse, MapleInventoryType.EQUIP, item, false, true, false));
                        c.getCharacter().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                        int tofind = 0;
                        if (chr.itemQuantity(2460003) > 0) {
                            tofind = 2460003;
                        } else if (chr.itemQuantity(2460002) > 0) {
                            tofind = 2460002;
                        } else if (chr.itemQuantity(2460001) > 0) {
                            tofind = 2460001;
                        } else if (chr.itemQuantity(2460000) > 0) {
                            tofind = 2460000;
                        }
                    if (tofind != 0) {
                        Item magnify = c.getCharacter().getInventory(MapleInventoryType.USE).findById(tofind);
                        if (magnifyEquip(c, magnify, item, (byte) item.getPosition())) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, magnify.getPosition(), (short) 1, false);
                            c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass (Premium) has been used."));
                        } else {
                            c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass was not found. The equipment will stay as Hidden Potential."));
                        }
                    } else {
                        c.getSession().write(CField.getGameMessage((short) 7, "A Magnifying Glass was not found. The equipment will stay as Hidden Potential."));
                    }
                    MapleInventoryManipulator.addById(c, 2430759, (short) 1, "Cube" + " on " + FileoutputUtil.CurrentReadable_Date());
                    c.getSession().write(CField.enchantResult(tofind == 0 ? 1 : 1));//3
                    used = true;
                } else {
                    c.getCharacter().dropMessage(5, "This item's Potential cannot be reset.");
                }
            } else {
                c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), false, itemId));
                c.getSession().write(CField.enchantResult(0));
                }
                break;
            }
            case 5062300: { //white awakening stamp
                final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem((byte) lea.readInt());
                if (item != null) {
                    final Equip eq = (Equip) item;
                    if (eq.getState() < 17) {
                        c.getCharacter().dropMessage(5, "This item's Potential cannot be reset.");
                        return;
                    }
                    if (eq.getPotential3() != 0) {
                        c.getCharacter().dropMessage(5, "Cannot be used on this item.");
                        return;
                    }
                    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    final List<List<StructItemOption>> pots = new LinkedList<>(ii.getAllPotentialInfo().values());
                    final int reqLevel = ii.getReqLevel(eq.getItemId()) / 10;
                    int new_state = Math.abs(eq.getPotential1());
                    if (new_state > 20 || new_state < 17) { // incase overflow
                        new_state = 17;
                    }
                    boolean rewarded = false;
                    while (!rewarded) {
                        StructItemOption pot = pots.get(Randomizer.nextInt(pots.size())).get(reqLevel);
                        if (pot != null && pot.reqLevel / 10 <= reqLevel && GameConstants.optionTypeFits(pot.optionType, eq.getItemId()) && GameConstants.potentialIDFits(pot.opID, new_state, 3)) { //optionType
                            eq.setPotential3(pot.opID);
                            rewarded = true;
                        }
                    }
                    c.getSession().write(InventoryPacket.scrolledItem(toUse, MapleInventoryType.EQUIP, item, false, true, false));
                    c.getCharacter().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                    used = true;
                }
                break;
            }
            case 5062400:
            case 5062401:
            case 5062402:
            case 5062403: {
                short appearance = (short) lea.readInt();
                short function = (short) lea.readInt();
                Equip appear = (Equip) c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem(appearance);
                Equip equip = (Equip) c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem(function);
                if (equip.getFusionAnvil() != 0) {
                    return;
                } else if (GameConstants.isEquip(appear.getItemId()) || GameConstants.isEquip(equip.getItemId())) {
                    if (appear.getItemId() / 10000 != equip.getItemId() / 10000) {
                        return;
                    }
                } else if (appear.getItemId() / 100000 != equip.getItemId() / 100000) {
                    return;
                }
                equip.setFusionAnvil(appear.getItemId());
                c.getCharacter().forceReAddItem_NoUpdate(equip, MapleInventoryType.EQUIP);
                used = true;
                break;
            }
            case 5750000: { //alien cube
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(1, "You may not use this until level 10.");
                } else {
                    final Item item = c.getCharacter().getInventory(MapleInventoryType.SETUP).getItem((byte) lea.readInt());
                    if (item != null && c.getCharacter().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1 && c.getCharacter().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() >= 1) {
                        final int grade = GameConstants.getNebuliteGrade(item.getItemId());
                        if (grade != -1 && grade < 4) {
                            final int rank = Randomizer.nextInt(100) < 7 ? (Randomizer.nextInt(100) < 2 ? (grade + 1) : (grade != 3 ? (grade + 1) : grade)) : grade;
                            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                            final List<StructItemOption> pots = new LinkedList<>(ii.getAllSocketInfo(rank).values());
                            int newId = 0;
                            while (newId == 0) {
                                StructItemOption pot = pots.get(Randomizer.nextInt(pots.size()));
                                if (pot != null) {
                                    newId = pot.opID;
                                }
                            }
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.SETUP, item.getPosition(), (short) 1, false);
                            MapleInventoryManipulator.addById(c, newId, (short) 1, "Upgraded from alien cube on " + FileoutputUtil.CurrentReadable_Date());
                            MapleInventoryManipulator.addById(c, 2430691, (short) 1, "Alien Cube" + " on " + FileoutputUtil.CurrentReadable_Date());
                            used = true;
                        } else {
                            c.getCharacter().dropMessage(1, "Grade S Nebulite cannot be added.");
                        }
                    } else {
                        c.getCharacter().dropMessage(5, "You do not have sufficient inventory slot.");
                    }
                }
                break;
            }
            case 5750001: { // socket diffuser
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(1, "You may not use this until level 10.");
                } else {
                    final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem((byte) lea.readInt());
                    if (item != null) {
                        final Equip eq = (Equip) item;
                        if (eq.getSocket1() > 0) { // first slot only.
                            eq.setSocket1(0);
                            c.getSession().write(InventoryPacket.scrolledItem(toUse, MapleInventoryType.EQUIP, item, false, true, false));
                            c.getCharacter().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                            used = true;
                        } else {
                            c.getCharacter().dropMessage(5, "This item do not have a socket.");
                        }
                    } else {
                        c.getCharacter().dropMessage(5, "This item's nebulite cannot be removed.");
                    }
                }
                break;
            }
            case 5521000: { // Karma
                final MapleInventoryType type = MapleInventoryType.getByType((byte) lea.readInt());
                final Item item = c.getCharacter().getInventory(type).getItem((byte) lea.readInt());

                if (item != null && !ItemFlag.KARMA_ACC.check(item.getFlag())
                        && !ItemFlag.KARMA_ACC_USE.check(item.getFlag())
                        && GameConstants.isEquip(item.getItemId())
                        && ((Equip) item).getKarmaCount() != 0) {
                    Equip eq = (Equip) item;
                    if (MapleItemInformationProvider.getInstance().isShareTagEnabled(item.getItemId())) {
                        short flag = item.getFlag();
                        if (ItemFlag.UNTRADABLE.check(flag)) {
                            flag -= ItemFlag.UNTRADABLE.getValue();
                        } else if (type == MapleInventoryType.EQUIP) {
                            flag |= ItemFlag.KARMA_ACC.getValue();
                        } else {
                            flag |= ItemFlag.KARMA_ACC_USE.getValue();
                        }
                        item.setFlag(flag);
                        eq.setKarmaCount((byte) (eq.getKarmaCount() - 1));
                        c.getCharacter().forceReAddItem_NoUpdate(item, type);
                        c.getSession().write(InventoryPacket.updateSpecialItemUse(item, type.getType(), item.getPosition(), true, c.getCharacter()));
                        used = true;
                    }
                }
                break;
            }
            case 5520001: //p.karma
            case 5520000: { // Karma
                final MapleInventoryType type = MapleInventoryType.getByType((byte) lea.readInt());
                final Item item = c.getCharacter().getInventory(type).getItem((byte) lea.readInt());

                if (item != null && !ItemFlag.KARMA_EQ.check(item.getFlag())
                        && !ItemFlag.KARMA_USE.check(item.getFlag())
                        && GameConstants.isEquip(item.getItemId())
                        && ((Equip) item).getKarmaCount() != 0) {
                    Equip eq = (Equip) item;
                    if ((itemId == 5520000 && MapleItemInformationProvider.getInstance().isKarmaEnabled(item.getItemId())) || (itemId == 5520001 && MapleItemInformationProvider.getInstance().isPKarmaEnabled(item.getItemId()))) {
                        short flag = item.getFlag();
                        if (ItemFlag.UNTRADABLE.check(flag)) {
                            flag -= ItemFlag.UNTRADABLE.getValue();
                        } else if (type == MapleInventoryType.EQUIP) {
                            flag |= ItemFlag.KARMA_EQ.getValue();
                        } else {
                            flag |= ItemFlag.KARMA_USE.getValue();
                        }
                        item.setFlag(flag);
                        eq.setKarmaCount((byte) (eq.getKarmaCount() - 1));
                        c.getCharacter().forceReAddItem_NoUpdate(item, type);
                        c.getSession().write(InventoryPacket.updateSpecialItemUse(item, type.getType(), item.getPosition(), true, c.getCharacter()));
                        used = true;
                    }
                }
                break;
            }
            case 5570000: { // Vicious Hammer
                lea.readInt(); // Inventory type, Hammered eq is always EQ.
                final Equip item = (Equip) c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem((byte) lea.readInt());
                // another int here, D3 49 DC 00
                if (item != null) {
                    if (GameConstants.canHammer(item.getItemId()) && MapleItemInformationProvider.getInstance().getSlots(item.getItemId()) > 0 && item.getViciousHammer() < 2) {
                        item.setViciousHammer((byte) (item.getViciousHammer() + 1));
                        item.setUpgradeSlots((byte) (item.getUpgradeSlots() + 1));
                        c.getCharacter().forceReAddItem(item, MapleInventoryType.EQUIP);
                        c.getSession().write(CSPacket.ViciousHammer(true, item.getViciousHammer()));
                        used = true;
                    } else {
                        c.getCharacter().dropMessage(5, "You may not use it on this item.");
                        c.getSession().write(CSPacket.ViciousHammer(true, (byte) 0));
                    }
                }
                break;
            }
            case 5610001:
            case 5610000: { // Vega 30
                lea.readInt(); // Inventory type, always eq
                final short dst = (short) lea.readInt();
                lea.readInt(); // Inventory type, always use
                final short src = (short) lea.readInt();
                used = InventoryHandler.UseUpgradeScroll(src, dst, (short) 2, c, c.getCharacter(), itemId, false); //cannot use ws with vega but we dont care
                cc = used;
                break;
            }
            case 5060001: { // Sealing Lock
                final MapleInventoryType type = MapleInventoryType.getByType((byte) lea.readInt());
                final Item item = c.getCharacter().getInventory(type).getItem((byte) lea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getExpiration() == -1) {
                    short flag = item.getFlag();
                    flag |= ItemFlag.LOCK.getValue();
                    item.setFlag(flag);

                    c.getCharacter().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5061000: { // Sealing Lock 7 days
                final MapleInventoryType type = MapleInventoryType.getByType((byte) lea.readInt());
                final Item item = c.getCharacter().getInventory(type).getItem((byte) lea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getExpiration() == -1) {
                    short flag = item.getFlag();
                    flag |= ItemFlag.LOCK.getValue();
                    item.setFlag(flag);
                    item.setExpiration(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000));

                    c.getCharacter().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5061001: { // Sealing Lock 30 days
                final MapleInventoryType type = MapleInventoryType.getByType((byte) lea.readInt());
                final Item item = c.getCharacter().getInventory(type).getItem((byte) lea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getExpiration() == -1) {
                    short flag = item.getFlag();
                    flag |= ItemFlag.LOCK.getValue();
                    item.setFlag(flag);

                    item.setExpiration(System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000));

                    c.getCharacter().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5061002: { // Sealing Lock 90 days
                final MapleInventoryType type = MapleInventoryType.getByType((byte) lea.readInt());
                final Item item = c.getCharacter().getInventory(type).getItem((byte) lea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getExpiration() == -1) {
                    short flag = item.getFlag();
                    flag |= ItemFlag.LOCK.getValue();
                    item.setFlag(flag);

                    item.setExpiration(System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000));

                    c.getCharacter().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5061003: { // Sealing Lock 365 days
                final MapleInventoryType type = MapleInventoryType.getByType((byte) lea.readInt());
                final Item item = c.getCharacter().getInventory(type).getItem((byte) lea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getExpiration() == -1) {
                    short flag = item.getFlag();
                    flag |= ItemFlag.LOCK.getValue();
                    item.setFlag(flag);

                    item.setExpiration(System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000));

                    c.getCharacter().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5063000: {
                final MapleInventoryType type = MapleInventoryType.getByType((byte) lea.readInt());
                final Item item = c.getCharacter().getInventory(type).getItem((byte) lea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getType() == 1) { //equip
                    short flag = item.getFlag();
                    flag |= ItemFlag.LUCKY_DAY.getValue();
                    item.setFlag(flag);

                    c.getCharacter().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5064000: {
                //System.out.println("slea..." + slea.toString());
                final MapleInventoryType type = MapleInventoryType.getByType((byte) lea.readInt());
                final Item item = c.getCharacter().getInventory(type).getItem((byte) lea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getType() == 1) { //equip
                    if (((Equip) item).getEnhance() >= 12) {
                        break; //cannot be used
                    }
                    short flag = item.getFlag();
                    flag |= ItemFlag.SHIELD_WARD.getValue();
                    item.setFlag(flag);

                    c.getCharacter().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }

            case 5060003:
            case 5060004:
            case 5060005:
            case 5060006:
            case 5060007: {
                Item item = c.getCharacter().getInventory(MapleInventoryType.ETC).findById(itemId == 5060003 ? 4170023 : 4170024);
                if (item == null || item.getQuantity() <= 0) { // hacking{
                    return;
                }
                if (getIncubatedItems(c, itemId)) {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, item.getPosition(), (short) 1, false);
                    used = true;
                }
                break;
            }

            case 5070000: { // Megaphone
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getCharacter().getMapId() == GameConstants.JAIL) {
                    c.getCharacter().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getCharacter().getCheatTracker().canSmega()) {
                    c.getCharacter().dropMessage(5, "You may only use this every 15 seconds.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = lea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getCharacter(), sb);
                    sb.append(c.getCharacter().getName());
                    sb.append(" : ");
                    sb.append(message);

                    c.getCharacter().getMap().broadcastMessage(CWvsContext.broadcastMsg(2, sb.toString()));
                    used = true;
                } else {
                    c.getCharacter().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5071000: { // Megaphone
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getCharacter().getMapId() == GameConstants.JAIL) {
                    c.getCharacter().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getCharacter().getCheatTracker().canSmega()) {
                    c.getCharacter().dropMessage(5, "You may only use this every 15 seconds.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = lea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getCharacter(), sb);
                    sb.append(c.getCharacter().getName());
                    sb.append(" : ");
                    sb.append(message);

                    c.getChannelServer().broadcastSmegaPacket(CWvsContext.broadcastMsg(2, sb.toString()));
                    used = true;
                } else {
                    c.getCharacter().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5077000: { // 3 line Megaphone
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getCharacter().getMapId() == GameConstants.JAIL) {
                    c.getCharacter().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getCharacter().getCheatTracker().canSmega()) {
                    c.getCharacter().dropMessage(5, "You may only use this every 15 seconds.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final byte numLines = lea.readByte();
                    if (numLines > 3) {
                        return;
                    }
                    final List<String> messages = new LinkedList<>();
                    String message;
                    for (int i = 0; i < numLines; i++) {
                        message = lea.readMapleAsciiString();
                        if (message.length() > 65) {
                            break;
                        }
                        messages.add(c.getCharacter().getName() + " : " + message);
                    }
                    final boolean ear = lea.readByte() > 0;

                    World.Broadcast.broadcastSmega(CWvsContext.tripleSmega(messages, ear, c.getChannel()));
                    used = true;
                } else {
                    c.getCharacter().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5079004: { // Heart Megaphone
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getCharacter().getMapId() == GameConstants.JAIL) {
                    c.getCharacter().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getCharacter().getCheatTracker().canSmega()) {
                    c.getCharacter().dropMessage(5, "You may only use this every 15 seconds.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = lea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    World.Broadcast.broadcastSmega(CWvsContext.echoMegaphone(c.getCharacter().getName(), message));
                    used = true;
                } else {
                    c.getCharacter().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5073000: { // Heart Megaphone
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getCharacter().getMapId() == GameConstants.JAIL) {
                    c.getCharacter().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getCharacter().getCheatTracker().canSmega()) {
                    c.getCharacter().dropMessage(5, "You may only use this every 15 seconds.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = lea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getCharacter(), sb);
                    sb.append(c.getCharacter().getName());
                    sb.append(" : ");
                    sb.append(message);

                    final boolean ear = lea.readByte() != 0;
                    World.Broadcast.broadcastSmega(CWvsContext.broadcastMsg(9, c.getChannel(), sb.toString(), ear));
                    used = true;
                } else {
                    c.getCharacter().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5074000: { // Skull Megaphone
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getCharacter().getMapId() == GameConstants.JAIL) {
                    c.getCharacter().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getCharacter().getCheatTracker().canSmega()) {
                    c.getCharacter().dropMessage(5, "You may only use this every 15 seconds.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = lea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getCharacter(), sb);
                    sb.append(c.getCharacter().getName());
                    sb.append(" : ");
                    sb.append(message);

                    final boolean ear = lea.readByte() != 0;

                    World.Broadcast.broadcastSmega(CWvsContext.broadcastMsg(22, c.getChannel(), sb.toString(), ear));
                    used = true;
                } else {
                    c.getCharacter().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5072000: { // Super Megaphone
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getCharacter().getMapId() == GameConstants.JAIL) {
                    c.getCharacter().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getCharacter().getCheatTracker().canSmega()) {
                    c.getCharacter().dropMessage(5, "You may only use this every 15 seconds.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = lea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getCharacter(), sb);
                    sb.append(c.getCharacter().getName());
                    sb.append(" : ");
                    sb.append(message);

                    final boolean ear = lea.readByte() != 0;

                    World.Broadcast.broadcastSmega(CWvsContext.broadcastMsg(3, c.getChannel(), sb.toString(), ear));
                    used = true;
                } else {
                    c.getCharacter().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5076000: { // Item Megaphone
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getCharacter().getMapId() == GameConstants.JAIL) {
                    c.getCharacter().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getCharacter().getCheatTracker().canSmega()) {
                    c.getCharacter().dropMessage(5, "You may only use this every 15 seconds.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = lea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getCharacter(), sb);
                    sb.append(c.getCharacter().getName());
                    sb.append(" : ");
                    sb.append(message);

                    final boolean ear = lea.readByte() > 0;

                    Item item = null;
                    if (lea.readByte() == 1) { //item
                        byte invType = (byte) lea.readInt();
                        byte pos = (byte) lea.readInt();
                        if (pos <= 0) {
                            invType = -1;
                        }
                        item = c.getCharacter().getInventory(MapleInventoryType.getByType(invType)).getItem(pos);
                    }
                    World.Broadcast.broadcastSmega(CWvsContext.itemMegaphone(sb.toString(), ear, c.getChannel(), item));
                    used = true;
                } else {
                    c.getCharacter().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5079000: {
                break;
            }
            case 5079001:
            case 5079002: {
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getCharacter().getMapId() == GameConstants.JAIL) {
                    c.getCharacter().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getCharacter().getCheatTracker().canSmega()) {
                    c.getCharacter().dropMessage(5, "You may only use this every 15 seconds.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = lea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getCharacter(), sb);
                    sb.append(c.getCharacter().getName());
                    sb.append(" : ");
                    sb.append(message);

                    final boolean ear = lea.readByte() != 0;

                    World.Broadcast.broadcastSmega(CWvsContext.broadcastMsg(24 + itemId % 10, c.getChannel(), sb.toString(), ear));
                    used = true;
                } else {
                    c.getCharacter().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5075000: // MapleTV Messenger
            case 5075001: // MapleTV Star Messenger
            case 5075002: { // MapleTV Heart Messenger
                c.getCharacter().dropMessage(5, "There are no MapleTVs to broadcast the message to.");
                break;
            }
            case 5075003:
            case 5075004:
            case 5075005: {
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getCharacter().getMapId() == GameConstants.JAIL) {
                    c.getCharacter().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getCharacter().getCheatTracker().canSmega()) {
                    c.getCharacter().dropMessage(5, "You may only use this every 15 seconds.");
                    break;
                }
                int tvType = itemId % 10;
                if (tvType == 3) {
                    lea.readByte(); //who knows
                }
                boolean ear = tvType != 1 && tvType != 2 && lea.readByte() > 1; //for tvType 1/2, there is no byte. 
                MapleCharacter victim = tvType == 1 || tvType == 4 ? null : c.getChannelServer().getPlayerStorage().getCharacterByName(lea.readMapleAsciiString()); //for tvType 4, there is no string.
                if (tvType == 0 || tvType == 3) { //doesn't allow two
                    victim = null;
                } else if (victim == null) {
                    c.getCharacter().dropMessage(1, "That character is not in the channel.");
                    break;
                }
                String message = lea.readMapleAsciiString();
                World.Broadcast.broadcastSmega(CWvsContext.broadcastMsg(3, c.getChannel(), c.getCharacter().getName() + " : " + message, ear));
                used = true;
                break;
            }
            case 5090100: // Wedding Invitation Card
            case 5090000: { // Note
                final String sendTo = lea.readMapleAsciiString();
                final String msg = lea.readMapleAsciiString();
                if (c.getChannelServer().getPlayerStorage().getCharacterByName(sendTo) != null) {
                    c.getSession().write(CSPacket.OnMemoResult((byte) 5, (byte) 0));
                    break;
                }
                c.getCharacter().sendNote(sendTo, msg);
                c.getSession().write(CSPacket.OnMemoResult((byte) 4, (byte) 0));
                used = true;
                break;
            }
            case 5100000: { // Congratulatory Song
                c.getCharacter().getMap().broadcastMessage(CField.musicChange("Jukebox/Congratulation"));
                used = true;
                break;
            }
            case 5190001:
            case 5190002:
            case 5190003:
            case 5190004:
            case 5190005:
            case 5190006:
            case 5190007:
            case 5190008:
            case 5190000: { // Pet Flags
                final int uniqueid = (int) lea.readLong();
                MaplePet pet = c.getCharacter().getPet(0);
                int slo = 0;

                if (pet == null) {
                    break;
                }
                if (pet.getUniqueId() != uniqueid) {
                    pet = c.getCharacter().getPet(1);
                    slo = 1;
                    if (pet != null) {
                        if (pet.getUniqueId() != uniqueid) {
                            pet = c.getCharacter().getPet(2);
                            slo = 2;
                            if (pet != null) {
                                if (pet.getUniqueId() != uniqueid) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
                PetFlag zz = PetFlag.getByAddId(itemId);
                if (zz != null && !zz.check(pet.getFlags())) {
                    pet.setFlags(pet.getFlags() | zz.getValue());
                    c.getSession().write(PetPacket.updatePet(pet, c.getCharacter().getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition()), true));
                    c.getSession().write(CWvsContext.enableActions());
                    c.getSession().write(CSPacket.changePetFlag(uniqueid, true, zz.getValue()));
                    used = true;
                }
                break;
            }
            case 5191001:
            case 5191002:
            case 5191003:
            case 5191004:
            case 5191000: { // Pet Flags
                final int uniqueid = (int) lea.readLong();
                MaplePet pet = c.getCharacter().getPet(0);
                int slo = 0;

                if (pet == null) {
                    break;
                }
                if (pet.getUniqueId() != uniqueid) {
                    pet = c.getCharacter().getPet(1);
                    slo = 1;
                    if (pet != null) {
                        if (pet.getUniqueId() != uniqueid) {
                            pet = c.getCharacter().getPet(2);
                            slo = 2;
                            if (pet != null) {
                                if (pet.getUniqueId() != uniqueid) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
                PetFlag zz = PetFlag.getByDelId(itemId);
                if (zz != null && zz.check(pet.getFlags())) {
                    pet.setFlags(pet.getFlags() - zz.getValue());
                    c.getSession().write(PetPacket.updatePet(pet, c.getCharacter().getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition()), true));
                    c.getSession().write(CWvsContext.enableActions());
                    c.getSession().write(CSPacket.changePetFlag(uniqueid, false, zz.getValue()));
                    used = true;
                }
                break;
            }
            case 5501001:
            case 5501002: { //expiry mount
                final Skill skil = SkillFactory.getSkill(lea.readInt());
                if (skil == null || skil.getId() / 10000 != 8000 || c.getCharacter().getSkillLevel(skil) <= 0 || !skil.isTimeLimited() || GameConstants.getMountItem(skil.getId(), c.getCharacter()) <= 0) {
                    break;
                }
                final long toAdd = (itemId == 5501001 ? 30 : 60) * 24 * 60 * 60 * 1000L;
                final long expire = c.getCharacter().getSkillExpiry(skil);
                if (expire < System.currentTimeMillis() || expire + toAdd >= System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L)) {
                    break;
                }
                c.getCharacter().changeSingleSkillLevel(skil, c.getCharacter().getSkillLevel(skil), c.getCharacter().getMasterLevel(skil), expire + toAdd);
                used = true;
                break;
            }
            case 5170000: { // Pet name change
                final int uniqueid = (int) lea.readLong();
                MaplePet pet = c.getCharacter().getPet(0);
                int slo = 0;

                if (pet == null) {
                    break;
                }
                if (pet.getUniqueId() != uniqueid) {
                    pet = c.getCharacter().getPet(1);
                    slo = 1;
                    if (pet != null) {
                        if (pet.getUniqueId() != uniqueid) {
                            pet = c.getCharacter().getPet(2);
                            slo = 2;
                            if (pet != null) {
                                if (pet.getUniqueId() != uniqueid) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
                String nName = lea.readMapleAsciiString();
                for (String z : GameConstants.RESERVED) {
                    if (pet.getName().indexOf(z) != -1 || nName.indexOf(z) != -1) {
                        break;
                    }
                }
                if (MapleCharacterUtil.canChangePetName(nName)) {
                    pet.setName(nName);
                    c.getSession().write(PetPacket.updatePet(pet, c.getCharacter().getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition()), true));
                    c.getSession().write(CWvsContext.enableActions());
                    c.getCharacter().getMap().broadcastMessage(PetPacket.changePetName(c.getCharacter().getID(), slo, nName));
                    used = true;
                }
                break;
            }
            case 5700000: {
                lea.skip(8);
                if (c.getCharacter().getAndroid() == null) {
                    break;
                }
                String nName = lea.readMapleAsciiString();
                for (String z : GameConstants.RESERVED) {
                    if (c.getCharacter().getAndroid().getName().indexOf(z) != -1 || nName.indexOf(z) != -1) {
                        break;
                    }
                }
                if (MapleCharacterUtil.canChangePetName(nName)) {
                    c.getCharacter().getAndroid().setName(nName);
                    c.getCharacter().setAndroid(c.getCharacter().getAndroid()); //respawn it
                    used = true;
                }
                break;
            }
            case 5230001:
            case 5230000: {// owl of minerva
                final int itemSearch = lea.readInt();
                final List<HiredMerchant> hms = c.getChannelServer().searchMerchant(itemSearch);
                if (hms.size() > 0) {
                    c.getSession().write(CWvsContext.getOwlSearched(itemSearch, hms));
                    used = true;
                } else {
                    c.getCharacter().dropMessage(1, "Unable to find the item.");
                }
                break;
            }
            case 5281001: //idk, but probably
            case 5280001: // Gas Skill
            case 5281000: { // Passed gas
                Rectangle bounds = new Rectangle((int) c.getCharacter().getPosition().getX(), (int) c.getCharacter().getPosition().getY(), 1, 1);
                MapleMist mist = new MapleMist(bounds, c.getCharacter());
                c.getCharacter().getMap().spawnMist(mist, 10000, true);
                c.getSession().write(CWvsContext.enableActions());
                used = true;
                break;
            }
            case 5370001:
            case 5370000: { // Chalkboard
                for (MapleEventType t : MapleEventType.values()) {
                    final MapleEvent e = ChannelServer.getInstance(c.getChannel()).getEvent(t);
                    if (e.isRunning()) {
                        for (int i : e.getType().mapids) {
                            if (c.getCharacter().getMapId() == i) {
                                c.getCharacter().dropMessage(5, "You may not use that here.");
                                c.getSession().write(CWvsContext.enableActions());
                                return;
                            }
                        }
                    }
                }
                c.getCharacter().setChalkboard(lea.readMapleAsciiString());
                break;
            }
            case 5390000: // Diablo Messenger
            case 5390001: // Cloud 9 Messenger
            case 5390002: // Loveholic Messenger
            case 5390003: // New Year Messenger 1
            case 5390004: // New Year Messenger 2
            case 5390005: // Cute Tiger Messenger
            case 5390006: // Tiger Roar's Messenger
            case 5390007:
            case 5390008:
            case 5390009: {
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getCharacter().getMapId() == GameConstants.JAIL) {
                    c.getCharacter().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getCharacter().getCheatTracker().canAvatarSmega()) {
                    c.getCharacter().dropMessage(5, "You may only use this every 5 minutes.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final List<String> lines = new LinkedList<>();
                    if (itemId == 5390009) { //friend finder megaphone
                        lines.add("I'm looking for ");
                        lines.add("friends! Send a ");
                        lines.add("Friend Request if ");
                        lines.add("you're intetested!");
                    } else {
                        for (int i = 0; i < 4; i++) {
                            final String text = lea.readMapleAsciiString();
                            if (text.length() > 55) {
                                continue;
                            }
                            lines.add(text);
                        }
                    }
                    final boolean ear = lea.readByte() != 0;
                    World.Broadcast.broadcastSmega(CWvsContext.getAvatarMega(c.getCharacter(), c.getChannel(), itemId, lines, ear));
                    used = true;
                } else {
                    c.getCharacter().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5452001:
            case 5450006:
            case 5450007:
            case 5450013:
            case 5450003:
            case 5450000: { // Mu Mu the Travelling Merchant
                for (int i : GameConstants.blockedMaps) {
                    if (c.getCharacter().getMapId() == i) {
                        c.getCharacter().dropMessage(5, "You may not use this here.");
                        c.getSession().write(CWvsContext.enableActions());
                        return;
                    }
                }
                if (c.getCharacter().getLevel() < 10) {
                    c.getCharacter().dropMessage(5, "You must be over level 10 to use this.");
                } else if (c.getCharacter().hasBlockedInventory() || c.getCharacter().getMap().getSquadByMap() != null || c.getCharacter().getEventInstance() != null || c.getCharacter().getMap().getEMByMap() != null || c.getCharacter().getMapId() >= 990000000) {
                    c.getCharacter().dropMessage(5, "You may not use this here.");
                } else if ((c.getCharacter().getMapId() >= 680000210 && c.getCharacter().getMapId() <= 680000502) || (c.getCharacter().getMapId() / 1000 == 980000 && c.getCharacter().getMapId() != 980000000) || (c.getCharacter().getMapId() / 100 == 1030008) || (c.getCharacter().getMapId() / 100 == 922010) || (c.getCharacter().getMapId() / 10 == 13003000)) {
                    c.getCharacter().dropMessage(5, "You may not use this here.");
                } else {
                    MapleShopFactory.getInstance().getShop(9090000).sendShop(c);
                }
                //used = true;
                break;
            }
            case 5300000:
            case 5300001:
            case 5300002: { // Cash morphs
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                ii.getItemEffect(itemId).applyTo(c.getCharacter());
                used = true;
                break;
            }
            case 5781000: { //pet color dye
                lea.readInt();
                lea.readInt();
                int color = lea.readInt();

                break;
            }
            default:
                if (itemId / 10000 == 524 || itemId / 10000 == 546) { //Pet food & snacks
                    used = UsePetFood(c, itemId);
                    break;
                    }  
                if (itemId / 10000 == 512) {
                    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    String msg = ii.getMsg(itemId);
                    final String ourMsg = lea.readMapleAsciiString();
                    if (!msg.contains("%s")) {
                        msg = ourMsg;
                    } else {
                        msg = msg.replaceFirst("%s", c.getCharacter().getName());
                        if (!msg.contains("%s")) {
                            msg = ii.getMsg(itemId).replaceFirst("%s", ourMsg);
                        } else {
                            try {
                                msg = msg.replaceFirst("%s", ourMsg);
                            } catch (Exception e) {
                                msg = ii.getMsg(itemId).replaceFirst("%s", ourMsg);
                            }
                        }
                    }
                    c.getCharacter().getMap().startMapEffect(msg, itemId);

                    final int buff = ii.getStateChangeItem(itemId);
                    if (buff != 0) {
                        for (MapleCharacter mChar : c.getCharacter().getMap().getCharactersThreadsafe()) {
                            ii.getItemEffect(buff).applyTo(mChar);
                        }
                    }
                    used = true;
                } else if (itemId / 10000 == 510) {
                    c.getCharacter().getMap().startJukebox(c.getCharacter().getName(), itemId);
                    used = true;
                } else if (itemId / 10000 == 520) {
                    final int mesars = MapleItemInformationProvider.getInstance().getMeso(itemId);
                    if (mesars > 0 && c.getCharacter().getMeso() < (Integer.MAX_VALUE - mesars)) {
                        used = true;
                        if (Math.random() > 0.1) {
                            final int gainmes = Randomizer.nextInt(mesars);
                            c.getCharacter().gainMeso(gainmes, false);
                            c.getSession().write(CSPacket.sendMesobagSuccess(gainmes));
                        } else {
                            c.getSession().write(CSPacket.sendMesobagFailed(false)); // not random
                        }
                    }
                } else if (itemId / 10000 == 562) {
                    if (InventoryHandler.UseSkillBook(slot, itemId, c, c.getCharacter())) {
                        c.getCharacter().gainSP(1);
                    } //this should handle removing
                } else if (itemId / 10000 == 553) {
                    InventoryHandler.UseRewardItem(slot, itemId, false, c, c.getCharacter());// this too
                } else if (itemId / 10000 != 519) {
                    System.out.println("Unhandled CS item : " + itemId);
                    System.out.println(lea.toString(true));
                }
                break;
        }

        if (used) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, slot, (short) 1, false, true);
        }
        c.getSession().write(CWvsContext.enableActions());
        if (cc) {
            if (!c.getCharacter().isAlive() || c.getCharacter().getEventInstance() != null || FieldLimitType.ChannelSwitch.check(c.getCharacter().getMap().getFieldLimit())) {
                c.getCharacter().dropMessage(1, "Auto relog failed.");
                return;
            }
            c.getCharacter().dropMessage(5, "Auto relogging. Please wait.");
            c.getCharacter().fakeRelog();
            if (c.getCharacter().getScrolledPosition() != 0) {
                c.getSession().write(CWvsContext.pamSongUI());
            }
        }
	}
	
	private static boolean checkPotentialLock(MapleCharacter chr, Equip eq, int line, int potential) {
        if (line == 0 || potential == 0) {
            return false;
        }
        if (line < 0 || line > 3) {
            System.out.println("[Hacking Attempt] " + MapleCharacterUtil.makeMapleReadable(chr.getName()) + " Tried to lock potential line which does not exists.");
            return false;
        }
        if (line == 1 && eq.getPotential1() != potential - line * 100000
         || line == 2 && eq.getPotential2() != potential - line * 100000
         || line == 3 && eq.getPotential3() != potential - line * 100000) {
            System.out.println("[Hacking Attempt] " + MapleCharacterUtil.makeMapleReadable(chr.getName()) + " Tried to lock potential which equip doesn't have.");
            return false;
        }
        return true;
    }
	
	private static boolean magnifyEquip(final MapleClient c, Item magnify, Item toReveal, byte eqSlot) {
        final boolean insight = c.getCharacter().getTrait(MapleTraitType.sense).getLevel() >= 30;
        final Equip eqq = (Equip) toReveal;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final int reqLevel = ii.getReqLevel(eqq.getItemId()) / 10;
        if (eqq.getState() == 1 && (insight || magnify.getItemId() == 2460003 || (magnify.getItemId() == 2460002 && reqLevel <= 12) || (magnify.getItemId() == 2460001 && reqLevel <= 7) || (magnify.getItemId() == 2460000 && reqLevel <= 3))) {
            final List<List<StructItemOption>> pots = new LinkedList<>(ii.getAllPotentialInfo().values());
            int lockedLine = 0;
            int locked = 0;
            if (Math.abs(eqq.getPotential1()) / 100000 > 0) {
                lockedLine = 1;
                locked = Math.abs(eqq.getPotential1());
            } else if (Math.abs(eqq.getPotential2()) / 100000 > 0) {
                lockedLine = 2;
                locked = Math.abs(eqq.getPotential2());
            } else if (Math.abs(eqq.getPotential3()) / 100000 > 0) {
                lockedLine = 3;
                locked = Math.abs(eqq.getPotential3());
            }
            int new_state = Math.abs(eqq.getPotential1());
            if (lockedLine == 1) {
                new_state = locked / 10000 < 1 ? 17 : 16 + locked / 10000;
            }
            if (new_state > 20 || new_state < 17) { // incase overflow
                new_state = 17;
            }
            int lines = 2; // default
            if (eqq.getPotential2() != 0) {
                lines++;
            }
            while (eqq.getState() != new_state) {
                //31001 = haste, 31002 = door, 31003 = se, 31004 = hb, 41005 = combat orders, 41006 = advanced blessing, 41007 = speed infusion
                for (int i = 0; i < lines; i++) { // minimum 2 lines, max 3
                    boolean rewarded = false;
                    while (!rewarded) {
                        StructItemOption pot = pots.get(Randomizer.nextInt(pots.size())).get(reqLevel);
                        if (pot != null && pot.reqLevel / 10 <= reqLevel && GameConstants.optionTypeFits(pot.optionType, eqq.getItemId()) && GameConstants.potentialIDFits(pot.opID, new_state, i)) { //optionType
                            //have to research optionType before making this truely official-like
                            if (InventoryHandler.isAllowedPotentialStat(eqq, pot.opID)) {
                                if (i == 0) {
                                    eqq.setPotential1(pot.opID);
                                } else if (i == 1) {
                                    eqq.setPotential2(pot.opID);
                                } else if (i == 2) {
                                    eqq.setPotential3(pot.opID);
                                }  else if (i == 3) {
                                    eqq.setPotential4(pot.opID);
                                }
                                rewarded = true;
                            }
                        }
                    }
                }
            }
            switch (lockedLine) {
                case 1:
                    eqq.setPotential1(Math.abs(locked - lockedLine * 100000));
                    break;
                case 2:
                    eqq.setPotential2(Math.abs(locked - lockedLine * 100000));
                    break;
                case 3:
                    eqq.setPotential3(Math.abs(locked - lockedLine * 100000));
                    break;
            }
            c.getCharacter().getTrait(MapleTraitType.insight).addExp((insight ? 10 : ((magnify.getItemId() + 2) - 2460000)) * 2, c.getCharacter());
            c.getCharacter().getMap().broadcastMessage(UserPacket.showMagnifyingEffect(c.getCharacter().getID(), eqq.getPosition()));
            if (!insight) {
                c.getSession().write(InventoryPacket.scrolledItem(magnify, eqSlot >= 0 ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED, toReveal, false, true, false));
            } else {
                c.getCharacter().forceReAddItem(toReveal, eqSlot >= 0 ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED);
            }
            return true;
        } else {
            return false;
        }
    }
	
	private static boolean getIncubatedItems(MapleClient c, int itemId) {
        if (c.getCharacter().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < 2 || c.getCharacter().getInventory(MapleInventoryType.USE).getNumFreeSlot() < 2 || c.getCharacter().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < 2) {
            c.getCharacter().dropMessage(5, "Please make room in your inventory.");
            return false;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int id1 = RandomRewards.getPeanutReward(), id2 = RandomRewards.getPeanutReward();
        while (!ii.itemExists(id1)) {
            id1 = RandomRewards.getPeanutReward();
        }
        while (!ii.itemExists(id2)) {
            id2 = RandomRewards.getPeanutReward();
        }
        c.getSession().write(CWvsContext.getPeanutResult(id1, (short) 1, id2, (short) 1, itemId));
        MapleInventoryManipulator.addById(c, id1, (short) 1, ii.getName(itemId) + " on " + FileoutputUtil.CurrentReadable_Date());
        MapleInventoryManipulator.addById(c, id2, (short) 1, ii.getName(itemId) + " on " + FileoutputUtil.CurrentReadable_Date());
        c.getSession().write(NPCPacket.getNPCTalk(1090000, (byte) 0, "You have obtained the following items:\r\n#i" + id1 + "##z" + id1 + "#\r\n#i" + id2 + "##z" + id2 + "#", "00 00", (byte) 0));
        return true;
    }
	
	private static void addMedalString(final MapleCharacter c, final StringBuilder sb) {
        final Item medal = c.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -49);
        if (medal != null) { // Medal
            sb.append("<");
                sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
            }
            sb.append("> ");
	}
	
	private static final boolean UsePetFood(MapleClient c, int itemId) {
        MaplePet pet = c.getCharacter().getPet(0);
        if (pet == null) {
            return false;
        }
        if (!pet.canConsume(itemId)) {
            pet = c.getCharacter().getPet(1);
            if (pet != null) {
                if (!pet.canConsume(itemId)) {
                    pet = c.getCharacter().getPet(2);
                    if (pet != null) {
                        if (!pet.canConsume(itemId)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        final byte petindex = c.getCharacter().getPetIndex(pet);
        pet.setFullness(100);
        if (pet.getCloseness() < 30000) {
            if (pet.getCloseness() + (100 * c.getChannelServer().getTraitRate()) > 30000) {
                pet.setCloseness(30000);
            } else {
                pet.setCloseness(pet.getCloseness() + (100 * c.getChannelServer().getTraitRate()));
            }
            if (pet.getCloseness() >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                pet.setLevel(pet.getLevel() + 1);
                c.getSession().write(EffectPacket.showOwnPetLevelUp(c.getCharacter().getPetIndex(pet)));
                c.getCharacter().getMap().broadcastMessage(PetPacket.showPetLevelUp(c.getCharacter(), petindex));
            }
        }
        c.getSession().write(PetPacket.updatePet(pet, c.getCharacter().getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), true));
        c.getCharacter().getMap().broadcastMessage(c.getCharacter(), PetPacket.commandResponse(c.getCharacter().getID(), (byte) 1, petindex, true, true), true);
        return true;
    }

}
