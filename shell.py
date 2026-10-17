from lexer import Lexer
from parser import Parser
while True:
    text = input("ShadowScript : ")
    if text.strip() == "exit":
        break
    try:
        tokens = Lexer(text).tokenize()
        parse =  Parser(tokens).
        print(tokens)
    except Exception as e:
        print("Error:", e)