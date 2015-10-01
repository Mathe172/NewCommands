package net.minecraft.server.dedicated;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommand;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.rcon.IServer;
import net.minecraft.network.rcon.RConThreadMain;
import net.minecraft.network.rcon.RConThreadQuery;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerEula;
import net.minecraft.server.gui.MinecraftServerGui;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.CryptManager;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

public class DedicatedServer extends MinecraftServer implements IServer
{
	private static final Logger logger = LogManager.getLogger();
	private final List<ServerCommand> pendingCommandList = Collections.synchronizedList(new ArrayList<ServerCommand>());
	private RConThreadQuery theRConThreadQuery;
	private RConThreadMain theRConThreadMain;
	private PropertyManager settings;
	private ServerEula eula;
	private boolean canSpawnStructures;
	private WorldSettings.GameType gameType;
	private boolean guiIsEnabled;
	private static final String __OBFID = "CL_00001784";
	
	public DedicatedServer(final File workDir)
	{
		super(workDir, Proxy.NO_PROXY, USER_CACHE_FILE);
		final Thread var10001 = new Thread("Server Infinisleeper")
		{
			private static final String __OBFID = "CL_00001787";
			{
				this.setDaemon(true);
				this.start();
			}
			
			@Override
			public void run()
			{
				while (true)
					try
					{
						while (true)
							Thread.sleep(2147483647L);
					} catch (final InterruptedException var2)
					{
						;
					}
			}
		};
	}
	
	/**
	 * Initialises the server and starts it.
	 */
	@Override
	protected boolean startServer() throws IOException
	{
		final Thread var1 = new Thread("Server console handler")
		{
			private static final String __OBFID = "CL_00001786";
			
			@Override
			public void run()
			{
				final BufferedReader var1 = new BufferedReader(new InputStreamReader(System.in));
				String var2;
				
				try
				{
					while (!DedicatedServer.this.isServerStopped() && DedicatedServer.this.isServerRunning() && (var2 = var1.readLine()) != null)
						DedicatedServer.this.addPendingCommand(var2, DedicatedServer.this);
				} catch (final IOException var4)
				{
					DedicatedServer.logger.error("Exception handling console input", var4);
				}
			}
		};
		var1.setDaemon(true);
		var1.start();
		logger.info("Starting minecraft server version 1.8");
		
		if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L)
			logger.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
		
		logger.info("Loading properties");
		this.settings = new PropertyManager(new File("server.properties"));
		this.eula = new ServerEula(new File("eula.txt"));
		
