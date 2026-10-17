from lexer import Lexer
from parser import Parser
from interpreter import Interpreter
from data import Data

data = Data()

print("FunScript :")

while True:
    try:
        text = input()
    except (KeyboardInterrupt, EOFError):
        print("\nBye!")
        break

    if text.strip() == "exit":
        break

    try:
        tokens = Lexer(text).tokenize()
        tree = Parser(tokens).parse()
        print(Interpreter(tree, data).interpret())
    except (ValueError, ZeroDivisionError) as error:
        print("Error:", error)