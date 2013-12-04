package net.stormdev.mariokart.utils;

import net.stormdev.mariokart.Race;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RaceUpdateEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Race race = null;

	public RaceUpdateEvent(Race race) {
		this.race = race;
	}

	public Race getRace() {
		return this.race;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
