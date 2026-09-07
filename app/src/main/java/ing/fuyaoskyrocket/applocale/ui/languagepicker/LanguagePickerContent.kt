package ing.fuyaoskyrocket.applocale.ui.languagepicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.LocaleGroup
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppComponentDefaults
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppUiTheme
import ing.fuyaoskyrocket.applocale.ui.designsystem.readableContentWidth
import ing.fuyaoskyrocket.applocale.ui.components.rememberSystemLocaleTag
import ing.fuyaoskyrocket.applocale.ui.components.LocaleBadge
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppFilterChip
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIcon
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppIconButton
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppLocaleChoiceRow
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSearchField
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSettingsRow
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbol
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppSymbolVector
import ing.fuyaoskyrocket.applocale.ui.designsystem.component.AppText

/**
 * The fixed candidate projection behind the detail search: the active group's
 * options when drilled in, otherwise the merged directory (groups + system +
 * pinned) deduplicated by tag. The live query then filters it with the same
 * predicate the search results use. Never a claim about the target app's
 * supported languages.
 */
internal fun LocalePickerUiState.candidateOptions(
    systemLocaleTag: String,
): List<LocaleOption> {
    val base = activeGroup?.options ?: run {
        localeGroups.asSequence()
            .flatMap { it.options.asSequence() }
            .plus(systemLocales.asSequence())
            .plus(pinnedLocales.asSequence())
            .distinctBy { it.languageTag }
            .toList()
    }
    val query = this.query.trim()
    val filtered = if (query.isBlank()) {
        base
    } else {
        val groupByTag = localeGroups
            .flatMap { group -> group.options.map { it.languageTag to group } }
            .toMap()
        base.filter { option ->
            option.languageTag.contains(query, ignoreCase = true) ||
                option.displayName.contains(query, ignoreCase = true) ||
                option.localizedDisplayName.contains(query, ignoreCase = true) ||
                groupByTag[option.languageTag]?.let { group ->
                    group.language.contains(query, ignoreCase = true) ||
                        group.localizedLanguage.contains(query, ignoreCase = true)
                } == true
        }
    }
    return sortedLocaleVariants(filtered, systemLocaleTag)
}

/**
 * Reusable Material 3 language picker for both app detail and batch operations.
 *
 * The top level presents a real search field, system default, pinned/device locales, and
 * language groups. Search results are selectable locales rather than another level of groups.
 * Opening a group replaces the directory with a section heading and radio-style locale choices.
 *
 * Round-8 036: the list is wrapped in the shared [LanguageGroupTransition], so
 * every directory-style caller gets the same predictive group return. The
 * three scroll states live OUTSIDE the transition so the parent directory
 * position survives the drill-in and back. [backEnabled] must follow the
 * sheet's own visibility and stay true while a query is blank — callers keep
 * their plain BackHandler for clearing a non-blank query only, never for
 * leaving the group.
 */
@Composable
fun LanguagePickerContent(
    state: LocalePickerUiState,
    onAction: (LocalePickerAction) -> Unit,
    onResetToSystemDefault: () -> Unit,
    onSelectLocale: (LocaleOption) -> Unit,
    modifier: Modifier = Modifier,
    canPin: Boolean = true,
    showSystemDefault: Boolean = true,
    backEnabled: Boolean = true,
) {
    val listStates = rememberLanguagePickerListStates(
        state = state,
        firstVisibleItemIndex = 0,
    )
    val systemLocaleTag = rememberSystemLocaleTag()

    LanguageGroupTransition(
        groupId = state.selectedGroupId,
        onBack = { onAction(LocalePickerAction.BackToGroups) },
        backEnabled = backEnabled,
        modifier = modifier.fillMaxWidth(),
    ) { visibleGroupId, interactive ->
        // Each animated layer projects its own group; never feed the outgoing
        // layer the incoming directory (same contract as the app detail page).
        val visibleState = state.copy(selectedGroupId = visibleGroupId)

        // Round-7 028: the list itself keeps no horizontal inset — each item owns
        // its frame: 16dp for search/chips/headers, 4dp for the locale/group rows
        // whose selected backgrounds expand into that outer margin. One 840dp box
        // overall.
        LazyColumn(
            state = listStates.forState(visibleState),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                top = AppSpacing.sm,
                bottom = AppSpacing.xl,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            languagePickerItems(
                state = visibleState,
                // The transition's interactive permit is the single gate for
                // business callbacks, matching the app detail page.
                onAction = { action -> if (interactive) onAction(action) },
                onResetToSystemDefault = { if (interactive) onResetToSystemDefault() },
                onSelectLocale = { option -> if (interactive) onSelectLocale(option) },
                canPin = canPin,
                showSystemDefault = showSystemDefault,
                systemLocaleTag = systemLocaleTag,
                contentModifier = Modifier
                    .readableContentWidth()
                    .padding(horizontal = AppLayout.contentFrameMargin),
                rowModifier = Modifier
                    .readableContentWidth()
                    .padding(horizontal = AppLayout.localeRowOuterMargin),
            )
        }
    }
}

