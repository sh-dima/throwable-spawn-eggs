package io.gitlab.shdima.throwables

import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class ThrowableSpawnEggs : JavaPlugin() {

    override fun onEnable() {
        try {
            Metrics(this, 27754)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}
