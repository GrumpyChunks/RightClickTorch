package org.walterweight.rightclicktorch;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

@Mod(modid = RightClickTorch.MODID, name = RightClickTorch.MODID)
public class RightClickTorch
{

	public static final String MODID = "RightClickTorch";
	private static final int NOTORCHESFOUND = -1;
	private boolean processingEvent = false;

	@Mod.Instance(MODID)
	public static RightClickTorch instance;


	public RightClickTorch()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}


	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
	}


	@SubscribeEvent
	public void playerInteractEventHandler(PlayerInteractEvent event)
	{

		if (processingEvent | eventNotRelevant(event))
			return;

		ItemStack itemInHand = event.entityPlayer.inventory.getCurrentItem();

		if (notHoldingTool(itemInHand))
			return;

		InventoryPlayer inventory = event.entityPlayer.inventory;
		int mainInventoryTorchSlotIndex = getTorchSlotIndex(inventory.mainInventory);

		if (mainInventoryTorchSlotIndex == NOTORCHESFOUND)
			return;

		if (!targetBlockAcceptsTorches(event))
			return;

		processingEvent = true;
		ItemStack torchStack = inventory.mainInventory[mainInventoryTorchSlotIndex];
		useItem(event, torchStack);

		if (torchStack.stackSize == 0)
			inventory.mainInventory[mainInventoryTorchSlotIndex] = null;

		event.entityPlayer.openContainer.detectAndSendChanges();
		processingEvent = false;
		event.setCanceled(true);
	}


	private void useItem(PlayerInteractEvent event, ItemStack torchStack)
	{
		((EntityPlayerMP) event.entityPlayer).theItemInWorldManager
				.activateBlockOrUseItem(event.entityPlayer, event.world, torchStack, event.x, event.y, event.z,
						event.face, 0.5f, 0.5f, 0.5f);
	}


	private boolean targetBlockAcceptsTorches(PlayerInteractEvent event)
	{
		// Unless I'm having a serious brain fart, it seems that .canPlaceTorchOnTop() should be a static method from the Block class
		Block block = event.world.getBlock(event.x, event.y, event.z);
		return block.canPlaceTorchOnTop(event.world, event.x, event.y, event.z);

	}


	private boolean eventNotRelevant(PlayerInteractEvent event)
	{
		return event
				.isCanceled() || event.world.isRemote || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;
	}


	private boolean notHoldingTool(ItemStack itemInHand)
	{
		return itemInHand == null || !(itemInHand.getItem() instanceof ItemTool);
	}


	private int getTorchSlotIndex(ItemStack[] itemStacks)
	{
		for (int index = 0; index < itemStacks.length; index++)
		{
			if (itemStacks[index] != null && itemStacks[index].getItem() == net.minecraft.item.Item
					.getItemFromBlock(Blocks.torch))
				return index;
		}
		return NOTORCHESFOUND;
	}


}