		if (!this.eula.hasAcceptedEULA())
		{
			logger.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
			this.eula.createEULAFile();
			return false;
		}
		else
		{
			if (this.isSinglePlayer())
				this.setHostname("127.0.0.1");
			else
			{
				this.setOnlineMode(this.settings.getBooleanProperty("online-mode", true));
				this.setHostname(this.settings.getStringProperty("server-ip", ""));
			}
			
			this.setCanSpawnAnimals(this.settings.getBooleanProperty("spawn-animals", true));
			this.setCanSpawnNPCs(this.settings.getBooleanProperty("spawn-npcs", true));
			this.setAllowPvp(this.settings.getBooleanProperty("pvp", true));
			this.setAllowFlight(this.settings.getBooleanProperty("allow-flight", false));
			this.setResourcePack(this.settings.getStringProperty("resource-pack", ""), this.settings.getStringProperty("resource-pack-hash", ""));
			this.setMOTD(this.settings.getStringProperty("motd", "A Minecraft Server"));
			this.setForceGamemode(this.settings.getBooleanProperty("force-gamemode", false));
			this.setPlayerIdleTimeout(this.settings.getIntProperty("player-idle-timeout", 0));
			
			if (this.settings.getIntProperty("difficulty", 1) < 0)
				this.settings.setProperty("difficulty", Integer.valueOf(0));
			else if (this.settings.getIntProperty("difficulty", 1) > 3)
				this.settings.setProperty("difficulty", Integer.valueOf(3));
			
			this.canSpawnStructures = this.settings.getBooleanProperty("generate-structures", true);
			final int var2 = this.settings.getIntProperty("gamemode", WorldSettings.GameType.SURVIVAL.getID());
			this.gameType = WorldSettings.getGameTypeById(var2);
			logger.info("Default game type: " + this.gameType);
			InetAddress var3 = null;
			
			if (this.getServerHostname().length() > 0)
				var3 = InetAddress.getByName(this.getServerHostname());
			
			if (this.getServerPort() < 0)
				this.setServerPort(this.settings.getIntProperty("server-port", 25565));
			
			logger.info("Generating keypair");
			this.setKeyPair(CryptManager.generateKeyPair());
			logger.info("Starting Minecraft server on " + (this.getServerHostname().length() == 0 ? "*" : this.getServerHostname()) + ":" + this.getServerPort());
			
			try
			{
				this.getNetworkSystem().addLanEndpoint(var3, this.getServerPort());
			} catch (final IOException var17)
			{
				logger.warn("**** FAILED TO BIND TO PORT!");
				logger.warn("The exception was: {}", var17.toString());
				logger.warn("Perhaps a server is already running on that port?");
				return false;
			}
			
			if (!this.isServerInOnlineMode())
			{
				logger.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
				logger.warn("The server will make no attempt to authenticate usernames. Beware.");
				logger.warn("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
				logger.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
			}
			
			if (this.convertFiles())
				this.getPlayerProfileCache().func_152658_c();
			
			if (!PreYggdrasilConverter.tryConvert(this.settings))
				return false;
			else
			{
				this.setConfigManager(new DedicatedPlayerList(this));
				final long var4 = System.nanoTime();
				
				if (this.getFolderName() == null)
					this.setFolderName(this.settings.getStringProperty("level-name", "world"));
				
				final String var6 = this.settings.getStringProperty("level-seed", "");
				final String var7 = this.settings.getStringProperty("level-type", "DEFAULT");
				final String var8 = this.settings.getStringProperty("generator-settings", "");
				long var9 = (new Random()).nextLong();
				
				if (var6.length() > 0)
					try
					{
						final long var11 = Long.parseLong(var6);
						
						if (var11 != 0L)
							var9 = var11;
					} catch (final NumberFormatException var16)
					{
						var9 = var6.hashCode();
					}
				
				WorldType var18 = WorldType.parseWorldType(var7);
				
				if (var18 == null)
					var18 = WorldType.DEFAULT;
				
				this.isAnnouncingPlayerAchievements();
				this.isCommandBlockEnabled();
				this.getOpPermissionLevel();
				this.isSnooperEnabled();
				this.getNetworkCompressionTreshold();
				this.setBuildLimit(this.settings.getIntProperty("max-build-height", 256));
				this.setBuildLimit((this.getBuildLimit() + 8) / 16 * 16);
				this.setBuildLimit(MathHelper.clamp_int(this.getBuildLimit(), 64, 256));
				this.settings.setProperty("max-build-height", Integer.valueOf(this.getBuildLimit()));
				logger.info("Preparing level \"" + this.getFolderName() + "\"");
				this.loadAllWorlds(this.getFolderName(), this.getFolderName(), var9, var18, var8);
				final long var12 = System.nanoTime() - var4;
				final String var14 = String.format("%.3fs", new Object[] { Double.valueOf(var12 / 1.0E9D) });
				logger.info("Done (" + var14 + ")! For help, type \"help\" or \"?\"");
				
				if (this.settings.getBooleanProperty("enable-query", false))
				{
					logger.info("Starting GS4 status listener");
					this.theRConThreadQuery = new RConThreadQuery(this);
					this.theRConThreadQuery.startThread();
				}
				
				if (this.settings.getBooleanProperty("enable-rcon", false))
				{
					logger.info("Starting remote control listener");
					this.theRConThreadMain = new RConThreadMain(this);
					this.theRConThreadMain.startThread();
				}
				
				if (this.getMaxTickTime() > 0L)
				{
					final Thread var15 = new Thread(new ServerHangWatchdog(this));
					var15.setName("Server Watchdog");
					var15.setDaemon(true);
					var15.start();
				}
				
				return true;
			}
		}
	}
	
	/**
	 * Sets the game type for all worlds.
	 */
	@Override
	public void setGameType(final WorldSettings.GameType gameMode)
	{
		super.setGameType(gameMode);
		this.gameType = gameMode;
	}
	
	@Override
	public boolean canStructuresSpawn()
	{
		return this.canSpawnStructures;
	}
	
	@Override
	public WorldSettings.GameType getGameType()
	{
		return this.gameType;
	}
	
	/**
	 * Get the server's difficulty
	 */
	@Override
	public EnumDifficulty getDifficulty()
	{
		return EnumDifficulty.getDifficultyEnum(this.settings.getIntProperty("difficulty", 1));
	}
	
	/**
	 * Defaults to false.
	 */
	@Override
	public boolean isHardcore()
	{
		return this.settings.getBooleanProperty("hardcore", false);
	}
	
	/**
	 * Called on exit from the main run() loop.
	 */
	@Override
	protected void finalTick(final CrashReport report)
	{
		while (this.isServerRunning())
		{
			this.executePendingCommands();
			
			try
			{
				Thread.sleep(10L);
			} catch (final InterruptedException var3)
			{
				;
			}
		}
	}
	
