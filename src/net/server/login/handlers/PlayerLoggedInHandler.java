package net.server.login.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.MapleClient;
import client.MapleQuestStatus;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.character.MapleCharacter;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MapleWeaponType;
import constants.GameConstants;
import constants.ServerConfig;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.PlayersHandler;
import net.packet.CField;
import net.packet.CSPacket;
import net.packet.CWvsContext;
import net.packet.CWvsContext.BuddylistPacket;
import net.packet.CWvsContext.GuildPacket;
import net.packet.JobPacket.AvengerPacket;
import net.server.cashshop.CashShopServer;
import net.server.cashshop.handlers.CashShopOperation;
import net.server.channel.ChannelServer;
import net.server.farm.FarmServer;
import net.server.farm.handlers.FarmOperation;
import net.server.login.LoginServer;
import net.world.CharacterIdChannelPair;
import net.world.CharacterTransfer;
import net.world.MapleMessenger;
import net.world.MapleMessengerCharacter;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.PlayerBuffStorage;
import net.world.World;
import net.world.exped.MapleExpedition;
import net.world.guild.MapleGuild;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.Triple;
import tools.data.LittleEndianAccessor;

public class PlayerLoggedInHandler extends AbstractMaplePacketHandler {

	public PlayerLoggedInHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public final boolean validateState(MapleClient c) {
		return !c.isLoggedIn();
	}
	
	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		lea.readInt();
        final int charid = lea.readInt();
        
        final Map<Skill, SkillEntry> list = new HashMap<>();
        try {
        MapleCharacter player;
        CharacterTransfer transfer = CashShopServer.getPlayerStorage().getPendingCharacter(charid);
        if (transfer != null) {
//            c.getSession().write(CWvsContext.BuffPacket.cancelBuff());
            CashShopOperation.EnterCS(transfer, c);
            return;
        }
        CharacterTransfer farmtransfer = FarmServer.getPlayerStorage().getPendingCharacter(charid);
        if (farmtransfer != null) {
            FarmOperation.EnterFarm(farmtransfer, c);
            return;
        }
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            transfer = cserv.getPlayerStorage().getPendingCharacter(charid);
            if (transfer != null) {
                c.setChannel(cserv.getChannel());
                break;
            }
        }

        if (transfer == null) { // Player isn't in storage, probably isn't CC
        	System.out.println("Debug 1");
            Triple<String, String, Integer> ip = LoginServer.getLoginAuth(charid);
            String s = c.getSessionIPAddress();
            if (ip == null || !s.substring(s.indexOf('/') + 1, s.length()).equals(ip.left)) {
                if (ip != null) {
                    LoginServer.putLoginAuth(charid, ip.left, ip.mid, ip.right);
                }
                c.getSession().close();
                return;
            }
            c.setTempIP(ip.mid);
            c.setChannel(ip.right);
            System.out.println("Debug 1.1");
            player = MapleCharacter.loadCharFromDB(charid, c, true);
            System.out.println("Debug 2");
        } else {
        	System.out.println("Debug 3");
            player = MapleCharacter.ReconstructChr(transfer, c, true);
        }
        final ChannelServer channelServer = c.getChannelServer();
        c.setPlayer(player);
        c.setAccID(player.getAccountID());

