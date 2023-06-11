from http.client import HTTPException
from multiprocessing import Process, Queue
from typing import Dict

from connector import Connector
from gui import GUI
from hotspot import HotSpot
from ws import WebSocketServer
import config
import random
import asyncio
import gui_events

# Event Dictionaries are passed
# {
#  "event" : <event-type>
#  "message" : <value-or-message>
# }
qGUI = Queue()

class User : 
    def __init__(self, host, port, email, username, credit_balance) -> None:
        self.host = host
        self.port = port
        self.username = username
        self.email = email
        self.credit_balance = credit_balance
        self.current_transaction : Dict[str, any] = None

class Bin : 
    AVAILABLE = 0
    ENGAGED = 1
    LID_CLOSED = 2
    LID_OPEN = 3
    ANALYZING = 4
    SERVICE = -1

    HTTP_FORMAT_EXCEPTION = HTTPException("Message Format unprocessable")
    HTTP_INVALID_REQUEST = HTTPException("Invalid Request")
    
    def __init__(self, logging=None) -> None:
        self.hotspot = HotSpot()
        self.hotspot.start()
        self.current_state = self.AVAILABLE
        self.api = Connector()
        self.init_api()
        self.ws = WebSocketServer()
        self.init_ws()
        self.current_user : User = None
        self.gui = GUI(qGUI)
        self.startGUI()

    def kill (self) :
        self.hotspot.stop()
    
    def startGUI(self) :
        self.gui_process = Process(target=self.gui.run)
        self.gui_process.start()
        self.qGUI_setSecreen(gui_events.SET_WELCOME_SCREEN)
    
    def qGUI_putEvent(self, event:str, message:any) :
        print("adding event, "+event)
        qGUI.put({
            "event": event,
            "message": message

        })
    
    def qGUI_updatevalue(self, name:str, value:any) :
        self.qGUI_putEvent(
            gui_events.UPDATE_VALUE,
            (name, value)
        )
    
    def qGUI_setSecreen (self, screen:str ):
        self.qGUI_putEvent(
            gui_events.SET_SCREEN,
            screen
        )
    
    def init_api(self) :
        self.api.authenticate()
        self.bin_info = self.api.get_info()
        self.qGUI_updatevalue(gui_events.BIN_UID, self.bin_info.get("uid"))
        self.waste_types: Dict[str, float] = {waste.get('type') : waste.get('credits') for waste in self.api.get_wastes()}
        rates = ""
        for waste, credits in self.waste_types.items() : 
            rates += f"{waste} -> {credits}/kg \n"
        self.qGUI_updatevalue(gui_events.BIN_CONVERSION_RATES, rates)

    
    def init_ws(self):

        @self.ws.registerEvent("initiate")
        async def register (websocket, message) :
            # Check if BIN is available 
            if not self.current_state == self.AVAILABLE :
                return {
                    "state": self.current_state
                }
            # Check if it is a valid username
            email = message.get("email")
            try: user_info = self.api.get_user(email)
            except Exception as e : 
                print(e)
                raise self.HTTP_FORMAT_EXCEPTION
            if not user_info : raise self.HTTP_INVALID_REQUEST
            self.current_user = User(
                host = websocket.remote_address[0],
                port = websocket.remote_address[1],
                email = email,
                username=user_info.get("name"),
                credit_balance=user_info.get("credit_balance")
            )
            self.current_state = self.ENGAGED
            
            self.qGUI_updatevalue(gui_events.USER_NAME, user_info.get("name"))
            self.qGUI_updatevalue(gui_events.USER_EMAIL, user_info.get("email"))
            self.qGUI_updatevalue(gui_events.USER_CREDITS, user_info.get("credit_balance"))
            self.qGUI_updatevalue(gui_events.USER_AADHAAR, user_info.get("aadhaar_id"))
            self.qGUI_setSecreen(gui_events.SET_INFO_SCREEN)

            return {
                "state": self.AVAILABLE,
                "uid": config.BIN_UID,
                "location": self.bin_info.get("address"),
                "waste_types" : self.waste_types
            }

        @self.ws.registerEvent("open_bin")
        async def open_bin(websocket, message) :
            # TODO : add functionality to open the bin lid.
            self.current_user.current_transaction = None
            self.qGUI_setSecreen(gui_events.SET_TRANSACTION_SCREEN)
            self.qGUI_updatevalue(gui_events.BIN_LID_STATE, "Open")
            self.qGUI_updatevalue(gui_events.MESSAGE_VARIABLE, "The Bin lid is now open")
            await asyncio.sleep(1)
            return True

        @self.ws.registerEvent("close_bin")
        async def close_bin(websocket, message) :
            # TODO : add functionality to close the bin lid
            self.qGUI_updatevalue(gui_events.BIN_LID_STATE, "Close")
            self.qGUI_updatevalue(gui_events.MESSAGE_VARIABLE, "The Bin Lid is now closed")
            await asyncio.sleep(1)
            return True

        @self.ws.registerEvent("terminate")       
        async def terminate(websocket, message) :
            self.current_state = self.AVAILABLE
            self.current_user = None
            self.qGUI_setSecreen(gui_events.SET_BYE_SCREEN)
            print("User has terminated !")
            await websocket.close()

        @self.ws.registerEvent("analyze_waste")
        async def analyze_waste(websocket, message) :
            cat = message.get("type")
            # TODO : add functionality to verify the waste
            self.qGUI_updatevalue(gui_events.MESSAGE_VARIABLE, "Analyzing the waste ...")
            self.qGUI_updatevalue(gui_events.TRANSACTION_TARGET_WASTE_TYPE, cat)
            await asyncio.sleep(2)
            if (not self.waste_types.get(cat)) : raise self.HTTP_FORMAT_EXCEPTION
            weight = random.random() * 1
            credits = self.waste_types.get(cat) * weight
            verification = True
            if verification : 
                self.qGUI_updatevalue(gui_events.TRANSACTION_WASTE_TYPE, cat)
                self.qGUI_updatevalue(gui_events.MESSAGE_VARIABLE, "The Waste Type has been Verified !")
            else : 
                self.qGUI_updatevalue(gui_events.TRANSACTION_WASTE_TYPE, "Mixed Waste")
                self.qGUI_updatevalue(gui_events.MESSAGE_VARIABLE, "The Waste Types cannot be confiremd !")

            self.qGUI_updatevalue(gui_events.TRANSACTION_WEIGHT, f"{round(weight*100, 2)} grams")
            self.qGUI_updatevalue(gui_events.TRANSACTION_CREDTIS, credits)
            self.qGUI_updatevalue(gui_events.TRANSACTION_STATE, "Verifeid")

            self.qGUI_setSecreen(gui_events.SET_ANALYSIS_RESULT_SCREEN)

            if verification : self.current_user.current_transaction = {
                "type" : cat,
                "weight" : weight
            }
            return {
                "verification": verification,
                "weight": weight,
                "credits": credits,
            }

        @self.ws.registerEvent("confirm_transaction")
        async def confirm_transaction(websocket, message) : 
            if self.current_user.current_transaction == None : raise self.HTTP_INVALID_REQUEST
            try :
                data =  self.api.add_transaction(
                    self.current_user.email,
                    self.current_user.current_transaction.get("type"),
                    self.current_user.current_transaction.get("weight"),
                )
                self.qGUI_updatevalue(gui_events.MESSAGE_VARIABLE, "The transaction has been completed succesfully")
                self.qGUI_updatevalue(gui_events.TRANSACTION_UID, data.get("uid"))
                self.qGUI_setSecreen(gui_events.SET_TRANSACTION_RECIEPT_SCREEN)

                return data
            except : 
                raise HTTPException("Could not process transaction !")     
   
if __name__ == "__main__" :
    bin = Bin()
    try :
        asyncio.run(bin.ws.start(8001))
    except KeyboardInterrupt : 
        bin.kill()





