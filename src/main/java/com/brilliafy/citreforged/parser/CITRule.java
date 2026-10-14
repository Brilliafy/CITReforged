package com.brilliafy.citreforged.parser;

import com.brilliafy.citreforged.CITReforged;
import com.brilliafy.citreforged.util.CITPathResolver;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.regex.Pattern;

public class CITRule {
    public static final CITRule NO_MATCH = new CITRule();

    private final List<Item> targetItems = new ArrayList<>();
    private final Map<List<String>, Pattern> nbtConditions = new HashMap<>();
    private final List<Range> damageConditions = new ArrayList<>();
    private int damageMaskCondition = 0;
    private final List<Pattern> enchantmentConditions = new ArrayList<>();
    private final List<Range> enchantmentLevelConditions = new ArrayList<>();
    private final List<Range> stackSizeConditions = new ArrayList<>();
    private ResourceLocation texture;
    private ResourceLocation model;
    private ResourceLocation bowPulling0;
    private ResourceLocation bowPulling1;
    private ResourceLocation bowPulling2;
    private ResourceLocation fishingRodCast;
    private int weight = 0;
    private IBakedModel bakedModel;
    private IBakedModel bowPulling0Model;
    private IBakedModel bowPulling1Model;
    private IBakedModel bowPulling2Model;
    private IBakedModel fishingRodCastModel;
    private final String path;

    private CITRule() {
        this.path = "";
    }

