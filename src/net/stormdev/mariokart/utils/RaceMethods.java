package net.stormdev.mariokart.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.stormdev.mariokart.MarioKart;
import net.stormdev.mariokart.Race;
import net.stormdev.mariokart.User;

import org.bukkit.entity.Player;

public class RaceMethods {
	private MarioKart plugin = null;

	public RaceMethods() {
		this.plugin = MarioKart.plugin;
	}

	public Race inAGame(Player player) {
		HashMap<String, Race> games = plugin.gameScheduler.getGames();
		Set<String> keys = games.keySet();
		Boolean inAGame = false;
		Race mgame = null;
		for (String key : keys) {
			Race game = games.get(key);
			for (User user : game.getUsersIn()) {
				if (user.getPlayer().equals(player)) {
					inAGame = true;
					mgame = game;
				}
			}
		}
		if (inAGame) {
			return mgame;
		}
		return null;
	}

	public String inGameQue(Player player) {
		Set<String> arenaNames = Queues.getQues();
		for (String arenaName : arenaNames) {
			try {
				List<Player> que = Queues.getQue(arenaName)
						.getPlayers();
				if (que.contains(player)) {
					return arenaName;
				}
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
}
