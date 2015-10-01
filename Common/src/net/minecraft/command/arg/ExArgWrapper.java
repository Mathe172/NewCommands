package net.minecraft.command.arg;

import net.minecraft.command.arg.CommandArg.Primitive;
import net.minecraft.command.type.management.TypeID;

public class ExArgWrapper<R, A extends CommandArg<R>> extends ArgWrapper<R>
{
	public final A arg;
	
	public ExArgWrapper(final TypeID<R> type, final A arg)
	{
		super(type);
		this.arg = arg;
	}
	
	@Override
	public A arg()
	{
		return this.arg;
	}
	
	public static class GetterWrapper<R> extends ExArgWrapper<R, Primitive<R>> implements TypedWrapper<R>
	{
		public GetterWrapper(final TypeID<R> type, final Primitive<R> arg)
		{
			super(type, arg);
		}
		
		@Override
		public Getter<R> get()
		{
			return this.arg();
		}
		
		// This is checked...
		@SuppressWarnings("unchecked")
		@Override
		public <T> Primitive<T> get(final TypeID<T> type)
		{
			this.checkTypes(type);
			
			return (Primitive<T>) this.arg();
		}
		
		@Override
		public TypeID<R> type()
		{
			return this.type;
		}
	}
}