    public CITRule(Properties properties, String path, boolean defaultPngExists) {
        this.path = path;

        String type = properties.getProperty("type");
        if (type == null) type = "item";
        if (!type.equals("item")) {
            CITReforged.LOGGER.warn("CIT Reforged cannot currently load property types other than item, skipping type " + type);
            return;
        }

        String items = properties.getProperty("items");
        if (items == null) items = properties.getProperty("matchItems");
        if (items != null) {
            parseItems(items, path);
        } else return;

        String texture = properties.getProperty("texture");
        if (texture == null) texture = properties.getProperty("tile");

        String bowStandby = properties.getProperty("texture.bow_standby");
        if (bowStandby == null) bowStandby = properties.getProperty("texture.standby");
        if (bowStandby == null) bowStandby = properties.getProperty("model.bow_standby");
        if (bowStandby == null) bowStandby = properties.getProperty("model.standby");

        String rodUncast = properties.getProperty("texture.fishing_rod_uncast");
        if (rodUncast == null) rodUncast = properties.getProperty("texture.uncast");

        if (texture == null) {
            if (bowStandby != null) {
                texture = bowStandby;
            } else if (rodUncast != null) {
                texture = rodUncast;
            }
        }

        String model = properties.getProperty("model");

        if (texture != null) {
            this.texture = CITPathResolver.resolve(path, texture, ".png");
        } else if (model == null && defaultPngExists) {
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            this.texture = CITPathResolver.resolve(path, fileName, ".properties");
        }

        if (model != null) {
            this.model = CITPathResolver.resolve(path, model, ".json", ".jpm");
        }

        String pull0 = properties.getProperty("texture.bow_pulling_0");
        if (pull0 == null) pull0 = properties.getProperty("texture.pulling_0");
        if (pull0 == null) pull0 = properties.getProperty("model.bow_pulling_0");
        if (pull0 == null) pull0 = properties.getProperty("model.pulling_0");
        if (pull0 != null) {
            this.bowPulling0 = CITPathResolver.resolve(path, pull0, ".png", ".json", ".jpm");
        }

        String pull1 = properties.getProperty("texture.bow_pulling_1");
        if (pull1 == null) pull1 = properties.getProperty("texture.pulling_1");
        if (pull1 == null) pull1 = properties.getProperty("model.bow_pulling_1");
        if (pull1 == null) pull1 = properties.getProperty("model.pulling_1");
        if (pull1 != null) {
            this.bowPulling1 = CITPathResolver.resolve(path, pull1, ".png", ".json", ".jpm");
        }

        String pull2 = properties.getProperty("texture.bow_pulling_2");
        if (pull2 == null) pull2 = properties.getProperty("texture.pulling_2");
        if (pull2 == null) pull2 = properties.getProperty("model.bow_pulling_2");
        if (pull2 == null) pull2 = properties.getProperty("model.pulling_2");
        if (pull2 != null) {
            this.bowPulling2 = CITPathResolver.resolve(path, pull2, ".png", ".json", ".jpm");
        }

        String rodCast = properties.getProperty("texture.fishing_rod_cast");
        if (rodCast == null) rodCast = properties.getProperty("texture.cast");
        if (rodCast == null) rodCast = properties.getProperty("model.fishing_rod_cast");
        if (rodCast == null) rodCast = properties.getProperty("model.cast");
        if (rodCast != null) {
            this.fishingRodCast = CITPathResolver.resolve(path, rodCast, ".png", ".json", ".jpm");
        }

        CITRegistry.registerTexture(this.texture);
        CITRegistry.registerTexture(this.bowPulling0);
        CITRegistry.registerTexture(this.bowPulling1);
        CITRegistry.registerTexture(this.bowPulling2);
        CITRegistry.registerTexture(this.fishingRodCast);

        String stringWeight = properties.getProperty("weight");
        if (stringWeight != null) {
            try {
                weight = Integer.parseInt(stringWeight);
            } catch (NumberFormatException ignored) {}
        }

        String damage = properties.getProperty("damage");
        if (damage != null) {
            damageConditions.addAll(Range.fromList(Arrays.asList(damage.trim().split("\\s+"))));
        }

        String damageMask = properties.getProperty("damageMask");
        if (damageMask != null) {
            int tempMask = 0;
            try {
                tempMask = Integer.parseInt(damageMask);
            } catch (NumberFormatException e) {
                CITReforged.LOGGER.warn("Properties file contains non-number value in damage mask condition");
            }
            damageMaskCondition = tempMask;
        }

        String enchantments = properties.getProperty("enchantments");
        if (enchantments == null) enchantments = properties.getProperty("enchantmentIDs");
        if (enchantments != null) {
            for (String enchantStr : enchantments.trim().split("\\s+")) {
                if (isNumeric(enchantStr)) {
                    try {
                        int numId = Integer.parseInt(enchantStr);
                        Enchantment e = Enchantment.getEnchantmentByID(numId);
                        if (e != null) {
                            ResourceLocation regName = Enchantment.REGISTRY.getNameForObject(e);
                            if (regName != null) {
                                enchantmentConditions.add(Pattern.compile(Pattern.quote(regName.toString())));
                                enchantmentConditions.add(Pattern.compile(Pattern.quote(regName.getPath())));
                                continue;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
                enchantmentConditions.add(evaluatePattern(enchantStr));
            }
        }

        String enchantmentLevels = properties.getProperty("enchantmentLevels");
        if (enchantmentLevels != null) {
            enchantmentLevelConditions.addAll(Range.fromList(Arrays.asList(enchantmentLevels.trim().split("\\s+"))));
        }

        String stackSize = properties.getProperty("stackSize");
        if (stackSize != null) {
            stackSizeConditions.addAll(Range.fromList(Arrays.asList(stackSize.trim().split("\\s+"))));
        }

        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("nbt.")) {
                String nbtPath = key.substring(4);
                nbtConditions.put(Arrays.asList(nbtPath.split("\\.")), evaluatePattern(properties.getProperty(key)));
            }
        }
    }

    public boolean matches(ItemStack stack) {
        if (!this.stackSizeConditions.isEmpty()) {
            if (!evaluateStackSizeConditions(stack.getCount())) return false;
        }

        if (!this.damageConditions.isEmpty()) {
            if (damageMaskCondition != 0) {
                if (!evaluateDamageConditions(stack.getItemDamage() & damageMaskCondition, stack.getMaxDamage())) return false;
            } else {
                if (!evaluateDamageConditions(stack.getItemDamage(), stack.getMaxDamage())) return false;
            }
        }

        if (!this.enchantmentConditions.isEmpty() || !this.enchantmentLevelConditions.isEmpty()) {
            NBTTagList enchants = stack.getItem() == net.minecraft.init.Items.ENCHANTED_BOOK
                    ? net.minecraft.item.ItemEnchantedBook.getEnchantments(stack)
                    : stack.getEnchantmentTagList();
            if (enchants == null || enchants.tagCount() == 0) return false;
            if (!evaluateEnchantmentConditions(enchants)) return false;
        }

        if (!this.nbtConditions.isEmpty()) {
            NBTTagCompound root = stack.getTagCompound();
            if (root == null) return false;
            for (Map.Entry<List<String>, Pattern> entry : this.nbtConditions.entrySet()) {
                if (!checkNBTCondition(root, entry.getKey(), entry.getValue())) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) return false;
        }
        return true;
    }

    private void parseItems(String items, String path) {
        String[] affectedItems = items.trim().split("\\s+");
        List<Pattern> patterns = new ArrayList<>();
        for (String itemStr : affectedItems) {
            if (itemStr.startsWith("iregex:")) {
                patterns.add(Pattern.compile(itemStr.substring(7), Pattern.CASE_INSENSITIVE));
            } else if (itemStr.startsWith("regex:")) {
                patterns.add(Pattern.compile(itemStr.substring(6)));
            } else if (itemStr.startsWith("ipattern:")) {
                patterns.add(Pattern.compile(itemStr.substring(9).replace("*", ".*").replace("?", "."), Pattern.CASE_INSENSITIVE));
            } else if (itemStr.startsWith("patterns:")) {
                patterns.add(Pattern.compile(itemStr.substring(8).replace("*", ".*").replace("?", ".")));
            } else {
                Item parsedItem;
                if (isNumeric(itemStr)) {
                    parsedItem = Item.getItemById(Integer.parseInt(itemStr));
                } else {
                    if (!itemStr.contains(":")) {
                        itemStr = "minecraft:" + itemStr;
                    }
                    parsedItem = Item.REGISTRY.getObject(new ResourceLocation(itemStr));
                }
                if (parsedItem != null) {
                    targetItems.add(parsedItem);
                } else {
                    CITReforged.LOGGER.warn("Unknown item '{}' in {}", itemStr, path);
                }
            }
        }
        if (!patterns.isEmpty()) {
            for (Item registeredItem : Item.REGISTRY) {
                ResourceLocation loc = Item.REGISTRY.getNameForObject(registeredItem);
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(loc.toString()).matches() || pattern.matcher(loc.getPath()).matches()) {
                        targetItems.add(registeredItem);
                    }
                }
            }
        }
    }

