package net.minecraft.command.collections;

import net.minecraft.command.IPermission;
import net.minecraft.command.construction.RegistrationHelper;
import net.minecraft.command.selectors.PrimitiveWrapper;
import net.minecraft.command.selectors.SelectorBlock;
import net.minecraft.command.selectors.SelectorNBT;
import net.minecraft.command.selectors.SelectorScore;
import net.minecraft.command.selectors.SelectorSelf;
import net.minecraft.command.selectors.SelectorTiming;
import net.minecraft.command.selectors.entity.SelectorDescriptorEntity;
import net.minecraft.command.selectors.entity.SelectorEntity.SelectorType;
import net.minecraft.command.type.custom.ParserName;
import net.minecraft.command.type.custom.TypeAlternatives;
import net.minecraft.command.type.custom.TypeScoreObjective;
import net.minecraft.command.type.custom.TypeUntypedOperator;
import net.minecraft.command.type.custom.command.TypeCommand;
import net.minecraft.command.type.custom.coordinate.TypeBlockPos;
import net.minecraft.command.type.custom.coordinate.TypeCoordinates;
import net.minecraft.command.type.custom.nbt.TypeNBTBase;

public final class Selectors extends RegistrationHelper
{
	private Selectors()
	{
	}
	
	public static final void init()
	{
		register("s", selector(IPermission.unrestricted, SelectorSelf.constructable, TypeIDs.ICmdSender));
		
		register("t",
			selector(
				"cmd",
				TypeCommand.parserSingleCmd,
				SelectorTiming.constructable,
				IPermission.unrestricted,
				TypeIDs.Integer));
		
		register("c",
			selector(
				TypeUntypedOperator.parser,
				PrimitiveWrapper.constructable,
				IPermission.unrestricted,
				TypeIDs.Double,
				TypeIDs.Integer,
				TypeIDs.Coordinates,
				TypeIDs.NBTBase,
				TypeIDs.Boolean));
		
		register("o",
			selector(
				TypeScoreObjective.type,
				PrimitiveWrapper.constructable,
				IPermission.level2,
				TypeIDs.ScoreObjective));
		
		register("sc", selector(IPermission.level2, TypeIDs.Integer)
			.then("o", TypeScoreObjective.type)
			.then("t", Types.scoreHolder)
			.construct(SelectorScore.constructable));
		
		register("n", selector(IPermission.level2, TypeIDs.NBTBase)
			.then(new TypeAlternatives<>(
				TypeCoordinates.nonCentered,
				Types.entity,
				TypeNBTBase.parserDefault,
				ParserName.parser))
			.then(ParserName.parser)
			.construct(SelectorNBT.constructable));
		
		register("b", selector(IPermission.level2, TypeIDs.BlockState)
			.then(TypeBlockPos.parser)
			.construct(SelectorBlock.constructable));
		
		register("p", new SelectorDescriptorEntity(SelectorType.p));
		register("a", new SelectorDescriptorEntity(SelectorType.a));
		register("r", new SelectorDescriptorEntity(SelectorType.r));
		register("e", new SelectorDescriptorEntity(SelectorType.e));
	}
}
