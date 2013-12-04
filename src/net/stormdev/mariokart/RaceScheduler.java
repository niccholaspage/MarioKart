package net.stormdev.mario.mariokart;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import net.stormdev.mario.utils.RaceQue;
import net.stormdev.mario.utils.RaceTrack;
import net.stormdev.mario.utils.RaceType;
import net.stormdev.mario.utils.SerializableLocation;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import com.useful.ucarsCommon.StatValue;

public class RaceScheduler {
	private HashMap<String, Race> games = new HashMap<String, Race>();
	private MarioKart plugin;
	Random random = null;
	public int runningGames = 0;
	public int maxGames = 10;

	public RaceScheduler() {
		this.plugin = MarioKart.plugin;
		random = new Random();
		this.maxGames = MarioKart.config.getInt("general.raceLimit");
	}

	public Boolean joinGame(Player player, RaceTrack track, RaceQue que, String trackName) {
		que.validatePlayers();
		if (que.getHowManyPlayers() < que.getPlayerLimit()
				&& player.isOnline()
				&& !(que.getType() == RaceType.TIME_TRIAL && que
				.getHowManyPlayers() > 0)) {
			if (player.isOnline()) {
				// que.addPlayer(playername);
				List<Player> arenaque = que.getPlayers();
				if (arenaque.contains(player)) {
					player.sendMessage(MarioKart.colors.getError()
							+ MarioKart.msgs.get("race.que.existing"));
					return true;
				}

				que.addPlayer(player);

				arenaque = que.getPlayers();

				for (Player p : arenaque) {
					if (!(p.isOnline() && p != null)) {
						arenaque.remove(p);

						for (Player pp : arenaque) {
							if (pp != null && pp.isOnline()) {
								pp.sendMessage(MarioKart.colors.getTitle()
										+ "[MarioKart:] "
										+ MarioKart.colors.getInfo() + p.getName()
										+ MarioKart.msgs.get("race.que.left"));
							}
						}
					} else {
						p.sendMessage(MarioKart.colors.getTitle() + "[MarioKart:] "
								+ MarioKart.colors.getInfo() + player.getName()
								+ MarioKart.msgs.get("race.que.joined"));
					}
				}
				plugin.raceQues.setQue(trackName, que);
				this.reCalculateQues();
				player.sendMessage(MarioKart.colors.getSuccess()
						+ MarioKart.msgs.get("race.que.success"));

				player.teleport(track.getLobby(plugin.getServer()));

				String rl = MarioKart.config.getString("mariokart.resourcePack");

				Boolean valid = true;
				try {
					new URL(rl);
				} catch (MalformedURLException e2) {
					rl = "https://dl.dropboxusercontent.com/u/147363358/MarioKart/Resource/MarioKart-latest.zip";
				}
				if (valid && MarioKart.config.getBoolean("bitlyUrlShortner")) {
					// Shorten url
					player.sendMessage(MarioKart.colors.getInfo() + MarioKart.msgs.get("resource.download"));
					player.sendMessage(MarioKart.colors.getInfo()
							+ MarioKart.msgs.get("resource.downloadHelp")
							+ ChatColor.RESET + " " + "http://bit.ly/19h9R6P");
					player.setTexturePack(rl);
				} else {
					// Dont shorten url
					player.sendMessage(MarioKart.colors.getInfo()
							+ MarioKart.msgs.get("resource.download"));
					player.sendMessage(MarioKart.colors.getInfo()
							+ MarioKart.msgs.get("resource.downloadHelp")
							+ ChatColor.RESET + " " + rl);
					player.setTexturePack(rl);
				}

				return true;
			}
		}
		if (player.isOnline()) {
			player.sendMessage(MarioKart.colors.getError()
					+ MarioKart.msgs.get("race.que.full"));
		}
		return false;
	}

