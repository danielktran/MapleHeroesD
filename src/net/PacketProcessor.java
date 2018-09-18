/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net;

import net.server.handlers.*;
import net.server.login.handlers.*;
import net.server.talk.handlers.*;
import net.server.channel.handlers.*;
import net.server.channel.handlers.chat.*;
import net.server.channel.handlers.inventory.*;
import net.server.channel.handlers.monster.*;
import net.server.channel.handlers.pet.*;
import net.server.channel.handlers.player.*;
import net.server.channel.handlers.stat.*;
import net.server.channel.handlers.summon.*;
import net.server.farm.handlers.*;

public final class PacketProcessor {

	private static final PacketProcessor instance = new PacketProcessor();
    private MaplePacketHandler[] handlers;

    private PacketProcessor() {
        int maxRecvOp = 0;
        for (RecvPacketOpcode op : RecvPacketOpcode.values()) {
            if (op.getOpcode() > maxRecvOp) {
                maxRecvOp = op.getOpcode();
            }
        }
        handlers = new MaplePacketHandler[maxRecvOp + 1];
    }

    public MaplePacketHandler getHandler(short packetHeader) {
        if (packetHeader > handlers.length) {
            return null;
        }
        MaplePacketHandler handler = handlers[packetHeader];
        if (handler != null) {
            return handler;
        }
        return null;
    }
    
    public void registerHandler(MaplePacketHandler handler) {
        try {
            handlers[handler.getRecvOpcode().getOpcode()] = handler;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Error registering packet handler - " + handler.getRecvOpcode().name());
            e.printStackTrace();
        }
    }
    
    public static PacketProcessor getInstance() {
    	return instance;
    }

