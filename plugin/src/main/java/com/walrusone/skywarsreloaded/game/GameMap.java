package com.walrusone.skywarsreloaded.game;

import com.walrusone.skywarsreloaded.enums.GameType;
import com.walrusone.skywarsreloaded.enums.ScoreVar;
import com.walrusone.skywarsreloaded.menus.TeamSelectionMenu;
import com.walrusone.skywarsreloaded.menus.TeamSpectateMenu;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Iterables;
import com.walrusone.skywarsreloaded.managers.MatchManager;
import com.walrusone.skywarsreloaded.managers.PlayerStat;
import com.walrusone.skywarsreloaded.managers.WorldManager;
import com.walrusone.skywarsreloaded.matchevents.AnvilRainEvent;
import com.walrusone.skywarsreloaded.matchevents.ArrowRainEvent;
import com.walrusone.skywarsreloaded.matchevents.ChestRefillEvent;
import com.walrusone.skywarsreloaded.matchevents.CrateDropEvent;
import com.walrusone.skywarsreloaded.matchevents.DeathMatchEvent;
import com.walrusone.skywarsreloaded.matchevents.DisableRegenEvent;
import com.walrusone.skywarsreloaded.matchevents.EnderDragonEvent;
import com.walrusone.skywarsreloaded.matchevents.HealthDecayEvent;
import com.walrusone.skywarsreloaded.matchevents.MatchEvent;
import com.walrusone.skywarsreloaded.matchevents.MobSpawnEvent;
import com.walrusone.skywarsreloaded.matchevents.WitherEvent;
import com.walrusone.skywarsreloaded.menus.ArenaMenu;
import com.walrusone.skywarsreloaded.menus.ArenasMenu;
import com.walrusone.skywarsreloaded.menus.gameoptions.ChestOption;
import com.walrusone.skywarsreloaded.menus.gameoptions.GameOption;
import com.walrusone.skywarsreloaded.menus.gameoptions.HealthOption;
import com.walrusone.skywarsreloaded.menus.gameoptions.KitVoteOption;
import com.walrusone.skywarsreloaded.menus.gameoptions.ModifierOption;
import com.walrusone.skywarsreloaded.menus.gameoptions.TimeOption;
import com.walrusone.skywarsreloaded.menus.gameoptions.WeatherOption;
import com.walrusone.skywarsreloaded.menus.gameoptions.objects.CoordLoc;
import com.walrusone.skywarsreloaded.menus.gameoptions.objects.GameKit;
import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.enums.MatchState;
import com.walrusone.skywarsreloaded.game.cages.Cage;
import com.walrusone.skywarsreloaded.game.cages.CageType;
import com.walrusone.skywarsreloaded.game.cages.CubeCage;
import com.walrusone.skywarsreloaded.game.cages.DomeCage;
import com.walrusone.skywarsreloaded.game.cages.PyramidCage;
import com.walrusone.skywarsreloaded.game.cages.SphereCage;
import com.walrusone.skywarsreloaded.game.cages.StandardCage;
import com.walrusone.skywarsreloaded.utilities.Messaging;
import com.walrusone.skywarsreloaded.utilities.Party;
import com.walrusone.skywarsreloaded.utilities.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.util.ArrayList;

public class GameMap {
 
	static {
		new ArenasMenu();
	}
	private static ArrayList<GameMap> arenas;

	private ArrayList<Crate> crates = new ArrayList<>();
	private boolean forceStart;
	private boolean allowFallDamage;
	private boolean allowRegen;
    private boolean thunder;
    private boolean allowFriendlyFire;
    private List<String> winners = new ArrayList<>();
    private int strikeCounter;
    private int nextStrike;
    private MatchState matchState;
    private ArrayList<TeamCard> teamCards;
    private int teamSize;

    private ArrayList<UUID> spectators = new ArrayList<>();
    private String name;
    private int timer;
    private int minPlayers;
    private GameKit kit;
    private Cage cage;
    private String currentTime;
    private String currentHealth;
    private String currentChest;
    private String currentWeather;
    private String currentModifier;
    private KitVoteOption kitVoteOption;
    private ChestOption chestOption;
    private HealthOption healthOption;
    private TimeOption timeOption;
    private WeatherOption weatherOption;
    private ModifierOption modifierOption;
	private ArrayList<CoordLoc> chests;
	private String environment;
	private String displayName;
	private String designedBy;
	private ArrayList<SWRSign> signs;
	private GameBoard gameboard;
	private boolean registered;
	private String arenakey;
	private GameQueue joinQueue;
	private boolean inEditing = false;
	private CoordLoc spectateSpawn;
	private ArrayList<CoordLoc> deathMatchSpawns;
	private boolean legacy = false;
	private ArrayList<MatchEvent> events = new ArrayList<>();
	private ArrayList<String> deathMatchWaiters = new ArrayList<>();
	private ArrayList<String> anvils = new ArrayList<>();
		
    public GameMap(final String name) {
        this.name = name;
    	this.matchState = MatchState.OFFLINE;
    	teamCards = new ArrayList<>();
    	deathMatchSpawns = new ArrayList<>();
    	signs = new ArrayList<>();
    	chests = new ArrayList<>();
    	loadArenaData();
        this.thunder = false;
        allowRegen = true;
        timer = SkyWarsReloaded.getCfg().getWaitTimer();
        joinQueue = new GameQueue(this);
        arenakey = name + "menu";
        if (SkyWarsReloaded.getCfg().kitVotingEnabled()) {
			kitVoteOption = new KitVoteOption(this, name + "kitVote");
        }
        chestOption = new ChestOption(this, name + "chest");
        healthOption = new HealthOption(this, name + "health");
        timeOption =  new TimeOption(this, name + "time");
        weatherOption = new WeatherOption(this, name + "weather");
        modifierOption = new ModifierOption(this, name + "modifier");
        gameboard = new GameBoard(this);
        if (legacy) {
        	 boolean loaded = loadWorldForScanning(name);
             if (loaded) {
             	ChunkIterator();
      			SkyWarsReloaded.getWM().deleteWorld(name);
      			saveArenaData();
             }
        }
        loadEvents();
        if (registered) {
        	registerMap();
        }
        new ArenaMenu(arenakey, this);
        if (SkyWarsReloaded.getCfg().joinMenuEnabled()) {
			new TeamSelectionMenu(this);
		}
		if (SkyWarsReloaded.getCfg().spectateMenuEnabled()) {
        	new TeamSpectateMenu(this);
		}
    }

