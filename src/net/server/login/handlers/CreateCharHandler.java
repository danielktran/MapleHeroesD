package net.server.login.handlers;

import java.util.HashMap;
import java.util.Map;

import client.MapleCharacterUtil;
import client.MapleClient;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.character.MapleCharacter;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.JobConstants;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.LoginPacket;
import net.server.login.LoginInformationProvider;
import net.server.login.LoginInformationProvider.JobType;
import server.MapleItemInformationProvider;
import tools.data.LittleEndianAccessor;

public class CreateCharHandler extends AbstractMaplePacketHandler {

	public CreateCharHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        final String name = lea.readMapleAsciiString();
        int hairColor = -1, hat = -1, bottom = -1, cape = -1, faceMark = -1, ears = -1, tail = -1, shield = -1;
        System.out.println("Creating Character Name: " + name);
        if (!MapleCharacterUtil.canCreateChar(name, false)) {
            System.out.println("[Alert] Character Name Hack: " + name);
            return;
        }
        final int keySettings = lea.readInt(); // Key Settings
        lea.readInt(); //-1
        final int jobType = lea.readInt();
        final JobType job = JobType.getByType(jobType);
        if (job == null) {
            System.out.println("[Notice] New Job Type Found: " + jobType);
            return;
        }
        
        for (JobConstants.LoginJob j : JobConstants.LoginJob.values()) {
            if (j.getJobType() == jobType) {
                if (j.getFlag() != JobConstants.LoginJob.JobFlag.ENABLED.getFlag()) {
                    System.out.println("[Alert] Attempted to create a disabled job.");
                    return;
                }
            }
        }
        
        final short subcategory = lea.readShort();
        final byte gender = lea.readByte();
        final byte skin = lea.readByte();
        /*
         * unk possible variables:
         * 5: Mercedes
         * 6: Adventurer, DualBlade, Cannoneer, Jett, Kinesis
         * 7: Cygnus, DemonSlayer, 
         * 8: Mihile, Shade, Hayato, Kanna
         */
        final byte unk = lea.readByte();
        final int face = lea.readInt();
        int hair = lea.readInt();
        
        if (job.hairColor) {
            hairColor = lea.readInt();
        }
        if (job.skinColor) {
            lea.readInt();
        }
        if (job.faceMark) {
            faceMark = lea.readInt();
        }
        if (job.ears) {
            ears = lea.readInt();
        }
        if (job.tail) {
            tail = lea.readInt();
        }
        if (job.hat) {
            hat = lea.readInt();
        }
        final int top = lea.readInt();
        if (job.bottom) {
            bottom = lea.readInt();
        }
        if (job.cape) {
            cape = lea.readInt();
        }
        
        final int shoes = lea.readInt();
        final int weapon = lea.readInt();
        
        if (lea.available() >= 4) {
            shield = lea.readInt();
        }
        
