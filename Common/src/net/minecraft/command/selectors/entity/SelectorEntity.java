package net.minecraft.command.selectors.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.collections.TypeIDs;
import net.minecraft.command.construction.SelectorConstructable;
import net.minecraft.command.selectors.entity.FilterList.InvertableArg;
import net.minecraft.command.selectors.entity.SelectorDescriptorEntity.ExParserData;
import net.minecraft.command.type.custom.coordinate.Coordinates;
import net.minecraft.command.type.custom.coordinate.SingleCoordinate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Ordering;

public class SelectorEntity extends CommandArg<List<Entity>>
{
	
	private final SelectorType selType;
	private final boolean isPlayerSelector;
	
	private final CommandArg<Vec3> coords;
	private final CommandArg<Integer> r;
	private final CommandArg<Integer> rm;
	private final CommandArg<Double> dx;
	private final CommandArg<Double> dy;
	private final CommandArg<Double> dz;
	private final CommandArg<Integer> c;
	private final CommandArg<Integer> m;
	private final CommandArg<Integer> l;
	private final CommandArg<Integer> lm;
	private final CommandArg<Double> rx;
	private final CommandArg<Double> rxm;
	private final CommandArg<Double> ry;
	private final CommandArg<Double> rym;
	private final InvertableArg name;
	private final InvertableArg team;
	private final InvertableArg type;
	private final CommandArg<Predicate<Map<ScoreObjective, Score>>> scores;
	
	private final boolean nullScoreAllowed;
	
	private final CommandArg<NBTTagCompound> nbt;
	
	private final boolean allWorlds;
	
	public static enum SelectorType
	{
		p, e, r, a;
	}
	
	public SelectorEntity(final SelectorType selType, final ExParserData parserData)
	{
		this.selType = selType;
		
		final CommandArg<Vec3> coords = SelectorConstructable.getParam(TypeIDs.Coordinates, "xyz", parserData);
		
		if (coords == null)
		{
			final CommandArg<Double> x = SelectorConstructable.getParam(TypeIDs.Double, 0, "x", parserData);
			final CommandArg<Double> y = SelectorConstructable.getParam(TypeIDs.Double, 1, "y", parserData);
			final CommandArg<Double> z = SelectorConstructable.getParam(TypeIDs.Double, 2, "z", parserData);
			
			this.coords = new Coordinates(
				x == null ? SingleCoordinate.tildexNC : x,
				y == null ? SingleCoordinate.tildeyNC : y,
				z == null ? SingleCoordinate.tildezNC : z);
		}
		else
			this.coords = coords;
		
		this.r = SelectorConstructable.getParam(TypeIDs.Integer, 3, "r", parserData);
		this.rm = SelectorConstructable.getParam(TypeIDs.Integer, "rm", parserData);
		this.dx = SelectorConstructable.getParam(TypeIDs.Double, "dx", parserData);
		this.dy = SelectorConstructable.getParam(TypeIDs.Double, "dy", parserData);
		this.dz = SelectorConstructable.getParam(TypeIDs.Double, "dz", parserData);
		this.c = SelectorConstructable.getParam(TypeIDs.Integer, "c", parserData);
		this.m = SelectorConstructable.getParam(TypeIDs.Integer, "m", parserData);
		this.l = SelectorConstructable.getParam(TypeIDs.Integer, "l", parserData);
		this.lm = SelectorConstructable.getParam(TypeIDs.Integer, "lm", parserData);
		this.rx = SelectorConstructable.getParam(TypeIDs.Double, "rx", parserData);
		this.rxm = SelectorConstructable.getParam(TypeIDs.Double, "rxm", parserData);
		this.ry = SelectorConstructable.getParam(TypeIDs.Double, "ry", parserData);
		this.rym = SelectorConstructable.getParam(TypeIDs.Double, "rym", parserData);
		this.name = parserData.name;
		this.team = parserData.team;
		this.type = parserData.type;
		
		this.nbt = SelectorConstructable.getParam(TypeIDs.NBTCompound, "nbt", parserData);
		
		final Map<String, MutablePair<CommandArg<Integer>, CommandArg<Integer>>> pScores = parserData.primitiveScores;
		
		this.nullScoreAllowed = parserData.nullScoreAllowed && pScores.isEmpty();
		
		this.scores = pScores == null ? null : this.procScores(pScores);
		
		this.isPlayerSelector = selType == SelectorType.a || selType == SelectorType.p || (selType == SelectorType.r && this.type == null);
		
		this.allWorlds = !(this.r != null || this.rm != null || this.dx != null || this.dy != null || this.dz != null || this.coords != null);
	}
	
