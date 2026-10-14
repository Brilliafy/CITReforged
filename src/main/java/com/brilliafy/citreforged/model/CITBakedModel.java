package com.brilliafy.citreforged.model;

import com.brilliafy.citreforged.parser.CITRegistry;
import com.brilliafy.citreforged.parser.CITRule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

public class CITBakedModel implements IBakedModel {
    private static final ThreadLocal<ItemCacheKey.LookupKey> LOOKUP_KEY = ThreadLocal.withInitial(ItemCacheKey.LookupKey::new);
    private final ModelCache<ItemCacheKey, CITRule> ruleCache = new ModelCache<>(500);
    private final IBakedModel baseModel;
    private final ItemOverrideList overrideList;

    CITBakedModel(IBakedModel baseModel) {
        this.baseModel = baseModel;
        this.overrideList = new ItemOverrideList(java.util.Collections.emptyList()) {
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
                ItemCacheKey.LookupKey lookup = LOOKUP_KEY.get();
                lookup.reset(stack);
                CITRule matchedRule = ruleCache.get(lookup);
                if (matchedRule == null) {
                    List<CITRule> rules = CITRegistry.getRules(stack.getItem());
                    for (int i = 0; i < rules.size(); i++) {
                        CITRule rule = rules.get(i);
                        if (rule.matches(stack)) {
                            matchedRule = rule;
                            break;
                        }
                    }
                    if (matchedRule == null) {
                        matchedRule = CITRule.NO_MATCH;
                    }
                    ruleCache.put(new ItemCacheKey(stack), matchedRule);
                }

                if (matchedRule != CITRule.NO_MATCH) {
                    IBakedModel dynamicModel = null;
                    if (stack.getItem() instanceof ItemBow && entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack) {
                        float pull = (float)(stack.getMaxItemUseDuration() - entity.getItemInUseCount()) / 20.0F;
                        if (pull >= 0.9F && matchedRule.getBowPulling2Model() != null) {
                            dynamicModel = matchedRule.getBowPulling2Model();
                        } else if (pull >= 0.65F && matchedRule.getBowPulling1Model() != null) {
                            dynamicModel = matchedRule.getBowPulling1Model();
                        } else if (matchedRule.getBowPulling0Model() != null) {
                            dynamicModel = matchedRule.getBowPulling0Model();
                        }
                    } else if (stack.getItem() instanceof ItemFishingRod && entity instanceof EntityPlayer && ((EntityPlayer) entity).fishEntity != null) {
                        boolean isHeld = entity.getHeldItemMainhand() == stack || entity.getHeldItemOffhand() == stack;
                        if (isHeld && matchedRule.getFishingRodCastModel() != null) {
                            dynamicModel = matchedRule.getFishingRodCastModel();
                        }
                    }

                    if (dynamicModel == null) {
                        dynamicModel = matchedRule.getBakedModel();
                    }

                    if (dynamicModel != null) {
                        IBakedModel overridden = dynamicModel.getOverrides().handleItemState(dynamicModel, stack, world, entity);
                        if (overridden == dynamicModel) {
                            return dynamicModel;
                        }
                        return new PerspectiveModelWrapper(overridden);
                    }
                }
                return baseModel.getOverrides().handleItemState(originalModel, stack, world, entity);
            }
        };
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return baseModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return baseModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return baseModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return baseModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return baseModel.getParticleTexture();
    }

    @Override
    @SuppressWarnings("deprecation")
    public net.minecraft.client.renderer.block.model.ItemCameraTransforms getItemCameraTransforms() {
        return baseModel.getItemCameraTransforms();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        Pair<? extends IBakedModel, Matrix4f> perspective = baseModel.handlePerspective(cameraTransformType);
        return Pair.of(this, perspective.getRight());
    }

    @Override
    public ItemOverrideList getOverrides() {
        return this.overrideList;
    }
}
