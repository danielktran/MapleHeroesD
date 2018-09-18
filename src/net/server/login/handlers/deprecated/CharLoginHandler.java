package net.server.login.handlers.deprecated;

import client.LoginCrypto;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.PartTimeJob;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.character.MapleCharacter;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.JobConstants;
import constants.ServerConfig;
import constants.ServerConstants;
import constants.WorldConstants;
import constants.WorldConstants.WorldOption;
import net.packet.CField;
import net.packet.CWvsContext;
import net.packet.LoginPacket;
import net.packet.PacketHelper;
import net.server.channel.ChannelServer;
import net.server.login.LoginInformationProvider;
import net.server.login.LoginServer;
import net.server.login.LoginWorker;
import net.server.login.LoginInformationProvider.JobType;
import net.world.World;
import constants.WorldConstants.TespiaWorldOption;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import server.MapleItemInformationProvider;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.Triple;
import tools.data.LittleEndianAccessor;

public class CharLoginHandler {

    private static boolean loginFailCount(final MapleClient c) {
        c.loginAttempt++;
        return c.loginAttempt > 3;
    }

    public static void redirectorLogin(final LittleEndianAccessor slea, final MapleClient c) {
        String username = slea.readMapleAsciiString();
        String password = slea.readMapleAsciiString(); //this LOL
        int status = c.login(username, password, false);
        if (status == 0) {
            c.loginAttempt = 0;
            LoginWorker.registerClient(c);
        }
    }
    
    public static void CreateChar(final LittleEndianAccessor slea, final MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        final String name = slea.readMapleAsciiString();
        slea.readInt(); // key settings
        slea.readInt(); // -1 ?
        final JobType jobType = JobType.getByType(slea.readInt()); // BIGBANG: 0 = Resistance, 1 = Adventurer, 2 = Cygnus, 3 = Aran, 4 = Evan, 5 = mercedes
        final short subcategory = slea.readShort(); //whether dual blade = 1 or adventurer = 0
        final byte gender = slea.readByte(); //??idk corresponds with the thing in addCharStats
        byte skinColor = slea.readByte(); // 01
        int hairColor = 0;
        final byte unk = slea.readByte(); // 08
        final boolean mercedes = (jobType == JobType.Mercedes);
        final boolean demon = (jobType == JobType.Demon);
        final boolean mihile = (jobType == JobType.Mihile);
        final boolean aran = (jobType == JobType.Aran);
        final boolean evan = (jobType == JobType.Evan);
        final boolean resist = (jobType == JobType.Resistance);
        final boolean cygnus = (jobType == JobType.Cygnus);
        final boolean adv = (jobType == JobType.Adventurer);
        boolean jettPhantom = (jobType == LoginInformationProvider.JobType.Jett) || (jobType == LoginInformationProvider.JobType.Phantom) || (jobType == LoginInformationProvider.JobType.DualBlade);
        final int face = slea.readInt();
        final int hair = slea.readInt();
        if (!mercedes && !mihile && !demon && !jettPhantom) { //mercedes/demon dont need hair color since its already in the hair
            if (!adv) {
                hairColor = slea.readInt();
            }
            skinColor = (byte) slea.readInt();
        }
        if(mihile){ //For some reason an int was missing.
            final int unk3 = slea.readInt();
        }
        final int faceMark = (aran ? 0 : evan ? 0 : resist ? 0 : cygnus ? 0 : adv ? 0 : slea.readInt());
        final int top = slea.readInt();
        final int bottom = adv ? 0 : slea.readInt();
        final int shoes = slea.readInt();
        final int weapon = jobType == LoginInformationProvider.JobType.Phantom ? 1362046 : mercedes ? 1522038 : evan ? 1372005 : slea.readInt();
        int shield = jobType == LoginInformationProvider.JobType.Phantom ? 1352100 : jobType == LoginInformationProvider.JobType.Mihile ? 1098000 : mercedes ? 1352000 : demon ? slea.readInt() : 0;

       // System.out.println("Name:" + name + " jobType: " +jobType.toString() + " db: " + db + " gender: " + gender + " skinColor: " + skinColor + " unk2: " + unk2 + "face: " + face + " hair: " + hair
        //        + " demon: " + demonMark + " Top: "+ top + " bottom: " + bottom + " shoes: " + shoes + " wep: " + weapon + " shield: " + shield);
        
        MapleCharacter newchar = MapleCharacter.getDefault(c, jobType);
        newchar.setWorld((byte) c.getWorld());
        newchar.setFace(face);
        newchar.setHair(hair + hairColor);
        newchar.setGender(gender);
        newchar.setName(name);
        newchar.setSkinColor(skinColor);
        newchar.setFaceMarking(faceMark);
        newchar.setHcMode((short)c.getWorld());

        final MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();
        final MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);

