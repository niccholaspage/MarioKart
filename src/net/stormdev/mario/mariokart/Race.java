package net.stormdev.mario.mariokart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

import net.stormdev.mario.utils.CheckpointCheck;
import net.stormdev.mario.utils.DoubleValueComparator;
import net.stormdev.mario.utils.RaceEndEvent;
import net.stormdev.mario.utils.RaceFinishEvent;
import net.stormdev.mario.utils.RaceStartEvent;
import net.stormdev.mario.utils.RaceTrack;
import net.stormdev.mario.utils.RaceType;
import net.stormdev.mario.utils.RaceUpdateEvent;
import net.stormdev.mario.utils.SerializableLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.useful.ucarsCommon.StatValue;

public class Race {
	public List<User> users = new ArrayList<User>();
	private String gameId = "";
	private RaceTrack track = null;
	private String trackName = "";
	private User winner = null;
	public String winning = "";
	public Boolean running = false;
	public long startTimeMS = 0;
	public long endTimeMS = 0;
	public long tickrate = 6;
	public long scorerate = 15;
	private BukkitTask task = null;
	private BukkitTask scoreCalcs = null;
	public int maxCheckpoints = 3;
	public int totalLaps = 3;
	public ArrayList<Location> reloadingItemBoxes = new ArrayList<Location>();
	public int finishCountdown = 60;
	Boolean ending = false;
	Boolean ended = false;
	public Scoreboard board = null;
	public Objective scores = null;
	public RaceType type = RaceType.RACE;
	public Objective scoresBoard = null;

	public Race(RaceTrack track, String trackName, RaceType type) {
		this.type = type;
		this.gameId = UUID.randomUUID().toString();
		this.track = track;
		this.trackName = trackName;
		this.totalLaps = this.track.getLaps();
		this.maxCheckpoints = this.track.getCheckpoints().size() - 1;
		this.tickrate = MarioKart.config.getLong("general.raceTickrate");
		this.scorerate = (long) ((this.tickrate * 2) + (this.tickrate / 0.5));
		this.board = MarioKart.plugin.getServer().getScoreboardManager()
				.getNewScoreboard();
		this.scores = board.registerNewObjective("", "dummy");
		scores.setDisplaySlot(DisplaySlot.BELOW_NAME);
		if (type != RaceType.TIME_TRIAL) {
			this.scoresBoard = board.registerNewObjective(ChatColor.GOLD
					+ "Race Positions", "dummy");
		} else { // Time Trial
			this.scoresBoard = board.registerNewObjective(ChatColor.GOLD
					+ "Race Time(s)", "dummy");
		}
		scoresBoard.setDisplaySlot(DisplaySlot.SIDEBAR);
		MarioKart.plugin.gameScheduler.runningGames++;
	}

	public RaceType getType() {
		return this.type;
	}

	public User getUser(Player player) {
		for (User user : getUsers()) {
			if (user.getPlayer().equals(player)) {
				return user;
			}
		}

		return null;
	}

	public List<User> getUsers() {
		return new ArrayList<User>(users);
	}

	public void setUsers(List<User> users) {
		this.users = users;
		return;
	}

	public List<User> getUsersIn() {
		List<User> inUsers = new ArrayList<User>();
		for (User user : getUsers()) {
			if (user.isInRace()) {
				inUsers.add(user);
			}
		}
		return inUsers;
	}

	public List<User> getUsersFinished() {
		List<User> usersFinished = new ArrayList<User>();
		for (User user : getUsers()) {
			if (user.isFinished()) {
				usersFinished.add(user);
			}
		}
		return usersFinished;
	}

	public void playerOut(User user) {
		user.setInRace(false);

		Player player = user.getPlayer();

		player.setLevel(user.getOldLevel());

		player.setExp(user.getOldExp());

		return;
	}

	public Boolean join(Player player) {
		if (users.size() < this.track.getMaxPlayers()) {
			User user = new User(player);
			users.add(user);
			return true;
		}
		return false;
	}