    public void initialize() {
        handlers = new MaplePacketHandler[handlers.length];
        
        /*
         * General Handlers
         */
        registerHandler(new AuthRequestHandler(RecvPacketOpcode.AUTH_REQUEST));
        registerHandler(new PongHandler(RecvPacketOpcode.PONG));
        registerHandler(new CrashHandler(RecvPacketOpcode.CRASH_INFO));
        /*
         * Talk Handlers
         */
        registerHandler(new MigrateInHandler(RecvPacketOpcode.MIGRATE_IN));
        registerHandler(new GuildInfoInHandler(RecvPacketOpcode.TALK_GUILD_INFO));
        registerHandler(new GuildChatHandler(RecvPacketOpcode.GUILDCHAT));
        //registerHandler(RecvPacketOpcode.PONG_TALK, new PongTalkHandler(RecvPacketOpcode.PONG_TALK));
    	/*
    	 * Login Handlers
    	 */
    	registerHandler(new ClientHelloHandler(RecvPacketOpcode.CLIENT_HELLO));
    	registerHandler(new ClientRequestHandler(RecvPacketOpcode.CLIENT_REQUEST));
    	registerHandler(new LoginPasswordHandler(RecvPacketOpcode.LOGIN_PASSWORD));
    	registerHandler(new ViewServerListHandler(RecvPacketOpcode.VIEW_SERVERLIST));
        registerHandler(new ServerlistRequestHandler(RecvPacketOpcode.REDISPLAY_SERVERLIST));
        registerHandler(new CharlistRequestHandler(RecvPacketOpcode.CHARLIST_REQUEST));
        registerHandler(new CharSelectWithPicHandler(RecvPacketOpcode.CHAR_SELECT));
        registerHandler(new PlayerLoggedInHandler(RecvPacketOpcode.PLAYER_LOGGEDIN));
        registerHandler(new AcceptToSHandler(RecvPacketOpcode.ACCEPT_TOS)); // Doesn't do anything yet.
        registerHandler(new ServerlistRequestHandler(RecvPacketOpcode.SERVERLIST_REQUEST));
        registerHandler(new ServerStatusRequestHandler(RecvPacketOpcode.SERVERSTATUS_REQUEST));
        registerHandler(new CheckCharNameHandler(RecvPacketOpcode.CHECK_CHAR_NAME));
        registerHandler(new CreateCharHandler(RecvPacketOpcode.CREATE_CHAR));
        registerHandler(new CreateCharHandler(RecvPacketOpcode.CREATE_SPECIAL_CHAR));
        registerHandler(new CreateUltimateHandler(RecvPacketOpcode.CREATE_ULTIMATE));
        registerHandler(new DeleteCharHandler(RecvPacketOpcode.DELETE_CHAR));
        registerHandler(new CharacterCardHandler(RecvPacketOpcode.CHARACTER_CARD));
        registerHandler(new CharSelectWithNoPicHandler(RecvPacketOpcode.CHAR_SELECT_NO_PIC));
        registerHandler(new ChangePicHandler(RecvPacketOpcode.CHANGE_PIC_REQUEST));
        registerHandler(new PartTimeJobHandler(RecvPacketOpcode.PART_TIME_JOB));
    	/*
    	 * Channel Handlers
    	 */
    	registerHandler(new ChangeMapHandler(RecvPacketOpcode.CHANGE_MAP));
        registerHandler(new ChangeChannelHandler(RecvPacketOpcode.CHANGE_CHANNEL));
        registerHandler(new ChangeChannelHandler(RecvPacketOpcode.CHANGE_ROOM_CHANNEL));
        registerHandler(new EnterCashShopHandler(RecvPacketOpcode.ENTER_CASH_SHOP));
        /*
         * Farm Handlers
         */
        registerHandler(new EnterFarmHandler(RecvPacketOpcode.ENTER_FARM));
        registerHandler(new LeaveFarmHandler(RecvPacketOpcode.LEAVE_FARM));
        registerHandler(new CreateFarmHandler(RecvPacketOpcode.CREATE_FARM));
        registerHandler(new FarmCompleteQuestHandler(RecvPacketOpcode.FARM_COMPLETE_QUEST));
        registerHandler(new FarmPlaceObjectHandler(RecvPacketOpcode.PLACE_FARM_OBJECT));
        registerHandler(new FarmShopBuyHandler(RecvPacketOpcode.FARM_SHOP_BUY));
        registerHandler(new FarmHarvestHandler(RecvPacketOpcode.HARVEST_FARM_BUILDING));
        registerHandler(new FarmUseItemHandler(RecvPacketOpcode.USE_FARM_ITEM));
        registerHandler(new RenameMonsterHandler(RecvPacketOpcode.RENAME_MONSTER));
        registerHandler(new NutureMonsterHandler(RecvPacketOpcode.NURTURE_MONSTER));
        registerHandler(new FarmCheckQuestHandler(RecvPacketOpcode.FARM_CHECK_QUEST));
        registerHandler(new FarmFirstEntryHandler(RecvPacketOpcode.FARM_FIRST_ENTRY));
        /*
         * Azwan Handlers
         */
        registerHandler(new EnterAzwanHandler(RecvPacketOpcode.ENTER_AZWAN));
        registerHandler(new EnterAzwanEventHandler(RecvPacketOpcode.ENTER_AZWAN_EVENT));
        registerHandler(new LeaveAzwanHandler(RecvPacketOpcode.LEAVE_AZWAN));
        
        registerHandler(new EnterPVPHandler(RecvPacketOpcode.ENTER_PVP));
        registerHandler(new EnterPVPHandler(RecvPacketOpcode.ENTER_PVP_PARTY));
        registerHandler(new LeavePVPHandler(RecvPacketOpcode.LEAVE_PVP));
        registerHandler(new RespawnPVPHandler(RecvPacketOpcode.PVP_RESPAWN));
        /*
         * Player Handlers
         */
        registerHandler(new MovePlayerHandler(RecvPacketOpcode.MOVE_PLAYER));
        registerHandler(new CancelChairHandler(RecvPacketOpcode.CANCEL_CHAIR));
        registerHandler(new UseChairHandler(RecvPacketOpcode.USE_CHAIR));
        registerHandler(new CloseRangeDamageHandler(RecvPacketOpcode.CLOSE_RANGE_ATTACK));
        registerHandler(new RangedAttackHandler(RecvPacketOpcode.RANGED_ATTACK));
        registerHandler(new MagicDamageHandler(RecvPacketOpcode.MAGIC_ATTACK));
        registerHandler(new CloseRangeDamageHandler(RecvPacketOpcode.PASSIVE_ENERGY));
        registerHandler(new TakeDamageHandler(RecvPacketOpcode.TAKE_DAMAGE));
        registerHandler(new PVPAttackHandler(RecvPacketOpcode.PVP_ATTACK));
        
        registerHandler(new CloseChalkboardHandler(RecvPacketOpcode.CLOSE_CHALKBOARD));
        registerHandler(new FaceExpressionHandler(RecvPacketOpcode.FACE_EXPRESSION));
        registerHandler(new MoveAndroidHandler(RecvPacketOpcode.MOVE_ANDROID));
        registerHandler(new AndroidFaceExpressionHandler(RecvPacketOpcode.ANDROID_FACE_EXPRESSION));
        
        registerHandler(new WheelOfFortuneHandler(RecvPacketOpcode.WHEEL_OF_FORTUNE)); // Doesn't do anything.
        registerHandler(new LinkSkillHandler(RecvPacketOpcode.LINK_SKILL));
        /*
         * Stat & Skill Handlers
         */
        registerHandler(new SkillMacroHandler(RecvPacketOpcode.SKILL_MACRO));
        registerHandler(new SpecialStatHandler(RecvPacketOpcode.SPECIAL_STAT));
        registerHandler(new DistributeHyperHandler(RecvPacketOpcode.DISTRIBUTE_HYPER));
        registerHandler(new ResetHyperHandler(RecvPacketOpcode.RESET_HYPER));
        registerHandler(new DistributeAPHandler(RecvPacketOpcode.DISTRIBUTE_AP));
        registerHandler(new AutoAssignAPHandler(RecvPacketOpcode.AUTO_ASSIGN_AP));
        registerHandler(new DistributeSPHandler(RecvPacketOpcode.DISTRIBUTE_SP));
        registerHandler(new SpecialMoveHandler(RecvPacketOpcode.SPECIAL_MOVE));
        registerHandler(new CancelBuffHandler(RecvPacketOpcode.CANCEL_BUFF));
        
        
        registerHandler(new HealOverTimeHandler(RecvPacketOpcode.HEAL_OVER_TIME));
        registerHandler(new MesoDropHandler(RecvPacketOpcode.MESO_DROP));
        registerHandler(new GiveFameHandler(RecvPacketOpcode.GIVE_FAME));
        registerHandler(new CharInfoRequestHandler(RecvPacketOpcode.CHAR_INFO_REQUEST));
        registerHandler(new MonsterBookInfoHandler(RecvPacketOpcode.GET_BOOK_INFO));
        registerHandler(new MonsterBookDropsRequestHandler(RecvPacketOpcode.MONSTER_BOOK_DROPS));
        registerHandler(new ChangeCodexSetHandler(RecvPacketOpcode.CHANGE_CODEX_SET));
        /*
         * Chat Handlers
         */
        registerHandler(new GeneralChatHandler(RecvPacketOpcode.GENERAL_CHAT));
        registerHandler(new AdminChatHandler(RecvPacketOpcode.ADMIN_CHAT));
        registerHandler(new PartyChatHandler(RecvPacketOpcode.PARTYCHAT));
        registerHandler(new CommandHandler(RecvPacketOpcode.COMMAND));
        registerHandler(new MessengerHandler(RecvPacketOpcode.MESSENGER));
        /*
         * NPC Handlers
         */
        registerHandler(new NPCTalkHandler(RecvPacketOpcode.NPC_TALK));
        registerHandler(new NPCTalkMoreHandler(RecvPacketOpcode.NPC_TALK_MORE));
        registerHandler(new NPCAnimationHandler(RecvPacketOpcode.NPC_ACTION));
        registerHandler(new QuestActionHandler(RecvPacketOpcode.QUEST_ACTION));
        registerHandler(new NPCShopHandler(RecvPacketOpcode.NPC_SHOP));
        registerHandler(new StorageHandler(RecvPacketOpcode.STORAGE_OPERATION));

        //registerHandler(new UseHiredMerchantHandler(RecvPacketOpcode.USE_HIRED_MERCHANT));
        registerHandler(new MerchItemStoreHandler(RecvPacketOpcode.MERCH_ITEM_STORE));
        registerHandler(new PackageOperationHandler(RecvPacketOpcode.PACKAGE_OPERATION));
        registerHandler(new CancelMechHandler(RecvPacketOpcode.CANCEL_MECH));
        registerHandler(new HollyHandler(RecvPacketOpcode.HOLLY));
        registerHandler(new OwlHandler(RecvPacketOpcode.OWL));
        registerHandler(new OwlWarpHandler(RecvPacketOpcode.OWL_WARP));
        /*
         * Inventory Handlers
         */
        registerHandler(new ItemPickupHandler(RecvPacketOpcode.ITEM_PICKUP));
        registerHandler(new ItemSortHandler(RecvPacketOpcode.ITEM_SORT));
        registerHandler(new ItemGatherHandler(RecvPacketOpcode.ITEM_GATHER));
        registerHandler(new ItemMoveHandler(RecvPacketOpcode.ITEM_MOVE));
        registerHandler(new UseItemHandler(RecvPacketOpcode.USE_ITEM));
        registerHandler(new UseCashItemHandler(RecvPacketOpcode.USE_CASH_ITEM));
        registerHandler(new UseItemEffectHandler(RecvPacketOpcode.USE_ITEMEFFECT));
        registerHandler(new CancelItemEffectHandler(RecvPacketOpcode.CANCEL_ITEM_EFFECT));
        registerHandler(new UseSummonBagHandler(RecvPacketOpcode.USE_SUMMON_BAG));
        registerHandler(new UseMountFoodHandler(RecvPacketOpcode.USE_MOUNT_FOOD));
        registerHandler(new UseScriptedNPCItemHandler(RecvPacketOpcode.USE_SCRIPTED_NPC_ITEM));
        registerHandler(new ReturnScrollHandler(RecvPacketOpcode.USE_RETURN_SCROLL));
        registerHandler(new MagnifyGlassHandler(RecvPacketOpcode.USE_MAGNIFY_GLASS));
        registerHandler(new UseBagHandler(RecvPacketOpcode.USE_BAG));
        registerHandler(new UseRecipeHandler(RecvPacketOpcode.USE_RECIPE));
        registerHandler(new UseCosmeticHandler(RecvPacketOpcode.USE_COSMETIC));
        registerHandler(new UseNebuliteHandler(RecvPacketOpcode.USE_NEBULITE));
        registerHandler(new UseAlienSocketHandler(RecvPacketOpcode.USE_ALIEN_SOCKET));
        registerHandler(new UseAlienSocketResponseHandler(RecvPacketOpcode.USE_ALIEN_SOCKET_RESPONSE));
        registerHandler(new UseNebuliteFusionHandler(RecvPacketOpcode.USE_NEBULITE_FUSION));
        registerHandler(new UseUpgradeScrollHandler(RecvPacketOpcode.USE_UPGRADE_SCROLL));
        registerHandler(new UsePotentialScrollHandler(RecvPacketOpcode.USE_FLAG_SCROLL));
        registerHandler(new UsePotentialScrollHandler(RecvPacketOpcode.USE_POTENTIAL_SCROLL));
        registerHandler(new UsePotentialScrollHandler(RecvPacketOpcode.USE_EQUIP_SCROLL));
        registerHandler(new UseAbyssScrollHandler(RecvPacketOpcode.USE_ABYSS_SCROLL));
        registerHandler(new UseCarvedSealHandler(RecvPacketOpcode.USE_CARVED_SEAL));
        registerHandler(new UseCraftedCubeHandler(RecvPacketOpcode.USE_CRAFTED_CUBE));
        registerHandler(new UseTreasureChestHandler(RecvPacketOpcode.USE_TREASURE_CHEST));
        registerHandler(new UseSkillBookHandler(RecvPacketOpcode.USE_SKILL_BOOK));
        registerHandler(new UseExpPotionHandler(RecvPacketOpcode.USE_EXP_POTION));
        registerHandler(new UseCatchItemHandler(RecvPacketOpcode.USE_CATCH_ITEM));

        /*
         * Familiar Handlers
         */
        registerHandler(new UseFamiliarHandler(RecvPacketOpcode.USE_FAMILIAR));
        registerHandler(new SpawnFamiliarHandler(RecvPacketOpcode.SPAWN_FAMILIAR));
        registerHandler(new RenameFamiliarHandler(RecvPacketOpcode.RENAME_FAMILIAR));
        registerHandler(new MoveFamiliarHandler(RecvPacketOpcode.MOVE_FAMILIAR));
        registerHandler(new TouchFamiliarHandler(RecvPacketOpcode.TOUCH_FAMILIAR));
        registerHandler(new AttackFamiliarHandler(RecvPacketOpcode.ATTACK_FAMILIAR));
        registerHandler(new RevealFamiliarHandler(RecvPacketOpcode.REVEAL_FAMILIAR)); // Does not do anything.
        /*
         * Pet Handlers
         */
        registerHandler(new SpawnPetHandler(RecvPacketOpcode.SPAWN_PET));
        registerHandler(new MovePetHandler(RecvPacketOpcode.MOVE_PET));
        registerHandler(new PetChatHandler(RecvPacketOpcode.PET_CHAT));
        registerHandler(new PetCommandHandler(RecvPacketOpcode.PET_COMMAND));
        registerHandler(new PetFoodHandler(RecvPacketOpcode.USE_PET_FOOD));
        registerHandler(new PetLootHandler(RecvPacketOpcode.PET_LOOT));
        registerHandler(new PetAutoPotHandler(RecvPacketOpcode.PET_AUTO_POT));
        registerHandler(new MoveHakuHandler(RecvPacketOpcode.MOVE_HAKU));
        registerHandler(new ChangeHakuHandler(RecvPacketOpcode.CHANGE_HAKU));
        /*
         * Summon Handlers
         */
        registerHandler(new MoveSummonHandler(RecvPacketOpcode.MOVE_SUMMON));
        registerHandler(new SummonAttackHandler(RecvPacketOpcode.SUMMON_ATTACK));
        registerHandler(new RemoveSummonHandler(RecvPacketOpcode.REMOVE_SUMMON));
        registerHandler(new DamageSummonHandler(RecvPacketOpcode.DAMAGE_SUMMON));
        registerHandler(new SubSummonHandler(RecvPacketOpcode.SUB_SUMMON));
        registerHandler(new PVPSummonHandler(RecvPacketOpcode.PVP_SUMMON));
        registerHandler(new MoveDragonHandler(RecvPacketOpcode.MOVE_DRAGON));
        
        registerHandler(new PetBuffHandler(RecvPacketOpcode.PET_BUFF));
        registerHandler(new CancelDebuffHandler(RecvPacketOpcode.CANCEL_DEBUFF)); // Does nothing.
        registerHandler(new SpecialPortalHandler(RecvPacketOpcode.SPECIAL_PORTAL));
        registerHandler(new InnerPortalHandler(RecvPacketOpcode.USE_INNER_PORTAL));
        registerHandler(new TeleportRockAddMapHandler(RecvPacketOpcode.TELEPORT_ROCK_ADD_MAP));
        registerHandler(new ReportHandler(RecvPacketOpcode.REPORT));
        registerHandler(new ReissueMedalHandler(RecvPacketOpcode.REISSUE_MEDAL));

        
        registerHandler(new PlayerInteractionHandler(RecvPacketOpcode.PLAYER_INTERACTION));
        registerHandler(new PartyOperationHandler(RecvPacketOpcode.PARTY_OPERATION));
        registerHandler(new PartyRequestHandler(RecvPacketOpcode.PARTY_REQUEST));
        registerHandler(new AllowPartyInviteHandler(RecvPacketOpcode.ALLOW_PARTY_INVITE));
        registerHandler(new GuildOperationHandler(RecvPacketOpcode.GUILD_OPERATION));
        registerHandler(new GuildInvitationHandler(RecvPacketOpcode.GUILD_INVITATION));
        //registerHandler(new AllianceOperationHandler(RecvPacketOpcode.ALLIANCE_OPERATION));
        registerHandler(new AllianceOperationHandler(RecvPacketOpcode.ALLIANCE_REQUEST));
        
        //registerHandler(new BuddylistModifyHandler(RecvPacketOpcode.BUDDYLIST_MODIFY));
        registerHandler(new MysticDoorHandler(RecvPacketOpcode.USE_MYSTIC_DOOR));
        registerHandler(new MechDoorHandler(RecvPacketOpcode.USE_MECH_DOOR));
        registerHandler(new HolyFountainHandler(RecvPacketOpcode.USE_HOLY_FOUNTAIN));
        registerHandler(new ChangeKeymapHandler(RecvPacketOpcode.CHANGE_KEYMAP)); 
        
        /*
         * Monster Handlers
         */
        registerHandler(new MoveLifeHandler(RecvPacketOpcode.MOVE_LIFE));
        registerHandler(new AutoAggroHandler(RecvPacketOpcode.AUTO_AGGRO));
        registerHandler(new FriendlyDamageHandler(RecvPacketOpcode.FRIENDLY_DAMAGE));
        registerHandler(new MonsterBombHandler(RecvPacketOpcode.MONSTER_BOMB));
        registerHandler(new MobBombHandler(RecvPacketOpcode.MOB_BOMB));
        registerHandler(new HypnotizeDamageHandler(RecvPacketOpcode.HYPNOTIZE_DMG));
        registerHandler(new MobNodeHandler(RecvPacketOpcode.MOB_NODE));
        registerHandler(new DisplayNodeHandler(RecvPacketOpcode.DISPLAY_NODE));
        
        /*
         * Reactor Handlers
         */
        registerHandler(new DamageReactorHandler(RecvPacketOpcode.DAMAGE_REACTOR));
        registerHandler(new TouchReactorHandler(RecvPacketOpcode.CLICK_REACTOR));
        registerHandler(new TouchReactorHandler(RecvPacketOpcode.TOUCH_REACTOR));
        
        
    }
}

