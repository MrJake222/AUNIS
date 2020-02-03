package mrjake.aunis.block;

import java.util.List;

import mrjake.aunis.AunisProps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

public class StargateMilkyWayMemberBlockBakedModel implements IBakedModel {

	private final Block defaultBlock;
	private final IBakedModel defaultModel;
	
	public StargateMilkyWayMemberBlockBakedModel(Block defaultBlock, IBakedModel defaultModel) {
		this.defaultBlock = defaultBlock;
		this.defaultModel = defaultModel;
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
//		Mouse.setGrabbed(false);
		
		if (state != null) {
			IBlockState camoBlockState = ((IExtendedBlockState) state).getValue(AunisProps.CAMO_BLOCKSTATE);
		
			if (camoBlockState != null && camoBlockState.getBlock() != Blocks.AIR && camoBlockState.getBlock() != defaultBlock) {
		        IBakedModel camoBlockModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(camoBlockState);
		        
		        if (camoBlockModel != defaultModel)
		        	return camoBlockModel.getQuads(state, side, rand);
			}
		}
		
		return defaultModel.getQuads(state, side, rand);
	}

	@Override
	public boolean isAmbientOcclusion() {
		return defaultModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return defaultModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return defaultModel.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return defaultModel.getParticleTexture();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return defaultModel.getOverrides();
	}
}
