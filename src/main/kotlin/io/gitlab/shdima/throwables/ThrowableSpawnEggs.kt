
package io.gitlab.shdima.throwables

import com.destroystokyo.paper.event.entity.ThrownEggHatchEvent
import org.bstats.bukkit.Metrics
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Egg
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SpawnEggMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import kotlin.random.Random

private const val POWER = 1.5
private const val VARIATION = 0.0172275
private const val METRICS_PLUGIN_ID = 27754

@Suppress("SameParameterValue")
private fun triangle(min: Double, max: Double): Double {
    return min + max * (Random.nextDouble() - Random.nextDouble())
}

@Suppress("unused")
class ThrowableSpawnEggs : JavaPlugin(), Listener {

    private val key = NamespacedKey(this, "throwable")

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        initializeMetrics()
    }

    private fun initializeMetrics() {
        try {
            Metrics(this, METRICS_PLUGIN_ID)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @EventHandler
    private fun onThrowEgg(event: PlayerInteractEvent) {
        if (!event.action.isRightClick) return
        if (event.hand != EquipmentSlot.HAND) return
        val item = event.item ?: return

        if (!isSpawnEgg(item)) return

        val player = event.player
        throwSpawnEgg(player, item, event.hand!!)

        if (shouldConsumeItem(player)) {
            item.amount--
        }

        player.swingMainHand()

        event.isCancelled = true
    }

    @EventHandler
    private fun onEggLand(event: ThrownEggHatchEvent) {
        val entity = event.egg
        val item = entity.item

        if (!isThrowableEgg(item)) return

        val entityType = getEntityTypeFromEgg(item) ?: return

        spawnEggEntity(entity, entityType)

        event.numHatches = 0
        event.isHatching = false
    }

    private fun isSpawnEgg(item: ItemStack): Boolean {
        return item.type.toString().endsWith("_SPAWN_EGG")
    }

    private fun isThrowableEgg(item: ItemStack): Boolean {
        val meta = item.itemMeta as? SpawnEggMeta ?: return false
        return meta.persistentDataContainer[key, PersistentDataType.BOOLEAN] == true
    }

    private fun getEntityTypeFromEgg(item: ItemStack): EntityType? {
        return EntityType.entries.firstOrNull {
            item.type.name == "${it.name}_SPAWN_EGG"
        }
    }

    private fun throwSpawnEgg(player: Player, item: ItemStack, hand: EquipmentSlot) {
        val velocity = calculateThrowVelocity(player)
        val markedItem = markEggAsThrowable(item)

        player.launchProjectile(Egg::class.java, velocity) {
            it.item = markedItem
        }

        playThrowSound(player)
        player.swingHand(hand)
    }

    private fun calculateThrowVelocity(player: Player): Vector {
        val direction = player.location.direction

        return direction.add(Vector(
            triangle(0.0, VARIATION),
            triangle(0.0, VARIATION),
            triangle(0.0, VARIATION)
        )).multiply(POWER).add(player.velocity)
    }

    private fun markEggAsThrowable(item: ItemStack): ItemStack {
        return item.clone().apply {
            amount = 1

            editMeta(SpawnEggMeta::class.java) {
                it.persistentDataContainer[key, PersistentDataType.BOOLEAN] = true
            }
        }
    }

    private fun playThrowSound(player: Player) {
        player.playSound(
            player.location,
            Sound.ENTITY_EGG_THROW,
            SoundCategory.PLAYERS,
            0.5F,
            0.4F / (Random.nextFloat() * 0.4F + 0.8F)
        )
    }

    private fun shouldConsumeItem(player: Player): Boolean {
        return player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR
    }

    private fun spawnEggEntity(egg: Egg, type: EntityType) {
        egg.world.spawnEntity(
            egg.location,
            type,
            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
        )
    }
}