        if (!c.CheckIPAddress()) { // Remote hack
            c.getSession().close();
            return;
        }
        final int state = c.getLoginState();
        boolean allowLogin = false;
        if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL || state == MapleClient.LOGIN_NOTLOGGEDIN) {
            allowLogin = !World.isCharacterListConnected(c.loadCharacterNames(c.getWorld()));
        }
        if (!allowLogin) {
            c.setPlayer(null);
            c.getSession().close();
            return;
        }
        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
        channelServer.addPlayer(player);

        player.giveCoolDowns(PlayerBuffStorage.getCooldownsFromStorage(player.getID()));
        player.silentGiveBuffs(PlayerBuffStorage.getBuffsFromStorage(player.getID()));
        player.giveSilentDebuff(PlayerBuffStorage.getDiseaseFromStorage(player.getID()));
        if (GameConstants.isBeastTamer(player.getJob())) {
            String info = "bTail=1;bEar=1;TailID=" + player.getTail() + ";EarID=" + player.getEars();
            player.updateInfoQuest(GameConstants.BEAST_TEAMER_INFO, info, false);
        }
        c.getSession().write(CWvsContext.updateCrowns(new int[]{-1, -1, -1, -1, -1}));
        c.getSession().write(CField.getCharInfo(player));
        PlayersHandler.calcHyperSkillPointCount(c);
        c.getSession().write(CSPacket.enableCSUse());
        c.getSession().write(CWvsContext.updateSkills(c.getCharacter().getSkills(), false));//skill to 0 "fix"
        player.getStolenSkills();
  //      c.getSession().write(JobPacket.addStolenSkill());

        player.getMap().addPlayer(player);
        try {
            // Start of buddylist
            final int buddyIds[] = player.getBuddylist().getBuddyIds();
            World.Buddy.loggedOn(player.getName(), player.getID(), c.getChannel(), buddyIds);
            if (player.getParty() != null) {
                final MapleParty party = player.getParty();
                World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));

                if (party != null && party.getExpeditionId() > 0) {
                    final MapleExpedition me = World.Party.getExped(party.getExpeditionId());
                    if (me != null) {
                        c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(me, false, true));
                    }
                }
            }
            final CharacterIdChannelPair[] onlineBuddies = World.Find.multiBuddyFind(player.getID(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : onlineBuddies) {
                player.getBuddylist().get(onlineBuddy.getCharacterId()).setChannel(onlineBuddy.getChannel());
            }
            c.getSession().write(BuddylistPacket.updateBuddylist(player.getBuddylist().getBuddies()));

            // Start of Messenger
            final MapleMessenger messenger = player.getMessenger();
            if (messenger != null) {
                World.Messenger.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getCharacter()));
                World.Messenger.updateMessenger(messenger.getId(), c.getCharacter().getName(), c.getChannel());
            }

            // Start of Guild and alliance
            if (player.getGuildId() > 0) {
                World.Guild.setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                c.getSession().write(GuildPacket.showGuildInfo(player));
                final MapleGuild gs = World.Guild.getGuild(player.getGuildId());
                if (gs != null) {
                    final List<byte[]> packetList = World.Alliance.getAllianceInfo(gs.getAllianceId(), true);
                    if (packetList != null) {
                        for (byte[] pack : packetList) {
                            if (pack != null) {
                                c.getSession().write(pack);
                            }
                        }
                    }
                } else { //guild not found, change guild id
                    player.setGuildId(0);
                    player.setGuildRank((byte) 5);
                    player.setAllianceRank((byte) 5);
                    player.saveGuildStatus();
                }
            }
            if (player.getFamilyId() > 0) {
                World.Family.setFamilyMemberOnline(player.getMFC(), true, c.getChannel());
            }
            //c.getSession().write(FamilyPacket.getFamilyData());
            //c.getSession().write(FamilyPacket.getFamilyInfo(player));
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.Login_Error, e);
        }
        player.getClient().getSession().write(CWvsContext.broadcastMsg(channelServer.getServerMessage()));
        player.sendMacros();
        player.showNote();
        player.sendImp();
        player.updatePartyMemberHP();
        player.startFairySchedule(false);
        player.baseSkills(); //fix people who've lost skills.
        if (GameConstants.isZero(player.getJob())) {
            c.getSession().write(CWvsContext.updateSkills(player.getSkills(), false));
        }
        c.getSession().write(CField.getKeymap(player.getKeyLayout()));
        player.updatePetAuto();
        player.expirationTask(true, transfer == null);
        c.getSession().write(CWvsContext.updateMaplePoint(player.getCSPoints(2)));
        
         int[] warriorz = {25121131, 20050012, 20050073, 80001155, 80000000, 80000001, 80000002, 80000005, 80000006, 80001140, 80000050, 80000047, 80001040, 20051284, 20050285, 20051287, 25100010, 20050286, 20051251, 25120113, 25121116, 25001000, 25111111, 25120214, 25111206, 25120133, 25121030, 25121131, 71000251, 25110107, 25120112, 25110108, 25121108, 25120115, 25121211, 25120110, 25121209, 25100106, 25110210, 25111209, 25100009, 25100108, 25101111, 25100107, 25001204, 25000105, 25101205, 25101000};
            for (int i = 0; i < warriorz.length; i++) {
                c.getSession().write(CWvsContext.updateSkill(list, warriorz[i], c.getCharacter().getOriginSkillLevel(warriorz[i]), c.getCharacter().getMasterLevel(warriorz[i]), -1L));
            }

        
        if (player.getJob() == 132) { // DARKKNIGHT
            player.checkBerserk();
        }
        if (GameConstants.isXenon(player.getJob())) {
            player.startXenonSupply();
        }
        if (GameConstants.isDemonAvenger(player.getJob())) {
            c.getSession().write(AvengerPacket.giveAvengerHpBuff(player.getStat().getHp()));
        }
        player.spawnClones();
        player.spawnSavedPets();
        if (player.getStat().equippedSummon > 0) {
            SkillFactory.getSkill(player.getStat().equippedSummon + (GameConstants.getBeginnerJob(player.getJob()) * 1000)).getEffect(1).applyTo(player);
        }
        MapleQuestStatus stat = player.getQuestNoAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT));
        c.getSession().write(CWvsContext.pendantSlot(stat != null && stat.getCustomData() != null && Long.parseLong(stat.getCustomData()) > System.currentTimeMillis()));
        stat = player.getQuestNoAdd(MapleQuest.getInstance(GameConstants.QUICK_SLOT));
        c.getSession().write(CField.quickSlot(stat != null && stat.getCustomData() != null ? stat.getCustomData() : null));
        // c.getSession().write(CWvsContext.getFamiliarInfo(player));
        MapleInventory equipped = player.getInventory(MapleInventoryType.EQUIPPED);
        MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
        List<Short> slots = new ArrayList<>();
        for (Item item : equipped.newList()) {
            slots.add(item.getPosition());
        }
        for (short slot : slots) {
            if (GameConstants.isIllegalItem(equipped.getItem(slot).getItemId())) {
                MapleInventoryManipulator.removeFromSlot(player.getClient(), MapleInventoryType.EQUIPPED, slot, (short) 1, false);
            }
        }
        //c.getSession().write(CWvsContext.shopDiscount(ServerConstants.SHOP_DISCOUNT));
        //List<Pair<Integer, String>> npcs = new ArrayList<>();
        //npcs.add(new Pair<>(9070006, "Why...why has this happened to me? My knightly honor... My knightly pride..."));
        //npcs.add(new Pair<>(9000021, "Are you enjoying the event?"));
        //c.getSession().write(NPCPacket.setNpcScriptable(npcs));
        //c.getSession().write(NPCPacket.setNPCScriptable());
        player.updateReward();
        //        player.setDeathCount(99);
        //        c.getSession().write(CField.EffectPacket.updateDeathCount(99)); //for fun
        player.getClient().getSession().write(CWvsContext.broadcastMsg(channelServer.getServerMessage()));
        Thread.sleep(3100);
                if (c.getCharacter().getLevel() < 11 && ServerConfig.RED_EVENT_10) { 
        NPCScriptManager.getInstance().start(c, 9000108, "LoginTot");
        } else if (c.getCharacter().getLevel() > 10 && ServerConfig.RED_EVENT) { 
        NPCScriptManager.getInstance().start(c, 9000108, "LoginRed");
        }
  
        if (!GameConstants.isZero(player.getJob())) { //tell all players 2 login so u can remove this from ther
            Equip a = (Equip) player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
            if (GameConstants.getWeaponType(a.getItemId()) == MapleWeaponType.LONG_SWORD) {
                player.getInventory(MapleInventoryType.EQUIPPED).removeItem((short) -11);
            }
            Equip b = (Equip) player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
            if (GameConstants.getWeaponType(b.getItemId()) == MapleWeaponType.BIG_SWORD) {
                player.getInventory(MapleInventoryType.EQUIPPED).removeItem((short) -10);
            }
        }
        } catch (InterruptedException e) {
        }
	}

}
