package dev.chsr.imageBuilder.command

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.net.URI
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

data class WoolColor(val wool: Material, val rgb: Triple<Int, Int, Int>)

fun findNearestWoolColor(r: Int, g: Int, b: Int): Material {
    val woolColors = listOf(
        WoolColor(Material.WHITE_WOOL, Triple(255, 255, 255)),
        WoolColor(Material.ORANGE_WOOL, Triple(216, 127, 51)),
        WoolColor(Material.MAGENTA_WOOL, Triple(178, 76, 216)),
        WoolColor(Material.LIGHT_BLUE_WOOL, Triple(102, 153, 216)),
        WoolColor(Material.YELLOW_WOOL, Triple(229, 229, 51)),
        WoolColor(Material.LIME_WOOL, Triple(127, 204, 25)),
        WoolColor(Material.PINK_WOOL, Triple(242, 127, 165)),
        WoolColor(Material.GRAY_WOOL, Triple(76, 76, 76)),
        WoolColor(Material.LIGHT_GRAY_WOOL, Triple(153, 153, 153)),
        WoolColor(Material.CYAN_WOOL, Triple(76, 127, 153)),
        WoolColor(Material.PURPLE_WOOL, Triple(127, 63, 178)),
        WoolColor(Material.BLUE_WOOL, Triple(51, 76, 178)),
        WoolColor(Material.BROWN_WOOL, Triple(102, 76, 51)),
        WoolColor(Material.GREEN_WOOL, Triple(102, 127, 51)),
        WoolColor(Material.RED_WOOL, Triple(153, 51, 51)),
        WoolColor(Material.BLACK_WOOL, Triple(25, 25, 25)),
        WoolColor(Material.WHITE_CONCRETE, Triple(207, 213, 214)),
        WoolColor(Material.ORANGE_CONCRETE, Triple(242, 127, 46)),
        WoolColor(Material.MAGENTA_CONCRETE, Triple(195, 84, 205)),
        WoolColor(Material.LIGHT_BLUE_CONCRETE, Triple(58, 175, 217)),
        WoolColor(Material.YELLOW_CONCRETE, Triple(247, 213, 62)),
        WoolColor(Material.LIME_CONCRETE, Triple(128, 199, 31)),
        WoolColor(Material.PINK_CONCRETE, Triple(243, 140, 170)),
        WoolColor(Material.GRAY_CONCRETE, Triple(54, 57, 61)),
        WoolColor(Material.LIGHT_GRAY_CONCRETE, Triple(125, 125, 115)),
        WoolColor(Material.CYAN_CONCRETE, Triple(21, 119, 136)),
        WoolColor(Material.PURPLE_CONCRETE, Triple(100, 32, 156)),
        WoolColor(Material.BLUE_CONCRETE, Triple(44, 46, 143)),
        WoolColor(Material.BROWN_CONCRETE, Triple(96, 59, 31)),
        WoolColor(Material.GREEN_CONCRETE, Triple(73, 91, 36)),
        WoolColor(Material.RED_CONCRETE, Triple(142, 32, 32)),
        WoolColor(Material.BLACK_CONCRETE, Triple(25, 25, 25)),
        WoolColor(Material.WHITE_TERRACOTTA, Triple(209, 178, 161)),
        WoolColor(Material.ORANGE_TERRACOTTA, Triple(161, 83, 37)),
        WoolColor(Material.MAGENTA_TERRACOTTA, Triple(149, 88, 108)),
        WoolColor(Material.LIGHT_BLUE_TERRACOTTA, Triple(113, 108, 137)),
        WoolColor(Material.YELLOW_TERRACOTTA, Triple(186, 133, 35)),
        WoolColor(Material.LIME_TERRACOTTA, Triple(103, 117, 52)),
        WoolColor(Material.PINK_TERRACOTTA, Triple(160, 77, 78)),
        WoolColor(Material.GRAY_TERRACOTTA, Triple(57, 42, 35)),
        WoolColor(Material.LIGHT_GRAY_TERRACOTTA, Triple(135, 107, 98)),
        WoolColor(Material.CYAN_TERRACOTTA, Triple(87, 91, 91)),
        WoolColor(Material.PURPLE_TERRACOTTA, Triple(118, 70, 86)),
        WoolColor(Material.BLUE_TERRACOTTA, Triple(74, 59, 91)),
        WoolColor(Material.BROWN_TERRACOTTA, Triple(77, 51, 35)),
        WoolColor(Material.GREEN_TERRACOTTA, Triple(76, 83, 42)),
        WoolColor(Material.RED_TERRACOTTA, Triple(143, 61, 46)),
        WoolColor(Material.BLACK_TERRACOTTA, Triple(37, 22, 16))
    )

    return (woolColors.minByOrNull { color ->
        val (cr, cg, cb) = color.rgb
        sqrt(((cr - r).toDouble().pow(2) + (cg - g).toDouble().pow(2) + (cb - b).toDouble().pow(2)))
    })?.wool ?: Material.BLACK_WOOL
}

fun getRGBComponents(rgb: Int): Triple<Int, Int, Int> {
    val red = (rgb shr 16) and 0xFF
    val green = (rgb shr 8) and 0xFF
    val blue = rgb and 0xFF
    return Triple(red, green, blue)
}

class BuildCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>): Boolean {
        if ((sender as Player).location.y > 319 && sender.location.y < 0){
            sender.sendMessage("§cX coordinate must be between 0 and 319")
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage("§cUsage: /build §4<image-url> <step-reduce=1>")
            return true
        }
        try {
            val imageURL = URI(args[0]).toURL()
            val stepReduce = if (args.size >= 2)
                (args[1].toFloatOrNull() ?: 1).toFloat()
            else
                1f
            val image = ImageIO.read(imageURL)
            if (image == null) {
                sender.sendMessage("§cIncorrect URL")
                return true
            }
            val startMcX = sender.location.x
            var mcX = startMcX
            val mcY = sender.location.y
            var mcZ = sender.location.z
            val mcWorld = sender.world
            val max = max(
                ((image.width.toFloat()) / 300f) * stepReduce,
                ((image.height.toFloat()) / 300f) * stepReduce
            ).toInt()
            val step = if (max != 0) max else 1
            sender.sendMessage("§aImage size: §b${image.width}x${image.height}")
            sender.sendMessage("§aStep: §b$step")
            sender.sendMessage("§aImage size in game: §b${image.width/step}x${image.height/step}")
            for (y in 0 + 5..<image.height step (step)) {
                for (x in 0 + 5..<image.width step (step)) {
                    var rSum = 0
                    var gSum = 0
                    var bSum = 0
                    var count = 0
                    for (dY in -1..1) {
                        for (dX in -1..1) {
                            if (x + dX < image.width - 2 && x + dX > 0 && y + dY < image.height - 2 && y + dY > 0) {
                                val rgb = getRGBComponents(image.getRGB(x + dY, y + dX))
                                rSum += rgb.first
                                gSum += rgb.second
                                bSum += rgb.third
                                count++
                            }
                        }
                    }
                    count = if (count != 0) count else 1
                    val rgbMean = Triple(rSum / count, gSum / count, bSum / count)
                    val wool = findNearestWoolColor(rgbMean.first, rgbMean.second, rgbMean.third)
                    Location(mcWorld, mcX, mcY, mcZ).block.type = wool
                    mcX++
                }
                mcZ++
                mcX = startMcX
            }
            sender.sendMessage("§aSuccessfully built")
        } catch (e: Exception) {
            sender.sendMessage("§cError: §4${e.message}")
        }
        return true
    }
}