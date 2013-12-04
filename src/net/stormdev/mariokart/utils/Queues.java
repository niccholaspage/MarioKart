package net.stormdev.mariokart.utils;

import java.util.HashMap;
import java.util.Set;

public class Queues
{
	public static HashMap<String, RaceQue> queues = new HashMap<String, RaceQue>();
	
	public static void setQue(String name, RaceQue toAdd) {
		queues.put(name, toAdd);
	}

	public static void removeQue(String name) {
		queues.remove(name);
	}

	public static RaceQue getQue(String name) {
		return queues.get(name);
	}

	public static Boolean queExists(String name) {
		return queues.keySet().contains(name);
	}

	public static Set<String> getQues() {
		return queues.keySet();
	}
}
