package net.minecraft.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.imageio.ImageIO;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.command.completion.ITabCompletion;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.command.completion.TabCompletionData.Weighted;
import net.minecraft.command.parser.CompletionParser.CompletionData;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.parser.ParsingManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Bootstrap;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.profiler.IPlayerUsage;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

public abstract class MinecraftServer implements ICommandSender, Runnable, IThreadListener, IPlayerUsage
{
	private static final Logger logger = LogManager.getLogger();
	public static final File USER_CACHE_FILE = new File("usercache.json");
	
	/** Instance of Minecraft Server. */
	private static MinecraftServer mcServer;
	private final ISaveFormat anvilConverterForAnvilFile;
	
	/** The PlayerUsageSnooper instance. */
	private final PlayerUsageSnooper usageSnooper = new PlayerUsageSnooper("server", this, getCurrentTimeMillis());
	private final File anvilFile;
	
	/** List of names of players who are online. */
	private final List playersOnline = Lists.newArrayList();
	private final ICommandManager commandManager;
	public final Profiler theProfiler = new Profiler();
	private final NetworkSystem networkSystem;
	private final ServerStatusResponse statusResponse = new ServerStatusResponse();
	private final Random random = new Random();
	
	/** The server's hostname. */
	private String hostname;
	
	/** The server's port. */
	private int serverPort = -1;
	
	/** The server world instances. */
	public WorldServer[] worldServers;
	
	/** The ServerConfigurationManager instance. */
	private ServerConfigurationManager serverConfigManager;
	
	/**
	 * Indicates whether the server is running or not. Set to false to initiate a shutdown.
	 */
	private boolean serverRunning = true;
	
	/** Indicates to other classes that the server is safely stopped. */
	private boolean serverStopped;
	
	/** Incremented every tick. */
	private int tickCounter;
	protected final Proxy serverProxy;
	
	/**
	 * The task the server is currently working on(and will output on outputPercentRemaining).
	 */
	public String currentTask;
	
	/** The percentage of the current task finished so far. */
	public int percentDone;
	
	/** True if the server is in online mode. */
	private boolean onlineMode;
	
	/** True if the server has animals turned on. */
	private boolean canSpawnAnimals;
	private boolean canSpawnNPCs;
	
	/** Indicates whether PvP is active on the server or not. */
	private boolean pvpEnabled;
	
	/** Determines if flight is allowed or not. */
	private boolean allowFlight;
	
	/** The server MOTD string. */
	private String motd;
	
	/** Maximum build height. */
	private int buildLimit;
	private int maxPlayerIdleMinutes = 0;
	public final long[] tickTimeArray = new long[100];
	
	/** Stats are [dimension][tick%100] system.nanoTime is stored. */
	public long[][] timeOfLastDimensionTick;
	private KeyPair serverKeyPair;
	
	/** Username of the server owner (for integrated servers) */
	private String serverOwner;
	private String folderName;
	private boolean isDemo;
	private boolean enableBonusChest;
	
	/**
	 * If true, there is no need to save chunks or stop the server, because that is already being done.
	 */
	private boolean worldIsBeingDeleted;
	
	/** The texture pack for the server */
	private String resourcePackUrl = "";
	private String resourcePackHash = "";
	private boolean serverIsRunning;
	
	/**
	 * Set when warned for "Can't keep up", which triggers again after 15 seconds.
	 */
	private long timeOfLastWarning;
	private String userMessage;
	private boolean startProfiling;
	private boolean isGamemodeForced;
	private final YggdrasilAuthenticationService authService;
	private final MinecraftSessionService sessionService;
	private long nanoTimeSinceStatusRefresh = 0L;
	private final GameProfileRepository profileRepo;
	private final PlayerProfileCache profileCache;
	protected final Queue futureTaskQueue = Queues.newArrayDeque();
	private Thread serverThread;
	private long currentTime = getCurrentTimeMillis();
	private static final String __OBFID = "CL_00001462";
	
