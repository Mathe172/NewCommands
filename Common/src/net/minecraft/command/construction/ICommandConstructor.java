package net.minecraft.command.construction;

import java.util.Set;

import net.minecraft.command.type.IDataType;

public interface ICommandConstructor
{
	public Set<CommandProtoDescriptor> baseCommands();
	
	public Set<CommandProtoDescriptor> ends();
	
	public ICommandConstructor then(final IDataType<?> arg);
	
	public ICommandConstructor sub(final ICommandConstructor... subCommands);
	
	public P sub(final P... subCommands);
	
	public C sub(final C... subCommands);
	
	public CP sub(final CP... subCommands);
	
	public ICommandConstructor optional(final IDataType<?> arg);
	
	public ICommandConstructor optional(final IDataType<?> arg, final CommandConstructable constructable);
	
	public ICommandConstructor optional(final ICommandConstructor... commands);
	
	public static interface P extends ICommandConstructor
	{
		@Override
		public P then(final IDataType<?> arg);
		
		@Override
		public P sub(final ICommandConstructor... subCommands);
		
		@Override
		public CP sub(final C... subCommands);
		
		@Override
		public P optional(final IDataType<?> arg);
		
		@Override
		public P optional(final IDataType<?> arg, final CommandConstructable constructable);
		
		@Override
		public P optional(final ICommandConstructor... commands);
	}
	
	public static interface C extends ICommandConstructor
	{
		@Override
		public C then(final IDataType<?> arg);
		
		@Override
		public C sub(final ICommandConstructor... subCommands);
		
		@Override
		public CP sub(final P... subCommands);
		
		@Override
		public C optional(final IDataType<?> arg);
		
		@Override
		public C optional(final IDataType<?> arg, final CommandConstructable constructable);
		
		@Override
		public C optional(final ICommandConstructor... commands);
	}
	
	public static interface U extends ICommandConstructor
	{
		@Override
		public U then(final IDataType<?> arg);
		
		@Override
		public U sub(final ICommandConstructor... subCommands);
		
		@Override
		public PU sub(final P... subCommands);
		
		@Override
		public CU sub(final C... subCommands);
		
		@Override
		public CPU sub(final CP... subCommands);
		
		@Override
		public U optional(final IDataType<?> arg);
		
		@Override
		public U optional(final IDataType<?> arg, final CommandConstructable constructable);
		
		@Override
		public U optional(final ICommandConstructor... commands);
	}
	
	public static interface CP extends P, C
	{
		@Override
		public CP then(final IDataType<?> arg);
		
		@Override
		public CP sub(final ICommandConstructor... subCommands);
		
		@Override
		public CP optional(final IDataType<?> arg);
		
		@Override
		public CP optional(final IDataType<?> arg, final CommandConstructable constructable);
		
		@Override
		public CP optional(final ICommandConstructor... commands);
	}
	
	public static interface PU extends P, U
	{
		@Override
		public PU then(final IDataType<?> arg);
		
		@Override
		public PU sub(final ICommandConstructor... subCommands);
		
		@Override
		public CPU sub(final C... subCommands);
		
		@Override
		public PU optional(final IDataType<?> arg);
		
		@Override
		public PU optional(final IDataType<?> arg, final CommandConstructable constructable);
		
		@Override
		public PU optional(final ICommandConstructor... commands);
	}
	
	public static interface CU extends C, U
	{
		@Override
		public CU then(final IDataType<?> arg);
		
		@Override
		public CU sub(final ICommandConstructor... subCommands);
		
		@Override
		public CPU sub(final P... subCommands);
		
		@Override
		public CU optional(final IDataType<?> arg);
		
		@Override
		public CU optional(final IDataType<?> arg, final CommandConstructable constructable);
		
		@Override
		public CU optional(final ICommandConstructor... commands);
	}
	
	public static interface CPU extends PU, CU, CP
	{
		@Override
		public CPU then(final IDataType<?> arg);
		
		@Override
		public CPU sub(final ICommandConstructor... subCommands);
		
		@Override
		public CPU optional(final IDataType<?> arg);
		
		@Override
		public CPU optional(final IDataType<?> arg, final CommandConstructable constructable);
		
		@Override
		public CPU optional(final ICommandConstructor... commands);
	}
}
