package net.stormdev.mariokart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import net.stormdev.mariokart.powerups.MarioKartStuff;
import net.stormdev.mariokart.utils.Ques;
import net.stormdev.mariokart.utils.RaceMethods;
import net.stormdev.mariokart.utils.RaceQue;
import net.stormdev.mariokart.utils.RaceTrackManager;
import net.stormdev.mariokart.utils.TrackCreator;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.useful.ucars.Colors;
import com.useful.ucars.ucars;

public class MarioKart extends JavaPlugin
{
    public static YamlConfiguration lang = new YamlConfiguration(); //TODO: Fe's lang stuff
    public static FileConfiguration config = new YamlConfiguration();
    public static File langFile = null;
    public static File configFile = null;
    public static MarioKart plugin;
    public static Colors colors;
    public static ucars ucars = null;
    public static URaceCommandExecutor cmdExecutor = null;
    public static URaceListener listener = null;
    public RaceTrackManager trackManager = null;
    public RaceScheduler gameScheduler = null;
    public static HashMap<String, TrackCreator> trackCreators = new HashMap<String, TrackCreator>();
    public HashMap<String, RaceQue> ques = new HashMap<String, RaceQue>();
    public Ques raceQues = null;
    public static Lang msgs = null;
    public RaceMethods raceMethods = null;
    public static MarioKartStuff marioKart = null;
    public RaceTimes raceTimes = null;
    public void onEnable()
    {
        plugin = this;
        configFile = new File(getDataFolder() + File.separator+ "config.yml");
        langFile = new File(getDataFolder() + File.separator + "lang.yml");
        if (!configFile.exists()|| configFile.length() < 1)
        {
            getDataFolder().mkdirs();
            try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
            copy(getResource("marioKartConfigHeader.yml"), configFile);
        }
        config = getConfig();
        msgs = new Lang(this);
        saveConfig();
        try {
            lang.save(langFile);
        }
        catch (IOException e1) {
            getLogger().info("Error parsing lang file!");
        }
        // Load the colour scheme
        colors = new Colors(config.getString("colorScheme.success"),
        config.getString("colorScheme.error"),
        config.getString("colorScheme.info"),
        config.getString("colorScheme.title"),
        config.getString("colorScheme.title"));
        getLogger().info("Config loaded!");
        getLogger().info("Searching for uCars...");
        ucars = (ucars) getServer().getPluginManager().getPlugin("uCars"); //Should work
        if (ucars == null) {
            getLogger().info("Unable to find uCars!");
            getServer().getPluginManager().disablePlugin(this);
        }
        ucars.hookPlugin(this);
        getLogger().info("uCars found and hooked!");
        
        getCommand("marioRaceAdmin").setExecutor(cmdExecutor);
        getCommand("race").setExecutor(cmdExecutor);
        getCommand("racetimes").setExecutor(cmdExecutor); //Replace crappy code that scanns plugin.yml
        
        MarioKart.listener = new URaceListener(this);
        getServer().getPluginManager().registerEvents(MarioKart.listener, this);
        this.trackManager = new RaceTrackManager(this, new File(getDataFolder()
        + File.separator + "Data" + File.separator
        + "tracks.uracetracks"));
        this.raceQues = new Ques(this);
        this.raceMethods = new RaceMethods();
        this.gameScheduler = new RaceScheduler();
        // Setup marioKart
        marioKart = new MarioKartStuff(this);
        this.raceTimes = new RaceTimes(new File(getDataFolder()
        + File.separator + "Data" + File.separator
        + "raceTimes.uracetimes"),
        config.getBoolean("general.race.timed.log"));
    }
    public void onDisable() {
        if (ucars != null) {
            ucars.unHookPlugin(this);
        }
        for (Race r : gameScheduler.getGames().values()) {
            r.end();
            gameScheduler.stopGame(r.getTrack(), r.getGameId());
        }
        getServer().getScheduler().cancelTasks(this);
    }
    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                // System.out.write(buf, 0, len);
            }
            out.close();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String colorise(String prefix) {
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }
}