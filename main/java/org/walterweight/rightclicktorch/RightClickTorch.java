package org.walterweight.rightclicktorch;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

@Mod(modid = RightClickTorch.MODID, name= RightClickTorch.MODID)
public class RightClickTorch {

    public static final String MODID = "RightClickTorch";
    private static final int NOTORCHESFOUND = -1;
    private boolean processingEvent = false;

    @Mod.Instance(MODID)
    public static RightClickTorch instance;

    public RightClickTorch(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
    }

    @SubscribeEvent
    public void playerInteractEventHandler(PlayerInteractEvent event){

        if (processingEvent | eventNotRelevant(event))
            return;

        ItemStack itemInHand = event.entityPlayer.inventory.getCurrentItem();

        if (notHoldingTool(itemInHand))
            return;

        InventoryPlayer inventory = event.entityPlayer.inventory;
        int torchSlotIndex = getTorchSlotIndex(inventory);

        if (torchSlotIndex == NOTORCHESFOUND)
            return;

        processingEvent = true;
        ItemStack torchStack = inventory.getStackInSlot(torchSlotIndex);

        useItem(event, torchStack);
	    torchStack = clearZeroSizedStack(torchStack);
	    torchSlotIndex = sanitiseTorchSlotIndex(torchSlotIndex);
	    refreshPlayerInventory(event, torchSlotIndex, torchStack);
	    processingEvent = false;
	    event.setCanceled(true);
    }

	private ItemStack clearZeroSizedStack(ItemStack torchStack) {
		if (torchStack.stackSize == 0)
			torchStack = null;
		return torchStack;
	}

	private int sanitiseTorchSlotIndex(int torchSlotIndex) {
		if (torchSlotIndex < 9)
			torchSlotIndex += 36;
		return torchSlotIndex;
	}

	private void refreshPlayerInventory(PlayerInteractEvent event, int torchSlotIndex, ItemStack torchStack) {
		((EntityPlayerMP) event.entityPlayer).playerNetServerHandler.sendPacket(new S2FPacketSetSlot(event.entityPlayer.openContainer.windowId, torchSlotIndex, torchStack));
	}

	private void useItem(PlayerInteractEvent event, ItemStack torchStack){
        ((EntityPlayerMP) event.entityPlayer).theItemInWorldManager.activateBlockOrUseItem(event.entityPlayer, event.world, torchStack, event.x, event.y, event.z, event.face, 0.5f, 0.5f, 0.5f);
    }

    private boolean eventNotRelevant(PlayerInteractEvent event){
	    return event.isCanceled() || event.world.isRemote || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;
    }

    private boolean notHoldingTool(ItemStack itemInHand){
	    return itemInHand == null || !(itemInHand.getItem() instanceof ItemTool);
    }

    private int getTorchSlotIndex(InventoryPlayer inventory){
        for (int index = 0; index < inventory.mainInventory.length; index++){
            if (inventory.mainInventory[index] != null && inventory.mainInventory[index].getItem() == net.minecraft.item.Item.getItemFromBlock(Blocks.torch))
                return index;
        }
        return -1;
    }

}