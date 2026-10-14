package com.brilliafy.citreforged.parser;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class CITRegistry {
    private static final Map<Item, List<CITRule>> RULES = new HashMap<>();
    private static final Set<ResourceLocation> REGISTERED_TEXTURES = new HashSet<>();
    private static final Set<CITRule> ALL_RULES = new LinkedHashSet<>();
    private static final Comparator<CITRule> RULE_COMPARATOR = Comparator.comparingInt(CITRule::getWeight).reversed().thenComparing(CITRule::getPath);

    private CITRegistry() {}

    public static void register(CITRule rule) {
        ALL_RULES.add(rule);
        for (Item item : rule.getTargetItems()) {
            RULES.computeIfAbsent(item, k -> new ArrayList<>()).add(rule);
        }
    }

    public static void registerTexture(ResourceLocation texture) {
        if (texture == null) return;
        REGISTERED_TEXTURES.add(texture);
        String path = texture.getPath();
        String namespace = texture.getNamespace();

        String rawPath = path;
        if (rawPath.startsWith("textures/")) {
            rawPath = rawPath.substring(9);
        } else if (rawPath.startsWith("models/")) {
            rawPath = rawPath.substring(7);
        }

        String noExt = rawPath;
        if (noExt.endsWith(".png")) {
            noExt = noExt.substring(0, noExt.length() - 4);
        } else if (noExt.endsWith(".json")) {
            noExt = noExt.substring(0, noExt.length() - 5);
        }

        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, noExt));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, noExt + ".png"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, noExt + ".png.mcmeta"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, noExt + ".mcmeta"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, noExt + ".json"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, noExt + ".json.mcmeta"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, "textures/" + noExt));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, "textures/" + noExt + ".png"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, "textures/" + noExt + ".png.mcmeta"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, "textures/" + noExt + ".mcmeta"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, "models/" + noExt));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, "models/" + noExt + ".json"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, "models/" + noExt + ".json.mcmeta"));

        // Emissive OptiFine/CTM textures (_e)
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, noExt + "_e"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, noExt + "_e.png"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, noExt + "_e.png.mcmeta"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, "textures/" + noExt + "_e"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, "textures/" + noExt + "_e.png"));
        REGISTERED_TEXTURES.add(new ResourceLocation(namespace, "textures/" + noExt + "_e.png.mcmeta"));
    }

    public static boolean isRegisteredTexture(ResourceLocation texture) {
        if (texture == null) return false;
        return REGISTERED_TEXTURES.contains(texture);
    }

    public static void sortRules() {
        for (List<CITRule> ruleList : RULES.values()) {
            ruleList.sort(RULE_COMPARATOR);
        }
    }

    public static List<CITRule> getRules(Item item) {
        List<CITRule> list = RULES.get(item);
        return list != null ? list : Collections.emptyList();
    }

    public static Set<Item> getTargetedItems() {
        return Collections.unmodifiableSet(RULES.keySet());
    }

    public static Set<CITRule> getAllRules() {
        return ALL_RULES;
    }

    public static void clear() {
        RULES.clear();
        REGISTERED_TEXTURES.clear();
        ALL_RULES.clear();
    }
}
