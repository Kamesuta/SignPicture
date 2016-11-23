package com.kamesuta.mc.signpic.handler;

import java.lang.reflect.Field;
import java.util.List;

import com.kamesuta.mc.signpic.Client;
import com.kamesuta.mc.signpic.Config;
import com.kamesuta.mc.signpic.CoreEvent;
import com.kamesuta.mc.signpic.Reference;
import com.kamesuta.mc.signpic.entry.Entry;
import com.kamesuta.mc.signpic.entry.EntryId;
import com.kamesuta.mc.signpic.entry.EntryIdBuilder;
import com.kamesuta.mc.signpic.mode.CurrentMode;
import com.kamesuta.mc.signpic.preview.SignEntity;
import com.kamesuta.mc.signpic.util.ChatBuilder;
import com.kamesuta.mc.signpic.util.Sign;

import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class SignHandler {
	private static Field f;

	public static void init() {
		try {
			final Field[] fields = GuiEditSign.class.getDeclaredFields();
			for (final Field field : fields)
				if (TileEntitySign.class.equals(field.getType())) {
					Reference.logger.info("Hook the TileEntitySign field included by GuiEditSign");
					field.setAccessible(true);
					f = field;
				}
		} catch (final SecurityException e) {
			Reference.logger.error("Could not hook TileEntitySign field included by GuiEditSign", e);
		}
	}

	@CoreEvent
	public void onSign(final GuiOpenEvent event) {
		if (CurrentMode.instance.isMode(CurrentMode.Mode.PLACE))
			if (event.gui instanceof GuiEditSign)
				if (f!=null)
					try {
						final GuiEditSign ges = (GuiEditSign) event.gui;
						final TileEntitySign tileSign = (TileEntitySign) f.get(ges);
						Sign.placeSign(CurrentMode.instance.getEntryId(), tileSign);
						event.setCanceled(true);
						if (!CurrentMode.instance.isState(CurrentMode.State.CONTINUE)) {
							CurrentMode.instance.setMode();
							final SignEntity se = Sign.preview;
							if (se.isRenderable()) {
								final TileEntitySign preview = se.getTileEntity();
								if (preview.xCoord==tileSign.xCoord&&preview.yCoord==tileSign.yCoord&&preview.zCoord==tileSign.zCoord) {
									Sign.preview.setVisible(false);
									CurrentMode.instance.setState(CurrentMode.State.PREVIEW, false);
									CurrentMode.instance.setState(CurrentMode.State.SEE, false);
								}
							}
						}
					} catch (final Exception e) {
						Reference.logger.error(I18n.format("signpic.chat.error.place"), e);
						ChatBuilder.create("signpic.chat.error.place").setId().useTranslation().chatClient();
					}
				else
					ChatBuilder.create("signpic.chat.error.place").setId().useTranslation().chatClient();
	}

	@CoreEvent
	public void onClick(final MouseEvent event) {
		if (event.buttonstate&&Client.mc.gameSettings.keyBindUseItem.getKeyCode()==event.button-100)
			if (CurrentMode.instance.isMode(CurrentMode.Mode.SETPREVIEW)) {
				Sign.preview.capturePlace();
				event.setCanceled(true);
				CurrentMode.instance.setMode();
				Client.openEditor();
			} else if (CurrentMode.instance.isMode(CurrentMode.Mode.LOAD)) {
				final TileEntitySign tilesign = Client.getTileSignLooking();
				if (tilesign!=null) {
					final Entry entry = EntryId.fromTile(tilesign).entry();
					if (entry.isValid()) {
						final Entry old = CurrentMode.instance.getEntryId().entry();
						final EntryIdBuilder idb = new EntryIdBuilder();
						idb.setURI(CurrentMode.instance.isState(CurrentMode.State.LOAD_CONTENT) ? entry.contentId.getID() : old.contentId.getID());
						idb.setMeta(CurrentMode.instance.isState(CurrentMode.State.LOAD_META) ? entry.meta : old.meta);
						CurrentMode.instance.setEntryId(idb.build());
						event.setCanceled(true);
						Client.openEditor();
						if (!CurrentMode.instance.isState(CurrentMode.State.CONTINUE))
							CurrentMode.instance.setMode();
					}
				}
			}
	}

	@CoreEvent
	public void onTooltip(final ItemTooltipEvent event) {
		if (event.itemStack.getItem()==Items.sign&&(Config.instance.signTooltip||!Config.instance.guiExperienced)) {
			final KeyBinding binding = KeyHandler.Keys.KEY_BINDING_GUI.binding;
			final List<KeyBinding> conflict = KeyHandler.getKeyConflict(binding);
			String keyDisplay = GameSettings.getKeyDisplayString(binding.getKeyCode());
			if (!conflict.isEmpty())
				keyDisplay = EnumChatFormatting.RED+keyDisplay;
			event.toolTip.add(I18n.format("signpic.item.sign.desc", keyDisplay));
			if (!conflict.isEmpty()) {
				event.toolTip.add(I18n.format("signpic.item.sign.desc.keyconflict", I18n.format("menu.options"), I18n.format("options.controls")));
				for (final KeyBinding key : conflict)
					event.toolTip.add(I18n.format("signpic.item.sign.desc.keyconflict.key", I18n.format(key.getKeyCategory()), I18n.format(key.getKeyDescription())));
			}
		}
	}
}