package com.enigmaticdevs.wallhaven.ui.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.navigation.Screen


data class NavItem(
    val title: String,
    val icon: ImageVector,
)

private val items = listOf(
    NavItem("Home", Icons.Rounded.Home) ,
    NavItem("Collections", Icons.Default.Favorite),
    NavItem("Auto", Icons.Default.PlayArrow,),
    NavItem("Settings", Icons.Default.Settings),
)

@Composable
fun FloatingNavBar(
    modifier: Modifier,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
           // .fillMaxHeight(.15f)
             .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shadowElevation = 8.dp,
            //tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index,item ->
                    NavBarItem(
                        item = item,
                        isSelected = index ==selectedIndex,
                        onClick = { onSelect(index) }
                    )
                }

            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        animationSpec = NavSpringColor,
        label = "bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = NavSpringColor,
        label = "tint"
    )

    Row(
        modifier = Modifier
            .height(54.dp)
            .clip(CircleShape)
            .background(containerColor)
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick)
            //.padding(horizontal = 4.dp)
            .animateContentSize(NavSpringSize),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    )
    {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = contentColor,
            modifier = Modifier.minimumInteractiveComponentSize()
        )
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(NavSpringFloat) + expandHorizontally(NavSpringSize),
            exit = fadeOut(NavSpringFloat) + shrinkHorizontally(NavSpringSize)
        ) {

            Text(
                text = item.title,
                color = contentColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

    }
}

private val NavSpringColor = spring<Color>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMedium
)
private val NavSpringSize = spring<IntSize>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMedium
)
private val NavSpringFloat = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMedium
)

@Composable
@Preview
fun Preview() {
 //   FloatingNavBar(Modifier,)
}