	public CommandArg<Predicate<Map<ScoreObjective, Score>>> procScores(final Map<String, MutablePair<CommandArg<Integer>, CommandArg<Integer>>> pScores)
	{
		return new CommandArg<Predicate<Map<ScoreObjective, Score>>>()
		{
			@Override
			public Predicate<Map<ScoreObjective, Score>> eval(final ICommandSender sender) throws CommandException
			{
				final List<Predicate<Map<ScoreObjective, Score>>> predicates = new ArrayList<>(pScores.size());
				
				final Scoreboard scoreboard = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
				
				for (final Entry<String, MutablePair<CommandArg<Integer>, CommandArg<Integer>>> score : pScores.entrySet())
				{
					final ScoreObjective objective = scoreboard.getObjective(score.getKey());
					
					if (objective == null)
						throw new CommandException("Objective not found: " + score.getKey());
					
					final CommandArg<Integer> minArg = score.getValue().left;
					final CommandArg<Integer> maxArg = score.getValue().right;
					
					if (minArg != null)
					{
						final int min = minArg.eval(sender);
						
						if (maxArg != null)
						{
							final int max = maxArg.eval(sender);
							predicates.add(new Predicate<Map<ScoreObjective, Score>>()
							{
								@Override
								public boolean apply(final Map<ScoreObjective, Score> scores)
								{
									final Score score = scores.get(objective);
									
									if (score == null)
										return false;
									
									final int scoreVal = score.getScorePoints();
									
									return min <= scoreVal && scoreVal <= max;
								}
							});
							continue;
						}
						
						predicates.add(new Predicate<Map<ScoreObjective, Score>>()
						{
							@Override
							public boolean apply(final Map<ScoreObjective, Score> scores)
							{
								final Score score = scores.get(objective);
								
								if (score == null)
									return false;
								
								final int scoreVal = score.getScorePoints();
								
								return min <= scoreVal;
							}
						});
						continue;
					}
					
					final int max = maxArg.eval(sender);
					predicates.add(new Predicate<Map<ScoreObjective, Score>>()
					{
						@Override
						public boolean apply(final Map<ScoreObjective, Score> scores)
						{
							final Score score = scores.get(objective);
							
							if (score == null)
								return false;
							
							final int scoreVal = score.getScorePoints();
							
							return scoreVal <= max;
						}
					});
				}
				
				return Predicates.and(predicates);
			}
		};
	}
	
	@Override
	public List<Entity> eval(final ICommandSender sender) throws CommandException
	{
		if (!sender.canCommandSenderUseCommand(1))
			return Collections.emptyList();
		
		final List<Predicate<Entity>> predList = new ArrayList<Predicate<Entity>>();
		final List<Predicate<Entity>> predList2 = new ArrayList<Predicate<Entity>>();
		Predicate<Entity> typePredicate;
		
		final Vec3 coords = this.coords.eval(sender);
		
		final AxisAlignedBB dBox = this.dPredicate(predList, sender, coords);
		this.rotPredicate(predList, sender);
		this.lPredicate(predList, sender);
		this.mPredicate(predList, sender);
		final AxisAlignedBB rBox = this.rPredicate(predList, sender, new BlockPos(coords));
		this.namePredicate(predList, sender);
		this.teamPredicate(predList, sender);
		
		this.scorePredicate(predList2, sender);
		this.nbtPredicate(predList2, sender);
		
		typePredicate = this.typePredicate(sender);
		
		final AxisAlignedBB box = intersectBoxes(dBox, rBox);
		
		final int predCount = predList.size() + predList2.size();
		final ArrayList<Predicate<Entity>> allPredicates = new ArrayList<>(predCount);
		allPredicates.addAll(predList);
		allPredicates.addAll(predList2);
		
		final Predicate<Entity> allPredicate = Predicates.and(allPredicates);
		
		final ArrayList<Predicate<Entity>> allPredsWithType = new ArrayList<>(predCount + 1);
		if (this.isPlayerSelector)
			this.forcePlayerPredicate(allPredsWithType);
		allPredsWithType.addAll(predList);
		if (typePredicate != null)
			allPredsWithType.add(typePredicate);
		allPredsWithType.addAll(predList2);
		
		final Predicate<Entity> allPredWithType = Predicates.and(allPredsWithType);
		
		final World[] worlds = this.allWorlds ? MinecraftServer.getServer().worldServers : new World[] { sender.getEntityWorld() };
		
		final ArrayList<Entity> matches = new ArrayList<>();
		
		for (final World world : worlds)
		{
			if (this.isPlayerSelector && world.playerEntities.size() < world.loadedEntityList.size() * 16)
			{
				for (final Object player : world.playerEntities)
				{
					if (allPredicate.apply((Entity) player))
						matches.add((Entity) player);
				}
			}
			else
			{
				world.filterEntities(box, matches, allPredWithType);
			}
		}
		
		return this.applySelType(sender, matches, coords);
	}
	
