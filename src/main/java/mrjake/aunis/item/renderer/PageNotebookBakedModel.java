package mrjake.aunis.item.renderer;

import java.util.List;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.util.EnumFacing;

public class PageNotebookBakedModel implements IBakedModel {

	private IBakedModel defaultModel;
	
	/**
	 * Static fields black magic fuckery
	 * 
	 * In {@link TileEntityItemStackRenderer} there is no way of knowing
	 * in which hand the item is being rendered. There's no even {@link IBakedModel} instance...
	 * 
	 * The last known transform can be read from here. This is set by {@link IBakedModel#handlePerspective(TransformType)}
	 * called from {@link RenderItem#renderItemModel}
	 * 
	 */
	public static TransformType lastTransform;
	
	public PageNotebookBakedModel(IBakedModel defaultModel) {
		this.defaultModel = defaultModel;
	}
	
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		return this.defaultModel.getQuads(state, side, rand);
	}

	@Override
	public boolean isAmbientOcclusion() {
		return this.defaultModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return this.defaultModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return true;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return this.defaultModel.getParticleTexture();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return this.defaultModel.getOverrides();
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
		if (cameraTransformType == TransformType.FIRST_PERSON_LEFT_HAND || cameraTransformType == TransformType.FIRST_PERSON_RIGHT_HAND || cameraTransformType == TransformType.FIXED) {
			lastTransform = cameraTransformType;
			
			return IBakedModel.super.handlePerspective(cameraTransformType);
		}
		
		return defaultModel.handlePerspective(cameraTransformType);
	}
}
