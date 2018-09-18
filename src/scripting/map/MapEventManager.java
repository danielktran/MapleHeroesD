package scripting.map;

import client.MapleClient;
import net.packet.CField.UIPacket;
import scripting.AbstractPlayerInteraction;

public class MapEventManager extends AbstractPlayerInteraction {

	
	public MapEventManager(MapleClient c) {
		super(c, c.getCharacter().getMapId(), 0, null);
	}
	
	public void enableDirectionStatus() {
		c.getSession().write(UIPacket.getDirectionStatus(true));
	}
	
	public void introEnableUI(boolean enable) {
		c.getSession().write(UIPacket.IntroEnableUI(enable ? 1 : 0));
	}
	
	public void sleep(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			
		}
	}
	
	/**
	 * Temporarily pauses threads to delay map events for a {@code delay} of milliseconds.
	 * @param delay the delay in milliseconds to pause threads
	 */
	public void delayEvent(int delay) {
		c.getSession().write(UIPacket.delayDirectionInfo(delay));
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			
		}
	}
	
	/**
	 * Temporarily pauses threads to delay map events for a {@code delay} of milliseconds with an additional {@code offset} delay.
	 * @param delay the delay in milliseconds to pause threads
	 * @param offset the additional delay in milliseconds
	 */
	public void delayEvent(int delay, int offset) {
		c.getSession().write(UIPacket.delayDirectionInfo(delay));
		try {
			Thread.sleep(delay + offset);
		} catch (InterruptedException e) {
			
		}
	}
	
	/**
	 * Forces the character to move in a certain direction during map events.
	 * @param input
	 */
	public void forceMoveCharacter(int input) {
		c.getSession().write(UIPacket.forceMoveCharacter(input));
	}
	
	
	public void showDirectionEffect(String data, int value, int x, int y) {
		c.getSession().write(UIPacket.getDirectionInfo(data, value, x, y, 0, 0));
	}
	
	public void showDirectionEffect(String data, int value, int x, int y, int a, int b) {
		c.getSession().write(UIPacket.getDirectionInfo(data, value, x, y, a, b));
	}
	
	

}