	private List<Entity> applySelType(final ICommandSender sender, final List<Entity> matches, final Vec3 origin) throws CommandException
	{
		final Integer c = CommandArg.eval(this.c, sender);
		
		if (this.selType == SelectorType.r)
		{
			Collections.shuffle(matches);
			
			if (c != null && c != 0)
				return matches.subList(0, Math.min(matches.size(), Math.abs(c)));
			
			return matches.subList(0, Math.min(matches.size(), 1));
		}
		
		final Ordering<Entity> ordering = Ordering.from(new Comparator<Entity>()
		{
			@Override
			public int compare(final Entity e1, final Entity e2)
			{
				return Double.compare(e1.getPositionVector().squareDistanceTo(origin), e2.getPositionVector().squareDistanceTo(origin));
			}
		});
		
		if (c != null)
		{
			if (c < 0)
				return ordering.greatestOf(matches, -c);
			if (c > 0)
				return ordering.leastOf(matches, c);
			
			return ordering.greatestOf(matches, matches.size());
		}
		
		if (this.selType == SelectorType.p)
			return ordering.leastOf(matches, 1);
		
		return matches;
	}
	
	private static AxisAlignedBB intersectBoxes(final AxisAlignedBB box1, final AxisAlignedBB box2)
	{
		return new AxisAlignedBB(Math.max(box1.minX, box2.minX), Math.max(box1.minY, box2.minY), Math.max(box1.minZ, box2.minZ), Math.min(box1.maxX, box2.maxX), Math.min(box1.maxY, box2.maxY), Math.min(box1.maxZ, box2.maxZ));
	}
	
	private void nbtPredicate(final List<Predicate<Entity>> predList2, final ICommandSender sender) throws CommandException
	{
		if (this.nbt == null)
			return;
		
		final NBTTagCompound nbt = this.nbt.eval(sender);
		
		predList2.add(new Predicate<Entity>()
		{
			@Override
			public boolean apply(final Entity e)
			{
				final NBTTagCompound entityNbt = new NBTTagCompound();
				e.writeToNBT(entityNbt);
				
				return NBTBase.compareTags(nbt, entityNbt, true);
			}
		});
	}
	
	private static boolean checkTypeValid(final ICommandSender sender, final String type)
	{
		if (!EntityList.func_180125_b(type))
		{
			final ChatComponentTranslation var3 = new ChatComponentTranslation("commands.generic.entity.invalidType",  type );
			var3.getChatStyle().setColor(EnumChatFormatting.RED);
			sender.addChatMessage(var3);
			return false;
		}
		
		return true;
	}
	
	private void scorePredicate(final List<Predicate<Entity>> predList2, final ICommandSender sender) throws CommandException
	{
		if (this.scores == null)
			return;
		
		final Scoreboard scoreboard = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
		
		final Predicate<Map<ScoreObjective, Score>> predicate = this.scores.eval(sender);
		
		predList2.add(new Predicate<Entity>()
		{
			@Override
			public boolean apply(final Entity e)
			{
				final Map<ScoreObjective, Score> map = scoreboard.getScores(ParsingUtilities.getEntityIdentifier(e));
				
				if (map == null)
					return SelectorEntity.this.nullScoreAllowed;
				
				return predicate.apply(map);
			}
		});
	}
	