        int index = 0;
        boolean noSkin = job == JobType.Demon || job == JobType.Mercedes || job == JobType.Jett;
        int[] items = new int[]{face, hair, hairColor, noSkin ? -1 : skin, faceMark, ears, tail, hat, top, bottom, cape, shoes, weapon, shield};
        if (job != JobType.BeastTamer) {
            for (int i : items) {
                if (i > -1) {
                    if (!LoginInformationProvider.getInstance().isEligibleItem(gender, index, job.type, i)) {
                        System.out.println(gender + " | " + index + " | " + job.type + " | " + i);
                        return;
                    }
                    index++;
                }
            }
        }
        MapleCharacter newchar = MapleCharacter.getDefault(c, job);
        newchar.setWorld((byte) c.getWorld());
        newchar.setFace(face);
        newchar.setSecondFace(21290);
        if (hairColor < 0) {
            hairColor = 0;
        }
        if (job != JobType.Mihile) {
            hair += hairColor;
        }
        newchar.setHair(hair);
        newchar.setSecondHair(37623);
        if (job == JobType.AngelicBuster) {
            newchar.setSecondFace(21173);
            newchar.setSecondHair(37141);
            newchar.setJob((short) 6500);
            newchar.setLevel((short) 10);
            newchar.getStat().dex = 68;
            newchar.getStat().maxhp = 1000;
            newchar.getStat().hp = 1000;
            newchar.setRemainingSp(3);
        } else if (job == JobType.Zero) {
            newchar.setSecondFace(21290);
            newchar.setSecondHair(37623);
        }
        newchar.setGender(gender);
        newchar.setName(name);
        newchar.setSkinColor(skin);
        if (faceMark < 0) {
            faceMark = 0;
        }
        newchar.setFaceMarking(faceMark);
        int[] wrongEars = {1004062, 1004063, 1004064};
        int[] correctEars = {5010116, 5010117, 5010118};
        int[] wrongTails = {1102661, 1102662, 1102663};
        int[] correctTails = {5010119, 5010120, 5010121};
        for (int i = 0; i < wrongEars.length; i++) {
            if (ears == wrongEars[i]) {
                ears = correctEars[i];
            }
        }
        for (int i = 0; i < wrongTails.length; i++) {
            if (tail == wrongTails[i]) {
                tail = correctTails[i];
            }
        }
        if (ears < 0) {
            ears = 0;
        }
        newchar.setEars(ears);
        if (tail < 0) {
            tail = 0;
        }
        newchar.setTail(tail);
        final MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();
        final MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
        Item item;
        //-1 Hat | -2 Face | -3 Eye acc | -4 Ear acc | -5 Topwear 
        //-6 Bottom | -7 Shoes | -9 Cape | -10 Shield | -11 Weapon
        //todo check zero's beta weapon slot
        int[][] equips = new int[][]{{hat, -1}, {top, -5}, {bottom, -6}, {cape, -9}, {shoes, -7}, {weapon, -11}, {shield, -10}};
        for (int[] i : equips) {
            if (i[0] > 0) {
                item = li.getEquipById(i[0]);
                item.setPosition((byte) i[1]);
                item.setGMLog("Character Creation");
                equip.addFromDB(item);
            }
        }
        if (job == JobType.AngelicBuster || job == JobType.Kaiser) {
            item = li.getEquipById(job == JobType.Kaiser ? 1352500 : 1352601);
            item.setPosition((byte) -10);
            item.setGMLog("Nova Shield");
            equip.addFromDB(item);
        }
        // Additional skills for all first job classes. Some skills are not added by default,
        // so adding the skill ID here between the {}, will give the skills you entered to the desired job.
        int[][] skills = new int[][]{
            {80001152, 20021061},// Resistance
            {80001152, 1281},// Explorer
            {10001244, 10000252, 80001152},// Cygnus
            {20000194},// Aran
            {20010022, 20010194},// Evan
            {20020109, 20021110, 20020111, 20020112}, // Mercedes
            {30010112, 30010110, 30010111, 30010185},//Demon
            {20031251, 20030204, 20030206, 20031208, 20031207, 20031203},// Phantom
            {80001152, 1281},// Dualblade
            {50001214},// Mihile
            {20040216, 20040217, 20040218, 20040219, 20040220, 20040221, 20041222, 27001100, 27000207, 27001201},// Luminous
            {},// Kaiser
            {60011216, 60010217, 60011218, 60011219, 60011220, 60011221, 60011222},// AngelicBuster
            {},//Cannoneer
            {30020232, 30020233, 30020234, 30020240, 30021238},// Xenon
            {100000279, 100000282, 100001262, 100001263, 100001264, 100001265, 100001266, 100001268},// Zero
            {}, // Shade
            {228, 80001151},// Jett
            {},// Hayato
            {40020000, 40020001, 40020002, 40021023, 40020109},// Kanna
            {80001152, 110001251},// BeastTamer
            {}, // Pink Bean
            {} // Kinesis
        };
        if (skills[job.type].length > 0) {
            final Map<Skill, SkillEntry> ss = new HashMap<>();
            Skill s;
            for (int i : skills[job.type]) {
            	try {
	                s = SkillFactory.getSkill(i);
	                int maxLevel = s.getMaxLevel();
	                if (maxLevel < 1) {
	                    maxLevel = s.getMasterLevel();
	                }
	                ss.put(s, new SkillEntry((byte) 1, (byte) maxLevel, -1));
            	} catch(Exception ex) {
            		System.out.println("Error?");
            	}
            }
            if (job == JobType.Zero) {
                ss.put(SkillFactory.getSkill(101000103), new SkillEntry((byte) 8, (byte) 10, -1));
                ss.put(SkillFactory.getSkill(101000203), new SkillEntry((byte) 8, (byte) 10, -1));
            }
            if (job == JobType.BeastTamer) {
                ss.put(SkillFactory.getSkill(110001511), new SkillEntry((byte) 0, (byte) 30, -1));
                ss.put(SkillFactory.getSkill(110001512), new SkillEntry((byte) 0, (byte) 5, -1));
                ss.put(SkillFactory.getSkill(110000513), new SkillEntry((byte) 0, (byte) 30, -1));
                ss.put(SkillFactory.getSkill(110000515), new SkillEntry((byte) 0, (byte) 10, -1));
                ss.put(SkillFactory.getSkill(110001501), new SkillEntry((byte) 1, (byte) 1, -1));
                ss.put(SkillFactory.getSkill(110001502), new SkillEntry((byte) 1, (byte) 1, -1));
                ss.put(SkillFactory.getSkill(110001503), new SkillEntry((byte) 1, (byte) 1, -1));
                ss.put(SkillFactory.getSkill(110001504), new SkillEntry((byte) 1, (byte) 1, -1));
            }
            if (job == JobType.Resistance) { // hacky fix for mech.
                ss.put(SkillFactory.getSkill(35120000), new SkillEntry((byte) 1, (byte) 10, -1));
            }
            newchar.changeSkillLevel_Skip(ss, false);
        }
        int[][] guidebooks = new int[][]{{4161001, 0}, {4161047, 1}, {4161048, 2000}, {4161052, 2001}, {4161054, 3}, {4161079, 2002}};
        int guidebook = 0;
        for (int[] i : guidebooks) {
            if (newchar.getJob() == i[1]) {
                guidebook = i[0];
            } else if (newchar.getJob() / 1000 == i[1]) {
                guidebook = i[0];
            }
        }
        if (guidebook > 0) {
            newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(guidebook, (byte) 0, (short) 1, (byte) 0));
        }
        if (job == JobType.Zero) {
            newchar.setLevel((short) 100);
            newchar.getStat().str = 518;
            newchar.getStat().maxhp = 6910;
            newchar.getStat().hp = 6910;
            newchar.getStat().maxmp = 100;
            newchar.getStat().mp = 100;
            newchar.setRemainingSp(3, 0); //alpha
            newchar.setRemainingSp(3, 1); //beta
        }
        if (job == JobType.BeastTamer) {
            newchar.setJob((short) 11212);
            newchar.setLevel((short) 10);
            newchar.getStat().maxhp = 567;
            newchar.getStat().hp = 551;
            newchar.getStat().maxmp = 270;
            newchar.getStat().mp = 263;
            newchar.setRemainingAp(45);
            newchar.setRemainingSp(3, 0);
        }
        if (job == JobType.Luminous) {
            newchar.setJob((short) 2700);
            newchar.setLevel((short) 10);
            newchar.getStat().str = 4;
            newchar.getStat().int_ = 57;
            newchar.getStat().maxhp = 500;
            newchar.getStat().hp = 500;
            newchar.getStat().maxmp = 1000;
            newchar.getStat().mp = 1000;
            newchar.setRemainingSp(3);
        }
        int[] StarterItems = {1102041, 1102042, 1082146};
        if (job == JobType.Luminous) {
            StarterItems = new int[]{1212001, 1352400, 1102041, 1102042, 1082146};
        }
        if (job == JobType.Adventurer
                || job == JobType.UltimateAdventurer
                || job == JobType.Resistance
                || job == JobType.Aran
                || job == JobType.Cygnus
                || job == JobType.Demon
                || job == JobType.Evan
                || job == JobType.Jett
                || job == JobType.Mihile) {
            StarterItems = new int[]{};
        }
        if (job == JobType.AngelicBuster) {
            StarterItems = new int[]{1222062, 1352601, 1102041, 1102042, 1082146};
        }
        if (job == JobType.Cannoneer) {
            StarterItems = new int[]{1532000, 1102041, 1102042, 1082146};
        }
        if (job == JobType.DualBlade) {
            StarterItems = new int[]{1332081, 1342047, 1102041, 1102042, 1082146};
        }
        if (job == JobType.Mercedes) {
            StarterItems = new int[]{1352000, 1522000, 1102041, 1102042, 1082146};
        }
        if (job == JobType.Phantom) {
            StarterItems = new int[]{1362000, 1352100, 1102041, 1102042, 1082146};
        }
        if (job == JobType.Xenon) {
            StarterItems = new int[]{1242001, 1102041, 1102042, 1082146};
        }
        for (int i = 0; i < StarterItems.length; i++) {
            item = li.getEquipById(StarterItems[i]);
            item.setPosition((byte) (i + 1));//Ain't no slot 0, only a slot 1
            newchar.getInventory(MapleInventoryType.EQUIP).addFromDB(item);
        }
        if (MapleCharacterUtil.canCreateChar(name, c.isGm()) && (!LoginInformationProvider.getInstance().isForbiddenName(name) || c.isGm()) && (c.isGm() || c.canMakeCharacter(c.getWorld()))) {
            MapleCharacter.saveNewCharToDB(newchar, job, subcategory);
            c.getSession().write(LoginPacket.addNewCharEntry(newchar, true));
            c.createdChar(newchar.getID());
            newchar.newCharRewards();
        } else {
            c.getSession().write(LoginPacket.addNewCharEntry(newchar, false));
        }
    }
}
