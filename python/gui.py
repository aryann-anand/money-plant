from asyncio import Handle
from cmath import exp
from email import message
from email.header import Header
from queue import Queue
from tkinter import *
from typing import Dict
from PIL import ImageTk, Image
from gui_events import *

import os
import config

class StatusWidget (Frame) :

    Header1 = ("Courier", 30)
    Header2 = ("Courier", 20)
    Header3 = ("Courier", 15)

    Body = ("Courier", 12)

    def __init__ (self, parent, label, default_status: StringVar, img=None) :
        Frame.__init__(self, parent)
        self.columnconfigure(0, weight=1)
        self.columnconfigure(1, weight=3)
        
        self.image = Image.open(os.path.join(config.MEDIA_DIR, img)).resize((75, 75), Image.Resampling.LANCZOS)
        self.image = ImageTk.PhotoImage(self.image)
        self.icon = Label(self, image=self.image)
        self.icon.grid(rowspan=2, column=0)

        self.label = Label(self, text=label, font=self.Header2, justify="left")
        self.label.grid(row=0, column=1)

        self.status = Label(self, textvariable=default_status, font=("Courier", 20, 'bold'), justify="left", wraplength=400)
        self.status.grid(row=1, column=1)
    


class GUI :
    AVAILABLE = 0
    ENGAGED = 1
    LID_CLOSED = 2
    LID_OPEN = 3
    ANALYZING = 4
    SERVICE = -1

    Header1 = ("Courier", 30, "bold")
    Header2 = ("Courier", 20)
    Header3 = ("Courier", 15)

    Body = ("Courier", 12)
    

    def __init__(s, q:Queue) -> None:

        s.root = Tk() 
        s.root.geometry("1024x600+0+0")
        s.root.title("MoneyPlant - Smart Bin")
        s.queue = q

        s.state = s.AVAILABLE

        s.uid_state = StringVar()
        s.bin_lid_status = StringVar()
        s.bin_lid_status.set("Closed")
        s.connection_status = StringVar()
        s.connection_status.set("Connected")
        s.weight_reading = StringVar() 
        s.waste_level = StringVar()
        s.waste_level.set("Empty")

        s.user_name = StringVar()
        s.user_email = StringVar()
        s.user_credits = StringVar()
        s.aadhar_id = StringVar()

        s.frames = {
        SET_INFO_SCREEN: s.setup_info_screen,
        SET_OUT_OF_ORDER_SCREEN: s.setup_out_of_order_screen,
        SET_WELCOME_SCREEN: s.setup_welcome_screen,
        SET_TRANSACTION_SCREEN: s.setup_transcation_screen,
        SET_ANALYSIS_RESULT_SCREEN: s.setup_analysis_result,
        SET_TRANSACTION_RECIEPT_SCREEN: s.setup_transaction_reciept_screen,
        SET_BYE_SCREEN: s.setup_goodbye_screen,
        }

        s.variables = {
            USER_NAME: s.user_name,
            USER_AADHAAR: s.aadhar_id,
            USER_CREDITS: s.user_credits,
            USER_EMAIL: s.user_email,
            BIN_UID: s.uid_state,
            BIN_LID_STATE: s.bin_lid_status,
            BIN_FILL_LEVEL: s.waste_level,
            TRANSACTION_TARGET_WASTE_TYPE: StringVar(value="Awaiting User Input"),
            TRANSACTION_WASTE_TYPE: StringVar(),
            TRANSACTION_WEIGHT: StringVar(),
            BIN_CONVERSION_RATES: StringVar(),
            MESSAGE_VARIABLE: StringVar(),
            TRANSACTION_STATE: StringVar(),
            TRANSACTION_CREDTIS: StringVar()
                
        }
        s.listen_for_updates()

        s.current_frame: Frame = None

    def set_frame (s, screen:str) :
        if s.current_frame :
            s.current_frame.destroy()
        s.current_frame = s.frames[screen]()
                 

    def setup_out_of_order_screen(s) : 
        s.warning_screen = Frame(s.root)
        s.warning_screen.pack(fill=BOTH, expand=True)

        s.warning_screen.grid_columnconfigure(0, weight=1)
        # s.warning_screen.grid_rowconfigure(0, weight=2)
        # s.warning_screen.grid_rowconfigure(1, weight=1)
        # s.warning_screen.grid_rowconfigure(2, weight=3)
        header = Label(s.root, text="Sorry the bin is out of service !", font=s.Header1)
        header.grid(column=0, row=0)

        info_text = Label(s.warning_screen, text="The bin is awaiting maintenance, the status has been updated with the authorities", font=("Courier", 20), wraplength=1000, justify="center")
        info_text.grid(row=1, column=0)
        s.thank_you = ImageTk.PhotoImage(Image.open("/home/pi/Code/MoneyPlant/images/out_of_order.png").resize((400, 400)))
        image = Label(image=s.thank_you)
        image.grid(row=2, column=0)

        return s.warning_screen
  
    def setup_info_screen(s) : 
        s.info_screen = Frame(s.root)
        s.info_screen.pack(fill=BOTH, expand=True)


        s.info_screen.columnconfigure(0, weight=1)
        s.info_screen.columnconfigure(1, weight=1)
        s.info_screen.rowconfigure(0, weight=2)
        s.info_screen.rowconfigure(1, weight=1)
        s.info_screen.rowconfigure(2, weight=1)
        s.info_screen.rowconfigure(3, weight=1)
        # s.info_screen.rowconfigure(4, weight=1)


        header = Label(s.info_screen, text="Hello " + s.user_name.get() + ", you're connected to the Bin", font=s.Header1, wraplength=1000, justify="center")
        header.grid(row=0, columnspan=2)

        # Bin Information 

        connection_status = StatusWidget(s.info_screen, "Connection", s.connection_status, "connection.png")
        connection_status.grid(row=1, column=0, sticky = "nsew")
        
        bin_uid_status = StatusWidget(s.info_screen, "Bin UID", s.variables[BIN_UID], "uid.png")
        bin_uid_status.grid(row=2, column=0, sticky = "nsew")

        conversion_rate = StatusWidget(s.info_screen, "Waste Conversion Rate", s.variables[BIN_CONVERSION_RATES], "conversion.png")
        conversion_rate.grid(row=3, column=0, sticky="nsew")

        # User Information
        email_status = StatusWidget(s.info_screen, "Email", s.user_email, "email.png")
        email_status.grid(row=1, column=1, sticky=NSEW)
        credits_balance = StatusWidget(s.info_screen, "Credits Balance", s.variables[USER_CREDITS], "credits.png")
        credits_balance.grid(row=2, column=1, sticky=NSEW)
        aadhar_id = StatusWidget(s.info_screen, "Aadhaar ID", s.variables[USER_AADHAAR], "aadhaar.png")
        aadhar_id.grid(row=3, column=1, sticky=NSEW)

        return s.info_screen

    def setup_transcation_screen (s) : 
        transaction_screen = Frame(s.root)
        transaction_screen.pack(fill=BOTH, expand=True)

        transaction_screen.columnconfigure(0, weight=1)
        transaction_screen.columnconfigure(1, weight=1)
        transaction_screen.rowconfigure(0, weight=1)
        # transaction_screen.rowconfigure(1, weight=1)
        transaction_screen.rowconfigure(2, weight=1)
        transaction_screen.rowconfigure(3, weight=1)
        transaction_screen.rowconfigure(4, weight=1)

        welcome_text = Label(transaction_screen, text="Transactional Information", font=("Courier", 30))
        welcome_text.grid(columnspan=2, row=0, sticky=EW)

        message_text = Label(transaction_screen, textvariable=s.variables[MESSAGE_VARIABLE], font=("Courier", 20, "bold"), fg="red")
        message_text.grid(columnspan=2, row=1, sticky=NSEW)

        connection_status = StatusWidget(transaction_screen, "Connection", s.connection_status, "connection.png")
        connection_status.grid(row=2, column=0, sticky = "nsew")
        
        bin_uid_status = StatusWidget(transaction_screen, "Bin UID", s.variables[BIN_UID], "uid.png")
        bin_uid_status.grid(row=3, column=0, sticky = "nsew")
        
        conversion_rate = StatusWidget(transaction_screen, "Waste Conversion Rate", s.variables[BIN_CONVERSION_RATES], "conversion.png")
        conversion_rate.grid(row=4, column=0, sticky="nsew")

        
        bin_door_status = StatusWidget(transaction_screen, "Bin Lid State", s.variables[BIN_LID_STATE], "bin_lid.png")
        bin_door_status.grid(row=2, column=1, sticky="nsew")

        target_waste = StatusWidget(transaction_screen, "Target Waste Type", s.variables[TRANSACTION_TARGET_WASTE_TYPE], "types.png")
        target_waste.grid(row=3, column=1, sticky="nsew")

        waste_level  = StatusWidget(transaction_screen, "Bin Fill Level", s.variables[BIN_FILL_LEVEL], "waste_level.png")
        waste_level.grid(row=4, column=1, sticky = "nsew")

        return transaction_screen

    def setup_analysis_result (s) : 
        analysis_report_screen = Frame(s.root)
        analysis_report_screen.pack(fill=BOTH, expand=True)

        analysis_report_screen.columnconfigure(0, weight=1)
        analysis_report_screen.columnconfigure(1, weight=1)
        analysis_report_screen.rowconfigure(0, weight=1)
        # transaction_screen.rowconfigure(1, weight=1)
        analysis_report_screen.rowconfigure(2, weight=1)
        analysis_report_screen.rowconfigure(3, weight=1)
        analysis_report_screen.rowconfigure(4, weight=1)

        welcome_text = Label(analysis_report_screen, text="Analysis Result", font=("Courier", 30))
        welcome_text.grid(columnspan=2, row=0, sticky=EW)

        message_text = Label(analysis_report_screen, textvariable=s.variables[MESSAGE_VARIABLE], font=("Courier", 20, "bold"), fg="red")
        message_text.grid(columnspan=2, row=1, sticky=NSEW)

        target_waste = StatusWidget(analysis_report_screen, "Weight ", s.variables[TRANSACTION_WEIGHT], "waste_weight.png")
        target_waste.grid(row=2, column=0, sticky="nsew")

        target_waste = StatusWidget(analysis_report_screen, "Target Waste Type", s.variables[TRANSACTION_TARGET_WASTE_TYPE], "types.png")
        target_waste.grid(row=3, column=0, sticky="nsew")

        analysis_result = StatusWidget(analysis_report_screen, "Analysis Result", s.variables[TRANSACTION_STATE], "analysis.png")
        analysis_result.grid(row=4, column=0, sticky=NSEW)
        
        estimated_credits = StatusWidget(analysis_report_screen, "Estimated Credits Earned", s.variables[BIN_CONVERSION_RATES], "conversion.png")
        estimated_credits.grid(row=2, column=1, sticky=NSEW)
        
        bin_door_status = StatusWidget(analysis_report_screen, "Bin Lid State", s.variables[BIN_LID_STATE], "bin_lid.png")
        bin_door_status.grid(row=3, column=1, sticky="nsew")

        waste_level  = StatusWidget(analysis_report_screen, "Bin Fill Level", s.variables[BIN_FILL_LEVEL], "waste_level.png")
        waste_level.grid(row=4, column=1, sticky = "nsew")

        return analysis_report_screen

    def setup_transaction_reciept_screen (s) :
        transaction_reciept_screen = Frame(s.root)
        transaction_reciept_screen.pack(fill=BOTH, expand=True)

        transaction_reciept_screen.columnconfigure(0, weight=1)
        transaction_reciept_screen.columnconfigure(1, weight=1)
        transaction_reciept_screen.rowconfigure(0, weight=1)
        # transaction_screen.rowconfigure(1, weight=1)
        transaction_reciept_screen.rowconfigure(2, weight=1)
        transaction_reciept_screen.rowconfigure(3, weight=1)
        transaction_reciept_screen.rowconfigure(4, weight=1)

        header = Label(transaction_reciept_screen, text="Transaction Reciept", font=("Courier", 30))
        header.grid(columnspan=2, row=0, sticky=EW)
        

        message_text = Label(transaction_reciept_screen, textvariable=s.variables[MESSAGE_VARIABLE], font=("Courier", 20, "bold"), fg="red")
        message_text.grid(columnspan=2, row=1, sticky=NSEW)

        target_waste = StatusWidget(transaction_reciept_screen, "Weight ", s.variables[TRANSACTION_WEIGHT], "waste_weight.png")
        target_waste.grid(row=2, column=0, sticky="nsew")

        target_waste = StatusWidget(transaction_reciept_screen, "Transaction Waste Type", s.variables[TRANSACTION_WASTE_TYPE], "types.png")
        target_waste.grid(row=3, column=0, sticky="nsew")

        analysis_result = StatusWidget(transaction_reciept_screen, "Analysis Result", s.variables[TRANSACTION_STATE], "analysis.png")
        analysis_result.grid(row=4, column=0, sticky=NSEW)
        
        estimated_credits = StatusWidget(transaction_reciept_screen, "Estimated Credits Earned", s.variables[TRANSACTION_CREDTIS], "conversion.png")
        estimated_credits.grid(row=2, column=1, sticky=NSEW)
        
        bin_door_status = StatusWidget(transaction_reciept_screen, "Bin Lid State", s.variables[BIN_LID_STATE], "bin_lid.png")
        bin_door_status.grid(row=3, column=1, sticky="nsew")

        waste_level  = StatusWidget(transaction_reciept_screen, "Bin Fill Level", s.variables[BIN_FILL_LEVEL], "waste_level.png")
        waste_level.grid(row=4, column=1, sticky = "nsew")

        return transaction_reciept_screen

    def setup_goodbye_screen (s) :
        goodbye_screen = Frame(s.root)
        goodbye_screen.pack(fill=BOTH, expand=True)

        goodbye_screen.grid_columnconfigure(0, weight=1)
        goodbye_screen.grid_rowconfigure(0, weight=2)
        # s.warning_screen.grid_rowconfigure(1, weight=1)
        goodbye_screen.grid_rowconfigure(2, weight=3)
        header = Label(goodbye_screen, text="Thank you for using Money Plant", font=s.Header1)
        header.grid(column=0, row=0)

        info_text = Label(goodbye_screen, text="This screen will automatically reset after 5 seconds", font=("Courier", 20), wraplength=1000, justify="center")
        info_text.grid(row=1, column=0)
        s.thank_you = ImageTk.PhotoImage(Image.open("/home/pi/Code/MoneyPlant/images/thank_you.png").resize((400, 400)))
        image = Label(goodbye_screen, image=s.thank_you)
        image.grid(row=2, column=0)

        s.root.after(5000, lambda : s.set_frame(SET_WELCOME_SCREEN))
        return goodbye_screen

    def setup_welcome_screen(s) :
        s.welcome_screen = Frame(s.root)
        s.welcome_screen.pack(fill=BOTH, expand=True)


        s.welcome_screen.grid_columnconfigure(0, weight=1)
        s.welcome_screen.grid_rowconfigure(0, weight=2)
        s.welcome_screen.grid_rowconfigure(1, weight=1)
        s.welcome_screen.grid_rowconfigure(2, weight=5)
        
        welcome_text = Label(s.welcome_screen, text="Welcome to Smart Bin !", font=("Courier", 30))
        welcome_text.grid(column=0, row=0, sticky=EW)

        helper_text = Label(s.welcome_screen, text="Use the Moneyplant app to scan the QR Code", font=("Courier", 20))
        helper_text.grid(row=1, column=0)

        s.qr_code = ImageTk.PhotoImage(Image.open("/home/pi/Code/MoneyPlant/qrcode.png"))
        label_qr_code = Label(s.welcome_screen, image=s.qr_code)
        label_qr_code.grid(row=2, column=0)

        return s.welcome_screen
        
    def listen_for_updates(s) -> None : 
        try : 
            res:Dict = s.queue.get(0)
            print(res)
            event = res.get("event")
            message = res.get("message")
            if (event == SET_SCREEN) : 
                s.set_frame(message)
            
            elif event == UPDATE_VALUE : 
                name, value = message
                s.variables[name].set(value)

            s.root.after(100, s.listen_for_updates)
        except: 
            s.root.after(100, s.listen_for_updates)

    def run(s) -> None : 
        s.root.mainloop()
    
    



if __name__ == "__main__" : 
    g = GUI(Queue())
    g.set_frame(SET_BYE_SCREEN)
    g.run()

