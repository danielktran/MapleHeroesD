package constants;

public enum Interaction {

//  SET_ITEMS(0),
//  UPDATE_MESO(4),
//  SET_MESO(5),
//  SET_MESO_1(6),//was 4
//  SET_MESO_2(7),//was 4
//
//  CREATE(16),
//  INVITE_TRADE(21),//was11
//  DENY_TRADE(12),
//  VISIT(19),//was 9
  HIRED_MERCHANT_MAINTENANCE(31),//was 21//25
  //        //        CHAT_RECV(14),
  //        CHAT(24),
  //        EXIT(28),//was 18
  //        OPEN(17),
  //        CONFIRM_TRADE(8),//was 2
  //        START_TRADE(20),
  //        PLAYER_SHOP_ADD_ITEM(40),
  //        BUY_ITEM_PLAYER_SHOP(22),
  //        ADD_ITEM(23),
  //        BUY_ITEM_STORE(27),//was 24
  //        BUY_ITEM_HIREDMERCHANT(26),
  //        REMOVE_ITEM(30),
  //        MAINTANCE_OFF(31), //This is misspelled...
  //        MAINTANCE_ORGANISE(32),
  //        CLOSE_MERCHANT(52), //was33
  //        TAKE_MESOS(35),
  //        ADMIN_STORE_NAMECHANGE(37),
  //        VIEW_MERCHANT_VISITOR(38),
  //        VIEW_MERCHANT_BLACKLIST(39),
  //        MERCHANT_BLACKLIST_ADD(40),
  //        MERCHANT_BLACKLIST_REMOVE(41),
  //        REQUEST_TIE(51),
  //        ANSWER_TIE(53),//was52
  //        GIVE_UP(53),
  //        REQUEST_REDO(55),
  //        ANSWER_REDO(56),
  //        EXIT_AFTER_GAME(57),
  //        CANCEL_EXIT(58),
  //        READY(59),
  //        UN_READY(60),
  //        EXPEL(61),
  //        START(62),
  //        SKIP(64),
  //        MOVE_OMOK(65),
  //        SELECT_CARD(68);

  SET_ITEMS(0),
  SET_ITEMS2(1),
  SET_ITEMS3(2),
  SET_ITEMS4(3),
  UPDATE_MESO(4),
  SET_MESO2(5),
  SET_MESO3(6),
  SET_MESO4(7),
  CONFIRM_TRADE(8),
  CONFIRM_TRADE2(9),
  CONFIRM_TRADE_MESO(10),
  CONFIRM_TRADE_MESO2(11),
  CREATE(16),
  VISIT(19),
  INVITE_TRADE(21),
  DENY_TRADE(22),
  CHAT(24),
  EXIT(28),
  OPEN1(25),
  OPEN2(26),
  OPEN3(80),
  ADD_ITEM1(33),
  ADD_ITEM2(34),
  ADD_ITEM3(35),
  ADD_ITEM4(36),
  BUY_ITEM_HIREDMERCHANT(37),
  BUY_ITEM_STORE1(38),
  BUY_ITEM_STORE2(39),
  BUY_ITEM_STORE3(40),
  MAINTANCE_OFF(50),//41
  REMOVE_ITEM(49), //+10 
  //MERCHANT_EXIT(50), //+10 v192
  MAINTANCE_ORGANISE(51), //+10 v192
  CLOSE_MERCHANT(52), //+10 v192
  MESO_BACK(53), //+10 v192
  VIEW_MERCHANT_VISITOR(57),//55
  VIEW_MERCHANT_BLACKLIST(59),//56
  MERCHANT_BLACKLIST_ADD(60),//57
  MERCHANT_BLACKLIST_REMOVE(61), //+10 v192 //58
  ADMIN_STORE_NAMECHANGE(62),//59
  START_ROCK_PAPER_SCISSORS1(96),
  START_ROCK_PAPER_SCISSORS2(97),
  START_ROCK_PAPER_SCISSORS3(98),
  INVITE_ROCK_PAPER_SCISSORS(112),
  FINISH_ROCK_PAPER_SCISSORS(113),
  //            
  //            REQUEST_TIE(0x30),
  //            ANSWER_TIE(0x31),
  GIVE_UP(114),
  //            REQUEST_REDO(0x34),
  //            ANSWER_REDO(0x35),
  //            EXIT_AFTER_GAME(0x36),
  //            CANCEL_EXIT(0x37),
  //            READY(0x38),
  //            UN_READY(0x39),
  //            EXPEL(0x3A),
  START(125);
  public int action;

  private Interaction(int action) {
      this.action = action;
  }

  public static Interaction getByAction(int i) {
      for (Interaction s : Interaction.values()) {
          if (s.action == i) {
              return s;
          }
      }
      return null;
  }
 
}
