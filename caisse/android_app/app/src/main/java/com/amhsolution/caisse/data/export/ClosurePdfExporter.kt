package com.amhsolution.caisse.data.export

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.amhsolution.caisse.domain.printing.ClosureReportPrintPayload
import com.amhsolution.caisse.domain.printing.toPrintableText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.ZoneId

object ClosurePdfExporter {
    suspend fun export(
        payload: ClosureReportPrintPayload,
        file: File,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): File = withContext(Dispatchers.IO) {
        file.parentFile?.mkdirs()
        val document = PdfDocument()
        try {
            val lines = payload.toPrintableText(zoneId).lines()
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = TextSize
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }
            val footerPaint = Paint(textPaint).apply {
                textSize = FooterTextSize
                textAlign = Paint.Align.RIGHT
            }
            var lineIndex = 0
            var pageNumber = 1

            do {
                val pageInfo = PdfDocument.PageInfo.Builder(PageWidth, PageHeight, pageNumber).create()
                val page = document.startPage(pageInfo)
                page.canvas.drawColor(Color.WHITE)
                var baseline = TopMargin

                while (lineIndex < lines.size && baseline <= LastTextBaseline) {
                    page.canvas.drawText(lines[lineIndex], LeftMargin, baseline, textPaint)
                    lineIndex += 1
                    baseline += LineHeight
                }

                page.canvas.drawText(
                    "Page $pageNumber",
                    PageWidth - RightMargin,
                    PageHeight - BottomMargin,
                    footerPaint
                )
                document.finishPage(page)
                pageNumber += 1
            } while (lineIndex < lines.size)

            FileOutputStream(file).use { output -> document.writeTo(output) }
        } finally {
            document.close()
        }
        file
    }

    private const val PageWidth = 595
    private const val PageHeight = 842
    private const val LeftMargin = 40f
    private const val RightMargin = 40f
    private const val TopMargin = 52f
    private const val BottomMargin = 32f
    private const val LastTextBaseline = PageHeight - 52f
    private const val TextSize = 10.5f
    private const val FooterTextSize = 8f
    private const val LineHeight = 15f
}