	public void leave(User user, boolean quit) {
		Player player = null;
		player = user.getPlayer();

		player.setLevel(user.getOldLevel());

		if (quit) {
			users.remove(user);
			if (users.size() < 2) {

				for (User u : getUsersIn()) {
					String msg = MarioKart.msgs.get("race.end.soon");

					u.getPlayer().sendMessage(MarioKart.colors.getInfo() + msg);
				}
				startEndCount();
			}
		}
		playerOut(user);
		player.removeMetadata("car.stayIn", MarioKart.plugin);
		if (quit) {
			scoresBoard.getScore(player).setScore(0);
			this.board.resetScores(player);
			player.getInventory().clear();
			if (player.getVehicle() != null) {
				Vehicle veh = (Vehicle) player.getVehicle();
				veh.eject();
				veh.remove();
			}
			player.removeMetadata("car.stayIn", MarioKart.plugin);
			player.getInventory().setContents(user.getOldInventory());
			player.setGameMode(GameMode.SURVIVAL);
			try {
				player.teleport(this.track.getExit(MarioKart.plugin.getServer()));
			} catch (Exception e) {
				player.teleport(player.getWorld().getSpawnLocation());
			}
			player.sendMessage(ChatColor.GOLD + "Successfully quit the race!");
			player.setScoreboard(MarioKart.plugin.getServer().getScoreboardManager()
					.getMainScoreboard());
			for (User us : getUsers()) {
				us.getPlayer().sendMessage(ChatColor.GOLD + player.getName() + " quit the race!");
			}
		}
		try {
			recalculateGame();
		} catch (Exception e) {
		}
		return;
	}

	public void recalculateGame() {
		if (getUsersIn().size() < 1) {
			this.running = false;
			this.ended = true;
			this.ending = true;
			try {
				end();
			} catch (Exception e) {
			}
			MarioKart.plugin.gameScheduler.reCalculateQues();
		}
	}

	public Boolean isEmpty() {
		if (getUsers().size() < 1) {
			return true;
		}
		return false;
	}

	public String getGameId() {
		return this.gameId;
	}

	public String getTrackName() {
		return this.trackName;
	}

	public RaceTrack getTrack() {
		return this.track;
	}

	public void setWinner(User winner) {
		this.winner = winner;
	}

