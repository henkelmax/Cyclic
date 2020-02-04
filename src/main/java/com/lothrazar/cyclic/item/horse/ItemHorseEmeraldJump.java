/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (C) 2014-2018 Sam Bassett (aka Lothrazar)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.lothrazar.cyclic.item.horse;

import com.lothrazar.cyclic.base.ItemBase;
import com.lothrazar.cyclic.util.UtilEntity;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.util.ActionResultType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemHorseEmeraldJump extends ItemBase {

  private static final int JUMP_MAX = 10;
  private static final double JUMP_AMT = 0.008;

  public ItemHorseEmeraldJump(Properties prop) {
    super(prop);
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void onEntityInteractEvent(EntityInteract event) {
    if (event.getItemStack().getItem() == this
        && event.getTarget() instanceof HorseEntity) {
      // lets go 
      HorseEntity ahorse = (HorseEntity) event.getTarget();
      IAttributeInstance attr = UtilEntity.getAttributeJump(ahorse);
      double current = attr.getValue();
      double newSpeed = current + JUMP_AMT;
      if (UtilEntity.getJumpTranslated(newSpeed) < JUMP_MAX) {
        attr.setBaseValue(newSpeed);
        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);
        event.getItemStack().shrink(1);
        UtilEntity.eatingHorse(ahorse);
      }
    }
  }
}
