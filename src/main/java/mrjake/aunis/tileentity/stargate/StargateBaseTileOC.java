//package mrjake.aunis.tileentity.stargate;
//
//import li.cil.oc.api.Network;
//import li.cil.oc.api.machine.Arguments;
//import li.cil.oc.api.machine.Callback;
//import li.cil.oc.api.machine.Context;
//import li.cil.oc.api.network.Environment;
//import li.cil.oc.api.network.Message;
//import li.cil.oc.api.network.Node;
//import li.cil.oc.api.network.Visibility;
//import mrjake.aunis.integration.opencomputers.OCHelper;
//import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToServer;
//import mrjake.aunis.renderer.stargate.StargateRingSpinHelper;
//import mrjake.aunis.sound.AunisSoundHelper;
//import mrjake.aunis.sound.EnumAunisSoundEvent;
//import mrjake.aunis.stargate.EnumGateState;
//import mrjake.aunis.stargate.EnumScheduledTask;
//import mrjake.aunis.stargate.EnumStargateState;
//import mrjake.aunis.stargate.EnumSymbol;
//import mrjake.aunis.tileentity.ScheduledTask;
//import net.minecraft.nbt.NBTTagCompound;
//
//public class StargateBaseTileOC extends StargateBaseTileSG1 implements Environment {
//	
//	@Override
//	public void sendSignal(Object context, String name, Object... params) {
//		OCHelper.sendSignalToReachable(node, (Context) context, name, params);
//	}
//	
//	@Override
//	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
//		if (node != null) {
//			NBTTagCompound nodeCompound = new NBTTagCompound();
//			node.save(nodeCompound);
//			
//			compound.setTag("node", nodeCompound);
//		}
//		
//		return super.writeToNBT(compound);
//	}
//	
//	@Override
//	public void readFromNBT(NBTTagCompound compound) {
//		if (node != null && compound.hasKey("node"))
//			node.load((NBTTagCompound) compound.getTag("node"));
//		
//		super.readFromNBT(compound);
//	}
//	
//	// function(arg:type[, optionArg:type]):resultType; Description.
//	@Callback(getter = true)
//	public Object[] stargateAddress(Context context, Arguments args) {
//		return new Object[] {gateAddress};
//	}
//
//	@Callback(getter = true)
//	public Object[] dialedAddress(Context context, Arguments args) {
//		return new Object[] {dialedAddress};
//	}
//
//	@Callback(doc = "function(symbolName:string) -- Spins the ring to the given symbol and engages/locks it")
//	public Object[] engageSymbol(Context context, Arguments args) {
//		if (!stargateState.idle()) {
//			return new Object[] {null, "stargate_busy", stargateState.toString()};
//		}
//		
//		if (gateAddress.size() == 8) {
//			return new Object[] {null, "stargate_failure_full", "Already dialed 8 chevrons"};
//		}
//		
//		String name = args.checkString(0);
//		
//		EnumSymbol symbol = EnumSymbol.forName(name);
//		
//		if (symbol == null)
//			throw new IllegalArgumentException("bad argument #1 (symbol name invalid)");
//		
//		boolean moveOnly = symbol == targetSymbol;
//		
//		if (dialedAddress.contains(symbol)) {
//			return new Object[] {null, "stargate_failure_contains", "Dialed address contains this symbol already"};
//		}
//		
//		this.targetSymbol = symbol;	
//		this.targetSymbolDialing = true;
//		
//		spinDirection = spinDirection.opposite();
//		
//		double distance = spinDirection.getDistance(getStargateRendererState().ringCurrentSymbol.angle, symbol.angle);
////			Aunis.info("position: " + getStargateRendererState().ringCurrentSymbol.angle + ", target: " + targetSymbol + ", direction: " + spinDirection + ", distance: " + distance + ", moveOnly: " + moveOnly);
//		
//		if (distance < (StargateRingSpinHelper.getStopAngleTraveled() + 5))
//			spinDirection = spinDirection.opposite();
//		
//		int symbolCount = getEnteredSymbolsCount() + 1;
//		boolean lock = symbolCount == 8 || (symbolCount == 7 && symbol == EnumSymbol.ORIGIN);
//		
//		if (moveOnly) {
//			if (lock) {
//				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_SHUT, 1f);
//				
//				addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_CHEVRON_SHUT_SOUND));
//				addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_CHEVRON_OPEN_SOUND));
//			}
//			
//			else
//				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_LOCKING, 0.2f);
//		}
//		
//		stargateState = EnumStargateState.COMPUTER_DIALING;
//		getServerRingSpinHelper().requestStart(getStargateRendererState().ringCurrentSymbol.angle, spinDirection, symbol, lock, context, moveOnly);
//		ringRollLoopPlayed = false || moveOnly;
//		
//		OCHelper.sendSignalToReachable(node, context, "stargate_spin_start", new Object[] { symbolCount, lock, targetSymbol.name });
//		
//		markDirty();
//		
//		return new Object[] {"stargate_spin"};
//	}
//
//	@Callback(doc = "function() -- Tries to open the gate")
//	public Object[] engageGate(Context context, Arguments args) {
//		EnumGateState gateState = GateRenderingUpdatePacketToServer.attemptOpen(world, this, null, false);
//
//		if (gateState == EnumGateState.OK) {
//			
//			if (isLinked()) {
//				getLinkedDHD(world).activateSymbol(EnumSymbol.BRB.id);
//			}
//		}
//		
//		else {
//			targetSymbol = null;
//			targetSymbolDialing = false;
//			
//			markStargateIdle();
//			markDirty();
//			
//			OCHelper.sendSignalToReachable(node, null, "stargate_failed", new Object[] {});
//		}
//		
//		return new Object[] {gateState.toString()};
//	}
//	
//	@Callback(doc = "function() -- Tries to close the gate")
//	public Object[] disengageGate(Context context, Arguments args) {
//		if (stargateState == EnumStargateState.ENGAGED) {
//			if (isInitiating()) {
//				GateRenderingUpdatePacketToServer.closeGatePacket(this, false);
//				return new Object[] {};
//			}
//			
//			else
//				return new Object[] {null, "stargate_failure_wrong_end", "Unable to close the gate on this end"};
//		}
//		
//		else {
//			return new Object[] {null, "stargate_failure_not_open", "The gate is closed"};
//		}
//	}
//	
//	private Node node = Network.newNode(this, Visibility.Network).withComponent("stargate", Visibility.Network).create();
//
//	@Override
//	public Node node() {
//		return node;
//	}
//
//	@Override
//	public void onConnect(Node node) {}
//
//	@Override
//	public void onDisconnect(Node node) {}
//
//	@Override
//	public void onMessage(Message message) {}
//
//	@Override
//	public void onLoad() {
//		Network.joinOrCreateNetwork(this);
//		
//		super.onLoad();
//	}
//
//	@Override
//	public void onChunkUnload() {
//		if (node != null)
//			node.remove();
//		
//		super.onChunkUnload();
//	}
//
//	@Override
//	public void invalidate() {
//		if (node != null)
//			node.remove();
//		
//		super.invalidate();
//	}
//}