	/**
	 * Adds the server info, including from theWorldServer, to the crash report.
	 */
	@Override
	public CrashReport addServerInfoToCrashReport(CrashReport report)
	{
		report = super.addServerInfoToCrashReport(report);
		report.getCategory().addCrashSectionCallable("Is Modded", new Callable()
		{
			private static final String __OBFID = "CL_00001785";
			
			@Override
			public String call()
			{
				final String var1 = DedicatedServer.this.getServerModName();
				return !var1.equals("vanilla") ? "Definitely; Server brand changed to \'" + var1 + "\'" : "Unknown (can\'t tell)";
			}
		});
		report.getCategory().addCrashSectionCallable("Type", new Callable()
		{
			private static final String __OBFID = "CL_00001788";
			
			@Override
			public String call()
			{
				return "Dedicated Server (map_server.txt)";
			}
		});
		return report;
	}
	
	/**
	 * Directly calls System.exit(0), instantly killing the program.
	 */
	@Override
	protected void systemExitNow()
	{
		System.exit(0);
	}
	
	@Override
	public void updateTimeLightAndEntities()
	{
		super.updateTimeLightAndEntities();
		this.executePendingCommands();
	}
	
	@Override
	public boolean getAllowNether()
	{
		return this.settings.getBooleanProperty("allow-nether", true);
	}
	
	@Override
	public boolean allowSpawnMonsters()
	{
		return this.settings.getBooleanProperty("spawn-monsters", true);
	}
	
	@Override
	public void addServerStatsToSnooper(final PlayerUsageSnooper playerSnooper)
	{
		playerSnooper.addClientStat("whitelist_enabled", Boolean.valueOf(this.func_180508_aN().isWhiteListEnabled()));
		playerSnooper.addClientStat("whitelist_count", Integer.valueOf(this.func_180508_aN().getWhitelistedPlayerNames().length));
		super.addServerStatsToSnooper(playerSnooper);
	}
	
	/**
	 * Returns whether snooping is enabled or not.
	 */
	@Override
	public boolean isSnooperEnabled()
	{
		return this.settings.getBooleanProperty("snooper-enabled", true);
	}
	
	public void addPendingCommand(final String input, final ICommandSender sender)
	{
		this.pendingCommandList.add(new ServerCommand(input, sender));
	}
	
	public void executePendingCommands()
	{
		while (!this.pendingCommandList.isEmpty())
		{
			final ServerCommand var1 = this.pendingCommandList.remove(0);
			CommandHandler.executeCommand(var1.sender, var1.command.get(), 0);
		}
	}
	
	@Override
	public boolean isDedicatedServer()
	{
		return true;
	}
	
	public DedicatedPlayerList func_180508_aN()
	{
		return (DedicatedPlayerList) super.getConfigurationManager();
	}
	
	/**
	 * Gets an integer property. If it does not exist, set it to the specified value.
	 */
	@Override
	public int getIntProperty(final String key, final int defaultValue)
	{
		return this.settings.getIntProperty(key, defaultValue);
	}
	
	/**
	 * Gets a string property. If it does not exist, set it to the specified value.
	 */
	@Override
	public String getStringProperty(final String key, final String defaultValue)
	{
		return this.settings.getStringProperty(key, defaultValue);
	}
	
	/**
	 * Gets a boolean property. If it does not exist, set it to the specified value.
	 */
	public boolean getBooleanProperty(final String key, final boolean defaultValue)
	{
		return this.settings.getBooleanProperty(key, defaultValue);
	}
	
	/**
	 * Saves an Object with the given property name.
	 */
	@Override
	public void setProperty(final String key, final Object value)
	{
		this.settings.setProperty(key, value);
	}
	
	/**
	 * Saves all of the server properties to the properties file.
	 */
	@Override
	public void saveProperties()
	{
		this.settings.saveProperties();
	}
	
	/**
	 * Returns the filename where server properties are stored
	 */
	@Override
	public String getSettingsFilename()
	{
		final File var1 = this.settings.getPropertiesFile();
		return var1 != null ? var1.getAbsolutePath() : "No settings file";
	}
	
	public void setGuiEnabled()
	{
		MinecraftServerGui.createServerGui(this);
		this.guiIsEnabled = true;
	}
	
	@Override
	public boolean getGuiEnabled()
	{
		return this.guiIsEnabled;
	}
	
	/**
	 * On dedicated does nothing. On integrated, sets commandsAllowedForAll, gameType and allows external connections.
	 */
	@Override
	public String shareToLAN(final WorldSettings.GameType type, final boolean allowCheats)
	{
		return "";
	}
	
	/**
	 * Return whether command blocks are enabled.
	 */
	@Override
	public boolean isCommandBlockEnabled()
	{
		return this.settings.getBooleanProperty("enable-command-block", false);
	}
	