	public MinecraftServer(final File workDir, final Proxy proxy, final File profileCacheDir)
	{
		this.serverProxy = proxy;
		mcServer = this;
		this.anvilFile = workDir;
		this.networkSystem = new NetworkSystem(this);
		this.profileCache = new PlayerProfileCache(this, profileCacheDir);
		this.commandManager = this.createNewCommandManager();
		this.anvilConverterForAnvilFile = new AnvilSaveConverter(workDir);
		this.authService = new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString());
		this.sessionService = this.authService.createMinecraftSessionService();
		this.profileRepo = this.authService.createProfileRepository();
	}
	
	protected ServerCommandManager createNewCommandManager()
	{
		return new ServerCommandManager();
	}
	
	/**
	 * Initialises the server and starts it.
	 */
	protected abstract boolean startServer() throws IOException;
	
	protected void convertMapIfNeeded(final String worldNameIn)
	{
		if (this.getActiveAnvilConverter().isOldMapFormat(worldNameIn))
		{
			logger.info("Converting map!");
			this.setUserMessage("menu.convertingLevel");
			this.getActiveAnvilConverter().convertMapFormat(worldNameIn, new IProgressUpdate()
			{
				private long startTime = System.currentTimeMillis();
				private static final String __OBFID = "CL_00001417";
				
				@Override
				public void displaySavingString(final String message)
				{
				}
				
				@Override
				public void setLoadingProgress(final int progress)
				{
					if (System.currentTimeMillis() - this.startTime >= 1000L)
					{
						this.startTime = System.currentTimeMillis();
						MinecraftServer.logger.info("Converting... " + progress + "%");
					}
				}
				
				@Override
				public void displayLoadingString(final String message)
				{
				}
			});
		}
	}
	
	/**
	 * Typically "menu.convertingLevel", "menu.loadingLevel" or others.
	 */
	protected synchronized void setUserMessage(final String message)
	{
		this.userMessage = message;
	}
	
	protected void loadAllWorlds(final String p_71247_1_, final String p_71247_2_, final long seed, final WorldType type, final String p_71247_6_)
	{
		this.convertMapIfNeeded(p_71247_1_);
		this.setUserMessage("menu.loadingLevel");
		this.worldServers = new WorldServer[3];
		this.timeOfLastDimensionTick = new long[this.worldServers.length][100];
		final ISaveHandler var7 = this.anvilConverterForAnvilFile.getSaveLoader(p_71247_1_, true);
		this.setResourcePackFromWorld(this.getFolderName(), var7);
		WorldInfo var9 = var7.loadWorldInfo();
		WorldSettings var8;
		
		if (var9 == null)
		{
			if (this.isDemo())
			{
				var8 = DemoWorldServer.demoWorldSettings;
			}
			else
			{
				var8 = new WorldSettings(seed, this.getGameType(), this.canStructuresSpawn(), this.isHardcore(), type);
				var8.setWorldName(p_71247_6_);
				
				if (this.enableBonusChest)
				{
					var8.enableBonusChest();
				}
			}
			
			var9 = new WorldInfo(var8, p_71247_2_);
		}
		else
		{
			var9.setWorldName(p_71247_2_);
			var8 = new WorldSettings(var9);
		}
		
		for (int var10 = 0; var10 < this.worldServers.length; ++var10)
		{
			byte var11 = 0;
			
			if (var10 == 1)
			{
				var11 = -1;
			}
			
			if (var10 == 2)
			{
				var11 = 1;
			}
			
			if (var10 == 0)
			{
				if (this.isDemo())
				{
					this.worldServers[var10] = (WorldServer) (new DemoWorldServer(this, var7, var9, var11, this.theProfiler)).init();
				}
				else
				{
					this.worldServers[var10] = (WorldServer) (new WorldServer(this, var7, var9, var11, this.theProfiler)).init();
				}
				
				this.worldServers[var10].initialize(var8);
			}
			else
			{
				this.worldServers[var10] = (WorldServer) (new WorldServerMulti(this, var7, var11, this.worldServers[0], this.theProfiler)).init();
			}
			
			this.worldServers[var10].addWorldAccess(new WorldManager(this, this.worldServers[var10]));
			
			if (!this.isSinglePlayer())
			{
				this.worldServers[var10].getWorldInfo().setGameType(this.getGameType());
			}
		}
		
		this.serverConfigManager.setPlayerManager(this.worldServers);
		this.setDifficultyForAllWorlds(this.getDifficulty());
		this.initialWorldChunkLoad();
	}
	
	protected void initialWorldChunkLoad()
	{
		final boolean var1 = true;
		final boolean var2 = true;
		final boolean var3 = true;
		final boolean var4 = true;
		int var5 = 0;
		this.setUserMessage("menu.generatingTerrain");
		final byte var6 = 0;
		logger.info("Preparing start region for level " + var6);
		final WorldServer var7 = this.worldServers[var6];
		final BlockPos var8 = var7.getSpawnPoint();
		long var9 = getCurrentTimeMillis();
		
		for (int var11 = -192; var11 <= 192 && this.isServerRunning(); var11 += 16)
		{
			for (int var12 = -192; var12 <= 192 && this.isServerRunning(); var12 += 16)
			{
				final long var13 = getCurrentTimeMillis();
				
				if (var13 - var9 > 1000L)
				{
					this.outputPercentRemaining("Preparing spawn area", var5 * 100 / 625);
					var9 = var13;
				}
				
				++var5;
				var7.theChunkProviderServer.loadChunk(var8.getX() + var11 >> 4, var8.getZ() + var12 >> 4);
			}
		}
		
		this.clearCurrentTask();
	}
	
	protected void setResourcePackFromWorld(final String worldNameIn, final ISaveHandler saveHandlerIn)
	{
		final File var3 = new File(saveHandlerIn.getWorldDirectory(), "resources.zip");
		
		if (var3.isFile())
		{
			this.setResourcePack("level://" + worldNameIn + "/" + var3.getName(), "");
		}
	}
	
	public abstract boolean canStructuresSpawn();
	
	public abstract WorldSettings.GameType getGameType();
	
	/**
	 * Get the server's difficulty
	 */
	public abstract EnumDifficulty getDifficulty();
	
	/**
	 * Defaults to false.
	 */
	public abstract boolean isHardcore();
	
	public abstract int getOpPermissionLevel();
	
	/**
	 * Used to display a percent remaining given text and the percentage.
	 */
	protected void outputPercentRemaining(final String message, final int percent)
	{
		this.currentTask = message;
		this.percentDone = percent;
		logger.info(message + ": " + percent + "%");
	}
	
	/**
	 * Set current task to null and set its percentage to 0.
	 */
	protected void clearCurrentTask()
	{
		this.currentTask = null;
		this.percentDone = 0;
	}
	
	/**
	 * par1 indicates if a log message should be output.
	 */
	protected void saveAllWorlds(final boolean dontLog)
	{
		if (!this.worldIsBeingDeleted)
		{
			final WorldServer[] var2 = this.worldServers;
			final int var3 = var2.length;
			
			for (int var4 = 0; var4 < var3; ++var4)
			{
				final WorldServer var5 = var2[var4];
				
				if (var5 != null)
				{
					if (!dontLog)
					{
						logger.info("Saving chunks for level \'" + var5.getWorldInfo().getWorldName() + "\'/" + var5.provider.getDimensionName());
					}
					
					try
					{
						var5.saveAllChunks(true, (IProgressUpdate) null);
					} catch (final MinecraftException var7)
					{
						logger.warn(var7.getMessage());
					}
				}
			}
		}
	}
	
	/**
	 * Saves all necessary data as preparation for stopping the server.
	 */
	public void stopServer()
	{
		if (!this.worldIsBeingDeleted)
		{
			logger.info("Stopping server");
			
			if (this.getNetworkSystem() != null)
			{
				this.getNetworkSystem().terminateEndpoints();
			}
			
			if (this.serverConfigManager != null)
			{
				logger.info("Saving players");
				this.serverConfigManager.saveAllPlayerData();
				this.serverConfigManager.removeAllPlayers();
			}
			
			if (this.worldServers != null)
			{
				logger.info("Saving worlds");
				this.saveAllWorlds(false);
				
				for (int var1 = 0; var1 < this.worldServers.length; ++var1)
				{
					final WorldServer var2 = this.worldServers[var1];
					var2.flush();
				}
			}
			
			if (this.usageSnooper.isSnooperRunning())
			{
				this.usageSnooper.stopSnooper();
			}
		}
	}
	
	/**
	 * "getHostname" is already taken, but both return the hostname.
	 */
	public String getServerHostname()
	{
		return this.hostname;
	}
	
	public void setHostname(final String host)
	{
		this.hostname = host;
	}
	
	public boolean isServerRunning()
	{
		return this.serverRunning;
	}
	
	/**
	 * Sets the serverRunning variable to false, in order to get the server to shut down.
	 */
	public void initiateShutdown()
	{
		this.serverRunning = false;
	}
	
	@Override
	public void run()
	{
		try
		{
			if (this.startServer())
			{
				this.currentTime = getCurrentTimeMillis();
				long var1 = 0L;
				this.statusResponse.setServerDescription(new ChatComponentText(this.motd));
				this.statusResponse.setProtocolVersionInfo(new ServerStatusResponse.MinecraftProtocolVersionIdentifier("1.8", 47));
				this.addFaviconToStatusResponse(this.statusResponse);
				
				while (this.serverRunning)
				{
					final long var48 = getCurrentTimeMillis();
					long var5 = var48 - this.currentTime;
					
					if (var5 > 2000L && this.currentTime - this.timeOfLastWarning >= 15000L)
					{
						logger.warn("Can\'t keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", new Object[] { Long.valueOf(var5), Long.valueOf(var5 / 50L) });
						var5 = 2000L;
						this.timeOfLastWarning = this.currentTime;
					}
					
					if (var5 < 0L)
					{
						logger.warn("Time ran backwards! Did the system time change?");
						var5 = 0L;
					}
					
					var1 += var5;
					this.currentTime = var48;
					
					if (this.worldServers[0].areAllPlayersAsleep())
					{
						this.tick();
						var1 = 0L;
					}
					else
					{
						while (var1 > 50L)
						{
							var1 -= 50L;
							this.tick();
						}
					}
					
					Thread.sleep(Math.max(1L, 50L - var1));
					this.serverIsRunning = true;
				}
			}
			else
			{
				this.finalTick((CrashReport) null);
			}
		} catch (final Throwable var46)
		{
			logger.error("Encountered an unexpected exception", var46);
			CrashReport var2 = null;
			
			if (var46 instanceof ReportedException)
			{
				var2 = this.addServerInfoToCrashReport(((ReportedException) var46).getCrashReport());
			}
			else
			{
				var2 = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", var46));
			}
			
			final File var3 = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
			
			if (var2.saveToFile(var3))
			{
				logger.error("This crash report has been saved to: " + var3.getAbsolutePath());
			}
			else
			{
				logger.error("We were unable to save this crash report to disk.");
			}
			
			this.finalTick(var2);
		} finally
		{
			try
			{
				this.stopServer();
				this.serverStopped = true;
			} catch (final Throwable var44)
			{
				logger.error("Exception stopping the server", var44);
			} finally
			{
				this.systemExitNow();
			}
		}
	}
	
	private void addFaviconToStatusResponse(final ServerStatusResponse response)
	{
		final File var2 = this.getFile("server-icon.png");
		
		if (var2.isFile())
		{
			final ByteBuf var3 = Unpooled.buffer();
			
			try
			{
				final BufferedImage var4 = ImageIO.read(var2);
				Validate.validState(var4.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
				Validate.validState(var4.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
				ImageIO.write(var4, "PNG", new ByteBufOutputStream(var3));
				final ByteBuf var5 = Base64.encode(var3);
				response.setFavicon("data:image/png;base64," + var5.toString(Charsets.UTF_8));
			} catch (final Exception var9)
			{
				logger.error("Couldn\'t load server icon", var9);
			} finally
			{
				var3.release();
			}
		}
	}
	
	public File getDataDirectory()
	{
		return new File(".");
	}
	
	/**
	 * Called on exit from the main run() loop.
	 */
	protected void finalTick(final CrashReport report)
	{
	}
	
	/**
	 * Directly calls System.exit(0), instantly killing the program.
	 */
	protected void systemExitNow()
	{
	}
	
	/**
	 * Main function called by run() every loop.
	 */
	protected void tick()
	{
		final long var1 = System.nanoTime();
		++this.tickCounter;
		
		if (this.startProfiling)
		{
			this.startProfiling = false;
			this.theProfiler.profilingEnabled = true;
			this.theProfiler.clearProfiling();
		}
		
		this.theProfiler.startSection("root");
		this.updateTimeLightAndEntities();
		
		if (var1 - this.nanoTimeSinceStatusRefresh >= 5000000000L)
		{
			this.nanoTimeSinceStatusRefresh = var1;
			this.statusResponse.setPlayerCountData(new ServerStatusResponse.PlayerCountData(this.getMaxPlayers(), this.getCurrentPlayerCount()));
			final GameProfile[] var3 = new GameProfile[Math.min(this.getCurrentPlayerCount(), 12)];
			final int var4 = MathHelper.getRandomIntegerInRange(this.random, 0, this.getCurrentPlayerCount() - var3.length);
			
			for (int var5 = 0; var5 < var3.length; ++var5)
			{
				var3[var5] = ((EntityPlayerMP) this.serverConfigManager.playerEntityList.get(var4 + var5)).getGameProfile();
			}
			
			Collections.shuffle(Arrays.asList(var3));
			this.statusResponse.getPlayerCountData().setPlayers(var3);
		}
		
		if (this.tickCounter % 900 == 0)
		{
			this.theProfiler.startSection("save");
			this.serverConfigManager.saveAllPlayerData();
			this.saveAllWorlds(true);
			this.theProfiler.endSection();
		}
		
		this.theProfiler.startSection("tallying");
		this.tickTimeArray[this.tickCounter % 100] = System.nanoTime() - var1;
		this.theProfiler.endSection();
		this.theProfiler.startSection("snooper");
		
		if (!this.usageSnooper.isSnooperRunning() && this.tickCounter > 100)
		{
			this.usageSnooper.startSnooper();
		}
		
		if (this.tickCounter % 6000 == 0)
		{
			this.usageSnooper.addMemoryStatsToSnooper();
		}
		
		this.theProfiler.endSection();
		this.theProfiler.endSection();
	}
	
	public void updateTimeLightAndEntities()
	{
		this.theProfiler.startSection("jobs");
		final Queue var1 = this.futureTaskQueue;
		
		synchronized (this.futureTaskQueue)
		{
			while (!this.futureTaskQueue.isEmpty())
			{
				try
				{
					((FutureTask) this.futureTaskQueue.poll()).run();
				} catch (final Throwable var9)
				{
					logger.fatal(var9);
				}
			}
		}
		
		this.theProfiler.endStartSection("levels");
		int var11;
		
		for (var11 = 0; var11 < this.worldServers.length; ++var11)
		{
			final long var2 = System.nanoTime();
			
			if (var11 == 0 || this.getAllowNether())
			{
				final WorldServer var4 = this.worldServers[var11];
				this.theProfiler.startSection(var4.getWorldInfo().getWorldName());
				
				if (this.tickCounter % 20 == 0)
				{
					this.theProfiler.startSection("timeSync");
					this.serverConfigManager.sendPacketToAllPlayersInDimension(new S03PacketTimeUpdate(var4.getTotalWorldTime(), var4.getWorldTime(), var4.getGameRules().getGameRuleBooleanValue("doDaylightCycle")), var4.provider.getDimensionId());
					this.theProfiler.endSection();
				}
				
				this.theProfiler.startSection("tick");
				CrashReport var6;
				
				try
				{
					var4.tick();
				} catch (final Throwable var8)
				{
					var6 = CrashReport.makeCrashReport(var8, "Exception ticking world");
					var4.addWorldInfoToCrashReport(var6);
					throw new ReportedException(var6);
				}
				
				try
				{
					var4.updateEntities();
				} catch (final Throwable var7)
				{
					var6 = CrashReport.makeCrashReport(var7, "Exception ticking world entities");
					var4.addWorldInfoToCrashReport(var6);
					throw new ReportedException(var6);
				}
				
				this.theProfiler.endSection();
				this.theProfiler.startSection("tracker");
				var4.getEntityTracker().updateTrackedEntities();
				this.theProfiler.endSection();
				this.theProfiler.endSection();
			}
			
			this.timeOfLastDimensionTick[var11][this.tickCounter % 100] = System.nanoTime() - var2;
		}
		
		this.theProfiler.endStartSection("connection");
		this.getNetworkSystem().networkTick();
		this.theProfiler.endStartSection("players");
		this.serverConfigManager.onTick();
		this.theProfiler.endStartSection("tickables");
		
		for (var11 = 0; var11 < this.playersOnline.size(); ++var11)
		{
			((IUpdatePlayerListBox) this.playersOnline.get(var11)).update();
		}
		
		this.theProfiler.endSection();
	}
	
	public boolean getAllowNether()
	{
		return true;
	}
	
	public void registerTickable(final IUpdatePlayerListBox tickable)
	{
		this.playersOnline.add(tickable);
	}
	
	public static void main(final String[] p_main_0_)
	{
		Bootstrap.register();
		
		try
		{
			boolean var1 = true;
			String var2 = null;
			String var3 = ".";
			String var4 = null;
			boolean var5 = false;
			boolean var6 = false;
			int var7 = -1;
			
			for (int var8 = 0; var8 < p_main_0_.length; ++var8)
			{
				final String var9 = p_main_0_[var8];
				final String var10 = var8 == p_main_0_.length - 1 ? null : p_main_0_[var8 + 1];
				boolean var11 = false;
				
				if (!var9.equals("nogui") && !var9.equals("--nogui"))
				{
					if (var9.equals("--port") && var10 != null)
					{
						var11 = true;
						
						try
						{
							var7 = Integer.parseInt(var10);
						} catch (final NumberFormatException var13)
						{
							;
						}
					}
					else if (var9.equals("--singleplayer") && var10 != null)
					{
						var11 = true;
						var2 = var10;
					}
					else if (var9.equals("--universe") && var10 != null)
					{
						var11 = true;
						var3 = var10;
					}
					else if (var9.equals("--world") && var10 != null)
					{
						var11 = true;
						var4 = var10;
					}
					else if (var9.equals("--demo"))
					{
						var5 = true;
					}
					else if (var9.equals("--bonusChest"))
					{
						var6 = true;
					}
				}
				else
				{
					var1 = false;
				}
				
				if (var11)
				{
					++var8;
				}
			}
			
			final DedicatedServer var15 = new DedicatedServer(new File(var3));
			
			if (var2 != null)
			{
				var15.setServerOwner(var2);
			}
			
			if (var4 != null)
			{
				var15.setFolderName(var4);
			}
			
			if (var7 >= 0)
			{
				var15.setServerPort(var7);
			}
			
			if (var5)
			{
				var15.setDemo(true);
			}
			
			if (var6)
			{
				var15.canCreateBonusChest(true);
			}
			
			if (var1 && !GraphicsEnvironment.isHeadless())
			{
				var15.setGuiEnabled();
			}
			
			var15.startServerThread();
			Runtime.getRuntime().addShutdownHook(new Thread("Server Shutdown Thread")
			{
				private static final String __OBFID = "CL_00001806";
				
				@Override
				public void run()
				{
					var15.stopServer();
				}
			});
		} catch (final Exception var14)
		{
			logger.fatal("Failed to start the minecraft server", var14);
		}
	}
	
	public void startServerThread()
	{
		this.serverThread = new Thread(this, "Server thread");
		this.serverThread.start();
	}
	
	/**
	 * Returns a File object from the specified string.
	 */
	public File getFile(final String fileName)
	{
		return new File(this.getDataDirectory(), fileName);
	}
	
	/**
	 * Logs the message with a level of INFO.
	 */
	public void logInfo(final String msg)
	{
		logger.info(msg);
	}
	
	/**
	 * Logs the message with a level of WARN.
	 */
	public void logWarning(final String msg)
	{
		logger.warn(msg);
	}
	
	/**
	 * Gets the worldServer by the given dimension.
	 */
	public WorldServer worldServerForDimension(final int dimension)
	{
		return dimension == -1 ? this.worldServers[1] : (dimension == 1 ? this.worldServers[2] : this.worldServers[0]);
	}
	
	/**
	 * Returns the server's hostname.
	 */
	public String getHostname()
	{
		return this.hostname;
	}
	
	/**
	 * Never used, but "getServerPort" is already taken.
	 */
	public int getPort()
	{
		return this.serverPort;
	}
	
	/**
	 * Returns the server message of the day
	 */
	public String getMotd()
	{
		return this.motd;
	}
	
	/**
	 * Returns the server's Minecraft version as string.
	 */
	public String getMinecraftVersion()
	{
		return "1.8";
	}
	
	/**
	 * Returns the number of players currently on the server.
	 */
	public int getCurrentPlayerCount()
	{
		return this.serverConfigManager.getCurrentPlayerCount();
	}
	
	/**
	 * Returns the maximum number of players allowed on the server.
	 */
	public int getMaxPlayers()
	{
		return this.serverConfigManager.getMaxPlayers();
	}
	
	/**
	 * Returns an array of the usernames of all the connected players.
	 */
	public String[] getAllUsernames()
	{
		return this.serverConfigManager.getAllUsernames();
	}
	
	/**
	 * Returns an array of the GameProfiles of all the connected players
	 */
	public GameProfile[] getGameProfiles()
	{
		return this.serverConfigManager.getAllProfiles();
	}
	
	/**
	 * Used by RCon's Query in the form of "MajorServerMod 1.2.3: MyPlugin 1.3; AnotherPlugin 2.1; AndSoForth 1.0".
	 */
	public String getPlugins()
	{
		return "";
	}
	
	/**
	 * Handle a command received by an RCon instance
	 */
	public String handleRConCommand(final String command)
	{
		RConConsoleSource.func_175570_h().resetLog();
		this.commandManager.executeCommand(RConConsoleSource.func_175570_h(), command);
		return RConConsoleSource.func_175570_h().getLogContents();
	}
	
	/**
	 * Returns true if debugging is enabled, false otherwise.
	 */
	public boolean isDebuggingEnabled()
	{
		return false;
	}
	
	/**
	 * Logs the error message with a level of SEVERE.
	 */
	public void logSevere(final String msg)
	{
		logger.error(msg);
	}
	
	/**
	 * If isDebuggingEnabled(), logs the message with a level of INFO.
	 */
	public void logDebug(final String msg)
	{
		if (this.isDebuggingEnabled())
		{
			logger.info(msg);
		}
	}
	
	public String getServerModName()
	{
		return "vanilla";
	}
	
	/**
	 * Adds the server info, including from theWorldServer, to the crash report.
	 */
	public CrashReport addServerInfoToCrashReport(final CrashReport report)
	{
		report.getCategory().addCrashSectionCallable("Profiler Position", new Callable()
		{
			private static final String __OBFID = "CL_00001418";
			
			public String func_179879_a()
			{
				return MinecraftServer.this.theProfiler.profilingEnabled ? MinecraftServer.this.theProfiler.getNameOfLastSection() : "N/A (disabled)";
			}
			
			@Override
			public Object call()
			{
				return this.func_179879_a();
			}
		});
		
		if (this.serverConfigManager != null)
		{
			report.getCategory().addCrashSectionCallable("Player Count", new Callable()
			{
				private static final String __OBFID = "CL_00001419";
				
				@Override
				public String call()
				{
					return MinecraftServer.this.serverConfigManager.getCurrentPlayerCount() + " / " + MinecraftServer.this.serverConfigManager.getMaxPlayers() + "; " + MinecraftServer.this.serverConfigManager.playerEntityList;
				}
			});
		}
		
		return report;
	}
	
	public void handleCompletion(final C14PacketTabComplete packet, final NetHandlerPlayServer handler)
	{
		final String toComplete = packet.getMessage();
		
		if (toComplete.startsWith("/"))
		{
			ParsingManager.submit(new Runnable()
			{
				@Override
				public void run()
				{
					Parser.parseCompletion(new CompletionData(toComplete, packet.getCursorIndex(), handler.playerEntity, packet.func_179709_b()), 1).sendPacket(handler);
				}
			});
		}
		else
		{
			final int cursorIndex = packet.getCursorIndex();
			final int startIndex = toComplete.lastIndexOf(' ', cursorIndex - 1) + 1;
			
			ParsingManager.submit(new Runnable()
			{
				@Override
				public void run()
				{
					final Set<ITabCompletion> completions = MinecraftServer.this.serverConfigManager.playerCompletions;
					final Set<Weighted> tcDataSet = new TreeSet<>();
					
					final CompletionData cData = new CompletionData(toComplete, cursorIndex, handler.playerEntity, null);
					
					for (final ITabCompletion tc : completions)
						TabCompletionData.addToSet(tcDataSet, startIndex, cData, tc);
					
					handler.sendPacket(new S3APacketTabComplete(tcDataSet));
				}
			});
		}
	}
	
	/**
	 * Gets mcServer.
	 */
	public static MinecraftServer getServer()
	{
		return mcServer;
	}
	
	public boolean func_175578_N()
	{
		return this.anvilFile != null;
	}
	
	/**
	 * Gets the name of this command sender (usually username, but possibly "Rcon")
	 */
	@Override
	public String getName()
	{
		return "Server";
	}
	
	/**
	 * Notifies this sender of some sort of information. This is for messages intended to display to the user. Used for typical output (like "you asked for whether or not this game rule is set, so here's your answer"), warnings (like "I fetched this block for you by ID, but I'd like you to know that every time you do this, I die a little inside"), and errors (like "it's not called iron_pixacke, silly").
	 */
	@Override
	public void addChatMessage(final IChatComponent message)
	{
		logger.info(message.getUnformattedText());
	}
	
	/**
	 * Returns true if the command sender is allowed to use the given command.
	 */
	@Override
	public boolean canCommandSenderUseCommand(final int permissionLevel)
	{
		return true;
	}
	
	public ICommandManager getCommandManager()
	{
		return this.commandManager;
	}
	
	/**
	 * Gets KeyPair instanced in MinecraftServer.
	 */
	public KeyPair getKeyPair()
	{
		return this.serverKeyPair;
	}
	
	/**
	 * Gets serverPort.
	 */
	public int getServerPort()
	{
		return this.serverPort;
	}
	
	public void setServerPort(final int port)
	{
		this.serverPort = port;
	}
	
	/**
	 * Returns the username of the server owner (for integrated servers)
	 */
	public String getServerOwner()
	{
		return this.serverOwner;
	}
	
	/**
	 * Sets the username of the owner of this server (in the case of an integrated server)
	 */
	public void setServerOwner(final String owner)
	{
		this.serverOwner = owner;
	}
	
	public boolean isSinglePlayer()
	{
		return this.serverOwner != null;
	}
	
	public String getFolderName()
	{
		return this.folderName;
	}
	
	public void setFolderName(final String name)
	{
		this.folderName = name;
	}
	
	public void setKeyPair(final KeyPair keyPair)
	{
		this.serverKeyPair = keyPair;
	}
	
	public void setDifficultyForAllWorlds(final EnumDifficulty difficulty)
	{
		for (int var2 = 0; var2 < this.worldServers.length; ++var2)
		{
			final WorldServer var3 = this.worldServers[var2];
			
			if (var3 != null)
			{
				if (var3.getWorldInfo().isHardcoreModeEnabled())
				{
					var3.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
					var3.setAllowedSpawnTypes(true, true);
				}
				else if (this.isSinglePlayer())
				{
					var3.getWorldInfo().setDifficulty(difficulty);
					var3.setAllowedSpawnTypes(var3.getDifficulty() != EnumDifficulty.PEACEFUL, true);
				}
				else
				{
					var3.getWorldInfo().setDifficulty(difficulty);
					var3.setAllowedSpawnTypes(this.allowSpawnMonsters(), this.canSpawnAnimals);
				}
			}
		}
	}
	
	protected boolean allowSpawnMonsters()
	{
		return true;
	}
	
	/**
	 * Gets whether this is a demo or not.
	 */
	public boolean isDemo()
	{
		return this.isDemo;
	}
	
	/**
	 * Sets whether this is a demo or not.
	 */
	public void setDemo(final boolean demo)
	{
		this.isDemo = demo;
	}
	
	public void canCreateBonusChest(final boolean enable)
	{
		this.enableBonusChest = enable;
	}
	
	public ISaveFormat getActiveAnvilConverter()
	{
		return this.anvilConverterForAnvilFile;
	}
	
	/**
	 * WARNING : directly calls getActiveAnvilConverter().deleteWorldDirectory(theWorldServer[0].getSaveHandler().getWorldDirectoryName());
	 */
	public void deleteWorldAndStopServer()
	{
		this.worldIsBeingDeleted = true;
		this.getActiveAnvilConverter().flushCache();
		
		for (int var1 = 0; var1 < this.worldServers.length; ++var1)
		{
			final WorldServer var2 = this.worldServers[var1];
			
			if (var2 != null)
			{
				var2.flush();
			}
		}
		
		this.getActiveAnvilConverter().deleteWorldDirectory(this.worldServers[0].getSaveHandler().getWorldDirectoryName());
		this.initiateShutdown();
	}
	
	public String getResourcePackUrl()
	{
		return this.resourcePackUrl;
	}
	
	public String getResourcePackHash()
	{
		return this.resourcePackHash;
	}
	
	public void setResourcePack(final String url, final String hash)
	{
		this.resourcePackUrl = url;
		this.resourcePackHash = hash;
	}
	
	@Override
	public void addServerStatsToSnooper(final PlayerUsageSnooper playerSnooper)
	{
		playerSnooper.addClientStat("whitelist_enabled", Boolean.valueOf(false));
		playerSnooper.addClientStat("whitelist_count", Integer.valueOf(0));
		
		if (this.serverConfigManager != null)
		{
			playerSnooper.addClientStat("players_current", Integer.valueOf(this.getCurrentPlayerCount()));
			playerSnooper.addClientStat("players_max", Integer.valueOf(this.getMaxPlayers()));
			playerSnooper.addClientStat("players_seen", Integer.valueOf(this.serverConfigManager.getAvailablePlayerDat().length));
		}
		
		playerSnooper.addClientStat("uses_auth", Boolean.valueOf(this.onlineMode));
		playerSnooper.addClientStat("gui_state", this.getGuiEnabled() ? "enabled" : "disabled");
		playerSnooper.addClientStat("run_time", Long.valueOf((getCurrentTimeMillis() - playerSnooper.getMinecraftStartTimeMillis()) / 60L * 1000L));
		playerSnooper.addClientStat("avg_tick_ms", Integer.valueOf((int) (MathHelper.average(this.tickTimeArray) * 1.0E-6D)));
		int var2 = 0;
		
		if (this.worldServers != null)
		{
			for (int var3 = 0; var3 < this.worldServers.length; ++var3)
			{
				if (this.worldServers[var3] != null)
				{
					final WorldServer var4 = this.worldServers[var3];
					final WorldInfo var5 = var4.getWorldInfo();
					playerSnooper.addClientStat("world[" + var2 + "][dimension]", Integer.valueOf(var4.provider.getDimensionId()));
					playerSnooper.addClientStat("world[" + var2 + "][mode]", var5.getGameType());
					playerSnooper.addClientStat("world[" + var2 + "][difficulty]", var4.getDifficulty());
					playerSnooper.addClientStat("world[" + var2 + "][hardcore]", Boolean.valueOf(var5.isHardcoreModeEnabled()));
					playerSnooper.addClientStat("world[" + var2 + "][generator_name]", var5.getTerrainType().getWorldTypeName());
					playerSnooper.addClientStat("world[" + var2 + "][generator_version]", Integer.valueOf(var5.getTerrainType().getGeneratorVersion()));
					playerSnooper.addClientStat("world[" + var2 + "][height]", Integer.valueOf(this.buildLimit));
					playerSnooper.addClientStat("world[" + var2 + "][chunks_loaded]", Integer.valueOf(var4.getChunkProvider().getLoadedChunkCount()));
					++var2;
				}
			}
		}
		
		playerSnooper.addClientStat("worlds", Integer.valueOf(var2));
	}
	
	@Override
	public void addServerTypeToSnooper(final PlayerUsageSnooper playerSnooper)
	{
		playerSnooper.addStatToSnooper("singleplayer", Boolean.valueOf(this.isSinglePlayer()));
		playerSnooper.addStatToSnooper("server_brand", this.getServerModName());
		playerSnooper.addStatToSnooper("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
		playerSnooper.addStatToSnooper("dedicated", Boolean.valueOf(this.isDedicatedServer()));
	}
	
	/**
	 * Returns whether snooping is enabled or not.
	 */
	@Override
	public boolean isSnooperEnabled()
	{
		return true;
	}
	
	public abstract boolean isDedicatedServer();
	
	public boolean isServerInOnlineMode()
	{
		return this.onlineMode;
	}
	
	public void setOnlineMode(final boolean online)
	{
		this.onlineMode = online;
	}
	
	public boolean getCanSpawnAnimals()
	{
		return this.canSpawnAnimals;
	}
	
	public void setCanSpawnAnimals(final boolean spawnAnimals)
	{
		this.canSpawnAnimals = spawnAnimals;
	}
	
	public boolean getCanSpawnNPCs()
	{
		return this.canSpawnNPCs;
	}
	
	public void setCanSpawnNPCs(final boolean spawnNpcs)
	{
		this.canSpawnNPCs = spawnNpcs;
	}
	
	public boolean isPVPEnabled()
	{
		return this.pvpEnabled;
	}
	
	public void setAllowPvp(final boolean allowPvp)
	{
		this.pvpEnabled = allowPvp;
	}
	
	public boolean isFlightAllowed()
	{
		return this.allowFlight;
	}
	
	public void setAllowFlight(final boolean allow)
	{
		this.allowFlight = allow;
	}
	
	/**
	 * Return whether command blocks are enabled.
	 */
	public abstract boolean isCommandBlockEnabled();
	
	public String getMOTD()
	{
		return this.motd;
	}
	
	public void setMOTD(final String motdIn)
	{
		this.motd = motdIn;
	}
	
	public int getBuildLimit()
	{
		return this.buildLimit;
	}
	
	public void setBuildLimit(final int maxBuildHeight)
	{
		this.buildLimit = maxBuildHeight;
	}
	
	public boolean isServerStopped()
	{
		return this.serverStopped;
	}
	
	public ServerConfigurationManager getConfigurationManager()
	{
		return this.serverConfigManager;
	}
	
	public void setConfigManager(final ServerConfigurationManager configManager)
	{
		this.serverConfigManager = configManager;
	}
	
	/**
	 * Sets the game type for all worlds.
	 */
	public void setGameType(final WorldSettings.GameType gameMode)
	{
		for (int var2 = 0; var2 < this.worldServers.length; ++var2)
		{
			getServer().worldServers[var2].getWorldInfo().setGameType(gameMode);
		}
	}
	
	public NetworkSystem getNetworkSystem()
	{
		return this.networkSystem;
	}
	
	public boolean getGuiEnabled()
	{
		return false;
	}
	
	/**
	 * On dedicated does nothing. On integrated, sets commandsAllowedForAll, gameType and allows external connections.
	 */
	public abstract String shareToLAN(WorldSettings.GameType type, boolean allowCheats);
	
	public int getTickCounter()
	{
		return this.tickCounter;
	}
	
	public void enableProfiling()
	{
		this.startProfiling = true;
	}
	
	@Override
	public BlockPos getPosition()
	{
		return BlockPos.ORIGIN;
	}
	
	@Override
	public Vec3 getPositionVector()
	{
		return new Vec3(0.0D, 0.0D, 0.0D);
	}
	
	@Override
	public World getEntityWorld()
	{
		return this.worldServers[0];
	}
	
	@Override
	public Entity getCommandSenderEntity()
	{
		return null;
	}
	
	/**
	 * Return the spawn protection area's size.
	 */
	public int getSpawnProtectionSize()
	{
		return 16;
	}
	
	public boolean isBlockProtected(final World worldIn, final BlockPos pos, final EntityPlayer playerIn)
	{
		return false;
	}
	
	public void setForceGamemode(final boolean force)
	{
		this.isGamemodeForced = force;
	}
	
	public boolean getForceGamemode()
	{
		return this.isGamemodeForced;
	}
	
	public Proxy getServerProxy()
	{
		return this.serverProxy;
	}
	
	public static long getCurrentTimeMillis()
	{
		return System.currentTimeMillis();
	}
	
	public int getMaxPlayerIdleMinutes()
	{
		return this.maxPlayerIdleMinutes;
	}
	
	public void setPlayerIdleTimeout(final int idleTimeout)
	{
		this.maxPlayerIdleMinutes = idleTimeout;
	}
	
	@Override
	public IChatComponent getDisplayName()
	{
		return new ChatComponentText(this.getName());
	}
	
	public boolean isAnnouncingPlayerAchievements()
	{
		return true;
	}
	
	public MinecraftSessionService getMinecraftSessionService()
	{
		return this.sessionService;
	}
	
	public GameProfileRepository getGameProfileRepository()
	{
		return this.profileRepo;
	}
	
	public PlayerProfileCache getPlayerProfileCache()
	{
		return this.profileCache;
	}
	
	public ServerStatusResponse getServerStatusResponse()
	{
		return this.statusResponse;
	}
	
	public void refreshStatusNextTick()
	{
		this.nanoTimeSinceStatusRefresh = 0L;
	}
	
	public Entity getEntityFromUuid(final UUID uuid)
	{
		final WorldServer[] var2 = this.worldServers;
		final int var3 = var2.length;
		
		for (int var4 = 0; var4 < var3; ++var4)
		{
			final WorldServer var5 = var2[var4];
			
			if (var5 != null)
			{
				final Entity var6 = var5.getEntityFromUuid(uuid);
				
				if (var6 != null)
				{
					return var6;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public boolean sendCommandFeedback()
	{
		return getServer().worldServers[0].getGameRules().getGameRuleBooleanValue("sendCommandFeedback");
	}
	
	@Override
	public void func_174794_a(final CommandResultStats.Type p_174794_1_, final int p_174794_2_)
	{
	}
	
	public int getMaxWorldSize()
	{
		return 29999984;
	}
	
	public ListenableFuture callFromMainThread(final Callable callable)
	{
		Validate.notNull(callable);
		
		if (!this.isCallingFromMinecraftThread())
		{
			final ListenableFutureTask var2 = ListenableFutureTask.create(callable);
			final Queue var3 = this.futureTaskQueue;
			
			synchronized (this.futureTaskQueue)
			{
				this.futureTaskQueue.add(var2);
				return var2;
			}
		}
		else
		{
			try
			{
				return Futures.immediateFuture(callable.call());
			} catch (final Exception var6)
			{
				return Futures.immediateFailedCheckedFuture(var6);
			}
		}
	}
	
	@Override
	public ListenableFuture addScheduledTask(final Runnable runnableToSchedule)
	{
		Validate.notNull(runnableToSchedule);
		return this.callFromMainThread(Executors.callable(runnableToSchedule));
	}
	
	@Override
	public boolean isCallingFromMinecraftThread()
	{
		return Thread.currentThread() == this.serverThread;
	}
	
	/**
	 * The compression treshold. If the packet is larger than the specified amount of bytes, it will be compressed
	 */
	public int getNetworkCompressionTreshold()
	{
		return 256;
	}
	
	public long getCurrentTime()
	{
		return this.currentTime;
	}
	
	public Thread getServerThread()
	{
		return this.serverThread;
	}
}
