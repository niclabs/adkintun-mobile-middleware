import os
import logging
from logging.handlers import RotatingFileHandler
from flask import Flask, abort, send_from_directory, request
from datetime import datetime

# Static files directory
STATIC_FOLDER = 'web'
UPLOAD_FOLDER = 'data'
ALLOWED_EXTENSIONS = set(['gz'])

app = Flask(__name__, static_folder=STATIC_FOLDER, static_url_path='')
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Logging
handler = RotatingFileHandler('application.log', maxBytes=10000, backupCount=1)
handler.setLevel(logging.INFO)
app.logger.addHandler(handler)


class WSGITransferEncodingChunked:
    def __init__(self, app):
        self.app = app


    def __call__(self, environ, start_response):
        from cStringIO import StringIO
        input = environ.get('wsgi.input')
        length = environ.get('CONTENT_LENGTH', '0')
        length = 0 if length == '' else int(length)
        body = ''
        if length == 0:
            if input is None:
                return
            if environ.get('HTTP_TRANSFER_ENCODING','0') == 'chunked':
                size = int(input.readline(),16)
                while size > 0:
                    body += input.read(size+2)
                    size = int(input.readline(),16)

        else:
            body = environ['wsgi.input'].read(length)

        environ["CONTENT_LENGTH"] = str(len(body))
        environ['wsgi.input'] = StringIO(body)

        return self.app(environ, start_response)


# Setup wsgi app
app.wsgi_app = WSGITransferEncodingChunked(app.wsgi_app)


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS


@app.route('/')
def index():
    return send_from_directory(STATIC_FOLDER, 'index.html')


@app.route('/upload', methods=['POST'])
def upload_file():
    if request.method == 'POST':
        file = request.files.get('measurements')
        if file and allowed_file(file.filename):
            filename = '%s.%s.%s.%s.%s.gz' % (request.form.get('data_type'), 
                request.form.get('transport'), 
                request.form.get('journey_id'), 
                datetime.now().strftime('%Y-%m-%d.%H%M%S'), 
                request.form.get('device_id'))

            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            return "File successfully saved to '%s'." % filename
        else:
            abort(400)

    return send_from_directory(STATIC_FOLDER, 'upload.html')


@app.errorhandler(404)
def page_not_found(error):
    return send_from_directory(STATIC_FOLDER, '404.html'), 404


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8080)