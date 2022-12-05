package me.hasunemiku2015.signpic.TileEntityHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.fml.Logging;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class RenderEvent {
  static Minecraft mc = Minecraft.getInstance();
  protected static HashMap<BlockPos, RenderInfo> renderInfoMap = new HashMap<>();
  protected static HashMap<BlockPos, RenderInfo> renderDelMap = new HashMap<>();
  protected static HashMap<String, DynamicTexture> textureMap = new HashMap<>();
  /**
   * This event draws the image itself
   */
  @SuppressWarnings("deprecation")
  @SubscribeEvent
  public void renderImage(RenderWorldLastEvent event) {
    if (!ModController.isEnabled) return;

    int renderDistance = mc.gameSettings.renderDistanceChunks;
    Vector3d projView = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
    MatrixStack stack = event.getMatrixStack();

    HashMap<BlockPos, RenderInfo> rDM = new HashMap<>();
    renderDelMap.forEach((key, info) -> {
      renderInfoMap.remove(key, info);
      rDM.put(key, info);
    });
    rDM.forEach((key, info) -> {
      renderDelMap.remove(key, info);
    });

    List<String> deadKeys = new ArrayList<>();
    //for (RenderInfo info : renderInfoMap.values()) {
    //while(y = renderInfoMap.values().) {
    renderInfoMap.forEach((key, info) -> {
      if(info != null && textureMap.containsKey(info.texture))
      {
        assert mc.player != null;
        //Add non-sign block textures to removable list, remove from removable list if still needed
        if (Math
                .sqrt(Math.pow((info.x - mc.player.getPosX()), 2)
                        + Math.pow((info.z - mc.player.getPosZ()), 2)) > renderDistance * 16
                || !(Objects.requireNonNull(mc.world).getBlockState(new BlockPos(info.x, info.y, info.z))
                .getBlock() instanceof AbstractSignBlock)) {
          //renderInfoMap.remove(key, info);
          renderDelMap.put(key, info);
          deadKeys.add(info.texture);
        }else {
          deadKeys.remove(info.texture);

          stack.push();
          stack.translate(-projView.x, -projView.y, -projView.z);
          Matrix4f matrix = stack.getLast().getMatrix();
          GlStateManager.multMatrix(matrix);

          textureMap.get(info.texture).bindTexture();
          RenderSystem.enableBlend();
          RenderSystem.defaultBlendFunc();

          BufferBuilder buffer = Tessellator.getInstance().getBuffer();
          buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
          renderWithState(buffer, info);
          Tessellator.getInstance().draw();

          // Fixes the hand problem (So only the Tessellator got multiplied)
          matrix.invert();
          GlStateManager.multMatrix(matrix);
          stack.pop();
          RenderSystem.disableBlend();
        }
      }
    });

    for(String key: deadKeys)
      textureMap.remove(key);
  }

  /**
   *  Draws the image itself into the world, based on oritation code
   *  @param buffer: The buffer build to draw the image
   *  @param info: The data packet to be rendered
   */
  private void renderWithState(BufferBuilder buffer, RenderInfo info) {
    switch (info.state) {
      //0.45 not 0.5 to avoid culling
      default:
      case 0: {
        double wallSignOffset = info.isWallSign ? 0.45 : 0;

        // South, Vertical
        buffer.pos(-info.width / 2.0 + (info.x + 0.5) + info.offsetW, -info.height / 2.0 + (info.y + 0.5) + info.offsetH, (info.z + 0.5 + wallSignOffset)).tex(1.0F, 1.0F).endVertex();
        buffer.pos(-info.width / 2.0 + (info.x + 0.5) + info.offsetW,  info.height / 2.0 + (info.y + 0.5) + info.offsetH, (info.z + 0.5 + wallSignOffset)).tex(1.0F, 0.0F).endVertex();
        buffer.pos( info.width / 2.0 + (info.x + 0.5) + info.offsetW,  info.height / 2.0 + (info.y + 0.5) + info.offsetH, (info.z + 0.5 + wallSignOffset)).tex(0.0F, 0.0F).endVertex();
        buffer.pos( info.width / 2.0 + (info.x + 0.5) + info.offsetW, -info.height / 2.0 + (info.y + 0.5) + info.offsetH, (info.z + 0.5 + wallSignOffset)).tex(0.0F, 1.0F).endVertex();
        return;
      }

      case 1: {
        double wallSignOffset = info.isWallSign ? -0.45 : 0;

        // North, Vertical
        buffer.pos( info.width / 2.0 + (info.x + 0.5) + info.offsetW, -info.height / 2.0 + (info.y + 0.5) + info.offsetH, (info.z + 0.5 + wallSignOffset)).tex(1.0F, 1.0F).endVertex();
        buffer.pos( info.width / 2.0 + (info.x + 0.5) + info.offsetW,  info.height / 2.0 + (info.y + 0.5) + info.offsetH, (info.z + 0.5 + wallSignOffset)).tex(1.0F, 0.0F).endVertex();
        buffer.pos(-info.width / 2.0 + (info.x + 0.5) + info.offsetW,  info.height / 2.0 + (info.y + 0.5) + info.offsetH, (info.z + 0.5 + wallSignOffset)).tex(0.0F, 0.0F).endVertex();
        buffer.pos(-info.width / 2.0 + (info.x + 0.5) + info.offsetW, -info.height / 2.0 + (info.y + 0.5) + info.offsetH, (info.z + 0.5 + wallSignOffset)).tex(0.0F, 1.0F).endVertex();
        return;
      }

      case 2: {
        double wallSignOffset = info.isWallSign ? 0.45 : 0;

        // East, Vertical
        buffer.pos((info.x + 0.5 + wallSignOffset), -info.height / 2.0 + (info.y + 0.5) + info.offsetH,  info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(1.0F, 1.0F).endVertex();
        buffer.pos((info.x + 0.5 + wallSignOffset),  info.height / 2.0 + (info.y + 0.5) + info.offsetH,  info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(1.0F, 0.0F).endVertex();
        buffer.pos((info.x + 0.5 + wallSignOffset),  info.height / 2.0 + (info.y + 0.5) + info.offsetH, -info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(0.0F, 0.0F).endVertex();
        buffer.pos((info.x + 0.5 + wallSignOffset), -info.height / 2.0 + (info.y + 0.5) + info.offsetH, -info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(0.0F, 1.0F).endVertex();
        return;
      }

      case 3: {
        double wallSignOffset = info.isWallSign ? -0.45 : 0;

        // West, Vertical
        buffer.pos((info.x + 0.5 + wallSignOffset), -info.height / 2.0 + (info.y + 0.5) + info.offsetH, -info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(1.0F, 1.0F).endVertex();
        buffer.pos((info.x + 0.5 + wallSignOffset),  info.height / 2.0 + (info.y + 0.5) + info.offsetH, -info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(1.0F, 0.0F).endVertex();
        buffer.pos((info.x + 0.5 + wallSignOffset),  info.height / 2.0 + (info.y + 0.5) + info.offsetH,  info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(0.0F, 0.0F).endVertex();
        buffer.pos((info.x + 0.5 + wallSignOffset), -info.height / 2.0 + (info.y + 0.5) + info.offsetH,  info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(0.0F, 1.0F).endVertex();
        return;
      }

      //Added 0.1 to y to avoid culling.
      case 4: {
        // North, Horizontal
        buffer.pos(-info.width / 2.0 + (info.x + 0.5) + info.offsetW, (info.y + 0.5),-info.height / 2.0 + (info.z + 0.5) + info.offsetH).tex(0.0F, 0.0F).endVertex();
        buffer.pos(-info.width / 2.0 + (info.x + 0.5) + info.offsetW, (info.y + 0.5), info.height / 2.0 + (info.z + 0.5) + info.offsetH).tex(0.0F, 1.0F).endVertex();
        buffer.pos( info.width / 2.0 + (info.x + 0.5) + info.offsetW, (info.y + 0.5), info.height / 2.0 + (info.z + 0.5) + info.offsetH).tex(1.0F, 1.0F).endVertex();
        buffer.pos( info.width / 2.0 + (info.x + 0.5) + info.offsetW, (info.y + 0.5),-info.height / 2.0 + (info.z + 0.5) + info.offsetH).tex(1.0F, 0.0F).endVertex();
        return;
      }

      case 5: {
        // East, Horizontal
        buffer.pos( info.height / 2.0 + (info.x + 0.5) + info.offsetH, (info.y + 0.5), info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(1.0F, 0.0F).endVertex();
        buffer.pos( info.height / 2.0 + (info.x + 0.5) + info.offsetH, (info.y + 0.5),-info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(0.0F, 0.0F).endVertex();
        buffer.pos(-info.height / 2.0 + (info.x + 0.5) + info.offsetH, (info.y + 0.5),-info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(0.0F, 1.0F).endVertex();
        buffer.pos(-info.height / 2.0 + (info.x + 0.5) + info.offsetH, (info.y + 0.5), info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(1.0F, 1.0F).endVertex();
        return;
      }

      case 6: {
        // South, Horizontal
        buffer.pos(-info.width / 2.0 + (info.x + 0.5) + info.offsetW, (info.y + 0.5),-info.height / 2.0 + (info.z + 0.5) + info.offsetH).tex(1.0F, 1.0F).endVertex();
        buffer.pos(-info.width / 2.0 + (info.x + 0.5) + info.offsetW, (info.y + 0.5), info.height / 2.0 + (info.z + 0.5) + info.offsetH).tex(1.0F, 0.0F).endVertex();
        buffer.pos( info.width / 2.0 + (info.x + 0.5) + info.offsetW, (info.y + 0.5), info.height / 2.0 + (info.z + 0.5) + info.offsetH).tex(0.0F, 0.0F).endVertex();
        buffer.pos( info.width / 2.0 + (info.x + 0.5) + info.offsetW, (info.y + 0.5),-info.height / 2.0 + (info.z + 0.5) + info.offsetH).tex(0.0F, 1.0F).endVertex();
        return;
      }

      case 7: {
        // West, Horizontal
        buffer.pos( info.height / 2.0 + (info.x + 0.5) + info.offsetH, (info.y + 0.5), info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(0.0F, 1.0F).endVertex();
        buffer.pos( info.height / 2.0 + (info.x + 0.5) + info.offsetH, (info.y + 0.5),-info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(1.0F, 1.0F).endVertex();
        buffer.pos(-info.height / 2.0 + (info.x + 0.5) + info.offsetH, (info.y + 0.5),-info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(1.0F, 0.0F).endVertex();
        buffer.pos(-info.height / 2.0 + (info.x + 0.5) + info.offsetH, (info.y + 0.5), info.width / 2.0 + (info.z + 0.5) + info.offsetW).tex(0.0F, 0.0F).endVertex();
      }
    }
  }
}

class RenderInfo {
  int x, y, z, state;
  double height, width, offsetW, offsetH;
  String texture;
  boolean isWallSign;

  public RenderInfo(int x, int y, int z, double width, double height, double offSetW, double offSetH, int state, String texture,
      boolean isWallSign) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.width = width;
    this.height = height;
    this.offsetW= offSetW;
    this.offsetH = offSetH;
    this.state = state;
    this.texture = texture;

    this.isWallSign = isWallSign;
  }

  /**
   * Register the RenderInfo so it gets rendered.
   */
  public void register() {
    RenderEvent.renderInfoMap.put(new BlockPos(x, y, z), this);
  }
}