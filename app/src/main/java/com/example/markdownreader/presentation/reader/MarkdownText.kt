package com.example.markdownreader.presentation.reader

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.markdownreader.R
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin

@Composable
fun MarkdownText(
    markdown: String,
    fontSize: Float,
    isDarkTheme: Boolean,
    searchQuery: String = "",
    activeMatchIndex: Int = 0,
    onMatchCountChanged: (Int) -> Unit = {},
    onActiveMatchYChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val markwon = remember(context) {
        Markwon.builder(context)
            .usePlugin(CorePlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(ImagesPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(StrikethroughPlugin.create())
            .build()
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            TextView(viewContext).apply {
                movementMethod = LinkMovementMethod.getInstance()
                linksClickable = true
                setTextIsSelectable(true)
            }
        },
        update = { textView ->
            val newTextColor = if (isDarkTheme) Color.rgb(238, 238, 238) else Color.rgb(32, 32, 32)
            if (textView.currentTextColor != newTextColor) {
                textView.setTextColor(newTextColor)
            }

            val bgIsDark = if (isDarkTheme) 1 else 0
            if (textView.getTag(R.id.tag_bg_tracker) as? Int != bgIsDark) {
                textView.setBackgroundColor(
                    if (isDarkTheme) Color.rgb(17, 24, 39) else Color.WHITE
                )
                textView.setTag(R.id.tag_bg_tracker, bgIsDark)
            }

            if (textView.textSize != fontSize) {
                textView.textSize = fontSize
            }

            val sourceKey = markdown.hashCode()
            val cachedSource = textView.getTag(R.id.tag_markdown_source) as? Int
            val rendered: Spanned = if (cachedSource == null || cachedSource != sourceKey) {
                markwon.toMarkdown(markdown).also {
                    textView.setTag(R.id.tag_markdown_source, sourceKey)
                    textView.setTag(R.id.tag_rendered_markdown, it)
                }
            } else {
                textView.getTag(R.id.tag_rendered_markdown) as? Spanned
                    ?: markwon.toMarkdown(markdown).also {
                        textView.setTag(R.id.tag_markdown_source, sourceKey)
                        textView.setTag(R.id.tag_rendered_markdown, it)
                    }
            }

            val highlighted = rendered.highlightSearch(
                query = searchQuery,
                activeMatchIndex = activeMatchIndex,
                onMatchCountChanged = onMatchCountChanged,
                onActiveMatchOffsetChanged = { offset ->
                    textView.post {
                        val layout = textView.layout ?: return@post
                        val line = layout.getLineForOffset(offset)
                        onActiveMatchYChanged(layout.getLineTop(line))
                    }
                },
            )
            textView.text = highlighted
        },
    )
}

private fun CharSequence.highlightSearch(
    query: String,
    activeMatchIndex: Int,
    onMatchCountChanged: (Int) -> Unit,
    onActiveMatchOffsetChanged: (Int) -> Unit,
): CharSequence {
    if (query.isBlank()) {
        onMatchCountChanged(0)
        return this
    }

    val text = toString()
    val matches = Regex(Regex.escape(query), RegexOption.IGNORE_CASE)
        .findAll(text)
        .toList()
    onMatchCountChanged(matches.size)
    if (matches.isEmpty()) return this

    val activeIndex = activeMatchIndex.coerceIn(0, matches.lastIndex)
    val spannable = SpannableString(this)
    matches.forEachIndexed { index, match ->
        val color = if (index == activeIndex) {
            Color.rgb(255, 138, 0)
        } else {
            Color.rgb(255, 209, 102)
        }
        spannable.setSpan(
            BackgroundColorSpan(color),
            match.range.first,
            match.range.last + 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
    }
    onActiveMatchOffsetChanged(matches[activeIndex].range.first)
    return spannable
}
