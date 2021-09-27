package com.zuxelus.energycontrol;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientTickHandler {
	public final static ClientTickHandler instance = new ClientTickHandler();
	public static List<TileEntity> holo_panels = Lists.newArrayList();

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if (EnergyControl.oreHelper == null)
			return;
		ItemStack stack = event.getItemStack();
		if (stack.isEmpty())
			return;
		OreHelper ore = EnergyControl.oreHelper.get(OreHelper.getId(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage()));
		if (ore != null)
			event.getToolTip().add(1, ore.getDescription());
	}

	@SubscribeEvent
	public void render(RenderWorldLastEvent event) {
		TileEntityRendererDispatcher.instance.preDrawBatch();
		for (TileEntity te : holo_panels)
			TileEntityRendererDispatcher.instance.render(te, -1, -1);
		TileEntityRendererDispatcher.instance.drawBatch(0);
		holo_panels.clear();
	}
}
