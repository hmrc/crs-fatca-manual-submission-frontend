var countrySelect = document.querySelector('select#country');
if (countrySelect !== null) {
    // Prevent HMRC module auto-init so we control initialisation ourselves
    countrySelect.removeAttribute('data-module');

    var countryOptions = countrySelect.options;

    HMRCAccessibleAutocomplete.enhanceSelectElement({
        defaultValue: '',
        selectElement: countrySelect,
        showAllValues: countrySelect.getAttribute('data-show-all-values') === 'true',
        autoselect: countrySelect.getAttribute('data-auto-select') === 'true',
        minLength: 2,
        source: function(query, syncResults) {
            var q = query.toLowerCase().trim();
            var matches = [];
            for (var i = 0; i < countryOptions.length; i++) {
                var opt = countryOptions[i];
                if (!opt.value) continue;
                var dataText = opt.getAttribute('data-text');
                // Use data-text (colon-separated aliases) for search,
                // but treat the first segment as the display name.
                var searchPool = dataText || opt.textContent;
                var displayName = dataText ? dataText.split(':')[0] : opt.textContent;
                if (searchPool.toLowerCase().indexOf(q) !== -1 && matches.indexOf(displayName) === -1) {
                    matches.push(displayName);
                }
            }
            syncResults(matches);
        },
        onConfirm: function(selected) {
            // Match the display name (first colon segment) back to the option
            for (var j = 0; j < countryOptions.length; j++) {
                var opt = countryOptions[j];
                if (!opt.value) continue;
                var dataText = opt.getAttribute('data-text');
                var candidate = dataText ? dataText.split(':')[0] : opt.textContent;
                if (candidate === selected) {
                    countrySelect.value = opt.value;
                    opt.selected = true;
                    return;
                }
            }
        }
    });
}

