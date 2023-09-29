package com.zuxelus.energycontrol.items.cards;

import java.util.ArrayList;
import java.util.List;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.utils.FluidInfo;
import com.zuxelus.energycontrol.utils.StringUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCardLiquidArray extends ItemCardBase {
	private static final long STATUS_NOT_FOUND = Integer.MIN_VALUE;
	private static final long STATUS_OUT_OF_RANGE = Integer.MIN_VALUE + 1;

	public ItemCardLiquidArray() {
		super(ItemCardType.CARD_LIQUID_ARRAY, "card_liquid_array");
	}

	@Override
	public CardState update(World world, ICardReader reader, int range, BlockPos pos) {
		int cardCount = reader.getCardCount();
		if (cardCount == 0)
			return CardState.INVALID_CARD;

		double totalAmount = 0.0;

		boolean foundAny = false;
		boolean outOfRange = false;
		for (int i = 0; i < cardCount; i++) {
			BlockPos target = getCoordinates(reader, i);
			int dx = target.getX() - pos.getX();
			int dy = target.getY() - pos.getY();
			int dz = target.getZ() - pos.getZ();
			if (Math.abs(dx) <= range && Math.abs(dy) <= range && Math.abs(dz) <= range) {
				FluidInfo storage = CrossModLoader.getTankAt(world, target);
				if (storage != null) {
					storage.write(reader, i);
					foundAny = true;
				} else
					reader.setLong(String.format("_%damount", i), STATUS_NOT_FOUND);
			} else {
				reader.setLong(String.format("_%damount", i), STATUS_OUT_OF_RANGE);
				outOfRange = true;
			}
		}
		reader.setDouble("energy", totalAmount);
		if (!foundAny) {
			if (outOfRange)
				return CardState.OUT_OF_RANGE;
			return CardState.NO_TARGET;
		}
		return CardState.OK;
	}

	@Override
	public List<PanelString> getStringData(int settings, ICardReader reader, boolean isServer, boolean showLabels) {
		List<PanelString> result = reader.getTitleList();
		double totalAmount = 0;
		double totalCapacity = 0;
		boolean showName = (settings & 1) > 0;
		boolean showAmount = true;
		boolean showFree = (settings & 2) > 0;
		boolean showCapacity = (settings & 4) > 0;
		boolean showPercentage = (settings & 8) > 0;
		boolean showEach = (settings & 16) > 0;
		boolean showSummary = (settings & 32) > 0;
		for (int i = 0; i < reader.getInt("cardCount"); i++) {
			long amount = reader.getLong(String.format("_%damount", i));
			long capacity = reader.getLong(String.format("_%dcapacity", i));
			boolean isOutOfRange = amount == STATUS_OUT_OF_RANGE;
			boolean isNotFound = amount == STATUS_NOT_FOUND;
			if (showSummary && !isOutOfRange && !isNotFound) {
				totalAmount += amount;
				totalCapacity += capacity;
			}

			if (showEach) {
				if (isOutOfRange) {
					result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelOutOfRangeN", i + 1)));
				} else if (isNotFound) {
					result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelNotFoundN", i + 1)));
				} else {
					if (showName) {
						if (showLabels)
							result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelLiquidNameN", i + 1,
									reader.getString(String.format("_%dname", i)))));
						else
							result.add(new PanelString(StringUtils.getFormatted("", amount, false)));
					}
					if (showAmount) {
						if (showLabels)
							result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelLiquidN", i + 1,
									StringUtils.getFormatted("", amount, false))));
						else
							result.add(new PanelString(StringUtils.getFormatted("", amount, false)));
					}
					if (showFree) {
						if (showLabels)
							result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelLiquidFreeN", i + 1,
									StringUtils.getFormatted("", capacity - amount, false))));
						else
							result.add(new PanelString(StringUtils.getFormatted("", capacity - amount, false)));
					}
					if (showCapacity) {
						if (showLabels)
							result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelLiquidCapacityN",
									i + 1, StringUtils.getFormatted("", capacity, false))));
						else
							result.add(new PanelString(StringUtils.getFormatted("", capacity, false)));
					}
					if (showPercentage) {
						if (showLabels)
							result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelLiquidPercentageN",
									i + 1, StringUtils.getFormatted("",
											capacity == 0 ? 100 : (((double) amount / capacity) * 100), false))));
						else
							result.add(new PanelString(StringUtils.getFormatted("",
									capacity == 0 ? 100 : (((double) amount / capacity) * 100), false)));
					}
				}
			}
		}
		if (showSummary) {
			if (showAmount)
				result.add(new PanelString("msg.ec.InfoPanelAmount", totalAmount, showLabels));
			if (showFree)
				result.add(new PanelString("msg.ec.InfoPanelFree", totalCapacity - totalAmount, showLabels));
			if (showName)
				result.add(new PanelString("msg.ec.InfoPanelCapacity", totalCapacity, showLabels));
			if (showPercentage)
				result.add(new PanelString("msg.ec.InfoPanelPercentage",
						totalCapacity == 0 ? 100 : ((totalAmount / totalCapacity) * 100), showLabels));
		}
		return result;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<PanelSetting> getSettingsList(ItemStack stack) {
		List<PanelSetting> result = new ArrayList<>(6);
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelLiquidName"), 1));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelLiquidFree"), 2));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelLiquidCapacity"), 4));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelLiquidPercentage"), 8));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelEachCard"), 16));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelTotal"), 32));
		return result;
	}

	@Override
	public boolean isRemoteCard(ItemStack stack) {
		return false;
	}
}
