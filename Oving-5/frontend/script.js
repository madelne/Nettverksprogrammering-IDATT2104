document.getElementById('run-btn').addEventListener('click', function() {
    const code = document.getElementById('code-input').value;
    const language = document.getElementById('language-selector').value;
    fetch('http://127.0.0.1:6969/run-code', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ code, language }),
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('output').textContent = data.output || data.error;
    })
    .catch(error => {
        console.error('Error:', error);
    });
});

// Add an event listener to the language selector
document.getElementById('language-selector').addEventListener('change', function() {
    updateTextAreaWithHelloWorld(this.value);
});

function updateTextAreaWithHelloWorld(language) {
    const helloWorldExamples = {
        python: 'print("Hello, Python!")',
        
    };

    document.getElementById('code-input').value = helloWorldExamples[language] || '';
}

// Initialize the textarea with the default language's Hello World
updateTextAreaWithHelloWorld(document.getElementById('language-selector').value);