    private boolean evaluateDamageConditions(int damage, int maxDamage) {
        for (Range range : damageConditions) {
            if (range.compare(damage, maxDamage)) return true;
        }
        return false;
    }

    private boolean evaluateEnchantmentConditions(NBTTagList enchants) {
        if (enchants == null || enchants.tagCount() == 0) return false;

        if (enchantmentConditions.isEmpty()) {
            if (enchantmentLevelConditions.isEmpty()) return true;
            for (int i = 0; i < enchants.tagCount(); i++) {
                int lvl = enchants.getCompoundTagAt(i).getInteger("lvl");
                for (Range range : enchantmentLevelConditions) {
                    if (range.compare(lvl, 0)) return true;
                }
            }
            return false;
        }

        for (Pattern pattern : enchantmentConditions) {
            boolean match = false;
            for (int i = 0; i < enchants.tagCount(); i++) {
                NBTTagCompound enchantment = enchants.getCompoundTagAt(i);
                String id = enchantment.getString("id");
                if (id.isEmpty()) {
                    short numID = enchantment.getShort("id");
                    Enchantment e = Enchantment.getEnchantmentByID(numID);
                    if (e != null) {
                        ResourceLocation rloc = Enchantment.REGISTRY.getNameForObject(e);
                        if (rloc != null) id = rloc.toString();
                    }
                }
                int colonIdx = id.indexOf(':');
                String idPath = colonIdx != -1 ? id.substring(colonIdx + 1) : id;
                boolean idMatches = pattern.matcher(id).matches() || (!idPath.equals(id) && pattern.matcher(idPath).matches());
                if (idMatches) {
                    if (enchantmentLevelConditions.isEmpty()) {
                        match = true;
                        break;
                    } else {
                        int lvl = enchantment.getInteger("lvl");
                        for (Range range : enchantmentLevelConditions) {
                            if (range.compare(lvl, 0)) {
                                match = true;
                                break;
                            }
                        }
                        if (match) break;
                    }
                }
            }
            if (!match) return false;
        }
        return true;
    }

    private boolean evaluateStackSizeConditions(int stackSize) {
        for (Range range : stackSizeConditions) {
            if (range.compare(stackSize, 0)) return true;
        }
        return false;
    }

    private static int evaluatePercentage(int percentage, int max) { return max * percentage / 100; }

    private Pattern evaluatePattern(String value) {
        if (value.startsWith("iregex:")) {
            return Pattern.compile(value.substring(7), Pattern.CASE_INSENSITIVE);
        } else if (value.startsWith("regex:")) {
            return Pattern.compile(value.substring(6));
        } else if (value.startsWith("ipattern:")) {
            return Pattern.compile(value.substring(9).replace("*", ".*").replace("?", "."), Pattern.CASE_INSENSITIVE);
        } else if (value.startsWith("pattern:")) {
            return Pattern.compile(value.substring(8).replace("*", ".*").replace("?", "."));
        } else {
            return Pattern.compile(Pattern.quote(value));
        }
    }