	public void reCalculateQues() {
		Set<String> queNames = plugin.raceQues.getQues();
		for (String aname : queNames) {
			RaceQue que = plugin.raceQues.getQue(aname);
			List<Player> arenaque = que.getPlayers();

			for (Player player : arenaque) {
				if (!(player != null && player.isOnline())) {
					arenaque.remove(player);
				}
			}
			if (que.getTransitioning() == null) {
				que.setTransitioning(false);
			}
			Boolean timed_valid = false;
			if (que.getType() == RaceType.TIME_TRIAL
					&& que.getHowManyPlayers() > 0) {
				timed_valid = true;
			}
			if (!trackInUse(aname)
					&& que.getHowManyPlayers() >= MarioKart.config
					.getInt("race.que.minPlayers")
					&& !que.getTransitioning()
					&& !(this.runningGames >= this.maxGames) || timed_valid
					&& !trackInUse(aname) && !que.getTransitioning()
					&& !(this.runningGames >= this.maxGames)) {
				Boolean timed = que.getType() == RaceType.TIME_TRIAL;
				que.setTransitioning(true);
				plugin.raceQues.setQue(aname, que);
				final String queName = aname;
				double seconds = MarioKart.config
						.getDouble("general.raceGracePeriod");
				double time = seconds * 20;
				long grace = (long) time;
				if (!timed) {
					for (Player player : que.getPlayers()) {
						String msg = MarioKart.msgs.get("race.que.players");

						msg = msg.replaceAll(Pattern.quote("%time%"), ""
								+ seconds);

						player.sendMessage(MarioKart.colors.getInfo() + msg);
					}
				}
				if (!timed) {
					plugin.getServer().getScheduler()
					.runTaskLater(plugin, new Runnable() {

						public void run() {
							String aname = queName;
							RaceQue arena = MarioKart.plugin.raceQues
									.getQue(aname);
							if (arena.getHowManyPlayers() < MarioKart.config
									.getInt("race.que.minPlayers")) {
								arena.setTransitioning(false);
								plugin.raceQues.setQue(aname, arena);
								return;
							}
							Race game = new Race(arena.getTrack(),
									arena.getTrack().getTrackName(),
									arena.getType()); // Add new stuff
							// when the
							// system is
							// ready
							List<Player> players = arena.getPlayers();
							for (Player player : players) {
								game.join(player);

								arena.removePlayer(player);
							}
							arena.setTransitioning(false);
							plugin.raceQues.removeQue(aname);
							startGame(arena, aname, game);
							return;
						}
					}, grace); // 10 seconds
				} else {
					plugin.getServer().getScheduler()
					.runTask(plugin, new Runnable() {

						public void run() {
							String aname = queName;
							RaceQue arena = MarioKart.plugin.raceQues
									.getQue(aname);
							if (arena.getHowManyPlayers() < 1) {
								arena.setTransitioning(false);
								plugin.raceQues.removeQue(aname);
								return;
							}
							Race game = new Race(arena.getTrack(),
									arena.getTrack().getTrackName(),
									arena.getType()); // Add new stuff
							// when the
							// system is
							// ready

							for (Player player : arena.getPlayers()) {
								game.join(player);

								arena.removePlayer(player);
							}
							arena.setTransitioning(false);
							plugin.raceQues.removeQue(aname);
							startGame(arena, aname, game);
							return;
						}
					});
				}
			}
		}
		return;
	}