	private void loadEvents() {
		File dataDirectory = SkyWarsReloaded.get().getDataFolder();
        File mapDataDirectory = new File(dataDirectory, "mapsData");

        if (!mapDataDirectory.exists() && !mapDataDirectory.mkdirs()) {
        	return;
        }

        File mapFile = new File(mapDataDirectory, name + ".yml");
        FileConfiguration fc = YamlConfiguration.loadConfiguration(mapFile);
       	events.add(new DisableRegenEvent(this, fc.getBoolean("events.DisableRegenEvent.enabled")));
       	events.add(new HealthDecayEvent(this, fc.getBoolean("events.HealthDecayEvent.enabled")));
       	events.add(new EnderDragonEvent(this, fc.getBoolean("events.EnderDragonEvent.enabled")));
       	events.add(new WitherEvent(this, fc.getBoolean("events.WitherEvent.enabled")));
       	events.add(new MobSpawnEvent(this, fc.getBoolean("events.MobSpawnEvent.enabled")));
        events.add(new ChestRefillEvent(this, fc.getBoolean("events.ChestRefillEvent.enabled")));
       	events.add(new DeathMatchEvent(this, fc.getBoolean("events.DeathMatchEvent.enabled")));
       	events.add(new ArrowRainEvent(this, fc.getBoolean("events.ArrowRainEvent.enabled")));
        events.add(new AnvilRainEvent(this, fc.getBoolean("events.AnvilRainEvent.enabled")));
       	events.add(new CrateDropEvent(this, fc.getBoolean("events.CrateDropEvent.enabled")));
	}

	public void update() {
		updateArenasManager();
		this.updateArenaManager();
        this.updateSigns();
        this.sendBungeeUpdate();
        if (SkyWarsReloaded.getIC().has("joinsinglemenu") && teamSize == 1) {
            SkyWarsReloaded.getIC().getMenu("joinsinglemenu").update();
        }
        if (SkyWarsReloaded.getIC().has("jointeammenu") && teamSize > 1) {
            SkyWarsReloaded.getIC().getMenu("jointeammenu").update();
            if (matchState == MatchState.WAITINGSTART && SkyWarsReloaded.getIC().has(name + "teamselect")) {
				SkyWarsReloaded.getIC().getMenu(name + "teamselect").update();
			}
        }
	}
   
	/*Player Handling Methods*/
	
	public boolean addPlayers(@Nullable TeamCard teamToTry, final Player player) {
		if (Util.get().isBusy(player.getUniqueId())) {
			return false;
		}
		boolean result = false;
		PlayerStat ps = PlayerStat.getPlayerStats(player.getUniqueId());
		if (teamSize > 1) {
		    teamCards.sort(new TeamCardComparator());
		} else {
			Collections.shuffle(teamCards);
		}
		if (ps != null && ps.isInitialized()) {
			TeamCard reserved = null;
			if (teamToTry == null) {
				for (TeamCard tCard: teamCards) {
					if (tCard.getFullCount() > 0) {
						reserved = tCard.sendReservation(player, ps);
						break;
					}
				}
			} else {
				if (teamToTry.getFullCount() > 0) {
					reserved = teamToTry.sendReservation(player, ps);
				}
			}
			if (reserved != null) {
				result = reserved.joinGame(player);
			}
			this.update();
			gameboard.updateScoreboardVar(ScoreVar.PLAYERS);
		}
    	return result;
    }
	
	public boolean addPlayers(@Nullable TeamCard teamToTry, final Party party) {
		TeamCard team = null;
		Map<TeamCard, ArrayList<Player>> players = new HashMap<>();
		if (teamSize == 1) {
			Collections.shuffle(teamCards);
			for (UUID uuid: party.getMembers()) {
				Player player = Bukkit.getPlayer(uuid);
				if (Util.get().isBusy(uuid)) {
					party.sendPartyMessage(new Messaging.MessageFormatter().setVariable("player", player.getName()).format("party.memberbusy"));
				} else {
					PlayerStat ps = PlayerStat.getPlayerStats(uuid);
					if (ps != null && player != null && ps.isInitialized()) {
						for (TeamCard tCard: teamCards) {
							if (tCard.getFullCount() > 0) {
								TeamCard reserve = tCard.sendReservation(player, ps);
								this.update();
								gameboard.updateScoreboardVar(ScoreVar.PLAYERS);
								if (reserve != null) {
								    players.computeIfAbsent(reserve, k -> new ArrayList<>());
									players.get(reserve).add(player);
								}
								break;
							}
						}
					}
				}
			}
		} else {
			if (teamToTry == null) {
				teamCards.sort(new TeamCardComparator());
				for (TeamCard tCard: teamCards) {
					if (tCard.getFullCount() >= party.getSize()) {
						for (int i = 0; i < party.getSize(); i++) {
							Player player = Bukkit.getPlayer(party.getMembers().get(i));
							PlayerStat ps = PlayerStat.getPlayerStats(player.getUniqueId());
							if (ps != null && ps.isInitialized()) {
								TeamCard reserve = tCard.sendReservation(player, ps);
								if (reserve != null) {
									players.computeIfAbsent(reserve, k -> new ArrayList<>()).add(player);
								}
								team = reserve;
							}
						}
						this.update();
						gameboard.updateScoreboardVar(ScoreVar.PLAYERS);
					}
					if (team != null && players.get(team).size() == party.getSize()) {
						break;
					}
				}
			} else {
				if (teamToTry.getFullCount() >= party.getSize()) {
					for (int i = 0; i < party.getSize(); i++) {
						Player player = Bukkit.getPlayer(party.getMembers().get(i));
						PlayerStat ps = PlayerStat.getPlayerStats(player.getUniqueId());
						if (ps != null && ps.isInitialized()) {
							TeamCard reserve = teamToTry.sendReservation(player, ps);
							if (reserve != null) {
								players.computeIfAbsent(reserve, k -> new ArrayList<>()).add(player);
							}
							team = reserve;
						}
					}
					this.update();
					gameboard.updateScoreboardVar(ScoreVar.PLAYERS);
				}
			}
		}

		boolean result = false;
		if (teamSize == 1 && players.size() == party.getSize()) {
			for (TeamCard tCard: players.keySet()) {
				result = tCard.joinGame(players.get(tCard).get(0));
			}
			this.update();
			gameboard.updateScoreboardVar(ScoreVar.PLAYERS);
			return result;
		} else if (teamSize > 1 && team != null && players.get(team).size() == party.getSize()) {
			for (int i = 0; i < players.get(team).size(); i++) {
				result = team.joinGame(players.get(team).get(i));
			}
			this.update();
			gameboard.updateScoreboardVar(ScoreVar.PLAYERS);
			return result;
		} else {
			for (ArrayList<Player> play: players.values()) {
				for (Player player: play) {
					PlayerCard pCard = this.getPlayerCard(player);
					pCard.reset();
				}
			}
		}
    	this.update();
		gameboard.updateScoreboardVar(ScoreVar.PLAYERS);
    	return false;
	}

	public void removePlayer(final UUID uuid) {
		boolean result;
		for (TeamCard tCard: teamCards) {
            result = tCard.removePlayer(uuid);
            if (result) {
                this.update();
				gameboard.updateScoreboardVar(ScoreVar.PLAYERS);
                break;
            }
        }
        this.update();
		gameboard.updateScoreboardVar(ScoreVar.PLAYERS);
    }
 
