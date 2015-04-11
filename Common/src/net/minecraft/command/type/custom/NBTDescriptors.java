package net.minecraft.command.type.custom;

import net.minecraft.command.construction.NBTConstructor;
import net.minecraft.command.construction.NBTConstructorList;
import net.minecraft.command.type.custom.nbt.NBTDescriptor;

public class NBTDescriptors
{
	public static final NBTConstructor entity = new NBTConstructor();
	public static final NBTConstructor block = new NBTConstructor();
	public static final NBTConstructor item = new NBTConstructor();
	
	public static void init()
	{
		final NBTConstructorList resultStats = new NBTConstructorList().then(new NBTConstructor()
			.key("AffectedBlocksName",
				"AffectedBlocksObjective",
				"AffectedEntitiesName",
				"AffectedEntitiesObjective",
				"AffectedItemsName",
				"AffectedItemsObjective",
				"QueryResultName",
				"QueryResultObjective",
				"SuccessCountName",
				"SuccessCountObjective"));
		
		final NBTConstructorList blockList = new NBTConstructorList(Completers.blockCompleter);
		final NBTConstructorList effectList = new NBTConstructorList(new NBTConstructor()
			.key("Ambient",
				"Amplifier",
				"Duration",
				"Id",
				"ShowParticles"));
		final NBTConstructorList enchantmentList = new NBTConstructorList(new NBTConstructor()
			.key("id",
				"lvl"));
		
		final NBTConstructorList itemList = new NBTConstructorList(item);
		
		item.key("Count",
			"Damage")
			.key("id", Completers.itemCompleter)
			.sKey("Slot")
			.key("tag", new NBTConstructor()
				.key("CanDestroy", blockList)
				.key("CanPlaceOn", blockList)
				.key("display", new NBTConstructor()
					.key("Name")
					.key("Lore", NBTDescriptor.defaultTagList))
				.key("HideFlags",
					"Unbreakable")
				.sKey("map_is_scaling")
				.sKey("author",
					"generation",
					"resolved",
					"title")
				.sKey("pages", NBTDescriptor.defaultTagList)
				.sKey("Fireworks", new NBTConstructor()
					.key("Explosions", NBTDescriptor.defaultTagList)
					.key("Flight"))
				.sKey("Explosion", new NBTConstructor()
					.key("Colors", NBTDescriptor.defaultTagList)
					.key("FadeColors", NBTDescriptor.defaultTagList)
					.key("Flicker",
						"Trail",
						"Type"))
				.sKey("RepairCost")
				.sKey("display", new NBTConstructor()
					.key("color"))
				.key("AttributeModifiers", new NBTConstructorList(new NBTConstructor()
					.key("Amount",
						"Name",
						"AttributeName",
						"Operation")
					.sKey("UUIDLeast",
						"UUIDMost")))
				.sKey("SkullOwner")
				.key("BlockEntityTag", block)
				.sKey("CustomPotionEffects", effectList)
				.sKey("pages", NBTDescriptor.defaultTagList)
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
			
			.key("Motion", NBTDescriptor.defaultTagList)
			.key("Rotation", NBTDescriptor.defaultTagList)
			.key("Pos", NBTDescriptor.defaultTagList)
			
			.sKey("OnGround",
				"PortalCooldown")
			.key("Riding", entity)
			.sKey("UUID",
				"UUIDLeast",
				"UUIDMost")
			.sKey("AbsorptionAmount")
			.key("ActiveEffects", effectList)
			.sKey("AttackTime")
			.key("Attributes", new NBTConstructorList(new NBTConstructor()
				.key("Name",
					"Base")
				.key("Modifiers", new NBTConstructorList(new NBTConstructor()
					.key("Amount",
						"Name",
						"Operation")
					.sKey("UUIDLeast",
						"UUIDMost")))))
			.sKey("CanPickUpLoot",
				"DeathTime",
				"Equipment",
				"HealF",
				"Health",
				"HurtByTimestamp",
				"HurtTime")
			.sKey("DropChances", NBTDescriptor.defaultTagList)
			.sKey("Leash", new NBTConstructor()
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
			.sKey("Offers", new NBTConstructor()
				.key("Recipies", new NBTConstructorList(new NBTConstructor()
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
			.sKey("direction", NBTDescriptor.defaultTagList)
			.sKey("FireworksItem", item)
			.sKey("ownerName", Completers.userCompleter)
			.sKey("Potion", item)
			.sKey("Pose", new NBTConstructor()
				.key("Head", NBTDescriptor.defaultTagList)
				.key("Body", NBTDescriptor.defaultTagList)
				.key("LeftArm", NBTDescriptor.defaultTagList)
				.key("RightArm", NBTDescriptor.defaultTagList)
				.key("LeftLeg", NBTDescriptor.defaultTagList)
				.key("RightLeg", NBTDescriptor.defaultTagList));
		
		block.key("CustomName",
			"Lock")
			.key("id", Completers.blockCompleter)
			.key("x",
				"y",
				"z")
			.sKey("Base")
			.sKey("Patterns", new NBTConstructorList(new NBTConstructor()
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
			.sKey("Owner", new NBTConstructor()
				.key("Id")
				.key("Name", Completers.userCompleter)
				.key("Properties", new NBTConstructor()
					.key("textures", new NBTConstructorList(new NBTConstructor()
						.key("Signature",
							"Value")))))
			.sKey("EntityId", Completers.entityID)
			.sKey("SpawnData", entity)
			.sKey("SpawnPotentials", new NBTConstructorList(new NBTConstructor()
				.key("Properties", entity)
				.key("Type", Completers.entityID)
				.key("Weight")))
			.sKey("RecordItem", item)
			.sKey("blockId", Completers.blockCompleter);
	}
}