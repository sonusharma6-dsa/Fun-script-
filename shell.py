from lexer import Lexer
from parser import Parser
from interpreter import Interpreter
while True:
    text = input("ShadowScript : ")
    if text.strip() == "exit":
        break
    try:
        tokens = Lexer(text).tokenize()
        tree = Parser(tokens).parse()
        print(Interpreter(tree).interpret())
    except (ValueError, ZeroDivisionError) as error:
        print("Error:", error)