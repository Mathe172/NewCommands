package net.minecraft.command.parser;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.minecraft.command.arg.CommandArg;

public final class ParsingManager
{
	private ParsingManager()
	{
	}
	
	private static final ExecutorService threadPool = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));
	
	public static final Future<CommandArg<Integer>> submit(final String toParse)
	{
		return threadPool.submit(new Callable<CommandArg<Integer>>()
		{
			@Override
			public CommandArg<Integer> call() throws Exception
			{
				return Parser.parseCommand(toParse);
			}
		});
	}
	
	public static final Future<CommandArg<List<String>>> submitTarget(final String targetString)
	{
		return threadPool.submit(new Callable<CommandArg<List<String>>>()
		{
			@Override
			public CommandArg<List<String>> call() throws Exception
			{
				return Parser.parseStatsTarget(targetString);
			}
		});
	}
	
	public static final void submit(final Runnable runnable)
	{
		threadPool.submit(runnable);
	}
}
