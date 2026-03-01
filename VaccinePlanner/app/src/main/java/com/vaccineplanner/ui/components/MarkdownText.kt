package com.vaccineplanner.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.image.ImagesPlugin

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            TextView(context).apply {
                layoutDirection = android.view.View.LAYOUT_DIRECTION_LTR
            }
        },
        update = { textView ->
            val markwon = Markwon.builder(textView.context)
                .usePlugin(CorePlugin.create())
                .usePlugin(TablePlugin.create(textView.context))
                .usePlugin(TaskListPlugin.create(textView.context))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(ImagesPlugin.create())
                .usePlugin(LinkifyPlugin.create())
                .usePlugin(object : AbstractMarkwonPlugin() {
                    override fun configureTheme(builder: MarkwonTheme.Builder) {
                        builder
                            .headingTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
                            .headingTextSizeMultipliers(floatArrayOf(2f, 1.5f, 1.17f, 1f, 0.83f, 0.67f))
                            .codeBlockMargin(16.dp.value.toInt())
                            .blockQuoteColor(0xFF6B7280.toInt())
                            .blockQuoteWidth(4.dp.value.toInt())
                            .linkColor(0xFF3B82F6.toInt())
                            .codeTypeface(Typeface.MONOSPACE)
                            .codeTextSize(13.sp.value.toInt())
                    }
                })
                .build()
            
            markwon.setMarkdown(textView, markdown)
        },
        modifier = modifier
    )
}