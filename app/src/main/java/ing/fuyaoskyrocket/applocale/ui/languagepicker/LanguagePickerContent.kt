package ing.fuyaoskyrocket.applocale.ui.languagepicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import ing.fuyaoskyrocket.applocale.R
import ing.fuyaoskyrocket.applocale.model.LocaleGroup
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppLayout
import ing.fuyaoskyrocket.applocale.ui.designsystem.AppSpacing
import ing.fuyaoskyrocket.applocale.ui.components.marqueeOnOverflow
import ing.fuyaoskyrocket.applocale.ui.components.rememberSystemLocaleTag
import ing.fuyaoskyrocket.applocale.ui.components.LocaleBadge

/**
 * Reusable Material 3 language picker for both app detail and batch operations.
 *
 * The top level presents a real search field, system default, pinned/device locales, and
 * language groups. Search results are selectable locales rather than another level of groups.
 * Opening a group replaces the directory with a section heading and radio-style locale choices.
 */
@Composable
fun LanguagePickerContent(
    state: LocalePickerUiState,
    onAction: (LocalePickerAction) -> Unit,
    onResetToSystemDefault: () -> Unit,
    onSelectLocale: (LocaleOption) -> Unit,
    modifier: Modifier = Modifier,
    canPin: Boolean = true,
) {
    val listState = rememberLanguagePickerListState(
        state = state,
        firstVisibleItemIndex = 0,
    )
    val systemLocaleTag = rememberSystemLocaleTag()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = AppSpacing.screenCompact,
            top = AppSpacing.sm,
            end = AppSpacing.screenCompact,
            bottom = AppSpacing.xl,
        ),
    ) {
        languagePickerItems(
            state = state,
            onAction = onAction,
            onResetToSystemDefault = onResetToSystemDefault,
            onSelectLocale = onSelectLocale,
            canPin = canPin,
            systemLocaleTag = systemLocaleTag,
            contentModifier = Modifier.fillMaxWidth(),
        )
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
): LazyListState {
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

    return when {
        state.isInGroup -> groupState
        state.query.isNotBlank() -> searchState
        else -> directoryState
    }
}

/** Adds the same picker to an existing lazy list without nesting scroll containers. */
fun LazyListScope.languagePickerItems(
    state: LocalePickerUiState,
    onAction: (LocalePickerAction) -> Unit,
    onResetToSystemDefault: () -> Unit,
    onSelectLocale: (LocaleOption) -> Unit,
    canPin: Boolean = true,
    systemLocaleTag: String,
    contentModifier: Modifier = Modifier,
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
        )
        return
    }

    item(key = "language_search") {
        OutlinedTextField(
            value = state.query,
            onValueChange = { onAction(LocalePickerAction.QueryChanged(it)) },
            modifier = contentModifier.padding(
                vertical = AppSpacing.sm,
            ),
            placeholder = { Text(stringResource(R.string.search_languages)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                )
            },
            trailingIcon = if (state.query.isNotEmpty()) {
                {
                    IconButton(
                        onClick = { onAction(LocalePickerAction.QueryChanged("")) },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.clear),
                        )
                    }
                }
            } else {
                null
            },
            shape = MaterialTheme.shapes.extraLarge,
            singleLine = true,
        )
    }

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
            modifier = contentModifier,
        )
    }

    item(key = "language_sort_controls") {
        LanguageSortControls(
            state = state,
            onAction = onAction,
            useVariantOptions = state.query.isNotBlank(),
            modifier = contentModifier.padding(horizontal = AppSpacing.lg),
        )
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
        )
        return
    }

    val sortedPinnedLocales = state.sortedLocaleVariants(
        options = state.pinnedLocales,
        systemLocaleTag = systemLocaleTag,
    )
    if (sortedPinnedLocales.isNotEmpty()) {
        item(key = "header_pinned") {
            SectionHeader(
                text = stringResource(R.string.pinned),
                modifier = contentModifier,
            )
        }
        items(
            items = sortedPinnedLocales,
            key = { "pinned_${it.languageTag}" },
            contentType = { "locale" },
        ) { locale ->
            LocaleOptionRow(
                locale = locale,
                selected = locale.languageTag == state.selectedLanguageTag,
                isPinned = true,
                canPin = canPin,
                onPinAction = { onAction(LocalePickerAction.UnpinClicked(locale)) },
                onClick = { onSelectLocale(locale) },
                modifier = contentModifier,
            )
        }
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
                modifier = contentModifier,
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
            modifier = contentModifier,
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
) {
    item(key = "group_header_${group.id}") {
        Column(
            modifier = contentModifier.padding(
                horizontal = AppSpacing.lg,
                vertical = AppSpacing.sm,
            ),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                AppSpacing.xs,
            ),
        ) {
            Text(
                text = group.localizedLanguage,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = group.supportingLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LanguageSortControls(
                state = state,
                onAction = onAction,
                useVariantOptions = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    items(
        items = state.sortedLocaleVariants(group.options, systemLocaleTag),
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
            modifier = contentModifier,
        )
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
            Text(
                text = stringResource(R.string.no_language_results),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = contentModifier.padding(AppSpacing.lg),
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
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun LanguageSortControls(
    state: LocalePickerUiState,
    onAction: (LocalePickerAction) -> Unit,
    useVariantOptions: Boolean,
    modifier: Modifier = Modifier,
) {
    var showSortMenu by rememberSaveable(useVariantOptions) { mutableStateOf(false) }
    val currentLabel = if (useVariantOptions) {
        stringResource(state.variantSortOption.labelRes())
    } else {
        stringResource(state.groupSortOption.labelRes())
    }
    val ascending = if (useVariantOptions) {
        state.variantSortAscending
    } else {
        state.groupSortAscending
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            AssistChip(
                onClick = { showSortMenu = true },
                label = { Text(currentLabel) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = null,
                    )
                },
            )
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
            ) {
                if (useVariantOptions) {
                    LocaleVariantSortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(option.labelRes())) },
                            onClick = {
                                onAction(LocalePickerAction.VariantSortChanged(option))
                                showSortMenu = false
                            },
                            trailingIcon = {
                                if (option == state.variantSortOption) {
                                    Icon(Icons.Outlined.Check, contentDescription = null)
                                }
                            },
                        )
                    }
                } else {
                    LanguageGroupSortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(option.labelRes())) },
                            onClick = {
                                onAction(LocalePickerAction.GroupSortChanged(option))
                                showSortMenu = false
                            },
                            trailingIcon = {
                                if (option == state.groupSortOption) {
                                    Icon(Icons.Outlined.Check, contentDescription = null)
                                }
                            },
                        )
                    }
                }
            }
        }
        IconButton(
            onClick = {
                onAction(
                    if (useVariantOptions) {
                        LocalePickerAction.ToggleVariantSortDirection
                    } else {
                        LocalePickerAction.ToggleGroupSortDirection
                    },
                )
            },
        ) {
            Icon(
                imageVector = if (ascending) {
                    Icons.Outlined.ArrowUpward
                } else {
                    Icons.Outlined.ArrowDownward
                },
                contentDescription = stringResource(R.string.toggle_sort_direction),
            )
        }
    }
}