	private void mPredicate(final List<Predicate<Entity>> predList, final ICommandSender sender) throws CommandException
	{
		
		if (this.m == null)
			return;
		
		final int m = this.m.eval(sender);
		
		predList.add(new Predicate<Entity>()
		{
			@Override
			public boolean apply(final Entity e)
			{
				if (!(e instanceof EntityPlayerMP))
					return false;
				
				return ((EntityPlayerMP) e).theItemInWorldManager.getGameType().getID() == m;
			}
		});
		
	}
	
	private Predicate<Entity> typePredicate(final ICommandSender sender) throws CommandException
	{
		if (this.type == null)
			return null;
		
		final Set<String> types = filterTypes(this.type.arg.eval(sender), sender);
		
		final boolean inverted = this.type.inverted;
		
		if (types.size() == 0)
			return inverted ? null : Predicates.<Entity> alwaysFalse();
		
		return new Predicate<Entity>()
		{
			@Override
			public boolean apply(final Entity e)
			{
				return inverted != types.contains(SelectorEntity.this.getTypeString(e));
			}
		};
	}
	
	private static Set<String> filterTypes(final List<String> types, final ICommandSender sender) throws CommandException
	{
		final Set<String> ret = new HashSet<String>(types.size());
		
		for (final String type : types)
		{
			if (checkTypeValid(sender, type))
				ret.add(type);
		}
		return ret;
	}
	
	private void forcePlayerPredicate(final ArrayList<Predicate<Entity>> predList)
	{
		predList.add(new Predicate<Entity>()
		{
			@Override
			public boolean apply(final Entity e)
			{
				return e instanceof EntityPlayerMP;
			}
		});
	}
	
	private String getTypeString(final Entity e)
	{
		final String str = EntityList.getEntityString(e);
		
		if (str != null)
			return str;
		
		if (e instanceof EntityPlayer)
			return "Player";
		
		if (e instanceof EntityLightningBolt)
			return "LightningBolt";
		
		return null;
	}
	
	private void teamPredicate(final List<Predicate<Entity>> predList, final ICommandSender sender) throws CommandException
	{
		if (this.team == null)
			return;
		
		final boolean inverted = this.team.inverted;
		
		if (this.team.arg == null)
		{
			predList.add(new Predicate<Entity>()
			{
				@Override
				public boolean apply(final Entity e)
				{
					if (!(e instanceof EntityLivingBase))
						return false;
					
					final Team t = ((EntityLivingBase) e).getTeam();
					return inverted != (t == null);
				}
			});
			return;
		}
		
		final Set<String> teams = new HashSet<>(this.team.arg.eval(sender));
		
		predList.add(new Predicate<Entity>()
		{
			@Override
			public boolean apply(final Entity e)
			{
				if (!(e instanceof EntityLivingBase))
					return false;
				
				final Team t = ((EntityLivingBase) e).getTeam();
				if (t == null)
					return inverted;
				
				return inverted != teams.contains(t.getRegisteredName());
			}
		});
	}
	
	private void namePredicate(final List<Predicate<Entity>> predList, final ICommandSender sender) throws CommandException
	{
		if (this.name == null)
			return;
		
		final Set<String> names = new HashSet<>(this.name.arg.eval(sender));
		
		final boolean inverted = this.name.inverted;
		
		predList.add(new Predicate<Entity>()
		{
			@Override
			public boolean apply(final Entity e)
			{
				return inverted != names.contains(e.getName());
			}
		});
	}
	