/**
 * Keeps directory, search and locale-variant scroll positions independent.
 * Returning from a child group restores the exact parent directory position.
 */
@Composable
internal fun rememberLanguagePickerListState(
    state: LocalePickerUiState,
    firstVisibleItemIndex: Int,
    contentKey: Any? = Unit,
): LazyListState = rememberLanguagePickerListStates(state, firstVisibleItemIndex, contentKey).forState(state)

@Stable
class LanguagePickerListStates(
    val directory: LazyListState,
    val search: LazyListState,
    val group: LazyListState,
) {
    fun forState(state: LocalePickerUiState): LazyListState = when {
        state.isInGroup -> group
        state.query.isNotBlank() -> search
        else -> directory
    }
}

@Composable
fun rememberLanguagePickerListStates(
    state: LocalePickerUiState,
    firstVisibleItemIndex: Int = 0,
    contentKey: Any? = Unit,
): LanguagePickerListStates {
    val directoryState = rememberLazyListState(
        initialFirstVisibleItemIndex = firstVisibleItemIndex,
    )
    val searchState = rememberLazyListState(
        initialFirstVisibleItemIndex = firstVisibleItemIndex,
    )
    val groupState = rememberLazyListState(
        initialFirstVisibleItemIndex = firstVisibleItemIndex,
    )

    LaunchedEffect(contentKey) {
        directoryState.scrollToItem(firstVisibleItemIndex)
        searchState.scrollToItem(firstVisibleItemIndex)
        groupState.scrollToItem(firstVisibleItemIndex)
    }
    LaunchedEffect(state.query) {
        if (state.query.isNotBlank()) {
            searchState.scrollToItem(firstVisibleItemIndex)
        }
    }
    LaunchedEffect(state.selectedGroupId) {
        if (state.selectedGroupId != null) {
            groupState.scrollToItem(firstVisibleItemIndex)
        }
    }
    LaunchedEffect(state.groupSortOption, state.groupSortAscending) {
        directoryState.scrollToItem(firstVisibleItemIndex)
    }
    LaunchedEffect(state.variantSortOption, state.variantSortAscending) {
        searchState.scrollToItem(firstVisibleItemIndex)
        groupState.scrollToItem(firstVisibleItemIndex)
    }

    return remember(directoryState, searchState, groupState) {
        LanguagePickerListStates(directoryState, searchState, groupState)
    }
}

/**
 * Adds the same picker to an existing lazy list without nesting scroll containers.
 *
 * [contentModifier] frames the search field, sort chips, headers and empty states
 * at the shared 16dp foreground edge; [rowModifier] frames the locale/group rows
 * whose selected background expands 4dp into the outer margin (round-7 028). The
 * two are never swapped: rows never take 16, headers never take 4.
 */
