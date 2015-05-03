package net.minecraft.server.integrated;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ThreadLanServerPing;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.CryptManager;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;

public class IntegratedServer extends MinecraftServer
{
	private static final Logger logger = LogManager.getLogger();
	
	/** The Minecraft instance. */
	private final Minecraft mc;
	private final WorldSettings theWorldSettings;
	private boolean isGamePaused;
	private boolean isPublic;
	private ThreadLanServerPing lanServerPing;
	private static final String __OBFID = "CL_00001129";
	
	public IntegratedServer(final Minecraft mcIn)
	{
		super(mcIn.getProxy(), new File(mcIn.mcDataDir, USER_CACHE_FILE.getName()));
		this.mc = mcIn;
		this.theWorldSettings = null;
	}
	
	public IntegratedServer(final Minecraft mcIn, final String p_i1317_2_, final String p_i1317_3_, final WorldSettings p_i1317_4_)
	{
		super(new File(mcIn.mcDataDir, "saves"), mcIn.getProxy(), new File(mcIn.mcDataDir, USER_CACHE_FILE.getName()));
		this.setServerOwner(mcIn.getSession().getUsername());
		this.setFolderName(p_i1317_2_);
		this.setWorldName(p_i1317_3_);
		this.setDemo(mcIn.isDemo());
		this.canCreateBonusChest(p_i1317_4_.isBonusChestEnabled());
		this.setBuildLimit(256);
		this.setConfigManager(new IntegratedPlayerList(this));
		this.mc = mcIn;
		this.theWorldSettings = this.isDemo() ? DemoWorldServer.demoWorldSettings : p_i1317_4_;
	}
	
	@Override
	protected void loadAllWorlds(final String p_71247_1_, final String p_71247_2_, final long seed, final WorldType type, final String p_71247_6_)
	{
		this.convertMapIfNeeded(p_71247_1_);
		this.worldServers = new WorldServer[3];
		this.timeOfLastDimensionTick = new long[this.worldServers.length][100];
		final ISaveHandler var7 = this.getActiveAnvilConverter().getSaveLoader(p_71247_1_, true);
		this.setResourcePackFromWorld(this.getFolderName(), var7);
		WorldInfo var8 = var7.loadWorldInfo();
		
		if (var8 == null)
		{
			var8 = new WorldInfo(this.theWorldSettings, p_71247_2_);
		}
		else
		{
			var8.setWorldName(p_71247_2_);
		}
		
		for (int var9 = 0; var9 < this.worldServers.length; ++var9)
		{
			byte var10 = 0;
			
			if (var9 == 1)
			{
				var10 = -1;
			}
			
			if (var9 == 2)
			{
				var10 = 1;
			}
			
			if (var9 == 0)
			{
				if (this.isDemo())
				{
					this.worldServers[var9] = (WorldServer) (new DemoWorldServer(this, var7, var8, var10, this.theProfiler)).init();
				}
				else
				{
					this.worldServers[var9] = (WorldServer) (new WorldServer(this, var7, var8, var10, this.theProfiler)).init();
				}
				
				this.worldServers[var9].initialize(this.theWorldSettings);
			}
			else
			{
				this.worldServers[var9] = (WorldServer) (new WorldServerMulti(this, var7, var10, this.worldServers[0], this.theProfiler)).init();
			}
			
			this.worldServers[var9].addWorldAccess(new WorldManager(this, this.worldServers[var9]));
		}
		
		this.getConfigurationManager().setPlayerManager(this.worldServers);
		
		if (this.worldServers[0].getWorldInfo().getDifficulty() == null)
		{
			this.setDifficultyForAllWorlds(this.mc.gameSettings.difficulty);
		}
		
		this.initialWorldChunkLoad();
	}
	
	/**
	 * Initialises the server and starts it.
	 */
	@Override
	protected boolean startServer() throws IOException
	{
		logger.info("Starting integrated minecraft server version 1.8");
		this.setOnlineMode(true);
		this.setCanSpawnAnimals(true);
		this.setCanSpawnNPCs(true);
		this.setAllowPvp(true);
		this.setAllowFlight(true);
		logger.info("Generating keypair");
		this.setKeyPair(CryptManager.generateKeyPair());
		this.loadAllWorlds(this.getFolderName(), this.getWorldName(), this.theWorldSettings.getSeed(), this.theWorldSettings.getTerrainType(), this.theWorldSettings.getWorldName());
		this.setMOTD(this.getServerOwner() + " - " + this.worldServers[0].getWorldInfo().getWorldName());
		return true;
	}
	
