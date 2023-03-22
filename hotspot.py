import threading
import subprocess
from time import sleep

class HotSpot () :
    
    def __init__ (self ) :
        self.stdout = None
        self.stderr = None
        threading.Thread.__init__(self)
    
    def start(self) -> None:
        p = subprocess.Popen(
            'sudo systemctl start hostapd'.split(" "),
            shell=False,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL)
        p.wait(timeout=10)
    
    def stop (self) -> None :
        p = subprocess.Popen(
            'sudo systemctl stop hostapd'.split(" "),
            shell=False,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL)
        p.wait(timeout=10)

    def isRunning (self) ->bool : 
        p = subprocess.Popen(
            'sudo systemctl status hostapd'.split(" "),
            shell=False,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE)
        out, _ = p.communicate()
        try : 
            res = str(out).split("\\n")[2].split(":")[1].split()[0]
            return True if res == "active" else False
        except Exception as e :
            print(e)
            return None



if __name__ == "__main__" : 
    h = HotSpot()
    h.start()