    public ArrayList<Player> getAlivePlayers() {
    	ArrayList<Player> alivePlayers = new ArrayList<>();
    	for (TeamCard tCard: teamCards) {
    		for (PlayerCard pCard: tCard.getPlayerCards()) {
        		if (pCard.getPlayer() != null) {
            		if (!mapContainsDead(pCard.getPlayer().getUniqueId())) {
                		alivePlayers.add(pCard.getPlayer());
            		}
        		}
        	}
    	}
    	return alivePlayers;
    }
    
    public boolean mapContainsDead(UUID uuid) {
    	for(TeamCard tCard: teamCards) {
    		if (tCard.getDead().contains(uuid)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public ArrayList<Player> getAllPlayers() {
    	ArrayList<Player> allPlayers = new ArrayList<>();
    	for (TeamCard tCard: teamCards) {
    		for (PlayerCard pCard: tCard.getPlayerCards()) {
        		if (pCard.getPlayer() != null) {
                	allPlayers.add(pCard.getPlayer());
        		}
        	}
    	}
    	return allPlayers;
    }
    
    public ArrayList<Player> getMessageAblePlayers(boolean isSpectator) {
    	ArrayList<Player> recievers = new ArrayList<>();
    	if (!isSpectator) {
        	for (TeamCard tCard: teamCards) {
        		for (PlayerCard pCard: tCard.getPlayerCards()) {
            		if (pCard.getPlayer() != null) {
                		if (!mapContainsDead(pCard.getPlayer().getUniqueId())) {
                    		recievers.add(pCard.getPlayer());
                		}
            		}
            	}
        	}
    	}
    	for (UUID uuid: spectators) {
    		Player player = SkyWarsReloaded.get().getServer().getPlayer(uuid);
    		if (player != null) {
    			recievers.add(player);
    		}
    	}
    	return recievers;
    }
    
    public boolean canAddPlayer() {
    	if (!(this.matchState == MatchState.WAITINGSTART && this.registered)) {
    		return false;
    	}
    	for (TeamCard tCard: teamCards) {
    		if (tCard.getFullCount() > 0) {
    			return true;
    		}
    	}
        return false;
    }
    
    public boolean canAddParty(Party party) {
    	if (!(this.matchState == MatchState.WAITINGSTART && this.registered)) {
    		return false;
    	}
    	if (teamSize == 1) {
    		int playerCount = getPlayerCount();
    		return playerCount + party.getSize()-1 < teamCards.size();
    	} else {
    		for (TeamCard tCard: teamCards) {
        		if (tCard.getFullCount() >= party.getSize()) {
        			return true;
        		}
        	}
    	}
    	return false;
    }
    
	/*Map Handling Methods*/
	
    static {
        GameMap.arenas = new ArrayList<>();
    }
	
	public static GameMap getMap(final String mapName) {
    	shuffle();
    	for (final GameMap map : GameMap.arenas) {
            if (map.name.equals(ChatColor.stripColor(mapName))) {
                return map;
            }
        }
        return null;
    }
	
	private static void addMap(String name) {
		GameMap gMap = new GameMap(name);
		arenas.add(gMap);
	}
	
	public boolean removeMap() {
		unregister();
		File dataDirectory = new File (SkyWarsReloaded.get().getDataFolder(), "maps");
		File target = new File (dataDirectory, name);
		SkyWarsReloaded.getWM().deleteWorld(target);

        File mapDataDirectory = new File(SkyWarsReloaded.get().getDataFolder(), "mapsData");
        if (!mapDataDirectory.exists() && !mapDataDirectory.mkdirs()) {
        	return false;
        }
        File mapFile = new File(mapDataDirectory, name + ".yml");
        boolean result = mapFile.delete();
        if (result) {
            arenas.remove(this);
        }
        return result;
	}
        
    public static void loadMaps() {
    	File mapFile = new File(SkyWarsReloaded.get().getDataFolder(), "maps.yml");
        if (mapFile.exists()) {
        	updateMapData();
        }
    	arenas.clear();
    	File dataDirectory = SkyWarsReloaded.get().getDataFolder();
		File maps = new File (dataDirectory, "maps");
		if (maps.exists() && maps.isDirectory()) {
		    File[] files = maps.listFiles();
		    if (files != null) {
                for (File map : files) {
                    if (map.isDirectory()) {
                        addMap(map.getName());
                    }
                }
            }
		} else {
			SkyWarsReloaded.get().getLogger().info("Maps directory is missing or no Maps were found!");
		} 
    }
    
	private static void updateMapData() {
		 File mapFile = new File(SkyWarsReloaded.get().getDataFolder(), "maps.yml");       
	        if (mapFile.exists()) {
	            FileConfiguration storage = YamlConfiguration.loadConfiguration(mapFile);

	            if (storage.getConfigurationSection("maps") != null) {
	                for (String key: storage.getConfigurationSection("maps").getKeys(false)) {
	                	String displayname = storage.getString("maps." + key + ".displayname");
	                	int minplayers = storage.getInt("maps." + key + ".minplayers");
	                	String creator = storage.getString("maps." + key + ".creator");
	                	List<String> signs = storage.getStringList("maps." + key + ".signs");
	                	boolean registered = storage.getBoolean("maps." + key + ".registered");
	                	
	            		File dataDirectory = SkyWarsReloaded.get().getDataFolder();
	                    File mapDataDirectory = new File(dataDirectory, "mapsData");

	                    if (!mapDataDirectory.exists() && !mapDataDirectory.mkdirs()) {
	                    	return;
	                    }

	                    File newMapFile = new File(mapDataDirectory, key + ".yml");
	                    copyDefaults(newMapFile);
	                    FileConfiguration fc = YamlConfiguration.loadConfiguration(newMapFile);
	                    fc.set("displayname", displayname);
	    	            fc.set("minplayers", minplayers);
	    	            fc.set("creator", creator);
	    	            fc.set("signs", signs);
	    	            fc.set("registered", registered);
	    	            fc.set("environment", "NORMAL");
	    	            fc.set("spectateSpawn", "0:95:0");
	    	            fc.set("deathMatchSpawns", null);
	    	            fc.set("legacy", true);
	    	            try {
							fc.save(newMapFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
	                }
	            }
	            boolean result = mapFile.delete();
	            if (!result) {
	                SkyWarsReloaded.get().getLogger().info("Failed to Delete Old MapData File");
                }
	        }
	}
	
	private void saveArenaData() {
		File dataDirectory = SkyWarsReloaded.get().getDataFolder();
        File mapDataDirectory = new File(dataDirectory, "mapsData");

        if (!mapDataDirectory.exists() && !mapDataDirectory.mkdirs()) {
        	return;
        }

        File mapFile = new File(mapDataDirectory, name + ".yml");
        if (!mapFile.exists()) {
        	SkyWarsReloaded.get().getLogger().info("File doesn't exist!");
        	return;
        }
        copyDefaults(mapFile);
        FileConfiguration fc = YamlConfiguration.loadConfiguration(mapFile);
        fc.set("displayname", displayName);
        fc.set("minplayers", minPlayers);
        fc.set("creator", designedBy);
        fc.set("registered", registered);
        fc.set("spectateSpawn", spectateSpawn.getLocation());
        fc.set("cage", cage.getType().toString().toLowerCase());
        fc.set("teamSize", teamSize);
        fc.set("environment", environment);
        fc.set("allowFriendlyFire", allowFriendlyFire);
       
        List<String> spawns = new ArrayList<>();
        for (TeamCard tCard: teamCards) {
        	spawns.add(tCard.getSpawn().getLocation());
        }
        fc.set("spawns", spawns);
        
        List<String> dSpawns = new ArrayList<>();
        for (CoordLoc loc: deathMatchSpawns) {
        	dSpawns.add(loc.getLocation());
        }
        fc.set("deathMatchSpawns", dSpawns);
        
        List<String> stringSigns = new ArrayList<>();
   	 	for (SWRSign s: signs) {
   	 		stringSigns.add(Util.get().locationToString(s.getLocation()));
   	 	}
   	 	fc.set("signs", stringSigns);

   	 	List<String> stringChests = new ArrayList<>();
   	 	for (CoordLoc chest: chests) {
   	 		stringChests.add(chest.getLocation());
   	 	}
   	 	fc.set("chests", stringChests);
   	 	fc.set("legacy", null);
   	 	try {
			fc.save(mapFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadArenaData() {
		File dataDirectory = SkyWarsReloaded.get().getDataFolder();
        File mapDataDirectory = new File(dataDirectory, "mapsData");

        if (!mapDataDirectory.exists() && !mapDataDirectory.mkdirs()) {
        	return;
        }

        File mapFile = new File(mapDataDirectory, name + ".yml");
        copyDefaults(mapFile);
        
        FileConfiguration fc = YamlConfiguration.loadConfiguration(mapFile);
        displayName = fc.getString("displayname", name);
        designedBy = fc.getString("creator", "");
        registered = fc.getBoolean("registered", false);
        spectateSpawn = Util.get().getCoordLocFromString(fc.getString("spectateSpawn", "0:95:0"));
        legacy = fc.getBoolean("legacy");
        teamSize = fc.getInt("teamSize", 1);
        environment = fc.getString("environment", "NORMAL");
        allowFriendlyFire = fc.getBoolean("allowFriendlyFire", false); 
     
        String cage = fc.getString("cage");
        CageType ct = CageType.matchType(cage.toUpperCase());
        setCage(ct);		
        
        List<String> spawns = fc.getStringList("spawns");
        List<String> dSpawns = fc.getStringList("deathMatchSpawns");
        List<String> stringSigns = fc.getStringList("signs");
        List<String> stringChests = fc.getStringList("chests");
       
        for (String spawn: spawns) {
        	addTeamCard(Util.get().getCoordLocFromString(spawn));
        }

        int def = 2;
        if (teamCards.size() > 4) {
        	def = teamCards.size()/2;
        }
        minPlayers = fc.getInt("minplayers", def);
        
        for (String dSpawn: dSpawns) {
        	deathMatchSpawns.add(Util.get().getCoordLocFromString(dSpawn));
        }
        
   	 	for (String s: stringSigns) {
   	 		signs.add(new SWRSign(name, Util.get().stringToLocation(s)));
   	 	}

   	 	for (String chest: stringChests) {
   	 		addChest(Util.get().getCoordLocFromString(chest));
   	 	}
   	 	try {
			fc.save(mapFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static World createNewMap(String mapName, World.Environment environment) {
    	World newWorld = SkyWarsReloaded.getWM().createEmptyWorld(mapName, environment);
		if (newWorld == null) {
			return null;
		}
		newWorld.save();
		SkyWarsReloaded.getWM().unloadWorld(mapName);
		addMap(mapName);
		GameMap map = GameMap.getMap(mapName);
		if (map != null) {
			map.environment = environment.toString();
			map.saveArenaData();
		}
		File dataDirectory = new File (SkyWarsReloaded.get().getDataFolder(), "maps");
		File target = new File (dataDirectory, mapName);
		SkyWarsReloaded.getWM().deleteWorld(target);
		File source = new File (SkyWarsReloaded.get().getServer().getWorldContainer().getAbsolutePath(), mapName);
		SkyWarsReloaded.getWM().copyWorld(source, target);
		SkyWarsReloaded.getWM().loadWorld(mapName, environment);
		return SkyWarsReloaded.get().getServer().getWorld(mapName);
	}
	
	public boolean registerMap() {
		if (inEditing) {
			saveMap(null);
		}
    	if (teamCards.size() > 1) {
    		registered = true;
    		gameboard = new GameBoard(this);
            refreshMap();
            SkyWarsReloaded.get().getLogger().info("Registered Map " + name + "!");
    	} else {
    		registered = false;
    		SkyWarsReloaded.get().getLogger().info("Could Not Register Map: " + name + " - Map must have at least 2 Spawn Points");
    	}
    	return registered;
	}
	
	public void unregister() {
		this.registered = false;
		saveArenaData();
		stopGameInProgress();
	}
	
	public void stopGameInProgress() {
		this.matchState = MatchState.OFFLINE;
		for (final UUID uuid: this.getSpectators()) {
    		final Player player = SkyWarsReloaded.get().getServer().getPlayer(uuid);
    		if (player != null) {
    			MatchManager.get().removeSpectator(player);
    		}
    	}
        for (final Player player : this.getAlivePlayers()) {
        	if (player != null) {
                MatchManager.get().playerLeave(player, DamageCause.CUSTOM, true, false);
        	}
        }
        SkyWarsReloaded.getWM().deleteWorld(this.getName());
	}

    private static boolean loadWorldForScanning(String name) {
        	File dataDirectory = SkyWarsReloaded.get().getDataFolder();
    		File maps = new File (dataDirectory, "maps");
    		
    			String root = SkyWarsReloaded.get().getServer().getWorldContainer().getAbsolutePath();
    			File rootDirectory = new File(root);
    			WorldManager wm = SkyWarsReloaded.getWM();
    			File source = new File(maps, name);
    			File target = new File(rootDirectory, name);
    			wm.copyWorld(source, target);
    			boolean mapExists = false;
    			if(target.isDirectory()) {
    			    String[] list = target.list();
    				if(list != null && list.length > 0) {
    		 			mapExists = true;
    				}	 
    			}
    			if (mapExists) {
    				SkyWarsReloaded.getWM().deleteWorld(name);
    			}
    			
    			wm.copyWorld(source, target);
    			
    			boolean loaded = SkyWarsReloaded.getWM().loadWorld(name, World.Environment.NORMAL);
    			if(!loaded) {
    				SkyWarsReloaded.get().getLogger().info("Could Not Load Map: " + name);
    			}
    			return loaded;
	}

	public static ArrayList<GameMap> getMaps() {
		return new ArrayList<>(arenas);
	}
	
	public static ArrayList<GameMap> getPlayableArenas(GameType type) {
		ArrayList<GameMap> sorted = new ArrayList<>();
		if (type == GameType.TEAM) {
			for (GameMap gMap: arenas) {
				if (gMap.isRegistered() && gMap.teamSize > 1) {
					sorted.add(gMap);
				}
			}
		} else if (type == GameType.SINGLE) {
			for (GameMap gMap: arenas) {
				if (gMap.isRegistered() && gMap.teamSize == 1) {
					sorted.add(gMap);
				}
			}
		} else {
			for (GameMap gMap: arenas) {
				if (gMap.isRegistered()) {
					sorted.add(gMap);
				}
			}
		}
		sorted.sort(new GameMapComparator());
		return sorted;
	}
	
	public static ArrayList<GameMap> getSortedArenas() {
		ArrayList<GameMap> sorted = new ArrayList<>(arenas);
		sorted.sort(new GameMapComparator());
		return sorted;
	}

	private void loadMap() {
			WorldManager wm = SkyWarsReloaded.getWM();
			String mapName = name;
			boolean mapExists = false;
	    	File dataDirectory = SkyWarsReloaded.get().getDataFolder();
			File maps = new File (dataDirectory, "maps");
			File source = new File(maps, name);
			String root = SkyWarsReloaded.get().getServer().getWorldContainer().getAbsolutePath();
			File rootDirectory = new File(root);
			File target = new File(rootDirectory, mapName);
			if(target.isDirectory()) {
			    String[] list = target.list();
				if(list != null && list.length > 0) {
		 			mapExists = true;
				}	 
			}
			if (mapExists) {
				SkyWarsReloaded.getWM().deleteWorld(mapName);
			}
			
			wm.copyWorld(source, target);
			
			boolean loaded = SkyWarsReloaded.getWM().loadWorld(mapName, World.Environment.valueOf(environment));
			
			if (loaded) {
				World world = SkyWarsReloaded.get().getServer().getWorld(mapName);
			    world.setAutoSave(false);
			    world.setThundering(false);
			    world.setStorm(false);
			    world.setDifficulty(Difficulty.NORMAL);
			    world.setSpawnLocation(2000, 0, 2000);
			    world.setTicksPerAnimalSpawns(1);
			    world.setTicksPerMonsterSpawns(1);
		        world.setGameRuleValue("doMobSpawning", "false");
		        world.setGameRuleValue("mobGriefing", "false");
		        world.setGameRuleValue("doFireTick", "false");
		        world.setGameRuleValue("showDeathMessages", "false");
		        cage.createSpawnPlatforms(this);
			}
	}
	
	private void ChunkIterator() {
		World chunkWorld;
		chunkWorld = SkyWarsReloaded.get().getServer().getWorld(name);
		int mapSize = SkyWarsReloaded.getCfg().getMaxMapSize();
		int max1 = mapSize/2;
		int min1 = -mapSize/2;
		Block min = chunkWorld.getBlockAt(min1, 0, min1);
		Block max = chunkWorld.getBlockAt(max1, 0, max1);
		Chunk cMin = min.getChunk();
		Chunk cMax = max.getChunk();
		teamCards.clear();
		chests.clear();
		
		for(int cx = cMin.getX(); cx < cMax.getX(); cx++) {
			for(int cz = cMin.getZ(); cz < cMax.getZ(); cz++) {
		           Chunk currentChunk = chunkWorld.getChunkAt(cx, cz);
		           currentChunk.load(true);

		           for(BlockState te : currentChunk.getTileEntities()) {
		               	if(te instanceof Beacon){
			                  Beacon beacon = (Beacon) te;
			                  Block block = beacon.getBlock().getRelative(0, -1, 0);
			                  if(!block.getType().equals(Material.GOLD_BLOCK) && !block.getType().equals(Material.IRON_BLOCK) 
			                		  && !block.getType().equals(Material.DIAMOND_BLOCK)&& !block.getType().equals(Material.EMERALD_BLOCK)) {
				                  Location loc = beacon.getLocation();
				                  addTeamCard(loc);
			                  }
			            } else if (te instanceof Chest) {
				                  Chest chest = (Chest) te;
				                  addChest(chest);
			            } 
		           }
		        }
	     }
		
	}
	
	public static void editMap(GameMap gMap, Player player) {
    	if (gMap.isRegistered()) {
			gMap.unregister();
		}
		String worldName = gMap.getName();
		if (gMap.isEditing()) {
			boolean loaded = false;
			for (World world: SkyWarsReloaded.get().getServer().getWorlds()) {
				if (world.getName().equals(worldName)) {
					loaded = true;
				}
			}
			if (!loaded) {
				loaded = loadWorld(worldName, gMap);
			}
			if (loaded) {
				prepareForEditor(player, gMap, worldName);
			}
		} else {
			gMap.setEditing(true);
			boolean loaded = loadWorld(worldName, gMap);
			if (loaded) {
				prepareForEditor(player, gMap, worldName);
			} else {
				player.sendMessage(new Messaging.MessageFormatter().format("error.map-fail-load"));
			}
		}
	}

	public static boolean loadWorld(String worldName, GameMap gMap) {
		File dataDirectory = new File(SkyWarsReloaded.get().getDataFolder(), "maps");
		File source = new File(dataDirectory, worldName);
		File target = new File(SkyWarsReloaded.get().getServer().getWorldContainer().getAbsolutePath(), worldName);
		boolean mapExists = false;
		if (target.isDirectory()) {
			String[] list = target.list();
			if (list != null && list.length > 0) {
				mapExists = true;
			}
		}
		if (mapExists) {
			SkyWarsReloaded.getWM().deleteWorld(worldName);
		}
		SkyWarsReloaded.getWM().copyWorld(source, target);
		return SkyWarsReloaded.getWM().loadWorld(worldName, World.Environment.valueOf(gMap.environment));
	}

	public static void prepareForEditor(Player player, GameMap gMap, String worldName) {
		World editWorld = SkyWarsReloaded.get().getServer().getWorld(worldName);
		for (TeamCard tCard: gMap.getTeamCards()) {
			if (tCard.getSpawn() != null) {
				editWorld.getBlockAt(tCard.getSpawn().getX(), tCard.getSpawn().getY(), tCard.getSpawn().getZ()).setType(Material.DIAMOND_BLOCK);
			}
		}
		for (CoordLoc cl: gMap.getDeathMatchSpawns()) {
			editWorld.getBlockAt(cl.getX(), cl.getY(), cl.getZ()).setType(Material.EMERALD_BLOCK);
		}
		SkyWarsReloaded.get().getServer().getScheduler().scheduleSyncDelayedTask(SkyWarsReloaded.get(), () -> {
			player.teleport(new Location(editWorld, 0, 95, 0), TeleportCause.PLUGIN);
			player.setGameMode(GameMode.CREATIVE);
			player.setAllowFlight(true);
			player.setFlying(true);
		}, 20);
	}

	public void refreshMap() {
		for (TeamCard tCard: teamCards) {
			tCard.reset();
		}
		thunder = false;
		forceStart = false;
		allowRegen = true;
        kit = null;
        winners.clear();
        deathMatchWaiters.clear();
		if (SkyWarsReloaded.getCfg().kitVotingEnabled()) {
			kitVoteOption.restore();
		}
		for (MatchEvent event: events) {
			event.reset();
		}
		healthOption.restore();
        chestOption.restore();
        timeOption.restore();
        weatherOption.restore();
        modifierOption.restore();
        SkyWarsReloaded.getWM().deleteWorld(name);
        this.loadMap();
        final GameMap gMap = this;
        if (SkyWarsReloaded.get().isEnabled()) {
            new BukkitRunnable() {
				@Override
				public void run() {
					matchState = MatchState.WAITINGSTART;
			        gameboard.updateScoreboard();
			        MatchManager.get().start(gMap);
			        update();
				}
            }.runTaskLater(SkyWarsReloaded.get(), 40);
        }  
	}
	
	/*Inventories*/
	
	public void updateArenaManager() {
		if (SkyWarsReloaded.getIC().has(arenakey)) {
			SkyWarsReloaded.getIC().getMenu(arenakey).update();
		}
	}
	
	public static void openArenasManager(Player player) {
		if (player.hasPermission("sw.arenas")) {
			SkyWarsReloaded.getIC().show(player, "arenasmenu");
		}
	}
	
	public static void updateArenasManager() {
		if (SkyWarsReloaded.getIC().has("arenasmenu")) {
			SkyWarsReloaded.getIC().getMenu("arenasmenu").update();
		}
	}
	
	public void setKitVote(Player player, GameKit kit2) {
		for (TeamCard tCard: teamCards) {
			for (PlayerCard pCard: tCard.getPlayerCards()) {
				if (pCard.getPlayer() != null && pCard.getPlayer().equals(player)) {
					pCard.setKitVote(kit2);
					return;
				}
			}
		}
	}
   
    public GameKit getSelectedKit(Player player) {
    	for (TeamCard tCard: teamCards) {
        	for (PlayerCard pCard: tCard.getPlayerCards()) {
        		if (pCard != null) {
        			if (pCard.getPlayer() != null && pCard.getPlayer().equals(player)) {
        				return pCard.getKitVote();
        			}
        		}
        	}
    	}
    	return null;
    }

	/*Bungeemode Methods*/
	
	private void sendBungeeUpdate() {
		if (SkyWarsReloaded.getCfg().bungeeMode()) {
			String playerCount = "" + this.getAlivePlayers().size();
			String maxPlayers = "" + this.getMaxPlayers();
			String gameStarted = "" + this.matchState.toString();
			ArrayList<String> messages = new ArrayList<>();
			messages.add("ServerUpdate");
			messages.add(SkyWarsReloaded.get().getServerName());
			messages.add(playerCount);
			messages.add(maxPlayers);
			messages.add(gameStarted);
			Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
			if (player != null) {
				SkyWarsReloaded.get().sendSWRMessage(player, SkyWarsReloaded.getCfg().getBungeeLobby(), messages);
			}
		}
	}	
	
    /*Sign Methods*/
	
	private void updateSigns() {
		for (SWRSign s : signs) {
			s.update();
		}
	}
	
	public List<SWRSign> getSigns() {
		return this.signs;
	}
	

	public boolean hasSign(Location loc) {
		for (SWRSign s: signs) {
			if (s.getLocation().equals(loc)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean removeSign(Location loc) {
		SWRSign sign = null;
		for (SWRSign s: signs) {
			if (s.getLocation().equals(loc)) {
				sign = s;
			}
		}
		if (sign != null) {
			signs.remove(sign);
			saveArenaData();
			updateSigns();
			return true;
		}
		return false;
	}
	
	public void addSign(Location loc) {
		signs.add(new SWRSign(name, loc));
		saveArenaData();
		updateSigns();
	}
	
	
	
	/*Getter and Setter Methods*/
        
    public String getDisplayName() {
    	return this.displayName;
    }
    
    public String getDesigner() {
    	return this.designedBy;
    }
    
    public ArrayList<CoordLoc> getChests(){
		return chests;
	}
    
    public MatchState getMatchState() {
        return this.matchState;
    }
    
    public void setMatchState(final MatchState state) {
        this.matchState = state;
    }
    
    public int getPlayerCount() {
    	int count = 0;
    	for (TeamCard tCard: teamCards) {
    		for (PlayerCard pCard: tCard.getPlayerCards()) {
    			if (pCard.getPreElo() != -1) {
    				count++;
    			}
    		}
    	}
		return count;
    }
    
    public int getMinTeams() {
    	if (minPlayers == 0) {
    		return teamCards.size();
    	}
    	return minPlayers;
    }
    
    public void setMinTeams(int x) {
    	minPlayers = x;
    	saveArenaData();
    }
        
    public int getTimer() {
        return this.timer;
    }
    
    public void setTimer(final int lenght) {
        this.timer = lenght;
    }
    
    public GameKit getKit() {
    	return kit;
    }
          
    public String getName() {
        return this.name;
    }
       
	public static void shuffle() {
		Collections.shuffle(arenas);
	}
	
	public void setAllowFallDamage(boolean b) {
		allowFallDamage = b;
	}
   
	public boolean allowFallDamage() {
		return allowFallDamage;
	}

	public ArrayList<UUID> getSpectators() {
		return spectators;
	}
	
	public boolean isThunder() {
		return thunder;
	}

	public void setNextStrike(int randomNum) {
		nextStrike = randomNum;
	}
	
	public int getNextStrike() {
		return nextStrike;
	}
	
	public void setStrikeCounter(int num) {
		strikeCounter = num;
	}
	
	public int getStrikeCounter() {
		return strikeCounter;
	}

	/**Returns the maximum number of players that can join a match*/
	public int getMaxPlayers() {
		return teamCards.size() * teamSize;
	}
	
	public void setThunderStorm(boolean b) {
		this.thunder = b;
	}
	

	public ArrayList<PlayerCard> getPlayerCards() {
		ArrayList<PlayerCard> cards = new ArrayList<>();
		for (TeamCard tCard: teamCards) {
			cards.addAll(tCard.getPlayerCards());
		}
		return cards;
	}
	
	public PlayerCard getPlayerCard(Player player) {
		for (TeamCard tCard: teamCards) {
			for (PlayerCard pCard: tCard.getPlayerCards()) {
				if (pCard.getPlayer() != null && pCard.getPlayer().equals(player)) {
					return pCard;
				}
			}
		}
		return null;
	}
	
	public void setForceStart(boolean state) {
		forceStart = state;
	}
	
	public boolean getForceStart() {
		return forceStart;
	}

	public static GameMap getMapByDisplayName(String name) {
		for (GameMap gMap: arenas) {
			if (ChatColor.stripColor((ChatColor.translateAlternateColorCodes('&', gMap.getDisplayName()))).equalsIgnoreCase(name)) {
				return gMap;
			}
		}
		return null;
	}

	public void setAllowRegen(boolean b) {
		allowRegen = b;
	}

	public boolean allowRegen() {
		return allowRegen;
	}
	
	public void addWinner(String name) {
		winners.add(name);
	}
	
	public boolean isRegistered() {
		return registered;
	}

	public void setRegistered(boolean b) {
		registered = b;
		saveArenaData();
		update();
	}

	public void setCreator(String creator) {
		this.designedBy = creator;
		saveArenaData();
	}

	public void setDisplayName(String displayName2) {
		this.displayName = displayName2;
		saveArenaData();
	}

	public String getArenaKey() {
		return arenakey;
	}

	public void setCurrentChest(String voteString) {
		currentChest = voteString;
	}

	public void setCurrentModifier(String voteString) {
		currentModifier = voteString;
	}

	public void setCurrentTime(String voteString) {
		currentTime = voteString;	
	}
	
	public void setCurrentHealth(String voteString) {
		currentHealth = voteString;	
	}

	public void setCurrentWeather(String voteString) {
		currentWeather = voteString;
	}

	public GameOption getChestOption() {
		return chestOption;
	}

	public GameOption getTimeOption() {
		return timeOption;
	}

	public GameOption getWeatherOption() {
		return weatherOption;
	}

	public GameOption getModifierOption() {
		return modifierOption;
	}

	public void setKit(GameKit voted) {
		this.kit = voted;
	}

	public KitVoteOption getKitVoteOption() {
		return kitVoteOption;
	}

	public GameOption getHealthOption() {
		return healthOption;
	}

	public void setEditing(boolean b) {
		inEditing = b;
	}
	
	public boolean isEditing() {
		return inEditing;
	}
	
	private static void copyDefaults(File mapFile) {
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(mapFile);
		Reader defConfigStream = new InputStreamReader(SkyWarsReloaded.get().getResource("mapFile.yml"));
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        playerConfig.options().copyDefaults(true);
        playerConfig.setDefaults(defConfig);
        try {
            playerConfig.save(mapFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public World getCurrentWorld() {
        World mapWorld;
		mapWorld = SkyWarsReloaded.get().getServer().getWorld(name);
		return mapWorld;
	}

	public void setSpectateSpawn(Location location) {
		spectateSpawn = new CoordLoc(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		saveArenaData();
	}

	public void setTeamSize(int size) {
		teamSize = size;
		for (TeamCard tCard: teamCards) {
			tCard.updateCard(size);
		}
		saveArenaData();
	}

	public void setFriendlyFire(boolean state) {
    	allowFriendlyFire = state;
    	saveArenaData();
	}

	public void addTeamCard(Location loc) {
		addTeamCard(new CoordLoc(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		saveArenaData();
	}
	
	private void addTeamCard(CoordLoc loc) {
		String prefix = "";
		if (teamSize > 1) {
			prefix = getChatColor(teamCards.size());
		}
		teamCards.add(new TeamCard(teamSize, loc, this, prefix, getStringColor(teamCards.size()), teamCards.size() + 1));
	}
	
	private String getChatColor(int size) {
		double d = ((double)size + 1)/14;
		long i = (long) d;
		double f = d - i;
		int s = (int)(f * 14);
		switch(s) {
		case 1: return ChatColor.GREEN + "";
		case 2: return ChatColor.RED + "";
		case 3: return ChatColor.DARK_BLUE + "";
		case 4: return ChatColor.YELLOW + "";
		case 5: return ChatColor.WHITE + "";
		case 6: return ChatColor.AQUA + "";
		case 7: return ChatColor.GRAY + "";
		case 8: return ChatColor.DARK_PURPLE + "";
		case 9: return ChatColor.DARK_GREEN + "";
		case 10: return ChatColor.BLUE + "";
		case 11: return ChatColor.DARK_GRAY + "";
		case 12: return ChatColor.BLACK + "";
		case 13: return ChatColor.LIGHT_PURPLE + "";
		case 14: return ChatColor.GOLD + "";
		default: return ChatColor.GREEN + "";
		}
	}

	private String getStringColor(int size) {
		double d = ((double)size + 1)/14;
		long i = (long) d;
		double f = d - i;
		int s = (int)(f * 14);
		switch(s) {
			case 1: return "Lime";
			case 2: return "Red";
			case 3: return "Blue";
			case 4: return "Yellow";
			case 5: return "White";
			case 6: return "Cyan";
			case 7: return "Light Gray";
			case 8: return "Purple";
			case 9: return "Green";
			case 10: return "Light Blue";
			case 11: return "Gray";
			case 12: return "Black";
			case 13: return "Magenta";
			case 14: return "Orange";
			default: return "Lime";
		}
	}

	public boolean removeTeamCard(Location loc) {
		CoordLoc remove = new CoordLoc(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		TeamCard toRemove = null;
		for (TeamCard tCard: teamCards) {
			if (tCard.getSpawn().equals(remove)) {
				toRemove = tCard;
			}
		}
		if (toRemove != null) {
			teamCards.remove(toRemove);
			return true;
		}
		return false;
	}
	
	public void addDeathMatchSpawn(Location loc) {
		addDeathMatchSpawn(new CoordLoc(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		saveArenaData();
	}
	
	private void addDeathMatchSpawn(CoordLoc loc) {
		deathMatchSpawns.add(loc);
	}
	
	public boolean removeDeathMatchSpawn(Location loc) {
		CoordLoc remove = new CoordLoc(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		return deathMatchSpawns.remove(remove);
	}
	
	public void addChest(Chest chest) {
		InventoryHolder ih = chest.getInventory().getHolder();
        if (ih instanceof DoubleChest) {
        	DoubleChest dc = (DoubleChest) ih;
			Chest left = (Chest) dc.getLeftSide();
			Chest right = (Chest) dc.getRightSide();
			CoordLoc locLeft = new CoordLoc(left.getX(), left.getY(), left.getZ());
			CoordLoc locRight = new CoordLoc(right.getX(), right.getY(), right.getZ());
			if (!(chests.contains(locLeft) || chests.contains(locRight))) {
				addChest(locLeft);
			}
        } else {
        	CoordLoc loc = new CoordLoc(chest.getX(), chest.getY(), chest.getZ());
            if (!chests.contains(loc)){
      	  		addChest(loc);
            }
        }
	}
	
	private void addChest(CoordLoc loc) {
		chests.add(loc);
	}
	
	public void removeChest(Chest chest) {
		InventoryHolder ih = chest.getInventory().getHolder();
		if (ih instanceof DoubleChest) {
			DoubleChest dc = (DoubleChest) ih;
			Chest left = (Chest) dc.getLeftSide();
			Chest right = (Chest) dc.getRightSide();
			CoordLoc locLeft = new CoordLoc(left.getX(), left.getY(), left.getZ());
			CoordLoc locRight = new CoordLoc(right.getX(), right.getY(), right.getZ());
			if (chests.contains(locLeft)) {
				chests.remove(locLeft);
			}
			if (chests.contains(locRight)) {
				chests.remove(locRight);
			}
		} else {
			CoordLoc loc = new CoordLoc(chest.getX(), chest.getY(), chest.getZ());
			if (chests.contains(loc)) {
				chests.remove(loc);
			}
		}

	}

	public CoordLoc getSpectateSpawn() {
		return spectateSpawn;
	}

	public void saveMap(Player mess) {
        boolean success = false;
		Location respawn = SkyWarsReloaded.getCfg().getSpawn();
		for (World world: SkyWarsReloaded.get().getServer().getWorlds()) {
			if (world.getName().equals(name)) {
				World editWorld = SkyWarsReloaded.get().getServer().getWorld(world.getName());
				for (Player player: editWorld.getPlayers()) {
					player.teleport(respawn, TeleportCause.PLUGIN);
				}					
				editWorld.save();
				SkyWarsReloaded.getWM().unloadWorld(world.getName());
				File dataDirectory = new File (SkyWarsReloaded.get().getDataFolder(), "maps");
				File target = new File (dataDirectory, world.getName());
				SkyWarsReloaded.getWM().deleteWorld(target);
				File source = new File (SkyWarsReloaded.get().getServer().getWorldContainer().getAbsolutePath(), world.getName());
				SkyWarsReloaded.getWM().copyWorld(source, target);
				SkyWarsReloaded.getWM().deleteWorld(source);
				if (mess != null) {
					mess.sendMessage(new Messaging.MessageFormatter().setVariable("mapname", world.getName()).format("maps.saved"));
					mess.sendMessage(new Messaging.MessageFormatter().format("maps.register-reminder"));
				}
				saveArenaData();
				inEditing = false;
				success = true;
				break;
			} 	
		}
		if (mess != null && !success) {
			mess.sendMessage(new Messaging.MessageFormatter().setVariable("mapname", name).format("error.map-not-in-edit"));
		}
	}

	public ArrayList<MatchEvent> getEvents() {
		return events;
	}

	public  ArrayList<CoordLoc> getDeathMatchSpawns() {
		return deathMatchSpawns;
	}

	public void removeDMSpawnBlocks() {
		for (CoordLoc loc: deathMatchSpawns) {
			World world = getCurrentWorld();
			Location loca = new Location(world, loc.getX(), loc.getY(), loc.getZ());
			world.getBlockAt(loca).setType(Material.AIR);
		}
	}
	
	public ArrayList<String> getDeathMatchWaiters() {
	   	return deathMatchWaiters;
	}
	    
	public void addDeathMatchWaiter(Player player) {
	  	if (player != null) {
	       	deathMatchWaiters.add(player.getUniqueId().toString());
	   	}
	}
	    
	public void clearDeathMatchWaiters() {
		deathMatchWaiters.clear();	
	}

	public ArrayList<String> getAnvils() {
		return anvils;
	}
	
	public void addCrate(Location loc, int max) {
		crates.add(new Crate(loc, max));
	}
	
	public void removeCrates() {
		for (Crate crate: crates) {
			if (crate.getLocation() != null) {
				crate.getLocation().getWorld().getBlockAt(crate.getLocation()).setType(Material.AIR);
			}
		}
		crates.clear();
	}

	public ArrayList<Crate> getCrates() {
		return crates;
	}

	public Cage getCage() {
		return cage;
	}

	public void setCage(CageType next) {
        switch(next) {
        case CUBE: this.cage = new CubeCage();
        break;
        case DOME: this.cage = new DomeCage();
		break;
        case PYRAMID: this.cage = new PyramidCage();
		break;
        case SPHERE: this.cage = new SphereCage();
		break;
        case STANDARD: this.cage = new StandardCage();
		break;
		default: this.cage = new StandardCage();
        }
        saveArenaData();
	}

	public ArrayList<TeamCard> getTeamCards() {
		return teamCards;
	}

	public GameQueue getJoinQueue() {
		return joinQueue;
	}

	public TeamCard getTeamCard(Player player) {
		for (TeamCard tCard: teamCards) {
			if (tCard.containsPlayer(player.getUniqueId()) != null) {
				return tCard;
			}
		}
		return null;
	}

	public int getTeamCount() {
		int count = 0;
		for (TeamCard tCard: teamCards) {
			if (tCard.getPlayersSize() > 0) {
				count++;
			}
		}
		return count;
	}

	public int getTeamsOut() {
		int count = 0;
		for (TeamCard tCard: teamCards) {
			if (tCard.isElmininated()) {
				count++;
			}
		}
		return count;
	}
	
	public int getTeamsleft() {
		return teamCards.size() - getTeamsOut();
	}

	public int getTeamSize() {
		return teamSize;
	}

	public TeamCard getWinningTeam() {
		for (TeamCard tCard: teamCards) {
			if (!tCard.isElmininated()) {
				return tCard;
			}
		}
		return null;
	}

	public int getFullTeams() {
		int count = 0;
		for (TeamCard tCard: teamCards) {
			if (tCard.isFull()) {
				count++;
			}
		}
		return count;
	}

	public String getCurrentChest() {
		return currentChest;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public String getCurrentHealth() {
		return currentHealth;
	}

	public String getCurrentWeather() {
		return currentWeather;
	}

	public String getCurrentModifier() {
		return currentModifier;
	}

	public List<String> getWinners() {
		return winners;
	}

	public GameBoard getGameBoard() {
		return gameboard;
	}

    public TeamCard getTeamCardFromName(String name) {
		for (TeamCard tCard: teamCards) {
			if (tCard.getTeamName().equalsIgnoreCase(name)) {
				return tCard;
			}
		}
		return null;
    }

    public class TeamCardComparator implements Comparator<TeamCard> {
		@Override
	    public int compare(TeamCard f1, TeamCard f2) {
			return Integer.compare(f1.getFullCount(), f2.getFullCount());
	    }
	}

	public boolean allowFriendlyFire() {
    	return allowFriendlyFire;
	}
}


