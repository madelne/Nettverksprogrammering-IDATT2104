import sys
import tempfile
import os
import subprocess
from flask import Flask, request, jsonify, send_from_directory

app = Flask(__name__, static_folder='frontend')


@app.route('/')
def index():
    return send_from_directory(app.static_folder, 'index.html')


@app.route('/<path:path>')
def static_file(path):
    return send_from_directory(app.static_folder, path)


@app.route('/run-code', methods=['POST'])
def run_code():
    print("Received request")
    data = request.json
    code = data['code']
    language = data['language']
    result = {"output": "", "error": ""}

    try:
        with tempfile.TemporaryDirectory() as tmpdirname:
            dockerfile_content = ""
            code_filename = ""
            run_command = []

            if language == "python":
                dockerfile_content = """
                FROM python:3.9
                WORKDIR /code
                COPY . /code
                CMD ["python", "script.py"]
                """
                code_filename = "script.py"

            # Write the user's code to a file
            with open(os.path.join(tmpdirname, code_filename), "w") as file:
                file.write(code)

            # Write the Dockerfile
            with open(os.path.join(tmpdirname, "Dockerfile"), "w") as dockerfile:
                dockerfile.write(dockerfile_content)

            # Build the Docker image
            image_tag = f"user_code_execution_{language}:latest"
            build_command = ["docker", "build", "-t", image_tag, tmpdirname]
            subprocess.run(build_command, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

            # Run the Docker container
            container_run_command = ["docker", "run", "--rm", image_tag]
            completed = subprocess.run(container_run_command, capture_output=True, text=True)

            result["output"] = completed.stdout
            result["error"] = completed.stderr

    except subprocess.CalledProcessError as e:
        result["error"] = f"Error during Docker operations: {str(e)}"
    except Exception as e:
        result["error"] = str(e)

    return jsonify(result)


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port='6969')