fun LazyListScope.languagePickerItems(
    state: LocalePickerUiState,
    onAction: (LocalePickerAction) -> Unit,
    onResetToSystemDefault: () -> Unit,
    onSelectLocale: (LocaleOption) -> Unit,
    canPin: Boolean = true,
    showSystemDefault: Boolean = true,
    systemLocaleTag: String,
    contentModifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    topLevelSortControls: Boolean = true,
    showSearchField: Boolean = true,
) {
    val activeGroup = state.activeGroup
    val pinnedTags = state.pinnedLocales.mapTo(mutableSetOf()) { it.languageTag }

    if (activeGroup != null) {
        groupLocaleItems(
            group = activeGroup,
            state = state,
            pinnedTags = pinnedTags,
            onAction = onAction,
            onSelectLocale = onSelectLocale,
            canPin = canPin,
            systemLocaleTag = systemLocaleTag,
            contentModifier = contentModifier,
            rowModifier = rowModifier,
        )
        return
    }

    if (showSearchField) {
        item(key = "language_search") {
            AppSearchField(
                query = state.query,
                onQueryChange = { onAction(LocalePickerAction.QueryChanged(it)) },
                placeholder = stringResource(R.string.search_languages),
                modifier = contentModifier.padding(
                    vertical = AppSpacing.sm,
                ),
            )
        }
    }

    if (topLevelSortControls) {
        item(key = "language_sort_controls") {
            LanguageSortFilterBar(
                state = state,
                onAction = onAction,
                useVariantOptions = state.query.isNotBlank(),
                modifier = contentModifier,
            )
        }
    }

    if (showSystemDefault) {
        item(key = "system_default") {
            val localizedSystemLocale = state.systemLocales
                .firstOrNull { option -> option.languageTag == systemLocaleTag }
            LocaleSelectionRow(
                text = stringResource(R.string.system_default),
                supportingText = localizedSystemLocale?.supportingLabel()
                    ?: systemLocaleTag.takeIf { it.isNotBlank() },
                selected = state.selectedLanguageTag == null,
                onClick = onResetToSystemDefault,
                leading = {
                    LocaleBadge(
                        languageTag = systemLocaleTag,
                        preferRegion = true,
                    )
                },
                modifier = rowModifier,
            )
        }
    }

    if (state.query.isNotBlank()) {
        searchResultItems(
            state = state,
            pinnedTags = pinnedTags,
            onAction = onAction,
            onSelectLocale = onSelectLocale,
            canPin = canPin,
            systemLocaleTag = systemLocaleTag,
            contentModifier = contentModifier,
            rowModifier = rowModifier,
        )
        return
    }

    val unpinnedSystemLocales = state.sortedLocaleVariants(
        options = state.systemLocales.filterNot { it.languageTag in pinnedTags },
        systemLocaleTag = systemLocaleTag,
    )
    if (unpinnedSystemLocales.isNotEmpty()) {
        item(key = "header_user") {
            SectionHeader(
                text = stringResource(R.string.user_languages),
                modifier = contentModifier,
            )
        }
        items(
            items = unpinnedSystemLocales,
            key = { "system_${it.languageTag}" },
            contentType = { "locale" },
        ) { locale ->
            LocaleOptionRow(
                locale = locale,
                selected = locale.languageTag == state.selectedLanguageTag,
                isPinned = false,
                canPin = canPin,
                onPinAction = { onAction(LocalePickerAction.PinClicked(locale)) },
                onClick = { onSelectLocale(locale) },
                modifier = rowModifier,
            )
        }
    }

    item(key = "header_all") {
        SectionHeader(
            text = stringResource(R.string.all_languages),
            modifier = contentModifier,
        )
    }
    items(
        items = state.sortedLanguageGroups(systemLocaleTag),
        key = { "group_${it.id}" },
        contentType = { "group" },
    ) { group ->
        LanguageGroupRow(
            group = group,
            onClick = { onAction(LocalePickerAction.GroupOpened(group.id)) },
            modifier = rowModifier,
        )
    }
}

