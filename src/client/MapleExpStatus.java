package client;

/**
 * @author RichardL
 * @see EXP_INCREASE_INFO::Decode()
 */
public enum MapleExpStatus {
	
	PARTY_BONUS(0x10),
	WEDDING_BONUS(0x20),
	ITEM_BONUS(0x40),
	// nPremiumIPBonusExp
	// nRainbowWeekEventBonusExp
	// nBoomUpEventBonusExp
	// nPlusExpBuffBonusExp
	// nIndieBonusExp
	// nRelaxBonusExp
	// nInstallItemBonusExp
	// nAswanWinnerBonusExp
	INCREASE_EXP_RATE(0x80000),
	// nValuePackBonusExp
	// nExpByIncPQExpR
	// nBaseAddExp
	// nBloodAllianceBonusExp
	// nFreezeHotEventBonusExp
	BURNING_FIELD(0x400000);
	// nUserHPRateBonusExp
	// nFieldValueBonusExp
	// nMobKillBonusExp
	// nLiveEventBonusExp
	
	private int flag;
	
	private MapleExpStatus(int flag) {
		this.flag = flag;
	}

	public int getFlag() {
		return flag;
	}
}
