// require modules
var fs = require('fs');
var archiver = require('archiver');
var config = require('./package.json');

// create a file to stream archive data to.
var output = fs.createWriteStream(__dirname + '/build/' + config.name + '-' + config.version + '.zip');
var archive = archiver('zip', {
  store: true // Sets the compression method to STORE.
});

// listen for all archive data to be written
output.on('close', function() {
  console.log(archive.pointer() + ' total bytes');
});

// good practice to catch this error explicitly
archive.on('error', function(err) {
  throw err;
});

// pipe archive data to the file
archive.pipe(output);

// append files
archive.directory('dist/', '');

// finalize the archive (ie we are done appending files but streams have to finish yet)
archive.finalize();
