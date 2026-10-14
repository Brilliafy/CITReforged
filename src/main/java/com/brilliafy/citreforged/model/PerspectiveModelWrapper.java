package com.brilliafy.citreforged.model;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;

public class PerspectiveModelWrapper extends BakedModelWrapper<IBakedModel> {
    public PerspectiveModelWrapper(IBakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
            ItemCameraTransforms.TransformType cameraTransformType) {
        Pair<? extends IBakedModel, Matrix4f> perspective = originalModel.handlePerspective(cameraTransformType);
        return Pair.of(this, perspective.getRight());
    }
}
