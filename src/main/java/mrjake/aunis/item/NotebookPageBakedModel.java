package mrjake.aunis.item;

import java.util.List;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;

public class NotebookPageBakedModel implements IBakedModel {

	private IBakedModel defaultModel;
	
	public NotebookPageBakedModel(IBakedModel defaultModel) {
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
		if (cameraTransformType == TransformType.FIRST_PERSON_LEFT_HAND || cameraTransformType == TransformType.FIRST_PERSON_RIGHT_HAND) {
			return IBakedModel.super.handlePerspective(cameraTransformType);
		}
		
		return ForgeHooksClient.handlePerspective(defaultModel, cameraTransformType);
	}
}
