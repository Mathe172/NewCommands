package net.minecraft.command.collections;

import net.minecraft.command.IPermission;
import net.minecraft.command.construction.RegistrationHelper;
import net.minecraft.command.selectors.PrimitiveWrapper;
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
import net.minecraft.command.type.custom.coordinate.TypeCoordinates;
import net.minecraft.command.type.custom.nbt.TypeNBTBase;

public final class Selectors extends RegistrationHelper
{
	private Selectors()
	{
	}
	
	public static final void init()
	{
		register("s", selector(IPermission.unrestricted, TypeIDs.ICmdSender)
			.construct(SelectorSelf.constructable));
		
		register("t", selector(IPermission.unrestricted, TypeIDs.Integer)
			.then("cmd", TypeCommand.parserSingleCmd)
			.construct(SelectorTiming.constructable));
		
		register("c", selector(IPermission.unrestricted, TypeIDs.Double, TypeIDs.Integer)
			.then(TypeUntypedOperator.parser)
			.construct(PrimitiveWrapper.constructable));
		
		register("o", selector(IPermission.level2, TypeIDs.ScoreObjective)
			.then(TypeScoreObjective.type)
			.construct(PrimitiveWrapper.constructable));
		
		register("sc", selector(IPermission.level2, TypeIDs.Integer)
			.then("objective", TypeScoreObjective.type)
			.then("target", Types.scoreHolder)
			.construct(SelectorScore.constructable));
		
		register("n", selector(IPermission.level2, TypeIDs.NBTBase)
			.then(new TypeAlternatives(
				TypeCoordinates.nonCentered,
				Types.entity,
				TypeNBTBase.parserDefault,
				ParserName.parser))
			.then(ParserName.parser)
			.construct(SelectorNBT.constructable));
		
		register("p", new SelectorDescriptorEntity(SelectorType.p));
		register("a", new SelectorDescriptorEntity(SelectorType.a));
		register("r", new SelectorDescriptorEntity(SelectorType.r));
		register("e", new SelectorDescriptorEntity(SelectorType.e));
	}
}