        Item item = li.getEquipById(top);
        item.setPosition((byte) -5);
        equip.addFromDB(item);

        if (bottom > 0) { //resistance have overall
            item = li.getEquipById(bottom);
            item.setPosition((short)(byte)(jettPhantom ? -9 : -6));
            equip.addFromDB(item);
        }
        
        item = li.getEquipById(shoes);
        item.setPosition((byte) -7);
        equip.addFromDB(item);

        item = li.getEquipById(weapon);
        item.setPosition((byte) -11);
        equip.addFromDB(item);

        if (shield > 0) {
            item = li.getEquipById(shield);
            item.setPosition((byte) -10);
            equip.addFromDB(item);
        }

        //blue/red pots
        switch (jobType) {
            case Resistance: // Resistance
                //newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (byte) 0, (short) 1, (byte) 0));
                final Map<Skill, SkillEntry> ss = new HashMap<>();
                //ss.put(SkillFactory.getSkill(80001152), new SkillEntry((byte) 1, (byte) 1, -1));
                newchar.changeSkillLevel_Skip(ss, false);
                break;
            case Adventurer: // Adventurer
                //newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (byte) 0, (short) 1, (byte) 0));
                final Map<Skill, SkillEntry> ss1 = new HashMap<>();
                //ss1.put(SkillFactory.getSkill(80001152), new SkillEntry((byte) 1, (byte) 1, -1));
                newchar.changeSkillLevel_Skip(ss1, false);
                break;
            case Cygnus: // Cygnus
                //newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161047, (byte) 0, (short) 1, (byte) 0));
                newchar.setQuestAdd(MapleQuest.getInstance(20022), (byte) 1, "1");
                newchar.setQuestAdd(MapleQuest.getInstance(20010), (byte) 1, null); //>_>_>_> ugh
                final Map<Skill, SkillEntry> ss2 = new HashMap<>();
                //ss2.put(SkillFactory.getSkill(80001152), new SkillEntry((byte) 1, (byte) 1, -1));
                newchar.changeSkillLevel_Skip(ss2, false);
                break;
            case Aran: // Aran
                newchar.setJob(2100);
                final Map<Skill, SkillEntry> ss3 = new HashMap<>();
                //ss3.put(SkillFactory.getSkill(80001152), new SkillEntry((byte) 1, (byte) 1, -1));
                newchar.changeSkillLevel_Skip(ss3, false);
                break;
            case Evan: //Evan
                newchar.setLevel((short) 10);
                newchar.setJob(2200);
                newchar.getStat().maxhp += 140; //Beginner 10 levels
                newchar.getStat().maxmp += 210;
                newchar.getStat().hp += 140; //Beginner 10 levels
                newchar.getStat().mp += 210;
                newchar.getStat().str = 4;
                newchar.getStat().dex = 4;
                newchar.getStat().int_ = 4; //Why is int_ and luk switched?
                newchar.getStat().luk = 4;
                newchar.setRemainingAp((short) 54); 
                newchar.setRemainingSp(5); 
                final Map<Skill, SkillEntry> ss4= new HashMap<>();
                //ss4.put(SkillFactory.getSkill(80001152), new SkillEntry((byte) 1, (byte) 1, -1));
                newchar.changeSkillLevel_Skip(ss4, false);
                break;
            case Mercedes: // Mercedes
                //newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161079, (byte) 0, (short) 1, (byte) 0));
				final Map<Skill, SkillEntry> ss5 = new HashMap<>();
				ss5.put(SkillFactory.getSkill(20021000), new SkillEntry((byte) 0, (byte) 0, -1));
				ss5.put(SkillFactory.getSkill(20021001), new SkillEntry((byte) 0, (byte) 0, -1));
				ss5.put(SkillFactory.getSkill(20020002), new SkillEntry((byte) 0, (byte) 0, -1));				
				//ss5.put(SkillFactory.getSkill(20020022), new SkillEntry((byte) 1, (byte) 1, -1)); //wrong skill, fag
				ss5.put(SkillFactory.getSkill(20020109), new SkillEntry((byte) 1, (byte) 1, -1));
				ss5.put(SkillFactory.getSkill(20021110), new SkillEntry((byte) 1, (byte) 1, -1));
				ss5.put(SkillFactory.getSkill(20020111), new SkillEntry((byte) 1, (byte) 1, -1));
				ss5.put(SkillFactory.getSkill(20020112), new SkillEntry((byte) 1, (byte) 1, -1));
				ss5.put(SkillFactory.getSkill(20021181), new SkillEntry((byte) -1, (byte) 0, -1));
                ss5.put(SkillFactory.getSkill(20021166), new SkillEntry((byte) -1, (byte) 0, -1));
                //ss5.put(SkillFactory.getSkill(80001152), new SkillEntry((byte) 1, (byte) 1, -1));
            	newchar.changeSkillLevel_Skip(ss5, false);
                break;
            case Demon: //Demon
                //newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161054, (byte) 0, (short) 1, (byte) 0));
                final Map<Skill, SkillEntry> ss6 = new HashMap<>();
                //ss6.put(SkillFactory.getSkill(30011000), new SkillEntry((byte) 0, (byte) 0, -1));
                //ss6.put(SkillFactory.getSkill(30011001), new SkillEntry((byte) 0, (byte) 0, -1));
                //ss6.put(SkillFactory.getSkill(30010002), new SkillEntry((byte) 0, (byte) 0, -1));				
                ss6.put(SkillFactory.getSkill(30010185), new SkillEntry((byte) 1, (byte) 1, -1));
                ss6.put(SkillFactory.getSkill(30010112), new SkillEntry((byte) 1, (byte) 1, -1));
                ss6.put(SkillFactory.getSkill(30010111), new SkillEntry((byte) 1, (byte) 1, -1));
                ss6.put(SkillFactory.getSkill(30010110), new SkillEntry((byte) 1, (byte) 1, -1));
                //ss6.put(SkillFactory.getSkill(30010022), new SkillEntry((byte) 1, (byte) 1, -1));
                ss6.put(SkillFactory.getSkill(30011109), new SkillEntry((byte) 1, (byte) 1, -1));
                ss6.put(SkillFactory.getSkill(30011170), new SkillEntry((byte) 1, (byte) 1, -1));
                ss6.put(SkillFactory.getSkill(30011169), new SkillEntry((byte) 1, (byte) 1, -1));
                ss6.put(SkillFactory.getSkill(30011168), new SkillEntry((byte) 1, (byte) 1, -1));
                ss6.put(SkillFactory.getSkill(30011167), new SkillEntry((byte) 1, (byte) 1, -1));
                ss6.put(SkillFactory.getSkill(30010166), new SkillEntry((byte) 1, (byte) 1, -1));
                //ss6.put(SkillFactory.getSkill(80001152), new SkillEntry((byte) 1, (byte) 1, -1));
                newchar.changeSkillLevel_Skip(ss6, false);
                break;

            case Phantom:
                newchar.setLevel((short) 10);
                newchar.setJob(2400);
                newchar.getStat().maxhp += 294; //Beginner 10 levels
                newchar.getStat().maxmp += 113;
                newchar.getStat().hp += 294; //Beginner 10 levels
                newchar.getStat().mp += 113;
                newchar.getStat().str = 4;
                newchar.getStat().dex = 4;
                newchar.getStat().int_ = 30; //Why is int_ and luk switched?
                newchar.getStat().luk = 4;
                newchar.setRemainingAp((short) 28); 
                newchar.setRemainingSp(5); 
                final Map<Skill, SkillEntry> ss7 = new HashMap<>();
                ss7.put(SkillFactory.getSkill(20031203), new SkillEntry((byte) 1, (byte) 1, -1));
                ss7.put(SkillFactory.getSkill(20030204), new SkillEntry((byte) 1, (byte) 1, -1));
                ss7.put(SkillFactory.getSkill(20031205), new SkillEntry((byte) 1, (byte) 1, -1));
                ss7.put(SkillFactory.getSkill(20030206), new SkillEntry((byte) 1, (byte) 1, -1));
                ss7.put(SkillFactory.getSkill(20031207), new SkillEntry((byte) 1, (byte) 1, -1));
                ss7.put(SkillFactory.getSkill(20031208), new SkillEntry((byte) 1, (byte) 1, -1));
                ss7.put(SkillFactory.getSkill(20031209), new SkillEntry((byte) 1, (byte) 1, -1)); //Judgement Draw I
                //ss7.put(SkillFactory.getSkill(20031210), new SkillEntry((byte) 1, (byte) 1, -1)); //Judgement Draw II is at 4th Job.
                //ss7.put(SkillFactory.getSkill(80001152), new SkillEntry((byte) 1, (byte) 1, -1));
                newchar.changeSkillLevel_Skip(ss7, false);
                break;	
                
            case Jett:
                newchar.setLevel((short) 10);
                newchar.setJob(508);
                newchar.getStat().maxhp += 294; //Beginner 10
                newchar.getStat().maxmp += 113;
                newchar.getStat().hp += 294; //Beginner 10
                newchar.getStat().mp += 113;
                newchar.getStat().str -= 8;
                newchar.getStat().dex += 15;
                newchar.setRemainingAp((short) 38); 
                newchar.setRemainingSp(1); 
                final Map<Skill, SkillEntry> ss8 = new HashMap<>();
                ss8.put(SkillFactory.getSkill(228), new SkillEntry((byte) 1, (byte) 1, -1));
                //ss8.put(SkillFactory.getSkill(80001152), new SkillEntry((byte) 1, (byte) 1, -1));
                //ss8.put(SkillFactory.getSkill(80001151), new SkillEntry((byte) 1, (byte) 1, -1));
                newchar.changeSkillLevel_Skip(ss8, false);
                break;	
        
            case Mihile:
            	newchar.setLevel((short) 10);
            	newchar.setJob(5100);
            	newchar.getStat().maxhp += 382; //Starting ten levels
            	newchar.getStat().maxmp += 87;
            	newchar.getStat().hp += 382; 
            	newchar.getStat().mp += 87;
            	newchar.getStat().str = 4;
            	newchar.getStat().dex = 4;
            	newchar.getStat().int_ = 4;
            	newchar.getStat().luk = 4;
            	newchar.setRemainingAp((short) 54); 
            	newchar.setRemainingSp(5);
            	final Map<Skill, SkillEntry> ss9 = new HashMap<>();
            	ss9.put(SkillFactory.getSkill(50001214), new SkillEntry((byte) 1, (byte) 1, -1));
            	newchar.changeSkillLevel_Skip(ss9, false);
            	break;	
            }

