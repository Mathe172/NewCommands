package net.minecraft.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.command.completion.TabCompleter;
import net.minecraft.command.completion.TabCompletionData;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

public class GuiChat extends GuiScreen
{
	private static final Logger logger = LogManager.getLogger();
	private String historyBuffer = "";
	
	/**
	 * keeps position of which chat message you will select when you press up, (does not increase for duplicated messages sent immediately after each other)
	 */
	private int sentHistoryCursor = -1;
	private boolean waitingOnAutocomplete;
	// private Iterator<TabCompleter> tcIterator;
	private int tcIndex;
	private String cachedInput = null;
	private int cachedCursorIndex = 0;
	// private Set<TabCompleter> cachedTCs = null;
	private List<TabCompleter> cachedTCs = null;
	private ChatComponentText nameList = null;
	
	/** Chat entry field */
	protected GuiTextField inputField;
	
	/**
	 * is the text that appears when you press the chat key and the input box appears pre-filled
	 */
	private String defaultInputFieldText = "";
	private static final String __OBFID = "CL_00000682";
	
	public GuiChat()
	{
	}
	
	public GuiChat(final String p_i1024_1_)
	{
		this.defaultInputFieldText = p_i1024_1_;
	}
	
	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui()
	{
		Keyboard.enableRepeatEvents(true);
		this.sentHistoryCursor = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
		this.inputField = new GuiTextField(0, this.fontRendererObj, 4, this.height - 12, this.width - 4, 12);
		this.inputField.setMaxStringLength(100);
		this.inputField.setEnableBackgroundDrawing(false);
		this.inputField.setFocused(true);
		this.inputField.setText(this.defaultInputFieldText);
		this.inputField.setCanLoseFocus(false);
	}
	
	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
		this.mc.ingameGUI.getChatGUI().resetScroll();
	}
	
	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen()
	{
		this.inputField.updateCursorCounter();
	}
	
	/**
	 * Fired when a key is typed (except F11 who toggle full screen). This is the equivalent of KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	@Override
	protected void keyTyped(final char typedChar, final int keyCode) throws IOException
	{
		this.waitingOnAutocomplete = false;
		
		if ((keyCode != Keyboard.KEY_TAB || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) && keyCode != Keyboard.KEY_LSHIFT)
			this.cachedTCs = null;
		// this.tcIterator = null;
		
		if (keyCode == Keyboard.KEY_TAB)
			this.autoComplete(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT));
		
		if (keyCode == 1)
			this.mc.displayGuiScreen((GuiScreen) null);
		else if (keyCode != 28 && keyCode != 156)
		{
			if (keyCode == 200)
				this.getSentHistory(-1);
			else if (keyCode == 208)
				this.getSentHistory(1);
			else if (keyCode == 201)
				this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().getLineCount() - 1);
			else if (keyCode == 209)
				this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().getLineCount() + 1);
			else
				this.inputField.textboxKeyTyped(typedChar, keyCode);
		}
		else
		{
			final String var3 = this.inputField.getText().trim();
			
			if (var3.length() > 0)
				this.func_175275_f(var3);
			
			this.mc.displayGuiScreen((GuiScreen) null);
		}
	}
	
	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		int var1 = Mouse.getEventDWheel();
		
		if (var1 != 0)
		{
			if (var1 > 1)
				var1 = 1;
			
			if (var1 < -1)
				var1 = -1;
			
			if (!isShiftKeyDown())
				var1 *= 7;
			
			this.mc.ingameGUI.getChatGUI().scroll(var1);
		}
	}
	
	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		if (mouseButton == 0)
		{
			final IChatComponent var4 = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
			
			if (this.func_175276_a(var4))
				return;
		}
		
		this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void func_175274_a(final String p_175274_1_, final boolean p_175274_2_)
	{
		if (p_175274_2_)
			this.inputField.setText(p_175274_1_);
		else
			this.inputField.writeText(p_175274_1_);
	}
	
	public void autoComplete(final boolean reverse)
	{
		if (this.cachedTCs == null)
		{
			this.sendAutocompleteRequest();
			return;
		}
		
		if (this.cachedTCs.isEmpty())
			return;
		
		final int tcSize = this.cachedTCs.size();
		
		if (tcSize > 1)
			this.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(this.nameList, 1);
		
		if (reverse)
		{
			if (--this.tcIndex == -1)
				this.tcIndex = tcSize;
		}
		else if (++this.tcIndex > tcSize)
			this.tcIndex = 0;
		
		if (this.tcIndex < tcSize)
		{
			final TabCompleter completer = this.cachedTCs.get(this.tcIndex); // this.tcIterator.next();
			
			this.inputField.setText(completer.matchInto(this.cachedInput));
			this.inputField.setCursorPosition(completer.newCursorIndex());
			
			return;
		}
		
		// this.tcIterator = this.cachedTCs.iterator();
		// this.tcIndex = this.cachedTCs.size();
		
		this.inputField.setText(this.cachedInput);
		this.inputField.setCursorPosition(this.cachedCursorIndex);
	}
	
	private void sendAutocompleteRequest()
	{
		BlockPos pos = null;
		
		if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			pos = this.mc.objectMouseOver.func_178782_a();
		
		this.mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(this.inputField.getText(), this.inputField.getCursorPosition(), pos));
		
		this.waitingOnAutocomplete = true;
		this.cachedInput = this.inputField.getText();
		this.cachedCursorIndex = this.inputField.getCursorPosition();
	}
	
	/**
	 * input is relative and is applied directly to the sentHistoryCursor so -1 is the previous message, 1 is the next message from the current cursor position
	 */
	public void getSentHistory(final int p_146402_1_)
	{
		int var2 = this.sentHistoryCursor + p_146402_1_;
		final int var3 = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
		var2 = MathHelper.clamp_int(var2, 0, var3);
		
		if (var2 != this.sentHistoryCursor)
			if (var2 == var3)
			{
				this.sentHistoryCursor = var3;
				this.inputField.setText(this.historyBuffer);
			}
			else
			{
				if (this.sentHistoryCursor == var3)
					this.historyBuffer = this.inputField.getText();
				
				this.inputField.setText((String) this.mc.ingameGUI.getChatGUI().getSentMessages().get(var2));
				this.sentHistoryCursor = var2;
			}
	}
	
	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
	 */
	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks)
	{
		drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
		this.inputField.drawTextBox();
		final IChatComponent var4 = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
		
		if (var4 != null && var4.getChatStyle().getChatHoverEvent() != null)
			this.func_175272_a(var4, mouseX, mouseY);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	public void onAutocompleteResponse(final List<TabCompletionData> tcDataList)
	{
		if (this.waitingOnAutocomplete && tcDataList.size() > 0)
		{
			this.cachedTCs = new ArrayList<>(tcDataList.size());
			
			final StringBuilder sb = new StringBuilder();
			
			for (final TabCompletionData tcData : tcDataList)
				this.cachedTCs.add(new TabCompleter(tcData));
			
			for (final TabCompleter tcData : this.cachedTCs)
			{
				if (sb.length() > 0)
					sb.append(", ");
				
				sb.append(tcData.name());
			}
			
			this.tcIndex = this.cachedTCs.size();
			this.nameList = new ChatComponentText(sb.toString());
			
			this.autoComplete(false);
		}
	}
	
	/**
	 * Returns true if this GUI should pause the game when it is displayed in single-player
	 */
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
}