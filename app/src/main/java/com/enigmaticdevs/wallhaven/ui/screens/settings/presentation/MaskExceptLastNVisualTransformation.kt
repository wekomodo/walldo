package com.enigmaticdevs.wallhaven.ui.screens.settings.presentation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class MaskExceptLastNVisualTransformation(
    private val visibleChars : Int = 4,
    private val maskChar : Char = '*'
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val length = originalText.length

        val transformed = if ( length<= visibleChars){
            originalText
        }else{
            val maskedCount = length -visibleChars
            maskChar.toString().repeat(maskedCount) + originalText.takeLast(visibleChars)
        }

        return TransformedText(
            text = AnnotatedString(transformed),
            offsetMapping = OffsetMapping.Identity
        )
    }


}