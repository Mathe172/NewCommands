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

For all selectors, there is a parameter that can always be specified: `label=<label-name>`. If specified, the result of the selector is stored in the label with name `label-name`. For more information on labels, see the Labels section.
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
    <td><code>[cmd=]&lt;command&gt;</code></td>
    <td>The command to be benchmarked</td>
    <td>Measures the execution time of a given command and returns the result in microseconds</td>
</tr>
<tr>
    <td><code>@c</code></td>
    <td><code>&lt;operator&gt; ...</code></td>
    <td>see Operators-section</td>
    <td>Used to perform calculations. For a complete list of available operators, see <a href="#Operators">below</a></td>
</tr>
</table>

##Operators
