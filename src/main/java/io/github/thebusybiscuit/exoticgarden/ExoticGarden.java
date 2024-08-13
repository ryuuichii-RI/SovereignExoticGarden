package io.github.thebusybiscuit.exoticgarden;

import io.github.thebusybiscuit.exoticgarden.items.BonemealableItem;
import io.github.thebusybiscuit.exoticgarden.items.Crook;
import io.github.thebusybiscuit.exoticgarden.items.CustomFood;
import io.github.thebusybiscuit.exoticgarden.items.ExoticGardenFruit;
import io.github.thebusybiscuit.exoticgarden.items.FoodRegistry;
import io.github.thebusybiscuit.exoticgarden.items.GrassSeeds;
import io.github.thebusybiscuit.exoticgarden.items.Kitchen;
import io.github.thebusybiscuit.exoticgarden.items.MagicalEssence;
import io.github.thebusybiscuit.exoticgarden.listeners.AndroidListener;
import io.github.thebusybiscuit.exoticgarden.listeners.PlantsListener;
import io.github.thebusybiscuit.slimefun4.api.MinecraftVersion;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.food.Juice;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.GitHubBuildsUpdater;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class ExoticGarden extends JavaPlugin implements SlimefunAddon {

    public static ExoticGarden instance;
    private final File schematicsFolder = new File(getDataFolder(), "schematics");
    Gson gson = new Gson();
    private final List<Berry> berries = new ArrayList<>();
    private final List<Tree> trees = new ArrayList<>();
    private final Map<String, ItemStack> items = new HashMap<>();
    private final Set<String> treeFruits = new HashSet<>();

    protected Config cfg;

    private NestedItemGroup nestedItemGroup;
    private ItemGroup mainItemGroup;
    private ItemGroup miscItemGroup;
    private ItemGroup foodItemGroup;
    private ItemGroup drinksItemGroup;
    private ItemGroup magicalItemGroup;
    private Kitchen kitchen;

    @Override
    public void onEnable() {
        PaperLib.suggestPaper(this);

        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }

        instance = this;
        cfg = new Config(this);

        // Setting up bStats
        new Metrics(this, 4575);

        // Auto Updater
        if (cfg.getBoolean("options.auto-update") && getDescription().getVersion().startsWith("DEV - ")) {
            new GitHubBuildsUpdater(this, getFile(), "TheBusyBiscuit/ExoticGarden/master").start();
        }
        String jsonPath = "items.json";

        try {
            registerItems(jsonPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new AndroidListener(this);
        new PlantsListener(this);
    }

    private void registerItems(String path) throws IOException {
        nestedItemGroup = new NestedItemGroup(new NamespacedKey(this, "parent_category"), new CustomItemStack(PlayerHead.getItemStack(PlayerSkin.fromHashCode("847d73a91b52393f2c27e453fb89ab3d784054d414e390d58abd22512edd2b")), "&aExotic Garden"));
        mainItemGroup = new SubItemGroup(new NamespacedKey(this, "plants_and_fruits"), nestedItemGroup, new CustomItemStack(PlayerHead.getItemStack(PlayerSkin.fromHashCode("a5a5c4a0a16dabc9b1ec72fc83e23ac15d0197de61b138babca7c8a29c820")), "&aExotic Garden - Plants and Fruits"));
        miscItemGroup = new SubItemGroup(new NamespacedKey(this, "misc"), nestedItemGroup, new CustomItemStack(PlayerHead.getItemStack(PlayerSkin.fromHashCode("606be2df2122344bda479feece365ee0e9d5da276afa0e8ce8d848f373dd131")), "&aExotic Garden - Ingredients and Tools"));
        foodItemGroup = new SubItemGroup(new NamespacedKey(this, "food"), nestedItemGroup, new CustomItemStack(PlayerHead.getItemStack(PlayerSkin.fromHashCode("a14216d10714082bbe3f412423e6b19232352f4d64f9aca3913cb46318d3ed")), "&aExotic Garden - Food"));
        drinksItemGroup = new SubItemGroup(new NamespacedKey(this, "drinks"), nestedItemGroup, new CustomItemStack(PlayerHead.getItemStack(PlayerSkin.fromHashCode("2a8f1f70e85825607d28edce1a2ad4506e732b4a5345a5ea6e807c4b313e88")), "&aExotic Garden - Drinks"));
        magicalItemGroup = new SubItemGroup(new NamespacedKey(this, "magical_crops"), nestedItemGroup, new CustomItemStack(Material.BLAZE_POWDER, "&5Exotic Garden - Magical Plants"));

        kitchen = new Kitchen(this, miscItemGroup);
        kitchen.register(this);
        Research kitchenResearch = new Research(new NamespacedKey(this, "kitchen"), 600, "Kitchen", 0);
        kitchenResearch.addItems(kitchen);
        kitchenResearch.register();

        String jsonContent = readJSON("/src/resources/items.json");
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(jsonContent, JsonArray.class);

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();

            // check types and run constructors
            String itemType = jsonObject.get("item_type").getAsString();
            switch (itemType) {
                case "tree":
                    String treeName = jsonObject.get("name").getAsString();
                    String treeTexture = jsonObject.get("texture").getAsString();
                    String treeColor = jsonObject.get("color").getAsString();
                    Color treePotionColor = calculatePColorFromJson(jsonObject);
                    String treeJuice = jsonObject.get("juice").getAsString();
                    boolean treePie = jsonObject.get("pie").getAsBoolean();
                    Material[] treeMaterials = calculateItemMaterialsFromJson(jsonObject);

                    registerTree(treeName, treeTexture, treeColor, treePotionColor, treeJuice, treePie, treeMaterials);
                    break;

                case "berry":
                    String berryName = jsonObject.get("name").getAsString();
                    String berryColorJson = jsonObject.get("color").getAsString();
                    ChatColor berryColor = ChatColor.valueOf(berryColorJson);
                    String berryPlantTypeString = jsonObject.get("plant_type").getAsString();
                    PlantType berryPlantType = PlantType.valueOf(berryPlantTypeString);
                    String berryTexture = jsonObject.get("texture").getAsString();
                    Color berryPotionColor = calculatePColorFromJson(jsonObject);

                    registerBerry(berryName, berryColor, berryPotionColor, berryPlantType, berryTexture);
                    break;

                case "misc":
                    String miscId = jsonObject.get("id").getAsString();
                    String miscName = jsonObject.get("name").getAsString();
                    String miscTexture = jsonObject.get("texture").getAsString();
                    RecipeType miscRecipeType = getRecipeTypeFromJson(jsonObject);
                    ItemStack[] miscRecipe = calculateRecipeFromJson(jsonObject);
                    ItemStack miscRecipeOutput = getItemFromJson(jsonObject);

                    registerMisc(miscId, miscName, miscTexture, miscRecipeType, miscRecipe, miscRecipeOutput);
                    break;

                case "plant":
                    String plantName = jsonObject.get("name").getAsString();
                    String plantColorJson = jsonObject.get("color").getAsString();
                    ChatColor plantColor = ChatColor.valueOf(plantColorJson);
                    String plantPlantTypeString = jsonObject.get("plant_type").getAsString();
                    PlantType plantPlantType = PlantType.valueOf(plantPlantTypeString);
                    String plantTexture = jsonObject.get("texture").getAsString();

                    registerPlant(plantName, plantColor, plantPlantType, plantTexture);
                    break;

                case "magical_plant":
                    String magicalPlantName = jsonObject.get("name").getAsString();
                    ItemStack magicalPlantItem = getItemFromJson(jsonObject);
                    String magicalPlantTexture = jsonObject.get("texture").getAsString();
                    ItemStack[] magicalPlantRecipe = calculateRecipeFromJson(jsonObject);

                    registerMagicalPlant(magicalPlantName, magicalPlantItem, magicalPlantTexture, magicalPlantRecipe);
                    break;
            }

        }

        new Crook(miscItemGroup, new SlimefunItemStack("CROOK", new CustomItemStack(Material.WOODEN_HOE, "&rCrook", "", "&7+ &b25% &7Sapling Drop Rate")), RecipeType.ENHANCED_CRAFTING_TABLE,
                new ItemStack[] {new ItemStack(Material.STICK), new ItemStack(Material.STICK), null, null, new ItemStack(Material.STICK), null, null, new ItemStack(Material.STICK), null})
                .register(this);

        SlimefunItemStack grassSeeds = new SlimefunItemStack("GRASS_SEEDS", Material.PUMPKIN_SEEDS, "&rGrass Seeds", "", "&7&oCan be planted on Dirt");
        new GrassSeeds(mainItemGroup, grassSeeds, ExoticGardenRecipeTypes.BREAKING_GRASS, new ItemStack[] {null, null, null, null, new ItemStack(Material.GRASS), null, null, null, null})
                .register(this);
        // @formatter:on

        items.put("WHEAT_SEEDS", new ItemStack(Material.WHEAT_SEEDS));
        items.put("PUMPKIN_SEEDS", new ItemStack(Material.PUMPKIN_SEEDS));
        items.put("MELON_SEEDS", new ItemStack(Material.MELON_SEEDS));

        for (Material sapling : Tag.SAPLINGS.getValues()) {
            items.put(sapling.name(), new ItemStack(sapling));
        }

        items.put("GRASS_SEEDS", grassSeeds);

        Iterator<String> iterator = items.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            cfg.setDefaultValue("grass-drops." + key, true);

            if (!cfg.getBoolean("grass-drops." + key)) {
                iterator.remove();
            }
        }

        cfg.save();

        for (Tree tree : ExoticGarden.getTrees()) {
            treeFruits.add(tree.getFruitID());
        }


    }


    @Override
    public void onDisable() {
        instance = null;
    }

    private void registerTree(String name, String texture, String color, Color pcolor, String juice, boolean pie, Material... soil) {
        String id = name.toUpperCase(Locale.ROOT).replace(' ', '_');
        Tree tree = new Tree(id, texture, soil);
        trees.add(tree);

        SlimefunItemStack sapling = new SlimefunItemStack(id + "_SAPLING", Material.OAK_SAPLING, color + name + " Sapling");

        items.put(id + "_SAPLING", sapling);

        new BonemealableItem(mainItemGroup, sapling, ExoticGardenRecipeTypes.BREAKING_GRASS, new ItemStack[]{null, null, null, null, new ItemStack(Material.GRASS), null, null, null, null}).register(this);

        new ExoticGardenFruit(mainItemGroup, new SlimefunItemStack(id, texture, color + name), ExoticGardenRecipeTypes.HARVEST_TREE, true, new ItemStack[]{null, null, null, null, getItem(id + "_SAPLING"), null, null, null, null}).register(this);

        if (pcolor != null) {
            new Juice(drinksItemGroup, new SlimefunItemStack(juice.toUpperCase().replace(" ", "_"), new CustomPotion(color + juice, pcolor, new PotionEffect(PotionEffectType.SATURATION, 6, 0), "", "&7&oRestores &b&o" + "3.0" + " &7&oHunger")), RecipeType.JUICER, new ItemStack[]{getItem(id), null, null, null, null, null, null, null, null}).register(this);
        }

        if (pie) {
            new CustomFood(foodItemGroup, new SlimefunItemStack(id + "_PIE", "3418c6b0a29fc1fe791c89774d828ff63d2a9fa6c83373ef3aa47bf3eb79", color + name + " Pie", "", "&7&oRestores &b&o" + "6.5" + " &7&oHunger"), new ItemStack[]{getItem(id), new ItemStack(Material.EGG), new ItemStack(Material.SUGAR), new ItemStack(Material.MILK_BUCKET), SlimefunItems.WHEAT_FLOUR, null, null, null, null}, 13).register(this);
        }

        if (!new File(schematicsFolder, id + "_TREE.schematic").exists()) {
            saveSchematic(id + "_TREE");
        }
    }

    private void saveSchematic(@Nonnull String id) {
        try (InputStream input = getClass().getResourceAsStream("/schematics/" + id + ".schematic")) {
            try (FileOutputStream output = new FileOutputStream(new File(schematicsFolder, id + ".schematic"))) {
                byte[] buffer = new byte[1024];
                int len;

                while ((len = input.read(buffer)) > 0) {
                    output.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, e, () -> "Failed to load file: \"" + id + ".schematic\"");
        }
    }

    public void registerMisc(String id, String name, String texture, RecipeType recipeType, ItemStack[] recipe, ItemStack recipeOutput) {
        SlimefunItemStack newMisc = new SlimefunItemStack(id, texture, name);
        new SlimefunItem(miscItemGroup, newMisc, recipeType, recipe, recipeOutput).register(this);
    }

    public void registerBerry(String name, ChatColor color, Color potionColor, PlantType type, String texture) {
        String upperCase = name.toUpperCase(Locale.ROOT);
        Berry berry = new Berry(upperCase, type, texture);
        berries.add(berry);

        SlimefunItemStack sfi = new SlimefunItemStack(upperCase + "_BUSH", Material.OAK_SAPLING, color + name + " Bush");

        items.put(upperCase + "_BUSH", sfi);

        new BonemealableItem(mainItemGroup, sfi, ExoticGardenRecipeTypes.BREAKING_GRASS, new ItemStack[]{null, null, null, null, new ItemStack(Material.GRASS), null, null, null, null}).register(this);

        new ExoticGardenFruit(mainItemGroup, new SlimefunItemStack(upperCase, texture, color + name), ExoticGardenRecipeTypes.HARVEST_BUSH, true, new ItemStack[]{null, null, null, null, getItem(upperCase + "_BUSH"), null, null, null, null}).register(this);

        new Juice(drinksItemGroup, new SlimefunItemStack(upperCase + "_JUICE", new CustomPotion(color + name + " Juice", potionColor, new PotionEffect(PotionEffectType.SATURATION, 6, 0), "", "&7&oRestores &b&o" + "3.0" + " &7&oHunger")), RecipeType.JUICER, new ItemStack[]{getItem(upperCase), null, null, null, null, null, null, null, null}).register(this);

        new Juice(drinksItemGroup, new SlimefunItemStack(upperCase + "_SMOOTHIE", new CustomPotion(color + name + " Smoothie", potionColor, new PotionEffect(PotionEffectType.SATURATION, 10, 0), "", "&7&oRestores &b&o" + "5.0" + " &7&oHunger")), RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[]{getItem(upperCase + "_JUICE"), getItem("ICE_CUBE"), null, null, null, null, null, null, null}).register(this);

        new CustomFood(foodItemGroup, new SlimefunItemStack(upperCase + "_JELLY_SANDWICH", "8c8a939093ab1cde6677faf7481f311e5f17f63d58825f0e0c174631fb0439", color + name + " Jelly Sandwich", "", "&7&oRestores &b&o" + "8.0" + " &7&oHunger"), new ItemStack[]{null, new ItemStack(Material.BREAD), null, null, getItem(upperCase + "_JUICE"), null, null, new ItemStack(Material.BREAD), null}, 16).register(this);

        new CustomFood(foodItemGroup, new SlimefunItemStack(upperCase + "_PIE", "3418c6b0a29fc1fe791c89774d828ff63d2a9fa6c83373ef3aa47bf3eb79", color + name + " Pie", "", "&7&oRestores &b&o" + "6.5" + " &7&oHunger"), new ItemStack[]{getItem(upperCase), new ItemStack(Material.EGG), new ItemStack(Material.SUGAR), new ItemStack(Material.MILK_BUCKET), SlimefunItems.WHEAT_FLOUR, null, null, null, null}, 13).register(this);
    }

    @Nullable
    private static ItemStack getItem(@Nonnull String id) {
        SlimefunItem item = SlimefunItem.getById(id);
        return item != null ? item.getItem() : null;
    }

    public void registerPlant(String name, ChatColor color, PlantType type, String texture) {
        String upperCase = name.toUpperCase(Locale.ROOT);
        String enumStyle = upperCase.replace(' ', '_');

        Berry berry = new Berry(enumStyle, type, texture);
        berries.add(berry);

        SlimefunItemStack bush = new SlimefunItemStack(enumStyle + "_BUSH", Material.OAK_SAPLING, color + name + " Plant");
        items.put(upperCase + "_BUSH", bush);

        new BonemealableItem(mainItemGroup, bush, ExoticGardenRecipeTypes.BREAKING_GRASS, new ItemStack[]{null, null, null, null, new ItemStack(Material.GRASS), null, null, null, null})
                .register(this);

        new ExoticGardenFruit(mainItemGroup, new SlimefunItemStack(enumStyle, texture, color + name), ExoticGardenRecipeTypes.HARVEST_BUSH, true, new ItemStack[]{null, null, null, null, getItem(enumStyle + "_BUSH"), null, null, null, null}).register(this);
    }

    private void registerMagicalPlant(String name, ItemStack item, String texture, ItemStack[] recipe) {
        String upperCase = name.toUpperCase(Locale.ROOT);
        String enumStyle = upperCase.replace(' ', '_');

        SlimefunItemStack essence = new SlimefunItemStack(enumStyle + "_ESSENCE", Material.BLAZE_POWDER, "&rMagical Essence", "", "&7" + name);

        Berry berry = new Berry(essence, upperCase + "_ESSENCE", PlantType.ORE_PLANT, texture);
        berries.add(berry);

        new BonemealableItem(magicalItemGroup, new SlimefunItemStack(enumStyle + "_PLANT", Material.OAK_SAPLING, "&r" + name + " Plant"), RecipeType.ENHANCED_CRAFTING_TABLE, recipe)
                .register(this);

        MagicalEssence magicalEssence = new MagicalEssence(magicalItemGroup, essence);

        magicalEssence.setRecipeOutput(item.clone());
        magicalEssence.register(this);
    }

    @Nullable
    public static ItemStack harvestPlant(@Nonnull Block block) {
        SlimefunItem item = BlockStorage.check(block);

        if (item == null) {
            return null;
        }

        for (Berry berry : getBerries()) {
            if (item.getId().equalsIgnoreCase(berry.getID())) {
                switch (berry.getType()) {
                    case ORE_PLANT:
                    case DOUBLE_PLANT:
                        Block plant = block;

                        if (Tag.LEAVES.isTagged(block.getType())) {
                            block = block.getRelative(BlockFace.UP);
                        } else {
                            plant = block.getRelative(BlockFace.DOWN);
                        }

                        BlockStorage.deleteLocationInfoUnsafely(block.getLocation(), false);
                        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.OAK_LEAVES);
                        block.setType(Material.AIR);

                        plant.setType(Material.OAK_SAPLING);
                        BlockStorage.deleteLocationInfoUnsafely(plant.getLocation(), false);
                        BlockStorage.store(plant, getItem(berry.toBush()));
                        return berry.getItem().clone();
                    default:
                        block.setType(Material.OAK_SAPLING);
                        BlockStorage.deleteLocationInfoUnsafely(block.getLocation(), false);
                        BlockStorage.store(block, getItem(berry.toBush()));
                        return berry.getItem().clone();
                }
            }
        }

        return null;
    }

    public void harvestFruit(Block fruit) {
        Location loc = fruit.getLocation();
        SlimefunItem check = BlockStorage.check(loc);

        if (check == null) {
            return;
        }

        if (treeFruits.contains(check.getId())) {
            BlockStorage.clearBlockInfo(loc);
            ItemStack fruits = check.getItem().clone();
            fruit.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.OAK_LEAVES);
            fruit.getWorld().dropItemNaturally(loc, fruits);
            fruit.setType(Material.AIR);
        }
    }

    public static ExoticGarden getInstance() {
        return instance;
    }

    public File getSchematicsFolder() {
        return schematicsFolder;
    }

    public static Kitchen getKitchen() {
        return instance.kitchen;
    }

    public static List<Tree> getTrees() {
        return instance.trees;
    }

    public static List<Berry> getBerries() {
        return instance.berries;
    }

    public static Map<String, ItemStack> getGrassDrops() {
        return instance.items;
    }

    public Config getCfg() {
        return cfg;
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/TheBusyBiscuit/ExoticGarden/issues";
    }

    public String readJSON(String filepath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filepath)));
    }

    public Color calculatePColorFromJson(JsonObject jsonObject) {
        JsonArray itemPotionRgbValuesJson = jsonObject.get("potion_color").getAsJsonArray();
        List<Integer> itemPotionRgbValuesList = new ArrayList<>();

        for (JsonElement potionRgbValue : itemPotionRgbValuesJson) {
            itemPotionRgbValuesList.add(potionRgbValue.getAsInt());
        }
        Integer[] potionRgbValues = itemPotionRgbValuesList.toArray(new Integer[0]);

        return Color.fromRGB(potionRgbValues[0], potionRgbValues[1], potionRgbValues[2]);
    }

    public Material[] calculateItemMaterialsFromJson(JsonObject jsonObject) {
        JsonArray itemJsonMaterials = jsonObject.get("materials").getAsJsonArray();
        List<Material> itemMaterialsList = new ArrayList<>();

        for (JsonElement jsonMaterial : itemJsonMaterials) {
            Material itemMaterial = gson.fromJson(jsonMaterial, Material.class);
            itemMaterialsList.add(itemMaterial);
        }

        return itemMaterialsList.toArray(new Material[0]);
    }

    public RecipeType getRecipeTypeFromJson(JsonObject jsonObject) {
        return gson.fromJson(jsonObject.get("recipe_type"), RecipeType.class);
    }

    public ItemStack[] calculateRecipeFromJson(JsonObject jsonObject) {
        JsonArray itemJsonRecipe = jsonObject.get("recipe").getAsJsonArray();
        List<ItemStack> itemStacksList = new ArrayList<>();
        for (JsonElement jsonItemStack : itemJsonRecipe) {
            ItemStack itemStack = gson.fromJson(jsonItemStack, ItemStack.class);
            itemStacksList.add(itemStack);
        }
        return itemStacksList.toArray(new ItemStack[0]);
    }

    public ItemStack getItemFromJson(JsonObject jsonObject) {
        return gson.fromJson(jsonObject.get("recipe_output"), ItemStack.class);
    }

}