	public void startGame(RaceQue que, String trackName, final Race race) {
		this.games.put(race.getGameId(), race);
		final List<User> users = race.getUsers();
		for (User user : users) {
			Player player = user.getPlayer();

			if (player != null) {
				user.setOldInventory(player.getInventory().getContents().clone());

				player.getInventory().clear();

				player.setGameMode(GameMode.SURVIVAL);
			}
		}
		final ArrayList<Minecart> cars = new ArrayList<Minecart>();
		RaceTrack track = race.getTrack();
		ArrayList<SerializableLocation> sgrid = track.getStartGrid();
		HashMap<Integer, Location> grid = new HashMap<Integer, Location>();
		for (int i = 0; i < sgrid.size(); i++) {
			SerializableLocation s = sgrid.get(i);
			grid.put(i, s.getLocation(plugin.getServer()));
		}
		int count = grid.size();
		if (count > users.size()) { // If more grid slots than players, only
			// use the right number of grid slots
			count = users.size();
		}
		if (users.size() > count) {
			count = users.size(); // Should theoretically never happen but
			// sometimes does?
		}
		for (int i = 0; i < count; i++) {
			int min = 0;
			int max = users.size();
			if (!(max < 1)) {
				int randomNumber = random.nextInt(max - min) + min;

				User user = users.get(randomNumber);

				Player p = users.get(randomNumber).getPlayer();

				users.remove(user);

				Location loc = grid.get(i);

				if (p != null) {
					if (p.getVehicle() != null) {
						p.getVehicle().eject();
					}

					p.teleport(loc.add(0, 2, 0));

					Minecart car = (Minecart) loc.getWorld().spawnEntity(loc.add(0, 0.2, 0), EntityType.MINECART);

					car.setMetadata("car.frozen", new StatValue(null,MarioKart.plugin));

					car.setMetadata("kart.racing", new StatValue(null, MarioKart.plugin));

					car.setPassenger(p);

					p.setMetadata("car.stayIn", new StatValue(null, plugin));

					cars.add(car);
				}
			}
		}
		if (users.size() > 0) {
			User user = users.get(0);

			Player p = user.getPlayer();

			if (p != null){
				p.sendMessage(MarioKart.colors.getError() + MarioKart.msgs.get("race.que.full"));
			}

			race.leave(user, true);
		}

		for (User user : users) {
			Player player = user.getPlayer();

			if (player != null){
				user.setLocation(player.getLocation().clone());

				player.sendMessage(MarioKart.colors.getInfo() + MarioKart.msgs.get("race.que.preparing"));
			}
		}
		final List<User> users2 = race.getUsers();
		for (User user2 : users2) {
			user2.setInRace(true);
		}
		plugin.getServer().getScheduler()
		.runTaskAsynchronously(plugin, new Runnable() {
			public void run() {
				for (User user : users2) {
					Player player = user.getPlayer();

					if (player != null){
						user.getPlayer().sendMessage(MarioKart.colors.getInfo() + MarioKart.msgs.get("race.que.starting"));
					}
				}
				for (int i = 10; i > 0; i--) {
					try {
						if (i == 10) {
							try {
								Player player = users.get(0).getPlayer();

								if (player != null){
									player.getWorld().playSound(player.getLocation(), Sound.BREATH, 8, 1);
								}
							} catch (Exception e) {

							}
						}
						if (i == 3) {
							try {
								Player player = users.get(0).getPlayer();

								if (player != null){
									player.getWorld().playSound(player.getLocation(), Sound.NOTE_BASS_DRUM, 8, 1);
								}
							} catch (Exception e) {

							}
						}
					} catch (Exception e) {
						// Game ended
					}
					for (User user : users2) {
						Player p = user.getPlayer();

						if (p != null){
							p.sendMessage(MarioKart.colors.getInfo() + "" + i);
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
				}
				
				for (Minecart car : cars) {
					car.removeMetadata("car.frozen", MarioKart.plugin);
				}
				
				for (User user : users2) {
					Player player = user.getPlayer();

					if (player != null){
						player.sendMessage(MarioKart.colors.getInfo() + MarioKart.msgs.get("race.que.go"));
					}
				}
				race.start();
				return;
			}
		});

		return;
	}

	public void updateGame(Race game) {
		this.games.put(game.getGameId(), game);
		return;
	}

	public void stopGame(RaceTrack track, String gameId) {
		if (!trackInUse(track.getTrackName())) {
			return;
		}
		removeRace(track.getTrackName());
		reCalculateQues();
		return;
	}

	public void leaveQue(Player player, RaceQue arena, String arenaName) {
		if (getQue(arena).contains(player)) {
			arena.removePlayer(player);
		}

		for (Player p : getQue(arena)) {
			if (p != null && p.isOnline()) {
				p.sendMessage(MarioKart.colors.getTitle() + "[MarioKart:] "
						+ MarioKart.colors.getInfo() + player.getName()
						+ MarioKart.msgs.get("race.que.left"));
			}
		}
		reCalculateQues();
		return;
	}

	public List<Player> getQue(RaceQue que) {
		return que.getPlayers();
	}

	public Boolean trackInUse(String arenaName) {
		Set<String> keys = this.games.keySet();
		ArrayList<String> kz = new ArrayList<String>();
		kz.addAll(keys);
		for (String key : kz) {
			Race game = this.games.get(key);
			if (game.getTrackName().equalsIgnoreCase(arenaName)) {
				if (!game.running) {
					removeRace(game.getTrackName());
					this.games.remove(key);
				} else {
					return true;
				}
			}
		}
		return false;
	}

	public Boolean removeRace(String trackName) {
		Set<String> keys = this.games.keySet();
		ArrayList<String> kz = new ArrayList<String>();
		kz.addAll(keys);
		for (String key : kz) {
			Race game = this.games.get(key);
			if (game != null) {
				if (game.getTrackName().equalsIgnoreCase(trackName)) {
					for (User user : game.getUsers()) {
						try {
							Player pl = user.getPlayer();
							
							pl.removeMetadata("car.stayIn", plugin);
						} catch (Exception e) {
							// MetaData not set or player has left
						}
					}
					this.games.remove(key);
				}
			}
		}
		return false;
	}

	public HashMap<String, Race> getGames() {
		return this.games;
	}
}
