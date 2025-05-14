$libPath = "lib"

$mavenInstallCmds = @(
    "mvn install:install-file -Dfile=$libPath\commons-csv-1.10.0.jar -DgroupId=org.apache.commons -DartifactId=commons-csv -Dversion=1.10.0 -Dpackaging=jar",
    "mvn install:install-file -Dfile=$libPath\commons-logging-1.2.jar -DgroupId=commons-logging -DartifactId=commons-logging -Dversion=1.2 -Dpackaging=jar",
    "mvn install:install-file -Dfile=$libPath\itextpdf-5.5.13.2.jar -DgroupId=com.itextpdf -DartifactId=itextpdf -Dversion=5.5.13.2 -Dpackaging=jar",
    "mvn install:install-file -Dfile=$libPath\json-20240303.jar -DgroupId=org.json -DartifactId=json -Dversion=20240303 -Dpackaging=jar",
    "mvn install:install-file -Dfile=$libPath\jxmapviewer2-2.6.jar -DgroupId=jxmapviewer2 -DartifactId=jxmapviewer2 -Dversion=2.6 -Dpackaging=jar",
    "mvn install:install-file -Dfile=$libPath\jcommon-1.0.12.jar -DgroupId=jfree -DartifactId=jcommon -Dversion=1.0.12 -Dpackaging=jar",
    "mvn install:install-file -Dfile=$libPath\jfreechart-1.0.9.jar -DgroupId=jfree -DartifactId=jfreechart -Dversion=1.0.9 -Dpackaging=jar"
)

foreach ($cmd in $mavenInstallCmds) {
    Write-Host "▶️ Executing: $cmd" -ForegroundColor Cyan
    iex $cmd
}
