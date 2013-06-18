@echo off & setlocal EnableDelayedExpansion
set cp=.;%java_home%\lib\tools.jar;%java_home%\lib\dt.jar;%java_home%\jre\lib\rt.jar
for %%A in (.\*.jar) do (
	set cp=!cp!;%%A
)

echo %cp%
#java -classpath "%cp%" bdagent.Server
@start javaw -classpath "%cp%" bdagent.Server
