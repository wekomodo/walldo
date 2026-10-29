package com.enigmaticdevs.wallhaven.ui.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


sealed interface ImageSource {
    data class Vector(val imageVector: ImageVector) : ImageSource
    data class Resource(@DrawableRes val resId: Int) : ImageSource
    data class CustomPainter(val painter: Painter) : ImageSource
}

fun ImageVector.asSource() = ImageSource.Vector(this)
fun Int.asSource() = ImageSource.Resource(this)


sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class ResourceString(@StringRes val resId: Int) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is ResourceString -> stringResource(resId)
        }
    }
}

// title for different categories
@Composable
fun CategoryTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
    )
}



// Clickable Prefs replicated to feel like Old xml Prefs
@Composable
fun ClickablePref(text: String, description: String, icon: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            painter = painterResource(icon),
            contentDescription = text
        )
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 8.dp, top = 8.dp)
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodySmall)

        }
    }

}




@Composable
fun ClickablePrefWithButton(
    isChecked: Boolean,
    text: String,
    description: String,
    imageSource: ImageSource,
    onCheckedChange: () -> Unit
) {

    val painter = when (imageSource) {
        is ImageSource.Vector -> androidx.compose.ui.graphics.vector.rememberVectorPainter(imageSource.imageVector)
        is ImageSource.Resource -> painterResource(id = imageSource.resId)
        is ImageSource.CustomPainter -> imageSource.painter
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = onCheckedChange)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            painter = painter,
            contentDescription = text
        )
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 8.dp, top = 8.dp)
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodySmall)

        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Switch(
                checked = isChecked,
                onCheckedChange = { onCheckedChange() }
            )
        }
    }

}


@Composable
fun ClickablePrefWithCheckbox(
    isChecked: Boolean,
    text: UiText,
    description: UiText,
    imageSource: ImageSource,
    onCheckedChange: () -> Unit
) {

    val painter = when (imageSource) {
        is ImageSource.Vector -> androidx.compose.ui.graphics.vector.rememberVectorPainter(imageSource.imageVector)
        is ImageSource.Resource -> painterResource(id = imageSource.resId)
        is ImageSource.CustomPainter -> imageSource.painter
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = onCheckedChange)
            .padding(8.dp)
    ) {
        Icon(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            painter = painter,
            contentDescription = text.asString()
        )
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 8.dp, top = 8.dp)
        ) {
            Text(text = text.asString(), style = MaterialTheme.typography.bodyLarge)
            Text(text = description.asString(), style = MaterialTheme.typography.bodySmall)

        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onCheckedChange() }
            )
        }
    }

}




@Preview
@Composable
fun ClickablePrefsPreview(){
Column(){
    ClickablePrefWithButton(
        true,
        "Dynamic colors",
        "Accent colors from system",
        Icons.Rounded.Face.asSource(),
        onCheckedChange = {}
    )

    ClickablePrefWithCheckbox(
        true,
        UiText.DynamicString("Dynamic colors"),
        UiText.DynamicString("Accent colors from system"),
        Icons.Rounded.Face.asSource(),
        onCheckedChange = {}
    )
}



}

@Composable
fun PrefTopAppBar(title: String, onBack: () -> Unit){

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = {onBack()}) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
        }
    )
}