	private void lPredicate(final List<Predicate<Entity>> predList, final ICommandSender sender) throws CommandException
	{
		final Integer l = CommandArg.eval(this.l, sender);
		final Integer lm = CommandArg.eval(this.lm, sender);
		
		if (l != null && l >= 0)
		{
			if (lm != null && lm >= 0)
			{
				if (lm > l)
					throw new NumberInvalidException("The value for 'lm' (" + lm + ") must not be bigger than the one for 'l' (" + l + ")");
				
				predList.add(new Predicate<Entity>()
				{
					@Override
					public boolean apply(final Entity e)
					{
						if (!(e instanceof EntityPlayerMP))
							return false;
						
						final EntityPlayerMP player = (EntityPlayerMP) e;
						return lm <= player.experienceLevel && player.experienceLevel <= l;
					}
				});
				return;
			}
			
			predList.add(new Predicate<Entity>()
			{
				@Override
				public boolean apply(final Entity e)
				{
					if (!(e instanceof EntityPlayerMP))
						return false;
					
					final EntityPlayerMP player = (EntityPlayerMP) e;
					return player.experienceLevel <= l;
				}
			});
			return;
		}
		
		if (lm != null && lm > 0)
		{
			predList.add(new Predicate<Entity>()
			{
				@Override
				public boolean apply(final Entity e)
				{
					if (!(e instanceof EntityPlayerMP))
						return false;
					
					final EntityPlayerMP player = (EntityPlayerMP) e;
					return lm <= player.experienceLevel;
				}
			});
		}
	}
	
	private AxisAlignedBB dPredicate(final List<Predicate<Entity>> predList, final ICommandSender sender, final Vec3 origin) throws CommandException
	{
		final Double dx = CommandArg.eval(this.dx, sender);
		final Double dy = CommandArg.eval(this.dy, sender);
		final Double dz = CommandArg.eval(this.dz, sender);
		
		final AxisAlignedBB box = createBox(origin, dx, dy, dz);
		
		predList.add(new Predicate<Entity>()
		{
			@Override
			public boolean apply(final Entity e)
			{
				return e.posX >= box.minX && e.posX <= box.maxX && e.posY >= box.minY && e.posY <= box.maxY && e.posZ >= box.minZ && e.posZ <= box.maxZ;
			}
		});
		return box;
	}
	
	private static AxisAlignedBB createBox(final Vec3 origin, final Double dx, final Double dy, final Double dz)
	{
		double xMin = Double.NEGATIVE_INFINITY;
		double yMin = Double.POSITIVE_INFINITY;
		double zMin = Double.NEGATIVE_INFINITY;
		double xMax = Double.POSITIVE_INFINITY;
		double yMax = Double.NEGATIVE_INFINITY;
		double zMax = Double.POSITIVE_INFINITY;
		
		if (dx != null)
		{
			final boolean xNeg = dx < 0.0;
			xMin = origin.xCoord + (xNeg ? dx : 0);
			xMax = origin.xCoord + (xNeg ? 0 : dx);
		}
		
		if (dy != null)
		{
			final boolean yNeg = dy < 0.0;
			yMin = origin.yCoord + (yNeg ? dy : 0);
			yMax = origin.yCoord + (yNeg ? 0 : dy);
		}
		
		if (dz != null)
		{
			final boolean zNeg = dz < 0.0;
			zMin = origin.zCoord + (zNeg ? dz : 0);
			zMax = origin.zCoord + (zNeg ? 0 : dz);
		}
		
		return new AxisAlignedBB(xMin, yMin, zMin, xMax, yMax, zMax);
	}
	
	private static AxisAlignedBB createBox(final BlockPos origin, final Integer r)
	{
		final int xMin = r != null ? origin.getX() - r : Integer.MIN_VALUE;
		final int yMin = r != null ? origin.getY() - r : Integer.MIN_VALUE;
		final int zMin = r != null ? origin.getZ() - r : Integer.MIN_VALUE;
		final int xMax = r != null ? origin.getX() + r : Integer.MAX_VALUE;
		final int yMax = r != null ? origin.getY() + r : Integer.MAX_VALUE;
		final int zMax = r != null ? origin.getZ() + r : Integer.MAX_VALUE;
		return new AxisAlignedBB(xMin, yMin, zMin, xMax, yMax, zMax);
	}
	
