package net.minecraft.command.selectors.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Ordering;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.ParsingUtilities;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.TypedWrapper.Getter;
import net.minecraft.command.arg.TypedWrapper.SimpleGetter;
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
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SelectorEntity extends CommandArg<List<Entity>>
{
	
	private final SelectorType selType;
	private final boolean isPlayerSelector;
	
	private final CommandArg<Vec3> coords;
	private final Getter<Integer> r;
	private final Getter<Integer> rm;
	private final Getter<Double> dx;
	private final Getter<Double> dy;
	private final Getter<Double> dz;
	private final Getter<Vec3> dxyz;
	private final Getter<Integer> c;
	private final Getter<Integer> m;
	private final Getter<Integer> l;
	private final Getter<Integer> lm;
	private final Getter<Double> rx;
	private final Getter<Double> rxm;
	private final Getter<Double> ry;
	private final Getter<Double> rym;
	private final InvertableArg name;
	private final InvertableArg team;
	private final InvertableArg type;
	private final SimpleGetter<Predicate<Map<ScoreObjective, Score>>> scores;
	
	private final boolean nullScoreAllowed;
	
	private final Getter<NBTTagCompound> nbt;
	
	private final boolean allWorlds;
	
	public static enum SelectorType
	{
		p, e, r, a;
	}
	
	public SelectorEntity(final SelectorType selType, final ExParserData parserData)
	{
		this.selType = selType;
		
		final Getter<Vec3> coords = SelectorConstructable.getParam(TypeIDs.Coordinates, "xyz", parserData);
		
		if (coords == null)
		{
			final Getter<Double> x = SelectorConstructable.getParam(TypeIDs.Double, 0, "x", parserData);
			final Getter<Double> y = SelectorConstructable.getParam(TypeIDs.Double, 1, "y", parserData);
			final Getter<Double> z = SelectorConstructable.getParam(TypeIDs.Double, 2, "z", parserData);
			
			this.coords = new Coordinates(
				x == null ? SingleCoordinate.tildexNC : x.commandArg(),
				y == null ? SingleCoordinate.tildeyNC : y.commandArg(),
				z == null ? SingleCoordinate.tildezNC : z.commandArg());
		}
		else
			this.coords = coords.commandArg();
		
		this.r = SelectorConstructable.getParam(TypeIDs.Integer, 3, "r", parserData);
		this.rm = SelectorConstructable.getParam(TypeIDs.Integer, "rm", parserData);
		this.dx = SelectorConstructable.getParam(TypeIDs.Double, "dx", parserData);
		this.dy = SelectorConstructable.getParam(TypeIDs.Double, "dy", parserData);
		this.dz = SelectorConstructable.getParam(TypeIDs.Double, "dz", parserData);
		this.dxyz = SelectorConstructable.getParam(TypeIDs.Coordinates, "dxyz", parserData);
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
		
		final PatriciaTrie<MutablePair<Getter<Integer>, Getter<Integer>>> pScores = parserData.primitiveScores;
		
		this.nullScoreAllowed = parserData.nullScoreAllowed && pScores.isEmpty();
		
		this.scores = pScores == null ? null : this.procScores(pScores);
		
		this.isPlayerSelector = selType == SelectorType.a || selType == SelectorType.p || (selType == SelectorType.r && this.type == null);
		
		this.allWorlds = !(this.r != null || this.rm != null || this.dx != null || this.dy != null || this.dz != null || this.coords != null);
	}
	
	public SimpleGetter<Predicate<Map<ScoreObjective, Score>>> procScores(final Map<String, MutablePair<Getter<Integer>, Getter<Integer>>> pScores)
	{
		return new SimpleGetter<Predicate<Map<ScoreObjective, Score>>>()
		{
			@Override
			public Predicate<Map<ScoreObjective, Score>> get() throws CommandException
			{
				final List<Predicate<Map<ScoreObjective, Score>>> predicates = new ArrayList<>(pScores.size());
				
				final Scoreboard scoreboard = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
				
				for (final Entry<String, MutablePair<Getter<Integer>, Getter<Integer>>> score : pScores.entrySet())
				{
					final ScoreObjective objective = scoreboard.getObjective(score.getKey());
					
					if (objective == null)
						throw new CommandException("Objective not found: " + score.getKey());
					
					final Getter<Integer> minArg = score.getValue().left;
					final Getter<Integer> maxArg = score.getValue().right;
					
					if (minArg != null)
					{
						final int min = minArg.get();
						
						if (maxArg != null)
						{
							final int max = maxArg.get();
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
					
					final int max = maxArg.get();
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
		if (!sender.canCommandSenderUseCommand(1, "@"))
			return Collections.emptyList();
		
		final List<Predicate<Entity>> predList = new ArrayList<Predicate<Entity>>();
		final List<Predicate<Entity>> predList2 = new ArrayList<Predicate<Entity>>();
		Predicate<Entity> typePredicate;
		
		final Vec3 coords = this.coords.eval(sender);
		
		final AxisAlignedBB dBox = this.dPredicate(predList, coords);
		this.rotPredicate(predList);
		this.lPredicate(predList);
		this.mPredicate(predList);
		final AxisAlignedBB rBox = this.rPredicate(predList, new BlockPos(coords));
		this.namePredicate(predList);
		this.teamPredicate(predList);
		
		this.scorePredicate(predList2);
		this.nbtPredicate(predList2);
		
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
			if (this.isPlayerSelector && world.playerEntities.size() < world.loadedEntityList.size() * 16)
			{
				for (final Object player : world.playerEntities)
					if (allPredicate.apply((Entity) player))
						matches.add((Entity) player);
			}
			else
				world.filterEntities(box, matches, allPredWithType);
		
		return this.applySelType(matches, coords);
	}
	
	private List<Entity> applySelType(final List<Entity> matches, final Vec3 origin) throws CommandException
	{
		final Integer c = get(this.c);
		
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
	
	private void nbtPredicate(final List<Predicate<Entity>> predList2) throws CommandException
	{
		if (this.nbt == null)
			return;
		
		final NBTTagCompound nbt = this.nbt.get();
		
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
			final ChatComponentTranslation var3 = new ChatComponentTranslation("commands.generic.entity.invalidType", type);
			var3.getChatStyle().setColor(EnumChatFormatting.RED);
			sender.addChatMessage(var3);
			return false;
		}
		
		return true;
	}
	
	private void scorePredicate(final List<Predicate<Entity>> predList2) throws CommandException
	{
		if (this.scores == null)
			return;
		
		final Scoreboard scoreboard = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
		
		final Predicate<Map<ScoreObjective, Score>> predicate = this.scores.get();
		
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
	
	private void mPredicate(final List<Predicate<Entity>> predList) throws CommandException
	{
		
		if (this.m == null)
			return;
		
		final int m = this.m.get();
		
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
		
		final Set<String> types = filterTypes(this.type.arg.get(), sender);
		
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
			if (checkTypeValid(sender, type))
				ret.add(type);
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
	
	private void teamPredicate(final List<Predicate<Entity>> predList) throws CommandException
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
		
		final Set<String> teams = new HashSet<>(this.team.arg.get());
		
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
	
	private void namePredicate(final List<Predicate<Entity>> predList) throws CommandException
	{
		if (this.name == null)
			return;
		
		final Set<String> names = new HashSet<>(this.name.arg.get());
		
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
	
	private void lPredicate(final List<Predicate<Entity>> predList) throws CommandException
	{
		final Integer l = get(this.l);
		final Integer lm = get(this.lm);
		
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
	
	private AxisAlignedBB dPredicate(final List<Predicate<Entity>> predList, final Vec3 origin) throws CommandException
	{
		final Vec3 dxyz = get(this.dxyz);
		
		final AxisAlignedBB box = this.dxyz == null
			? createBox(origin, get(this.dx), get(this.dy), get(this.dz))
			: createBox(origin, dxyz.xCoord, dxyz.yCoord, dxyz.zCoord);
		
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
	
	private AxisAlignedBB rPredicate(final List<Predicate<Entity>> predList, final BlockPos origin) throws CommandException
	{
		final Integer r = get(this.r);
		final Integer rm = get(this.rm);
		
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
	
	private void rotPredicate(final List<Predicate<Entity>> predList) throws CommandException
	{
		final Double rx = get(this.rx);
		final Double rxm = get(this.rxm);
		
		final Double ry = get(this.ry);
		final Double rym = get(this.rym);
		
		if (rx != null)
		{
			final double fRx = wrapAngleTo90(rx);
			
			if (rxm != null)
			{
				final double fRxm = wrapAngleTo90(rxm);
				
				if (fRxm < fRx)
					predList.add(new Predicate<Entity>()
					{
						@Override
						public boolean apply(final Entity e)
						{
							final double pitch = e.rotationPitch;
							return pitch >= fRxm && pitch <= fRx;
						}
					});
				else
					predList.add(new Predicate<Entity>()
					{
						@Override
						public boolean apply(final Entity e)
						{
							final double pitch = e.rotationPitch;
							return pitch >= fRxm || pitch <= fRx;
						}
					});
			}
			else
				predList.add(new Predicate<Entity>()
				{
					@Override
					public boolean apply(final Entity e)
					{
						return e.rotationPitch <= fRx;
					}
				});
		}
		else if (rxm != null)
		{
			final double fRxm = wrapAngleTo90(rxm);
			
			predList.add(new Predicate<Entity>()
			{
				@Override
				public boolean apply(final Entity e)
				{
					return e.rotationPitch >= fRxm;
				}
			});
		}
		
		if (ry != null)
		{
			final double fRy = MathHelper.wrapAngleTo180_double(ry);
			
			if (rym != null)
			{
				final double fRym = MathHelper.wrapAngleTo180_double(rym);
				
				if (fRym < fRy)
					predList.add(new Predicate<Entity>()
					{
						@Override
						public boolean apply(final Entity e)
						{
							final double yaw = e.rotationYaw;
							return yaw >= fRym && yaw <= fRy;
						}
					});
				else
					predList.add(new Predicate<Entity>()
					{
						@Override
						public boolean apply(final Entity e)
						{
							final double yaw = e.rotationYaw;
							return yaw >= fRym || yaw <= fRy;
						}
					});
			}
			else
				predList.add(new Predicate<Entity>()
				{
					@Override
					public boolean apply(final Entity e)
					{
						return e.rotationYaw <= fRy;
					}
				});
		}
		else if (rym != null)
		{
			final double fRym = MathHelper.wrapAngleTo180_double(rym);
			
			predList.add(new Predicate<Entity>()
			{
				@Override
				public boolean apply(final Entity e)
				{
					return e.rotationYaw >= fRym;
				}
			});
		}
	}
	
	private static double wrapAngleTo90(double angle)
	{
		angle %= 360.0D;
		
		if (angle < -270)
			return 360.0 + angle;
		
		if (angle < -90.0)
			return -180.0 - angle;
		
		if (angle > 270.0)
			return angle - 360.0;
		
		if (angle > 90.0)
			return 180.0 - angle;
		
		return angle;
	}
}
