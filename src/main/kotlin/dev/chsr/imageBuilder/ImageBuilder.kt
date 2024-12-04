package dev.chsr.imageBuilder

import dev.chsr.imageBuilder.command.BuildCommand
import org.bukkit.plugin.java.JavaPlugin

class ImageBuilder : JavaPlugin() {

    override fun onEnable() {
        getCommand("build")?.setExecutor(BuildCommand())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}