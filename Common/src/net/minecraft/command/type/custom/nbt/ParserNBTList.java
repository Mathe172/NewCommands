package net.minecraft.command.type.custom.nbt;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.arg.CommandArg;
import net.minecraft.command.arg.PrimitiveParameter;
import net.minecraft.command.parser.CompletionException;
import net.minecraft.command.parser.Parser;
import net.minecraft.command.type.custom.nbt.NBTUtilities.NBTData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;

public class ParserNBTList
{
	private final NBTDescriptor.List descriptor;
	
	public ParserNBTList(final NBTDescriptor.List descriptor)
	{
		this.descriptor = descriptor;
	}
	
	public static class ListData extends NBTData
	{
		public final List<NBTBase> primitiveData = new ArrayList<>();
		public final List<CommandArg<NBTBase>> data = new ArrayList<>();
		
		@Override
		public void put(final NBTBase data)
		{
			if (this.data.isEmpty())
				this.primitiveData.add(data);
			else
				this.data.add(new PrimitiveParameter<>(data));
		}
		
		@Override
		public void put(final CommandArg<NBTBase> data)
		{
			this.data.add(data);
		}
		
	}
	
	public void parse(final Parser parser, final NBTData parserData) throws SyntaxErrorException, CompletionException
	{
		parser.terminateCompletion();
		
		final ListData data = new ListData();
		
		final Matcher m = parser.listEndMatcher;
		
		for (int i = 0;; ++i)
		{
			this.descriptor.getTagDescriptor(i).getTagParser().parse(parser, data);
			
			if (!parser.findInc(m))
				throw parser.SEE("No delimiter found while parsing tag list around index ");
			
			if ("]".equals(m.group(1)))
			{
				if (data.data.isEmpty())
				{
					final NBTTagList list = new NBTTagList();
					
					for (final NBTBase item : data.primitiveData)
						list.appendTag(item);
					
					parserData.put(list);
				}
				else
				{
					if (data.primitiveData.isEmpty())
					{
						parserData.put(new CommandArg<NBTBase>()
						{
							@Override
							public NBTTagList eval(final ICommandSender sender) throws CommandException
							{
								final NBTTagList list = new NBTTagList();
								
								for (final CommandArg<NBTBase> item : data.data)
									list.appendTag(item.eval(sender));
								
								return list;
							}
						});
					}
					else
					{
						final NBTTagList list = new NBTTagList();
						for (final NBTBase item : data.primitiveData)
							list.appendTag(item);
						
						parserData.put(new CommandArg<NBTBase>()
						{
							private boolean firstRun = true;
							
							@Override
							public NBTTagList eval(final ICommandSender sender) throws CommandException
							{
								if (this.firstRun)
								{
									this.firstRun = false;
									
									for (final CommandArg<NBTBase> item : data.data)
										list.appendTag(item.eval(sender));
								}
								else
								{
									int i = data.primitiveData.size();
									
									for (final CommandArg<NBTBase> item : data.data)
									{
										list.set(i, item.eval(sender));
										++i;
									}
								}
								
								return list;
							}
						});
					}
					
				}
				return;
			}
			
			if ("}".equals(m.group(1)))
				throw parser.SEE("Unexpected '}' around index ");
		}
	}
}
