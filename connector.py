from distutils.command.config import config
from email import header
from lib2to3.pgen2 import token
from typing import Dict, List
from urllib import response
import requests
import config

class Connector : 
    username = config.BIN_UID
    password = config.BIN_SECRET
    URL = config.API_BASE_URL

    def __init__(self) -> None:
        self.token:str = None
    
    def authenticate (self) -> str :
        response = requests.post(
            self.URL + "/auth/token",
            data={
                "username": self.username,
                "password": self.password
            }
        )
        print(response.status_code)
        assert (response.status_code == 200)
        
        self.token = response.json()['access_token']
        
        return self.token
    
    def api_call(self, path:str, type="GET",  retry=True, **kwargs) :
        assert self.token
        if type == "GET" : 
            response = requests.get(
                self.URL + path,
                headers= {"Authorization" : "Bearer "+self.token},
                **kwargs)
        else : 
            response = requests.post(
                self.URL+path,
                headers= {"Authorization" : "Bearer "+self.token},
                **kwargs)
        if response.status_code == 401 and retry : 
            self.authenticate()
            return self.get_auth_paths(path, type, False, **kwargs)
        
        assert response.status_code == 200

        return response

    def get_info (self) -> Dict :
        response = self.api_call("/bin/info")

        return response.json()
    
    def get_user (self, email:str) -> Dict : 
        response = self.api_call("/bin/user/info",
            params={
                "email" : email
            })

        return response.json()

    def get_wastes (self) -> List[Dict]: 
        # [
        #     {
        #     "type": "string",
        #     "credit": 0
        #     }
        # ]
        assert self.token
        response = self.api_call("/info/waste")
        
        return response.json()['types']

    
    def add_transaction(self, email:str, waste_type:str, weight:float) -> Dict:
        # {
        #     "email": "string",
        #     "bin_uid": "string",
        #     "waste_type": "string",
        #     "weight": 0,
        #     "uid": "string",
        #     "credits": "string"
        # }
        assert self.token
        response = self.api_call("/bin/transaction/deposit",
            type="POST",
            json={
                "email": email,
                "bin_uid": self.username,
                "waste_type": waste_type,
                "weight": weight
            })

        return response.json()
        

if __name__ == "__main__" : 
    s = Connector()
    s.authenticate()
    print(
        s.get_user("@gmail.com")
        )