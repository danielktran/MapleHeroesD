package constants;

public class Skills {
	
	public static final int LEVELUP = 80001770;
	
	public class Magician {
		public static final int MP_BOOST = 2000006;
		public static final int MAGIC_ARMOR = 2000010;
		public static final int MAGIC_GUARD = 2001002;
	}
	
	public class FirePoisonMage {
		public static final int MP_EATER = 2100000;
		public static final int SPELL_MASTERY = 2100006;
		public static final int HIGH_WISDOM = 2100007;
		public static final int MEDITATION = 2101001;
		public static final int ELEMENTAL_ADAPATION = 2111011;
	}
	
	public class IceLightningMage {
		public static final int MP_EATER = 2200000;
		public static final int SPELL_MASTERY = 2200006;
		public static final int HIGH_WISDOM = 2200007;
		public static final int MEDITATION = 2201001;
		public static final int ELEMENTAL_ADAPTATION = 2211012;
	}
	
	public class Bishop {
		public static final int MP_EATER = 2300000;
		public static final int SPELL_MASTERY = 2300006;
		public static final int HIGH_WISDOM = 2300007;
		public static final int BLESSED_ENSEMBLE = 2300009;
		public static final int BLESS = 2301004;
		public static final int HEAL = 2301002;
		public static final int HOLY_FOCUS = 2310008;
		public static final int ARCANE_OVERDRIVE = 2310010;
		public static final int HOLY_SYMBOL = 2311003;
		public static final int SHINING_RAY = 2311004;
		public static final int HOLY_MAGIC_SHELL = 2311009;
		public static final int HOLY_FOUNTAIN = 2311011;
		public static final int DIVINE_PROTECTION = 2311012;
		public static final int BUFF_MASTERY = 2320012;
		public static final int BIG_BANG = 2321001;
		public static final int ADVANCED_BLESSING = 2321005;
		public static final int GENESIS = 2321008;
		public static final int BLESSED_HARMONY = 2320013;
	}
	
	public class Rogue {
		public static final int HASTE = 4001005;
	}
	
	public class NightLord {
		public static final int SHADOW_WEB = 4111003;
	}
	
	public static final boolean affectsBlessedEnsemble(int skillid) {
		return skillid == Bishop.BLESS || skillid == Bishop.HOLY_SYMBOL || skillid == Bishop.HOLY_MAGIC_SHELL || skillid == Bishop.ADVANCED_BLESSING;
	}
}
