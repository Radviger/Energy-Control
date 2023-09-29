package com.zuxelus.energycontrol.crossmod;

import java.util.ArrayList;
import java.util.List;

import com.zuxelus.energycontrol.hooks.MekanismHooks;
import com.zuxelus.energycontrol.utils.DataHelper;
import com.zuxelus.energycontrol.utils.FluidInfo;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.infuse.InfuseType;
import mekanism.common.FluidSlot;
import mekanism.common.InfuseStorage;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mekanism.common.tile.*;
import mekanism.common.tile.prefab.*;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class CrossMekanism extends CrossModBase {

	@Override
	public NBTTagCompound getEnergyData(TileEntity te) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString(DataHelper.EUTYPE, getEUType());
		if (te instanceof TileEntityInductionCell) {
			tag.setDouble(DataHelper.ENERGY, MekanismUtils.convertToDisplay(((TileEntityInductionCell) te).getEnergy()));
			tag.setDouble(DataHelper.CAPACITY, MekanismUtils.convertToDisplay(((TileEntityInductionCell) te).getMaxEnergy()));
			return tag;
		}
		if (te instanceof TileEntityElectricBlock)
			return setStorage(te);
		if (te instanceof TileEntityInductionCasing) {
			SynchronizedMatrixData matrix = ((TileEntityInductionCasing) te).getSynchronizedData();
			if (matrix == null)
				return null;
			tag.setDouble(DataHelper.ENERGY, MekanismUtils.convertToDisplay(matrix.getEnergy()));
			tag.setDouble(DataHelper.CAPACITY, MekanismUtils.convertToDisplay(matrix.getStorageCap()));
			return tag;
		}
		if (te instanceof TileEntityLaserAmplifier) {
			tag.setDouble(DataHelper.ENERGY, MekanismUtils.convertToDisplay(((TileEntityLaserAmplifier) te).getEnergy()));
			tag.setDouble(DataHelper.CAPACITY, MekanismUtils.convertToDisplay(((TileEntityLaserAmplifier) te).getMaxEnergy()));
			return tag;
		}
		return null;
	}

	@Override
	public List<FluidInfo> getAllTanks(TileEntity te) {
		List<FluidInfo> result = new ArrayList<>();
		if (te instanceof TileEntityFluidTank) {
			result.add(new FluidInfo(((TileEntityFluidTank) te).fluidTank));
			return result;
		}
		if (te instanceof TileEntityAdvancedElectricMachine) {
			result.add(toFluidInfo(((TileEntityAdvancedElectricMachine) te).gasTank));
			return result;
		}
		if (te instanceof TileEntityMetallurgicInfuser) {
			result.add(toFluidInfo(((TileEntityMetallurgicInfuser) te).infuseStored, ((TileEntityMetallurgicInfuser) te).MAX_INFUSE));
			return result;
		}
		if (te instanceof TileEntityRotaryCondensentrator) {
			result.add(toFluidInfo(((TileEntityRotaryCondensentrator) te).gasTank));
			result.add(new FluidInfo(((TileEntityRotaryCondensentrator) te).fluidTank));
			return result;
		}
		if (te instanceof TileEntityChemicalOxidizer) {
			result.add(toFluidInfo(((TileEntityChemicalOxidizer) te).gasTank));
			return result;
		}
		if (te instanceof TileEntityChemicalInfuser) {
			result.add(toFluidInfo(((TileEntityChemicalInfuser) te).leftTank));
			result.add(toFluidInfo(((TileEntityChemicalInfuser) te).rightTank));
			result.add(toFluidInfo(((TileEntityChemicalInfuser) te).centerTank));
			return result;
		}
		if (te instanceof TileEntityElectrolyticSeparator) {
			result.add(new FluidInfo(((TileEntityElectrolyticSeparator) te).fluidTank));
			result.add(toFluidInfo(((TileEntityElectrolyticSeparator) te).leftTank));
			result.add(toFluidInfo(((TileEntityElectrolyticSeparator) te).rightTank));
			return result;
		}
		if (te instanceof TileEntityChemicalDissolutionChamber) {
			result.add(toFluidInfo(((TileEntityChemicalDissolutionChamber) te).injectTank));
			result.add(toFluidInfo(((TileEntityChemicalDissolutionChamber) te).outputTank));
			return result;
		}
		if (te instanceof TileEntityChemicalWasher) {
			result.add(new FluidInfo(((TileEntityChemicalWasher) te).fluidTank));
			result.add(toFluidInfo(((TileEntityChemicalWasher) te).inputTank));
			result.add(toFluidInfo(((TileEntityChemicalWasher) te).outputTank));
			return result;
		}
		if (te instanceof TileEntityChemicalCrystallizer) {
			result.add(toFluidInfo(((TileEntityChemicalCrystallizer) te).inputTank));
			return result;
		}
		if (te instanceof TileEntityBoilerCasing) {
			SynchronizedBoilerData boiler = ((TileEntityBoilerCasing) te).getSynchronizedData();
			if (boiler == null)
				return null;

			result.add(new FluidInfo(boiler.waterStored, boiler.waterVolume * 16000));
			result.add(new FluidInfo(boiler.steamStored, boiler.steamVolume * 160000));
			return result;
		}
		if (te instanceof TileEntitySolarNeutronActivator) {
			result.add(toFluidInfo(((TileEntitySolarNeutronActivator) te).inputTank));
			result.add(toFluidInfo(((TileEntitySolarNeutronActivator) te).outputTank));
			return result;
		}
		if (te instanceof TileEntityThermalEvaporationController) {
			result.add(new FluidInfo(((TileEntityThermalEvaporationController) te).inputTank));
			result.add(new FluidInfo(((TileEntityThermalEvaporationController) te).outputTank));
		}
		return null;
	}

	public static FluidInfo toFluidInfo(GasTank tank) {
		GasStack stack = tank.getGas();
		if (stack == null)
			return new FluidInfo("", null, 0, tank.getMaxGas());
		Fluid fluid = stack.getGas().getFluid();
		if (fluid != null)
			return new FluidInfo(fluid, stack.getGas().getLocalizedName(), stack.amount, tank.getMaxGas());
		return new FluidInfo(stack.getGas().getLocalizedName(), stack.getGas().getIcon().toString(), stack.amount, tank.getMaxGas());
	}

	public static FluidInfo toFluidInfo(InfuseStorage tank, int capacity) {
		if (tank.getType() == null)
			return new FluidInfo("", null, tank.getAmount(), capacity);
		return new FluidInfo(tank.getType().unlocalizedName, null, tank.getAmount(), capacity);
	}

	@Override
	public NBTTagCompound getCardData(World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityEnergyCube) {
			NBTTagCompound tag = setStorage(te);
			ArrayList values = getHookValues(te);
			if (values != null)
				tag.setDouble(DataHelper.DIFF, MekanismUtils.convertToDisplay(((Double) values.get(0) - (Double) values.get(20)) / 20.0D));
			return tag;
		}
		if (te instanceof TileEntityFactory) {
			NBTTagCompound tag = setStorage(te);
			tag.setDouble("usage", MekanismUtils.convertToDisplay(((TileEntityFactory) te).lastUsage));
			tag.setBoolean(DataHelper.ACTIVE, ((TileEntityFactory) te).lastUsage > 0);
			return tag;
		}
		if (te instanceof TileEntityElectricMachine) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			return tag;
		}
		if (te instanceof TileEntityAdvancedElectricMachine) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			addTank(DataHelper.TANK, tag, ((TileEntityAdvancedElectricMachine) te).gasTank);
			return tag;
		}
		if (te instanceof TileEntityCombiner) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			return tag;
		}
		if (te instanceof TileEntityMetallurgicInfuser) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			addTank(DataHelper.TANK, tag, ((TileEntityMetallurgicInfuser) te).infuseStored);
			return tag;
		}
		if (te instanceof TileEntityTeleporter) {
			NBTTagCompound tag = setStorage(te);
			return tag;
		}
		if (te instanceof TileEntityElectricPump) {
			NBTTagCompound tag = setStorage(te);
			FluidInfo.addTank(DataHelper.TANK, tag, ((TileEntityElectricPump) te).fluidTank);
			return tag;
		}
		if (te instanceof TileEntityChargepad) {
			NBTTagCompound tag = setStorage(te);
			tag.setBoolean(DataHelper.ACTIVE, ((TileEntityChargepad) te).getActive());
			return tag;
		}
		if (te instanceof TileEntityRotaryCondensentrator) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			addTank(DataHelper.TANK, tag, ((TileEntityRotaryCondensentrator) te).gasTank);
			FluidInfo.addTank(DataHelper.TANK2, tag, ((TileEntityRotaryCondensentrator) te).fluidTank);
			return tag;
		}
		if (te instanceof TileEntityChemicalOxidizer) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			addTank(DataHelper.TANK, tag, ((TileEntityChemicalOxidizer) te).gasTank);
			return tag;
		}
		if (te instanceof TileEntityChemicalInfuser) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			addTank(DataHelper.TANK, tag, ((TileEntityChemicalInfuser) te).leftTank);
			addTank(DataHelper.TANK2, tag, ((TileEntityChemicalInfuser) te).rightTank);
			addTank(DataHelper.TANK3, tag, ((TileEntityChemicalInfuser) te).centerTank);
			return tag;
		}
		if (te instanceof TileEntityElectrolyticSeparator) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			FluidInfo.addTank(DataHelper.TANK, tag, ((TileEntityElectrolyticSeparator) te).fluidTank);
			addTank(DataHelper.TANK2, tag, ((TileEntityElectrolyticSeparator) te).leftTank);
			addTank(DataHelper.TANK3, tag, ((TileEntityElectrolyticSeparator) te).rightTank);
			return tag;
		}
		if (te instanceof TileEntityPrecisionSawmill) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			return tag;
		}
		if (te instanceof TileEntityChemicalDissolutionChamber) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			addTank(DataHelper.TANK, tag, ((TileEntityChemicalDissolutionChamber) te).injectTank);
			addTank(DataHelper.TANK2, tag, ((TileEntityChemicalDissolutionChamber) te).outputTank);
			return tag;
		}
		if (te instanceof TileEntityChemicalWasher) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			FluidInfo.addTank(DataHelper.TANK, tag, ((TileEntityChemicalWasher) te).fluidTank);
			addTank(DataHelper.TANK2, tag, ((TileEntityChemicalWasher) te).inputTank);
			addTank(DataHelper.TANK3, tag, ((TileEntityChemicalWasher) te).outputTank);
			return tag;
		}
		if (te instanceof TileEntityChemicalCrystallizer) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			addTank(DataHelper.TANK, tag, ((TileEntityChemicalCrystallizer) te).inputTank);
			return tag;
		}
		if (te instanceof TileEntitySeismicVibrator) {
			NBTTagCompound tag = setStorage(te);
			return tag;
		}
		if (te instanceof TileEntityResistiveHeater) {
			NBTTagCompound tag = setStorage(te);
			if (((TileEntityResistiveHeater) te).getActive()) {
				tag.setDouble("usage", MekanismUtils.convertToDisplay(((TileEntityResistiveHeater) te).energyUsage));
				tag.setBoolean(DataHelper.ACTIVE, true);
			} else {
				tag.setDouble("usage", 0);
				tag.setBoolean(DataHelper.ACTIVE, false);
			}
			tag.setString("temp", CrossMekanism.getTempString(((TileEntityResistiveHeater) te).temperature + 300));
			return tag;
		}
		if (te instanceof TileEntityLaser) {
			NBTTagCompound tag = setStorage(te);
			return tag;
		}
		if (te instanceof TileEntityLaserAmplifier) {
			NBTTagCompound tag = new NBTTagCompound();
			EnergyType euType = MekanismConfig.current().general.energyUnit.val();
			tag.setString(DataHelper.EUTYPE, euType.name());
			tag.setDouble(DataHelper.ENERGY, MekanismUtils.convertToDisplay(((TileEntityLaserAmplifier) te).getEnergy()));
			tag.setDouble(DataHelper.CAPACITY, MekanismUtils.convertToDisplay(((TileEntityLaserAmplifier) te).getMaxEnergy()));
			return tag;
		}
		if (te instanceof TileEntityBoilerCasing) {
			SynchronizedBoilerData boiler = ((TileEntityBoilerCasing) te).getSynchronizedData();
			if (boiler == null)
				return null;
			NBTTagCompound tag = setStorage(te);
			tag.setDouble("boil_rate", boiler.lastBoilRate);
			tag.setString("temp", CrossMekanism.getTempString(boiler.getTemp() + 300));
			FluidInfo.addTank(DataHelper.TANK, tag, boiler.waterStored);
			FluidInfo.addTank(DataHelper.TANK2, tag, boiler.steamStored);
			return tag;
		}
		if (te instanceof TileEntityInductionCasing) {
			SynchronizedMatrixData matrix = ((TileEntityInductionCasing) te).getSynchronizedData();
			if (matrix == null)
				return null;
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString(DataHelper.EUTYPE, getEUType());
			tag.setDouble(DataHelper.ENERGY, MekanismUtils.convertToDisplay(matrix.getEnergy()));
			tag.setDouble(DataHelper.CAPACITY, MekanismUtils.convertToDisplay(matrix.getStorageCap()));
			tag.setDouble("input", MekanismUtils.convertToDisplay(matrix.getLastInput()));
			tag.setDouble("output", MekanismUtils.convertToDisplay(matrix.getLastOutput()));
			return tag;
		}
		if (te instanceof TileEntityFormulaicAssemblicator) {
			NBTTagCompound tag = setStorage(te);
			return tag;
		}
		if (te instanceof TileEntitySolarNeutronActivator) {
			NBTTagCompound tag = new NBTTagCompound();
			addTank(DataHelper.TANK, tag, ((TileEntitySolarNeutronActivator) te).inputTank);
			addTank(DataHelper.TANK2, tag, ((TileEntitySolarNeutronActivator) te).outputTank);
			return tag;
		}
		if (te instanceof TileEntityFuelwoodHeater) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setBoolean(DataHelper.ACTIVE, ((TileEntityFuelwoodHeater) te).getActive());
			tag.setString("temp", CrossMekanism.getTempString(((TileEntityFuelwoodHeater) te).temperature + 300));
			return tag;
		}
		if (te instanceof TileEntityFluidicPlenisher) {
			NBTTagCompound tag = setStorage(te);
			tag.setDouble("usage", MekanismUtils.convertToDisplay(((TileEntityFluidicPlenisher) te).energyPerTick));
			FluidInfo.addTank(DataHelper.TANK, tag, ((TileEntityFluidicPlenisher) te).fluidTank);
			return tag;
		}
		if (te instanceof TileEntityPRC) {
			NBTTagCompound tag = setStorage(te);
			addUsage(tag, te);
			FluidInfo.addTank(DataHelper.TANK, tag, ((TileEntityPRC) te).inputFluidTank);
			addTank(DataHelper.TANK2, tag, ((TileEntityPRC) te).inputGasTank);
			addTank(DataHelper.TANK3, tag, ((TileEntityPRC) te).outputGasTank);
			return tag;
		}
		if (te instanceof TileEntityThermalEvaporationController) {
			TileEntityThermalEvaporationController controller = (TileEntityThermalEvaporationController) te;
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("temp", CrossMekanism.getTempString(controller.temperature + 300));
			tag.setInteger("height", controller.height);
			ThermalEvaporationRecipe recipe = controller.getRecipe();
			if (recipe != null)
				tag.setDouble(DataHelper.CONSUMPTIONMB, controller.lastGain * ((FluidInput)recipe.recipeInput).ingredient.amount);
			tag.setDouble(DataHelper.OUTPUTMB, controller.lastGain);
			FluidInfo.addTank(DataHelper.TANK, tag, controller.inputTank);
			FluidInfo.addTank(DataHelper.TANK2, tag, controller.outputTank);
			return tag;
		}
		return null;
	}

	private static void addUsage(NBTTagCompound tag, TileEntity te) {
		if (!(te instanceof TileEntityMachine))
			return;
		if (((TileEntityMachine) te).getActive()) {
			tag.setDouble("usage", MekanismUtils.convertToDisplay(((TileEntityMachine) te).energyPerTick));
			tag.setBoolean(DataHelper.ACTIVE, true);
		} else {
			tag.setDouble("usage", 0);
			tag.setBoolean(DataHelper.ACTIVE, false);
		}
	}

	public static void addTank(String name, NBTTagCompound tag, GasTank tank) {
		GasStack stack = tank.getGas();
		if (stack == null)
			tag.setString(name, "N/A");
		else
			tag.setString(name, String.format("%s: %s mB", stack.getGas().getLocalizedName(), stack.amount));
	}

	public static void addTank(String name, NBTTagCompound tag, InfuseStorage tank) {
		InfuseType type = tank.getType();
		if (type == null)
			tag.setString(name, "N/A");
		else
			tag.setString(name, String.format("%s: %s mB", type.getLocalizedName(), tank.getAmount()));
	}

	public static void addTank(String name, NBTTagCompound tag, FluidSlot tank, String fluid) {
		if (tank.fluidStored == 0)
			tag.setString(name, "N/A");
		else
			tag.setString(name, String.format("%s: %s mB", I18n.translateToLocal("gui.bioGenerator.bioFuel"), tank.fluidStored));
	}

	public static NBTTagCompound setStorage(TileEntity te) {
		NBTTagCompound tag = new NBTTagCompound();
		if (!(te instanceof TileEntityElectricBlock))
			return tag;
		TileEntityElectricBlock tile = (TileEntityElectricBlock) te;
		tag.setString(DataHelper.EUTYPE, getEUType());
		tag.setDouble(DataHelper.ENERGY, MekanismUtils.convertToDisplay(tile.getEnergy()));
		tag.setDouble(DataHelper.CAPACITY, MekanismUtils.convertToDisplay(tile.getMaxEnergy()));
		return tag;
	}

	public static String getTempString(double temp) {
		switch (MekanismConfig.current().general.tempUnit.val()) {
		case K:
			return formatTemp(TemperatureUnit.KELVIN, temp);
		case C:
			return formatTemp(TemperatureUnit.CELSIUS, temp);
		case R:
			return formatTemp(TemperatureUnit.RANKINE, temp);
		case F:
			return formatTemp(TemperatureUnit.FAHRENHEIT, temp);
		case STP:
			return formatTemp(TemperatureUnit.AMBIENT, temp);
		default:
			return "";
		}
	}

	private static String formatTemp(TemperatureUnit unit, double temp) {
		return String.format("%.3f %s", unit.convertFromK(temp, true), unit.symbol);
	}

	public static String getEUType() {
		return MekanismConfig.current().general.energyUnit.val().name();
	}

	@Override
	public ArrayList getHookValues(TileEntity te) {
		ArrayList values = MekanismHooks.map.get(te);
		if (values == null)
			MekanismHooks.map.put(te, null);
		return values;
	}
}