private fun LazyListScope.groupLocaleItems(
    group: LocaleGroup,
    state: LocalePickerUiState,
    pinnedTags: Set<String>,
    onAction: (LocalePickerAction) -> Unit,
    onSelectLocale: (LocaleOption) -> Unit,
    canPin: Boolean,
    systemLocaleTag: String,
    contentModifier: Modifier,
    rowModifier: Modifier,
) {
    item(key = "group_header_${group.id}") {
        Column(
            modifier = contentModifier.padding(
                horizontal = 0.dp,
                vertical = AppSpacing.sm,
            ),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                AppSpacing.xs,
            ),
        ) {
            AppText(
                text = group.localizedLanguage,
                style = AppUiTheme.textStyles.pageTitle,
            )
            AppText(
                text = group.supportingLabel(),
                style = AppUiTheme.textStyles.metadata,
                color = AppUiTheme.palette.muted,
            )
            LanguageSortFilterBar(
                state = state,
                onAction = onAction,
                useVariantOptions = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    val candidates = state.candidateOptions(systemLocaleTag)
    if (candidates.isEmpty()) {
        // Same empty hint as the top-level search branch — a filtered-out group
        // must not reduce to a bare heading.
        item(key = "group_empty_results") {
            AppText(
                text = stringResource(R.string.no_language_results),
                style = AppUiTheme.textStyles.body,
                color = AppUiTheme.palette.muted,
                modifier = contentModifier.padding(vertical = AppSpacing.lg),
            )
        }
    } else {
        items(
            items = candidates,
            key = { "region_${it.languageTag}" },
            contentType = { "locale" },
        ) { locale ->
            val isPinned = locale.languageTag in pinnedTags
            LocaleOptionRow(
                locale = locale,
                selected = locale.languageTag == state.selectedLanguageTag,
                isPinned = isPinned,
                canPin = canPin,
                onPinAction = {
                    onAction(
                        if (isPinned) {
                            LocalePickerAction.UnpinClicked(locale)
                        } else {
                            LocalePickerAction.PinClicked(locale)
                        },
                    )
                },
                onClick = { onSelectLocale(locale) },
                modifier = rowModifier,
            )
        }
    }
}

private fun LazyListScope.searchResultItems(
    state: LocalePickerUiState,
    pinnedTags: Set<String>,
    onAction: (LocalePickerAction) -> Unit,
    onSelectLocale: (LocaleOption) -> Unit,
    canPin: Boolean,
    systemLocaleTag: String,
    contentModifier: Modifier,
    rowModifier: Modifier,
) {
    val query = state.query.trim()
    val results = state.localeGroups
        .asSequence()
        .flatMap { group ->
            group.options.asSequence().filter { option ->
                group.language.contains(query, ignoreCase = true) ||
                    group.localizedLanguage.contains(query, ignoreCase = true) ||
                    option.displayName.contains(query, ignoreCase = true) ||
                    option.localizedDisplayName.contains(query, ignoreCase = true) ||
                    option.languageTag.contains(query, ignoreCase = true)
            }
        }
        .distinctBy { it.languageTag }
        .toList()
        .let { options -> state.sortedLocaleVariants(options, systemLocaleTag) }

    item(key = "header_search_results") {
        SectionHeader(
            text = stringResource(R.string.language_search_results),
            modifier = contentModifier,
        )
    }
    if (results.isEmpty()) {
        item(key = "empty_search_results") {
            AppText(
                text = stringResource(R.string.no_language_results),
                style = AppUiTheme.textStyles.body,
                color = AppUiTheme.palette.muted,
                modifier = contentModifier.padding(vertical = AppSpacing.lg),
            )
        }
    } else {
        items(
            items = results,
            key = { "search_${it.languageTag}" },
            contentType = { "locale" },
        ) { locale ->
            val isPinned = locale.languageTag in pinnedTags
            LocaleOptionRow(
                locale = locale,
                selected = locale.languageTag == state.selectedLanguageTag,
                isPinned = isPinned,
                canPin = canPin,
                onPinAction = {
                    onAction(
                        if (isPinned) {
                            LocalePickerAction.UnpinClicked(locale)
                        } else {
                            LocalePickerAction.PinClicked(locale)
                        },
                    )
                },
                onClick = { onSelectLocale(locale) },
                modifier = rowModifier,
            )
        }
    }
}

/**
 * The language directory's single-row sort chips (round-5 019-A2): the same
 * shape as the home filter bar — one horizontal chip row, tri-state cycling
 * (unselected → ascending → descending → back to Recommended with no chip
 * selected), the active direction arrow ahead of the label, no vertical row
 * padding, and no dropdown or separate direction button.
 */
@Composable
internal fun LanguageSortFilterBar(
    state: LocalePickerUiState,
    onAction: (LocalePickerAction) -> Unit,
    useVariantOptions: Boolean,
    modifier: Modifier = Modifier,
) {
    val nameLabel = stringResource(R.string.language_sort_name)
    val autonymLabel = stringResource(R.string.language_sort_autonym)
    val tagLabel = stringResource(R.string.language_sort_tag)
    val directionNone = stringResource(R.string.sort_direction_none)
    val directionAscending = stringResource(R.string.sort_direction_ascending)
    val directionDescending = stringResource(R.string.sort_direction_descending)
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item(key = "sort_name") {
            LanguageSortChip(
                label = nameLabel,
                selected = currentSortOption(state, useVariantOptions) ==
                    SortChipOption.LocalizedName,
                ascending = currentSortAscending(state, useVariantOptions),
                directionNone = directionNone,
                directionAscending = directionAscending,
                directionDescending = directionDescending,
                onClick = {
                    onAction(
                        cycleSortAction(
                            SortChipOption.LocalizedName,
                            useVariantOptions,
                        ),
                    )
                },
            )
        }
        item(key = "sort_autonym") {
            LanguageSortChip(
                label = autonymLabel,
                selected = currentSortOption(state, useVariantOptions) ==
                    SortChipOption.NativeName,
                ascending = currentSortAscending(state, useVariantOptions),
                directionNone = directionNone,
                directionAscending = directionAscending,
                directionDescending = directionDescending,
                onClick = {
                    onAction(
                        cycleSortAction(
                            SortChipOption.NativeName,
                            useVariantOptions,
                        ),
                    )
                },
            )
        }
        item(key = "sort_tag") {
            LanguageSortChip(
                label = tagLabel,
                selected = currentSortOption(state, useVariantOptions) ==
                    SortChipOption.LanguageTag,
                ascending = currentSortAscending(state, useVariantOptions),
                directionNone = directionNone,
                directionAscending = directionAscending,
                directionDescending = directionDescending,
                onClick = {
                    onAction(
                        cycleSortAction(
                            SortChipOption.LanguageTag,
                            useVariantOptions,
                        ),
                    )
                },
            )
        }
    }
}

/** The three chip-exposed sort facets shared by both sort levels. */
private enum class SortChipOption { LocalizedName, NativeName, LanguageTag }

private fun currentSortOption(
    state: LocalePickerUiState,
    useVariantOptions: Boolean,
): SortChipOption? = when (useVariantOptions) {
    true -> when (state.variantSortOption) {
        LocaleVariantSortOption.LocalizedName -> SortChipOption.LocalizedName
        LocaleVariantSortOption.NativeName -> SortChipOption.NativeName
        LocaleVariantSortOption.LanguageTag -> SortChipOption.LanguageTag
        else -> null
    }

    false -> when (state.groupSortOption) {
        LanguageGroupSortOption.LocalizedName -> SortChipOption.LocalizedName
        LanguageGroupSortOption.NativeName -> SortChipOption.NativeName
        LanguageGroupSortOption.LanguageTag -> SortChipOption.LanguageTag
        else -> null
    }
}

private fun currentSortAscending(
    state: LocalePickerUiState,
    useVariantOptions: Boolean,
): Boolean = if (useVariantOptions) state.variantSortAscending else state.groupSortAscending

private fun cycleSortAction(
    option: SortChipOption,
    useVariantOptions: Boolean,
): LocalePickerAction = when (option) {
    SortChipOption.LocalizedName ->
        if (useVariantOptions) {
            LocalePickerAction.CycleVariantSort(LocaleVariantSortOption.LocalizedName)
        } else {
            LocalePickerAction.CycleGroupSort(LanguageGroupSortOption.LocalizedName)
        }

    SortChipOption.NativeName ->
        if (useVariantOptions) {
            LocalePickerAction.CycleVariantSort(LocaleVariantSortOption.NativeName)
        } else {
            LocalePickerAction.CycleGroupSort(LanguageGroupSortOption.NativeName)
        }

    SortChipOption.LanguageTag ->
        if (useVariantOptions) {
            LocalePickerAction.CycleVariantSort(LocaleVariantSortOption.LanguageTag)
        } else {
            LocalePickerAction.CycleGroupSort(LanguageGroupSortOption.LanguageTag)
        }
}

@Composable
private fun LanguageSortChip(
    label: String,
    selected: Boolean,
    ascending: Boolean,
    directionNone: String,
    directionAscending: String,
    directionDescending: String,
    onClick: () -> Unit,
) {
    AppFilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        // miuix has no single-direction arrow glyphs; the sort direction
        // vectors stay project assets drawn by the chip's backend icon
        // control (asset exception, see the 012 execution record).
        leadingIcon = if (selected) {
            if (ascending) {
                Icons.Outlined.ArrowUpward
            } else {
                Icons.Outlined.ArrowDownward
            }
        } else {
            null
        },
        modifier = Modifier.semantics {
            stateDescription = when {
                !selected -> directionNone
                ascending -> directionAscending
                else -> directionDescending
            }
        },
    )
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    AppText(
        text = text,
        style = AppComponentDefaults.titleStyle(),
        color = AppUiTheme.palette.accent,
        modifier = modifier.padding(
            horizontal = 0.dp,
            vertical = AppSpacing.sm,
        ),
    )
}

