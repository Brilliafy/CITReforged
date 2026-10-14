package com.brilliafy.citreforged.proxy;

import com.brilliafy.citreforged.CITProxyPack;
import com.brilliafy.citreforged.CITReforged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.List;

public class ClientProxy implements IProxy {
    public static List<IResourcePack> defaultPacks;
    public static CITProxyPack proxyPackInstance;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        try {
            defaultPacks = ObfuscationReflectionHelper.getPrivateValue(
                    Minecraft.class,
                    Minecraft.getMinecraft(),
                    "field_110449_ao"
            );
            proxyPackInstance = new CITProxyPack(defaultPacks);
            IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
            if (manager instanceof SimpleReloadableResourceManager) {
                ((SimpleReloadableResourceManager) manager).reloadResourcePack(proxyPackInstance);
            }
            defaultPacks.add(proxyPackInstance);
        } catch (Exception e) {
            CITReforged.LOGGER.error("Failed to inject CIT Proxy Pack into default list!", e);
        }
    }
}