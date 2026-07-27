(function () {
    'use strict';

    var countrySelect = document.querySelector('select#country');

    if (
        countrySelect === null ||
        typeof window.HMRCAccessibleAutocomplete === 'undefined'
    ) {
        return;
    }

    countrySelect.removeAttribute('data-module');

    var countryOptions = countrySelect.options;
    var countries = [];

    function normalise(value) {
        return (value || '').toLowerCase().trim();
    }

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
        var searchTerms = getSearchTerms(option);

        return searchTerms.length > 0
            ? searchTerms[0]
            : option.textContent.trim();
    }

    for (var i = 0; i < countryOptions.length; i++) {
        var option = countryOptions[i];

        if (!option.value) {
            continue;
        }

        countries.push({
            name: getDisplayName(option),
            value: option.value,
            searchTerms: getSearchTerms(option)
        });
    }

    function getDefaultValue() {
        var selectedOption =
            countrySelect.options[countrySelect.selectedIndex];

        if (!selectedOption || !selectedOption.value) {
            return '';
        }

        return getDisplayName(selectedOption);
    }

    function findCountryByInput(value) {
        var normalisedValue = normalise(value);

        if (!normalisedValue) {
            return null;
        }

        for (var i = 0; i < countries.length; i++) {
            var country = countries[i];

            for (var j = 0; j < country.searchTerms.length; j++) {
                if (
                    normalise(country.searchTerms[j]) === normalisedValue
                ) {
                    return country;
                }
            }
        }

        return null;
    }

    function setSelectedCountry(country) {

        for (var i = 0; i < countryOptions.length; i++) {
            countryOptions[i].selected = false;
        }

        if (!country) {
            countrySelect.value = '';

            if (countryOptions.length > 0) {
                countryOptions[0].selected = true;
            }

            return;
        }

        countrySelect.value = country.value;

        for (var j = 0; j < countryOptions.length; j++) {
            if (countryOptions[j].value === country.value) {
                countryOptions[j].selected = true;
                break;
            }
        }

        /*
         * Dispatch a change event in case anything else on the page is
         * listening for changes to the original select.
         */
        countrySelect.dispatchEvent(
            new Event('change', {
                bubbles: true
            })
        );
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
            var normalisedQuery = normalise(query);

            if (!normalisedQuery) {
                syncResults([]);
                return;
            }

            var exactMatches = [];
            var partialMatches = [];

            for (var i = 0; i < countries.length; i++) {
                var country = countries[i];
                var exactMatch = false;
                var partialMatch = false;

                for (
                    var j = 0;
                    j < country.searchTerms.length;
                    j++
                ) {
                    var searchTerm =
                        normalise(country.searchTerms[j]);

                    if (searchTerm === normalisedQuery) {
                        exactMatch = true;
                    }

                    if (
                        searchTerm.indexOf(normalisedQuery) !== -1
                    ) {
                        partialMatch = true;
                    }
                }

                if (exactMatch) {
                    exactMatches.push(country);
                } else if (partialMatch) {
                    partialMatches.push(country);
                }
            }

            syncResults(
                exactMatches.length > 0
                    ? exactMatches
                    : partialMatches
            );
        },

        templates: {
            inputValue: function (country) {
                return country
                    ? country.name
                    : '';
            },

            suggestion: function (country) {
                return country.name;
            }
        },

        onConfirm: function (country) {
            setSelectedCountry(country || null);
        }
    });

    var autocompleteInput =
        document.querySelector('input#country');

    if (autocompleteInput !== null) {

        autocompleteInput.addEventListener(
            'input',
            function () {
                var currentlySelected =
                    countries.find(function (country) {
                        return (
                            country.value === countrySelect.value
                        );
                    });

                if (
                    currentlySelected &&
                    normalise(this.value) !==
                    normalise(currentlySelected.name)
                ) {
                    setSelectedCountry(null);
                }
            }
        );
    }


    if (countrySelect.form !== null) {
        countrySelect.form.addEventListener(
            'submit',
            function () {
                if (autocompleteInput === null) {
                    return;
                }

                var country =
                    findCountryByInput(autocompleteInput.value);

                setSelectedCountry(country);
            }
        );
    }
})();