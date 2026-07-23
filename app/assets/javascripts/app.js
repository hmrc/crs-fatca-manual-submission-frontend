(function () {
    'use strict';

    function initialiseCountryAutocomplete() {
        var countrySelect = document.querySelector('select#country');

        if (countrySelect === null) {
            return;
        }

        if (
            typeof window.HMRCAccessibleAutocomplete === 'undefined' ||
            countrySelect.getAttribute('data-country-autocomplete-initialised') === 'true'
        ) {
            return;
        }

        countrySelect.removeAttribute('data-module');

        var countryOptions = countrySelect.options;

        function getSearchTerms(option) {
            var dataText = option.getAttribute('data-text');
            var rawTerms = dataText
                ? dataText.split(':')
                : [option.textContent || ''];

            var terms = [];

            for (var i = 0; i < rawTerms.length; i++) {
                var term = rawTerms[i].trim();

                if (term && terms.indexOf(term) === -1) {
                    terms.push(term);
                }
            }

            return terms;
        }
        function getDisplayName(option) {
            var terms = getSearchTerms(option);

            return terms.length > 0
                ? terms[0]
                : option.textContent.trim();
        }

        function getDefaultValue() {
            var selectedOption =
                countrySelect.options[countrySelect.selectedIndex];

            if (!selectedOption || !selectedOption.value) {
                return '';
            }

            return getDisplayName(selectedOption);
        }

        window.HMRCAccessibleAutocomplete.enhanceSelectElement({
            selectElement: countrySelect,
            defaultValue: getDefaultValue(),
            showAllValues:
                countrySelect.getAttribute('data-show-all-values') === 'true',
            autoselect:
                countrySelect.getAttribute('data-auto-select') === 'true',
            minLength: 2,

            source: function (query, syncResults) {
                var normalisedQuery = (query || '').toLowerCase().trim();

                if (!normalisedQuery) {
                    syncResults([]);
                    return;
                }

                var exactAliasMatches = [];
                var partialMatches = [];

                for (var i = 0; i < countryOptions.length; i++) {
                    var option = countryOptions[i];

                    if (!option.value) {
                        continue;
                    }

                    var searchTerms = getSearchTerms(option);
                    var displayName = getDisplayName(option);
                    var exactAliasMatch = false;
                    var partialMatch = false;

                    for (var j = 0; j < searchTerms.length; j++) {
                        var normalisedTerm = searchTerms[j]
                            .toLowerCase()
                            .trim();

                        if (
                            j > 0 &&
                            normalisedTerm === normalisedQuery
                        ) {
                            exactAliasMatch = true;
                        }

                        if (
                            normalisedTerm.indexOf(normalisedQuery) !== -1
                        ) {
                            partialMatch = true;
                        }
                    }

                    if (
                        exactAliasMatch &&
                        exactAliasMatches.indexOf(displayName) === -1
                    ) {
                        exactAliasMatches.push(displayName);
                    } else if (
                        partialMatch &&
                        partialMatches.indexOf(displayName) === -1
                    ) {
                        partialMatches.push(displayName);
                    }
                }

                syncResults(
                    exactAliasMatches.length > 0
                        ? exactAliasMatches
                        : partialMatches
                );
            },

            onConfirm: function (selected) {
                var selectedDisplayName =
                    typeof selected === 'string'
                        ? selected
                        : '';

                if (!selectedDisplayName) {
                    countrySelect.value = '';
                    return;
                }

                for (var i = 0; i < countryOptions.length; i++) {
                    var option = countryOptions[i];
                    var candidate = getDisplayName(option);

                    if (candidate === selectedDisplayName) {
                        countrySelect.value = option.value;
                        option.selected = true;
                        return;
                    }
                }
            }
        });

        countrySelect.setAttribute(
            'data-country-autocomplete-initialised',
            'true'
        );
    }

    if (document.querySelector('select#country') !== null) {
        initialiseCountryAutocomplete();
    } else {
        document.addEventListener(
            'DOMContentLoaded',
            initialiseCountryAutocomplete
        );
    }
})();