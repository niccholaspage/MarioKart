package net.stormdev.mariokartAddons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Powerup {
	RED_SHELL(Arrays.asList("+Slows down the victim", "*Right click to deploy")),
	GREEN_SHELL(Arrays.asList("+Slows down the victim", "*Left click to throw forwards", "*Right click to throw backwards")),
	BLUE_SHELL(Arrays.asList("+Targets and slows the leader", "*Right click to deploy")),
	BANANA(Arrays.asList("+Slows players down", "*Right click to deploy")),
	STAR(Arrays.asList("+Applies a large speed boost", "+Immunity to other powerups", "*Right click to use")),
	LIGHTNING(Arrays.asList("+Strikes all lightning on enemies", "*Right click to deploy")),
	BOMB(Arrays.asList("+Throws an ignited bomb", "*Right click to deploy")),
	POW(Arrays.asList("+Freezes other players", "*Right click to deploy")),
	MUSHROOM(Arrays.asList("+Applies a short speed boost", "*Right click to use")),
	BOO(Arrays.asList("+Invisible for 6s", "+Apply nausea to racer ahead", "*Right click to deploy")),
	RANDOM(Arrays.asList("+Gives a random powerup", "*Right click to use"));
	public List<String> lore = new ArrayList<String>();
	Powerup(List<String> lore) {
		this.lore = lore;
	}
}
