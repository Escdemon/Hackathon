var deployer = require('nexus-deployer');
var config = require('./package.json');

var repoUrl = 'http://forge-ge.groupinfra.com/nexus/content/repositories/releases';
if (config.version.endsWith('-SNAPSHOT')) {
  repoUrl = 'http://forge-ge.groupinfra.com/nexus/content/repositories/snapshots';
}

var release = {
  groupId: 'com.cgi',
  artifactId: config.name,
  version: config.version,
  packaging: 'zip',
  /*
  auth: {
    username:'_NEXUS_REPO_USERNAME_',
    password:'_NEXUS_REPO_PASSWORD_'
  },
  */
  pomDir: 'build/pom',
  url: repoUrl,
  artifact: 'build/' + config.name + '-' + config.version + '.zip',
  cwd: ''
};


deployer.deploy(release, function(){
  // your async call back here 
  // done(); 
});
