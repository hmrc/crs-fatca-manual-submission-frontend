(function () {
    'use strict';

    var countrySelect = document.querySelector('select#country');

    if (
        countrySelect === null ||
        typeof HMRCAccessibleAutocomplete === 'undefined'
    ) {
        return;
    }

    countrySelect.removeAttribute('data-module');

    var countryOptions = countrySelect.options;

    function getDataText(option) {
        return option.getAttribute('data-text') || option.textContent || '';
    }

    function getDisplayName(option) {
        var dataText = getDataText(option);

        return dataText.split(':')[0].trim();
    }

    function getDefaultValue() {
        var selectedOption =
            countrySelect.options[countrySelect.selectedIndex];

        if (!selectedOption || !selectedOption.value) {
            return '';
        }

        return getDisplayName(selectedOption);
    }

    HMRCAccessibleAutocomplete.enhanceSelectElement({
        selectElement: countrySelect,

        defaultValue: getDefaultValue(),

        showAllValues:
            countrySelect.getAttribute('data-show-all-values') === 'true',

        autoselect:
            countrySelect.getAttribute('data-auto-select') === 'true',

        minLength: 2,

        source: function (query, syncResults) {
            var normalisedQuery =
                (query || '').toLowerCase().trim();

            var matches = [];

            if (!normalisedQuery) {
                syncResults(matches);
                return;
            }

            for (var i = 0; i < countryOptions.length; i++) {
                var option = countryOptions[i];

                if (!option.value) {
                    continue;
                }

                var searchPool =
                    getDataText(option).toLowerCase();

                var displayName =
                    getDisplayName(option);

                if (
                    searchPool.indexOf(normalisedQuery) !== -1 &&
                    matches.indexOf(displayName) === -1
                ) {
                    matches.push(displayName);
                }
            }

            syncResults(matches);
        },

        onConfirm: function (selected) {

            if (typeof selected !== 'string') {
                return;
            }

            if (!selected.trim()) {
                countrySelect.value = '';
                return;
            }

            for (var i = 0; i < countryOptions.length; i++) {
                var option = countryOptions[i];

                if (!option.value) {
                    continue;
                }

                var displayName =
                    getDisplayName(option);

                if (displayName === selected) {
                    countrySelect.value = option.value;
                    option.selected = true;

                    countrySelect.dispatchEvent(
                        new Event('change', {
                            bubbles: true
                        })
                    );

                    return;
                }
            }
        }
    });
})();