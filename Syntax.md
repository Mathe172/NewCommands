##Commands
A single command looks like this:
```
[<selector1> <selector2> ...] <command> [<argument1> ...]
```
The primary purpose of selectors preceding the command is to ensure the result is captured before anything is executed - consequently, they should be labeled for later access (further explanation below)

###Command chaining
Commands of the form above can be chained and grouped, allowing for multiple commands inside one command block:
```
<command 1> [,|;] <command2> ...
//or even
(<command1>; <command2>), ((<command3>, <command4>); ...)
```
The difference between `,` and `;` is quite simple: While the `,` prevents further execution (of the current scope) when encountering an error, the `;` ignores any errors of the (one) preceeding command

Parentheses are used to group commands: The whole construct can be used anywhere a single command could be. For chaining purposes, the whole group counts as single command (especially for the `,` and `;`)

**Note:** Most commands raise an error if nothing happened (e.g. no block/entity was changed, ...)

###List of commands
The following commands are completely unchanged (compared to current implementation)
* `/blockdata`
* `/deop`
* `/entitydata`
* `/gamemode`
* `/gamerule`
* `/kill`
* `/op`
* `/particle`
* `/say` (**Note**: Since everything after this command is interpreted as string, this command can't be chained with others)
* `/scoreboard`
* `/setblock`
* `/stats`
* `/stop`
* `/tp` (**Note**: `/tp <x> <y> <z>` does not work if `<x>` can be interpreted as entity name (i.e. is a simple integer constant; there is no way to distinguish it from `/tp <target-entity> <rx> <ry>`) use `/tp @s <x> <y> <z>` instead

These commands have changed: (Only changes are listed)
* `/clone`: optional `fast` flag (first parameter). If this flag is set, most safety mechanisms are deactivated, e.g. redstone may drop,... and block updates can be omitted sometimes. Should only be used for 'stable' structures made out of simple blocks. (As a benefit, it is faster)
* `/execute`: Position can be omitted (`/execute @e say ...` is allowed), it defaults to `~ ~ ~`. **Note**: If the preamble (selectors before command) is non-empty, parentheses should be used around the command (otherwise, the selectors might be interpreted as postions). Also, the `detect` part optionally accepts NBT-data to filter the block (while metadata can be omitted even with NBT-data, the should be specified when the NBT-data come from a selector)
* `/fill`: Same `fast`-flag as for `/clone`
* `/summon`: Optional `label <label-name>` parameter (first parameter): If specified, the resulting entity is available in the label `<label-name>` (see [labels-section](#labels) for more information)

The commands listed below are completely new:
* `/activate`:<br>
    **Syntax**: `/activate [<delay>] [<pos1>] [<pos2>]`<br>
    'Activates' all blocks specified (the positon of the sender, the block at `<pos1>` or the box specifed by `<pos1>` and `<pos2>`) after a delay of `<delay>` (defaulted to `1`) ticks (minimum is one). This 'activation' triggers command-blocks, grows crops, ...
* `/break`:<br>
    No arguments. Raises an error to leave `for`-loops and similar constructs
* `/explain`:<br>
	**Syntax**: `/explain [all|extended] [<position>]`<br>
	Gives further information about the errors that occured during parsing a command. If no position is specified, the last command of the player is analyzed (does **not** work on command-blocks or entities), otherwise the command stored in the command-block at position `<position>` is used.
	Without the `all`/`extended`-flag, only the error causing the command to fail is printed (with all its causes)
	With the flag specified, every error that occured during parsing is printed (even the ones that did not cause the command to fail), e.g. error caused by different possible interpretation of arguments. On hover, the output shows where the error occured.
* `/for`:<br>
    **Syntax**: `/for [safe] <label-name> <start> <end> [step <step>] <command>`<br>
    Executes the command once for each value of `<label-name>` specified by `<start>`,`<end>` (and optionally `<step>`). Before each execution, the value of the label `<label-name>` is set appropriately (For more information on labels, see the [labels-section](#labels)). If the `safe`-flag is set, execution is cancelled when an error occurs, otherwise ignored (except for the '`/break`-error', this always cancels the loop)
* `/if`:<br>
	**Syntax**: `/if <condition> <then-command> [else <else-command>]`<br>
	Self-explanatory. **Note**: `<then-command>` must be enclosed in parentheses if the else clause is present
* `/try`:<br>
	**Syntax**: `/try <command> <command2>`<br>
	Executes `<command2>` if and only if `<command>` caused an error. **Note**: `<command>` must be enclosed in parentheses
    
##Selectors
While selectors are currently only used for selecting entities with specific properties, they can now be used to do a lot more: It's basically a way to input something in a different way than the default way: Instead of writing down the name of an entity/player, you can specify them using a selector. Now, you can also use things like the result of a calculation, the score of any player, ... (for a complete list, see below)
###Basic syntax
The syntax for all selectors is basically the same as it is now: `@<selector-name>`, optionally followed by a list of parameters to further specify what you want. If a parameter is specified more than once, only the last occurence counts. For some selectors, the parameter names for some of the parameters can be left away:
```
@e[<x>,<y>,<z>,<r>]
```
instead of
```
@e[x=<x>,y=<y>,z=<z>,r=<r>]
```

For all selectors, there is a parameter that can always be specified: `label=<label-name>`. If specified, the result of the selector is stored in the label with name `label-name`. For more information on labels, see the [labels-section](#labels).
While selectors can be used almost anywhere, there is one important exception: For most commands, there are keywords that further specify what the command should do (the `scoreboard` command for example has the keywords `players,objectives,...`). For technical reasons, these keywords can't be replaced by selectors.

###List of selectors
A complete list of selectors can be found in the table below. Parameter names in square bracked can be omitted and parameters in square brackets are optional. (note that the order of the unnamed parameters is important and can't be changed)

<table>
<thead>
<tr>
    <td align="center">Name</td>
    <td width="180em">Parameter</td>
    <td>Parameter-description</td>
    <td>Selector-description</td>
</tr>
</thead>
<tr>
    <td><code>@s</code></td>
    <td>-</td>
    <td>-</td>
    <td>A reference to whatever is executing the command</td>
</tr>
<tr>
    <td rowspan="5"><code>@p</code>
    <code>@a</code>
    <code>@r</code>
    <code>@e</code></td>
    <td>all parameters from vanilla</td>
    <td>see the wiki</td>
    <td rowspan="5">Selects entities that have specific properties (as it is already the c[ase in vanilla)</td>
</tr>
<tr>
    <td><code>[xyz=&lt;postion&gt;]</code></td>
    <td>Shorthand for <code>x</code>,<code>y</code>,<code>z</code>. If present, the others will be ignored
</tr>
<tr>
    <td><code>[dxyz=&lt;size&gt;]</code></td>
    <td>Shorthand for <code>dx</code>,<code>dy</code>,<code>dz</code>. If present, the others will be ignored. Only labels and selectors allowed (not things like <code>dxyz=2 2 2</code>)
</tr>
<tr>
    <td><code>[nbt=&lt;tag&gt;]</code></td>
    <td>Filters the entities using NBT-tags (like already present in <code>scoreboard</code> and some others
</tr>
<tr>
    <td><code>[team=[!]&lt;team(s)&gt;]</code><br/>
    <code>[name=[!]&lt;name(s)&gt;]</code><br/>
    <code>[type=[!]&lt;type(s)&gt;]</code></td>
    <td>Unlike in vanilla, these parameters accept lists (<code>(name1,name2,...)</code>) instead of single names ('either..., or ...'). If prefixed by a <code>!</code>, the list is inverted (as in 'neither ..., nor ...')</td>
</tr>
<tr>
    <td><code>@o</code></td>
    <td><code>&lt;objective-name&gt;</code></td>
    <td>The name of the objective to capture</td>
    <td>This is just for performance-reasons: Instead of searching the objective by name again and again, this allows you to label the result and use it elsewhere</td>
</tr>
<tr>
    <td rowspan="2"><code>@sc</code></td>
    <td><code>[o=]&lt;obj.-name&gt;</code>
    <td>The objective of interest (by default the name)</td>
    <td rowspan="2">Used to query the score of an entity/variable from a scoreboard objective</td>
</tr>
<tr>
    <td><code>[[t=]&lt;entity/var.&gt;]</code></td>
    <td>The entity/variable to read from. If omitted, the executor of the command is taken</td>
</tr>
<tr>
    <td rowspan="2"><code>@n</code></td>
    <td><code>&lt;coords|entity|tag|string&gt;</code></td>
    <td>Any source of nbt-data: Coordinates:the block is taken, entity: clear, tag: clear, string: the string is interpreted at runtime (can be used to store nbt inside a name/book/...)</td>
    <td rowspan="2">Reads nbt-data from the given source. Can also be used to read out parts of the tag</td>
</tr>
<tr>
    <td><code>[&lt;path&gt;]</code></td>
    <td>The path inside the tag to read from (separated by <code>.</code>). For compounds, this is the key name. For lists, this is the index</td>
</tr>
<tr>
    <td><code>@t</code></td>
    <td><code>[cmd=]&lt;command&gt;</code></td>
    <td>The command to be benchmarked</td>
    <td>Measures the execution time of a given command and returns the result in microseconds</td>
</tr>
<tr>
    <td><code>@b</code></td>
    <td><code>&lt;coordinates&gt;</code></td>
    <td>The position of the block</td>
    <td>Used to query the block-state at a given position (the state contains type and metadata, but no tile-entities)</td>
</tr>
<tr>
    <td><code>@c</code></td>
    <td><code>&lt;operator&gt; ...</code></td>
    <td>see <a href="#operators">Operators-section</a></td>
    <td>Used to perform calculations. For a complete list of available operators, see <a href="#operators">below</a></td>
</tr>
</table>

##Operators
Due to technical limitations, the operators use prefix notation (this means that the operator is before its operands: `+ 1 2` instead of `1 + 2`). 

**Important note**: Since operator names can contain anything (except a whitespace), there has to be a whitespace after every operator. This is especially true for the constants defined below (even if they are the last thing in the selector)

There is a wide range of operators available:

|Operator|Description|
|--------|-----------|
|`+`,`-`,`*`,`/`| The standard binary addition/... operators|
|`-0`| The unary sign inversion operator (shorthand for `- 0 ...`)
|`%`/`mod`| The modulo operator|
|`<`,`<=`,`>`,`>=`,`!=`,`==`| Comparision operators |
|`!`/`not`,`&`/`and`,`&&`,`|`/`or`,`||`| Logical operators. The `&&`  and `||` operator are the short-circuit versions of `&` and `|`: They do **not** evaluate the second argument if unnecessary|
|`sq`,`sqrt`| The square and squareroot operator|
|`sin`,`cos`,`exp`,`ln`,`^`| The standard analytic functions. `^` is the power operator: `^ 2 3` `=8`|
|`pi,e_`| The mathematical constants|
|`+v`,`-v`| Vector addition and subtraction|
|`*v`,`/v`| Scalar multiplication and division. The scalar is the first argument|
|`.`| The vector dot-product|
|`v0`| Normalizes the vector|
|`abs`| Length of the vector|
|`cv`| `x` and `z` are centered to the block-center, `y` is floored (`1.35 0.7 2.5`->`1.5 0 2.5`)|
|`xv`,`yv`,`zv`| Reads the specified component from a vector|
|`ex`,`ey`,`ez`| The vectors of unitiy|
|`rxv`,`ryv`| The angles to describe the direction of the vector. The convention is the same as for the entity-selector (see the `rx`,`ry` parameters|
|`pos`,`x`,`y`,`z`| The position / components of the position of an entity|
|`rx`,`ry`| The angles describing the facing direction of an entity (like above, see entity-selector)|
|`fv`| The normalized vector pointing in the direction the entity is facing|
|`rd`| The entity ridden by another entity|
|`slot`| The selected slot of a player (from `0` to `8`)|
|`items`| First operand: NBT-List contaning items. Second operand: An integer-list. This operator returns an NBT-List containing the items specified by the index-list in the correct order (The `slot:`-tag is used to find the items). Can be used to 'convert' the player equipment to entity-equipment|
|`isAir`| Returns if the given block-state (**not** position) is air|
|`meta`| Returns the metadata of a given block-state (**not** positon)|
|`i`,`s`,`e`,`v`| Converts the argument into integer/string/entity/vector|
