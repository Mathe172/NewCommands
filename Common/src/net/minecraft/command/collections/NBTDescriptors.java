package net.minecraft.command.collections;

import net.minecraft.command.construction.NBTConstructor;
import net.minecraft.command.construction.NBTConstructor.ConstructionHelper;
import net.minecraft.command.construction.NBTConstructorList;
import net.minecraft.command.type.custom.TypeScoreObjective;

public final class NBTDescriptors extends ConstructionHelper
{
	public static final NBTConstructor entity = compound();
	public static final NBTConstructor block = compound();
	public static final NBTConstructor item = compound();
	
	private NBTDescriptors()
	{
	}
	
	static
	{
		final NBTConstructorList resultStats = list(compound()
			.key("AffectedBlocksName", Completers.scoreHolder)
			.key("AffectedBlocksObjective", TypeScoreObjective.writeableCompleter)
			.key("AffectedEntitiesName", Completers.scoreHolder)
			.key("AffectedEntitiesObjective", TypeScoreObjective.writeableCompleter)
			.key("AffectedItemsName", Completers.scoreHolder)
			.key("AffectedItemsObjective", TypeScoreObjective.writeableCompleter)
			.key("QueryResultName", Completers.scoreHolder)
			.key("QueryResultObjective", TypeScoreObjective.writeableCompleter)
			.key("SuccessCountName", Completers.scoreHolder)
			.key("SuccessCountObjective", TypeScoreObjective.writeableCompleter));
		
		final NBTConstructorList blockList = list(Completers.blockCompleter);
		final NBTConstructorList effectList = list(compound()
			.key("Ambient",
				"Amplifier",
				"Duration",
				"Id",
				"ShowParticles"));
		final NBTConstructorList enchantmentList = list(compound()
			.key("id",
				"lvl"));
		
		final NBTConstructorList itemList = list(item);
		
		item.key("Count",
			"Damage")
			.key("id", Completers.itemCompleter)
			.sKey("Slot")
			.key("tag", compound()
				.key("CanDestroy", blockList)
				.key("CanPlaceOn", blockList)
				.key("display", compound()
					.key("Name")
					.key("Lore", defList))
				.key("HideFlags",
					"Unbreakable")
				.sKey("map_is_scaling")
				.sKey("author",
					"generation",
					"resolved",
					"title")
				.sKey("pages", defList)
				.sKey("Fireworks", compound()
					.key("Explosions", defList)
					.key("Flight"))
				.sKey("Explosion", compound()
					.key("Colors", defList)
					.key("FadeColors", defList)
					.key("Flicker",
						"Trail",
						"Type"))
				.sKey("RepairCost")
				.sKey("display", compound()
					.key("color"))
				.key("AttributeModifiers", list(compound()
					.key("Amount",
						"Name",
						"AttributeName",
						"Operation")
					.sKey("UUIDLeast",
						"UUIDMost")))
				.sKey("SkullOwner")
				.key("BlockEntityTag", block)
				.sKey("CustomPotionEffects", effectList)
				.sKey("pages", defList)
				.sKey("StoredEnchantments", enchantmentList)
				.sKey("ench", enchantmentList));
		
		entity.sKey("Air")
			.key("CommandStats", resultStats)
			.key("CustomName",
				"Invulnerable")
			.sKey("CustomNameVisible",
				"Dimension",
				"FallDistance",
				"Fire")
			.key("id", Completers.entityID)
			.key("Equipment", itemList)
			
			.key("Motion", defList)
			.key("Rotation", defList)
			.key("Pos", defList)
			
			.sKey("OnGround",
				"PortalCooldown")
			.key("Riding", entity)
			.sKey("UUID",
				"UUIDLeast",
				"UUIDMost")
			.sKey("AbsorptionAmount")
			.key("ActiveEffects", effectList)
			.sKey("AttackTime")
			.key("Attributes", list(compound()
				.key("Name",
					"Base")
				.key("Modifiers", list(compound()
					.key("Amount",
						"Name",
						"Operation")
					.sKey("UUIDLeast",
						"UUIDMost")))))
			.sKey("CanPickUpLoot",
				"DeathTime",
				"HealF",
				"Health",
				"HurtByTimestamp",
				"HurtTime")
			.sKey("DropChances", defList)
			.sKey("Leash", compound()
				.key("UUIDLeast",
					"UUIDMost",
					"X",
					"Y",
					"Z"))
			.sKey("Leashed")
			.key("NoAI",
				"PersistenceRequired",
				"Silent")
			.sKey("Inventory", itemList)
			.sKey("Offers", compound()
				.key("Recipies", list(compound()
					.key("buy", item)
					.key("buyB", item)
					.key("sell", item)
					.key("maxUses",
						"rewardExp",
						"uses"))))
			.sKey("carried", Completers.blockCompleter)
			.sKey("ArmorItem", item)
			.sKey("Items", itemList)
			.sKey("SaddleItem", item)
			.sKey("DisplayTile", Completers.blockCompleter)
			.sKey("Block", Completers.blockCompleter)
			.sKey("TileEntityData", block)
			.sKey("TileID", Completers.blockCompleter)
			.key("Item", item)
			.sKey("inTile", Completers.blockCompleter)
			.sKey("direction", defList)
			.sKey("FireworksItem", item)
			.sKey("ownerName", Completers.userCompleter)
			.sKey("Potion", item)
			.sKey("Pose", compound()
				.key("Head", defList)
				.key("Body", defList)
				.key("LeftArm", defList)
				.key("RightArm", defList)
				.key("LeftLeg", defList)
				.key("RightLeg", defList));
		
		block.key("CustomName",
			"Lock")
			.key("id", Completers.blockCompleter)
			.key("x",
				"y",
				"z")
			.sKey("Base")
			.sKey("Patterns", list(compound()
				.key("Color",
					"Pattern")))
			.key("Command")
			.sKey("CommandStats", resultStats)
			.sKey("LastOutput",
				"SuccessCount",
				"TrackOutput")
			.sKey("Data")
			.sKey("Item", Completers.blockCompleter)
			.sKey("BrewTime")
			.key("Items", itemList)
			.sKey("OutputSignal")
			.sKey("ExtraType",
				"Rot",
				"SkullType")
			.sKey("Owner", compound()
				.key("Id")
				.key("Name", Completers.userCompleter)
				.key("Properties", compound()
					.key("textures", list(compound()
						.key("Signature",
							"Value")))))
			.sKey("EntityId", Completers.entityID)
			.sKey("SpawnData", entity)
			.sKey("SpawnPotentials", list(compound()
				.key("Properties", entity)
				.key("Type", Completers.entityID)
				.key("Weight")))
			.sKey("RecordItem", item)
			.sKey("blockId", Completers.blockCompleter);
	}
}