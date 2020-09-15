package com.lothrazar.cyclic.block.solidifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.gson.JsonObject;
import com.lothrazar.cyclic.ModCyclic;
import com.lothrazar.cyclic.recipe.CyclicRecipe;
import com.lothrazar.cyclic.recipe.CyclicRecipeType;
import com.lothrazar.cyclic.util.UtilItemStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

@SuppressWarnings("rawtypes")
public class RecipeSolidifier<TileEntityBase> extends CyclicRecipe {

  public static List<RecipeSolidifier> RECIPES = new ArrayList<>();
  private ItemStack result = ItemStack.EMPTY;
  private NonNullList<Ingredient> ingredients = NonNullList.create();
  private FluidStack fluidInput;

  private RecipeSolidifier(ResourceLocation id,
      Ingredient in, Ingredient inSecond, Ingredient inThird, FluidStack fluid,
      ItemStack result) {
    super(id);
    this.result = result;
    this.fluidInput = fluid;
    ingredients.add(in);
    ingredients.add(inSecond);
    ingredients.add(inThird);
  }

  @Deprecated
  private RecipeSolidifier(ResourceLocation id,
      ItemStack in, ItemStack inSecond, ItemStack inThird, FluidStack fluid,
      ItemStack result) {
    super(id);
    this.result = result;
    this.fluidInput = fluid;
    ingredients.add(Ingredient.fromStacks(in));
    ingredients.add(Ingredient.fromStacks(inSecond));
    ingredients.add(Ingredient.fromStacks(inThird));
  }

  @Override
  public boolean matches(com.lothrazar.cyclic.base.TileEntityBase inv, World worldIn) {
    try {
      TileSolidifier tile = (TileSolidifier) inv;
      if (tile.getFluid() != null && tile.getFluid().getFluid() == this.fluidInput.getFluid()
      //          && this.fluidInput.getAmount() >= tile.getFluid().getAmount()
      ) {
        return matches(tile, 0) && matches(tile, 1) && matches(tile, 2);
      }
      else
        return false;
    }
    catch (ClassCastException e) {
      return false;
    }
  }

  public boolean matches(TileSolidifier tile, int slot) {
    ItemStack current = tile.getStackInputSlot(slot);//get slot thing
    Ingredient ing = ingredients.get(slot);
    for (ItemStack test : ing.getMatchingStacks()) {
      if (UtilItemStack.matches(current, test)) {
        return true;
      }
    }
    return false;
    //  ingredients.get(0).getMatchingStacks()
  }

  @Override
  public NonNullList<Ingredient> getIngredients() {
    return ingredients;
  }

  public ItemStack[] ingredientAt(int slot) {
    Ingredient ing = ingredients.get(slot);
    return ing.getMatchingStacks();
  }

  @Override
  public ItemStack getRecipeOutput() {
    return result.copy();
  }

  @Override
  public FluidStack getRecipeFluid() {
    return fluidInput.copy();
  }

  @Override
  public IRecipeType<?> getType() {
    return CyclicRecipeType.SOLID;
  }

  @Override
  public IRecipeSerializer<?> getSerializer() {
    return SERIALIZER;
  }

  public static final SerializeSolidifier SERIALIZER = new SerializeSolidifier();

  public static class SerializeSolidifier extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RecipeSolidifier> {

    SerializeSolidifier() {
      // This registry name is what people will specify in their json files.
      this.setRegistryName(new ResourceLocation(ModCyclic.MODID, "solidifier"));
    }

    @Override
    public RecipeSolidifier read(ResourceLocation recipeId, JsonObject json) {
      RecipeSolidifier r = null;
      try {
        Ingredient inputFirst = Ingredient.deserialize(JSONUtils.getJsonObject(json, "inputTop"));
        Ingredient inputSecond = Ingredient.deserialize(JSONUtils.getJsonObject(json, "inputMiddle"));
        Ingredient inputThird = Ingredient.deserialize(JSONUtils.getJsonObject(json, "inputBottom"));
        ItemStack resultStack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
        JsonObject mix = json.get("mix").getAsJsonObject();
        int count = mix.get("count").getAsInt();
        String fluidId = JSONUtils.getString(mix, "fluid");
        ResourceLocation resourceLocation = new ResourceLocation(fluidId);
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourceLocation);
        //is it air? which means not found
        if (fluid == FluidStack.EMPTY.getFluid()) {
          throw new IllegalArgumentException("Invalid fluid specified " + fluidId);
        }
        r = new RecipeSolidifier(recipeId, inputFirst, inputSecond,
            inputThird, new FluidStack(fluid, count), resultStack);
        addRecipe(r);
      }
      catch (Exception e) {
        ModCyclic.LOGGER.error("Error loading recipe" + recipeId, e);
      }
      return r;
    }