private fun LanguageGroupSortOption.labelRes(): Int = when (this) {
    LanguageGroupSortOption.Recommended -> R.string.sort_recommended
    LanguageGroupSortOption.LocalizedName -> R.string.sort_by_interface_language_name
    LanguageGroupSortOption.NativeName -> R.string.sort_by_native_language_name
    LanguageGroupSortOption.LanguageTag -> R.string.sort_by_language_tag
    LanguageGroupSortOption.VariantCount -> R.string.sort_by_variant_count
}

private fun LocaleVariantSortOption.labelRes(): Int = when (this) {
    LocaleVariantSortOption.Recommended -> R.string.sort_recommended
    LocaleVariantSortOption.LocalizedName -> R.string.sort_by_interface_language_name
    LocaleVariantSortOption.NativeName -> R.string.sort_by_native_language_name
    LocaleVariantSortOption.LanguageTag -> R.string.sort_by_language_tag
    LocaleVariantSortOption.Specificity -> R.string.sort_by_locale_specificity
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            horizontal = AppSpacing.lg,
            vertical = AppSpacing.sm,
        ),
    )
}

@Composable
private fun LanguageGroupRow(
    group: LocaleGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(
                text = group.localizedLanguage,
                modifier = Modifier.marqueeOnOverflow(),
                maxLines = 1,
                softWrap = false,
            )
        },
        supportingContent = {
            Text(group.supportingLabel())
        },
        leadingContent = {
            LocaleBadge(
                languageTag = group.id,
                preferRegion = false,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = modifier
            .heightIn(min = AppLayout.standardListItemMinHeight)
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
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
                IconButton(
                    onClick = onPinAction,
                    modifier = Modifier.offset(x = AppSpacing.md),
                ) {
                    Icon(
                        imageVector = if (isPinned) {
                            Icons.Filled.PushPin
                        } else {
                            Icons.Outlined.PushPin
                        },
                        contentDescription = stringResource(
                            if (isPinned) R.string.unpin else R.string.pin,
                        ),
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        } else {
            null
        },
        modifier = modifier,
    )
}

@Composable
private fun LocaleSelectionRow(
    text: String,
    supportingText: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val supportingColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    ListItem(
        modifier = modifier
            .heightIn(min = AppLayout.standardListItemMinHeight)
            .clip(MaterialTheme.shapes.large)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        headlineContent = {
            Text(
                text = text,
                modifier = Modifier.marqueeOnOverflow(),
                color = contentColor,
                maxLines = 1,
                softWrap = false,
            )
        },
        supportingContent = supportingText?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = supportingColor,
                )
            }
        },
        leadingContent = leading,
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                    AppSpacing.xs,
                ),
            ) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                    modifier = Modifier.offset(x = AppSpacing.md),
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
                trailing?.invoke()
            }
        },
        colors = ListItemDefaults.colors(containerColor = containerColor),
    )
}

@Composable
private fun LocaleGroup.supportingLabel(): String =
    "$id · $language · ${stringResource(R.string.locale_variant_count, options.size)}"

private fun LocaleOption.supportingLabel(): String = "$languageTag · $displayName"
