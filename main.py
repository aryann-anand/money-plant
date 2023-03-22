
from time import sleep
from hw_interface.weight_scale import get_weight


while True : 
    print(get_weight())
    sleep(0.1)