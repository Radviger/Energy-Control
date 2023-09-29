package com.zuxelus.energycontrol.crossmod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ItemStackHelper;
import com.zuxelus.energycontrol.crossmod.computercraft.CrossComputerCraft;
import com.zuxelus.energycontrol.crossmod.opencomputers.CrossOpenComputers;
import com.zuxelus.energycontrol.init.ModItems;
import com.zuxelus.energycontrol.items.cards.ItemCardType;
import com.zuxelus.energycontrol.utils.FluidInfo;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class CrossModLoader {
	private static final Map<String, CrossModBase> CROSS_MODS = new HashMap<>();

	public static void preInit() {
		CROSS_MODS.put(ModIDs.IC2, Loader.isModLoaded("ic2-classic-spmod") ? new CrossIC2Classic() : Loader.isModLoaded(ModIDs.IC2) ? new CrossIC2Exp() : new CrossModBase());
		loadCrossMod(ModIDs.TECH_REBORN, CrossTechReborn::new);
		loadCrossModSafely(ModIDs.APPLIED_ENERGISTICS, () -> CrossAppEng::new);
		loadCrossMod(ModIDs.EXTREME_REACTORS, CrossExtremeReactors::new);
		loadCrossMod(ModIDs.BUILDCRAFT, CrossBuildCraft::new);
		loadCrossModSafely(ModIDs.DRACONIC_EVOLUTION, () -> CrossDraconicEvolution::new);
		loadCrossModSafely(ModIDs.ENDER_IO, () -> CrossEnderIO::new);
		loadCrossMod(ModIDs.GALACTICRAFT_PLANETS, CrossGalacticraft::new);
		loadCrossMod(ModIDs.GREGTECH, CrossGregTech::new);
		loadCrossModSafely(ModIDs.HBM, () -> CrossHBM::new);
		loadCrossMod(ModIDs.MEKANISM, CrossMekanism::new);
		loadCrossMod(ModIDs.MEKANISM_GENERATORS, CrossMekanismGenerators::new);
		ModContainer nc = Loader.instance().getIndexedModList().get(ModIDs.NUCLEAR_CRAFT);
		if (nc != null)
			if (nc.getVersion().contains("2o"))
				loadCrossMod(ModIDs.NUCLEAR_CRAFT, CrossNuclearCraftOverhauled::new);
			else
				loadCrossMod(ModIDs.NUCLEAR_CRAFT, CrossNuclearCraft::new);
		loadCrossModSafely(ModIDs.COMPUTER_CRAFT, () -> CrossComputerCraft::new);
		loadCrossMod(ModIDs.PNEUMATICCRAFT, CrossPneumaticCraft::new);
		loadCrossModSafely(ModIDs.RAILCRAFT, () -> CrossRailcraft::new);
		loadCrossMod(ModIDs.THERMAL_EXPANSION, CrossThermalExpansion::new);
	}

	private static void loadCrossMod(String modid, Supplier<? extends CrossModBase> factory) {
		CROSS_MODS.put(modid, Loader.isModLoaded(modid) ? factory.get() : new CrossModBase());
	}

	private static void loadCrossModSafely(String modid, Supplier<Supplier<? extends CrossModBase>> factory) {
		CROSS_MODS.put(modid, Loader.isModLoaded(modid) ? factory.get().get() : new CrossModBase());
	}

	public static void init() {
		loadCrossMod(ModIDs.OPEN_COMPUTERS, CrossOpenComputers::new);
	}

	public static CrossModBase getCrossMod(String modid) {
		return CROSS_MODS.get(modid);
	}

	public static ItemStack getEnergyCard(World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te == null)
			return ItemStack.EMPTY;
		NBTTagCompound data = getEnergyData(te);
		if (data != null) {
			ItemStack card = new ItemStack(ModItems.itemCard, 1, ItemCardType.CARD_ENERGY);
			ItemStackHelper.setCoordinates(card, pos);
			return card;
		}
		return ItemStack.EMPTY;
	}

	public static NBTTagCompound getEnergyData(TileEntity te) {
		if (te == null)
			return null;
		for (CrossModBase crossMod : CROSS_MODS.values()) {
			NBTTagCompound tag = crossMod.getEnergyData(te);
			if (tag != null)
				return tag;
		}
		IEnergyStorage storage = te.getCapability(CapabilityEnergy.ENERGY, null);
		if (storage != null) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("euType", "FE");
			tag.setDouble("storage", storage.getEnergyStored());
			tag.setDouble("maxStorage", storage.getMaxEnergyStored());
			return tag;
		}
		return null;
	}

	public static List<FluidInfo> getAllTanks(World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te == null)
			return null;
		for (CrossModBase crossMod : CROSS_MODS.values()) {
			List<FluidInfo> list = crossMod.getAllTanks(te);
			if (list != null)
				return list;
		}
		IFluidHandler fluid = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
		if (fluid != null) {
			IFluidTankProperties[] tanks = fluid.getTankProperties();
			List<FluidInfo> result = new ArrayList<>();
			for (IFluidTankProperties tank : tanks)
				result.add(new FluidInfo(tank.getContents(), tank.getCapacity()));
			return result;
		}
		return null;
	}

	public static FluidInfo getTankAt(World world, BlockPos pos) {
		List<FluidInfo> tanks = getAllTanks(world, pos);
		return tanks != null && tanks.size() > 0 ? tanks.get(0) : null;
	}

	public static int getHeat(World world, BlockPos pos) {
		for (CrossModBase crossMod : CROSS_MODS.values()) {
			int heat = crossMod.getHeat(world, pos);
			if (heat != -1)
				return heat;
		}
		return -1;
	}

	public static NBTTagCompound getInventoryData(TileEntity te) {
		if (te == null)
			return null;
		for (CrossModBase crossMod : CROSS_MODS.values()) {
			NBTTagCompound tag = crossMod.getInventoryData(te);
			if (tag != null)
				return tag;
		}
		IItemHandler storage = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if (storage == null && !(te instanceof IInventory))
			return null;
		NBTTagCompound tag = new NBTTagCompound();
		if (storage != null) {
			int inUse = 0;
			int items = 0;
			tag.setInteger("size", storage.getSlots());
			for (int i = 0; i < Math.min(6, storage.getSlots()); i++) {
				if (storage.getStackInSlot(i) != ItemStack.EMPTY) {
					inUse++;
					items += storage.getStackInSlot(i).getCount();
				}
				tag.setTag("slot" + Integer.toString(i), storage.getStackInSlot(i).writeToNBT(new NBTTagCompound()));
			}
			tag.setInteger("used", inUse);
			tag.setInteger("items", items);
		}
		if (te instanceof IInventory) {
			IInventory inv = (IInventory) te;
			tag.setString("name", inv.getName());
			tag.setBoolean("sided", inv instanceof ISidedInventory);
			if (storage == null) {
				int inUse = 0;
				int items = 0;
				tag.setInteger("size", inv.getSizeInventory());
				for (int i = 0; i < Math.min(6, inv.getSizeInventory()); i++) {
					if (inv.getStackInSlot(i) != ItemStack.EMPTY) {
						inUse++;
						items += inv.getStackInSlot(i).getCount();
					}
					tag.setTag("slot" + Integer.toString(i), inv.getStackInSlot(i).writeToNBT(new NBTTagCompound()));
				}
				tag.setInteger("used", inUse);
				tag.setInteger("items", items);
			}
		}
		return tag;
	}

	public static boolean isElectricItem(ItemStack stack) {
		if (stack.isEmpty())
			return false;

		for (CrossModBase crossMod : CROSS_MODS.values())
			if (crossMod.isElectricItem(stack))
				return true;
		return false;
	}

	public static double dischargeItem(ItemStack stack, double amount, int tier) {
		for (CrossModBase crossMod : CROSS_MODS.values())
			if (crossMod.isElectricItem(stack)) {
				double result = crossMod.dischargeItem(stack, amount, tier);
				if (result > 0)
					return result;
			}
		return 0;
	}

	public static void registerBlocks(Register<Block> event) {
		for (CrossModBase crossMod : CROSS_MODS.values())
			crossMod.registerBlocks(event);
	}

	public static void registerItems(Register<Item> event) {
		for (CrossModBase crossMod : CROSS_MODS.values())
			crossMod.registerItems(event);
	}

	public static void registerModels(ModelRegistryEvent event) {
		for (CrossModBase crossMod : CROSS_MODS.values())
			crossMod.registerModels(event);
	}

	public static void registerTileEntities() {
		for (CrossModBase crossMod : CROSS_MODS.values())
			crossMod.registerTileEntities();
	}

	public static void loadOreInfo() {
		for (CrossModBase crossMod : CROSS_MODS.values())
			crossMod.loadOreInfo();
	}
}
