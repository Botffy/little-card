package hu.sarmin.yt2ig

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import hu.sarmin.yt2ig.util.getGradientColors
import java.io.IOException
import kotlin.math.roundToInt

data class ShareCard(
    val image: Bitmap,
    val gradientColors: Pair<Int, Int>
)

private const val MAX_CARD_WIDTH = 1800
private const val MIN_CARD_WIDTH = 800

suspend fun generateCard(
    videoInfo: YouTubeVideoInfo,
    imageLoader: ImageLoader
): ShareCard = CardGenerator(videoInfo, imageLoader).generate()

private class CardGenerator(
    private val videoInfo: YouTubeVideoInfo,
    private val imageLoader: ImageLoader
) {
    // Calculated dimensions (set after loading thumbnail)
    private var cardWidth: Int = 0
    private var thumbHeight: Int = 0
    private var scaleFactor: Float = 1f

    // Base layout constants (for 1800px width)
    private val baseHorizontalPadding = 48f
    private val baseVerticalPadding = 48f
    private val baseYoutubeIconHeight = 100f
    private val baseLogoTitleSpacing = 32f
    private val baseTitleSpacing = 12f
    private val baseTitleTextSize = 48f
    private val baseChannelTextSize = 32f

    // Scaled layout constants (calculated after thumbnail is loaded)
    private val horizontalPaddingPx: Float get() = (baseHorizontalPadding * scaleFactor).roundToInt().toFloat()
    private val verticalPaddingPx: Float get() = (baseVerticalPadding * scaleFactor).roundToInt().toFloat()
    private val youtubeIconHeightPx: Float get() = (baseYoutubeIconHeight * scaleFactor).roundToInt().toFloat()
    private val logoTitleSpacingPx: Float get() = (baseLogoTitleSpacing * scaleFactor).roundToInt().toFloat()
    private val titleSpacingPx: Float get() = (baseTitleSpacing * scaleFactor).roundToInt().toFloat()
    private val titleTextSizePx: Float get() = (baseTitleTextSize * scaleFactor).roundToInt().toFloat()
    private val channelTextSizePx: Float get() = (baseChannelTextSize * scaleFactor).roundToInt().toFloat()

    private val maxTitleLines = 4
    private val frameColor = Color.parseColor("#FAFAFA")

    // Loaded resources
    private lateinit var thumbnail: Bitmap
    private lateinit var thumbnailGradient: Pair<Int, Int>
    private lateinit var logoBitmap: Bitmap
    private var logoWidthPx: Float = 0f

    // Text layouts
    private lateinit var titleLayout: StaticLayout
    private lateinit var channelLayout: StaticLayout

    suspend fun generate(): ShareCard {
        loadResources()
        createTextLayouts()
        val card = createCardBitmap()
        drawContent(card)
        return ShareCard(card, thumbnailGradient)
    }

    private suspend fun loadResources() {
        thumbnail = loadThumbnail()
        thumbnailGradient = thumbnail.getGradientColors()

        cardWidth = thumbnail.width.coerceIn(MIN_CARD_WIDTH, MAX_CARD_WIDTH)
        thumbHeight = cardWidth * 9 / 16

        scaleFactor = cardWidth.toFloat() / MAX_CARD_WIDTH

        logoBitmap = loadLogo()
        logoWidthPx = calculateLogoWidth()
    }

    private suspend fun loadThumbnail(): Bitmap {
        val thumbnailBytes = imageLoader.fetchImage(videoInfo.thumbnailUrl)
        return BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.size)
            ?: throw IOException("Failed to decode thumbnail image")
    }

    private fun loadLogo(): Bitmap {
        val logoBytes = imageLoader.fetchPresetImage(ImageLoader.PresetImageId.YOUTUBE_LOGO)
        return BitmapFactory.decodeByteArray(logoBytes, 0, logoBytes.size)
            ?: throw IllegalStateException("Failed to decode YouTube logo")
    }

    private fun calculateLogoWidth(): Float {
        val aspectRatio = logoBitmap.width.toFloat() / logoBitmap.height.toFloat()
        return youtubeIconHeightPx * aspectRatio
    }

    private fun createTextLayouts() {
        val textWidth = calculateTextWidth()
        titleLayout = createTitleLayout(textWidth)
        channelLayout = createChannelLayout(textWidth)
    }

    private fun calculateTextWidth(): Int {
        return (cardWidth - 2 * horizontalPaddingPx - logoWidthPx - logoTitleSpacingPx).toInt()
    }

    private fun createTitleLayout(width: Int): StaticLayout {
        val paint = TextPaint().apply {
            color = Color.parseColor("#212121")
            textSize = titleTextSizePx
            isAntiAlias = true
            isFakeBoldText = true
        }

        return StaticLayout.Builder.obtain(
            videoInfo.title,
            0,
            videoInfo.title.length,
            paint,
            width
        ).apply {
            setAlignment(Layout.Alignment.ALIGN_NORMAL)
            setMaxLines(maxTitleLines)
            setEllipsize(TextUtils.TruncateAt.END)
            setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY)
        }.build()
    }

    private fun createChannelLayout(width: Int): StaticLayout {
        val paint = TextPaint().apply {
            color = Color.parseColor("#616161")
            textSize = channelTextSizePx
            isAntiAlias = true
        }

        return StaticLayout.Builder.obtain(
            videoInfo.channel,
            0,
            videoInfo.channel.length,
            paint,
            width
        ).apply {
            setAlignment(Layout.Alignment.ALIGN_NORMAL)
            setMaxLines(2)
            setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY)
            setEllipsize(TextUtils.TruncateAt.END)
        }.build()
    }

    private fun createCardBitmap(): Bitmap {
        val bottomBarHeight = calculateBottomBarHeight()
        val cardHeight = thumbHeight + bottomBarHeight
        return Bitmap.createBitmap(cardWidth, cardHeight, Bitmap.Config.ARGB_8888)
    }

    private fun calculateBottomBarHeight(): Int {
        val textContentHeight = titleLayout.height + titleSpacingPx + channelLayout.height
        val contentHeight = kotlin.math.max(textContentHeight, youtubeIconHeightPx)
        return (verticalPaddingPx + contentHeight + verticalPaddingPx).toInt()
    }

    private fun drawContent(card: Bitmap) {
        val canvas = Canvas(card)

        drawBackground(canvas)
        drawThumbnail(canvas)
        drawBottomBar(canvas, card.height)
        drawLogo(canvas)
        drawTextContent(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(frameColor)
    }

    private fun drawThumbnail(canvas: Canvas) {
        val scaledThumbnail = scaleThumbnail(thumbnail, cardWidth, thumbHeight)
        canvas.drawBitmap(scaledThumbnail, 0f, 0f, null)
    }

    private fun drawBottomBar(canvas: Canvas, cardHeight: Int) {
        val paint = Paint().apply {
            color = frameColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            0f,
            thumbHeight.toFloat(),
            cardWidth.toFloat(),
            cardHeight.toFloat(),
            paint
        )
    }

    private fun drawLogo(canvas: Canvas) {
        val contentStartY = thumbHeight + verticalPaddingPx
        val logoX = cardWidth - horizontalPaddingPx - logoWidthPx
        drawScaledLogo(canvas, logoX, contentStartY, youtubeIconHeightPx)
    }

    private fun drawTextContent(canvas: Canvas) {
        val contentStartY = thumbHeight + verticalPaddingPx

        canvas.save()
        canvas.translate(horizontalPaddingPx, contentStartY)
        titleLayout.draw(canvas)
        canvas.restore()

        canvas.save()
        val channelY = contentStartY + titleLayout.height + titleSpacingPx
        canvas.translate(horizontalPaddingPx, channelY)
        channelLayout.draw(canvas)
        canvas.restore()
    }

    private fun scaleThumbnail(thumbnail: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val sourceWidth = thumbnail.width
        val sourceHeight = thumbnail.height

        val scale = targetWidth.toFloat() / sourceWidth
        val scaledHeight = (sourceHeight * scale).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(thumbnail, targetWidth, scaledHeight, true)

        // Center crop if needed
        if (scaledHeight > targetHeight) {
            val yOffset = (scaledHeight - targetHeight) / 2
            return Bitmap.createBitmap(scaledBitmap, 0, yOffset, targetWidth, targetHeight)
        }

        return scaledBitmap
    }

    private fun drawScaledLogo(canvas: Canvas, x: Float, y: Float, height: Float) {
        val aspectRatio = logoBitmap.width.toFloat() / logoBitmap.height.toFloat()
        val scaledWidth = height * aspectRatio

        val scaledLogo = Bitmap.createScaledBitmap(
            logoBitmap,
            scaledWidth.toInt(),
            height.toInt(),
            true
        )

        canvas.drawBitmap(scaledLogo, x, y, null)

        if (scaledLogo != logoBitmap) {
            scaledLogo.recycle()
        }
    }
}
