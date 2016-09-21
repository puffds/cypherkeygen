const execSync = require('child_process').execSync;
const stdin = process.stdin;

execSync("set JAVA_HOME=\"D:\\Programs\\Java\\jdk1.6.0_45\"", {
  cwd: '..',
  stdio:[0,1,2]
});
execSync("mvn assembly:assembly", {
  cwd: '..',
  stdio:[0,1,2]
});
execSync("launch4jc assets/lcfg.xml", {
  stdio:[0,1,2]
});

console.log('Press any key to exit...');
stdin.setRawMode(true);
stdin.resume();
stdin.on('data', process.exit.bind(process, 0));
