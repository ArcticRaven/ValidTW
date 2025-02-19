package dev.arctic.validtw

import org.bukkit.plugin.java.JavaPlugin

class ValidTW : JavaPlugin() {

    override fun onEnable() {
        logger.info("ValidTW extension enabled - cyclic dependency validation active")
    }

    override fun onDisable() {
        logger.info("ValidTW extension disabled")
    }
}
