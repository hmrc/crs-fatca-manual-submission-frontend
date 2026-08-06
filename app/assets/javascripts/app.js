
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
        // When the user clears the autocomplete input, reset the underlying
        // <select> so the form submits an empty country value.
        var autocompleteInput = document.getElementById('country');

        if (autocompleteInput) {
            autocompleteInput.addEventListener('input', function () {
                if (!this.value || !this.value.trim()) {
                    countrySelect.value = '';
                    countrySelect.selectedIndex = 0;
                }
            });
        }

       var enhancedCurrencyInput = document.getElementById('country');
        if (enhancedCurrencyInput) {
            enhancedCurrencyInput.setAttribute('spellcheck', 'false');
        }
})();

(function () {
    'use strict';

    var currencySelect = document.querySelector('select#currency');

    if (
        currencySelect === null ||
        typeof HMRCAccessibleAutocomplete === 'undefined'
    ) {
        return;
    }

    currencySelect.removeAttribute('data-module');

    var currencyOptions = currencySelect.options;

    function getDataText(option) {
        return option.getAttribute('data-text') || option.textContent || '';
    }

    function getDisplayName(option) {
        var dataText = getDataText(option);

        return dataText.split(':')[0].trim();
    }

    function getDefaultValue() {
        var selectedOption =
            currencySelect.options[currencySelect.selectedIndex];

        if (!selectedOption || !selectedOption.value) {
            return '';
        }

        return getDisplayName(selectedOption);
    }

    HMRCAccessibleAutocomplete.enhanceSelectElement({
        selectElement: currencySelect,

        defaultValue: getDefaultValue(),

        showAllValues:
            currencySelect.getAttribute('data-show-all-values') === 'true',

        autoselect:
            currencySelect.getAttribute('data-auto-select') === 'true',

        minLength: 2,

        source: function (query, syncResults) {
            var normalisedQuery =
                (query || '').toLowerCase().trim();

            var matches = [];

            if (!normalisedQuery) {
                syncResults(matches);
                return;
            }

            for (var i = 0; i < currencyOptions.length; i++) {
                var option = currencyOptions[i];

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
                currencySelect.value = '';
                return;
            }

            for (var i = 0; i < currencyOptions.length; i++) {
                var option = currencyOptions[i];

                if (!option.value) {
                    continue;
                }

                var displayName =
                    getDisplayName(option);

                if (displayName === selected) {
                    currencySelect.value = option.value;
                    option.selected = true;

                    currencySelect.dispatchEvent(
                        new Event('change', {
                            bubbles: true
                        })
                    );

                    return;
                }
            }
        }
    });
       var enhancedCurrencyInput = document.getElementById('currency');
        if (enhancedCurrencyInput) {
            enhancedCurrencyInput.setAttribute('spellcheck', 'false');
        }
})();
