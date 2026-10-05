package com.puzzle.game.data

data class StoryPageData(
    val id: String,
    val storyId: String,
    val storyTitle: String,
    val chapter: String,
    val title: String,
    val story: String,
    val theme: ThemeData
)

object StoryPresets {
    val stories = BuiltinStoryImageCatalog.stories
    val defaultStoryId = stories.first().id

    val pages = pagesForStory(defaultStoryId)

    fun pagesForStory(storyId: String): List<StoryPageData> {
        val story = stories.firstOrNull { it.id == storyId } ?: stories.first()
        return story.pages.mapIndexed { index, page ->
            StoryPageData(
                id = page.id,
                storyId = story.id,
                storyTitle = story.title,
                chapter = "第${index + 1}幕",
                title = page.title,
                story = page.story,
                theme = ThemeData(
                    id = page.id,
                    name = page.title,
                    emoji = "",
                    primary = story.primary,
                    secondary = story.secondary,
                    accent = story.accent,
                    description = page.prompt,
                    assetFile = page.assetFile
                )
            )
        }
    }

    fun storyPreviewTheme(story: BuiltinStoryImageSet): ThemeData {
        val firstPage = story.pages.first()
        return ThemeData(
            id = story.id,
            name = story.title,
            emoji = "",
            primary = story.primary,
            secondary = story.secondary,
            accent = story.accent,
            description = "${story.origin} · ${story.ageRange} · ${story.moral}",
            assetFile = firstPage.assetFile
        )
    }

    fun indexOfTheme(themeId: String): Int {
        val storyId = themeId.substringBeforeLast("-", missingDelimiterValue = defaultStoryId)
        return pagesForStory(storyId).indexOfFirst { it.theme.id == themeId }.takeIf { it >= 0 } ?: 0
    }
}
