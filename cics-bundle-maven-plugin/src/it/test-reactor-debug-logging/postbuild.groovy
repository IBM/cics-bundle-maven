File buildLog = new File(basedir, 'build.log')

assert buildLog.text.contains("[DEBUG] Wrote resource to war-0.0.1-SNAPSHOT.war")