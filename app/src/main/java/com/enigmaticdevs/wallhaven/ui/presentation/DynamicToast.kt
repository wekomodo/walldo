package com.enigmaticdevs.wallhaven.ui.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.enigmaticdevs.wallhaven.R


@Composable
fun DynamicToast(
    message : String,
    modifier : Modifier
) {

    Surface(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            .graphicsLayer{
                shape = RoundedCornerShape(24.dp)
                clip = true
            },
        color = MaterialTheme.colorScheme.primaryContainer,
    ){

        Row(
            modifier = modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            Icon(painter = painterResource(R.drawable.wall),
                tint = Color.Unspecified, contentDescription = "")
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelLarge
            )
        }


    }

}


@Composable
fun DynamicToastOverlay(
    isVisible : Boolean,
    message: String
){
    Popup(
        alignment = Alignment.BottomCenter,
        properties = PopupProperties(
            focusable = false, // Lets touches pass through to underlying buttons
            dismissOnBackPress = false
        )
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.85f, animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f, animationSpec = tween(200))
        ) {
            DynamicToast(
                message = message,
                modifier = Modifier
                    .systemBarsPadding() // Safely shifts the toast above the navigation pill bar
            )
        }
    }
}



@Preview
@Composable
fun DynamicToastPreview(){
    DynamicToast(message = "Hello", modifier = Modifier)
}