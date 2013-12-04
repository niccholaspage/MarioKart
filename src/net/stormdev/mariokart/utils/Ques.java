package net.stormdev.mariokart.utils;

import java.util.Set;

import net.stormdev.mariokart.MarioKart;

public class Ques {
	MarioKart plugin = null;

	public Ques(MarioKart plugin) {
		this.plugin = plugin;
	}

	public void setQue(String name, RaceQue toAdd) {
		plugin.ques.put(name, toAdd);
		return;
	}

	public void removeQue(String name) {
		Set<String> keys = getQues();
		for (String key : keys) {
			if (key.equalsIgnoreCase(name)) {
				name = key;
			}
		}
		plugin.ques.remove(name);
		return;
	}

	public RaceQue getQue(String name) {
		return plugin.ques.get(name);
	}

	public Boolean queExists(String name) {
		Set<String> keys = getQues();
		for (String key : keys) {
			if (key.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public Set<String> getQues() {
		return plugin.ques.keySet();
	}
}
