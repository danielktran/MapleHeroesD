package net.packet.field;

import client.character.MapleCharacter;
import client.inventory.Equip;
import net.SendPacketOpcode;
import tools.data.MaplePacketWriter;

public class UserPacket {
	
	/**
	 * @see CUser::OnChat()
	 */
	public static byte[] onChatText(final int charID, final String text, final boolean whiteBG, final int show) {
        MaplePacketWriter out = new MaplePacketWriter(SendPacketOpcode.CHATTEXT);
		out.writeInt(charID);
        out.write(whiteBG ? 1 : 0);
        out.writeMapleAsciiString(text);
        out.write(show);
        out.write(0);
        out.write(-1);

        return out.getPacket();
    }
	
	/**
     * 
     * @see CUser::OnADBoard()
     */
    public static byte[] useChalkboard(final int charID, final String msg) {
        MaplePacketWriter out = new MaplePacketWriter(SendPacketOpcode.CHALKBOARD);
		out.writeInt(charID);
        if (msg == null || msg.length() <= 0) {
            out.write(0);
        } else {
            out.write(1);
            out.writeMapleAsciiString(msg);
        }

        return out.getPacket();
    }

	/**
	 * @see CUserPool::SetConsumeItemEffect()
	 */
	public static byte[] setConsumeItemEffect(final int charID, final int itemID) {
        final MaplePacketWriter out = new MaplePacketWriter(SendPacketOpcode.SHOW_CONSUME_EFFECT);
        out.writeInt(charID);
        out.writeInt(itemID);

        return out.getPacket();
    }
	
	/**
	 * @see CUser::ShowItemUpgradeEffect()
	 */
	public static byte[] showScrollEffect(final int charID, final Equip.ScrollResult scrollSuccess, final boolean legendarySpirit, final int item, final int scroll) {
        MaplePacketWriter out = new MaplePacketWriter(SendPacketOpcode.SHOW_SCROLL_EFFECT);
		out.writeInt(charID);
        out.write(scrollSuccess == Equip.ScrollResult.SUCCESS ? 1 : scrollSuccess == Equip.ScrollResult.CURSE ? 2 : 0); // nUpgradeRetType
        out.write(legendarySpirit ? 1 : 0); // bEnchantSkill
        out.writeInt(scroll); // nUItemID or Scroll
        out.writeInt(item); // nEItemID or Item
        out.writeInt(0); // IDB Skip
        out.write(0); // IDB Skip
        out.write(0);

        return out.getPacket();
    }
	
	/**
	 * @see CUser::ShowItemReleaseEffect()
	 */
	public static byte[] showMagnifyingEffect(final int charID, final short pos) {
        MaplePacketWriter out = new MaplePacketWriter(SendPacketOpcode.SHOW_MAGNIFYING_EFFECT);
		out.writeInt(charID);
        out.writeShort(pos);

        return out.getPacket();
    }
	
	/**
	 * @see CUser::ShowItemUnreleaseEffect()
	 */
	public static byte[] showPotentialReset(final int charID, final boolean success, final int itemID) {
        MaplePacketWriter out = new MaplePacketWriter(SendPacketOpcode.SHOW_POTENTIAL_RESET);
		out.writeInt(charID);
        out.write(success ? 1 : 0); // bSuccess
        out.writeInt(itemID); // nItemID

        /*
         if (!succes) {
         if (itemid / 100 == 20495 || itemid == 5062301) {//lol the itemid doesn't even exists yet.
         'Failed to expand Potential slots.'
         } else {
         'Resetting Potential has failed due to insufficient space in the Use item.'
         }
         } else {
         if (itemid / 100 == 20495 || itemid == 5062301) {//lol the itemid doesn't even exists yet.
         'Successfully expanded Potential slots.'
         } else {
         if (itemid != 2710000) {
         'Potential has been reset.\r\nYou've obtained: %s.' (%s is item name)
         }
         'Potential has been reset.'
         }
         }
         */
        return out.getPacket();
    }
	
	/**
	 * @see CUser::OnSetDamageSkin()
	 */
	public static byte[] setDamageSkin(final int charID, final int skinID) {
    	MaplePacketWriter out = new MaplePacketWriter(SendPacketOpcode.SET_DAMAGE_SKIN);
        out.writeInt(charID);
        out.writeInt(skinID);
        
        return out.getPacket();
    }
	
	/**
	 * @see CUser::OnSetPremiumDamageSkin()
	 */
	public static byte[] setPremiumDamageSkin(final int charID, final int skinID) {
    	MaplePacketWriter out = new MaplePacketWriter(SendPacketOpcode.SET_PREMIUM_DAMAGE_SKIN);
        out.writeInt(charID);
        out.writeInt(skinID);
        
        return out.getPacket();
    }
	
	/**
	 * @see CUser::OnSetSoulEffect()
	 */
	public static byte[] setSoulEffect(final int charID, final boolean soulEffect) {
    	MaplePacketWriter out = new MaplePacketWriter(SendPacketOpcode.SET_SOUL_EFFECT);
    	out.writeInt(charID);
        out.write(soulEffect);
        
        return out.getPacket();
    }
	
	/**
	 * @see CUser::OnSitResult()
	 */
	public static byte[] cancelChair(final int charID) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANCEL_CHAIR);
		mpw.writeInt(charID);
        mpw.write(0);

        return mpw.getPacket();
    }
	
	/**
	 * @see CUser::OnSetOneTimeAction()
	 */
	public static byte[] craftMake(int charID, int oneTimeAction, int duration) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CRAFT_EFFECT);
		mpw.writeInt(charID);
        mpw.writeInt(oneTimeAction); // nOneTimeAction
        mpw.writeInt(duration); // tDuration

        return mpw.getPacket();
    }
	
	/**
	 * @see CUser::OnMakingSkillResult()
	 */
	public static byte[] craftFinished(final int charID, final int craftID, final int ranking, final int itemID, final int quantity, final int exp) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CRAFT_COMPLETE);
		mpw.writeInt(charID);
        mpw.writeInt(craftID); // nRecipeCode
        mpw.writeInt(ranking); // nResult
        if (ranking == 25 || ranking == 26 || ranking == 27) {
        	mpw.writeInt(itemID); // nCreatedItemID
            mpw.writeInt(quantity); // nItemCount
        }
        mpw.writeInt(exp); // nIncSkillProficiency

        return mpw.getPacket();
    }
	
	/**
	 * @see CUser::OnSetMakingMeisterSkillEff()
	 */
	public static byte[] setCraftSkillEffect(final int charID, final int skillID) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CRAFT_MEISTER_EFFECT);
		mpw.writeInt(charID);
        mpw.writeInt(skillID); // nSkillID

        return mpw.getPacket();
    }
	
	/**
	 * @see CUser::OnGatherResult()
	 */
	public static byte[] harvestResult(final int charID, final boolean success) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.HARVESTED);
		mpw.writeInt(charID);
        mpw.write(success ? 1 : 0);

        return mpw.getPacket();
    }

}