	public void startEndCount() {
		final int count = this.finishCountdown;

		MarioKart.plugin.getServer().getScheduler()
		.runTaskAsynchronously(MarioKart.plugin, new Runnable() {

			public void run() {
				int z = count;
				while (z > 0) {
					z--;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				if (!ended) {
					try {
						try {
							MarioKart.plugin
							.getServer()
							.getScheduler()
							.runTask(MarioKart.plugin,
									new Runnable() {

								public void run() {
									end();

									return;
								}
							});
						} catch (Exception e) {
							end();
						}
					} catch (IllegalArgumentException e) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
						}
						run();
						return;
					}
				}
				return;
			}
		});
	}

	public User getWinner() {
		return this.winner;
	}

	public Boolean getRunning() {
		return this.running;
	}

	public void start() {
		this.running = true;
		final Race game = this;
		for (User user : getUsersIn()) {
			Player player = user.getPlayer();

			if (player != null){
				try {
					player.setScoreboard(board);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			} else {
				this.leave(user, true);
			}
		}
		this.task = MarioKart.plugin.getServer().getScheduler()
				.runTaskTimer(MarioKart.plugin, new Runnable() {

					public void run() {
						RaceUpdateEvent event = new RaceUpdateEvent(game);
						MarioKart.plugin.getServer().getPluginManager()
						.callEvent(event);
						return;
					}
				}, tickrate, tickrate);
		this.scoreCalcs = MarioKart.plugin.getServer().getScheduler()
				.runTaskTimer(MarioKart.plugin, new Runnable() {

					public void run() {
						if (!(type == RaceType.TIME_TRIAL)) {
							SortedMap<String, Double> sorted = game
									.getRaceOrder();
							Object[] keys = sorted.keySet().toArray();
							for (int i = 0; i < sorted.size(); i++) {
								int pos = i + 1;
								try {
									String pname = (String) keys[i];
									Player pl = Bukkit.getServer().getPlayerExact(pname);
									game.scores.getScore(pl).setScore(pos);
									game.scoresBoard.getScore(pl)
									.setScore(-pos);
								} catch (IllegalStateException e) {
									e.printStackTrace();
								} catch (IllegalArgumentException e) {
									e.printStackTrace();
								}
							}
						} else { // Time trial
							try {
								User user = game.getUsers().get(0);
								Player pl = user.getPlayer();
								long time = System.currentTimeMillis()
										- startTimeMS;
								time = time / 1000; // In s
								game.scores.getScore(pl).setScore((int) time);
								game.scoresBoard.getScore(pl).setScore(
										(int) time);
							} catch (Exception e) {
								// Game ended or user has left
							}
						}
						return;
					}
				}, this.scorerate, this.scorerate);
		try {
			if (type == RaceType.TIME_TRIAL) {
				this.startTimeMS = System.currentTimeMillis();
			}
			MarioKart.plugin.getServer().getPluginManager()
			.callEvent(new RaceStartEvent(this));
		} catch (Exception e) {
			MarioKart.plugin.getLogger().severe("Error starting race!");
			end();
		}
		return;
	}

	public SortedMap<String, Double> getRaceOrder() {
		Race game = this;
		HashMap<String, Double> checkpointDists = new HashMap<String, Double>();
		List<User> users = game.getUsers();
		for (User user : users) {
			Player player = user.getPlayer();
			if (player != null && player.hasMetadata("checkpoint.distance")) {
				List<MetadataValue> metas = player
						.getMetadata("checkpoint.distance");
				checkpointDists.put(user.getPlayer().getName(), (Double) ((StatValue) metas.get(0)).getValue());
			}
		}
		Map<String, Double> scores = new HashMap<String, Double>();
		for (User user : users) {
			int laps = game.totalLaps - user.getLapsLeft() + 1;
			int checkpoints = user.getCheckpoint();
			double distance = 1 / (checkpointDists.get(user.getPlayer().getName()));

			double score = (laps * game.getMaxCheckpoints()) + checkpoints
					+ distance;
			try {
				if (game.getWinner().equals(user)) {
					score = score + 1;
				}
			} catch (Exception e) {
			}
			scores.put(user.getPlayer().getName(), score);
		}
		DoubleValueComparator com = new DoubleValueComparator(scores);
		SortedMap<String, Double> sorted = new TreeMap<String, Double>(com);
		sorted.putAll(scores);
		if (sorted.size() >= 1) {
			this.winning = (String) sorted.keySet().toArray()[0];
		}
		return sorted;
	}

	public void end() {
		this.running = false;
		if (task != null) {
			task.cancel();
		}
		try {
			if (scoreCalcs != null) {
				scoreCalcs.cancel();
			}
		} catch (Exception e1) {
		}
		try {
			this.board.clearSlot(DisplaySlot.BELOW_NAME);
		} catch (IllegalArgumentException e) {
		}
		try {
			this.board.clearSlot(DisplaySlot.SIDEBAR);
		} catch (IllegalArgumentException e) {
		}
		try {
			this.scores.unregister();
			this.scoresBoard.unregister();
		} catch (IllegalStateException e) {
		}
		try {
			int current = MarioKart.plugin.gameScheduler.runningGames;
			current--;
			if (current < 0) {
				current = 0;
			}
			MarioKart.plugin.gameScheduler.runningGames = current;
		} catch (Exception e) {
			// Server reloaded when game ending
		}

		this.endTimeMS = System.currentTimeMillis();
		for (User user : getUsersIn()) {
			Player player = user.getPlayer();

			if (player != null){
				player.setScoreboard(MarioKart.plugin.getServer().getScoreboardManager().getMainScoreboard());

				player.setLevel(user.getOldLevel());

				player.setExp(user.getOldExp());

				MarioKart.plugin.getServer().getPluginManager().callEvent(new RaceFinishEvent(this, user));
			}
		}

		RaceEndEvent evt = new RaceEndEvent(this);

		if (evt != null) {
			MarioKart.plugin.getServer().getPluginManager().callEvent(evt);
		}

		MarioKart.plugin.gameScheduler.reCalculateQues();
	}

	public void finish(User user) {
		if (!ending) {
			ending = true;

			startEndCount();
		}
		user.setFinished(true);

		Player player = user.getPlayer();

		if (player != null){
			player.setLevel(user.getOldLevel());
			
			player.setExp(user.getOldExp());
			
			this.endTimeMS = System.currentTimeMillis();
			
			MarioKart.plugin.getServer().getPluginManager().callEvent(new RaceFinishEvent(this, user));
		}
	}

	public CheckpointCheck playerAtCheckpoint(Integer[] checks, Player p,
			Server server) {
		int checkpoint = 0;
		Boolean at = false;
		Map<Integer, SerializableLocation> schecks = this.track
				.getCheckpoints();
		Location pl = p.getLocation();
		for (Integer key : checks) {
			if (schecks.containsKey(key)) {
				SerializableLocation sloc = schecks.get(key);
				Location check = sloc.getLocation(server);
				double dist = check.distanceSquared(pl); // Squared because of
				// better
				// performance
				p.removeMetadata("checkpoint.distance", MarioKart.plugin);
				p.setMetadata("checkpoint.distance", new StatValue(dist,
						MarioKart.plugin));
				if (dist < 100) {
					at = true;
					checkpoint = key;
					return new CheckpointCheck(at, checkpoint);
				}
			}
		}
		return new CheckpointCheck(at, checkpoint);
	}

	public int getMaxCheckpoints() {
		return maxCheckpoints; // Starts at 0
	}

	public Boolean atLine(Server server, Location loc) {
		Location line1 = this.track.getLine1(server);
		Location line2 = this.track.getLine2(server);
		String lineAxis = "x";
		Boolean at = false;
		Boolean l1 = true;
		if (line1.getX() + 0.5 > line2.getX() - 0.5
				&& line1.getX() - 0.5 < line2.getX() + 0.5) {
			lineAxis = "z";
		}
		if (lineAxis == "x") {
			if (line2.getX() < line1.getX()) {
				l1 = false;
			}
			if (l1) {
				if (line2.getX() + 0.5 > loc.getX()
						&& loc.getX() > line1.getX() - 0.5) {
					at = true;
				}
			} else {
				if (line1.getX() + 0.5 > loc.getX()
						&& loc.getX() > line2.getX() - 0.5) {
					at = true;
				}
			}
			if (at) {
				if (line1.getZ() + 4 > loc.getZ()
						&& line1.getZ() - 4 < loc.getZ()) {
					if (line1.getY() + 4 > loc.getY()
							&& line1.getY() - 4 < loc.getY()) {
						return true;
					}
				}
			}
		} else if (lineAxis == "z") {
			if (line2.getZ() < line1.getZ()) {
				l1 = false;
			}
			if (l1) {
				if (line2.getZ() + 0.5 > loc.getZ()
						&& loc.getZ() > line1.getZ() - 0.5) {
					at = true;
				}
			} else {
				if (line1.getZ() + 0.5 > loc.getZ()
						&& loc.getZ() > line2.getZ() - 0.5) {
					at = true;
				}
			}
			if (at) {
				if (line1.getX() + 4 > loc.getX()
						&& line1.getX() - 4 < loc.getX()) {
					if (line1.getY() + 4 > loc.getY()
							&& line1.getY() - 4 < loc.getY()) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
