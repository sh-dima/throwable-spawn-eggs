package io.gitlab.shdima.throwables

import org.bstats.bukkit.Metrics
import org.bukkit.entity.ThrownExpBottle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class ThrowableSpawnEggs : JavaPlugin(), Listener {

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)

        try {
            Metrics(this, 27754)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @EventHandler
    private fun onThrowEgg(event: PlayerInteractEvent) {
        val item = event.item ?: return

        val type = item.type

        if (!type.toString().endsWith("_SPAWN_EGG")) return

        event.player.launchProjectile<ThrownExpBottle>(ThrownExpBottle::class.java, event.player.location.direction) {
            it.item = ItemStack(type)
        }
    }
}