    private static class Range {
        private int a;
        private int b;
        private boolean isPercentage = false;

        Range(String rangeStr) {
            this.isPercentage = rangeStr.contains("%");
            String clean = rangeStr.replace("%", "");

            String[] parts = clean.split("-", 2);
            try {
                this.a = parts[0].isEmpty() ? 0 : Integer.parseInt(parts[0]);
                if (clean.contains("-")) {
                    this.b = (parts.length < 2 || parts[1].isEmpty()) ? 65535 : Integer.parseInt(parts[1]);
                } else {
                    this.b = this.a;
                }
            } catch (NumberFormatException e) {
                CITReforged.LOGGER.error("Malformed range: {}", rangeStr);
            }
        }

        public boolean compare(int x, int max) {
            if (isPercentage) {
                return x >= evaluatePercentage(a, max) && x <= evaluatePercentage(b, max);
            }
            return x >= a && x <= b;
        }

        public static List<Range> fromList(List<String> list) {
            List<Range> ranges = new ArrayList<>(list.size());
            for (String s : list) {
                ranges.add(new Range(s));
            }
            return ranges;
        }
    }

    private boolean checkNBTCondition(NBTTagCompound root, List<String> path, Pattern pattern) {
        NBTBase current = root;
        for (int i = 0; i < path.size() - 1; i++) {
            current = getChildTag(current, path.get(i));
            if (current == null) return false;
        }
        String leafKey = path.get(path.size() - 1);
        if (leafKey.equals("*") && current instanceof NBTTagList list) {
            for (int i = 0; i < list.tagCount(); i++) {
                if (pattern.matcher(getNBTValueAsString(list.get(i))).matches()) return true;
            }
            return false;
        }
        NBTBase leaf = getChildTag(current, leafKey);
        return leaf != null && pattern.matcher(getNBTValueAsString(leaf)).matches();
    }

    private NBTBase getChildTag(NBTBase current, String key) {
        if (current instanceof NBTTagCompound comp) {
            return comp.getTag(key);
        }
        if (current instanceof NBTTagList list) {
            if (isNumeric(key)) {
                int index = Integer.parseInt(key);
                if (index >= 0 && index < list.tagCount()) {
                    return list.get(index);
                }
            }
            return null;
        }
        return null;
    }

    private String getNBTValueAsString(NBTBase tag) {
        if (tag instanceof NBTTagString nbtString) {
            return nbtString.getString();
        } else if (tag instanceof NBTPrimitive primitive) {
            if (tag instanceof NBTTagDouble || tag instanceof NBTTagFloat) {
                return String.valueOf(primitive.getDouble());
            }
            return String.valueOf(primitive.getLong());
        }
        return tag.toString();
    }

    public List<Item> getTargetItems() { return targetItems; }
    public ResourceLocation getTexture() { return texture; }
    public ResourceLocation getModel() { return model; }
    public ResourceLocation getBowPulling0() { return bowPulling0; }
    public ResourceLocation getBowPulling1() { return bowPulling1; }
    public ResourceLocation getBowPulling2() { return bowPulling2; }
    public ResourceLocation getFishingRodCast() { return fishingRodCast; }
    public int getWeight() { return weight; }
    public IBakedModel getBakedModel() { return bakedModel; }
    public void setBakedModel(IBakedModel bakedModel) { this.bakedModel = bakedModel; }
    public IBakedModel getBowPulling0Model() { return bowPulling0Model; }
    public void setBowPulling0Model(IBakedModel bowPulling0Model) { this.bowPulling0Model = bowPulling0Model; }
    public IBakedModel getBowPulling1Model() { return bowPulling1Model; }
    public void setBowPulling1Model(IBakedModel bowPulling1Model) { this.bowPulling1Model = bowPulling1Model; }
    public IBakedModel getBowPulling2Model() { return bowPulling2Model; }
    public void setBowPulling2Model(IBakedModel bowPulling2Model) { this.bowPulling2Model = bowPulling2Model; }
    public IBakedModel getFishingRodCastModel() { return fishingRodCastModel; }
    public void setFishingRodCastModel(IBakedModel fishingRodCastModel) { this.fishingRodCastModel = fishingRodCastModel; }
    public String getPath() { return path; }
}
