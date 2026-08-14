package ing.fuyaoskyrocket.applocale.model

import java.util.Locale

/**
 * Reusable query state for any screen that renders installed applications.
 * Keeping filtering and ordering outside Compose makes the same list behavior
 * available to the home, large-screen, and future configuration screens.
 */
data class AppListQuery(
    val query: String = "",
    val modifiedOnly: Boolean = false,
    val showSystemApps: Boolean = true,
    val sortOption: AppListSortOption = AppListSortOption.AppName,
    val sortAscending: Boolean = true,
)

enum class AppListSortOption {
    AppName,
    PackageName,
    Locale,
    Modified,
    AppType,
}

/** Applies the same filtering and stable ordering rules wherever an app list is shown. */
fun List<AppModel>.applyQuery(query: AppListQuery): List<AppModel> {
    val normalizedQuery = query.query.trim().lowercase(Locale.ROOT)
    val filtered = asSequence().filter { app ->
        (!query.modifiedOnly || app.isModified) &&
            (query.showSystemApps || !app.isSystemApp) &&
            (normalizedQuery.isBlank() ||
                app.label.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                app.packageName.lowercase(Locale.ROOT).contains(normalizedQuery))
    }

    val byName = compareBy<AppModel> { it.label.lowercase(Locale.ROOT) }
        .thenBy { it.packageName.lowercase(Locale.ROOT) }
    val comparator = when (query.sortOption) {
        AppListSortOption.AppName -> byName
        AppListSortOption.PackageName -> compareBy<AppModel> {
            it.packageName.lowercase(Locale.ROOT)
        }.then(byName)
        AppListSortOption.Locale -> compareBy<AppModel> {
            it.localeTag.orEmpty().lowercase(Locale.ROOT)
        }.then(byName)
        AppListSortOption.Modified -> compareByDescending<AppModel> { it.isModified }.then(byName)
        AppListSortOption.AppType -> compareBy<AppModel> { it.isSystemApp }.then(byName)
    }

    return filtered.sortedWith(
        if (query.sortAscending) comparator else comparator.reversed(),
    ).toList()
}
