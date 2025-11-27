package hu.sarmin.yt2ig

import hu.sarmin.yt2ig.AppState.Share.LoadingState


data class Navigation(
    val navigateTo: (AppState) -> Unit,
    val replaceState: (AppState, AppState) -> Unit
)

class CardCreationService(
    private val youTubeService: YouTubeService,
    private val imageLoader: ImageLoader,
    private val imageStore: ImageStore,
    private val navigation: Navigation
) {

    private val sequence = listOf(
        ::getInfo,
        ::getThumbnail,
        ::createCard
    )

    suspend fun createCard(target: YouTubeVideo) {
        var currentState: LoadingState = LoadingState.Starting(target)
        navigation.navigateTo(AppState.Share(target, currentState))

        for (step in sequence) {
            try {
                step(currentState).also { newState ->
                    navigation.replaceState(
                        AppState.Share(target, currentState),
                        AppState.Share(target, newState)
                    )
                    currentState = newState
                }
            } catch (e: Exception) {
                navigation.replaceState(
                    AppState.Share(target, currentState),
                    AppState.Error("Error: ${e.message ?: e::class.simpleName}")
                )
                return
            }
        }
    }

    private suspend fun getInfo(currentState: LoadingState): LoadingState {
        require(currentState is LoadingState.Starting)
        val videoInfo = youTubeService.getVideoInfo(currentState.target.videoId)
        return LoadingState.LoadedInfo(currentState.target, videoInfo)
    }

    private suspend fun getThumbnail(currentState: LoadingState): LoadingState {
        require(currentState is LoadingState.LoadedInfo)
        imageLoader.ensureLoaded(currentState.data.thumbnailUrl)
        return LoadingState.LoadedThumbnail(currentState.target, currentState.data)
    }

    private suspend fun createCard(currentState: LoadingState): LoadingState {
        require(currentState is LoadingState.LoadedThumbnail)
        val shareCard = generateCard(currentState.data, imageLoader, imageStore)
        return LoadingState.Created(currentState.target, currentState.data, shareCard)
    }
}
