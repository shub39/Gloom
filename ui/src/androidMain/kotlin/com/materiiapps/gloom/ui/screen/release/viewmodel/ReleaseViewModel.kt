package com.materiiapps.gloom.ui.screen.release.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.screenModelScope
import com.materiiapps.gloom.api.repository.GraphQLRepository
import com.materiiapps.gloom.api.util.getOrNull
import com.materiiapps.gloom.domain.manager.DownloadManager
import com.materiiapps.gloom.gql.ReleaseDetailsQuery
import com.materiiapps.gloom.gql.fragment.ReleaseAssetFragment
import com.materiiapps.gloom.gql.fragment.ReleaseDetails
import com.materiiapps.gloom.gql.type.ReactionContent
import com.materiiapps.gloom.ui.screen.list.viewmodel.BaseListViewModel
import com.materiiapps.gloom.ui.util.installApks
import kotlinx.coroutines.launch
import java.io.File

actual class ReleaseViewModel(
    private val repo: GraphQLRepository,
    private val downloadManager: DownloadManager,
    private val context: Context,
    nameAndTag: Triple<String, String, String>
) : BaseListViewModel<ReleaseAssetFragment, ReleaseDetailsQuery.Data?>() {

    actual val owner = nameAndTag.first
    actual val name = nameAndTag.second
    actual val tag = nameAndTag.third

    actual var details by mutableStateOf<ReleaseDetails?>(null)
        private set

    actual var apkFile by mutableStateOf<File?>(null)
        private set

    actual override suspend fun loadPage(cursor: String?): ReleaseDetailsQuery.Data? =
        repo.getReleaseDetails(owner, name, tag, cursor)
            .getOrNull()
            .also { details = it?.repository?.release?.releaseDetails }

    actual override fun getCursor(data: ReleaseDetailsQuery.Data?): String? =
        data?.repository?.release?.releaseDetails?.releaseAssets?.pageInfo?.endCursor

    actual override fun createItems(data: ReleaseDetailsQuery.Data?): List<ReleaseAssetFragment> =
        data?.repository?.release?.releaseDetails?.releaseAssets?.nodes?.mapNotNull { it?.releaseAssetFragment }
            ?: emptyList()


    actual fun react(reaction: ReactionContent, unreact: Boolean) {
        details?.let {
            screenModelScope.launch {
                if (unreact) {
                    repo.unreact(it.id, reaction)
                } else {
                    repo.react(it.id, reaction)
                }
            }
        }
    }

    actual fun downloadAsset(url: String, mimeType: String, onFinish: () -> Unit) {
        downloadManager.download(url) {
            onFinish()
            if (mimeType == "application/vnd.android.package-archive") apkFile = File(it)
        }
    }

    actual fun clearApk() {
        apkFile = null
    }

    actual fun installApk() {
        apkFile?.let { context.installApks(it) }
    }

}