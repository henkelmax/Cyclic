package com.lothrazar.cyclic.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UtilSound {

  public static void playSound(Entity entityIn, SoundEvent soundIn) {
    playSound(entityIn, entityIn.getPosition(), soundIn);
  }

  public static void playSound(World world, BlockPos pos, SoundEvent soundIn) {
    world.playSound(pos.getX(), pos.getY(), pos.getZ(),
        soundIn, SoundCategory.BLOCKS, 0.5F, 0.5F, false);
  }

  public static void playSound(Entity entityIn, BlockPos position, SoundEvent soundIn) {
    if (entityIn != null) {
      entityIn.playSound(soundIn, 1.0F, 1.0F);
    }
  }
  //should work but its just silent
  //  public static void playSoundFromServer(ServerWorld world, @Nullable PlayerEntity playerIn, BlockPos pos, SoundEvent soundIn) {
  //    world.playSound(playerIn, pos, soundIn, SoundCategory.PLAYERS, 1.0f, 1.0f);
  //  }

  public static void playSoundFromServer(ServerPlayerEntity entityIn, SoundEvent soundIn) {
    if (soundIn != null)
      entityIn.connection.sendPacket(new SPlaySoundEffectPacket(
          soundIn,
          SoundCategory.BLOCKS,
          entityIn.lastTickPosX, entityIn.lastTickPosY, entityIn.lastTickPosZ,
          1.0f, 1.0f));
  }
}
