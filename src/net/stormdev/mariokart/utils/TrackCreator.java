package net.stormdev.mariokart.utils;

import java.util.regex.Pattern;

import net.stormdev.mariokart.MarioKart;

import org.bukkit.entity.Player;

public class TrackCreator {
	Player player = null;
	RaceTrack track = null;
	int stage = 0;
	Boolean complete = false;

	public TrackCreator(Player player, RaceTrack track) {
		this.player = player;
		this.track = track;
		stage = 0;
		MarioKart.trackCreators.put(player.getName(), this);
		start();
	}

	public void start() {
		String msg = MarioKart.msgs.get("setup.create.lobby");
		player.sendMessage(MarioKart.colors.getInfo() + msg);
		return;
	}

	public void set(Boolean wand) {
		if (complete) {
			saveTrack();
			return;
		}
		if (wand && stage == 0) {
			setLobby();
		} else if (wand && stage == 1) {
			setExit();
		} else if (!wand && stage == 2) {
			addGrid();
		} else if (wand && stage == 2) {
			finishGrid();
		} else if (!wand && stage == 3) {
			addCheckpoint();
		} else if (wand && stage == 3) {
			finishCheckpoints();
		} else if (wand && stage == 4) {
			setLine1();
		} else if (wand && stage == 5) {
			setLine2();
		}
		MarioKart.trackCreators.put(player.getName(), this);
		return;
	}

	public void setLobby() {
		track.setLobby(player.getLocation());
		String msg = MarioKart.msgs.get("setup.create.exit");
		player.sendMessage(MarioKart.colors.getInfo() + msg);
		stage = 1;
		return;
	}

	public void setExit() {
		track.setExit(player.getLocation());
		player.sendMessage(MarioKart.colors.getInfo()
				+ MarioKart.msgs.get("setup.create.grid"));
		stage = 2;
		return;
	}

	public void addGrid() {
		track.addToStartGrid(player.getLocation());
		player.sendMessage(MarioKart.colors.getInfo() + "["
				+ track.getStartGrid().size() + "]");
		return;
	}

	public void finishGrid() {
		player.sendMessage(MarioKart.colors.getInfo()
				+ MarioKart.msgs.get("setup.create.checkpoints"));
		stage = 3;
		return;
	}

	public void addCheckpoint() {
		int pos = track.getCheckpoints().size();
		track.addToCheckpoints(pos, player.getLocation());
		player.sendMessage(MarioKart.colors.getInfo() + "["
				+ track.getCheckpoints().size() + "]");
		return;
	}

	public void finishCheckpoints() {
		int amount = track.getCheckpoints().size();
		if (amount < 3) {
			String msg = MarioKart.msgs.get("setup.create.notEnoughCheckpoints");
			msg = msg.replaceAll(Pattern.quote("%num%"), amount + "");
			player.sendMessage(MarioKart.colors.getError() + msg);
			return;
		}
		player.sendMessage(MarioKart.colors.getInfo()
				+ MarioKart.msgs.get("setup.create.line1"));
		stage = 4;
		return;
	}

	public void setLine1() {
		track.setLine1(player.getLocation());
		player.sendMessage(MarioKart.colors.getInfo()
				+ MarioKart.msgs.get("setup.create.line2"));
		stage = 5;
		return;
	}

	public void setLine2() {
		track.setLine2(player.getLocation());
		stage = 6;
		saveTrack();
		return;
	}

	public void saveTrack() {
		complete = true;
		MarioKart.plugin.trackManager.setRaceTrack(track.getTrackName(), track);
		String msg = MarioKart.msgs.get("setup.create.done");
		msg = msg.replaceAll(Pattern.quote("%name%"), track.getTrackName());
		player.sendMessage(MarioKart.colors.getSuccess() + msg);
		MarioKart.trackCreators.remove(player.getName());
		return;
	}
}
