package io.gitlab.shdima.throwables

import org.bstats.bukkit.Metrics
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.ThrownExpBottle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SpawnEggMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class ThrowableSpawnEggs : JavaPlugin(), Listener {

    private val key = NamespacedKey(this, "throwable")

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
        if (!event.action.isRightClick) return
        val item = event.item ?: return

        val type = item.type

        if (!type.toString().endsWith("_SPAWN_EGG")) return

        event.player.launchProjectile<ThrownExpBottle>(ThrownExpBottle::class.java, event.player.location.direction) {
            it.item = ItemStack(type).apply {
                editMeta(SpawnEggMeta::class.java) {
                    it.persistentDataContainer[key, PersistentDataType.BOOLEAN] = true
                }
            }
        }

        event.isCancelled = true
    }

    @EventHandler
    private fun onEggLand(event: ProjectileHitEvent) {
        val entity = event.entity as? ThrownExpBottle ?: return

        val item = entity.item
        val meta = item.itemMeta as? SpawnEggMeta ?: return
        val data = meta.persistentDataContainer
        val isThrowable = data[key, PersistentDataType.BOOLEAN] ?: return
        if (!isThrowable) return

        val type = EntityType.entries.firstOrNull {
            item.type.name == "${it.name}_SPAWN_EGG"
        } ?: return

        entity.world.spawnEntity(
            entity.location,
            type,
            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
        )
    }
}
