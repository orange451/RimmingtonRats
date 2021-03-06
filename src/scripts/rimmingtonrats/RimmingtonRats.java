package scripts.rimmingtonrats;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.tribot.api2007.Camera;
import org.tribot.api2007.Combat;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSNPC;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;

import scripts.gui.RSGuiFrame;
import scripts.gui.font.ChatColor;
import scripts.gui.font.RSFont;
import scripts.util.AntiBan;
import scripts.util.names.Locations;
import scripts.util.player.Navigation;

@ScriptManifest(authors = { "orange451" }, category = "Combat", name = "Rimmington Rats", version = 1.00, description = "Kill rats in Rimmington!", gameMode = 1)
public class RimmingtonRats extends Script implements Painting {
	public static RimmingtonRats plugin;
	private RSNPC attacking = null;
	private long attackingTimeOut = System.currentTimeMillis();
	private Locations loc = Locations.RIMMINGTON_RATS;
	private long afkTime;

	private RSNPC[] getClosestNPCS() {
		ArrayList<RSNPC> ret = new ArrayList<RSNPC>();
		// Get all NPCs
		RSNPC[] npcs = NPCs.getAll();
		for (int i = 0; i < npcs.length; i++) {
			RSNPC npc = npcs[i];
			if ( npc != null && npc.getName() != null ) {
				ret.add(npcs[i]);
			}
		}
		Collections.sort(ret, new Comparator<RSNPC>() {

			@Override
			public int compare(RSNPC o1, RSNPC o2) {
				return o1.getPosition().distanceTo(Player.getPosition()) < o2.getPosition().distanceTo(Player.getPosition()) ? -1 : 1;
			}

		});

		RSNPC[] ret2 = new RSNPC[ret.size()];
		for (int i = 0; i < ret.size(); i++) {
			ret2[i] = ret.get(i);
		}

		return ret2;
	}

	@Override
	public void run() {
		plugin = this;

		// Run script
		while(true) {
			sleep( 50L );

			// Do not execute script if we're breaking
			if ( this.isOnBreak() ) {
				attacking = null;
				continue;
			}

			// Randomly go afk. Max time is 40 seconds.
			AntiBan.afk( 60000 );

			// Reset our target if it dies.
			if ( attacking != null && attacking.getHealth() <= 0 || ( System.currentTimeMillis() - attackingTimeOut > 1000 * 30 )) {
				attacking = null;
			}

			// If we're not under attack
			if ( !Combat.isUnderAttack() && attacking == null ) {

				// Walk to rimmington If we're not there already.
				if ( loc.contains(Player.getPosition()) ) {

					// Get all NPCs
					RSNPC[] npcs = getClosestNPCS();
					for (int i = 0; i < npcs.length; i++) {
						RSNPC npc = npcs[i];

						// If it's a Rat
						if ( npc != null && npc.getName() != null && npc.getName().equals("Rat")) {

							// If no one else is attacking it
							if ( !npc.isInCombat() ) {

								// Attack it
								if ( npc.click("Attack") ) {
									attackingTimeOut = System.currentTimeMillis();
									attacking = npc;

									// Sometimes rotate to rat.
									if ( (int)(Math.random() * 4) == 2 )
										Camera.turnToTile(attacking.getPosition());

									// Sleep a little bit while we attack
									sleep( 500 + (int)(Math.random() * 3000) );

									break;
								} else {
									// Keep trying to click it if we failed.
									sleep( 200 + (int)(Math.random() * 400) );
								}
							}
						}
					}

					// Try to do some antiban stuff while we're looking for a rat
					AntiBan.timedActions();

					// Sometimes rotate camera
					if ( (int)(Math.random() * 4) == 2 )
						Camera.turnToTile(loc.getCenter());
					sleep( 500 + (int)(Math.random() * 2000) );
				} else {
					Navigation.walkTo(loc, true);
				}
			} else {
				AntiBan.timedActions();
			}
		}
	}

	@Override
	public void onPaint(Graphics arg0) {
		RSFont font = RSGuiFrame.FONT_BOLD;
		font.drawStringShadow( arg0, "Is under attack: " + (Combat.isUnderAttack()?ChatColor.RED+"true":ChatColor.GREEN+"false"), 9, 36);
		font.drawStringShadow( arg0, "Attacking: " + (attacking == null?ChatColor.GRAY+"null":ChatColor.YELLOW+attacking.getName()), 9, 52);
		font.drawStringShadow( arg0, "AFK: " + (afkTime - System.currentTimeMillis() > 2000?ChatColor.RED+"true":ChatColor.GREEN+"false"), 9, 68);
	}
}