    @Override
    public RecipeSolidifier read(ResourceLocation recipeId, PacketBuffer buffer) {
      return new RecipeSolidifier(recipeId,
          Ingredient.read(buffer), Ingredient.read(buffer), Ingredient.read(buffer), FluidStack.readFromPacket(buffer),
          buffer.readItemStack());
    }

    @Override
    public void write(PacketBuffer buffer, RecipeSolidifier recipe) {
      Ingredient zero = (Ingredient) recipe.ingredients.get(0);
      Ingredient one = (Ingredient) recipe.ingredients.get(1);
      Ingredient two = (Ingredient) recipe.ingredients.get(2);
      zero.write(buffer);
      one.write(buffer);
      two.write(buffer);
      recipe.fluidInput.writeToPacket(buffer);
      buffer.writeItemStack(recipe.getRecipeOutput());
    }
  }
  //water 
  //      Tags.Blocks.CHESTS
  //    //
  //    //
  //    RecipeSolidifier.addRecipe("slimefill",
  //          new ItemStack(Items.SLIME_BALL),
  //        new ItemStack(Items.SLIME_BALL),
  //        new ItemStack(Items.SLIME_BALL),
  //        new FluidStack(FluidSlimeHolder.STILL.get(), 600),
  //        new ItemStack(Items.SLIME_BALL, 9));
  //    //cr l
  //    RecipeSolidifier.addRecipe("magmaglow",
  //        new ItemStack(Items.PRISMARINE),
  //        new ItemStack(Items.GLOWSTONE_DUST),
  //        new ItemStack(Items.PRISMARINE),
  //        new FluidStack(FluidMagmaHolder.STILL.get(), 100),
  //        new ItemStack(Items.PRISMARINE_CRYSTALS));
  //    //create magma cream deal
  //    RecipeSolidifier.addRecipe("slimemagmacream",
  //        new ItemStack(Items.SLIME_BALL),
  //        new ItemStack(Items.SLIME_BALL),
  //        new ItemStack(Items.SLIME_BALL),
  //        new FluidStack(FluidMagmaHolder.STILL.get(), 100),
  //        new ItemStack(Items.MAGMA_CREAM, 6));
  //    //bricks
  //    RecipeSolidifier.addRecipe("magmabricks",
  //        new ItemStack(Items.NETHER_BRICK),
  //        new ItemStack(Items.NETHER_BRICK),
  //        new ItemStack(Items.NETHER_BRICK),
  //        new FluidStack(FluidMagmaHolder.STILL.get(), 10),
  //        new ItemStack(Blocks.NETHER_BRICKS));
  //    RecipeSolidifier.addRecipe("slimebricks",
  //        new ItemStack(Items.NETHER_BRICK),
  //        new ItemStack(Items.NETHER_BRICK),
  //        new ItemStack(Items.NETHER_BRICK),
  //        new FluidStack(FluidSlimeHolder.STILL.get(), 10),
  //        new ItemStack(Blocks.NETHER_BRICKS));
  //    RecipeSolidifier.addRecipe("rednetherbricks",
  //        new ItemStack(Items.NETHER_BRICK),
  //        new ItemStack(Items.NETHER_WART),
  //        new ItemStack(Items.NETHER_BRICK),
  //        new FluidStack(FluidSlimeHolder.STILL.get(), 10),
  //        new ItemStack(Blocks.RED_NETHER_BRICKS));
  //    //
  //    //
  //    RecipeSolidifier rec = new RecipeSolidifier(
  //        new ResourceLocation(ModCyclic.MODID, "solid_leafbiomass"),
  //        new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME / 4),
  //        new ItemStack(ItemRegistry.biomass));
  //    Ingredient leafs = Ingredient.fromStacks(new ItemStack(Blocks.ACACIA_LEAVES),
  //        new ItemStack(Blocks.OAK_LEAVES));
  //    //    leafs = Ingredient.fromTag(ItemTags.LEAVES);//ded
  //    rec.ingredients.add(leafs);
  //    rec.ingredients.add(leafs);
  //    rec.ingredients.add(leafs);
  //    RECIPES.add(rec);
  //    rec = new RecipeSolidifier(
  //        new ResourceLocation(ModCyclic.MODID, "solid_biomass"),
  //        new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME / 10),
  //        new ItemStack(ItemRegistry.biomass));
  //    Ingredient fws = Ingredient.fromStacks(new ItemStack(Blocks.POPPY),
  //        new ItemStack(Blocks.DANDELION));
  //    //    fws = Ingredient.fromTag(ItemTags.SMALL_FLOWERS);//ded
  //    rec.ingredients.add(fws);
  //    rec.ingredients.add(fws);
  //    rec.ingredients.add(fws);
  //    RECIPES.add(rec);
  //    //
  //    RecipeSolidifier.addRecipe("honeyamber",
  //        new ItemStack(Items.FIRE_CHARGE),
  //        new ItemStack(Items.REDSTONE),
  //        new ItemStack(Blocks.MAGMA_BLOCK),
  //        new FluidStack(FluidHoneyHolder.STILL.get(), FluidAttributes.BUCKET_VOLUME / 2),
  //        new ItemStack(ItemRegistry.gem_amber));
  //    RecipeSolidifier.addRecipe("honeyfireamber",
  //        new ItemStack(Items.FIRE_CHARGE),
  //        new ItemStack(Blocks.REDSTONE_BLOCK),
  //        new ItemStack(Items.GOLD_INGOT),
  //        new FluidStack(FluidMagmaHolder.STILL.get(), FluidAttributes.BUCKET_VOLUME),
  //        new ItemStack(ItemRegistry.gem_amber));
  //    RecipeSolidifier.addRecipe("purp",
  //        new ItemStack(Blocks.OBSIDIAN),
  //        new ItemStack(ItemRegistry.gem_amber),
  //        new ItemStack(Items.CHORUS_FRUIT),
  //        new FluidStack(FluidSlimeHolder.STILL.get(), FluidAttributes.BUCKET_VOLUME),
  //        new ItemStack(ItemRegistry.gem_obsidian));
  //    RecipeSolidifier.addRecipe("purpflower",
  //        new ItemStack(Blocks.OBSIDIAN),
  //        new ItemStack(ItemRegistry.gem_amber),
  //        new ItemStack(Items.WITHER_ROSE),
  //        new FluidStack(FluidSlimeHolder.STILL.get(), FluidAttributes.BUCKET_VOLUME),
  //        new ItemStack(ItemRegistry.gem_obsidian));
  //    //
  //    //
  //    RecipeSolidifier.addRecipe("gravelmelt",
  //        new ItemStack(Blocks.DIRT),
  //        new ItemStack(Blocks.DIRT),
  //        new ItemStack(Blocks.DIRT),
  //        new FluidStack(FluidMagmaHolder.STILL.get(), 100),
  //        new ItemStack(Blocks.GRAVEL, 9));
  //    RecipeSolidifier.addRecipe("sandmelt",
  //        new ItemStack(Blocks.GLASS),
  //        new ItemStack(Blocks.GLASS),
  //        new ItemStack(Blocks.GLASS),
  //        new FluidStack(FluidMagmaHolder.STILL.get(), 10),
  //        new ItemStack(Blocks.SAND, 3));
  //    RecipeSolidifier.addRecipe("gunpowder",
  //        new ItemStack(Blocks.RED_SAND),
  //        new ItemStack(Items.BLAZE_POWDER),
  //        new ItemStack(Items.BONE_MEAL),
  //        new FluidStack(FluidMagmaHolder.STILL.get(), 100),
  //        new ItemStack(Items.GUNPOWDER, 2));
  //    //xp
  //    // 
  //    RecipeSolidifier.addRecipe("expglass",
  //        new ItemStack(Items.GLASS_BOTTLE),
  //        new ItemStack(Items.GLASS_BOTTLE),
  //        new ItemStack(Items.GLASS_BOTTLE),
  //        new FluidStack(FluidXpJuiceHolder.STILL.get(), ExpItemGain.FLUID_PER_EXP * 30),
  //        new ItemStack(Items.EXPERIENCE_BOTTLE, 3));
  //    RecipeSolidifier.addRecipe("expglassfood",
  //        new ItemStack(Items.SUGAR),
  //        new ItemStack(Items.SUGAR),
  //        new ItemStack(Items.SUGAR),
  //        new FluidStack(FluidXpJuiceHolder.STILL.get(), ExpItemGain.FLUID_PER_EXP * ExpItemGain.EXP_PER_FOOD),
  //        new ItemStack(ItemRegistry.experience_food));
  //    ModCyclic.LOGGER.info("Solidifier Recipes added " + RECIPES.size());
  //  }

  private static Set<String> hashes = new HashSet<>();

  private static void addRecipe(RecipeSolidifier r) {
    ResourceLocation id = r.getId();
    if (hashes.contains(id.toString())) {
      ModCyclic.LOGGER.error("Duplicate solidifier recipe id " + id.toString());
      return;
    }
    RECIPES.add(r);
    hashes.add(id.toString());
    ModCyclic.LOGGER.info("Recipe loaded " + id.toString());
  }
}
