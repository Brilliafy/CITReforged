package com.brilliafy.citreforged;

import com.brilliafy.citreforged.proxy.IProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, acceptableRemoteVersions = "*")
public class CITReforged {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    @SidedProxy(
            clientSide = "com.brilliafy.citreforged.proxy.ClientProxy",
            serverSide = "com.brilliafy.citreforged.proxy.ServerProxy"
    )
    public static IProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Running {}", Tags.MOD_NAME);
        proxy.preInit(event);
    }
}
