package com.enigmaticdevs.wallhaven.ui.util

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.RenderVectorGroup
import androidx.compose.ui.graphics.vector.VectorConfig
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.VectorProperty
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import com.enigmaticdevs.wallhaven.R
import java.util.Arrays.fill

@Composable
fun themedAppDrawable(id : Int, color : Color) : VectorPainter {
    // 1. Fetch your Compose dynamic theme color
    val dynamicColor = MaterialTheme.colorScheme.secondary

    // 2. Load the base vector template from XML
    val imageVector = ImageVector.vectorResource(id = R.drawable.wall)

    // 3. Build a custom VectorPainter targeting the path name
    val painter = rememberVectorPainter(
        defaultWidth = imageVector.defaultWidth,
        defaultHeight = imageVector.defaultHeight,
        viewportWidth = imageVector.viewportWidth,
        viewportHeight = imageVector.viewportHeight,
        name = imageVector.name,
        tintColor = Color.Unspecified, // Critical: Prevents tinting the entire file
        tintBlendMode = BlendMode.SrcIn,
        autoMirror = false,
        content = { _, _ ->
            RenderVectorGroup(
                group = imageVector.root,
                configs = mapOf(
                    "dynamic_secondary_part" to object : VectorConfig {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T> getOrDefault(
                            property: VectorProperty<T>,
                            defaultValue: T
                        ): T {
                            return when (property) {
                                // Intercept the Fill property and inject our dynamic color brush
                                VectorProperty.Fill -> SolidColor(dynamicColor) as T
                                else -> defaultValue
                            }
                        }
                    })
            )
        })

    return painter

    /*// 4. Render it exactly like a regular painter
    Image(
        painter = painter,
        contentDescription = "App Custom Icon"
    )*/
}
