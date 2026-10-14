package com.brilliafy.citreforged;

import com.brilliafy.citreforged.parser.CITRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CITProxyPack implements IResourcePack {
    private final List<IResourcePack> defaultPacks;
    private final Map<ResourceLocation, IResourcePack> packCache = new HashMap<>();

    public CITProxyPack(List<IResourcePack> defaultPacks) {
        this.defaultPacks = defaultPacks;
    }

    public void clearCache() {
        packCache.clear();
    }

    private boolean isCITResource(ResourceLocation location) {
        if (location == null) return false;
        String path = location.getPath();
        if (path.startsWith("models/optifine/cit/") ||
                path.startsWith("models/mcpatcher/cit/") ||
                path.startsWith("textures/optifine/cit/") ||
                path.startsWith("textures/mcpatcher/cit/") ||
                path.startsWith("optifine/cit/") ||
                path.startsWith("mcpatcher/cit/")) {
            return true;
        }
        if (CITRegistry.isRegisteredTexture(location)) {
            return true;
        }
        if (path.endsWith(".mcmeta")) {
            String base = path.substring(0, path.length() - 7);
            if (CITRegistry.isRegisteredTexture(new ResourceLocation(location.getNamespace(), base))) {
                return true;
            }
        }
        return false;
    }

    private ResourceLocation cleanPath(ResourceLocation loc) {
        String path = loc.getPath();
        if (path.startsWith("models/")) {
            return new ResourceLocation(loc.getNamespace(), path.substring(7));
        } else if (path.startsWith("textures/")) {
            return new ResourceLocation(loc.getNamespace(), path.substring(9));
        }
        return loc;
    }

    @Nullable
    private IResourcePack findHandlingPack(ResourceLocation location) {
        if (packCache.containsKey(location)) {
            return packCache.get(location);
        }
        ResourceLocation clean = cleanPath(location);
        IResourcePack found = null;
        try {
            List<ResourcePackRepository.Entry> entries = Minecraft.getMinecraft().getResourcePackRepository().getRepositoryEntries();
            for (int i = entries.size() - 1; i >= 0; i--) {
                IResourcePack pack = entries.get(i).getResourcePack();
                if (pack.resourceExists(clean)) {
                    found = pack;
                    break;
                }
                if (!clean.equals(location) && pack.resourceExists(location)) {
                    found = pack;
                    break;
                }
            }
        } catch (Exception ignored) {}
        packCache.put(location, found);
        return found;
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        if (isCITResource(location)) {
            IResourcePack handlingPack = findHandlingPack(location);
            if (handlingPack != null) {
                ResourceLocation clean = cleanPath(location);
                if (handlingPack.resourceExists(clean)) {
                    return handlingPack.getInputStream(clean);
                }
                if (!clean.equals(location) && handlingPack.resourceExists(location)) {
                    return handlingPack.getInputStream(location);
                }
            }
        }
        throw new FileNotFoundException(location.toString());
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        if (!isCITResource(location)) {
            return false;
        }
        return findHandlingPack(location) != null;
    }

    @Override
    public Set<String> getResourceDomains() {
        Set<String> domains = new HashSet<>();
        try {
            for (ResourcePackRepository.Entry entry : Minecraft.getMinecraft().getResourcePackRepository().getRepositoryEntries()) {
                domains.addAll(entry.getResourcePack().getResourceDomains());
            }
        } catch (Exception ignored) {}
        return domains;
    }

    @Nullable
    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        return null;
    }

    @Override
    public String getPackName() {
        return "CIT Proxy";
    }
}