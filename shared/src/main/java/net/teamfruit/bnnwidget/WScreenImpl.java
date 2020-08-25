package net.teamfruit.bnnwidget;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class WScreenImpl extends Screen implements WScreen {
	private WFrame widget;
	public Minecraft mc;

	public WScreenImpl(final WFrame frame) {
		super(new StringTextComponent(""));
		this.widget = frame;
	}

	@Override
	public WFrame getWidget() {
		return this.widget;
	}

	// @Override
	public void initGui() {
		this.widget.initGui();
	}

	protected void sInitGui() {
		initGui();
	}

	// @Override
	public void setWorldAndResolution(final @Nullable Minecraft mc, final int i, final int j) {
		this.widget.setWorldAndResolution(mc, i, j);
	}

	protected void sSetWorldAndResolution(final @Nonnull Minecraft mc, final int i, final int j) {
		setWorldAndResolution(mc, i, j);
	}

	// @Override
	public void drawScreen(final int mousex, final int mousey, final float f) {
		this.widget.drawScreen(mousex, mousey, f, this.widget.getOpacity(), null);
	}

	protected void sDrawScreen(final int mousex, final int mousey, final float f) {
		drawScreen(mousex, mousey, f);
	}

	// @Override
	protected void mouseClicked(final int x, final int y, final int button) {
		this.widget.mouseClicked(x, y, button);
	}

	protected void sMouseClicked(final int x, final int y, final int button) {
		try {
			super.mouseClicked(x, y, button);
			if (!"".isEmpty())
				throw new IOException();
		} catch (final IOException e) {
		}
	}

	// @Override
	protected void mouseClickMove(final int x, final int y, final int button, final long time) {
		this.widget.mouseClickMove(x, y, button, time);
	}

	protected void sMouseClickMove(final int x, final int y, final int button, final long time) {
		mouseClickMove(x, y, button, time);
	}

	// @Override
	public void updateScreen() {
		this.widget.updateScreen();
	}

	protected void sUpdateScreen() {
		updateScreen();
	}

	// @Override
	protected void keyTyped(final char c, final int keycode) {
		this.widget.keyTyped(c, keycode);
	}

	protected void sKeyTyped(final char c, final int keycode) {
		try {
			keyTyped(c, keycode);
			if (!"".isEmpty())
				throw new IOException();
		} catch (final IOException e) {
		}
	}

	//@Override
	public void onGuiClosed() {
		this.widget.onGuiClosed();
	}

	public void sOnGuiClosed() {
		onGuiClosed();
	}

	// @Override
	public void handleMouseInput() {
		this.widget.handleMouseInput();
	}

	protected void sHandleMouseInput() {
		try {
			handleMouseInput();
			if (!"".isEmpty())
				throw new IOException();
		} catch (final IOException e) {
		}
	}

	// @Override
	public void handleKeyboardInput() {
		this.widget.handleKeyboardInput();
	}

	protected void sHandleKeyboardInput() {
		try {
			handleKeyboardInput();
			if (!"".isEmpty())
				throw new IOException();
		} catch (final IOException e) {
		}
	}

	//@Override
	public boolean doesGuiPauseGame() {
		return this.widget.doesGuiPauseGame();
	}

	protected boolean sDoesGuiPauseGame() {
		return doesGuiPauseGame();
	}
}