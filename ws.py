import asyncio
from asyncio import events
import functools
from http.client import HTTPException
from typing import Callable, Dict
from urllib import response
import websockets
import json



class WebSocketServer :

    def __init__ (self) :
        self.events : Dict[str, Callable[[any, Dict], None]]= {}

    async def start(self, port) :
        async with websockets.serve(self.handler, "", port):
            await asyncio.Future()
    
    async def handler (self, websocket) :
        while True : 
                        
            try :

                message = await websocket.recv()

                print (message)
                # parse the message to json
                try :
                    data = self.parse(message)
                    event = data.get("event")
                except :
                    await self.sender({
                        "status_code" : 400,
                        "message" : "unprocessable entity"
                    }, websocket)
                    continue

                # find a handler for the message and return its response
                try :
                    reply = await self.events.get(event) (websocket, data)
                    if reply == None : continue
                    response = {
                        "status_code": 200,
                        "event" : event,
                        "message" : reply
                    }
                except HTTPException as e :
                    response = {
                        "status_code": 400,
                        "event": event,
                        "message" : str(e)
                    }
                
                await self.sender(response, websocket)

            except websockets.ConnectionClosedOK : 
                print("Connection Terminated !")
                self.events.get("terminate")
                break
            except websockets.exceptions.ConnectionClosedError: 
                print("The connection was close unexpectedly ...")
                self.events.get("terminate")
                break            

    async def sender (self, data:dict, websocket) : 
        print(json.dumps(data))
        await websocket.send(json.dumps(data))

    def parse (self, msg : str) -> Dict[str, any]:
        try :
            data : dict = json.loads(msg)
            if data.get("event") == None :  return None
            assert data.get("event") in self.events.keys()
            return data

        except Exception as e : 
            raise HTTPException("Invalid message format")
    
    def registerEvent (self, event: str) :      
        def wrapper (func) :
            self.events[event] = func
            return func 

        return wrapper
    
    


if __name__ == "__main__" : 
    asyncio.run()