	private AxisAlignedBB rPredicate(final List<Predicate<Entity>> predList, final ICommandSender sender, final BlockPos origin) throws CommandException
	{
		final Integer r = CommandArg.eval(this.r, sender);
		final Integer rm = CommandArg.eval(this.rm, sender);
		
		final AxisAlignedBB box = createBox(origin, r);
		
		if (r != null && r >= 0)
		{
			final int rSq = r * r;
			
			if (rm != null && rm > 0)
			{
				if (rm > r)
					throw new NumberInvalidException("The value for 'rm' (" + rm + ") must not be bigger than the one for 'r' (" + r + ")");
				
				final int rmSq = rm * rm;
				
				predList.add(new Predicate<Entity>()
				{
					@Override
					public boolean apply(final Entity e)
					{
						final int dist = (int) e.func_174831_c(origin);
						return dist <= rSq && dist >= rmSq;
					}
				});
				return box;
			}
			
			predList.add(new Predicate<Entity>()
			{
				@Override
				public boolean apply(final Entity e)
				{
					final int dist = (int) e.func_174831_c(origin);
					return dist <= rSq;
				}
			});
			return box;
		}
		
		if (rm != null && rm > 0)
		{
			final int rmSq = rm * rm;
			
			predList.add(new Predicate<Entity>()
			{
				@Override
				public boolean apply(final Entity e)
				{
					final int dist = (int) e.func_174831_c(origin);
					return dist >= rmSq;
				}
			});
		}
		
		return box;
	}
	
	private void rotPredicate(final List<Predicate<Entity>> predList, final ICommandSender sender) throws CommandException
	{
		final Double ry = CommandArg.eval(this.ry, sender);
		final Double rym = CommandArg.eval(this.rym, sender);
		final Double rx = CommandArg.eval(this.rx, sender);
		final Double rxm = CommandArg.eval(this.rxm, sender);
		
		if (rx != null)
		{
			final double fRx = this.correctAngleX(rx);
			
			if (rxm != null)
			{
				final double fRxm = this.correctAngleX(rxm);
				
				if (fRxm < fRx)
				{
					predList.add(new Predicate<Entity>()
					{
						@Override
						public boolean apply(final Entity e)
						{
							final double yaw = SelectorEntity.this.correctAngleX(e.rotationPitch);
							return yaw >= fRxm && yaw <= fRx;
						}
					});
				}
				else
				{
					predList.add(new Predicate<Entity>()
					{
						@Override
						public boolean apply(final Entity e)
						{
							final double yaw = SelectorEntity.this.correctAngleX(e.rotationPitch);
							return yaw >= fRxm || yaw <= fRx;
						}
					});
				}
			}
			else
			{
				predList.add(new Predicate<Entity>()
				{
					@Override
					public boolean apply(final Entity e)
					{
						return SelectorEntity.this.correctAngleX(e.rotationPitch) <= fRx;
					}
				});
			}
		}
		else if (rxm != null)
		{
			final double fRxm = this.correctAngleX(rxm);
			
			predList.add(new Predicate<Entity>()
			{
				@Override
				public boolean apply(final Entity e)
				{
					return SelectorEntity.this.correctAngleX(e.rotationPitch) >= fRxm;
				}
			});
		}
		
		if (ry != null)
		{
			final double fRy = this.correctAngleY(ry);
			
			if (rym != null)
			{
				final double fRym = this.correctAngleY(rym);
				
				if (fRym < fRy)
				{
					predList.add(new Predicate<Entity>()
					{
						@Override
						public boolean apply(final Entity e)
						{
							final double yaw = SelectorEntity.this.correctAngleY(e.rotationYaw);
							return yaw >= fRym && yaw <= fRy;
						}
					});
				}
				else
				{
					predList.add(new Predicate<Entity>()
					{
						@Override
						public boolean apply(final Entity e)
						{
							final double yaw = SelectorEntity.this.correctAngleY(e.rotationYaw);
							return yaw >= fRym || yaw <= fRy;
						}
					});
				}
			}
			else
			{
				predList.add(new Predicate<Entity>()
				{
					@Override
					public boolean apply(final Entity e)
					{
						return SelectorEntity.this.correctAngleY(e.rotationYaw) <= fRy;
					}
				});
			}
		}
		else if (rym != null)
		{
			final double fRym = this.correctAngleY(rym);
			
			predList.add(new Predicate<Entity>()
			{
				@Override
				public boolean apply(final Entity e)
				{
					return SelectorEntity.this.correctAngleY(e.rotationYaw) >= fRym;
				}
			});
		}
		
	}
	
	private double correctAngleY(double angle)
	{
		angle %= 360.0;
		return angle < 0.0 ? angle + 360.0 : angle;
	}
	
	private double correctAngleX(double angle)
	{
		angle %= 360.0;
		if (angle > 180.0)
			angle -= 360.0;
		return angle < -180.0 ? angle + 360.0 : angle;
	}
}
