package ing.fuyaoskyrocket.applocale.ui.appinfo

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ing.fuyaoskyrocket.applocale.data.repository.AppRepository
import ing.fuyaoskyrocket.applocale.data.repository.LocaleChangeNotifier
import ing.fuyaoskyrocket.applocale.data.repository.LocaleRepository
import ing.fuyaoskyrocket.applocale.data.system.AppIconLoader
import ing.fuyaoskyrocket.applocale.data.system.PackageDataSource
import ing.fuyaoskyrocket.applocale.model.LocaleOption
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerAction
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerEvent
import ing.fuyaoskyrocket.applocale.ui.languagepicker.LocalePickerUiState
import ing.fuyaoskyrocket.applocale.ui.languagepicker.cycledLanguageGroupSort
import ing.fuyaoskyrocket.applocale.ui.languagepicker.cycledLocaleVariantSort
import javax.inject.Inject

@HiltViewModel
class AppInfoViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val localeRepository: LocaleRepository,
    private val localeChangeNotifier: LocaleChangeNotifier,
    private val packageDataSource: PackageDataSource,
    val appIconLoader: AppIconLoader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppInfoUiState())
    val uiState: StateFlow<AppInfoUiState> = _uiState.asStateFlow()

    private val _pickerState = MutableStateFlow(LocalePickerUiState())
    val pickerState: StateFlow<LocalePickerUiState> = _pickerState.asStateFlow()

    private val _events = Channel<LocalePickerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var packageName: String = ""
    private var displayLocaleTag: String = ""

    fun initFromPackage(pkg: String) {
        val currentDisplayLocaleTag = localeRepository.currentDisplayLocaleTag()
        if (pkg == packageName && !_uiState.value.isLoading) {
            if (currentDisplayLocaleTag != displayLocaleTag) {
                refreshLocaleDisplayNames(currentDisplayLocaleTag)
            }
            return
        }
        packageName = pkg
        viewModelScope.launch {
            // App identity
            val info = appRepository.getApplicationInfo(pkg)
            val label = packageDataSource.getLabel(info)

            // Locale data
            val groups = localeRepository.getAllLocaleGroups()
            val systemOptions = localeRepository.getSystemLocaleOptions()
            val pinned = localeRepository.getPinnedLocales()
            val currentTag = localeRepository.getAppLocaleTag(pkg)
            displayLocaleTag = currentDisplayLocaleTag

            _uiState.update {
                AppInfoUiState(
                    packageName = pkg,
                    label = label,
                    currentLocaleTag = currentTag.takeIf { tag -> tag.isNotBlank() },
                    isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    isLoading = false,
                )
            }
            _pickerState.update {
                it.copy(
                    localeGroups = groups,
                    systemLocales = systemOptions,
                    pinnedLocales = pinned,
                    selectedLanguageTag = currentTag.takeIf { tag -> tag.isNotBlank() },
                    displayLocaleTag = currentDisplayLocaleTag,
                    isLoading = false,
                )
            }
        }
    }

    private fun refreshLocaleDisplayNames(currentDisplayLocaleTag: String) {
        displayLocaleTag = currentDisplayLocaleTag
        viewModelScope.launch {
            val groups = localeRepository.getAllLocaleGroups()
            _pickerState.update { current ->
                current.copy(
                    localeGroups = groups,
                    displayLocaleTag = currentDisplayLocaleTag,
                    systemLocales = localeRepository.localizeLocaleTags(
                        current.systemLocales.map(LocaleOption::languageTag),
                    ),
                    pinnedLocales = localeRepository.getPinnedLocales(),
                )
            }
        }
    }

    // ----------------------------------------------------- Locale selection

    fun onSelectLocale(option: LocaleOption) {
        viewModelScope.launch {
            localeRepository.setAppLocale(packageName, option.languageTag)
            refreshCurrentLocale()
            localeChangeNotifier.publish(listOf(packageName))
        }
    }

    fun onResetLocale() {
        viewModelScope.launch {
            localeRepository.setAppLocale(packageName, null)
            _uiState.update { it.copy(currentLocaleTag = null) }
            _pickerState.update { it.copy(selectedLanguageTag = null) }
            localeChangeNotifier.publish(listOf(packageName))
        }
    }

    private suspend fun refreshCurrentLocale() {
        val tag = localeRepository.getAppLocaleTag(packageName)
        val cleanTag = tag.takeIf { it.isNotBlank() }
        _uiState.update { it.copy(currentLocaleTag = cleanTag) }
        _pickerState.update { it.copy(selectedLanguageTag = cleanTag) }
    }

    // -------------------------------------------------------- Picker actions

    fun onPickerAction(action: LocalePickerAction) {
        when (action) {
            is LocalePickerAction.QueryChanged ->
                _pickerState.update { it.copy(query = action.query) }
            is LocalePickerAction.GroupOpened ->
                _pickerState.update { it.copy(selectedGroupId = action.groupId) }
            LocalePickerAction.BackToGroups ->
                _pickerState.update { it.copy(selectedGroupId = null) }
            is LocalePickerAction.CycleGroupSort -> {
                val (option, ascending) = cycledLanguageGroupSort(
                    currentOption = _pickerState.value.groupSortOption,
                    currentAscending = _pickerState.value.groupSortAscending,
                    tapped = action.option,
                )
                _pickerState.update {
                    it.copy(groupSortOption = option, groupSortAscending = ascending)
                }
            }
            is LocalePickerAction.CycleVariantSort -> {
                val (option, ascending) = cycledLocaleVariantSort(
                    currentOption = _pickerState.value.variantSortOption,
                    currentAscending = _pickerState.value.variantSortAscending,
                    tapped = action.option,
                )
                _pickerState.update {
                    it.copy(variantSortOption = option, variantSortAscending = ascending)
                }
            }
            is LocalePickerAction.PinClicked -> {
                localeRepository.pinLocale(action.option)
                _pickerState.update { it.copy(pinnedLocales = localeRepository.getPinnedLocales()) }
                viewModelScope.launch {
                    _events.send(LocalePickerEvent.Pinned(action.option.displayName))
                }
            }
            is LocalePickerAction.UnpinClicked -> {
                localeRepository.unpinLocale(action.option)
                _pickerState.update { it.copy(pinnedLocales = localeRepository.getPinnedLocales()) }
                viewModelScope.launch {
                    _events.send(LocalePickerEvent.Unpinned(action.option.displayName))
                }
            }
            is LocalePickerAction.LocaleSelected -> {
                // Handled by the screen via onSelectLocale/onResetLocale
            }
        }
    }

    // ----------------------------------------------------------- App actions

    fun getOpenIntent(): Intent? = packageDataSource.getLaunchIntent(packageName)

    fun getSettingsIntent(): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }

    fun forceStop() {
        viewModelScope.launch { localeRepository.forceStopPackage(packageName) }
    }
}
