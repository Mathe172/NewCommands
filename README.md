# NewCommands
##Description
The primary intention of this Minecraft modification was to improve the performance and flexibility of the command system. After a few months of work, this is the first working version. Although a lot of the commands are still missing, nearly all of the core functionality is here, of which we will list the most important ones (for examples see below):

###For end users
* FULL backwards-compatibility (well almost... some things are interpreted that wouldn't have been before) - the syntax was just extended, not altered
* Commands in command blocks are asynchronously precompiled to greatly improve the performance of high frequency command block contraptions
* Everything can be used everywhere and more than once: Even inside selctors it is possible to write commands and the result can also be labeled for multiple usages later in the command
* Tab completion works everywhere and automatically: Everything the engine understands can be completed (and additionaly abbreviations (see below for examples)), no matter how complex the command is, and all this with minimal to no effort. The completion engine even understands NBT-Tags and helps completing them.

###For developers
* The parsing is handled by a central unit to which arbitrary ways of parsing the command can be added - from simple Integers to fully customized parsers
* Commands and selectors can easily be added using an intuitive chaining syntax, using predefined building blocks or completely new ones (as mentioned in the point above). It is also possible to register completely custom ones that would be too complex for this syntax, as it is the case with the Entity selector (since it requires dynamic parameter names for the `score_<name>` parameters)
* Tab completion is also processed asynchronously (except some thread-critical sections acquiring data from the main thread)
* Extremely modular: There is no difference between commands and other types (except that one is called by default) meaning they can be used everywhere interchangeably. It would for example be possible to parse the complete command (except the name) using a custom parser
* Everything is loaded at runtime: Command-, selector-, type- (...) registration is always possible, even at runtime

##MCP and Minecraft version
NewCommands is built on Minecraft 1.8 using MCP 9.10.

To try it out, just copy the files from the `bin`-folder into the jar and launch

To compile, delete the unused files from `net/minecraft/command`, a list of the ones to keep can be found [here](https://github.com/Mathe172/NewCommands/blob/master/Classes%20to%20keep.txt). AFTERWARDS copy both `Common/src/` and `Client/src/`/`Server/src/` into the source. 

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
The difference between `,` and `;` is quite simple: While the `,` prevents further execution (of the current scope) when encountering an error, the `;` ignores any errors of the (one) preceeding command

Parentheses are used to group commands: The whole construct can be used anywhere a single command could be. For chaining purposes, the whole group counts as single command (especially for the `,` and `;`)

###Selectors
Selectors stayed mainly the same, with one key difference: They can be labeled:
```
@<selector-name>[[<param-name1>=]<value1>,...,label=<label-name>,...]
//Referencing a label:
$<label-name>
```
Conversions from one data type to another are performed automatically if necessary (and possible) - this means a selector/label returning a list of entities can be also be used as a text component or anything else that is compatible (multiple different uses of one label are also possible)

We plan to greatly expand the number of available selectors: There a of course the current selectors, but also a wide range of new ones:
* `@s`: Self-selector - returns currently executing entity/command block
* `@p,@a,@r,@e`: The current entity selectors. Nearly the same as before, except: `xyz=` as shorthand for all coordinates, useful when using the result of other selectors, `nbt=`: self explaining. Also, `team,name,type` accept lists when written in parentheses (`type=!(Player,Snowball)` for example)
* `@o`: Captures a score objective (when using the same objectvie multiple times, this is much more efficient)
* `@sc`: Returns the score of a entity in the specified scoreboard (example: `@sc[score_name,entity_name]`)
* `@n`: To read NBT-data. Takes an entity, NBT-object (from selectors), coordinates (for blocks) or a string and can read a specific element from the resulting NBT (numbers can be used for lists) (example: `@n[@e[type=Snowball,c=1],Motion.0]`: returns the velocity in the x-direction of the nearest snowball)
* `@t`: Timing selector - while more a proof of concept, this selector takes a complete command as argument and returns the execution- (without compilation-) time in microseconds
* `@c`: Calculation selector: Allows the user to perform arbitrary calculations. Because of technical limitations, the selector uses prefix notation (`+ 1 2` instead of `1 + 2`) A list of operators can be found below

###Operators (for calculation selector)
* `+,-,*`: the standard operators (note that for `-1`, `- 0 1` has to be written (or `-0 1` as shorthand))
* `sq,sqrt`: square and squareroot operator
* `x,y,z,rx,ry`: returns a specific coordinate (or pitch/yaw) of an entity
* `pos`: returns the position of an entity (perfect for use with the entity selector, for example `@e[xyz=@c[pos @e[name=some_entity],c=1]`
* `i,s,e`: converters for integer, string and entity (the `@s` selector does not return an entity. If the entity is used multiple times, this can be used to capture the converted version)
* `sin,cos`: sine and cosine functions

##Examples
```
for x 1 5 summon Blaze ~ ~$x ~ {CustomName:"Blaze #\$x"}
```
Summons 5 Blazes on top of each other, named 'Blaze #1', ... (note that inside NBT-Tags, selectors and labels have to be escaped using `\` to improve backwards-compatibility)

```
activate, @p[label=p] @c[cos ry $p,label=fy] @c[-0 * $fy sin rx $p,label=dx] @c[* $fy cos rx $p,label=dz] @c[-0 sin ry $p,label=dy] execute $p (execute @e[type=Snowball,c=1,r=5] kill @s, for safe n 5 500 summon PrimedTnt ~@c[* $n $dx] ~@c[* $n $dy] ~@c[* $n $dz])
```
This command summons a ray of TNT in the direction the player is looking at when they throw a snowball. Without going in full detail, these are the main steps performed by the command:
* `activate` triggers the command-block in the next tick (this replaces the setblock-clock)
* calculate some things used to summon the ray in the correct direction
* `execute` on the player and the on the nearest snowball
* when a snowball is found, `kill` it and `summon` the ray using a `for`-loop. The `safe`-flag tells the `for`-command to stop when the first `summon` fails (we reached the top/bottom of the world

Note: To start the command, power the command-block once

###Tab completion
As mentioned, tab completion understands abbreviations: (`|` is the cursor)
```
/scr| -> /scoreboard|
/scr|brd -> /scoreboard|
/say @t| -> /say @t[|]
/summon PrimedTnt ~ ~ ~ {Custom|} -> /summon PrimentTnt ~ ~ ~ {CustomName:|} AND ... {CustomNameVisible:|}
```

As can be seen, even the cursor position is set appropriately after choosing a completion (also, neccessary characters like `:` are added). To iterate through the possible completions, `TAB` iterates forward, `SHIFT+TAB` backwards and holding down `CTRL` always starts a new completion (and finishes the current one by choosing the current item). Note that the first character (or nothing at all) has to be present for a completion to be proposed. So `/crbrd|` will not be completed (but `/|crbrd` will be, since nothing at all is before the cursor)

##Code model
There are only a few basic structures:
* `CommandArg`: The basic element that represents the result of one unit, most importantly they are passed to commands/selctors instead of plain Strings (it gets the `ICommandSender`for evaluation)
* `TypeID`: Represents a type like Integer, String or EntityList, knows and handles the conversions of `CommandArg's` to `CommandArg's` returning data of other types (again described by a `TypeID`)
* `ArgWrapper`: Combines the `CommandArg` and `TypeID` into one unit and allows for type-safe conversion
* `IDataType`, `IParse`, `IComplete`, ...: Interfaces and classes that describe the capabilities of a parsing unit and handle necessary wrappers for completion
* `CommandDescriptor`: As the name suggests, this describes a command. It knows a list of `IDataType's` (in some way, a `IDataType` is a `TypeID` with an associated parser and completion) as arguments and a list of subcommands (again `CommandDescriptor's`) identified by keywords. Also provides a construct-method that is called by the central parser once all the necessary data are acquired and provides usage description and an `IPermisson` instance to handle execution permissons
* `SelectorDescriptor`: Similary to the `CommandDescriptor` knows a list of parameters (optionally identified by a name) that it can handle (as in the current syntax, unnamed parameters can be written before other parameters as shorthand notation) with the associated `IDataType's`.

#Registration examples
The following example is the complete code required to register the `summon`-command (the necessary `IDataType's` are part of the default set)
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

More detailed explanations on request, since it would be too much to decribe it all
