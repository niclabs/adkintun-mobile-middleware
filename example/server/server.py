import bottle
import os
from bottle import Bottle, static_file, request
from datetime import datetime

app = Bottle()

@app.route('')
@app.route('/')
def index():
  return static_file('index.html', root='web')

@app.route('/:path#.+#', name='/')
def send_static(path):
    """Configure static routes"""
    return static_file(path, root='web')


@app.route('/upload', method='POST')
def do_upload():
    upload = request.files.get('measurements')
    name, ext = os.path.splitext(upload.filename)
    if ext not in ('.gz'):
        return "File extension not allowed."

    filename = '%s.%s.%s.%s.%s.gz' % (request.forms.get('data_type'), 
        request.forms.get('transport'), 
        request.forms.get('journey_id'), 
        datetime.now().strftime('%Y-%m-%d.%H%M%S'), 
        request.forms.get('device_id'))

    save_path = "./data/"
    if not os.path.exists(save_path):
        os.makedirs(save_path)

    file_path = "{path}/{file}".format(path=save_path, file=filename)
    upload.save(file_path)
    return "File successfully saved to '{0}'.".format(save_path)

###########################################
# Web errors
###########################################

@app.error(404)
def Error404(code):
    return static_file('404.html', root='web')


############################################
# Application execution
############################################

# The application execution goes at the end so all routes are loaded
def main():
    bottle.run(app=app, host='0.0.0.0')

if __name__ == '__main__':
    main()