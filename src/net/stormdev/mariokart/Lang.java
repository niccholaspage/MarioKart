package net.stormdev.mariokart;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;

public class Lang {
	MarioKart plugin = null;
	FileConfiguration lang = null;
	FileConfiguration config = null;
	public Lang(MarioKart main) {
		this.plugin = main;
        File langFile = new File(MarioKart.plugin.getDataFolder() + File.separator + "lang.yml");
        if (langFile.exists() == false || langFile.length() < 1) {
            try {
                langFile.createNewFile();
            }
            catch (IOException e) {
            }
        }
        try {
            MarioKart.lang.load(langFile);
        }
        catch (Exception e1) {
            MarioKart.plugin.getLogger().log(Level.WARNING,
            "Error creating/loading lang file! Regenerating..");
        }
        lang = MarioKart.lang;
        config = MarioKart.config;
        setup();
        MarioKart.plugin.saveConfig();
	}
	public void setup()
	{
        // Setup the Lang file
        if (!lang.contains("general.cmd.leave.success")) {
            lang.set("general.cmd.leave.success",
            "Successfully left %name%!");
        }
        if (!lang.contains("general.cmd.page")) {
            lang.set("general.cmd.page", "Page [%page%/%total%]:");
        }
        if (!lang.contains("general.cmd.full")) {
            lang.set("general.cmd.full",
            "There are no race tracks available!");
        }
        if (!lang.contains("general.cmd.playersOnly")) {
            lang.set("general.cmd.playersOnly",
            "This command is for players only!");
        }
        if (!lang.contains("general.cmd.leave.fail")) {
            lang.set("general.cmd.leave.fail", "You aren't in a game/que!");
        }
        if (!lang.contains("general.cmd.setlaps.success")) {
            lang.set("general.cmd.setlaps.success",
            "Successfully set laps for track %name%!");
        }
        if (!lang.contains("general.cmd.delete.success")) {
            lang.set("general.cmd.delete.success",
            "Successfully deleted track %name%!");
        }
        if (!lang.contains("general.cmd.delete.exists")) {
            lang.set("general.cmd.delete.exists",
            "That track doesn't exist!");
        }
        if (!lang.contains("general.cmd.racetimes")) {
            lang.set("general.cmd.racetimes",
            "Top %n% times for track %track%:");
        }
        if (!lang.contains("setup.create.exists")) {
            lang.set("setup.create.exists",
            "This track already exists! Please do /urace delete %name% before proceeding!");
        }
        if (!lang.contains("setup.create.start")) {
            lang.set("setup.create.start", "Wand: %id% (%name%)");
        }
        if (!lang.contains("setup.create.lobby")) {
            lang.set("setup.create.lobby",
            "Stand in the lobby and right click anywhere with the wand");
        }
        if (!lang.contains("setup.create.exit")) {
            lang.set("setup.create.exit",
            "Stand at the track exit and right click anywhere with the wand");
        }
        if (!lang.contains("setup.create.grid")) {
            lang.set(
            "setup.create.grid",
            "Stand where you want a car to start the race and right click anywhere (Without the wand). Repeat for all the starting positions. When done, right click anywhere with the wand");
        }
        if (!lang.contains("setup.create.checkpoints")) {
            lang.set(
            "setup.create.checkpoints",
            "Stand at each checkpoint along the track (Checkpoint 10x10 radius) and right click anywhere (Without the wand). Repeat for all checkpoints. When done, right click anywhere with the wand");
        }
        if (!lang.contains("setup.create.notEnoughCheckpoints")) {
            lang.set("setup.create.notEnoughCheckpoints",
            "You must have at least 3 checkpoints! You only have: %num%");
        }
        if (!lang.contains("setup.create.line1")) {
            lang.set(
            "setup.create.line1",
            "Stand at one end of the start/finish line and right click anywhere with the wand");
        }
        if (!lang.contains("setup.create.line2")) {
            lang.set(
            "setup.create.line2",
            "Stand at the other end of the start/finish line and right click anywhere with the wand");
        }
        if (!lang.contains("setup.create.done")) {
            lang.set("setup.create.done",
            "Successfully created Race Track %name%!");
        }
        if (!lang.contains("race.que.existing")) {
            lang.set("race.que.existing",
            "You are already in a game/que! Please leave it before joining this one!");
        }
        if (!lang.contains("race.que.other")) {
            lang.set("race.que.other",
            "Unavailable! Current queue race type: %type%");
        }
        if (!lang.contains("race.que.full")) {
            lang.set("race.que.full", "Race que full!");
        }
        if (!lang.contains("race.que.success")) {
            lang.set("race.que.success", "In Race Que!");
        }
        if (!lang.contains("race.que.joined")) {
            lang.set("race.que.joined", " joined the race que!");
        }
        if (!lang.contains("race.que.left")) {
            lang.set("race.que.left", " left the race que!");
        }
        if (!lang.contains("race.que.players")) {
            lang.set(
            "race.que.players",
            "Acquired minimum players for race! Waiting %time% seconds for additional players to join...");
        }
        if (!lang.contains("race.que.preparing")) {
            lang.set("race.que.preparing", "Preparing race...");
        }
        if (!lang.contains("race.que.starting")) {
            lang.set("race.que.starting", "Race starting in...");
        }
        if (!lang.contains("resource.download")) {
            lang.set("resource.download", "Downloading resources...");
        }
        if (!lang.contains("resource.downloadHelp")) {
            lang.set("resource.downloadHelp",
            "If the resources aren't downloaded automatically. Download it at: ");
        }
        if (!lang.contains("resource.clear")) {
            lang.set("resource.clear",
            "Switching back to default minecraft textures...");
        }
        if (!lang.contains("race.que.go")) {
            lang.set("race.que.go", "Go!");
        }
        if (!lang.contains("race.end.won")) {
            lang.set("race.end.won", " won the race!");
        }
        if (!lang.contains("race.end.time")) {
            lang.set("race.end.time", "Your time was %time% seconds!");
        }
        if (!lang.contains("race.mid.miss")) {
            lang.set("race.mid.miss",
            "You missed a section of the track! Please go back and do it!");
        }
        if (!lang.contains("race.mid.backwards")) {
            lang.set("race.mid.backwards", "You are going the wrong way!");
        }
        if (!lang.contains("race.mid.lap")) {
            lang.set("race.mid.lap", "Lap [%lap%/%total%]");
        }
        if (!lang.contains("race.end.soon")) {
            lang.set("race.end.soon",
            "You have 1 minute before the race ends!");
        }
        if (!lang.contains("race.end.position")) {
            lang.set("race.end.position", "You finished %position%!");
        }
        if (!lang.contains("mario.hit")) {
            lang.set("mario.hit", "You were hit by a %name%!");
        }
        // Setup the config
        if (!config.contains("setup.create.wand")) {
            config.set("setup.create.wand", 280);
        }
        if (!config.contains("general.logger.colour")) {
            config.set("general.logger.colour", true);
        }
        if (!config.contains("general.raceLimit")) {
            config.set("general.raceLimit", 10);
        }
        if (!config.contains("general.raceTickrate")) {
            config.set("general.raceTickrate", 2l);
        }
        if (!config.contains("general.raceGracePeriod")) {
            config.set("general.raceGracePeriod", (double) 10.0);
        }
        if (!config.contains("general.race.timed.log")) {
            config.set("general.race.timed.log", true);
        }
        if (!config.contains("race.que.minPlayers")) {
            config.set("race.que.minPlayers", 2);
        }
        if (!config.contains("bitlyUrlShortner")) {
            config.set("bitlyUrlShortner", true);
        }
        if (!config.contains("mariokart.resourcePack")) {
            config.set(
            "mariokart.resourcePack",
            "https://dl.dropboxusercontent.com/u/147363358/MarioKart/Resource/MarioKart-latest.zip");
        }
        if (!config.contains("mariokart.resourceNonMarioPack")) {
            config.set("mariokart.resourceNonMarioPack",
            "https://dl.dropboxusercontent.com/u/147363358/MarioKart/Resource/defaults.zip");
        }
        if (!config.contains("mariokart.enable")) {
            config.set("mariokart.enable", true);
        }
        if (!config.contains("mariokart.redShell")) {
            config.set("mariokart.redShell", "351:1");
        }
        if (!config.contains("mariokart.greenShell")) {
            config.set("mariokart.greenShell", "351:2");
        }
        if (!config.contains("mariokart.blueShell")) {
            config.set("mariokart.blueShell", "351:12");
        }
        if (!config.contains("mariokart.banana")) {
            config.set("mariokart.banana", "351:11");
        }
        if (!config.contains("mariokart.star")) {
            config.set("mariokart.star", "399");
        }
        if (!config.contains("mariokart.lightning")) {
            config.set("mariokart.lightning", "351:7");
        }
        if (!config.contains("mariokart.bomb")) {
            config.set("mariokart.bomb", "46");
        }
        if (!config.contains("mariokart.boo")) {
            config.set("mariokart.boo", "352");
        }
        if (!config.contains("mariokart.pow")) {
            config.set("mariokart.pow", "79");
        }
        if (!config.contains("mariokart.random")) {
            config.set("mariokart.random", "159:4");
        }
        if (!config.contains("mariokart.mushroom")) {
            config.set("mariokart.mushroom", "40");
        }
        // Setup the colour scheme
        if (!config.contains("colorScheme.success")) {
            config.set("colorScheme.success", "&c");
        }
        if (!config.contains("colorScheme.error")) {
            config.set("colorScheme.error", "&7");
        }
        if (!config.contains("colorScheme.info")) {
            config.set("colorScheme.info", "&6");
        }
        if (!config.contains("colorScheme.title")) {
            config.set("colorScheme.title", "&4");
        }
        if (!config.contains("colorScheme.tp")) {
            config.set("colorScheme.tp", "&1");
        }
	}
	public String get(String key) {
		String val = getRaw(key);
		val = MarioKart.colorise(val);
		return val;
	}

	public String getRaw(String key) {
		if (!MarioKart.lang.contains(key)) {
			return key;
		}
		return MarioKart.lang.getString(key);
	}
}
