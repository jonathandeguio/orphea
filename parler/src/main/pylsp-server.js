/* eslint-disable no-console */
const http = require('http');

const express = require('express');
const fs = require('fs');
const { exec } = require('child_process');

const ws = require('ws');
const url = require('url');
const bodyParser = require('body-parser');
const cors = require('cors');
const { launch } = require('./json-server-launcher');

const httpPort = 8999;
const SOCKETPATH = '/lsp/python';

const app = express();

// app.use(cors());

const httpServer = http.createServer(app);

const wss = new ws.Server({
  noServer: true,
  perMessageDeflate: false,
});

const server = httpServer.listen(httpPort, function () {
  const addr = server.address();
  console.log(
    `App listening on port ws://${addr.address}${httpPort}${SOCKETPATH}`
  );
});

app.get('/', (req, res) => {
  res.status(200).send('ok');
});

// Middleware for parsing JSON and urlencoded form data
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(cors());

app.post('/lsp/formatSQL', (req, res) => {
  // Get query parameters from the request
  const sql = atob(req.body.sql);
  const { dialect } = req.body;
  const tempFileName = `/tmp/temp_sql_${Date.now()}.sql`;

  // Write the SQL to the temporary file
  fs.writeFile(tempFileName, sql, (err) => {
    if (err) {
      console.error('Error writing SQL to file:', err);
    }

    exec(
      `sqlfluff fix ${tempFileName} -f -q -d ${dialect}`,
      (error, stdout, stderr) => {
        if (error) {
          fs.readFile(tempFileName, 'utf-8', (errF, fixedSql) => {
            if (err) {
              console.error('Error reading file:', errF);
            } else {
              res.status(200).send({
                status: 200,
                results: true,
                lintErrors: stderr,
                fixedSqlb64: btoa(fixedSql),
                // fixedSql,
              });
            }
          });

          // After processing, remove the temporary file
          fs.unlink(tempFileName, (unlinkErr) => {
            if (unlinkErr) {
              console.error('Error deleting temporary file:', unlinkErr);
            }
          });
          return res;
        }

        fs.readFile(tempFileName, 'utf-8', (errF, fixedSql) => {
          if (err) {
            console.error('Error reading file:', errF);
          } else {
            res.status(200).send({
              status: 200,
              results: true,
              lintErrors: stderr,
              fixedSqlb64: btoa(fixedSql),
              // fixedSql,
            });
          }
        });

        // After processing, remove the temporary file
        fs.unlink(tempFileName, (unlinkErr) => {
          if (unlinkErr) {
            console.error('Error deleting temporary file:', unlinkErr);
          }
        });
        return res;
      }
    );
  });
});

server.on('upgrade', (request, socket, head) => {
  console.log('server on upgrade');
  const pathname = request.url ? url.parse(request.url).pathname : undefined;
  console.log(`non secure  ${pathname}`);
  if (pathname === SOCKETPATH) {
    wss.handleUpgrade(request, socket, head, (webSocket) => {
      const socket2 = {
        send: (content) =>
          webSocket.send(content, (error) => {
            if (error) {
              throw error;
            }
          }),
        onMessage: (cb) => webSocket.on('message', cb),
        onError: (cb) => webSocket.on('error', cb),
        onClose: (cb) => webSocket.on('close', cb),
        dispose: () => webSocket.close(),
      };
      console.log({ state: webSocket.readyState, open: webSocket.OPEN });
      // launch the server when the web socket is opened
      if (webSocket.readyState === webSocket.OPEN) {
        launch(socket2);
      } else {
        webSocket.on('open', () => launch(socket));
      }
    });
  }
});
