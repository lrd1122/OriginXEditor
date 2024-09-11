package cc.originx.lrd1122.utils

import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapPalette
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.net.URL
import javax.imageio.ImageIO

class CustomRenderer(var url: String) : MapRenderer() {
    override fun render(mapView: MapView, mapCanvas: MapCanvas, player: Player) {
        val image = MapPalette.resizeImage(ImageIO.read(URL(url)))
        mapCanvas.drawImage(0, 0, image);
        mapView.setCenterX(30000);
        mapView.setCenterZ(30000);
    }
}