/**
 * A language group entry. Same frame and anatomy as the locale choice row
 * (round-7 028): the host's 4dp outer margin, start 12 / end 0 internal padding,
 * 72dp minimum height and the shared 48dp terminal slot with an explicit 24dp
 * glyph — only the terminal action is a forward chevron instead of a check.
 */
@Composable
private fun LanguageGroupRow(
    group: LocaleGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppLayout.localeChoiceRowMinHeight)
            .clip(RoundedCornerShape(AppComponentDefaults.rowCornerRadius))
            .clickable(onClick = onClick)
            .padding(
                start = AppSpacing.md,
                top = AppSpacing.md,
                end = 0.dp,
                bottom = AppSpacing.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LocaleBadge(languageTag = group.id, preferRegion = false)
        Spacer(Modifier.width(AppSpacing.md))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            AppText(text = group.localizedLanguage, style = AppComponentDefaults.titleStyle(), maxLines = 2)
            AppText(
                text = group.supportingLabel(),
                style = AppComponentDefaults.metadataStyle(),
                color = AppUiTheme.palette.muted,
                maxLines = 2,
            )
        }
        Box(Modifier.size(AppLayout.appListSelectionSlotWidth), contentAlignment = Alignment.Center) {
            AppIcon(
                imageVector = AppSymbolVector(AppSymbol.Forward),
                contentDescription = null,
                tint = AppUiTheme.palette.muted,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun LocaleOptionRow(
    locale: LocaleOption,
    selected: Boolean,
    isPinned: Boolean,
    canPin: Boolean,
    onPinAction: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LocaleSelectionRow(
        text = locale.localizedDisplayName,
        supportingText = locale.supportingLabel(),
        selected = selected,
        onClick = onClick,
        leading = {
            LocaleBadge(
                languageTag = locale.languageTag,
                preferRegion = true,
            )
        },
        trailing = if (canPin) {
            {
                // Real 48dp touch slot with an explicit 24dp glyph (round-7 028):
                // the slot stays a full touch target; only the icon is 24dp.
                AppIconButton(
                    onClick = onPinAction,
                    modifier = Modifier.size(AppLayout.appListSelectionSlotWidth),
                ) {
                    AppIcon(
                        imageVector = if (isPinned) {
                            AppSymbolVector(AppSymbol.PinActive)
                        } else {
                            AppSymbolVector(AppSymbol.Pin)
                        },
                        contentDescription = stringResource(
                            if (isPinned) R.string.unpin else R.string.pin,
                        ),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        } else {
            null
        },
        modifier = modifier,
    )
}

/** Compat forwarder: the shared choice row owns the layout, selection and slots. */
@Composable
private fun LocaleSelectionRow(
    text: String,
    supportingText: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    AppLocaleChoiceRow(
        title = text,
        subtitle = supportingText,
        selected = selected,
        onSelect = onClick,
        modifier = modifier,
        leading = leading,
        trailingAction = trailing,
    )
}

@Composable
private fun LocaleGroup.supportingLabel(): String =
    "$language · ${stringResource(R.string.locale_variant_count, options.size)}"

private fun LocaleOption.supportingLabel(): String = "$languageTag · $displayName"
