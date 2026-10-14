package com.brilliafy.citreforged.model;

import com.brilliafy.citreforged.*;
import com.brilliafy.citreforged.parser.CITParser;
import com.brilliafy.citreforged.parser.CITRegistry;
import com.brilliafy.citreforged.parser.CITRule;
import com.brilliafy.citreforged.proxy.ClientProxy;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = net.minecraftforge.fml.relauncher.Side.CLIENT)
public class CITModelBakeListener {

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        CITParser.load(ClientProxy.defaultPacks);
        for (CITRule rule : CITRegistry.getAllRules()) {
            if (rule.getModel() != null) {
                try {
                    Collection<ResourceLocation> textures = ModelLoaderRegistry.getModel(rule.getModel()).getTextures();
                    for (ResourceLocation texture : textures) {
                        event.getMap().registerSprite(texture);
                        CITRegistry.registerTexture(texture);
                    }
                } catch (Exception e) {
                    CITReforged.LOGGER.warn("CIT model missing or malformed, skipping textures for: {}", rule.getModel());
                }
            }
            if (rule.getTexture() != null) {
                event.getMap().registerSprite(rule.getTexture());
            }
            if (rule.getBowPulling0() != null) {
                event.getMap().registerSprite(rule.getBowPulling0());
            }
            if (rule.getBowPulling1() != null) {
                event.getMap().registerSprite(rule.getBowPulling1());
            }
            if (rule.getBowPulling2() != null) {
                event.getMap().registerSprite(rule.getBowPulling2());
            }
            if (rule.getFishingRodCast() != null) {
                event.getMap().registerSprite(rule.getFishingRodCast());
            }
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        for (Item item : CITRegistry.getTargetedItems()) {
            if (item == null) continue;
            ModelResourceLocation loc = new ModelResourceLocation(item.getRegistryName(), "inventory");
            IBakedModel vanillaModel = event.getModelRegistry().getObject(loc);
            if (vanillaModel == null) continue;
            event.getModelRegistry().putObject(loc, new CITBakedModel(vanillaModel));
        }

        Map<String, IBakedModel> bakedCache = new HashMap<>();

        for (Item item : CITRegistry.getTargetedItems()) {
            if (item == null) continue;
            boolean isHandheld = item instanceof ItemSword ||
                    item instanceof ItemTool ||
                    item instanceof ItemHoe ||
                    item instanceof ItemBow ||
                    item instanceof ItemShears ||
                    item instanceof ItemFishingRod;
            ResourceLocation baseModelLoc = new ResourceLocation(isHandheld ? "item/handheld" : "item/generated");
            for (CITRule rule : CITRegistry.getRules(item)) {
                if (rule.getBakedModel() != null) {
                    continue;
                }
                try {
                    String cacheKey = (rule.getModel() != null ? rule.getModel().toString() : baseModelLoc.toString())
                            + "#" + (rule.getTexture() != null ? rule.getTexture().toString() : "");

                    IBakedModel bakedModel = bakedCache.get(cacheKey);
                    if (bakedModel == null) {
                        if (rule.getModel() != null) {
                            IModel baseModel = ModelLoaderRegistry.getModel(rule.getModel());
                            if (rule.getTexture() != null) {
                                baseModel = baseModel.retexture(ImmutableMap.of("layer0", rule.getTexture().toString()));
                            }
                            bakedModel = baseModel.bake(baseModel.getDefaultState(), DefaultVertexFormats.ITEM,
                                    ModelLoader.defaultTextureGetter());
                        } else if (rule.getTexture() != null) {
                            IModel baseModel = ModelLoaderRegistry.getModel(baseModelLoc);
                            IModel retexturedModel = baseModel.retexture(ImmutableMap.of("layer0", rule.getTexture().toString()));
                            bakedModel = retexturedModel.bake(retexturedModel.getDefaultState(), DefaultVertexFormats.ITEM,
                                    ModelLoader.defaultTextureGetter());
                        }
                        if (bakedModel != null) {
                            bakedModel = wrapPerspective(bakedModel);
                            bakedCache.put(cacheKey, bakedModel);
                        }
                    }
                    rule.setBakedModel(bakedModel);

                    if (rule.getBowPulling0() != null) {
                        rule.setBowPulling0Model(wrapPerspective(bakeSubModel(rule.getBowPulling0(), baseModelLoc, bakedCache)));
                    }
                    if (rule.getBowPulling1() != null) {
                        rule.setBowPulling1Model(wrapPerspective(bakeSubModel(rule.getBowPulling1(), baseModelLoc, bakedCache)));
                    }
                    if (rule.getBowPulling2() != null) {
                        rule.setBowPulling2Model(wrapPerspective(bakeSubModel(rule.getBowPulling2(), baseModelLoc, bakedCache)));
                    }
                    if (rule.getFishingRodCast() != null) {
                        rule.setFishingRodCastModel(wrapPerspective(bakeSubModel(rule.getFishingRodCast(), baseModelLoc, bakedCache)));
                    }
                } catch (Exception e) {
                    CITReforged.LOGGER.warn("Failed to bake CIT model (file likely missing): {}", rule.getModel());
                }
            }
        }
    }

    private static IBakedModel wrapPerspective(IBakedModel model) {
        if (model == null) return null;
        if (model instanceof PerspectiveModelWrapper) return model;
        return new PerspectiveModelWrapper(model);
    }

    private static IBakedModel bakeSubModel(ResourceLocation subLoc, ResourceLocation fallbackBase, Map<String, IBakedModel> cache) {
        if (subLoc == null) return null;
        String cacheKey = "sub#" + fallbackBase.toString() + "#" + subLoc.toString();
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        IBakedModel baked = null;
        try {
            if (ModelLoaderRegistry.loaded(subLoc)) {
                IModel m = ModelLoaderRegistry.getModel(subLoc);
                baked = m.bake(m.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
            } else {
                IModel baseModel = ModelLoaderRegistry.getModel(fallbackBase);
                IModel retextured = baseModel.retexture(ImmutableMap.of("layer0", subLoc.toString()));
                baked = retextured.bake(retextured.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
            }
        } catch (Exception e) {
            CITReforged.LOGGER.warn("Failed to bake CIT sub-model for: {}", subLoc, e);
        }
        cache.put(cacheKey, baked);
        return baked;
    }
}