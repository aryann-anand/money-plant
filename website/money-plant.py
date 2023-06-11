from flask import Flask,request,render_template
import pickle

app = Flask(__name__)


#Login Screem
@app.route('/')
def index():
    return render_template ("Login.html")

#Database
database = {"Manu Mishra":"12345", "Vishu Mishra":"5555", "Shreya Mishra":"0000"} 

#Login Authentication
@app.route('/form_login', methods = ['POST','GET'])
def login():
    name_input = request.form['username']
    pwd        = request.form['password']

    if name_input  not in database:
        return render_template('Invalid_user.html')
    else:
        if database[name_input]!=pwd:
            return render_template('Invalid_pwd.html')  

        else:
            return render_template('Home.html',Name = name_input )      


#Cart Screen
@app.route('/Cart')
def Cart():
    return render_template ("Cart.html")

#Bin Screem
@app.route('/Bin')
def Bin():
    return render_template ("Bin.html")

#Bin Status Screem
@app.route('/Smart_bin')
def Smart_bin():
    return render_template ("Smart_bin.html")

#Bin Waste Status Screem
@app.route('/Waste_details')
def Bin_waste():
    return render_template ("Waste_details.html")    

#Running Command
if __name__ == '__main__':
    app.run()