	/**
	 * Main function called by run() every loop.
	 */
	@Override
	public void tick()
	{
		final boolean var1 = this.isGamePaused;
		this.isGamePaused = Minecraft.getMinecraft().getNetHandler() != null && Minecraft.getMinecraft().isGamePaused();
		
		if (!var1 && this.isGamePaused)
		{
			logger.info("Saving and pausing game...");
			this.getConfigurationManager().saveAllPlayerData();
			this.saveAllWorlds(false);
		}
		
		if (this.isGamePaused)
		{
			final Queue var2 = this.futureTaskQueue;
			
			synchronized (this.futureTaskQueue)
			{
				while (!this.futureTaskQueue.isEmpty())
				{
					try
					{
						((FutureTask) this.futureTaskQueue.poll()).run();
					} catch (final Throwable var8)
					{
						logger.fatal(var8);
					}
				}
			}
		}
		else
		{
			super.tick();
			
			if (this.mc.gameSettings.renderDistanceChunks != this.getConfigurationManager().getViewDistance())
			{
				logger.info("Changing view distance to {}, from {}", new Object[] { Integer.valueOf(this.mc.gameSettings.renderDistanceChunks), Integer.valueOf(this.getConfigurationManager().getViewDistance()) });
				this.getConfigurationManager().setViewDistance(this.mc.gameSettings.renderDistanceChunks);
			}
			
			if (this.mc.theWorld != null)
			{
				final WorldInfo var10 = this.worldServers[0].getWorldInfo();
				final WorldInfo var3 = this.mc.theWorld.getWorldInfo();
				
				if (!var10.isDifficultyLocked() && var3.getDifficulty() != var10.getDifficulty())
				{
					logger.info("Changing difficulty to {}, from {}",  var3.getDifficulty(), var10.getDifficulty() );
					this.setDifficultyForAllWorlds(var3.getDifficulty());
				}
				else if (var3.isDifficultyLocked() && !var10.isDifficultyLocked())
				{
					logger.info("Locking difficulty to {}",  var3.getDifficulty() );
					final WorldServer[] var4 = this.worldServers;
					final int var5 = var4.length;
					
					for (int var6 = 0; var6 < var5; ++var6)
					{
						final WorldServer var7 = var4[var6];
						
						if (var7 != null)
						{
							var7.getWorldInfo().setDifficultyLocked(true);
						}
					}
				}
			}
		}
	}
	
	@Override
	public boolean canStructuresSpawn()
	{
		return false;
	}
	
	@Override
	public WorldSettings.GameType getGameType()
	{
		return this.theWorldSettings.getGameType();
	}
	
	/**
	 * Get the server's difficulty
	 */
	@Override
	public EnumDifficulty getDifficulty()
	{
		return this.mc.theWorld.getWorldInfo().getDifficulty();
	}
	
	/**
	 * Defaults to false.
	 */
	@Override
	public boolean isHardcore()
	{
		return this.theWorldSettings.getHardcoreEnabled();
	}
	
	@Override
	public File getDataDirectory()
	{
		return this.mc.mcDataDir;
	}
	
	@Override
	public boolean isDedicatedServer()
	{
		return false;
	}
	
	/**
	 * Called on exit from the main run() loop.
	 */
	@Override
	protected void finalTick(final CrashReport report)
	{
		this.mc.crashed(report);
	}
	
