package client;

import java.util.EnumMap;

public class MapleExp {
	
	private int exp, increaseBonusExp, increaseExpRate, partyBonusExp, partyBonusExpRate, questBonusExpRate, burningFieldBonusExp, burningFieldExpRate;
	private boolean lastHit, partyBonus, quest;
	private EnumMap<MapleExpStatus, Integer> expStats;
	
	public MapleExp(int exp) {
		this.exp = exp;
		setExpStats(new EnumMap<>(MapleExpStatus.class));
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}
	
	public int getTotalExp() {
		return exp + partyBonusExp + increaseBonusExp + burningFieldBonusExp;
	}

	public boolean isLastHit() {
		return lastHit;
	}

	public void setLastHit(boolean lastHit) {
		this.lastHit = lastHit;
	}

	public boolean isQuest() {
		return quest;
	}

	public void setQuest(boolean quest) {
		this.quest = quest;
	}

	/**
	 * @return the questBonusExpRate
	 */
	public int getQuestBonusExpRate() {
		return questBonusExpRate;
	}

	/**
	 * @param questBonusExpRate the questBonusExpRate to set
	 */
	public void setQuestBonusExpRate(int questBonusExpRate) {
		this.questBonusExpRate = questBonusExpRate;
	}

	/**
	 * @return the partyBonus
	 */
	public boolean isPartyBonus() {
		return partyBonus;
	}

	/**
	 * @param partyBonus the partyBonus to set
	 */
	public void setPartyBonus(boolean partyBonus) {
		this.partyBonus = partyBonus;
	}

	/**
	 * @return the partyBonusExpRate
	 */
	public int getPartyBonusExpRate() {
		return partyBonusExpRate;
	}

	/**
	 * @param partyBonusExpRate the partyBonusExpRate to set
	 */
	public void setPartyBonusExpRate(int partyBonusExpRate) {
		this.partyBonusExpRate = partyBonusExpRate;
	}

	public int getPartyBonusExp() {
		return partyBonusExp;
	}

	public void setPartyBonusExp(int partyBonusExp) {
		this.partyBonusExp = partyBonusExp;
	}


	public int getIncreaseBonusExp() {
		return increaseBonusExp;
	}

	public void addIncreaseBonusExp(int increaseBonusExp) {
		this.increaseBonusExp += increaseBonusExp;
	}

	public int getIncreaseExpRate() {
		return increaseExpRate;
	}

	public void setIncreaseExpRate(int increaseExpRate) {
		this.increaseExpRate = increaseExpRate;
	}

	/**
	 * Returns the exp bonus gained from Burning Field.
	 * @return the burningFieldBonusExp
	 */
	public int getBurningFieldBonusExp() {
		return burningFieldBonusExp;
	}

	/**
	 * Sets the exp bonus gained from Burning Field.
	 * @param burningFieldBonusExp the burningFieldBonusExp to set
	 */
	public void setBurningFieldBonusExp(int burningFieldBonusExp) {
		this.burningFieldBonusExp = burningFieldBonusExp;
	}

	/**
	 * Returns the exp rate from the Burning Field bonus.
	 * @return the burningFieldExpRate
	 */
	public int getBurningFieldExpRate() {
		return burningFieldExpRate;
	}

	/**
	 * Sets the exp rate from the Burning Field bonus.
	 * @param burningFieldExpRate the burningFieldExpRate to set
	 */
	public void setBurningFieldExpRate(int burningFieldExpRate) {
		this.burningFieldExpRate = burningFieldExpRate;
	}

	public EnumMap<MapleExpStatus, Integer> getExpStats() {
		return expStats;
	}

	public void setExpStats(EnumMap<MapleExpStatus, Integer> expStats) {
		this.expStats = expStats;
	}
	

}
