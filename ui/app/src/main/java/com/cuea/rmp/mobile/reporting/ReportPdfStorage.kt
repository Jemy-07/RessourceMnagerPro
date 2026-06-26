package com.cuea.rmp.mobile.reporting

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

// Saves an exported report PDF to app-scoped external storage (no runtime storage
// permission required) and hands back a content:// Uri usable for opening/sharing.
@Singleton
class ReportPdfStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun save(type: ReportExportType, bytes: ByteArray): Uri {
        val dir = File(context.getExternalFilesDir(null), "reports").apply { mkdirs() }
        val file = File(dir, "${type.apiValue}-report-${System.currentTimeMillis()}.pdf")
        file.writeBytes(bytes)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
