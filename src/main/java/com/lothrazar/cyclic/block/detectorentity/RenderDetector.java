package com.lothrazar.cyclic.block.detectorentity;

import java.awt.Color;
import com.lothrazar.cyclic.util.UtilRender;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderDetector extends TileEntityRenderer<TileDetector> {

  public RenderDetector(TileEntityRendererDispatcher d) {
    super(d);
  }

  @Override
  public void render(TileDetector te, float v, MatrixStack matrixStack,
      IRenderTypeBuffer iRenderTypeBuffer, int partialTicks, int destroyStage) {
    if (te.getField(TileDetector.Fields.RENDER.ordinal()) == 1)
      UtilRender.renderOutline(te.getPos(), te.getShape(), matrixStack, 0.6F, Color.GREEN);
  }
}
