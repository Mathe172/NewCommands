# NewCommands
##Description
The primary intention of this Minecraft modification was to improve the performance and flexibility of the command system. After a few months of work, this is the first working version. Although a lot of the commands are still missing, nearly all of the core functionality is here, of which we will list the most important ones (for examples see below):
* FULL backwards-compatibility (well almost... some things are interpreted that wouldn't have been before) - the syntax was just extended, not altered
* Commands in command blocks are asynchronously precompiled to greatly improve the performance of high frequency command block contraptions
* Everything can be used everywhere and more than once: Even inside selctors it is possible to write commands and the result can also be labeled for multiple usages later in the command
* The parsing is handled by a central unit to which arbitrary ways of parsing the command can be added - from simple Integers to fully customized parsers
* Commands and selectors can easily be added using an intuitive chaining syntax, using predefined building blocks or completely new ones (as mentioned in the point above). It is also possible to register completely custom ones that would be too complex for this syntax, as it is the case with the Entity selector (since it requires dynamic parameter names for the `score_<name>` parameters)
* Tab completion works everywhere and automatically: Everything the engine understands can be completed, no matter complex the command is, and all this with minimal to no effort. The completion engine even understands NBT-Tags and helps completing them

##MCP and Minecraft version
NewCommands is built on Minecraft 1.8 using MCP 9.10.
To compile, delete the unused files from `net/minecraft/command`, a list of the ones to keep can be found [here](https://github.com/Mathe172/NewCommands/blob/master/Classes%20to%20keep.txt)

**Note**: As of now, MCP contains two bugs preventing direct use of the reobfuscated code (described [here](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-tools/1260561-toolkit-mod-coder-pack-mcp?comment=3271) and [here](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-tools/1260561-toolkit-mod-coder-pack-mcp?comment=3272)) - direct execution using the `startclient.bat` files is still possible
To fix them the following steps are required:
* Delete the files `pd.class`, `pe.class` (only server), `pf.class` and `pg.class` form the `reobf`-folder
* Copy `net/minecraft/server/MinecraftServer$4.class` from `temp/client_reobf.jar` to `reobf/minecraft/net/minecraft/server/` 

##Introduction to the syntax
###Commands
A single command looks like this:
```
[<selector1> <selector2> ...] <command> [<argument1> ...]
```
The primary purpose of selectors preceding the command is to ensure the result is captured before anything is executed - consequently, they should be labeled for later access (further explanation below)

##Command chaining
Commands of the form above can be chained and grouped, allowing for multiple commands inside one command block:
```
<command 1> [,|;] <command2> ...
//or even
(<command1>; <command2>), ((<command3>, <command4>); ...)
```
The difference between `,` and `;` is quite simple: While the `,` just stops execution (of the current scope), the `;` ignores any errors of the (one) preceeding command
Parentheses are used to group commands: The whole construct can be used anywhere a single command could be. For chaining purposes, the whole group counts as single command (especially for the `,` and `;`)

###Selectors
Selectors stayed mainly the same, with one key difference: They can be labeled:
```
@<selector-name>[[<param-name1>=]<value1>,...,label=<label-name>,...]
//Referencing a label:
$<label-name>
```
Conversions from one data type to another are performed automatically if necessary (and possible) - this means a selector/label returning a list of entities can be also be used as a text component or anything else that is compatible (multiple different uses of one label are also possible)

We plan to greatly expand the number of available selectors: While the existing ones are of course planned (not yet implemented), there will be several new ones:
* `@s`: Self-selector - returns currently executing entity/command block (already implemented)
* `@t`: Timing selector - while more a proof of concept, this selector takes a complete command as argument and returns the execution (without compilation) time in microseconds
* `@c`: Calculation selector: Allows the user to perform arbitrary calculations (not yet implemented)
* `@...`: Selectors to access scores and even NBT data of any entity/block (effectively allowing things like copying entities or even blocks into falling-sand entities)

##Examples
```
for x 1 5 summon Blaze ~ ~$x ~ {CustomName:"Blaze #\$x"}
```
Summons 4 (end index is exclusive) Blazes on top of each other, named 'Blaze #1', ... (note that inside NBT-Tags, selectors and labels have to be escaped using `\` to improve backwards-compatibility)
```
@t[cmd=(for x -10 10 for y -10 10 summon PrimedTnt ~$x ~50 ~$y),label=timeTaken] say The execution took $timeTaken microseconds
```
Summons an array of 400 TNT 50 blocks above and outputs the time taken to do so. Notes: The `cmd=` can also be omitted; The parentheses are necessary since the for command would otherwise try to interpret `label=timeTaken` as chained command (the selector only expects a single command by default, more have to be grouped using parentheses). This behaviour might change for the `for`-command

##Code model
There are only a few basic structures:
* CommandArg: The basic element that represents the result of one unit, most importantly they are passed to commands/selctors instead of plain Strings (it gets the `ICommandSender`for evaluation`)
* TypeID: Represents a type like Integer, String or EntityList, knows and handles the conversions of CommandArgs to CommandArgs returning data of other types (again described by a TypeID)
* ArgWrapper: Combines the CommandArg and TypeID into one unit and allows for type-safe conversion
* IDataType, IParse, IComplete, ...: Interfaces and classes that describe the capabilities of a parsing unit and handle necessary wrappers for completion
* CommandDescriptor: As the name suggests, this describes a command. It knows a list of IDataTypes (in some way, a IDataType is a TypeID with an associated parser and completion) as arguments and a list of subcommands (again CommandDescriptors) identified by keywords. Also provides a construct-method that is called by the central parser once all the necessary data are acquired and provides usage description and an IPermisson instance to handle execution permissons
* SelectorDescriptor: Similary to the CommandDescriptor knows a list of parameters (optionally identified by a name) that it can handle (as in the current syntax, unnamed parameters can be written before other parameters as shorthand notation) with the associated IDataTypes.

#Registration examples
The following example is the complete code required to register the `summon`-command (the necessary IDataTypes are part of the default set)
```java
CommandDescriptor.registerCommand(
	new CommandConstructorU(IPermission.PermissionLevel2, "commands.summon.usage", "summon")
		.then(TypeEntityId.type)
		.optional(ParserCoordinates.centered)
		.optional(NBTArg.nbtArg)
		.executes(CommandSummon.constructable));
		
//----CommandSummon.java----
public static final CommandConstructable constructable = new CommandConstructable()
{
	@Override
	public CommandBase construct(final List<ArgWrapper<?>> params, final IPermission permission)
	{
		return new CommandSummon(params.get(0).get(TypeIDs.String), CommandDescriptor.getParam(TypeIDs.Coordinates, 1, params), CommandDescriptor.getParam(TypeIDs.NBTCompound, 2, params), permission);
	}
};
	
public CommandSummon(final CommandArg<String> name, final CommandArg<Vec3> coords, final CommandArg<NBTTagCompound> nbt, final IPermission permission)
{
  super(permission);
  this.coords = coords;
  this.name = name;
  this.tag = nbt;
}
```

This one registers the timing-selector:
```java
SelectorDescriptor.registerSelector("t",
	new SelectorConstructor(TypeIDs.Integer)
	  .then("cmd", TypeCommand.parserSingleCmd)
		.construct(SelectorTiming.constructable));
		
//----SelectorTiming.java----
public static final SelectorConstructable constructable = new SelectorConstructable()
{
	@Override
	public ArgWrapper<Integer> construct(final List<ArgWrapper<?>> unnamedParams, final Map<String, ArgWrapper<?>> namedParams) throws SyntaxErrorException
	{
		return new ArgWrapper<>(TypeIDs.Integer, new SelectorTiming(SelectorDescriptor.getRequiredParam(TypeIDs.Integer, 0, "cmd", unnamedParams, namedParams)));
	}
};

public SelectorTiming(final CommandArg<Integer> command)
{
	this.command = command;
}
```
