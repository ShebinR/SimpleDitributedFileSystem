import subprocess
import sys
import time

pid_command = ["sh",  "./pid.sh"]
kill_command = ["sh", "./kill.sh"]
pause_command = ["sh", "./pause.sh"]
resume_command = ["sh", "./cont.sh"]

def executeCommand(command):
    p = subprocess.Popen(command, stdout=subprocess.PIPE)
    out = p.communicate()
    return out[0].rstrip()

def main():
    while True:
        print("(1)Process ID (2)Pause (3)Resume (4)Kill")
        option = raw_input("Opt : ")
        option = int(option)
        server_name = ""
        if option is 1:
            server_name = raw_input("Server : ")
            command = pid_command
            command.append(server_name)
            print(executeCommand(command))
        elif option is 2:
            server_name = raw_input("Server : ")
            command = pause_command
            command.append(server_name)
            print(executeCommand(command))
        elif option is 3:
            server_name = raw_input("Server : ")
            command = resume_command
            command.append(server_name)
            print(executeCommand(command))
        elif option is 4:
            server_name = raw_input("Server : ")
            command = kill_command
            command.append(server_name)
            print(executeCommand(command))

main()