        if (MapleCharacterUtil.canCreateChar(name, c.isGm()) && (!LoginInformationProvider.getInstance().isForbiddenName(name) || c.isGm()) && (c.isGm() || c.canMakeCharacter(c.getWorld()))) {
            MapleCharacter.saveNewCharToDB(newchar, jobType, subcategory);
            c.getSession().write(LoginPacket.addNewCharEntry(newchar, true));
            c.createdChar(newchar.getID());
        } else {
            c.getSession().write(LoginPacket.addNewCharEntry(newchar, false));
        }
    }

    public static void Character_login_noPIC(final LittleEndianAccessor slea, final MapleClient c, final boolean view, final boolean haspic) {
        if (haspic) {
            final String password = slea.readMapleAsciiString();
            final int charId = slea.readInt();
            if (view) {
                c.setChannel(1);
                c.setWorld(slea.readInt());
            }
            c.updateMacs(slea.readMapleAsciiString());
            final String s = c.getSessionIPAddress();
            LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP(), c.getChannel());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
            c.getSession().write(CField.getServerIP(Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), c.getWorld(), charId));
        } else {
            slea.readByte(); // 1?
            slea.readByte(); // 1?
            final int charId = slea.readInt();
            if (view) {
                c.setChannel(1);
                c.setWorld(slea.readInt());
            }
            c.updateMacs(slea.readMapleAsciiString());
            slea.readMapleAsciiString();
            final String s = c.getSessionIPAddress();
            LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP(), c.getChannel());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
            c.getSession().write(CField.getServerIP(Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), c.getWorld(), charId));

        }
    }

    public static void Character_WithoutSecondPassword(final LittleEndianAccessor slea, final MapleClient c, final boolean haspic, final boolean view) {
        if (constants.ServerConfig.DISABLE_PIC) {
            Character_login_noPIC(slea, c, view, haspic);
        }
        System.out.println("Does it atleast get here?");
        slea.readByte(); // 1?
        slea.readByte(); // 1?
        final int charId = slea.readInt();
        if (view) {
            c.setChannel(1);
            c.setWorld(slea.readInt());
        }
        final String currentpw = c.getSecondPassword();
        
        if (!c.isLoggedIn() || loginFailCount(c) || (currentpw != null && (!currentpw.equals("") || haspic)) || !c.login_Auth(charId) || ChannelServer.getInstance(c.getChannel()) == null || !WorldOption.isExists(c.getWorld())) {
            c.getSession().close();
            return;
        }
        
        c.updateMacs(slea.readMapleAsciiString());
        slea.readMapleAsciiString();
        System.out.println("Does it atleast get here? or here");
        if (slea.available() != 0) {
            final String setpassword = slea.readMapleAsciiString();

            if (setpassword.length() >= 6 && setpassword.length() <= 16) {
                c.setSecondPassword(setpassword);
                c.updateSecondPassword();
            } else {
                c.getSession().write(LoginPacket.secondPwError((byte) 0x14));
                return;
            }
        } else if (haspic) {
            return;
        }
        System.out.println("Does it atleast get here? or here? or here??");
        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }
        System.out.println("Does it atleast get here? or here? or here???");
        final String s = c.getSessionIPAddress();
        System.out.println("Made it to here: 1");
        LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP(), c.getChannel());
        System.out.println("Made it to here: 2");
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
        System.out.println("Made it to here: 3");
        c.getSession().write(CField.getServerIP(Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), c.getWorld(), charId));
    }

    public static void Character_WithSecondPassword(final LittleEndianAccessor slea, final MapleClient c, final boolean view) {
        if (constants.ServerConfig.DISABLE_PIC) {
            Character_login_noPIC(slea, c, view, true);
        }
        final String password = slea.readMapleAsciiString();
        final int charId = slea.readInt();
        if (view) {
            c.setChannel(1);
            c.setWorld(slea.readByte());
        }
        if (!c.isLoggedIn() || loginFailCount(c) || c.getSecondPassword() == null || !c.login_Auth(charId) || ChannelServer.getInstance(c.getChannel()) == null || !WorldOption.isExists(c.getWorld())) {
            c.getSession().close();
            return;
        }
        c.updateMacs(slea.readMapleAsciiString());
        if (c.CheckSecondPassword(password) && password.length() >= 6 && password.length() <= 16 || c.isGm() || c.isLocalhost()) {
            FileoutputUtil.logToFile("Secondary Passwords", "\r\nID: " + c.getAccountName() + " PIC: " + password);
            if (c.getIdleTask() != null) {
                c.getIdleTask().cancel(true);
            }

            final String s = c.getSessionIPAddress();
            LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP(), c.getChannel());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
            c.getSession().write(CField.getServerIP(Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), c.getWorld(), charId));
        } else {
            c.getSession().write(LoginPacket.secondPwError((byte) 0x14));
        }
    }

    public static void partTimeJob(final LittleEndianAccessor slea, final MapleClient c) {
        System.out.println("[Part Time Job] data: " + slea);
        byte mode = slea.readByte(); //1 = start 2 = end
        int cid = slea.readInt(); //character id
        byte job = slea.readByte(); //part time job
        if (mode == 0) {
            LoginPacket.partTimeJob(cid, (byte) 0, System.currentTimeMillis());
        } else if (mode == 1) {
            LoginPacket.partTimeJob(cid, job, System.currentTimeMillis());
        }
    }
}