	/**
	 * Adds the server info, including from theWorldServer, to the crash report.
	 */
	@Override
	public CrashReport addServerInfoToCrashReport(CrashReport report)
	{
		report = super.addServerInfoToCrashReport(report);
		report.getCategory().addCrashSectionCallable("Type", new Callable()
		{
			private static final String __OBFID = "CL_00001130";
			
			@Override
			public String call()
			{
				return "Integrated Server (map_client.txt)";
			}
		});
		report.getCategory().addCrashSectionCallable("Is Modded", new Callable()
		{
			private static final String __OBFID = "CL_00001131";
			
			@Override
			public String call()
			{
				String var1 = ClientBrandRetriever.getClientModName();
				
				if (!var1.equals("vanilla"))
				{
					return "Definitely; Client brand changed to \'" + var1 + "\'";
				}
				else
				{
					var1 = IntegratedServer.this.getServerModName();
					return !var1.equals("vanilla") ? "Definitely; Server brand changed to \'" + var1 + "\'" : (Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and both client + server brands are untouched.");
				}
			}
		});
		return report;
	}
	
	@Override
	public void setDifficultyForAllWorlds(final EnumDifficulty difficulty)
	{
		super.setDifficultyForAllWorlds(difficulty);
		
		if (this.mc.theWorld != null)
		{
			this.mc.theWorld.getWorldInfo().setDifficulty(difficulty);
		}
	}
	
	@Override
	public void addServerStatsToSnooper(final PlayerUsageSnooper playerSnooper)
	{
		super.addServerStatsToSnooper(playerSnooper);
		playerSnooper.addClientStat("snooper_partner", this.mc.getPlayerUsageSnooper().getUniqueID());
	}
	
	/**
	 * Returns whether snooping is enabled or not.
	 */
	@Override
	public boolean isSnooperEnabled()
	{
		return Minecraft.getMinecraft().isSnooperEnabled();
	}
	
	/**
	 * On dedicated does nothing. On integrated, sets commandsAllowedForAll, gameType and allows external connections.
	 */
	@Override
	public String shareToLAN(final WorldSettings.GameType type, final boolean allowCheats)
	{
		try
		{
			int var3 = -1;
			
			try
			{
				var3 = HttpUtil.getSuitableLanPort();
			} catch (final IOException var5)
			{
				;
			}
			
			if (var3 <= 0)
			{
				var3 = 25564;
			}
			
			this.getNetworkSystem().addLanEndpoint((InetAddress) null, var3);
			logger.info("Started on " + var3);
			this.isPublic = true;
			this.lanServerPing = new ThreadLanServerPing(this.getMOTD(), var3 + "");
			this.lanServerPing.start();
			this.getConfigurationManager().func_152604_a(type);
			this.getConfigurationManager().setCommandsAllowedForAll(allowCheats);
			return var3 + "";
		} catch (final IOException var6)
		{
			return null;
		}
	}
	
	/**
	 * Saves all necessary data as preparation for stopping the server.
	 */
	@Override
	public void stopServer()
	{
		super.stopServer();
		
		if (this.lanServerPing != null)
		{
			this.lanServerPing.interrupt();
			this.lanServerPing = null;
		}
	}
	
	/**
	 * Sets the serverRunning variable to false, in order to get the server to shut down.
	 */
	@Override
	public void initiateShutdown()
	{
		Futures.getUnchecked(this.addScheduledTask(new Runnable()
		{
			private static final String __OBFID = "CL_00002380";
			
			@Override
			public void run()
			{
				final ArrayList var1 = Lists.newArrayList(IntegratedServer.this.getConfigurationManager().playerEntityList);
				final Iterator var2 = var1.iterator();
				
				while (var2.hasNext())
				{
					final EntityPlayerMP var3 = (EntityPlayerMP) var2.next();
					IntegratedServer.this.getConfigurationManager().playerLoggedOut(var3);
				}
			}
		}));
		super.initiateShutdown();
		
		if (this.lanServerPing != null)
		{
			this.lanServerPing.interrupt();
			this.lanServerPing = null;
		}
	}
	
	public void func_175592_a()
	{
		this.func_175585_v();
	}
	
	/**
	 * Returns true if this integrated server is open to LAN
	 */
	public boolean getPublic()
	{
		return this.isPublic;
	}
	
	/**
	 * Sets the game type for all worlds.
	 */
	@Override
	public void setGameType(final WorldSettings.GameType gameMode)
	{
		this.getConfigurationManager().func_152604_a(gameMode);
	}
	
	/**
	 * Return whether command blocks are enabled.
	 */
	@Override
	public boolean isCommandBlockEnabled()
	{
		return true;
	}
	
	@Override
	public int getOpPermissionLevel()
	{
		return 4;
	}
}
