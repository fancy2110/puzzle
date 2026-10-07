package com.puzzle.game.data

/**
 * Persists per-story scene completion so players can leave a story and resume later.
 * Keys are story ids; values are the number of scenes completed from the first scene.
 */
interface StoryProgressStore {
    fun getCompletedSceneCounts(): Map<String, Int>
    fun recordCompletedScenes(storyId: String, completedSceneCount: Int)
}

/** [StoryProgressStore] backed by the platform [Preferences]. */
class PreferencesStoryProgressStore(
    private val preferences: Preferences
) : StoryProgressStore {
    override fun getCompletedSceneCounts(): Map<String, Int> = preferences.getStoryProgress()

    override fun recordCompletedScenes(storyId: String, completedSceneCount: Int) {
        preferences.setStoryProgress(storyId, completedSceneCount)
    }
}

/** In-memory implementation used for previews/tests when no platform store is available. */
class InMemoryStoryProgressStore : StoryProgressStore {
    private val progress = mutableMapOf<String, Int>()

    override fun getCompletedSceneCounts(): Map<String, Int> = progress.toMap()

    override fun recordCompletedScenes(storyId: String, completedSceneCount: Int) {
        progress[storyId] = maxOf(progress[storyId] ?: 0, completedSceneCount)
    }
}
