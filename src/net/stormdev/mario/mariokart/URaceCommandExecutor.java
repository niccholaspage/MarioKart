package net.stormdev.mario.mariokart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.regex.Pattern;

import net.stormdev.mario.utils.RaceQue;
import net.stormdev.mario.utils.RaceTrack;
import net.stormdev.mario.utils.RaceType;
import net.stormdev.mario.utils.TrackCreator;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class URaceCommandExecutor implements CommandExecutor {
	MarioKart plugin = null;
	
	public URaceCommandExecutor(MarioKart plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (cmd.getName().equalsIgnoreCase("marioRaceAdmin")) {
			if (args.length < 1) {
				return false;
			}
			String command = args[0];
			if (command.equalsIgnoreCase("create")) {
				// /urace create [TrackName]
				if (player == null) {
					sender.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("general.cmd.playersOnly"));
					return true;
				}
				if (args.length < 3) {
					return false;
				}
				String trackname = args[1];
				int laps = 3;
				try {
					laps = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					return false;
				}
				if (laps < 1) {
					laps = 1;
				}
				if (plugin.trackManager.raceTrackExists(trackname)) {
					String msg = MarioKart.msgs.get("setup.create.exists");
					msg = msg.replaceAll(Pattern.quote("%name%"), trackname);
					sender.sendMessage(MarioKart.colors.getError() + msg);
					return true;
				}
				int id = MarioKart.config.getInt("setup.create.wand");
				ItemStack named = new ItemStack(Material.matchMaterial(Integer.toString(id))); //Should Work
				String start = MarioKart.msgs.get("setup.create.start");
				start = start.replaceAll(Pattern.quote("%id%"), "" + id);
				start = start.replaceAll(Pattern.quote("%name%"), named
						.getType().name().toLowerCase());
				sender.sendMessage(MarioKart.colors.getInfo() + start);
				RaceTrack track = new RaceTrack(trackname, 2, 2, laps);
				new TrackCreator(player, track); // Create the track
				return true;
			} else if (command.equalsIgnoreCase("delete")) {
				if (args.length < 2) {
					return false;
				}
				String trackname = args[1];
				if (!plugin.trackManager.raceTrackExists(trackname)) {
					sender.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				plugin.trackManager.deleteRaceTrack(trackname);
				String msg = MarioKart.msgs.get("general.cmd.delete.success");
				msg = msg.replaceAll("%name%", trackname);
				sender.sendMessage(MarioKart.colors.getSuccess() + msg);
				return true;
			} else if (command.equalsIgnoreCase("list")) {
				int page = 1;
				if (args.length > 1) {
					try {
						page = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						page = 1;
					}
				}
				@SuppressWarnings("unchecked")
				ArrayList<RaceTrack> tracks = (ArrayList<RaceTrack>) plugin.trackManager
						.getRaceTracks().clone();
				ArrayList<String> names = new ArrayList<String>();
				for (RaceTrack track : tracks) {
					names.add(track.getTrackName());
				}
				double total = names.size() / 6;
				int totalpages = (int) Math.ceil(total);
				int pos = (page - 1) * 6;
				if (page > totalpages) {
					page = totalpages;
				}
				if (pos > names.size()) {
					pos = names.size() - 5;
				}
				if (pos < 0) {
					pos = 0;
				}
				if (page < 0) {
					page = 0;
				}
				String msg = MarioKart.msgs.get("general.cmd.page");
				msg = msg.replaceAll(Pattern.quote("%page%"), "" + (page + 1));
				msg = msg.replaceAll(Pattern.quote("%total%"), ""
						+ (totalpages + 1));
				sender.sendMessage(MarioKart.colors.getTitle() + msg);
				for (int i = pos; i < (i + 6) && i < names.size(); i++) {
					String Trackname = names.get(i);
					char[] chars = Trackname.toCharArray();
					if (chars.length >= 1) {
						String s = "" + chars[0];
						s = s.toUpperCase();
						Trackname = s + Trackname.substring(1);
					}
					sender.sendMessage(MarioKart.colors.getInfo() + Trackname);
				}
				return true;
			} else if (command.equalsIgnoreCase("setLaps")) {
				if (args.length < 3) {
					return false;
				}
				String trackname = args[1];
				if (!plugin.trackManager.raceTrackExists(trackname)) {
					sender.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				String lapsStr = args[2];
				int laps = 3;
				try {
					laps = Integer.parseInt(lapsStr);
				} catch (NumberFormatException e) {
					return false;
				}
				plugin.trackManager.getRaceTrack(trackname).laps = laps;
				plugin.trackManager.save();
				String msg = MarioKart.msgs.get("general.cmd.setlaps.success");
				msg = msg.replaceAll(Pattern.quote("%name%"),
						plugin.trackManager.getRaceTrack(trackname)
								.getTrackName());
				sender.sendMessage(MarioKart.colors.getSuccess() + msg);
				return true;
			}
			return false;
		} else if (cmd.getName().equalsIgnoreCase("race")) {
			return urace(sender, args, player);
		} else if (cmd.getName().equalsIgnoreCase("racetimes")) {
			if (args.length < 2) {
				return false;
			}
			String trackName = args[0];
			String amount = args[1];
			@SuppressWarnings("unchecked")
			List<String> names = (List<String>) plugin.trackManager
					.getRaceTrackNames().clone();
			for (String n : names) {
				if (n.equalsIgnoreCase(trackName)) {
					trackName = n;
				}
			}
			double d = 5;
			try {
				d = Double.parseDouble(amount);
			} catch (NumberFormatException e) {
				return false;
			}
			SortedMap<String, Double> topTimes = plugin.raceTimes.getTopTimes(
					d, trackName);
			Map<String, Double> times = plugin.raceTimes.getTimes(trackName);
			String msg = MarioKart.msgs.get("general.cmd.racetimes");
			msg = msg.replaceAll(Pattern.quote("%n%"), d + "");
			msg = msg.replaceAll(Pattern.quote("%track%"), trackName);
			sender.sendMessage(MarioKart.colors.getTitle() + msg);
			Object[] keys = topTimes.keySet().toArray();
			int pos = 1;
			for (Object o : keys) {
				String name = (String) o;
				sender.sendMessage(MarioKart.colors.getTitle() + pos + ")"
						+ MarioKart.colors.getInfo() + name + "- " + times.get(name)
						+ "s");
				pos++;
			}
			return true;
		}
		return false;
	}

	public Boolean urace(CommandSender sender, String[] args, Player player) {
		if (args.length < 1) {
			return false;
		}
		String command = args[0];
		if (command.equalsIgnoreCase("list")) {
			int page = 1;
			if (args.length > 1) {
				try {
					page = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					page = 1;
				}
			}
			@SuppressWarnings("unchecked")
			ArrayList<RaceTrack> tracks = (ArrayList<RaceTrack>) plugin.trackManager
					.getRaceTracks().clone();
			ArrayList<String> names = new ArrayList<String>();
			for (RaceTrack track : tracks) {
				names.add(track.getTrackName());
			}
			double total = names.size() / 6;
			int totalpages = (int) Math.ceil(total);
			int pos = (page - 1) * 6;
			if (page > totalpages) {
				page = totalpages;
			}
			if (pos > names.size()) {
				pos = names.size() - 5;
			}
			if (pos < 0) {
				pos = 0;
			}
			if (page < 0) {
				page = 0;
			}
			String msg = MarioKart.msgs.get("general.cmd.page");
			msg = msg.replaceAll(Pattern.quote("%page%"), "" + (page + 1));
			msg = msg.replaceAll(Pattern.quote("%total%"), ""
					+ (totalpages + 1));
			sender.sendMessage(MarioKart.colors.getTitle() + msg);
			for (int i = pos; i < (i + 6) && i < names.size(); i++) {
				String Trackname = names.get(i);
				char[] chars = Trackname.toCharArray();
				if (chars.length >= 1) {
					String s = "" + chars[0];
					s = s.toUpperCase();
					Trackname = s + Trackname.substring(1);
				}
				sender.sendMessage(MarioKart.colors.getInfo() + Trackname);
			}
			return true;
		} else if (command.equalsIgnoreCase("join")) {
			if (player == null) {
				sender.sendMessage(MarioKart.colors.getError()
						+ MarioKart.msgs.get("general.cmd.playersOnly"));
				return true;
			}
			String trackName = null;
			if (args.length < 2) {
				trackName = "auto";
			}
			trackName = args[1];
			RaceType type = RaceType.RACE;
			// /race join test cup
			if (args.length > 2) {
				String t = args[2];
				if (t.equalsIgnoreCase("race")) {
					type = RaceType.RACE;
				} else if (t.equalsIgnoreCase("timed")
						|| t.equalsIgnoreCase("time")
						|| t.equalsIgnoreCase("time_trial")
						|| t.equalsIgnoreCase("time-trial")) {
					type = RaceType.TIME_TRIAL;
				} else if (t.equalsIgnoreCase("cup")
						|| t.equalsIgnoreCase("championship")
						|| t.equalsIgnoreCase("grand")
						|| t.equalsIgnoreCase("grand-prix")
						|| t.equalsIgnoreCase("grand_prix")) {
					type = RaceType.GRAND_PRIX;
				}
			}
			if (trackName.equalsIgnoreCase("auto")) {
				if (MarioKart.plugin.raceMethods.inAGame(player) != null
						|| MarioKart.plugin.raceMethods.inGameQue(player) != null) {
					sender.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("race.que.existing"));
					return true;
				}
				List<String> gameArenas = new ArrayList<String>();
				List<String> order = new ArrayList<String>();
				int waitingPlayers = 0;
				for (String aname : plugin.raceQues.getQues()) {
					RaceQue arena = plugin.raceQues.getQue(aname);
					if (arena.getHowManyPlayers() < arena.getPlayerLimit()) {
						gameArenas.add(aname);
						if (arena.getHowManyPlayers() > waitingPlayers) {
							waitingPlayers = arena.getHowManyPlayers();
						}
					}
				}
				int waitNo = 1;
				List<String> remaining = new ArrayList<String>();
				remaining.addAll(gameArenas);
				for (int i = waitNo; i <= waitingPlayers; i++) {
					for (String aname : gameArenas) {
						RaceQue arena = plugin.raceQues.getQue(aname);
						if (arena.getHowManyPlayers() == waitNo) {
							order.add(aname);
							if (remaining.contains(aname)) {
								remaining.remove(aname);
							}
						}
					}
				}
				for (String aname : remaining) {
					order.add(aname);
				}
				if (order.size() < 1) {
					// Create a random raceQue
					int min = 0;
					int max = MarioKart.plugin.trackManager.getRaceTracks().size() - 1;
					if (MarioKart.plugin.trackManager.getRaceTracks().size() < 1) {
						// No tracks created
						sender.sendMessage(MarioKart.colors.getError()
								+ MarioKart.msgs.get("general.cmd.full"));
						return true;
					}
					int randomNumber;
					try {
						randomNumber = new Random().nextInt(max - min) //TODO: Not efficient
								+ min;
					} catch (Exception e) {
						randomNumber = 0;
					}
					RaceTrack track = MarioKart.plugin.trackManager.getRaceTracks()
							.get(randomNumber);
					RaceQue que = new RaceQue(track, type);
					plugin.gameScheduler.joinGame(player, track, que,
							track.getTrackName());
					return true;
				}
				String name = order.get(0);
				RaceQue arena = plugin.raceQues.getQue(name);
				if (arena.getHowManyPlayers() < 1
						|| arena.getType() == RaceType.TIME_TRIAL) {
					int rand = 0 + (int) (Math.random() * ((order.size() - 0) + 0));
					name = order.get(rand);
					arena = plugin.raceQues.getQue(name);
				}
				RaceTrack track = plugin.trackManager.getRaceTrack(name);
				if (track == null) {
					sender.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				if (player.getVehicle() != null) {
					Vehicle veh = (Vehicle) player.getVehicle();
					veh.eject();
					veh.remove();
				}
				plugin.gameScheduler.joinGame(player, track, arena, name);
				return true;
			} else {
				RaceTrack track = plugin.trackManager.getRaceTrack(trackName);
				if (track == null) {
					sender.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("general.cmd.delete.exists"));
					return true;
				}
				RaceQue que = new RaceQue(track, type);
				trackName = track.getTrackName();
				if (MarioKart.plugin.raceQues.getQue(trackName) != null) {
					que = MarioKart.plugin.raceQues.getQue(trackName);
				}
				if (que.getType() != type) {
					if (que.getHowManyPlayers() < 1) {
						plugin.raceQues
								.removeQue(que.getTrack().getTrackName());
						que = new RaceQue(track, type);
					} else {
						// Another queue for different RameType
						String msg = MarioKart.msgs.get("race.que.other");
						msg = msg.replaceAll(Pattern.quote("%type%"), que
								.getType().name().toLowerCase());
						sender.sendMessage(MarioKart.colors.getError() + msg);
						return true;
					}
				}
				if (MarioKart.plugin.raceMethods.inAGame(player) != null
						|| MarioKart.plugin.raceMethods.inGameQue(player) != null) {
					sender.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("race.que.existing"));
					return true;
				}
				if (player.getVehicle() != null) {
					Vehicle veh = (Vehicle) player.getVehicle();
					veh.eject();
					veh.remove();
				}
				MarioKart.plugin.gameScheduler.joinGame(player, track, que,
						trackName);
				return true;
			}
		} else if (command.equalsIgnoreCase("queues")
				|| command.equalsIgnoreCase("ques")) {
			int page = 1;
			if (args.length > 1) {
				try {
					page = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					page = 1;
				}
			}
			ArrayList<String> names = new ArrayList<String>();
			names.addAll(plugin.raceQues.getQues());
			double total = names.size() / 6;
			int totalpages = (int) Math.ceil(total);
			int pos = (page - 1) * 6;
			if (page > totalpages) {
				page = totalpages;
			}
			if (pos > names.size()) {
				pos = names.size() - 5;
			}
			if (pos < 0) {
				pos = 0;
			}
			if (page < 0) {
				page = 0;
			}
			String msg = MarioKart.msgs.get("general.cmd.page");
			msg = msg.replaceAll(Pattern.quote("%page%"), "" + (page + 1));
			msg = msg.replaceAll(Pattern.quote("%total%"), ""
					+ (totalpages + 1));
			sender.sendMessage(MarioKart.colors.getTitle() + msg);
			for (int i = pos; i < (i + 6) && i < names.size(); i++) {
				String Trackname = names.get(i);
				RaceQue que = plugin.raceQues.getQue(Trackname);
				ChatColor color = ChatColor.GREEN;
				if (que.getHowManyPlayers() > (que.getPlayerLimit() - 1)) {
					color = ChatColor.RED;
				}
				if (que.getHowManyPlayers() > (que.getPlayerLimit() - 2)) {
					color = ChatColor.YELLOW;
				}
				if (que.getHowManyPlayers() < MarioKart.config
						.getInt("race.que.minPlayers")) {
					color = ChatColor.YELLOW;
				}
				char[] chars = Trackname.toCharArray();
				if (chars.length >= 1) {
					String s = "" + chars[0];
					s = s.toUpperCase();
					Trackname = color + s + Trackname.substring(1)
							+ MarioKart.colors.getInfo() + " (" + color
							+ que.getHowManyPlayers() + MarioKart.colors.getInfo()
							+ "/" + que.getPlayerLimit() + ")";
				}
				sender.sendMessage(MarioKart.colors.getInfo() + Trackname);
			}
			return true;
		} else if (command.equalsIgnoreCase("leave")) {
			if (player == null) {
				sender.sendMessage(MarioKart.colors.getError()
						+ MarioKart.msgs.get("general.cmd.playersOnly"));
				return true;
			}
			Boolean game = true;
			Race race = plugin.raceMethods.inAGame(player);
			String que = plugin.raceMethods.inGameQue(player);
			if (race == null) {
				game = false;
			}
			if (que == null) {
				if (!game) {
					sender.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("general.cmd.leave.fail"));
					return true;
				}
			}
			if (game) {
				race.leave(race.getUser(player), true);
			} else {
				RaceQue queue = plugin.raceQues.getQue(que);
				plugin.gameScheduler.leaveQue(player, queue, queue.getTrack()
						.getTrackName());
				String msg = MarioKart.msgs.get("general.cmd.leave.success");
				msg = msg.replaceAll(Pattern.quote("%name%"), que);
				sender.sendMessage(MarioKart.colors.getSuccess() + msg);
				player.teleport(queue.getTrack().getExit(plugin.getServer()));
			}
			return true;
		}
		return false;
	}
}