	/**
	 * Return the spawn protection area's size.
	 */
	@Override
	public int getSpawnProtectionSize()
	{
		return this.settings.getIntProperty("spawn-protection", super.getSpawnProtectionSize());
	}
	
	@Override
	public boolean isBlockProtected(final World worldIn, final BlockPos pos, final EntityPlayer playerIn)
	{
		if (worldIn.provider.getDimensionId() != 0)
			return false;
		else if (this.func_180508_aN().getOppedPlayers().hasEntries())
			return false;
		else if (this.func_180508_aN().canSendCommands(playerIn.getGameProfile()))
			return false;
		else if (this.getSpawnProtectionSize() <= 0)
			return false;
		else
		{
			final BlockPos var4 = worldIn.getSpawnPoint();
			final int var5 = MathHelper.abs_int(pos.getX() - var4.getX());
			final int var6 = MathHelper.abs_int(pos.getZ() - var4.getZ());
			final int var7 = Math.max(var5, var6);
			return var7 <= this.getSpawnProtectionSize();
		}
	}
	
	@Override
	public int getOpPermissionLevel()
	{
		return this.settings.getIntProperty("op-permission-level", 4);
	}
	
	@Override
	public void setPlayerIdleTimeout(final int idleTimeout)
	{
		super.setPlayerIdleTimeout(idleTimeout);
		this.settings.setProperty("player-idle-timeout", Integer.valueOf(idleTimeout));
		this.saveProperties();
	}
	
	@Override
	public boolean isAnnouncingPlayerAchievements()
	{
		return this.settings.getBooleanProperty("announce-player-achievements", true);
	}
	
	@Override
	public int getMaxWorldSize()
	{
		int var1 = this.settings.getIntProperty("max-world-size", super.getMaxWorldSize());
		
		if (var1 < 1)
			var1 = 1;
		else if (var1 > super.getMaxWorldSize())
			var1 = super.getMaxWorldSize();
		
		return var1;
	}
	
	/**
	 * The compression treshold. If the packet is larger than the specified amount of bytes, it will be compressed
	 */
	@Override
	public int getNetworkCompressionTreshold()
	{
		return this.settings.getIntProperty("network-compression-threshold", super.getNetworkCompressionTreshold());
	}
	
	protected boolean convertFiles() throws IOException
	{
		boolean var2 = false;
		int var1;
		
		for (var1 = 0; !var2 && var1 <= 2; ++var1)
		{
			if (var1 > 0)
			{
				logger.warn("Encountered a problem while converting the user banlist, retrying in a few seconds");
				this.sleepFiveSeconds();
			}
			
			var2 = PreYggdrasilConverter.convertUserBanlist(this);
		}
		
		boolean var3 = false;
		
		for (var1 = 0; !var3 && var1 <= 2; ++var1)
		{
			if (var1 > 0)
			{
				logger.warn("Encountered a problem while converting the ip banlist, retrying in a few seconds");
				this.sleepFiveSeconds();
			}
			
			var3 = PreYggdrasilConverter.convertIpBanlist(this);
		}
		
		boolean var4 = false;
		
		for (var1 = 0; !var4 && var1 <= 2; ++var1)
		{
			if (var1 > 0)
			{
				logger.warn("Encountered a problem while converting the op list, retrying in a few seconds");
				this.sleepFiveSeconds();
			}
			
			var4 = PreYggdrasilConverter.convertOplist(this);
		}
		
		boolean var5 = false;
		
		for (var1 = 0; !var5 && var1 <= 2; ++var1)
		{
			if (var1 > 0)
			{
				logger.warn("Encountered a problem while converting the whitelist, retrying in a few seconds");
				this.sleepFiveSeconds();
			}
			
			var5 = PreYggdrasilConverter.convertWhitelist(this);
		}
		
		boolean var6 = false;
		
		for (var1 = 0; !var6 && var1 <= 2; ++var1)
		{
			if (var1 > 0)
			{
				logger.warn("Encountered a problem while converting the player save files, retrying in a few seconds");
				this.sleepFiveSeconds();
			}
			
			var6 = PreYggdrasilConverter.convertSaveFiles(this, this.settings);
		}
		
		return var2 || var3 || var4 || var5 || var6;
	}
	
	private void sleepFiveSeconds()
	{
		try
		{
			Thread.sleep(5000L);
		} catch (final InterruptedException var2)
		{
			;
		}
	}
	
	public long getMaxTickTime()
	{
		return this.settings.getLongProperty("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
	}
	
	@Override
	public ServerConfigurationManager getConfigurationManager()
	{
		return this.func_180508_aN();
	}
}
